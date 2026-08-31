using System;
using System.Collections;
using System.Collections.Generic;
using TodoSpirits.Core;
using TodoSpirits.Presentation.Common;
using TodoSpirits.Presentation.Record;
using TodoSpirits.Presentation.Reward;
using TodoSpirits.Runtime;
using UnityEngine;
using UnityEngine.InputSystem;
using UnityEngine.UI;

namespace TodoSpirits.Presentation.Main
{
    [DisallowMultipleComponent]
    public sealed class MainSceneController : MonoBehaviour
    {
        private const string RuntimeRootName = "[TodoSpirits Wireframe Runtime]";
        private const string GrowthStateText = "아직 어린 정령 · 오늘을 함께 살아가는 중";

        [SerializeField]
        [Tooltip("01_Main에 이미 존재하는 Screen Space Overlay Canvas를 연결합니다.")]
        private Canvas canvas;

        [SerializeField]
        [Tooltip("Editor-authored MainScreen prefab. Runtime fallback generation is intentionally disabled.")]
        private MainWireframeView mainScreenPrefab;

        [SerializeField]
        [Tooltip("Editor-authored TodayRecordScreen prefab. Runtime fallback generation is intentionally disabled.")]
        private TodayRecordWireframeView todayRecordScreenPrefab;

        private readonly List<GameObject> _screenStack = new List<GameObject>();
        private PrototypeApplicationService _application;
        private GameObject _wireframeRoot;
        private MainWireframeView _mainView;
        private TodayRecordWireframeView _recordView;
        private PlaceholderScreenView _giftView;
        private PlaceholderScreenView _travelView;
        private PlaceholderScreenView _decorateView;
        private RewardCardView _rewardCard;
        private DebugOverlayView _debugOverlay;
        private Coroutine _rewardRoutine;
        private Coroutine _delayedRewardRoutine;

        public Canvas Canvas => canvas;

        private void Awake()
        {
            if (canvas == null)
            {
                Debug.LogError(
                    $"{nameof(MainSceneController)} requires the existing 01_Main Canvas. " +
                    "Assign the serialized Canvas reference; runtime Canvas discovery is intentionally disabled.",
                    this);
                enabled = false;
                return;
            }

            if (mainScreenPrefab == null || todayRecordScreenPrefab == null)
            {
                Debug.LogError(
                    $"{nameof(MainSceneController)} requires authored MainScreen and TodayRecordScreen prefab references. " +
                    "Runtime fallback generation is intentionally disabled so missing prefab wiring is visible.",
                    this);
                enabled = false;
                return;
            }

            if (canvas.transform.Find(RuntimeRootName) != null)
            {
                Debug.LogError(
                    $"{nameof(MainSceneController)} found a duplicate '{RuntimeRootName}' below the assigned Canvas.",
                    this);
                enabled = false;
                return;
            }

            ConfigureExistingCanvas();
            BuildWireframeHierarchy();
        }

        private void Start()
        {
            if (!enabled)
            {
                return;
            }

            PrototypeBootstrap bootstrap = PrototypeBootstrap.Current;
            if (bootstrap == null || bootstrap.Application == null)
            {
                Debug.LogError(
                    $"{nameof(MainSceneController)} could not find {nameof(PrototypeBootstrap)}. " +
                    "Enter through 00_Boot so the prototype composition root is initialized before 01_Main.",
                    this);
                _wireframeRoot.SetActive(false);
                enabled = false;
                return;
            }

            _application = bootstrap.Application;

            try
            {
                DailyCompanionRecord record = _application.GetOrCreateCurrentRecord();
                RefreshViews(record, true);
                ShowPendingReward(record);
            }
            catch (Exception exception)
            {
                Debug.LogError(
                    $"{nameof(MainSceneController)} failed to bind the prototype record.\n{exception}",
                    this);
                _wireframeRoot.SetActive(false);
                enabled = false;
            }
        }

