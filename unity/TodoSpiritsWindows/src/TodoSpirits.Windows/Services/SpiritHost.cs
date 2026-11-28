using System.Diagnostics;
using System.IO;
using System.IO.Pipes;
using System.Runtime.InteropServices;
using Microsoft.Win32.SafeHandles;
using TodoSpirits.Contracts;

namespace TodoSpirits.Windows.Services;

/// <summary>정령을 눌렀을 때의 위치. 모니터의 실제 픽셀, 왼쪽 위 기준.</summary>
public sealed record SpiritClick(bool IsMenu, double X, double Y, double Width, double Height);

/// <summary>연결 상태. State 는 Stopped / Missing / Starting / Connected / Retrying / Failed.</summary>
public sealed record SpiritStatus(string State, string Detail);

/// <summary>
/// 바탕화면 정령(Unity 플레이어)을 띄우고 감시한다.
/// - 이 앱만 접속할 수 있는 Named Pipe 로 눌림·생존 신호를 받고, 종료·표시 전환을 보낸다.
/// - 응답이 끊기면 최대 3번 다시 띄운다. 앱을 끄면 정령도 함께 끈다.
/// 파이프 입출력과 프로세스 대기는 모두 UI 스레드 밖에서 돈다.
/// </summary>
public sealed class SpiritHost(string executablePath, string logDirectory) : IAsyncDisposable
{
    private static readonly TimeSpan ConnectTimeout = TimeSpan.FromSeconds(20);
    private static readonly TimeSpan HeartbeatTimeout = TimeSpan.FromSeconds(10);

    private readonly SemaphoreSlim _lifecycle = new(1, 1);
    private readonly SemaphoreSlim _writer = new(1, 1);
    private CancellationTokenSource? _stop;
    private Task? _run;
    private NamedPipeServerStream? _pipe;
    private SpiritStatus _status = new("Stopped", "정령이 꺼져 있어요.");

    public string ExecutablePath { get; } = executablePath;
    public SpiritStatus Status => Volatile.Read(ref _status);
    public event Action<SpiritClick>? Clicked;
    public event Action<SpiritStatus>? StatusChanged;

    public async Task StartAsync()
    {
        await _lifecycle.WaitAsync().ConfigureAwait(false);
        try
        {
            if (_run is { IsCompleted: false }) return;
            _stop?.Dispose();
            _stop = new CancellationTokenSource();
            var token = _stop.Token;
            _run = Task.Run(() => SuperviseAsync(token), CancellationToken.None);
        }
        finally { _lifecycle.Release(); }
    }

    public async Task StopAsync()
    {
        await _lifecycle.WaitAsync().ConfigureAwait(false);
        try
        {
            if (_stop is not null) await _stop.CancelAsync().ConfigureAwait(false);
            if (_run is not null) await _run.ConfigureAwait(false);
        }
        finally { _lifecycle.Release(); }
    }

    /// <summary>정령을 잠깐 숨기거나 다시 보이게 한다 (프로세스는 유지).</summary>
    public Task SetVisibleAsync(bool visible) =>
        SendAsync(new SpiritMessage { Kind = SpiritMessageKind.Visibility, Visible = visible });

    private void Report(string state, string detail)
    {
        var value = new SpiritStatus(state, detail);
        Volatile.Write(ref _status, value);
        StatusChanged?.Invoke(value);
    }

