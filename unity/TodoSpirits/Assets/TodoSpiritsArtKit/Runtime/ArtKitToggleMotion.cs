using UnityEngine;
using UnityEngine.UI;

namespace TodoSpirits.ArtKit
{
    [DisallowMultipleComponent]
    [RequireComponent(typeof(Toggle))]
    public sealed class ArtKitToggleMotion : MonoBehaviour
    {
        public Toggle owner;
        public RectTransform thumb;
        public Image track;
        public Sprite onTrack, offTrack;
        public float travel = 30;
        private float value;
        private float baseY;
        private bool ready;
        private void Awake()
        {
            if (!owner) owner = GetComponent<Toggle>();
            if (!thumb || !track || !onTrack || !offTrack)
            { Debug.LogError("ArtKitToggleMotion: missing visual references.", this); enabled = false; return; }
            baseY = thumb.anchoredPosition.y; value = owner.isOn ? 1 : 0; ready = true; Apply();
        }
        private void Update()
        {
            if (!ready) return;
            float target = owner.isOn ? 1 : 0;
            value = ArtKitMotionPolicy.ReduceMotion ? target :
                Mathf.MoveTowards(value, target, Mathf.Min(Time.unscaledDeltaTime, .05f) / .13f);
            Apply();
        }
        private void Apply()
        {
            float smooth = value * value * (3 - 2 * value);
            thumb.anchoredPosition = new Vector2(Mathf.Lerp(-travel, travel, smooth), baseY);
            track.sprite = owner.isOn ? onTrack : offTrack;
            track.color = owner.IsInteractable() ? Color.white : new Color(.82f, .82f, .82f, 1);
        }
    }
}