        private void Update()
        {
            Keyboard keyboard = Keyboard.current;
            Gamepad gamepad = Gamepad.current;
            bool keyboardBack = keyboard != null && keyboard.escapeKey.wasPressedThisFrame;
            bool gamepadCancel = gamepad != null && gamepad.buttonEast.wasPressedThisFrame;
            if (keyboardBack || gamepadCancel)
            {
                HandleBack();
            }
        }

        private void OnDisable()
        {
            if (_delayedRewardRoutine != null)
            {
                StopCoroutine(_delayedRewardRoutine);
                _delayedRewardRoutine = null;
            }

            if (_rewardRoutine != null)
            {
                StopCoroutine(_rewardRoutine);
                _rewardRoutine = null;
            }
        }

        private void ConfigureExistingCanvas()
        {
            canvas.renderMode = RenderMode.ScreenSpaceOverlay;

            CanvasScaler scaler = canvas.GetComponent<CanvasScaler>();
            if (scaler == null)
            {
                scaler = canvas.gameObject.AddComponent<CanvasScaler>();
            }

            scaler.uiScaleMode = CanvasScaler.ScaleMode.ScaleWithScreenSize;
            scaler.referenceResolution = new Vector2(1080f, 1920f);
            scaler.screenMatchMode = CanvasScaler.ScreenMatchMode.MatchWidthOrHeight;
            scaler.matchWidthOrHeight = 0.5f;

            if (canvas.GetComponent<GraphicRaycaster>() == null)
            {
                canvas.gameObject.AddComponent<GraphicRaycaster>();
            }
        }

        private void BuildWireframeHierarchy()
        {
            var runtimeRootObject = new GameObject(RuntimeRootName, typeof(RectTransform));
            var runtimeRootRect = (RectTransform)runtimeRootObject.transform;
            runtimeRootRect.SetParent(canvas.transform, false);
            WireframeUi.Stretch(runtimeRootRect);
            _wireframeRoot = runtimeRootObject;

            var safeAreaObject = new GameObject("SafeAreaRoot", typeof(RectTransform));
            var safeAreaRect = (RectTransform)safeAreaObject.transform;
            safeAreaRect.SetParent(runtimeRootRect, false);
            WireframeUi.Stretch(safeAreaRect);
            safeAreaObject.AddComponent<SafeAreaFitter>();

            Font font = RuntimeFontProvider.GetFont();
            if (font == null)
            {
                Debug.LogError($"{nameof(MainSceneController)} cannot create readable runtime UI without a Font.", this);
                enabled = false;
                return;
            }

            var ui = new WireframeUi(font);
            _mainView = Instantiate(mainScreenPrefab, safeAreaRect, false);
            _mainView.name = "MainScreen";
            _recordView = Instantiate(todayRecordScreenPrefab, safeAreaRect, false);
            _recordView.name = "TodayRecordScreen";

            const string giftMessage =
                "정수는 EXP가 아니라 함께한 기록의 흔적입니다.\n\n" +
                "Prototype에서 선물은 이후 구현 예정입니다.\n" +
                "성장·진로·기질·행동 reroll은 구매할 수 없습니다.";
            _giftView = new PlaceholderScreenView(ui, safeAreaRect, "Gift", "선물", PopScreen, giftMessage);
            _travelView = new PlaceholderScreenView(ui, safeAreaRect, "Travel", "여행", PopScreen);
            _decorateView = new PlaceholderScreenView(ui, safeAreaRect, "Decorate", "꾸미기", PopScreen);
            _rewardCard = new RewardCardView(ui, safeAreaRect, HandleRewardTapped);
            _debugOverlay = new DebugOverlayView(
                ui,
                safeAreaRect,
                ToggleDebug,
                () => RunDebugAction(() => { _application.SelectDemoDay(DemoDayId.Day1WorkFocus); }),
                () => RunDebugAction(() => { _application.SelectDemoDay(DemoDayId.Day2HealthRelationship); }),
                () => RunDebugAction(() => { _application.SelectDemoDay(DemoDayId.Day3Rest); }),
                () => RunDebugAction(() => { _application.SelectDemoProfile(DemoTemperamentProfile.ProfileA); }),
                () => RunDebugAction(() => { _application.SelectDemoProfile(DemoTemperamentProfile.ProfileB); }),
                () => RunDebugAction(() => { _application.ResetDemoSave(); }),
                () => RunDebugAction(() => { _application.RegenerateCurrentDay(); }),
                OpenTodayRecordFromDebug,
                () => RunDebugAction(() => _application.MoveDate(-1)),
                () => RunDebugAction(() => _application.MoveDate(1)),
                () => RunDebugAction(() => { _application.RefreshCachedCurrentDay(); }));

            bool debugAvailable = Application.isEditor || Debug.isDebugBuild;
            if (!_mainView.Initialize(
                    font,
                    OpenTodayRecord,
                    () => PushScreen(_giftView.Root),
                    () => PushScreen(_travelView.Root),
                    () => PushScreen(_decorateView.Root),
                    ToggleDebug,
                    debugAvailable) ||
                !_recordView.Initialize(font, PopScreen))
            {
                Debug.LogError(
                    $"{nameof(MainSceneController)} could not initialize the authored screen prefabs. " +
                    "Inspect their serialized references in Prefab Mode.",
                    this);
                enabled = false;
                _wireframeRoot.SetActive(false);
                return;
            }

            _screenStack.Add(_mainView.Root);
            _mainView.Root.SetActive(true);
            _recordView.Root.SetActive(false);
        }

