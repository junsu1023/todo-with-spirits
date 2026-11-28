using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using TodoSpirits.Core;
using UnityEngine;

namespace TodoSpirits.Runtime
{
    [Serializable] public sealed class ForestTodoSnapshot
    {
        public int schemaVersion;
        public string accountId;
        public long revision;
        public string date;
        public string timeZone;
        public string requestId;
        public List<ForestTodoSnapshotItem> items;
    }
    [Serializable] public sealed class ForestTodoSnapshotItem
    {
        public string id;
        public string title;
        public string category;
        public bool isHabit;
        public bool isCompleted;
    }
    [Serializable] public sealed class ForestHostSession
    {
        public string accountId;
        public string date;
        public string timeZone;
        public string requestId;
    }
    [Serializable] public sealed class ForestHostSessionResult
    {
        public string kind = "host-session";
        public string status;
        public string accountId;
        public string date;
        public string requestId;
        public string message;
    }
    [Serializable] public sealed class ForestHostSyncState
    {
        public string accountId;
        public string date;
        public string status;
        public string message;
        public string requestId;
    }
    [Serializable] public sealed class ForestHistoryRequest
    {
        public string accountId;
        public List<string> dates;
    }
    /// <summary>Host entry: UnitySendMessage("AppRoot", "ReceiveTodoSnapshot", JSON).
    /// The Android host must implement the sender; this component does not perform network I/O.</summary>
    [RequireComponent(typeof(PrototypeBootstrap))]
    public sealed class ForestTodoBridge : MonoBehaviour
    {
        public event Action SnapshotApplied;
        public string LastResult { get; private set; }
        private string _sessionAccount;
        private PrototypeApplicationService _boundApp;
        private void OnEnable()
        {
            GetComponent<PrototypeBootstrap>().ApplicationChanged += Update;
            Update();
        }
        private void OnDisable()
        {
            GetComponent<PrototypeBootstrap>().ApplicationChanged -= Update;
        }
        private void Start()
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            try {
                using (var player = new AndroidJavaClass("com.unity3d.player.UnityPlayer"))
                using (var activity = player.GetStatic<AndroidJavaObject>("currentActivity"))
                    activity.Call("onForestUnityReady");
            } catch (Exception error) { Debug.LogWarning("TODO 호스트 준비 알림 실패: " + error.GetType().Name); }
#endif
        }
        private void Update()
        {
            var app = GetComponent<PrototypeBootstrap>().Application;
            if (_boundApp == app) return;
            if (_boundApp != null) {
                _boundApp.ForestSnapshotRequested -= RequestHostSnapshot;
                _boundApp.ForestHistoryRequested -= RequestHostHistory;
            }
            _boundApp = app;
            if (_boundApp != null) {
                _boundApp.ForestSnapshotRequested += RequestHostSnapshot;
                _boundApp.ForestHistoryRequested += RequestHostHistory;
            }
        }
        private void OnDestroy()
        {
            if (_boundApp != null) {
                _boundApp.ForestSnapshotRequested -= RequestHostSnapshot;
                _boundApp.ForestHistoryRequested -= RequestHostHistory;
            }
        }
        private void RequestHostHistory(IReadOnlyList<string> dates)
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            try {
                var request = new ForestHistoryRequest { accountId = _boundApp.SaveData.Forest.HostAccountId, dates = dates.ToList() };
                using (var player = new AndroidJavaClass("com.unity3d.player.UnityPlayer"))
                using (var activity = player.GetStatic<AndroidJavaObject>("currentActivity"))
                    activity.Call("requestForestHistory", JsonUtility.ToJson(request));
            } catch (Exception error) { ReportResult("historical-rejected: " + error.GetType().Name); }
#endif
        }
        private void RequestHostSnapshot()
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            try {
                using (var player = new AndroidJavaClass("com.unity3d.player.UnityPlayer"))
                using (var activity = player.GetStatic<AndroidJavaObject>("currentActivity"))
                    activity.Call("requestForestRefresh");
            } catch (Exception) {
                _boundApp?.SetForestSyncState(ForestSyncState.Error, "TODO 앱에 다시 확인을 요청하지 못했어요.");
            }
#else
            _boundApp?.SetForestSyncState(ForestSyncState.Offline, "연결된 TODO 앱이 없어요 · 최신 일정 확인 불가");
