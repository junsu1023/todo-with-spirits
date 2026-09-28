using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using TodoSpirits.Core;
using TodoSpirits.Runtime;
using TodoSpirits.Presentation.Common;
using TodoSpirits.ArtKit;
using UnityEngine;
using UnityEngine.UI;
using UnityEngine.InputSystem;

namespace TodoSpirits.Presentation.Forest
{
    [DisallowMultipleComponent]
    public sealed partial class ForestAppView : MonoBehaviour
    {
        [SerializeField] private Font regularFont;
        [SerializeField] private Font boldFont;
        [SerializeField] private Sprite forestBackground;
        [SerializeField] private Sprite[] spiritPoses;
        [SerializeField] private Sprite[] adolescentSpiritPoses;
        [SerializeField] private Sprite[] adultSpiritPoses;
        [SerializeField] private Sprite[] forestProps;
        [SerializeField] private Sprite[] journeyProps;
        [SerializeField] private Sprite[] giftProps;
        [SerializeField] private Sprite tripLunchSprite;
        [SerializeField] private ArtKitCatalog artKitCatalog;
        [SerializeField] private Sprite readingSpirit;
        [SerializeField] private Sprite readingDesk;
        [SerializeField] private Sprite teaCup;
        [SerializeField] private Sprite restCushion;
        private static Sprite _sceneFade;
        private PrototypeApplicationService _app;
        private ForestUi _ui;
        private Image _ground;
        private RectTransform _safe, _page, _content, _modal, _toastPanel;
        private ForestSpiritAnimator _actor;
        private ScrollRect _pageScroll;
        private Text _toast;
        private float _toastUntil, _nextClock;
        private int _tab = 1, _renderedTab = -1;
        private readonly float[] _scrollPositions = new float[3];
        private float _width = 480;
        private Vector2 _lastSafeSize;
        private bool _showCompleted;
        private DateTime _month;
        private string _selectedSpirit;
        private ForestTodoBridge _bridge;
        private CanvasScaler _canvasScaler;
        private float _originalCanvasMatch;
        private bool _lastTripReady;
        private Text _spiritSpeech;
        private string _openedDay;
        private ForestTodaySummary _displayedToday;
        private bool _showingToday;
        private bool _openingToday;
        public PrototypeApplicationService ApplicationService => _app;
        public int ActiveTab => _tab;

        public Sprite[] PosesForPhase(int phase)
        {
            var poses = phase >= 2 ? adultSpiritPoses : phase == 1 ? adolescentSpiritPoses : spiritPoses;
            return poses != null && poses.Length == 4 && poses.All(p => p != null) ? poses : spiritPoses;
        }

        public Sprite[] PosesForStage(CompanionStage stage) => PosesForPhase(GrowthPhase(stage));
        private static int GrowthPhase(CompanionStage stage) => stage < CompanionStage.Interests ? 0 : stage < CompanionStage.Adult ? 1 : 2;

        private void Start()
        {
            if (regularFont == null || boldFont == null || forestBackground == null || spiritPoses == null || spiritPoses.Length != 4)
            { Debug.LogError("ForestAppView: 숲 프리팹의 폰트·배경·정령 4포즈 참조를 확인해 주세요.", this); enabled = false; return; }
            _app = PrototypeBootstrap.Current?.Application;
            if (_app == null) { Debug.LogError("00_Boot에서 숲을 시작해 주세요.", this); enabled = false; return; }
            _ui = new ForestUi(regularFont, boldFont);
            _canvasScaler = GetComponentInParent<CanvasScaler>();
            if (_canvasScaler != null)
            {
                _originalCanvasMatch = _canvasScaler.matchWidthOrHeight;
                MatchCanvasToOrientation();
            }
            _bridge = PrototypeBootstrap.Current.GetComponent<ForestTodoBridge>();
            _app.ForestTodayChanged += OnTodayChanged;
            PrototypeBootstrap.Current.ApplicationChanged += OnApplicationChanged;
            _ground = _ui.Panel("AppBackground", transform, 0, 0, 0, 0, ForestUi.Paper, false);
            _ui.Fill(_ground.rectTransform);
            _safe = _ui.Rect("SafeArea", transform, 0, 0, 480, 900);
            _ui.Fill(_safe); _safe.gameObject.AddComponent<SafeAreaFitter>();
            _month = new DateTime(_app.CurrentDate.Year, _app.CurrentDate.Month, 1);
            _selectedSpirit = _app.CurrentLife.SpiritId;
            Canvas.ForceUpdateCanvases(); Render();
            StartCoroutine(OpenFirstDay());
        }