        private void RefreshViews(DailyCompanionRecord record, bool animateSpirit)
        {
            if (record == null)
            {
                throw new InvalidOperationException("The prototype application returned a null DailyCompanionRecord.");
            }

            SpiritDayResult spiritDay = record.SpiritDay ?? new SpiritDayResult();
            SpiritActionDefinition action = SpiritActionCatalog.Get(spiritDay.PrimaryAction);
            int essenceBalance = _application.SaveData?.EssenceWallet?.Balance ?? 0;

            _mainView.Render(
                essenceBalance,
                action.DisplayName,
                spiritDay.Dialogue,
                GrowthStateText);
            _recordView.Render(
                record.Date,
                PresentationTextFormatter.CompletedTaskTitles(record),
                PresentationTextFormatter.TaskActionLinks(record));

            SpiritState spiritState = _application.CurrentSpiritState;
            string debugInfo = PresentationTextFormatter.BuildDebugInfo(
                record,
                spiritState,
                _application.CurrentDebugInfo);
            _debugOverlay.Render(
                _application.CurrentDateKey,
                GetDemoDayDisplayName(_application.CurrentDemoDay),
                _application.CurrentProfileName,
                debugInfo);

            if (animateSpirit)
            {
                _mainView.PresentSpiritDay(
                    spiritDay.PrimaryAction,
                    spiritDay.Dialogue,
                    true);
            }
        }

        private void ShowPendingReward(DailyCompanionRecord record)
        {
            if (!_application.HasPendingReward || record == null)
            {
                return;
            }

            int completedTaskCount = record.CompletedTasks?.Count ?? 0;
            int rewardDelta = record.GrantedEssenceDelta > 0
                ? record.GrantedEssenceDelta
                : record.EssenceReward;

            if (_rewardRoutine != null)
            {
                StopCoroutine(_rewardRoutine);
            }

            _rewardRoutine = StartCoroutine(PlayRewardAndAcknowledge(completedTaskCount, rewardDelta));
        }

        private IEnumerator PlayRewardAndAcknowledge(int completedTaskCount, int rewardDelta)
        {
            yield return _rewardCard.Play(completedTaskCount, rewardDelta);
            _application?.AcknowledgeReward();
            _rewardRoutine = null;
        }

