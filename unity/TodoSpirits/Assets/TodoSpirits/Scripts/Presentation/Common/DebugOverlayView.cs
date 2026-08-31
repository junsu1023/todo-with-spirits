using System;
using UnityEngine;
using UnityEngine.UI;

namespace TodoSpirits.Presentation.Common
{
    internal sealed class DebugOverlayView
    {
        private readonly GameObject _controlPanel;
        private readonly GameObject _detailsPanel;
        private readonly Text _summaryText;
        private readonly Text _detailsSummaryText;
        private readonly Text _debugText;
        private readonly RectTransform _detailsContent;
        private readonly RectTransform _detailsViewport;
        private readonly ScrollRect _scrollRect;

        public DebugOverlayView(
            WireframeUi ui,
            Transform safeAreaRoot,
            Action close,
            Action selectDay1,
            Action selectDay2,
            Action selectDay3,
            Action selectProfileA,
            Action selectProfileB,
            Action resetDemoSave,
            Action regenerate,
            Action openTodayRecord,
            Action previousDate,
            Action nextDate,
            Action refreshCached)
        {
            RectTransform root = ui.CreateRect(
                safeAreaRoot,
                "DebugOverlay",
                Vector2.zero,
                Vector2.one,
                Vector2.zero,
                Vector2.zero);
            Root = root.gameObject;

            Image controlPanelImage = ui.CreatePanel(
                root,
                "DemoControlPanel",
                new Color(WireframePalette.Debug.r, WireframePalette.Debug.g, WireframePalette.Debug.b, 0.98f),
                new Vector2(0.025f, 0.02f),
                new Vector2(0.975f, 0.43f),
                Vector2.zero,
                Vector2.zero);
            controlPanelImage.raycastTarget = true;
            _controlPanel = controlPanelImage.gameObject;

            ui.CreateText(
                _controlPanel.transform,
                "Title",
                "DEMO CONTROLS · 저장되는 프로토타입 상태",
                25,
                TextAnchor.MiddleLeft,
                WireframePalette.TextPrimary,
                new Vector2(0.025f, 0.87f),
                new Vector2(0.73f, 0.98f),
                Vector2.zero,
                Vector2.zero,
                FontStyle.Bold);
            ui.CreateButton(
                _controlPanel.transform,
                "CloseButton",
                "닫기",
                () => close?.Invoke(),
                new Vector2(0.77f, 0.875f),
                new Vector2(0.975f, 0.975f),
                Vector2.zero,
                Vector2.zero,
                WireframePalette.PanelRaised,
                21);

            _summaryText = ui.CreateText(
                _controlPanel.transform,
                "DemoSummary",
                "Day - · Profile -",
                22,
                TextAnchor.MiddleLeft,
                WireframePalette.Accent,
                new Vector2(0.025f, 0.78f),
                new Vector2(0.975f, 0.87f),
                Vector2.zero,
                Vector2.zero,
                FontStyle.Bold);

            CreateSectionLabel(ui, _controlPanel.transform, "DayLabel", "Demo Day", 0.68f, 0.77f);
            CreateThreeButtons(
                ui,
                _controlPanel.transform,
                "Day1Button",
                "Day 1 · 일",
                selectDay1,
                "Day2Button",
                "Day 2 · 건강/관계",
                selectDay2,
                "Day3Button",
                "Day 3 · 쉼",
                selectDay3,
                0.55f,
                0.68f);

            CreateSectionLabel(ui, _controlPanel.transform, "ProfileLabel", "Temperament Profile", 0.46f, 0.55f);
            CreateTwoButtons(
                ui,
                _controlPanel.transform,
                "ProfileAButton",
                "Profile A",
                selectProfileA,
                "ProfileBButton",
                "Profile B",
                selectProfileB,
                0.33f,
                0.46f);

            CreateTwoButtons(
                ui,
                _controlPanel.transform,
                "ResetDemoSaveButton",
                "Reset Demo Save",
                resetDemoSave,
                "RegenerateButton",
                "Regenerate",
                regenerate,
                0.18f,
                0.30f,
                new Color(0.84f, 0.69f, 0.62f, 1f));

            CreateTwoButtons(
                ui,
                _controlPanel.transform,
                "OpenTodayRecordButton",
                "오늘의 기록 열기",
                openTodayRecord,
                "DetailsButton",
                "결과 근거 Details",
                () => SetDetailsVisible(true),
                0.025f,
                0.145f,
                WireframePalette.AccentDark);

            Image detailsPanelImage = ui.CreatePanel(
                root,
                "DetailsPanel",
                new Color(WireframePalette.Background.r, WireframePalette.Background.g, WireframePalette.Background.b, 0.995f),
                new Vector2(0.025f, 0.025f),
                new Vector2(0.975f, 0.975f),
                Vector2.zero,
                Vector2.zero);
            detailsPanelImage.raycastTarget = true;
            _detailsPanel = detailsPanelImage.gameObject;

            ui.CreateText(
                _detailsPanel.transform,
                "Title",
                "DEVELOPER DETAILS · deterministic 결과 근거",
                29,
                TextAnchor.MiddleLeft,
                WireframePalette.TextPrimary,
                new Vector2(0.03f, 0.92f),
                new Vector2(0.73f, 0.985f),
                Vector2.zero,
                Vector2.zero,
                FontStyle.Bold);
            ui.CreateButton(
                _detailsPanel.transform,
                "CloseDetailsButton",
                "Controls로",
                () => SetDetailsVisible(false),
                new Vector2(0.75f, 0.93f),
                new Vector2(0.97f, 0.98f),
                Vector2.zero,
                Vector2.zero,
                WireframePalette.Debug,
                20);

            _detailsSummaryText = ui.CreateText(
                _detailsPanel.transform,
                "DetailsSummary",
                "날짜 - · Day - · Profile -",
                22,
                TextAnchor.MiddleCenter,
                WireframePalette.Accent,
                new Vector2(0.03f, 0.865f),
                new Vector2(0.97f, 0.92f),
                Vector2.zero,
                Vector2.zero,
                FontStyle.Bold);

            CreateThreeButtons(
                ui,
                _detailsPanel.transform,
                "PreviousDateButton",
                "날짜 -1",
                previousDate,
                "RefreshCachedButton",
                "저장 결과 Refresh",
                refreshCached,
                "NextDateButton",
                "날짜 +1",
                nextDate,
                0.79f,
                0.855f);

            Image viewportImage = ui.CreatePanel(
                _detailsPanel.transform,
                "DebugScrollViewport",
                WireframePalette.Debug,
                new Vector2(0.03f, 0.03f),
                new Vector2(0.97f, 0.77f),
                Vector2.zero,
                Vector2.zero);
            viewportImage.raycastTarget = true;
            viewportImage.gameObject.AddComponent<RectMask2D>();
            _detailsViewport = (RectTransform)viewportImage.transform;
            _scrollRect = viewportImage.gameObject.AddComponent<ScrollRect>();
            _scrollRect.horizontal = false;
            _scrollRect.vertical = true;
            _scrollRect.movementType = ScrollRect.MovementType.Clamped;
            _scrollRect.scrollSensitivity = 34f;
            _scrollRect.viewport = _detailsViewport;

            _detailsContent = ui.CreateRect(
                viewportImage.transform,
                "Content",
                new Vector2(0f, 1f),
                new Vector2(1f, 1f),
                new Vector2(0f, -800f),
                Vector2.zero);
            _detailsContent.pivot = new Vector2(0.5f, 1f);
            _scrollRect.content = _detailsContent;

            _debugText = ui.CreateText(
                _detailsContent,
                "DebugText",
                "Debug snapshot을 준비하는 중입니다.",
                21,
                TextAnchor.UpperLeft,
                WireframePalette.TextPrimary,
                Vector2.zero,
                Vector2.one,
                new Vector2(24f, 18f),
                new Vector2(-24f, -18f));
            _debugText.verticalOverflow = VerticalWrapMode.Overflow;

            _detailsPanel.SetActive(false);
            Root.SetActive(false);
        }

