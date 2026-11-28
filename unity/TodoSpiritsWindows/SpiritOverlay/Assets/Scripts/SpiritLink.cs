using System;
using System.Collections.Concurrent;
using System.Diagnostics;
using System.IO;
using System.IO.Pipes;
using System.Threading;
using System.Threading.Tasks;
using TodoSpirits.Contracts;

namespace TodoSpirits.Spirit
{
    // No Unity APIs on worker threads. Main thread supplies both heartbeat and click events.
    public sealed class SpiritLink : IDisposable
    {
        readonly ConcurrentQueue<SpiritMessage> outgoing = new ConcurrentQueue<SpiritMessage>();
        readonly ConcurrentQueue<SpiritMessage> incoming = new ConcurrentQueue<SpiritMessage>();
        readonly CancellationTokenSource stop = new CancellationTokenSource();
        readonly SemaphoreSlim pending = new SemaphoreSlim(0);
        readonly string name;
        readonly Process parent;
        NamedPipeClientStream pipe;
        Task worker;
        volatile bool failed, connected;
        public bool Connected => connected;
        public bool ShouldQuit => failed || (parent != null && parent.HasExited);

        public SpiritLink(string pipeName, int parentId)
        {
            name = pipeName;
            parent = Process.GetProcessById(parentId);
            worker = Task.Run(RunAsync);
        }
        public void Send(SpiritMessage message)
        { if (outgoing.Count < 64 && !stop.IsCancellationRequested) { outgoing.Enqueue(message); pending.Release(); } }
        public bool TryReceive(out SpiritMessage message) => incoming.TryDequeue(out message);

        async Task RunAsync()
        {
            try
            {
                using (pipe = new NamedPipeClientStream(".", name, PipeDirection.InOut, PipeOptions.Asynchronous))
                {
                    await pipe.ConnectAsync(15000, stop.Token).ConfigureAwait(false);
                    await WriteAsync(new SpiritMessage { Kind = SpiritMessageKind.Hello, SpiritId = "spirit-1" }, stop.Token).ConfigureAwait(false);
                    var ready = await ReadAsync(stop.Token).ConfigureAwait(false);
                    if (ready.Kind != SpiritMessageKind.Ready) throw new InvalidDataException("Missing WPF handshake.");
                    connected = true;
                    var reader = ReadLoopAsync(stop.Token);
                    var writer = WriteLoopAsync(stop.Token);
                    await Task.WhenAny(reader, writer).ConfigureAwait(false);
                    stop.Cancel(); pipe.Dispose();
                    await Task.WhenAll(reader, writer).ConfigureAwait(false);
                }
            }
            catch (Exception exception) when (exception is IOException || exception is OperationCanceledException || exception is ObjectDisposedException)
            { failed = !stop.IsCancellationRequested; }
            finally { connected = false; failed = true; }
        }
        async Task ReadLoopAsync(CancellationToken token)
        {
            while (!token.IsCancellationRequested)
            {
                var message = await ReadAsync(token).ConfigureAwait(false);
                if (message.Kind != SpiritMessageKind.Shutdown && message.Kind != SpiritMessageKind.Visibility)
                    throw new InvalidDataException("Unexpected WPF message.");
                if (incoming.Count >= 32) throw new InvalidDataException("WPF message queue overflow.");
                incoming.Enqueue(message);
            }
        }
        async Task WriteLoopAsync(CancellationToken token)
        {
            while (!token.IsCancellationRequested)
            {
                await pending.WaitAsync(token).ConfigureAwait(false);
                while (outgoing.TryDequeue(out var message)) await WriteAsync(message, token).ConfigureAwait(false);
            }
        }
        async Task WriteAsync(SpiritMessage message, CancellationToken token)
        {
            byte[] bytes = SpiritWire.Encode(message);
            await pipe.WriteAsync(bytes, 0, bytes.Length, token).ConfigureAwait(false);
            await pipe.FlushAsync(token).ConfigureAwait(false);
        }
        async Task<SpiritMessage> ReadAsync(CancellationToken token)
        {
            var prefix = new byte[4]; await ReadExactlyAsync(prefix, token).ConfigureAwait(false);
            int length = prefix[0] | prefix[1] << 8 | prefix[2] << 16 | prefix[3] << 24;
            if (length <= 0 || length > SpiritWire.MaxPayloadBytes) throw new InvalidDataException("Invalid frame size.");
            var payload = new byte[length]; await ReadExactlyAsync(payload, token).ConfigureAwait(false);
            return SpiritWire.Decode(payload);
        }
        async Task ReadExactlyAsync(byte[] buffer, CancellationToken token)
        {
            int offset = 0;
            while (offset < buffer.Length)
            {
                int read = await pipe.ReadAsync(buffer, offset, buffer.Length - offset, token).ConfigureAwait(false);
                if (read == 0) throw new EndOfStreamException();
                offset += read;
            }
        }
        public void Dispose()
        {
            stop.Cancel(); pipe?.Dispose();
            try { worker?.Wait(500); } catch (AggregateException) { }
            // The cancellation source remains alive until its pending IO continuations finish.
            if (worker == null || worker.IsCompleted) { stop.Dispose(); pending.Dispose(); }
            else worker.ContinueWith(_ => { stop.Dispose(); pending.Dispose(); }, TaskScheduler.Default);
            parent?.Dispose();
        }
    }
}
