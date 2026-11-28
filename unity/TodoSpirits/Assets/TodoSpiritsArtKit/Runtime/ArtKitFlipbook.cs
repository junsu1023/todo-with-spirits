using System;
using UnityEngine;
using UnityEngine.Events;
using UnityEngine.UI;

namespace TodoSpirits.ArtKit
{
    [DisallowMultipleComponent]
    [RequireComponent(typeof(Image))]
    public sealed class ArtKitFlipbook : MonoBehaviour
    {
        public Image target;
        public Sprite[] frames = Array.Empty<Sprite>();
        [Min(1)] public float fps = 20;
        public bool loop;
        public bool playOnEnable;
        public bool hideOnFinish = true;
        public UnityEvent finished = new UnityEvent();
        private bool playing;
        private float elapsed;
        private int lastIndex = -1;
        private void Awake() { if (!target) target = GetComponent<Image>(); }
        private void OnEnable() { if (playOnEnable) Play(); }
        public void Configure(ArtKitCatalog.Sequence sequence)
        {
            if (sequence == null) throw new ArgumentNullException(nameof(sequence));
            frames = sequence.frames; fps = sequence.fps; loop = sequence.loop;
        }
        public void Play()
        {
            if (!target) target = GetComponent<Image>();
            if (frames == null || frames.Length == 0 || fps <= 0)
            { Debug.LogError("ArtKitFlipbook: empty frames or invalid FPS.", this); return; }
            elapsed = 0; lastIndex = -1; playing = true; target.enabled = true;
            if (ArtKitMotionPolicy.ReduceMotion)
            {
                // The enclosing UI state remains visible. Decorative effects simply do not play.
                playing = false; if (hideOnFinish) target.enabled = false;
                else target.sprite = frames[0];
                finished.Invoke(); return;
            }
            ApplyFrame(0);
        }
        private void Update()
        {
            if (!playing) return;
            if (ArtKitMotionPolicy.ReduceMotion) { Stop(); return; }
            elapsed += Mathf.Min(Time.unscaledDeltaTime, .05f);
            int index = Mathf.FloorToInt(elapsed * fps);
            if (index >= frames.Length)
            {
                if (loop) { elapsed %= frames.Length / fps; index %= frames.Length; }
                else { Stop(); finished.Invoke(); return; }
            }
            ApplyFrame(index);
        }
        private void ApplyFrame(int index)
        {
            if (lastIndex == index) return;
            lastIndex = index; target.sprite = frames[index];
        }
        public void Stop() { playing = false; if (target && hideOnFinish) target.enabled = false; }
        private void OnDisable() { playing = false; elapsed = 0; lastIndex = -1; }
    }
}