        public GameObject Root { get; }

        public bool IsVisible => Root.activeSelf;

        public bool AreDetailsVisible => _detailsPanel.activeSelf;

        public void SetVisible(bool visible)
        {
            Root.SetActive(visible);
            if (!visible)
            {
                SetDetailsVisible(false);
            }
        }

        public void Render(
            string date,
            string demoDayName,
            string profileName,
            string debugInfo)
        {
            string safeDate = string.IsNullOrWhiteSpace(date) ? "-" : date;
            string safeDay = string.IsNullOrWhiteSpace(demoDayName) ? "-" : demoDayName;
            string safeProfile = string.IsNullOrWhiteSpace(profileName) ? "-" : profileName;
            _summaryText.text = $"{safeDay} · {safeProfile}";
            _detailsSummaryText.text = $"날짜 {safeDate} · {safeDay} · {safeProfile}";
            _debugText.text = string.IsNullOrWhiteSpace(debugInfo)
                ? "Debug snapshot이 비어 있습니다."
                : debugInfo;

            Canvas.ForceUpdateCanvases();
            float height = Mathf.Max(_detailsViewport.rect.height, _debugText.preferredHeight + 48f);
            _detailsContent.SetSizeWithCurrentAnchors(RectTransform.Axis.Vertical, height);
            LayoutRebuilder.ForceRebuildLayoutImmediate(_detailsContent);
            _scrollRect.verticalNormalizedPosition = 1f;
        }