        private void Update()
        {
            if (_app == null) return;
            MatchCanvasToOrientation();
            if (_modal == null && _dailyRefreshPending) { _dailyRefreshPending = false; Render(); }
            if (_toast != null && Time.unscaledTime > _toastUntil) _toastPanel.gameObject.SetActive(false);
            if (_modal == null && (_safe.rect.size - _lastSafeSize).sqrMagnitude > 4) Render();
            if (_modal == null && Time.unscaledTime > _nextClock)
            {
                _nextClock = Time.unscaledTime + 10;
                try
                {
                    bool ready = _app.PendingTrip != null && _app.CanClaimTrip(DateTime.UtcNow);
                    if (_app.SynchronizeLifeDate(PrototypeBootstrap.Current.ResolveToday())) { Render(); StartCoroutine(OpenFirstDay()); }
                    else if (ready != _lastTripReady) Render();
                }
                catch (Exception exception) { Toast(exception.Message); }
            }
            if (Keyboard.current != null && Keyboard.current.escapeKey.wasPressedThisFrame)
            { if (_modal != null) CloseModal(); else ShowTab(1); }
        }
        private void OnDestroy()
        {
            if (_app != null) _app.ForestTodayChanged -= OnTodayChanged;
            if (PrototypeBootstrap.Current != null) PrototypeBootstrap.Current.ApplicationChanged -= OnApplicationChanged;
            if (_canvasScaler != null) _canvasScaler.matchWidthOrHeight = _originalCanvasMatch;
        }
        private void MatchCanvasToOrientation()
        {
            if (_canvasScaler == null) return;
            float match = Screen.width > Screen.height ? 1f : _originalCanvasMatch;
            if (!Mathf.Approximately(_canvasScaler.matchWidthOrHeight, match))
                _canvasScaler.matchWidthOrHeight = match;
        }
        public void ShowTab(int tab)
        {
            if (tab == 0) { TodaySummary(); return; }
            _tab = tab == 2 ? 2 : 1;
            Render();
            if (_tab == 2 && !PrototypeBootstrap.Current.IsDemo) _app.RequestForestHistory(_month.ToString("yyyy-MM"));
        }
        private void ResetPageScroll()
        {
            _scrollPositions[_tab] = 0;
            if (_pageScroll != null && _renderedTab == _tab) _pageScroll.content.anchoredPosition = Vector2.zero;
        }
        private void Act(Action action, string message = null, string motion = null)
        {
            try { action(); Render(); if (!string.IsNullOrEmpty(message)) Toast(message); if (motion != null) _actor?.Perform(motion); }
            catch (Exception exception) { Toast(exception.Message); }
        }
        private void Clear(RectTransform rect) { if (rect == null) return; rect.gameObject.SetActive(false); Destroy(rect.gameObject); }

