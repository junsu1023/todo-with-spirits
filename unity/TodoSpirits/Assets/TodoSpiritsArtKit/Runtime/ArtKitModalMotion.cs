using UnityEngine;

namespace TodoSpirits.ArtKit
{
    [DisallowMultipleComponent]
    public sealed class ArtKitModalMotion : MonoBehaviour
    {
        public CanvasGroup group;
        public RectTransform visual;
        [Min(.01f)] public float openSeconds = .22f;
        [Min(.01f)] public float closeSeconds = .15f;
        public bool initiallyOpen;
        private Vector3 baseScale;
        private float value, destination;
        private bool ready;
        private void Awake()
        {
            if (!group || !visual)
            { Debug.LogError("ArtKitModalMotion: group and child Visual are required.", this); enabled = false; return; }
            baseScale = visual.localScale; ready = true;
            SetOpen(initiallyOpen, true);
        }
        public void Open() { SetOpen(true); }
        public void Close() { SetOpen(false); }
        public void SetOpen(bool open, bool instant = false)
        {
            destination = open ? 1 : 0;
            if (!ready) { initiallyOpen = open; return; }
            if (instant || ArtKitMotionPolicy.ReduceMotion) value = destination;
            // Keep pointer blocking through the closing animation to avoid click-through.
            group.blocksRaycasts = open || value > 0;
            group.interactable = open && value >= 1;
            Apply();
        }
        private void Update()
        {
            if (!ready || Mathf.Approximately(value, destination)) return;
            float duration = destination > value ? openSeconds : closeSeconds;
            value = ArtKitMotionPolicy.ReduceMotion ? destination :
                Mathf.MoveTowards(value, destination, Mathf.Min(Time.unscaledDeltaTime, .05f) / duration);
            Apply();
        }
        private void Apply()
        {
            float eased = 1 - Mathf.Pow(1 - value, 3);
            group.alpha = eased;
            visual.localScale = baseScale * (ArtKitMotionPolicy.ReduceMotion ? 1 : .965f + .035f * eased);
            group.blocksRaycasts = value > 0 || destination > 0;
            group.interactable = value >= 1 && destination > 0;
        }
        private void OnDisable()
        {
            if (!ready) return;
            value = destination = 0; Apply(); visual.localScale = baseScale;
        }
    }
}
