using System;
using UnityEngine;
using UnityEngine.Events;
using UnityEngine.UI;

namespace TodoSpirits.Presentation.Common
{
    internal sealed class WireframeUi
    {
        private readonly Font _font;

        public WireframeUi(Font font)
        {
            _font = font;
        }

        public RectTransform CreateRect(
            Transform parent,
            string name,
            Vector2 anchorMin,
            Vector2 anchorMax,
            Vector2 offsetMin,
            Vector2 offsetMax)
        {
            var gameObject = new GameObject(name, typeof(RectTransform));
            var rect = (RectTransform)gameObject.transform;
            rect.SetParent(parent, false);
            SetRect(rect, anchorMin, anchorMax, offsetMin, offsetMax);
            return rect;
        }

        public Image CreatePanel(
            Transform parent,
            string name,
            Color color,
            Vector2 anchorMin,
            Vector2 anchorMax,
            Vector2 offsetMin,
            Vector2 offsetMax)
        {
            var gameObject = new GameObject(name, typeof(RectTransform), typeof(CanvasRenderer), typeof(Image));
            var rect = (RectTransform)gameObject.transform;
            rect.SetParent(parent, false);
            SetRect(rect, anchorMin, anchorMax, offsetMin, offsetMax);
            var image = gameObject.GetComponent<Image>();
            image.color = color;
            image.raycastTarget = false;
            return image;
        }

        public Text CreateText(
            Transform parent,
            string name,
            string value,
            int fontSize,
            TextAnchor alignment,
            Color color,
            Vector2 anchorMin,
            Vector2 anchorMax,
            Vector2 offsetMin,
            Vector2 offsetMax,
            FontStyle fontStyle = FontStyle.Normal)
        {
            var gameObject = new GameObject(name, typeof(RectTransform), typeof(CanvasRenderer), typeof(Text));
            var rect = (RectTransform)gameObject.transform;
            rect.SetParent(parent, false);
            SetRect(rect, anchorMin, anchorMax, offsetMin, offsetMax);
            var text = gameObject.GetComponent<Text>();
            text.font = _font;
            text.text = value;
            text.fontSize = fontSize;
            text.fontStyle = fontStyle;
            text.alignment = alignment;
            text.color = color;
            text.raycastTarget = false;
            text.horizontalOverflow = HorizontalWrapMode.Wrap;
            text.verticalOverflow = VerticalWrapMode.Truncate;
            text.supportRichText = true;
            return text;
        }

        public Button CreateButton(
            Transform parent,
            string name,
            string label,
            UnityAction onClick,
            Vector2 anchorMin,
            Vector2 anchorMax,
            Vector2 offsetMin,
            Vector2 offsetMax,
            Color? background = null,
            int fontSize = 30)
        {
            var gameObject = new GameObject(name, typeof(RectTransform), typeof(CanvasRenderer), typeof(Image), typeof(Button));
            var rect = (RectTransform)gameObject.transform;
            rect.SetParent(parent, false);
            SetRect(rect, anchorMin, anchorMax, offsetMin, offsetMax);

            var image = gameObject.GetComponent<Image>();
            image.color = background ?? WireframePalette.PanelRaised;
            var button = gameObject.GetComponent<Button>();
            button.targetGraphic = image;
            button.onClick.AddListener(onClick);
            ColorBlock colors = button.colors;
            colors.normalColor = Color.white;
            colors.highlightedColor = new Color(1.08f, 1.08f, 1.08f, 1f);
            colors.pressedColor = new Color(0.82f, 0.82f, 0.82f, 1f);
            colors.selectedColor = colors.highlightedColor;
            button.colors = colors;

            CreateText(
                rect,
                "Label",
                label,
                fontSize,
                TextAnchor.MiddleCenter,
                WireframePalette.TextPrimary,
                Vector2.zero,
                Vector2.one,
                new Vector2(12f, 8f),
                new Vector2(-12f, -8f),
                FontStyle.Bold);

            return button;
        }

        public static void Stretch(RectTransform rect)
        {
            SetRect(rect, Vector2.zero, Vector2.one, Vector2.zero, Vector2.zero);
        }

        public static void SetRect(
            RectTransform rect,
            Vector2 anchorMin,
            Vector2 anchorMax,
            Vector2 offsetMin,
            Vector2 offsetMax)
        {
            rect.anchorMin = anchorMin;
            rect.anchorMax = anchorMax;
            rect.offsetMin = offsetMin;
            rect.offsetMax = offsetMax;
        }
    }
}