    private async Task SuperviseAsync(CancellationToken token)
    {
        if (!File.Exists(ExecutablePath))
        {
            Report("Missing", "정령 실행 파일이 없어요. Build-Spirit.ps1 로 먼저 빌드해 주세요.");
            return;
        }
        for (int restart = 0; !token.IsCancellationRequested; restart++)
        {
            Process? child = null;
            NamedPipeServerStream? pipe = null;
            string failure = "정령 연결이 끊겼어요.";
            try
            {
                string pipeName = "TodoSpirits.Spirit." + Guid.NewGuid().ToString("N");
                pipe = new NamedPipeServerStream(pipeName, PipeDirection.InOut, 1, PipeTransmissionMode.Byte,
                    PipeOptions.Asynchronous | PipeOptions.CurrentUserOnly);
                Directory.CreateDirectory(logDirectory);
                // UseShellExecute 를 쓰면 정령이 뜰 때까지 Start 가 돌아오지 않아 첫 창을 치울 틈이 없다. 직접 띄운다.
                var start = new ProcessStartInfo(ExecutablePath)
                {
                    UseShellExecute = false, WorkingDirectory = Path.GetDirectoryName(ExecutablePath)!,
                };
                foreach (string arg in new[]
                {
                    "-force-d3d11", "-logFile", Path.Combine(logDirectory, "spirit.log"),
                    "--spirit-pipe", pipeName, "--spirit-parent", Environment.ProcessId.ToString(System.Globalization.CultureInfo.InvariantCulture),
                }) start.ArgumentList.Add(arg);
                // 시험용: TODOSPIRITS_SPIRIT_ARGS 에 적은 인자를 정령에게 그대로 넘긴다 (예: --spirit-verify-click)
                foreach (string arg in (Environment.GetEnvironmentVariable("TODOSPIRITS_SPIRIT_ARGS") ?? "").Split(' ', StringSplitOptions.RemoveEmptyEntries))
                    start.ArgumentList.Add(arg);
                token.ThrowIfCancellationRequested();
                child = Process.Start(start) ?? throw new IOException("정령 프로세스를 시작하지 못했어요.");
                Report("Starting", "정령을 부르고 있어요…");
                using (var connect = CancellationTokenSource.CreateLinkedTokenSource(token))
                {
                    connect.CancelAfter(ConnectTimeout);
                    using var watching = CancellationTokenSource.CreateLinkedTokenSource(connect.Token);
                    var watcher = Task.Run(() => KeepFirstWindowOffscreenAsync(child.Id, watching.Token), CancellationToken.None);
                    using var stopWatching = new DeferredCancel(watching, watcher);
                    await pipe.WaitForConnectionAsync(connect.Token).ConfigureAwait(false);
                    if (!GetNamedPipeClientProcessId(pipe.SafePipeHandle, out uint pid) || pid != child.Id)
                        throw new IOException("다른 프로그램이 정령 연결에 접속했어요.");
                    var hello = await ReadAsync(pipe, connect.Token).ConfigureAwait(false);
                    if (hello.Kind != SpiritMessageKind.Hello) throw new InvalidDataException("정령의 첫 인사가 없어요.");
                    await WriteAsync(pipe, new SpiritMessage { Kind = SpiritMessageKind.Ready }, connect.Token).ConfigureAwait(false);
                }
                Volatile.Write(ref _pipe, pipe);
                Report("Connected", "정령이 바탕화면에 나와 있어요.");
                while (!token.IsCancellationRequested)
                {
                    using var heartbeat = CancellationTokenSource.CreateLinkedTokenSource(token);
                    heartbeat.CancelAfter(HeartbeatTimeout);
                    var message = await ReadAsync(pipe, heartbeat.Token).ConfigureAwait(false);
                    if (message.Kind is SpiritMessageKind.Clicked or SpiritMessageKind.MenuRequested)
                    {
                        if (message.Width <= 0 || message.Height <= 0) throw new InvalidDataException("정령 위치가 빠졌어요.");
                        Clicked?.Invoke(new SpiritClick(message.Kind == SpiritMessageKind.MenuRequested, message.X, message.Y, message.Width, message.Height));
                    }
                    else if (message.Kind != SpiritMessageKind.Heartbeat) throw new InvalidDataException("알 수 없는 정령 메시지예요.");
                }
            }
            catch (Exception exception) when (exception is not OutOfMemoryException)
            {
                failure = exception is OperationCanceledException ? "정령이 응답하지 않아요." : exception.Message;
            }
            finally
            {
                // 먼저 정상 종료를 부탁하고, 3초 안에 안 꺼지면 강제로 끈다.
                if (child is not null)
                {
                    if (!child.HasExited)
                    {
                        using var grace = new CancellationTokenSource(TimeSpan.FromSeconds(3));
                        try
                        {
                            if (pipe is { IsConnected: true })
                            {
                                await _writer.WaitAsync(grace.Token).ConfigureAwait(false);
                                try { await WriteAsync(pipe, new SpiritMessage { Kind = SpiritMessageKind.Shutdown }, grace.Token).ConfigureAwait(false); }
                                finally { _writer.Release(); }
                            }
                            await child.WaitForExitAsync(grace.Token).ConfigureAwait(false);
                        }
                        catch (Exception exception) when (exception is IOException or OperationCanceledException or InvalidOperationException) { }
                        if (!child.HasExited) { child.Kill(entireProcessTree: true); await child.WaitForExitAsync().ConfigureAwait(false); }
                    }
                    child.Dispose();
                }
                await _writer.WaitAsync().ConfigureAwait(false);
                try { Volatile.Write(ref _pipe, null); pipe?.Dispose(); }
                finally { _writer.Release(); }
            }
            if (token.IsCancellationRequested) break;
            if (restart >= 3) { Report("Failed", "정령을 세 번 다시 불렀지만 나오지 않았어요. " + failure); return; }
            int seconds = 1 << restart; // 1, 2, 4초
            Report("Retrying", $"{seconds}초 뒤에 정령을 다시 불러요. {failure}");
            try { await Task.Delay(TimeSpan.FromSeconds(seconds), token).ConfigureAwait(false); }
            catch (OperationCanceledException) { break; }
        }
        Report("Stopped", "정령이 꺼져 있어요.");
    }

