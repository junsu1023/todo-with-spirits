using System.Globalization;
using System.IO;
using System.Net;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text.Json;

namespace TodoSpirits.Windows.Services;

internal sealed class SpiritsApi : IDisposable
{
    private static readonly JsonSerializerOptions Json = new(JsonSerializerDefaults.Web);
    private readonly HttpClient _http;
    private Tokens? _tokens;

    public SpiritsApi()
    {
        var url = Environment.GetEnvironmentVariable("TODOSPIRITS_API_URL") ?? "https://todo-with-spirits.onrender.com";
        if (!Uri.TryCreate(url.TrimEnd('/') + "/", UriKind.Absolute, out var address) ||
            (address.Scheme != Uri.UriSchemeHttps && !(address.IsLoopback && address.Scheme == Uri.UriSchemeHttp)) ||
            address.UserInfo.Length > 0 || address.Query.Length > 0 || address.Fragment.Length > 0)
            throw new InvalidOperationException("API 주소는 HTTPS 주소여야 해요. 개발용 localhost에서는 HTTP도 쓸 수 있어요.");

        _http = new HttpClient(new HttpClientHandler { AllowAutoRedirect = false })
        {
            BaseAddress = address,
            Timeout = TimeSpan.FromSeconds(60),
        };
    }

    public async Task SignIn(string email, string password)
    {
        var tokens = await Send<Tokens>(HttpMethod.Post, "api/auth/login", new { email, password }, null);
        _tokens = ValidateTokens(tokens);
    }

    public Task<T> Get<T>(string path) => Request<T>(HttpMethod.Get, path);
    public async Task<ServerOccurrence?> GetTask(long id, DateOnly date)
    {
        var detail = await Get<JsonElement>($"{TaskPath(id)}?date={Day(date)}");
        return detail.ValueKind == JsonValueKind.Null ? null : detail.Deserialize<ServerOccurrence>(Json);
    }
    public Task<T> PublicGet<T>(string path) => Send<T>(HttpMethod.Get, path, null, null);
    public Task<T> PublicPost<T>(string path, object body) => Send<T>(HttpMethod.Post, path, body, null);
    public Task<T> Post<T>(string path, object? body = null) => Request<T>(HttpMethod.Post, path, body);
    public Task<T> Patch<T>(string path, object body) => Request<T>(HttpMethod.Patch, path, body);
    public Task Patch(string path, object? body = null) => Request<JsonElement>(HttpMethod.Patch, path, body);
    public Task Delete(string path, object? body = null) => Request<JsonElement>(HttpMethod.Delete, path, body);
    public Task SignOut() => Post<JsonElement>("api/auth/logout");

    public static string Day(DateOnly date) => date.ToString("yyyy-MM-dd", CultureInfo.InvariantCulture);
    public static string TaskPath(long id) => "api/task/" + id.ToString(CultureInfo.InvariantCulture);

    private async Task<T> Request<T>(HttpMethod method, string path, object? body = null)
    {
        if (_tokens is not { } tokens) throw new SessionExpiredException();
        try
        {
            return await Send<T>(method, path, body, tokens.AccessToken);
        }
        catch (SpiritsApiException ex) when (ex.Status == HttpStatusCode.Unauthorized)
        {
            try
            {
                var reissued = await Send<Tokens>(HttpMethod.Post, "api/auth/reissue", new { tokens.RefreshToken }, null);
                _tokens = ValidateTokens(reissued);
            }
            catch (SpiritsApiException refresh) when (refresh.Status == HttpStatusCode.Unauthorized)
            {
                _tokens = null;
                throw new SessionExpiredException();
            }
            return await Send<T>(method, path, body, _tokens.AccessToken);
        }
    }

    private async Task<T> Send<T>(HttpMethod method, string path, object? body, string? token)
    {
        using var request = new HttpRequestMessage(method, path);
        if (token is not null) request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", token);
        if (body is not null) request.Content = JsonContent.Create(body, options: Json);
        using var response = await _http.SendAsync(request);
        if (!response.IsSuccessStatusCode)
        {
            var code = "";
            try
            {
                var error = await response.Content.ReadFromJsonAsync<Envelope<ApiError>>(Json);
                code = error?.Detail?.ErrorCode ?? "";
            }
            catch (JsonException) { }
            throw new SpiritsApiException(response.StatusCode, code);
        }

        var envelope = await response.Content.ReadFromJsonAsync<Envelope<T>>(Json);
        if (envelope is not { Result: "success" }) throw new InvalidDataException("서버 응답 형식이 달라요.");
        if (envelope.Detail is { } detail) return detail;
        throw new InvalidDataException("서버 응답에 데이터가 없어요.");
    }

    private static Tokens ValidateTokens(Tokens tokens) =>
        !string.IsNullOrWhiteSpace(tokens.AccessToken) && !string.IsNullOrWhiteSpace(tokens.RefreshToken)
            ? tokens : throw new InvalidDataException("서버가 로그인 정보를 보내지 않았어요.");

    public void Dispose() => _http.Dispose();

    private sealed record Tokens(string AccessToken, string RefreshToken);
    private sealed record Envelope<T>(string Result, T? Detail);
    private sealed record ApiError(string ErrorCode);
}

internal sealed class SpiritsApiException(HttpStatusCode status, string code) : Exception($"API {(int)status} ({code})")
{
    public HttpStatusCode Status { get; } = status;
    public string Code { get; } = code;
}

internal sealed class SessionExpiredException : Exception;

internal sealed record ServerProfile(long UserId, string? Email, string? Nickname, string? Provider, string? EmailVerificationStatus = null);
internal sealed record ServerSubscription(string PlanType, DateTime? ExpiresAt);
internal sealed record EmailAvailability(bool Registered);
internal sealed record ServerCalendar(ServerOccurrence[] Items);
internal sealed record ServerOccurrence(
    long TaskId, string Title, DateOnly OccurrenceDate, TimeOnly? EndTime, bool IsAllDay,
    bool IsImportant, string RepeatType, DateOnly? RepeatEndDate, string[]? RepeatDaysOfWeek,
    int[]? RepeatDaysOfMonth, bool ExcludeHoliday, int? NotificationMinutes, bool IsCompleted, DateOnly? StartDate);
internal sealed record ServerTaskRequest(
    string Title, DateTime? EndDateTime, bool IsAllDay, bool IsImportant, string NotificationType,
    string RepeatType, DateOnly? RepeatEndDate, string[]? RepeatDaysOfWeek,
    int[]? RepeatDaysOfMonth, bool ExcludeHoliday);
internal sealed record CreatedTask(long TaskId, DateOnly StartDate);
internal sealed record TaskReward(long CurrencyEarned, long Balance);
internal sealed record ServerDisplay(bool DarkMode, bool DdayDisplayEnabled, string Language);
internal sealed record ServerNotificationSettings(bool ReminderEnabled, bool PostponedTaskEnabled,
    bool RoutineReminderEnabled, bool StreakSaveEnabled, bool NightPushEnabled);
internal sealed record ServerNotifications(ServerNotification[] Content, string? NextCursor, bool HasNext);
internal sealed record ServerNotification(long Id, string Category, string Content, bool Read, DateTime CreatedAt);
internal sealed record ServerAsset(string? Key, string? Url);
internal sealed record ServerRepresentative(long Id);
internal sealed record ServerSpirit(long SpeciesId, string SpeciesName, string? SpeciesDescription,
    ServerAsset? Image, bool IsOwned, long? SpiritId, string? SpiritName, int? AffectionLevel, bool? IsRepresentative,
    int? Affection, bool? IsImportant);
