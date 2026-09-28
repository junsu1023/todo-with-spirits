using System;
using UnityEngine;
using UnityEngine.UI;

namespace TodoSpirits.Presentation.Forest
{
    internal sealed class ForestUi
    {
        public static readonly Color Paper = Hex("FAF8F1");
        public static readonly Color Ink = Hex("293E36");
        public static readonly Color Moss = Hex("365B47");
        public static readonly Color Rose = Hex("A4667D");
        public static readonly Color Muted = Hex("68756B");
        public static readonly Color Pale = Hex("E8EEE3");
        public static readonly Color Sage = Hex("DDE8D9");
        public static readonly Color Pink = Hex("DCABBC");
        public static readonly Color Line = Hex("E2E5DB");
        private readonly Font _font;
        private readonly Font _bold;
        private static Sprite _rounded;
        private static Sprite _pill;

        public ForestUi(Font font, Font bold) { _font = font; _bold = bold != null ? bold : font; }
        public static bool ReduceMotion { get => ForestUiMotion.ReduceMotion; set => ForestUiMotion.ReduceMotion = value; }
        private static Color Hex(string hex) { ColorUtility.TryParseHtmlString("#" + hex, out var color); return color; }

        public RectTransform Rect(string name, Transform parent, float x, float y, float width, float height)
        {
            var rect = new GameObject(name, typeof(RectTransform)).GetComponent<RectTransform>();
            rect.SetParent(parent, false);
            rect.anchorMin = rect.anchorMax = new Vector2(0, 1);
            rect.pivot = new Vector2(0, 1);
            rect.anchoredPosition = new Vector2(x, -y);
            rect.sizeDelta = new Vector2(width, height);
            return rect;
        }

        public void Fill(RectTransform rect)
        {
            rect.anchorMin = Vector2.zero;
            rect.anchorMax = Vector2.one;
            rect.offsetMin = rect.offsetMax = Vector2.zero;
        }

        public Image Panel(string name, Transform parent, float x, float y, float width, float height, Color color, bool rounded = true)
        {
            var image = Rect(name, parent, x, y, width, height).gameObject.AddComponent<Image>();
            image.color = color;
            if (rounded) { image.sprite = Rounded; image.type = Image.Type.Sliced; }
            return image;
        }

        public Image Card(string name, Transform parent, float x, float y, float width, float height, Color? color = null)
        {
            var panel = Panel(name, parent, x, y, width, height, color ?? Color.white);
            var shadow = panel.gameObject.AddComponent<Shadow>();
            shadow.effectColor = new Color(Ink.r, Ink.g, Ink.b, .035f);
            shadow.effectDistance = new Vector2(0, -2);
            var edge = panel.gameObject.AddComponent<Outline>();
            edge.effectColor = new Color(Line.r, Line.g, Line.b, .7f);
            edge.effectDistance = new Vector2(1, -1);
            edge.useGraphicAlpha = true;
            return panel;
        }

        public Image Divider(Transform parent, float x, float y, float width, Color? color = null)
        {
            var line = Panel("Divider", parent, x, y, width, 1, color ?? Line, false);
            line.raycastTarget = false;
            return line;
        }

        public RectTransform Badge(string text, Transform parent, float x, float y, float width, float height, Color? color = null, Color? textColor = null)
        {
            var panel = Panel("Badge", parent, x, y, width, height, color ?? Sage);
            panel.sprite = Pill;
            panel.raycastTarget = false;
            Text(text, panel.transform, 8, 0, Mathf.Max(1, width - 16), height, 14, textColor ?? Moss, true, TextAnchor.MiddleCenter);
            return panel.rectTransform;
        }

        public Text Text(string text, Transform parent, float x, float y, float width, float height, int size = 22, Color? color = null, bool bold = false, TextAnchor align = TextAnchor.UpperLeft)
        {
            var label = Rect("Text", parent, x, y, width, height).gameObject.AddComponent<Text>();
            label.text = text;
            label.font = bold ? _bold : _font;
            label.fontSize = size;
            label.color = color ?? Ink;
            label.alignment = align;
            label.lineSpacing = 1.08f;
            label.supportRichText = false;
            label.raycastTarget = false;
            label.horizontalOverflow = HorizontalWrapMode.Wrap;
            label.verticalOverflow = VerticalWrapMode.Truncate;
            return label;
        }

        public Button Button(string name, string text, Transform parent, float x, float y, float width, float height, Action action, bool primary = false, int size = 22)
        {
            width = Mathf.Max(44, width);
            height = Mathf.Max(44, height);
            var panel = Panel(name, parent, x, y, width, height, primary ? Moss : new Color(.965f, .974f, .945f));
            if (!primary)
            {
                var edge = panel.gameObject.AddComponent<Outline>();
                edge.effectColor = new Color(.73f, .78f, .70f, .30f);
                edge.effectDistance = new Vector2(.65f, -.65f);
            }
            var button = ConfigureButton(panel, action);
            float inset = Mathf.Min(16, width * .14f);
            Text(text, panel.transform, inset, 0, width - inset * 2, height, size, primary ? Paper : Ink, primary, TextAnchor.MiddleCenter);
            return button;
        }

