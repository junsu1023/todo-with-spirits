using TodoSpirits.Core;
using UnityEngine;
using UnityEngine.SceneManagement;

namespace TodoSpirits.Runtime
{
    /// <summary>
    /// Single composition root for the prototype runtime.
    /// </summary>
    [DefaultExecutionOrder(-1000)]
    [DisallowMultipleComponent]
    public sealed class PrototypeBootstrap : MonoBehaviour
    {
        private const string BootSceneName = "00_Boot";
        private const string MainSceneName = "01_Main";

        public static PrototypeBootstrap Current { get; private set; }

        public PrototypeApplicationService Application { get; private set; }
        public string InitializationError { get; private set; }
        public event System.Action ApplicationChanged;
        public bool IsDemo { get; private set; }
        private string _normalSavePath;
        private string _hostAccount;
        private string _hostTimeZone;
        private System.DateTime _hostDate;

        private void Awake()
        {
            if (Current != null && Current != this)
            {
                Destroy(gameObject);
                return;
            }

            Current = this;
            DontDestroyOnLoad(gameObject);
            TryInitialize();
        }

        public void TryInitialize()
        {
            if (Application != null && Application.IsInitialized) return;
            InitializationError = null;
            try
            {

            var todoSource = new EmptyTodoCompletionSource();
            string savePath = System.IO.Path.Combine(UnityEngine.Application.persistentDataPath, "TodoSpirits", "forest-save.json");
#if UNITY_EDITOR
            // Session-only override lets Editor verification run without altering the user's journal.
            savePath = UnityEditor.SessionState.GetString("TodoSpirits.ForestVerificationSave", savePath);
#endif
            _normalSavePath = savePath;
            ISpiritSaveRepository saveRepository = IsAndroidHost() ? new AwaitingHostRepository() : new JsonSpiritSaveRepository(savePath);
            var classifier = new TaskClassifier();
            var dayGenerator = new SpiritDayGenerator(classifier);

            var application = new PrototypeApplicationService(
                todoSource,
                saveRepository,
                classifier,
                dayGenerator,
                lifeCycleEnabled: true);
            application.Initialize();
            if (!application.SaveData.Forest.Enabled) application.EnableForest();
            application.SynchronizeLifeDate(System.DateTime.Today);
            Application = application;

            if (SceneManager.GetActiveScene().name == BootSceneName)
            {
                SceneManager.LoadScene(MainSceneName, LoadSceneMode.Single);
            }
            }
            catch (System.Exception exception)
            {
                Application = null;
                InitializationError = "동행 기록을 불러오거나 저장하지 못했어요.\n기록 파일은 지우지 않았어요. 잠시 후 다시 시도해 주세요.";
                Debug.LogWarning("동행 시작 실패: " + exception.Message);
            }
        }

        public System.DateTime ResolveToday()
        {
            if (IsDemo) return Application.CurrentDate;
            if (string.IsNullOrEmpty(_hostAccount)) return System.DateTime.Today;
            return ResolveHostToday();
        }

        private System.DateTime ResolveHostToday()
        {
            try { return System.TimeZoneInfo.ConvertTimeBySystemTimeZoneId(System.DateTime.UtcNow, _hostTimeZone).Date; }
            catch (System.TimeZoneNotFoundException) { return _hostDate; }
            catch (System.InvalidTimeZoneException) { return _hostDate; }
        }

        private static bool IsAndroidHost()
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            try { using (var player = new AndroidJavaClass("com.unity3d.player.UnityPlayer"))
                using (var activity = player.GetStatic<AndroidJavaObject>("currentActivity"))
                using (var type = activity.Call<AndroidJavaObject>("getClass"))
                    return type.Call<string>("getName").EndsWith(".ForestUnityActivity", System.StringComparison.Ordinal); }
            catch (System.Exception error) { throw new System.InvalidOperationException("TODO 호스트의 시작 정보를 확인하지 못했어요.", error); }
#else
            return false;
#endif
        }

        // Native startup waits for its authenticated account without loading or writing legacy saves.
        private sealed class AwaitingHostRepository : ISpiritSaveRepository
        {
            private string _json;
            public string SavePath => "host-session-pending";
            public PrototypeSaveData Load() => string.IsNullOrEmpty(_json) ? new PrototypeSaveData() : JsonUtility.FromJson<PrototypeSaveData>(_json);
            public void Save(PrototypeSaveData data) { _json = JsonUtility.ToJson(data); }
        }

        public void OpenHostSession(string accountId, string date, string timeZone)
        {
            if (string.IsNullOrWhiteSpace(accountId) || accountId.Length > 128 || string.IsNullOrWhiteSpace(timeZone) || timeZone.Length > 128 ||
                !System.DateTime.TryParseExact(date, "yyyy-MM-dd", System.Globalization.CultureInfo.InvariantCulture,
                    System.Globalization.DateTimeStyles.None, out var hostDate))
                throw new System.ArgumentException("계정과 기준 날짜, 시간대를 확인해 주세요.");
            string hash;
            using (var sha = System.Security.Cryptography.SHA256.Create())
                hash = System.BitConverter.ToString(sha.ComputeHash(System.Text.Encoding.UTF8.GetBytes(accountId))).Replace("-", "").ToLowerInvariant();
            string path = System.IO.Path.Combine(System.IO.Path.GetDirectoryName(_normalSavePath), "accounts", hash + ".json");
            var next = !IsDemo && _hostAccount == accountId ? Application : OpenApplication(path, hostDate);
            next.BindForestHostAccount(accountId);
            next.SynchronizeLifeDate(hostDate);
            if (next.CurrentDate.Date != hostDate.Date) throw new System.InvalidOperationException("저장 날짜보다 이전 날짜로 연결할 수 없어요.");
            bool changed = !ReferenceEquals(Application, next);
            _hostAccount = accountId; _hostTimeZone = timeZone; _hostDate = hostDate;
            IsDemo = false; Application = next; InitializationError = null;
            if (changed) ApplicationChanged?.Invoke();
        }

