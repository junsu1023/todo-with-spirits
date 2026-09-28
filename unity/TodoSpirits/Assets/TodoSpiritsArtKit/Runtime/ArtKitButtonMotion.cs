using UnityEngine;
using UnityEngine.EventSystems;
using UnityEngine.UI;

namespace TodoSpirits.ArtKit
{
    [DisallowMultipleComponent]
    [RequireComponent(typeof(Selectable))]
    public sealed class ArtKitButtonMotion : MonoBehaviour,
        IPointerEnterHandler, IPointerExitHandler, IPointerDownHandler, IPointerUpHandler,
        ISelectHandler, IDeselectHandler, ISubmitHandler
    {
        public Selectable owner;
        public RectTransform visual;
        public Image surface;
        public Image keyboardFocusRing;
        public Sprite normal, highlighted, pressed, disabled;
        [Range(.9f, 1f)] public float pressScale = .965f;
        [Range(1f, 1.1f)] public float hoverScale = 1.008f;
        private Vector3 baseScale;
        private Vector2 basePosition;
        private bool hovered, held, focused, ready;
        private int activePointer = int.MinValue;
        private float submitUntil, scale = 1, depth;

        private void Awake()
        {
            if (!owner) owner = GetComponent<Selectable>();
            if (!visual || visual == transform || !surface || !normal || !highlighted || !pressed || !disabled)
            {
                Debug.LogError("ArtKitButtonMotion: wire a child Visual, surface and all four sprites. Never animate the hit target.", this);
                enabled = false; return;
            }
            baseScale = visual.localScale;
            basePosition = visual.anchoredPosition;
            owner.transition = Selectable.Transition.None;
            ready = true;
        }

        private void LateUpdate()
        {
            if (!ready) return;
            bool canUse = owner.IsActive() && owner.IsInteractable();
            if (!canUse) { held = hovered = focused = false; submitUntil = 0; activePointer = int.MinValue; }
            bool isPressed = canUse && (held || Time.unscaledTime < submitUntil);
            var wanted = !canUse ? disabled : isPressed ? pressed : hovered || focused ? highlighted : normal;
            if (surface.sprite != wanted) surface.sprite = wanted;
            if (keyboardFocusRing) keyboardFocusRing.enabled = canUse && focused;
            float targetScale = isPressed ? pressScale : canUse && (hovered || focused) ? hoverScale : 1;
            float targetDepth = isPressed ? -2 : 0;
            bool reduced = ArtKitMotionPolicy.ReduceMotion;
            float k = 1 - Mathf.Exp(-Time.unscaledDeltaTime / (isPressed ? .035f : .065f));
            scale = reduced ? 1 : Mathf.Lerp(scale, targetScale, k);
            depth = reduced ? 0 : Mathf.Lerp(depth, targetDepth, k);
            visual.localScale = baseScale * scale;
            var center = Vector2.Scale(new Vector2(.5f, .5f) - visual.pivot, visual.rect.size);
            var offset = Vector2.Scale(center, new Vector2(baseScale.x, baseScale.y)) * (1 - scale);
            var compensation = visual.localRotation * new Vector3(offset.x, offset.y, 0);
            visual.anchoredPosition = basePosition + new Vector2(compensation.x, compensation.y + depth);
        }

        public void OnPointerEnter(PointerEventData e) { if (owner && owner.IsInteractable()) hovered = true; }
        public void OnPointerExit(PointerEventData e)
        {
            hovered = false;
            if (e.pointerId == activePointer) { held = false; activePointer = int.MinValue; }
        }
        public void OnPointerDown(PointerEventData e)
        {
            if (e.button != PointerEventData.InputButton.Left || !owner || !owner.IsInteractable() || held) return;
            activePointer = e.pointerId; held = true; focused = false;
        }
        public void OnPointerUp(PointerEventData e)
        {
            if (e.pointerId != activePointer) return;
            held = false; activePointer = int.MinValue;
            if (e.pointerId >= 0) hovered = false;
        }
        public void OnSelect(BaseEventData e) { focused = !(e is PointerEventData); }
        public void OnDeselect(BaseEventData e) { focused = false; }
        public void OnSubmit(BaseEventData e)
        { if (owner && owner.IsInteractable()) submitUntil = Time.unscaledTime + .10f; }
        private void OnDisable()
        {
            held = hovered = focused = false; submitUntil = 0; activePointer = int.MinValue;
            scale = 1; depth = 0;
            if (ready && visual) { visual.localScale = baseScale; visual.anchoredPosition = basePosition; }
            if (keyboardFocusRing) keyboardFocusRing.enabled = false;
        }
        private void OnApplicationFocus(bool hasFocus) { if (!hasFocus) OnDisable(); }
    }
}
