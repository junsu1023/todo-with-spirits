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

            var todoSource = new MockTodoCompletionSource(MockTodoPreset.DemoDay1WorkFocus);
            var saveRepository = new JsonSpiritSaveRepository(System.IO.Path.Combine(
                UnityEngine.Application.persistentDataPath, "TodoSpirits", "companion-save.json"));
            var classifier = new TaskClassifier();
            var dayGenerator = new SpiritDayGenerator(classifier);

            var application = new PrototypeApplicationService(
                todoSource,
                saveRepository,
                classifier,
                dayGenerator,
                lifeCycleEnabled: true);
            application.Initialize();
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