        private void HandleRewardTapped()
        {
            _application?.AcknowledgeReward();
            OpenTodayRecord();
        }

        private void OpenTodayRecord()
        {
            HideRewardCard();
            PushScreen(_recordView.Root);
        }

        private void OpenTodayRecordFromDebug()
        {
            _debugOverlay.SetVisible(false);
            _mainView.SetActivityDebugVisible(false);
            OpenTodayRecord();
        }

        private void PushScreen(GameObject screen)
        {
            if (screen == null || _screenStack.Count == 0 || _screenStack[_screenStack.Count - 1] == screen)
            {
                return;
            }

            HideRewardCard();
            _screenStack[_screenStack.Count - 1].SetActive(false);
            screen.SetActive(true);
            _screenStack.Add(screen);
        }

        private void PopScreen()
        {
            if (_screenStack.Count <= 1)
            {
                return;
            }

            int topIndex = _screenStack.Count - 1;
            _screenStack[topIndex].SetActive(false);
            _screenStack.RemoveAt(topIndex);
            _screenStack[_screenStack.Count - 1].SetActive(true);
        }

        private void HandleBack()
        {
            if (_debugOverlay != null && _debugOverlay.IsVisible)
            {
                _debugOverlay.SetVisible(false);
                _mainView.SetActivityDebugVisible(false);
                return;
            }

            PopScreen();
        }

        private void ToggleDebug()
        {
            if (_application == null)
            {
                return;
            }

            bool show = !_debugOverlay.IsVisible;
            if (show)
            {
                RefreshViews(_application.GetOrCreateCurrentRecord(), false);
            }

            _debugOverlay.SetVisible(show);
            _mainView.SetActivityDebugVisible(show);
        }

        private void RunDebugAction(Action action)
        {
            if (_application == null || action == null)
            {
                return;
            }

            try
            {
                action();
                DailyCompanionRecord record = _application.GetOrCreateCurrentRecord();
                RefreshViews(record, true);
                if (_application.HasPendingReward)
                {
                    _debugOverlay.SetVisible(false);
                    _mainView.SetActivityDebugVisible(false);
                    if (_delayedRewardRoutine != null)
                    {
                        StopCoroutine(_delayedRewardRoutine);
                    }

                    _delayedRewardRoutine = StartCoroutine(
                        ShowPendingRewardAfterLivingBeat(record));
                }
            }
            catch (Exception exception)
            {
                Debug.LogError($"TodoSpirits debug action failed.\n{exception}", this);
            }
        }

        private IEnumerator ShowPendingRewardAfterLivingBeat(DailyCompanionRecord record)
        {
            yield return new WaitForSecondsRealtime(1.9f);
            _delayedRewardRoutine = null;
            if (enabled &&
                _application != null &&
                ReferenceEquals(_application.CurrentRecord, record) &&
                _application.HasPendingReward)
            {
                ShowPendingReward(record);
            }
        }

        private static string GetDemoDayDisplayName(DemoDayId day)
        {
            switch (day)
            {
                case DemoDayId.Day2HealthRelationship:
                    return "Day 2 · 건강/관계";
                case DemoDayId.Day3Rest:
                    return "Day 3 · 쉼";
                default:
                    return "Day 1 · 일 중심";
            }
        }

        private void HideRewardCard()
        {
            if (_delayedRewardRoutine != null)
            {
                StopCoroutine(_delayedRewardRoutine);
                _delayedRewardRoutine = null;
                _application?.AcknowledgeReward();
            }

            if (_rewardCard != null && _rewardCard.Root.activeSelf)
            {
                _application?.AcknowledgeReward();
            }

            if (_rewardRoutine != null)
            {
                StopCoroutine(_rewardRoutine);
                _rewardRoutine = null;
            }

            _rewardCard?.HideImmediately();
        }
    }
}