        public ForestIconGraphic Icon(ForestIcon icon, Transform parent, float x, float y, float width, float height, Color color)
        {
            var graphic = Rect(icon + "Icon", parent, x, y, width, height).gameObject.AddComponent<ForestIconGraphic>();
            graphic.Icon = icon;
            graphic.color = color;
            graphic.raycastTarget = false;
            return graphic;
        }

        public Button IconButton(string name, ForestIcon icon, Transform parent, float x, float y, float width, float height, Action action, Color? color = null)
        {
            float touchWidth = Mathf.Max(44, width), touchHeight = Mathf.Max(44, height);
            var panel = Panel(name, parent, x - (touchWidth - width) * .5f, y - (touchHeight - height) * .5f, touchWidth, touchHeight, Color.clear);
            var button = ConfigureButton(panel, action);
            float iconSize = Mathf.Min(25, Mathf.Min(width, height) * .65f);
            Icon(icon, panel.transform, (touchWidth - iconSize) * .5f, (touchHeight - iconSize) * .5f, iconSize, iconSize, color ?? Ink);
            return button;
        }

        private static Button ConfigureButton(Image panel, Action action)
        {
            var button = panel.gameObject.AddComponent<Button>();
            button.targetGraphic = panel;
            var colors = button.colors;
            colors.normalColor = Color.white;
            colors.highlightedColor = new Color(.955f, .975f, .945f);
            colors.pressedColor = new Color(.86f, .91f, .84f);
            colors.selectedColor = Color.white;
            colors.disabledColor = new Color(.83f, .86f, .82f, .5f);
            colors.fadeDuration = .12f;
            button.colors = colors;
            button.onClick.AddListener(() => action?.Invoke());
            panel.gameObject.AddComponent<ForestUiMotion>();
            return button;
        }

        public Image Sprite(string name, Sprite sprite, Transform parent, float x, float y, float width, float height)
        {
            var image = Rect(name, parent, x, y, width, height).gameObject.AddComponent<Image>();
            image.sprite = sprite;
            image.preserveAspect = true;
            image.raycastTarget = false;
            return image;
        }

        public InputField Input(Transform parent, float x, float y, float width, string placeholder, int limit)
        {
            var panel = Card("Input", parent, x, y, width, 62, Color.white);
            var input = panel.gameObject.AddComponent<InputField>();
            input.targetGraphic = panel;
            input.textComponent = Text("", panel.transform, 18, 0, width - 36, 62, 21, null, false, TextAnchor.MiddleLeft);
            input.placeholder = Text(placeholder, panel.transform, 18, 0, width - 36, 62, 20, Muted, false, TextAnchor.MiddleLeft);
            input.characterLimit = limit;
            input.lineType = InputField.LineType.SingleLine;
            input.caretColor = Moss;
            input.customCaretColor = true;
            input.selectionColor = new Color(Sage.r, Sage.g, Sage.b, .85f);
            var colors = input.colors;
            colors.highlightedColor = Color.white;
            colors.selectedColor = new Color(.965f, .986f, .949f);
            colors.pressedColor = Color.white;
            input.colors = colors;
            return input;
        }

        private static Sprite Rounded => _rounded != null ? _rounded : _rounded = CreateRounded("Forest UI corner", 14);
        private static Sprite Pill => _pill != null ? _pill : _pill = CreateRounded("Forest UI capsule", 31);

        private static Sprite CreateRounded(string name, float radius)
        {
            const int side = 64;
            var texture = new Texture2D(side, side, TextureFormat.RGBA32, false) { name = name, hideFlags = HideFlags.HideAndDontSave, filterMode = FilterMode.Bilinear, wrapMode = TextureWrapMode.Clamp };
            var pixels = new Color[side * side];
            for (int y = 0; y < side; y++)
            for (int x = 0; x < side; x++)
            {
                float dx = Mathf.Max(radius - x - .5f, x + .5f - (side - radius));
                float dy = Mathf.Max(radius - y - .5f, y + .5f - (side - radius));
                float distance = new Vector2(Mathf.Max(0, dx), Mathf.Max(0, dy)).magnitude;
                pixels[y * side + x] = new Color(1, 1, 1, Mathf.Clamp01(radius - distance + .5f));
            }
            texture.SetPixels(pixels);
            texture.Apply(false, true);
            var sprite = UnityEngine.Sprite.Create(texture, new Rect(0, 0, side, side), Vector2.one * .5f, 100, 0, SpriteMeshType.FullRect, Vector4.one * (radius + 1));
            sprite.name = name;
            sprite.hideFlags = HideFlags.HideAndDontSave;
            return sprite;
        }
    }
}