#endif
        }
        [UnityEngine.Scripting.Preserve]
        public void ReceiveHostSession(string json)
        {
            ForestHostSession session = null;
            try {
                if (string.IsNullOrWhiteSpace(json) || json.Length > 4096) throw new ArgumentException("계정 정보를 확인해 주세요.");
                session = JsonUtility.FromJson<ForestHostSession>(json);
                if (session == null || string.IsNullOrWhiteSpace(session.requestId) || session.requestId.Length > 64)
                    throw new ArgumentException("연결 요청 번호를 확인해 주세요.");
                GetComponent<PrototypeBootstrap>().OpenHostSession(session.accountId, session.date, session.timeZone);
                _sessionAccount = session.accountId;
                ReportSessionResult("session-ready", session);
            } catch (Exception error) { ReportSessionResult("session-rejected", session, error.Message); }
        }
        [UnityEngine.Scripting.Preserve]
        public void ReceiveTodoSyncState(string json)
        {
            ForestHostSyncState state = null;
            var app = GetComponent<PrototypeBootstrap>().Application;
            try {
                if (string.IsNullOrWhiteSpace(json) || json.Length > 8192) throw new ArgumentException("연결 상태를 확인해 주세요.");
                state = JsonUtility.FromJson<ForestHostSyncState>(json);
                if (state == null || !IsCurrentContext(app, state.accountId, state.date))
                    throw new ArgumentException("현재 계정과 날짜의 연결 상태만 받을 수 있어요.");
                var mapped = state.status == "loading" ? ForestSyncState.Awaiting : state.status == "offline" ? ForestSyncState.Offline :
                    state.status == "error" ? ForestSyncState.Error : throw new ArgumentException("알 수 없는 연결 상태예요.");
                app.SetForestSyncState(mapped);
                ReportCorrelatedResult("todo-sync", "status-" + state.status, state.accountId, state.date, state.requestId);
            } catch (Exception error) {
                if (state != null && IsCurrentContext(app, state.accountId, state.date))
                    app.SetForestSyncState(ForestSyncState.Error);
                ReportCorrelatedResult("todo-sync", "rejected", state?.accountId, state?.date, state?.requestId, error.Message);
            }
        }
        [UnityEngine.Scripting.Preserve]
        public void ReceiveTodoSnapshot(string json)
        {
            ForestTodoSnapshot snapshot = null;
            var app = GetComponent<PrototypeBootstrap>().Application;
            try {
                if(string.IsNullOrWhiteSpace(json)||json.Length>128000)throw new ArgumentException("입력 데이터 크기를 확인해 주세요.");
                snapshot=JsonUtility.FromJson<ForestTodoSnapshot>(json);
                if(app==null)throw new InvalidOperationException("숲이 아직 준비 중이에요.");
                if (_sessionAccount != null && snapshot?.accountId != _sessionAccount)
                    throw new InvalidOperationException("다른 계정의 응답은 반영하지 않아요.");
                bool changed=app.ApplyTodoSnapshot(snapshot);
                ReportCorrelatedResult("todo-snapshot", changed?"applied":"duplicate", snapshot.accountId, snapshot.date, snapshot.requestId);
                if(changed)SnapshotApplied?.Invoke();
            } catch(Exception error) {
                // A late request from another account/date must not change the current forest's state.
                if (snapshot != null && IsCurrentContext(app, snapshot.accountId, snapshot.date))
                    app.SetForestSyncState(ForestSyncState.Error);
                ReportCorrelatedResult("todo-snapshot", "rejected", snapshot?.accountId, snapshot?.date, snapshot?.requestId, error.Message);
            }
        }
        [UnityEngine.Scripting.Preserve]
        public void ReceiveHistoricalTodoSnapshot(string json)
        {
            try {
                if (string.IsNullOrWhiteSpace(json) || json.Length > 128000) throw new ArgumentException("기록 크기를 확인해 주세요.");
                var snapshot = JsonUtility.FromJson<ForestTodoSnapshot>(json);
                if (_sessionAccount != null && snapshot?.accountId != _sessionAccount)
                    throw new InvalidOperationException("다른 계정의 과거 기록은 반영하지 않아요.");
                var app = GetComponent<PrototypeBootstrap>().Application;
                if (app == null) throw new InvalidOperationException("숲이 아직 준비 중이에요.");
                ReportResult(app.CorrectForestDayFacts(snapshot) ? "historical-applied" : "historical-duplicate");
            } catch (Exception error) { ReportResult("historical-rejected: " + error.Message); }
        }
        private void ReportSessionResult(string status, ForestHostSession session, string message = null)
        {
            // Echo the request identity even on failure. The native host must never acknowledge
            // whichever account happens to be current when a delayed callback arrives.
            ReportResult(JsonUtility.ToJson(new ForestHostSessionResult {
                status = status, accountId = session?.accountId, date = session?.date,
                requestId = session?.requestId, message = message
            }));
        }
        private bool IsCurrentContext(PrototypeApplicationService app, string accountId, string date) =>
            app != null && ReferenceEquals(app, GetComponent<PrototypeBootstrap>().Application) && app.IsHostConnected &&
            app.SaveData.Forest.HostAccountId == accountId && app.CurrentDateKey == date;
        private void ReportCorrelatedResult(string kind, string status, string accountId, string date, string requestId, string message = null)
        {
            // Preserve local/older sender diagnostics; the current Android host only accepts correlated replies.
            if (string.IsNullOrEmpty(requestId)) {
                ReportResult(status + (string.IsNullOrEmpty(message) ? "" : ": " + message));
                return;
            }
            ReportResult(JsonUtility.ToJson(new ForestHostSessionResult {
                kind = kind, status = status, accountId = accountId, date = date, requestId = requestId, message = message
            }));
        }
        private void ReportResult(string result)
        {
            LastResult = result;
#if UNITY_ANDROID && !UNITY_EDITOR
            try {
                using (var player = new AndroidJavaClass("com.unity3d.player.UnityPlayer"))
                using (var activity = player.GetStatic<AndroidJavaObject>("currentActivity"))
                    activity.Call("onForestSnapshotResult", result);
            } catch (Exception error) { Debug.LogWarning("TODO 호스트 결과 전달 실패: " + error.GetType().Name); }
#endif
        }
    }
    public sealed partial class PrototypeApplicationService
    {
        public bool IsHostConnected => !string.IsNullOrEmpty(_saveData.Forest.HostAccountId);
        public bool ApplyTodoSnapshot(ForestTodoSnapshot snapshot)
        {
            EnsureInitialized();
            if(snapshot==null||snapshot.schemaVersion!=1||string.IsNullOrWhiteSpace(snapshot.accountId)||snapshot.accountId.Length>128||snapshot.revision<0)
                throw new ArgumentException("올바른 버전·계정·수정 번호가 필요합니다.");
            if(!DateTime.TryParseExact(snapshot.date,"yyyy-MM-dd",CultureInfo.InvariantCulture,DateTimeStyles.None,out _)||snapshot.date!=CurrentDateKey)
                throw new ArgumentException("현재 동행 날짜의 전체 목록만 반영할 수 있습니다.");
            if(IsHostConnected&&_saveData.Forest.HostAccountId!=snapshot.accountId)
                throw new InvalidOperationException("다른 계정의 생활 기록을 섞을 수 없습니다. 계정별 저장 분리가 필요합니다.");
            if(!IsHostConnected&&_saveData.Forest.Todos.Count>0)
                throw new InvalidOperationException("로컬 일기가 있는 저장에는 외부 계정을 덮어쓸 수 없습니다.");
            if(IsHostConnected&&snapshot.revision<=_saveData.Forest.HostRevision) {
                if (snapshot.revision == _saveData.Forest.HostRevision && snapshot.date == _saveData.Forest.HostDate) {
                    MutateLife(() => {
                        _saveData.Forest.LastConfirmedAtUtc = DateTime.UtcNow.ToString("O");
                        var day = FindForestDay(ForestAccountKey, CurrentDateKey, CurrentLife.SpiritId);
                        if (day != null) day.LastConfirmedAtUtc = _saveData.Forest.LastConfirmedAtUtc;
                    });
                    _forestSyncState = ForestSyncState.Confirmed; _forestSyncMessage = null; ForestTodayChanged?.Invoke();
                }
                return false;
            }
            if(snapshot.items==null||snapshot.items.Count>50)throw new ArgumentException("전체 목록은 0~50개여야 합니다.");
            var ids=new HashSet<string>();var rows=new List<ForestTodo>();
            foreach(var item in snapshot.items){
                if(item==null||string.IsNullOrWhiteSpace(item.id)||item.id.Length>128||!ids.Add(item.id)||string.IsNullOrWhiteSpace(item.title)||item.title.Length>255||item.title.Any(char.IsControl))
                    throw new ArgumentException("중복 없는 ID와 1~255자의 제목이 필요합니다.");
                var category=MapHostCategory(item.category);
                rows.Add(new ForestTodo{Id=item.id,Title=item.title,Category=category,CreatedOn=snapshot.date,Habit=item.isHabit,
                    CompletedDates=item.isCompleted?new List<string>{snapshot.date}:new List<string>()});
            }
            MutateLife(()=>{
                _saveData.Forest.Enabled=true;
                if (HasActiveForestCompanion) CurrentLife.Story.Enabled=true;
                _saveData.Forest.HostAccountId=snapshot.accountId;_saveData.Forest.HostRevision=snapshot.revision;_saveData.Forest.HostDate=snapshot.date;
                _saveData.Forest.LastConfirmedAtUtc=DateTime.UtcNow.ToString("O");
                _saveData.Forest.Todos=rows;RebuildForestRecord();
            });
            _forestSyncState=ForestSyncState.Confirmed;_forestSyncMessage=null;
            ForestTodayChanged?.Invoke();
            return true;
        }
        public static UserTaskCategory MapHostCategory(string value)
        {
            switch(value){
                case "NONE":return UserTaskCategory.Unspecified;
                case "WORK_STUDY":return UserTaskCategory.WorkStudy;
                case "HEALTH":return UserTaskCategory.Health;
                case "LIFE":return UserTaskCategory.Living;
                case "RELATIONSHIP":return UserTaskCategory.Relationships;
                case "GROWTH":return UserTaskCategory.SelfDevelopment;
                case "HOBBY":return UserTaskCategory.Hobby;
                case "REST":return UserTaskCategory.RestMind;
                case "FINANCE":return UserTaskCategory.Assets;
                default:throw new ArgumentException("알 수 없는 카테고리입니다: "+value);
            }
        }
    }
}