        private PrototypeApplicationService OpenApplication(string path, System.DateTime date)
        {
            var classifier = new TaskClassifier();
            var next = new PrototypeApplicationService(new EmptyTodoCompletionSource(), new JsonSpiritSaveRepository(path),
                classifier, new SpiritDayGenerator(classifier), date, lifeCycleEnabled: true);
            next.Initialize();
            if (!next.SaveData.Forest.Enabled) next.EnableForest();
            return next;
        }

        public void BeginPrototypeDemo()
        {
            string path = System.IO.Path.Combine(System.IO.Path.GetDirectoryName(_normalSavePath), "prototype-demo-v1.json");
            var next = OpenApplication(path, System.DateTime.Today);
            Application = next; IsDemo = true;
            if (!next.IsHostConnected) ApplyDemoDay(0, 1);
            else next.RestorePrototypeDemoConnection();
            ApplicationChanged?.Invoke();
        }

        public void EndPrototypeDemo()
        {
            if (!IsDemo) return;
            if (!string.IsNullOrEmpty(_hostAccount)) { OpenHostSession(_hostAccount, ResolveHostToday().ToString("yyyy-MM-dd"), _hostTimeZone); return; }
            var next = OpenApplication(_normalSavePath, System.DateTime.Today);
            next.SynchronizeLifeDate(System.DateTime.Today);
            Application = next; IsDemo = false; ApplicationChanged?.Invoke();
        }

        // Explicit sample host controls. Their file and account never overlap a user's journal.
        public void ApplyDemoDay(int family, int completed, bool advance = false, int count = 4)
        {
            if (!IsDemo) throw new System.InvalidOperationException("예시 하루 안에서만 사용할 수 있어요.");
            if (advance) Application.AdvanceLifeDay();
            string[][] names = { new[] { "책 10쪽 읽기", "작업 노트 정리", "새로운 표현 연습", "생활비 계획 살펴보기" },
                new[] { "동네 가볍게 산책", "몸을 천천히 풀기", "방 한 곳 정돈", "산책길 사진 남기기" },
                new[] { "친구에게 안부 전하기", "잠시 쉬어가기", "오늘의 작은 약속", "좋아하는 그림 그리기" } };
            string[][] categories = { new[] { "WORK_STUDY", "WORK_STUDY", "GROWTH", "FINANCE" },
                new[] { "HEALTH", "HEALTH", "LIFE", "HOBBY" }, new[] { "RELATIONSHIP", "REST", "NONE", "HOBBY" } };
            family = System.Math.Max(0, System.Math.Min(2, family));
            var snapshot = new ForestTodoSnapshot { schemaVersion = 1, accountId = "prototype-demo-v1",
                date = Application.CurrentDateKey, revision = Application.SaveData.Forest.HostRevision + 1,
                items = new System.Collections.Generic.List<ForestTodoSnapshotItem>() };
            for (int i = 0; i < System.Math.Min(50, count); i++) snapshot.items.Add(new ForestTodoSnapshotItem {
                id = "example-" + i, title = names[family][i % 4] + (i >= 4 ? " · " + (i + 1) : ""),
                category = categories[family][i % 4], isHabit = i == 0, isCompleted = i < completed });
            Application.ApplyTodoSnapshot(snapshot);
        }

        private void OnGUI()
        {
            if (string.IsNullOrEmpty(InitializationError)) return;
            // A boot fallback must also work when the normal canvas/presentation cannot initialize.
            Matrix4x4 previous = GUI.matrix;
            float scale = Mathf.Max(.1f, Mathf.Min(Screen.width / 540f, Screen.height / 960f));
            float width = Screen.width / scale;
            float height = Screen.height / scale;
            GUI.matrix = Matrix4x4.Scale(new Vector3(scale,scale,1));
            Color previousColor = GUI.color;
            GUI.color = new Color(.95f,.93f,.88f);
            GUI.DrawTexture(new Rect(0,0,width,height), Texture2D.whiteTexture);
            GUI.color = previousColor;
            var title = new GUIStyle(GUI.skin.label) { fontSize = 28, alignment = TextAnchor.MiddleCenter, wordWrap = true };
            var body = new GUIStyle(title) { fontSize = 20 };
            title.normal.textColor = body.normal.textColor = new Color(.2f,.29f,.24f);
            var button = new GUIStyle(GUI.skin.button) { fontSize = 22 };
            GUI.Label(new Rect(25,height*.28f,width-50,90), "동행을 준비하고 있어요", title);
            GUI.Label(new Rect(30,height*.4f,width-60,150), InitializationError, body);
            if (GUI.Button(new Rect(width*.15f,height*.64f,width*.7f,70), "다시 시도", button)) TryInitialize();
            GUI.matrix = previous;
        }

        private void OnDestroy()
        {
            if (Current == this)
            {
                Current = null;
            }
        }
    }
}
