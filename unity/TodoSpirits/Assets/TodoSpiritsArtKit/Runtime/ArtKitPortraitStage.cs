using UnityEngine;

namespace TodoSpirits.ArtKit
{
    // Keeps the interactive game portrait on tablets and wide browsers; scenery may fill the viewport.
    [DisallowMultipleComponent]
    [RequireComponent(typeof(RectTransform))]
    public sealed class ArtKitPortraitStage : MonoBehaviour
    {
        [Range(.4f, .9f)] public float maximumAspect = 9f / 16f;
        private RectTransform rect, parentRect;
        private Vector2 previous;
        private void Awake()
        {
            rect = (RectTransform)transform; parentRect = transform.parent as RectTransform;
            if (!parentRect) { Debug.LogError("ArtKitPortraitStage requires a RectTransform parent.", this); enabled = false; return; }
            Apply(true);
        }
        private void LateUpdate() { Apply(false); }
        private void Apply(bool force)
        {
            Vector2 size = parentRect.rect.size;
            if (!force && size == previous) return;
            previous = size;
            rect.anchorMin = rect.anchorMax = new Vector2(.5f, .5f);
            rect.pivot = new Vector2(.5f, .5f); rect.anchoredPosition = Vector2.zero;
            rect.sizeDelta = new Vector2(Mathf.Min(size.x, size.y * maximumAspect), size.y);
        }
    }
}
