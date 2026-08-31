using UnityEngine;

namespace TodoSpirits.Presentation.Common
{
    internal static class WireframePalette
    {
        // Internal Concept Demo palette: intentionally soft and editable, not final art direction.
        public static readonly Color Background = Hex("#F5F0E5");
        public static readonly Color Panel = Hex("#E5ECDB");
        public static readonly Color PanelRaised = Hex("#FAF6EB");
        public static readonly Color World = Hex("#DDE9D6");
        public static readonly Color Spot = Hex("#D5E3D4");
        public static readonly Color Accent = Hex("#7E9D78");
        public static readonly Color AccentDark = Hex("#B4CDA8");
        public static readonly Color TextPrimary = Hex("#33423C");
        public static readonly Color TextSecondary = Hex("#64726B");
        public static readonly Color Debug = Hex("#D8EAF0");
        public static readonly Color Spirit = Hex("#E7CF91");
        public static readonly Color Transparent = new Color(0f, 0f, 0f, 0f);

        private static Color Hex(string value)
        {
            return ColorUtility.TryParseHtmlString(value, out Color color) ? color : Color.magenta;
        }
    }
}
