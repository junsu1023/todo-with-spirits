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

        private void Awake()
        {
            if (Current != null && Current != this)
            {
                Destroy(gameObject);
                return;
            }

            Current = this;
            DontDestroyOnLoad(gameObject);

            var todoSource = new MockTodoCompletionSource(MockTodoPreset.DemoDay1WorkFocus);
            var saveRepository = new JsonSpiritSaveRepository();
            var classifier = new TaskClassifier();
            var dayGenerator = new SpiritDayGenerator(classifier);

            Application = new PrototypeApplicationService(
                todoSource,
                saveRepository,
                classifier,
                dayGenerator);
            Application.Initialize();

            if (SceneManager.GetActiveScene().name == BootSceneName)
            {
                SceneManager.LoadScene(MainSceneName, LoadSceneMode.Single);
            }
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
