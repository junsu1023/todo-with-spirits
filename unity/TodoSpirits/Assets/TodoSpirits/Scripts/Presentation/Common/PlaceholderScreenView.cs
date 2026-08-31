using System;
using TodoSpirits.Presentation.Common;
using UnityEngine;
using UnityEngine.UI;

namespace TodoSpirits.Presentation.Common
{
    internal sealed class PlaceholderScreenView
    {
        public PlaceholderScreenView(
            WireframeUi ui,
            Transform safeAreaRoot,
            string name,
            string title,
            Action goBack,
            string message = null)
        {
            Image rootPanel = ui.CreatePanel(
                safeAreaRoot,
                name + "Screen",
                WireframePalette.Background,
                Vector2.zero,
                Vector2.one,
                Vector2.zero,
                Vector2.zero);
            Root = rootPanel.gameObject;

            ui.CreateButton(
                Root.transform,
                "BackButton",
                "‹  Back",
                () => goBack(),
                new Vector2(0.035f, 0.925f),
                new Vector2(0.28f, 0.985f),
                Vector2.zero,
                Vector2.zero,
                WireframePalette.PanelRaised,
                25);

            ui.CreateText(
                Root.transform,
                "Title",
                title,
                52,
                TextAnchor.MiddleCenter,
                WireframePalette.TextPrimary,
                new Vector2(0.08f, 0.55f),
                new Vector2(0.92f, 0.72f),
                Vector2.zero,
                Vector2.zero,
                FontStyle.Bold);

            Image placeholder = ui.CreatePanel(
                Root.transform,
                "Placeholder",
                WireframePalette.Panel,
                new Vector2(0.12f, 0.34f),
                new Vector2(0.88f, 0.54f),
                Vector2.zero,
                Vector2.zero);
            ui.CreateText(
                placeholder.transform,
                "Message",
                string.IsNullOrWhiteSpace(message) ? "Prototype에서 이후 구현 예정" : message,
                29,
                TextAnchor.MiddleCenter,
                WireframePalette.TextSecondary,
                Vector2.zero,
                Vector2.one,
                new Vector2(30f, 20f),
                new Vector2(-30f, -20f));

            Root.SetActive(false);
        }

        public GameObject Root { get; }
    }
}
