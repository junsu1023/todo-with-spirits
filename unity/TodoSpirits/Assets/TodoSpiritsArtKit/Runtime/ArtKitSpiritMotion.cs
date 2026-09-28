using UnityEngine;
using UnityEngine.EventSystems;
using UnityEngine.UI;

namespace TodoSpirits.ArtKit
{
    [DisallowMultipleComponent]
    public sealed class ArtKitSpiritMotion : MonoBehaviour, IPointerClickHandler
    {
        public RectTransform visual;
        public Image body;
        public RectTransform shadow;
        public Sprite neutral, halfClosed, closed, happy, sleep;
        [Min(0)] public float floatAmplitude = 7;
        [Min(.2f)] public float floatPeriod = 3.4f;
        public bool sleeping;
        private Vector2 basePosition;
        private Vector3 baseScale, shadowScale;
        private float clock, blinkStart = -1, nextBlink = 3.2f, reactionStart = -1, nextTap;
        private bool ready, suspended;
        private readonly System.Random visualRandom = new System.Random(1709);
        private void Awake()
        {
            if (!visual || !body || !neutral || !halfClosed || !closed || !happy || !sleep)
            {
                Debug.LogError("ArtKitSpiritMotion: missing Visual/body/expressions.", this);
                enabled = false; return;
            }
            basePosition = visual.anchoredPosition;
            baseScale = visual.localScale;
            if (shadow) shadowScale = shadow.localScale;
            ready = true;
        }
        public void OnPointerClick(PointerEventData e)
        { if (e.button == PointerEventData.InputButton.Left) PlayPetResponse(); }
        // This never grants currency, completes tasks, changes growth or saves data.
        public void PlayPetResponse()
        {
            if (!ready || sleeping || clock < nextTap) return;
            reactionStart = clock; nextTap = clock + .9f; blinkStart = -1;
        }
        private void Update()
        {
            if (!ready || suspended) return;
            clock += Mathf.Min(Time.unscaledDeltaTime, .05f);
            bool reduced = ArtKitMotionPolicy.ReduceMotion;
            float wave = reduced ? 0 : Mathf.Sin(clock * Mathf.PI * 2 / floatPeriod);
            float y = wave * floatAmplitude;
            Vector3 scale = baseScale;
            if (!reduced)
                scale = Vector3.Scale(baseScale, new Vector3(1 - wave * .006f, 1 + wave * .012f, 1));
            Sprite expression = sleeping ? sleep : neutral;
            if (reactionStart >= 0)
            {
                float p = (clock - reactionStart) / .65f;
                if (p >= 1) reactionStart = -1;
                else
                {
                    expression = happy;
                    if (!reduced)
                    {
                        float hop = Mathf.Sin(p * Mathf.PI);
                        y += hop * 16;
                        scale = Vector3.Scale(scale, new Vector3(1 + .025f * hop, 1 - .015f * hop, 1));
                    }
                }
            }
            else if (!sleeping && !reduced)
            {
                if (clock >= nextBlink && blinkStart < 0) blinkStart = clock;
                if (blinkStart >= 0)
                {
                    float t = clock - blinkStart;
                    expression = t < .05f ? halfClosed : t < .13f ? closed : t < .18f ? halfClosed : neutral;
                    if (t >= .18f)
                    {
                        blinkStart = -1;
                        nextBlink = clock + 3 + (float)visualRandom.NextDouble() * 2.5f;
                    }
                }
            }
            visual.anchoredPosition = basePosition + Vector2.up * y;
            visual.localScale = scale;
            body.sprite = expression;
            if (shadow) shadow.localScale = shadowScale * (1 - Mathf.Clamp(y, -12, 26) * .002f);
        }
        private void OnApplicationPause(bool paused) { suspended = paused; }
        private void OnDisable()
        {
            if (!ready) return;
            visual.anchoredPosition = basePosition; visual.localScale = baseScale;
            if (shadow) shadow.localScale = shadowScale;
            body.sprite = sleeping ? sleep : neutral;
            reactionStart = blinkStart = -1; nextBlink = clock + 3;
        }
    }
}
