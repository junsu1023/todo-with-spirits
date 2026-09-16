using System;
using UnityEngine;
using UnityEngine.UI;

namespace TodoSpirits.Presentation.Common
{
    internal sealed class CompanionMenuView
    {
        private readonly WireframeUi _ui;
        private readonly RectTransform _content;
        private readonly Text _title;
        private readonly Text _notice;
        private readonly ScrollRect _scroll;
        public GameObject Root { get; }
        public Action Back;

        public CompanionMenuView(WireframeUi ui, Transform parent)
        {
            _ui = ui;
            Root = ui.CreatePanel(parent, "CompanionMenu", WireframePalette.Background,
                Vector2.zero, Vector2.one, Vector2.zero, Vector2.zero).gameObject;
            ui.CreateButton(Root.transform, "Back", "‹ 돌아가기", () => Back?.Invoke(),
                new Vector2(.04f,.925f), new Vector2(.31f,.985f), Vector2.zero, Vector2.zero, fontSize: 28);
            _title = ui.CreateText(Root.transform, "Title", "동행", 44, TextAnchor.MiddleLeft,
                WireframePalette.TextPrimary, new Vector2(.06f,.83f), new Vector2(.94f,.92f), Vector2.zero, Vector2.zero, FontStyle.Bold);
            var viewport = ui.CreatePanel(Root.transform, "Viewport", WireframePalette.Background,
                new Vector2(.06f,.12f), new Vector2(.94f,.82f), Vector2.zero, Vector2.zero);
            viewport.raycastTarget = true;
            viewport.gameObject.AddComponent<RectMask2D>();
            _content = ui.CreateRect(viewport.transform, "Content", new Vector2(0,1), Vector2.one, Vector2.zero, Vector2.zero);
            _content.pivot = new Vector2(.5f,1);
            var layout = _content.gameObject.AddComponent<VerticalLayoutGroup>();
            layout.spacing = 18;
            layout.childControlWidth = true;
            layout.childControlHeight = true;
            layout.childForceExpandWidth = true;
            layout.childForceExpandHeight = false;
            var fitter = _content.gameObject.AddComponent<ContentSizeFitter>();
            fitter.verticalFit = ContentSizeFitter.FitMode.PreferredSize;
            _scroll = viewport.gameObject.AddComponent<ScrollRect>();
            _scroll.viewport = viewport.rectTransform; _scroll.content = _content; _scroll.horizontal = false;
            _scroll.movementType = ScrollRect.MovementType.Clamped;
            _notice = ui.CreateText(Root.transform, "Notice", "", 26, TextAnchor.MiddleCenter,
                WireframePalette.TextSecondary, new Vector2(.06f,.025f), new Vector2(.94f,.115f), Vector2.zero, Vector2.zero);
            _notice.supportRichText = false;
            Root.SetActive(false);
        }
        public void Clear(string title, Action back)
        {
            foreach (Transform child in _content) { child.gameObject.SetActive(false); UnityEngine.Object.Destroy(child.gameObject); }
            _title.text = title; _notice.text = ""; Back = back;
            _scroll.StopMovement(); _content.anchoredPosition = Vector2.zero;
        }
        public void Notice(string text) => _notice.text = text;
        private void Place(RectTransform rect, float height)
        {
            // Minimum size preserves short rows; Text supplies its wrapped preferred height.
            var element = rect.gameObject.AddComponent<LayoutElement>();
            element.minHeight = height;
        }
        public void Text(string value, float height = 150)
        {
            var text = _ui.CreateText(_content, "Paragraph", value, 30, TextAnchor.UpperLeft,
                WireframePalette.TextPrimary, Vector2.zero, Vector2.one, Vector2.zero, Vector2.zero);
            text.supportRichText = false;
            Place(text.rectTransform, height);
        }

        public void Portrait(TodoSpirits.Presentation.Main.SpiritLivingActor authoredActor, TodoSpirits.Core.CompanionLife life)
        {
            var panel = _ui.CreatePanel(_content, "CompanionPortrait", WireframePalette.Panel,
                Vector2.zero, Vector2.one, Vector2.zero, Vector2.zero);
            panel.raycastTarget = false;
            Place(panel.rectTransform, 300);
            var destination = _ui.CreateRect(panel.transform, "PortraitDestination",new Vector2(.5f,.5f),new Vector2(.5f,.5f),Vector2.zero,Vector2.zero);
            var actor = UnityEngine.Object.Instantiate(authoredActor, panel.transform, false);
            actor.name = "PortraitActor";
            var rect = actor.ActorRoot;
            rect.anchorMin = rect.anchorMax = new Vector2(.5f,.5f);
            rect.anchoredPosition = Vector2.zero;
            actor.PresentLife(life);
            actor.Present(life.LastAction, destination, null, false);
        }
        public Button Button(string label, Action action, bool enabled = true)
        {
            var button = _ui.CreateButton(_content, "Action", label, () => action(), Vector2.zero, Vector2.one,
                Vector2.zero, Vector2.zero, fontSize: 29);
            button.GetComponentInChildren<Text>().supportRichText = false;
            button.interactable = enabled;
            Place((RectTransform)button.transform, 100);
            return button;
        }
        public InputField NameInput()
        {
            var panel = _ui.CreatePanel(_content, "NameInput", WireframePalette.PanelRaised, Vector2.zero, Vector2.one, Vector2.zero, Vector2.zero);
            panel.raycastTarget = true;
            Place(panel.rectTransform, 110);
            var input = panel.gameObject.AddComponent<InputField>();
            input.targetGraphic = panel;
            input.textComponent = _ui.CreateText(panel.transform, "Value", "", 34, TextAnchor.MiddleLeft,
                WireframePalette.TextPrimary, Vector2.zero, Vector2.one, new Vector2(20,10), new Vector2(-20,-10));
            input.textComponent.supportRichText = false;
            input.placeholder = _ui.CreateText(panel.transform, "Placeholder", "기억할 이름을 입력해 주세요", 30, TextAnchor.MiddleLeft,
                WireframePalette.TextSecondary, Vector2.zero, Vector2.one, new Vector2(20,10), new Vector2(-20,-10));
            input.characterLimit = 24;
            return input;
        }
    }
}