        private void SetDetailsVisible(bool visible)
        {
            _controlPanel.SetActive(!visible);
            _detailsPanel.SetActive(visible);
            if (visible)
            {
                _scrollRect.verticalNormalizedPosition = 1f;
            }
        }

        private static void CreateSectionLabel(
            WireframeUi ui,
            Transform parent,
            string name,
            string label,
            float minY,
            float maxY)
        {
            ui.CreateText(
                parent,
                name,
                label,
                19,
                TextAnchor.MiddleLeft,
                WireframePalette.TextSecondary,
                new Vector2(0.025f, minY),
                new Vector2(0.975f, maxY),
                Vector2.zero,
                Vector2.zero,
                FontStyle.Bold);
        }

        private static void CreateTwoButtons(
            WireframeUi ui,
            Transform parent,
            string firstName,
            string firstLabel,
            Action firstAction,
            string secondName,
            string secondLabel,
            Action secondAction,
            float minY,
            float maxY,
            Color? color = null)
        {
            ui.CreateButton(
                parent,
                firstName,
                firstLabel,
                () => firstAction?.Invoke(),
                new Vector2(0.025f, minY),
                new Vector2(0.49f, maxY),
                Vector2.zero,
                new Vector2(-8f, 0f),
                color ?? WireframePalette.PanelRaised,
                22);
            ui.CreateButton(
                parent,
                secondName,
                secondLabel,
                () => secondAction?.Invoke(),
                new Vector2(0.51f, minY),
                new Vector2(0.975f, maxY),
                new Vector2(8f, 0f),
                Vector2.zero,
                color ?? WireframePalette.PanelRaised,
                22);
        }

        private static void CreateThreeButtons(
            WireframeUi ui,
            Transform parent,
            string firstName,
            string firstLabel,
            Action firstAction,
            string secondName,
            string secondLabel,
            Action secondAction,
            string thirdName,
            string thirdLabel,
            Action thirdAction,
            float minY,
            float maxY)
        {
            ui.CreateButton(
                parent,
                firstName,
                firstLabel,
                () => firstAction?.Invoke(),
                new Vector2(0.025f, minY),
                new Vector2(0.335f, maxY),
                Vector2.zero,
                new Vector2(-7f, 0f),
                WireframePalette.PanelRaised,
                19);
            ui.CreateButton(
                parent,
                secondName,
                secondLabel,
                () => secondAction?.Invoke(),
                new Vector2(0.335f, minY),
                new Vector2(0.665f, maxY),
                new Vector2(7f, 0f),
                new Vector2(-7f, 0f),
                WireframePalette.PanelRaised,
                18);
            ui.CreateButton(
                parent,
                thirdName,
                thirdLabel,
                () => thirdAction?.Invoke(),
                new Vector2(0.665f, minY),
                new Vector2(0.975f, maxY),
                new Vector2(7f, 0f),
                Vector2.zero,
                WireframePalette.PanelRaised,
                19);
        }
    }
}