        public void Render()
        {
            if (_app == null || _safe == null) return;
            if (_pageScroll != null && _renderedTab >= 0) _scrollPositions[_renderedTab] = Mathf.Max(0, _pageScroll.content.anchoredPosition.y);
            ClearModalImmediately(); Clear(_toastPanel); Clear(_page); _actor = null; _spiritSpeech = null;
            ForestUi.ReduceMotion = _app.SaveData.Forest.ReduceMotion;
            ArtKitMotionPolicy.ReduceMotion = _app.SaveData.Forest.ReduceMotion;
            _lastTripReady = _app.PendingTrip != null && _app.CanClaimTrip(DateTime.UtcNow);
            Canvas.ForceUpdateCanvases();
            _lastSafeSize = _safe.rect.size;
            _width = Mathf.Clamp(_safe.rect.width, 320, 600);
            bool wide = _safe.rect.width > _width + 1;
            _ground.sprite = wide ? ArtSprite("bg_forest_storybook_fallback") ?? forestBackground : null;
            _ground.color = wide ? new Color(.45f, .52f, .4f, 1) : ForestUi.Paper;
            _page = _ui.Rect("ForestPage", _safe, 0, 0, _width, _safe.rect.height);
            _page.anchorMin = new Vector2(.5f, 0); _page.anchorMax = new Vector2(.5f, 1);
            _page.pivot = Vector2.one * .5f; _page.anchoredPosition = Vector2.zero; _page.sizeDelta = new Vector2(_width, 0);
            float headerHeight = _tab == 1 ? 0 : 76;
            if (_tab == 2)
            {
                var backdrop = _ui.Sprite("AlbumForestBackdrop", ArtSprite("bg_forest_storybook_fallback") ?? forestBackground,
                    _page, 0, 0, _width, _safe.rect.height);
                backdrop.preserveAspect = false;
                var header = _ui.Panel("AlbumHeader", _page, 0, 0, _width, headerHeight, ForestUi.Paper, false).rectTransform;
                UseArt(header.GetComponent<Image>(), "header_wood");
                _ui.Text("함께한 계절", header, 24, 16, _width - 112, 44, 27, ForestUi.Ink, true);
                _ui.IconButton("CloseAlbum", ForestIcon.Close, header, _width - 67, 8, 60, 60, () => ShowTab(1), ForestUi.Moss);
            }

            var viewport = _ui.Rect("PageScroll", _page, 0, 104, _width, 500);
            viewport.anchorMin = Vector2.zero; viewport.anchorMax = Vector2.one;
            viewport.offsetMin = Vector2.zero;
            viewport.offsetMax = new Vector2(0, -headerHeight);
            var viewportImage = viewport.gameObject.AddComponent<Image>(); viewportImage.color = new Color(1, 1, 1, .001f);
            IsolateCanvas(viewport.gameObject); viewport.gameObject.AddComponent<RectMask2D>();
            _pageScroll = viewport.gameObject.AddComponent<ScrollRect>(); _pageScroll.horizontal = false;
            _pageScroll.vertical = _tab == 2;
            _pageScroll.movementType = ScrollRect.MovementType.Clamped; _pageScroll.scrollSensitivity = 35;
            _pageScroll.decelerationRate = .09f; _pageScroll.viewport = viewport;
            _content = _ui.Rect("Content", viewport, 0, 0, _width, 100); _pageScroll.content = _content;
            Canvas.ForceUpdateCanvases();
            if (_tab == 2) UseArt(_content.gameObject.AddComponent<Image>(), "panel_paper");
            float height = _tab == 1 ? Forest() : Album();
            float padding = _tab == 1 ? 0 : 28;
            _content.sizeDelta = new Vector2(_width, height + padding);
            Canvas.ForceUpdateCanvases();
            _content.anchoredPosition = new Vector2(0, Mathf.Clamp(_scrollPositions[_tab], 0, Mathf.Max(0, height + padding - viewport.rect.height)));
            _renderedTab = _tab;
            if (_tab == 1) AlbumSideTab();
            var toast = _ui.Panel("Toast", _safe, 0, 0, _width - 40, 88, ForestUi.Ink);
            _toastPanel = toast.rectTransform;
            _toastPanel.anchorMin = _toastPanel.anchorMax = new Vector2(.5f, 0); _toastPanel.pivot = new Vector2(.5f, 0);
            _toastPanel.anchoredPosition = new Vector2(0, 101);
            _toast = _ui.Text("", toast.transform, 18, 10, _width - 76, 68, 18, ForestUi.Paper, false, TextAnchor.MiddleCenter);
            toast.gameObject.SetActive(false);
        }

