using UnityEngine;

namespace TodoSpirits.ArtKit
{
    // Put directly under a full-screen Canvas; put PortraitStage under this object.
    [DisallowMultipleComponent]
    [RequireComponent(typeof(RectTransform))]
    public sealed class ArtKitSafeArea : MonoBehaviour
    {
        private RectTransform rect;
        private Rect previous;
        private Vector2Int previousScreen;
        private void Awake()
        {
            rect = (RectTransform)transform;
            var parentCanvas = transform.parent ? transform.parent.GetComponent<Canvas>() : null;
            if (!parentCanvas || !parentCanvas.isRootCanvas || parentCanvas.renderMode == RenderMode.WorldSpace)
            {
                Debug.LogError("ArtKitSafeArea requires a direct full-screen Canvas parent.", this);
                enabled = false; return;
            }
            Apply(true);
        }
        private void Update() { Apply(false); }
        private void Apply(bool force)
        {
            int width = Screen.width, height = Screen.height;
            if (width < 1 || height < 1) return;
            Rect area = Screen.safeArea;
            if (!force && area == previous && previousScreen == new Vector2Int(width, height)) return;
            previous = area; previousScreen = new Vector2Int(width, height);
            rect.anchorMin = new Vector2(area.xMin / width, area.yMin / height);
            rect.anchorMax = new Vector2(area.xMax / width, area.yMax / height);
            rect.offsetMin = rect.offsetMax = Vector2.zero;
        }
    }
}
