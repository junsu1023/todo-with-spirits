using UnityEngine;

namespace TodoSpirits.Presentation.Common
{
    internal static class RuntimeFontProvider
    {
        private static readonly string[] KoreanFontCandidates =
        {
            "Malgun Gothic",
            "Apple SD Gothic Neo",
            "Noto Sans CJK KR",
            "Noto Sans KR",
            "Arial Unicode MS"
        };

        private static Font _cachedFont;

        public static Font GetFont()
        {
            if (_cachedFont != null)
            {
                return _cachedFont;
            }

            for (int i = 0; i < KoreanFontCandidates.Length; i++)
            {
                try
                {
                    Font candidate = Font.CreateDynamicFontFromOSFont(KoreanFontCandidates[i], 28);
                    if (candidate != null)
                    {
                        _cachedFont = candidate;
                        _cachedFont.name = "TodoSpirits Runtime Korean Font";
                        return _cachedFont;
                    }
                }
                catch (System.Exception exception)
                {
                    Debug.LogWarning($"TodoSpirits: OS font '{KoreanFontCandidates[i]}' could not be loaded. {exception.Message}");
                }
            }

            _cachedFont = Resources.GetBuiltinResource<Font>("LegacyRuntime.ttf");
            if (_cachedFont == null)
            {
                Debug.LogError("TodoSpirits: Neither a Korean OS font nor LegacyRuntime.ttf could be loaded.");
            }

            return _cachedFont;
        }
    }
}