        private void AlbumSideTab()
        {
            var tab = _ui.IconButton("AlbumSideTab", ForestIcon.Album, _page, _width - 66, 74, 58, 62,
                () => ShowTab(2), ForestUi.Moss);
            UseArt(tab.GetComponent<Image>(), "circle_paper_normal");
            var icon = ArtSprite("ic_album");
            if (icon != null) _ui.Sprite("AlbumIcon", icon, tab.transform, 16, 16, 27, 27);
            if (_app.ReunionGuest != null) _ui.Panel("AlbumNotice", tab.transform, 43, 5, 9, 9, ForestUi.Rose);
        }

        private Sprite ArtSprite(string id)
        {
            if (artKitCatalog == null) return null;
            try { return artKitCatalog.GetSprite(id); }
            catch (InvalidOperationException) { return null; }
        }

        private void UseArt(Image image, string id)
        {
            var sprite = ArtSprite(id);
            if (sprite == null) return;
            image.sprite = sprite;
            image.type = Image.Type.Sliced;
            image.color = Color.white;
        }

        private void StyleArtButton(Button button, string family)
        {
            var normal = ArtSprite("btn_" + family + "_normal");
            if (normal == null) return;
            var rect = button.GetComponent<RectTransform>();
            var children = new List<Transform>();
            foreach (Transform child in button.transform) children.Add(child);
            var visual = _ui.Rect("Visual", button.transform, 0, 0, rect.rect.width, rect.rect.height);
            _ui.Fill(visual);
            var surface = visual.gameObject.AddComponent<Image>();
            surface.sprite = normal;
            surface.type = Image.Type.Sliced;
            surface.raycastTarget = false;
            foreach (var child in children) child.SetParent(visual, false);
            var focus = _ui.Sprite("FocusRing", ArtSprite("focus_ring"), visual, 0, 0,
                rect.rect.width, rect.rect.height);
            _ui.Fill(focus.rectTransform);
            focus.raycastTarget = false;
            focus.enabled = false;
            var hit = button.GetComponent<Image>();
            hit.sprite = null;
            hit.color = new Color(1, 1, 1, .001f);
            button.transition = Selectable.Transition.None;
            var oldMotion = button.GetComponent<ForestUiMotion>();
            if (oldMotion != null) oldMotion.enabled = false;
            var outline = button.GetComponent<Outline>();
            if (outline != null) outline.enabled = false;
            button.gameObject.SetActive(false);
            var motion = button.gameObject.AddComponent<ArtKitButtonMotion>();
            motion.owner = button;
            motion.visual = visual;
            motion.surface = surface;
            motion.keyboardFocusRing = focus;
            motion.normal = normal;
            motion.highlighted = ArtSprite("btn_" + family + "_highlighted");
            motion.pressed = ArtSprite("btn_" + family + "_pressed");
            motion.disabled = ArtSprite("btn_" + family + "_disabled");
            button.gameObject.SetActive(true);
        }

        private void Toast(string message)
        {
            if (_toast == null) return;
            _toast.text = message; _toastPanel.gameObject.SetActive(true); _toastPanel.SetAsLastSibling(); _toastUntil = Time.unscaledTime + 5;
        }
        private static void IsolateCanvas(GameObject target)
        {
            // Keep masked scrolling content out of header and navigation batches on mobile.
            target.AddComponent<Canvas>(); target.AddComponent<GraphicRaycaster>();
        }
        private float CopyHeight(string text, Transform parent, float x, float y, float width, int size = 21, Color? color = null, bool bold = false)
        {
            var label = _ui.Text(text, parent, x, y, width, 500, size, color, bold);
            float height = Mathf.Ceil(label.preferredHeight) + 4;
            label.rectTransform.sizeDelta = new Vector2(width, height); return height;
        }
        private static Color CategoryTint(UserTaskCategory category)
        {
            Color[] colors = { new Color32(227,228,219,255), new Color32(216,225,236,255), new Color32(220,234,214,255),
                new Color32(237,226,207,255), new Color32(244,221,226,255), new Color32(229,221,239,255),
                new Color32(242,225,203,255), new Color32(218,235,231,255), new Color32(228,231,205,255), new Color32(228,231,205,255) };
            return colors[Mathf.Clamp((int)category, 0, colors.Length - 1)];
        }

    }
}
