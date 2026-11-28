using System.IO;
using System.Text.Encodings.Web;
using System.Text.Json;
using TodoSpirits.Windows.Models;

namespace TodoSpirits.Windows.Services;

/// <summary>
/// 일정과 설정을 %LOCALAPPDATA%\TodoWithSpirits\data.json 에 저장한다.
/// 임시 파일에 쓰고 바꿔 끼워서, 쓰는 도중 꺼져도 기존 파일이 깨지지 않게 한다.
/// </summary>
public sealed class TaskStore
{
    private static readonly JsonSerializerOptions Options = new()
    {
        WriteIndented = true,
        Encoder = JavaScriptEncoder.UnsafeRelaxedJsonEscaping,
    };

    public string FilePath { get; }

    public TaskStore(string? filePath = null)
    {
        // 시험할 때 실제 일정 파일을 건드리지 않도록 환경 변수로 위치를 바꿀 수 있다
        FilePath = filePath ?? Environment.GetEnvironmentVariable("TODOSPIRITS_DATA") ?? Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
            "TodoWithSpirits", "data.json");
    }

    /// <summary>파일이 없으면 빈 데이터. 읽지 못하면 원본을 .broken 으로 남기고 빈 데이터로 시작한다.</summary>
    public AppData Load(out string? warning)
    {
        warning = null;
        if (!File.Exists(FilePath)) return new AppData();
        try
        {
            return JsonSerializer.Deserialize<AppData>(File.ReadAllText(FilePath), Options) ?? new AppData();
        }
        catch (Exception ex) when (ex is JsonException or IOException or UnauthorizedAccessException)
        {
            var backup = FilePath + ".broken-" + DateTime.Now.ToString("yyyyMMdd-HHmmss");
            try { File.Copy(FilePath, backup, overwrite: true); } catch (IOException) { }
            warning = $"저장 파일을 읽지 못해 빈 상태로 시작했어요. 원본은 {Path.GetFileName(backup)} 로 남겨 두었어요.";
            return new AppData();
        }
    }

    public void Save(AppData data)
    {
        Directory.CreateDirectory(Path.GetDirectoryName(FilePath)!);
        var tmp = FilePath + ".tmp";
        File.WriteAllText(tmp, JsonSerializer.Serialize(data, Options));
        File.Move(tmp, FilePath, overwrite: true);
    }
}