    /// <summary>
    /// Unity 는 켜지자마자 테두리 있는 일반 창을 잠깐 보여 준다(투명 오버레이로 바뀌기 전, 0.2초쯤).
    /// 정령이 첫 인사를 보낼 때까지 그 창을 지켜보다가, 아직 투명 창이 되기 전(WS_EX_LAYERED 없음)에
    /// 화면에 나타나면 곧바로 화면 밖으로 옮긴다. 정령은 준비가 끝나면 스스로 작업 영역으로 돌아와 나타난다.
    /// </summary>
    private static async Task KeepFirstWindowOffscreenAsync(int processId, CancellationToken token)
    {
        const int GWL_EXSTYLE = -20;
        const long WS_EX_LAYERED = 0x80000;
        const uint SWP_NOSIZE = 0x1, SWP_NOZORDER = 0x4, SWP_NOACTIVATE = 0x10;
        try
        {
            while (!token.IsCancellationRequested)
            {
                EnumWindows((window, _) =>
                {
                    GetWindowThreadProcessId(window, out uint owner);
                    if (owner == processId && IsWindowVisible(window)
                        && (GetWindowLongPtr(window, GWL_EXSTYLE).ToInt64() & WS_EX_LAYERED) == 0
                        && GetWindowRect(window, out var rect) && rect.Left > -30000)
                        SetWindowPos(window, IntPtr.Zero, -32000, -32000, 0, 0, SWP_NOSIZE | SWP_NOZORDER | SWP_NOACTIVATE);
                    return true;
                }, IntPtr.Zero);
                await Task.Delay(2, token).ConfigureAwait(false);
            }
        }
        catch (OperationCanceledException) { }
    }

    private delegate bool EnumWindowsProc(IntPtr window, IntPtr data);
    [StructLayout(LayoutKind.Sequential)] private struct WindowRect { public int Left, Top, Right, Bottom; }
    [DllImport("user32.dll")] private static extern bool EnumWindows(EnumWindowsProc visitor, IntPtr data);
    [DllImport("user32.dll")] private static extern uint GetWindowThreadProcessId(IntPtr window, out uint processId);
    [DllImport("user32.dll")] private static extern bool IsWindowVisible(IntPtr window);
    [DllImport("user32.dll", EntryPoint = "GetWindowLongPtrW")] private static extern IntPtr GetWindowLongPtr(IntPtr window, int index);
    [DllImport("user32.dll")] private static extern bool GetWindowRect(IntPtr window, out WindowRect rect);
    [DllImport("user32.dll")] private static extern bool SetWindowPos(IntPtr window, IntPtr after, int x, int y, int width, int height, uint flags);

    private async Task SendAsync(SpiritMessage message)
    {
        await _writer.WaitAsync().ConfigureAwait(false);
        try
        {
            var pipe = Volatile.Read(ref _pipe);
            if (pipe is { IsConnected: true }) await WriteAsync(pipe, message, CancellationToken.None).ConfigureAwait(false);
        }
        catch (IOException) { /* 끊긴 연결은 감시 루프가 처리한다 */ }
        finally { _writer.Release(); }
    }

    private static async Task<SpiritMessage> ReadAsync(Stream stream, CancellationToken token)
    {
        var prefix = new byte[4];
        await stream.ReadExactlyAsync(prefix, token).ConfigureAwait(false);
        int length = System.Buffers.Binary.BinaryPrimitives.ReadInt32LittleEndian(prefix);
        if (length <= 0 || length > SpiritWire.MaxPayloadBytes) throw new InvalidDataException("정령 메시지 길이가 잘못됐어요.");
        var payload = new byte[length];
        await stream.ReadExactlyAsync(payload, token).ConfigureAwait(false);
        return SpiritWire.Decode(payload);
    }

    private static async Task WriteAsync(Stream stream, SpiritMessage message, CancellationToken token)
    {
        await stream.WriteAsync(SpiritWire.Encode(message), token).ConfigureAwait(false);
        await stream.FlushAsync(token).ConfigureAwait(false);
    }

    /// <summary>블록을 벗어날 때(연결 성공이든 실패든) 창 지켜보기를 멈춘다.</summary>
    private sealed class DeferredCancel(CancellationTokenSource source, Task task) : IDisposable
    {
        public void Dispose()
        {
            source.Cancel();
            try { task.Wait(TimeSpan.FromSeconds(1)); } catch (AggregateException) { }
        }
    }

    public async ValueTask DisposeAsync()
    {
        await StopAsync().ConfigureAwait(false);
        _stop?.Dispose(); _lifecycle.Dispose(); _writer.Dispose();
    }

    [DllImport("kernel32.dll", SetLastError = true)]
    [return: MarshalAs(UnmanagedType.Bool)]
    private static extern bool GetNamedPipeClientProcessId(SafePipeHandle pipe, out uint clientProcessId);
}
