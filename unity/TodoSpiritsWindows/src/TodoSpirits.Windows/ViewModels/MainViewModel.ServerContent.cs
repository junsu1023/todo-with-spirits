using System.Globalization;
using System.Net;
using System.Windows.Media.Imaging;
using TodoSpirits.Windows.Models;
using TodoSpirits.Windows.Services;

namespace TodoSpirits.Windows.ViewModels;

public sealed partial class MainViewModel
{
    private string? _nextNotificationCursor;
    private readonly HashSet<long> _hiddenServerNotices = [];
    public bool MoreNotifications => IsSignedIn && _nextNotificationCursor is not null;
    public System.Windows.Input.ICommand LoadMoreNoticesCommand => field ??= new RelayCommand(
        async _ => await RunServer(() => LoadNotices(append: true), "알림을 불러오지 못했어요"),
        _ => CanUseAccount && MoreNotifications);

    private async Task LoadNotices(bool append = false)
    {
        var path = "api/notification?size=20";
        if (append && _nextNotificationCursor is { } cursor) path += "&cursor=" + Uri.EscapeDataString(cursor);
        var result = await _api!.Get<ServerNotifications>(path);
        if (!append) _data.Notifications.RemoveAll(n => n.ServerId is not null);
        foreach (var notice in result.Content)
        {
            if (_hiddenServerNotices.Contains(notice.Id) || _data.Notifications.Any(n => n.ServerId == notice.Id)) continue;
            var (category, name) = notice.Category switch
            {
                "EVENT" => ("evt", "이벤트"),
                "SPIRIT" => ("forest", "정령의 숲"),
                _ => ("sys", "시스템"),
            };
            _data.Notifications.Add(new NotiItem
            {
                ServerId = notice.Id,
                Category = category,
                Title = name,
                Body = notice.Content,
                Read = notice.Read,
                When = notice.CreatedAt,
            });
        }
        _nextNotificationCursor = result.HasNext ? result.NextCursor : null;
        Raise(nameof(MoreNotifications));
        RaiseNoti();
    }

    private Task MarkNoticesRead(long? id = null) => RunServer(async () =>
    {
        var path = id is { } value ? $"api/notification/{value.ToString(CultureInfo.InvariantCulture)}/read" : "api/notification/read";
        await _api!.Patch(path);
        foreach (var notice in _data.Notifications.Where(n => id is null || n.ServerId == id)) notice.Read = true;
        RaiseNoti();
    }, "알림을 읽음 처리하지 못했어요");

    private async Task LoadDex()
    {
        var entries = await _api!.Get<ServerSpirit[]>("api/spirits");
        long? representative = null;
        try { representative = (await _api.Get<ServerRepresentative>("api/spirits/current")).Id; }
        catch (SpiritsApiException ex) when (ex.Status == HttpStatusCode.NotFound) { }
        var selected = SelectedSpirit?.Id;
        SetDexRows(entries.Select(MapSpirit).ToList());

        SpiritRow MapSpirit(ServerSpirit entry)
        {
            var id = entry.SpeciesId.ToString(CultureInfo.InvariantCulture);
            return new SpiritRow
            {
                Id = id,
                ServerId = entry.SpiritId,
                IsImportant = entry.IsImportant == true,
                Name = entry.IsOwned ? entry.SpiritName ?? entry.SpeciesName : "???",
                Category = "미분류",
                Affection = entry.AffectionLevel ?? 0,
                // 서버 호감도 20점마다 하트 한 단계. 막대는 현재 단계 안에서의 진행률이다.
                NextHeart = entry.Affection is >= 0 and < 80 && entry.AffectionLevel == entry.Affection / 20 + 1
                    ? entry.Affection % 20 * 5 : null,
                Owned = entry.IsOwned,
                Description = entry.SpeciesDescription ?? "",
                Hint = "등록된 획득 힌트가 없어요.",
                Art = ServerSpiritArt(entry, dark: false),
                ArtDark = ServerSpiritArt(entry, dark: true),
                IsRepresentative = entry.SpiritId is not null && (representative is null
                    ? entry.IsRepresentative == true : entry.SpiritId == representative),
                IsSelected = id == selected,
            }.WithHearts();
        }
    }

    private static System.Windows.Media.ImageSource? ServerSpiritArt(ServerSpirit entry, bool dark)
    {
        if (entry.IsOwned && Uri.TryCreate(entry.Image?.Url, UriKind.Absolute, out var url) && url.Scheme == Uri.UriSchemeHttps)
        {
            try { return new BitmapImage(url); }
            catch (Exception ex) when (ex is System.IO.IOException or NotSupportedException or ArgumentException) { }
        }
        return SpiritCatalog.FallbackArt(entry.IsOwned, dark);
    }

    public Task RenameSelectedSpirit(string name)
    {
        name = name.Trim();
        if (!CanUseAccount || SelectedSpirit is not { ServerId: { } id, Owned: true } row || name == row.Name)
            return Task.CompletedTask;
        if (name.Length is 0 or > 50)
        {
            ShowToast("정령 이름을 1~50자로 입력해 주세요", null);
            return Task.CompletedTask;
        }
        return RunServer(async () =>
        {
            await _api!.Patch($"{SpiritsApiPath(id)}/name", new { spiritName = name });
            row.Name = name;
            RebuildDexLists();
            ShowToast("정령 이름을 바꿨어요", null);
        }, "정령 이름을 바꾸지 못했어요");
    }

    public Task ToggleSelectedSpiritFavorite()
    {
        if (!CanUseAccount || SelectedSpirit is not { ServerId: { } id, Owned: true } row)
            return Task.CompletedTask;
        return RunServer(async () =>
        {
            var important = !row.IsImportant;
            await _api!.Patch($"{SpiritsApiPath(id)}/important", new { isImportant = important });
            row.IsImportant = important;
        }, "즐겨찾기를 바꾸지 못했어요");
    }

    private static string SpiritsApiPath(long id) => $"api/spirits/{id.ToString(CultureInfo.InvariantCulture)}";

    private Task SetServerRepresentative(SpiritRow row) => RunServer(async () =>
    {
        await _api!.Patch("api/spirits/current", new { id = row.ServerId });
        foreach (var spirit in AllSpirits) spirit.IsRepresentative = spirit.ServerId == row.ServerId;
        Raise(nameof(Representative));
        Raise(nameof(RepresentativeLabel));
        ShowToast($"{row.Name}{Fmt.Josa(row.Name, "이", "가")} 대표 정령이 됐어요", null);
    }, "대표 정령을 바꾸지 못했어요");
}
