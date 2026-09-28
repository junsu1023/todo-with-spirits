using System.Collections.Generic;
using UnityEngine;
using UnityEngine.EventSystems;
using UnityEngine.UI;

namespace TodoSpirits.Presentation.Forest
{
    /// <summary>Moves button content, keeping its surface and pointer hit rectangle stable.</summary>
    [DisallowMultipleComponent]
    public sealed class ForestUiMotion : MonoBehaviour,
        IPointerEnterHandler, IPointerDownHandler, IPointerUpHandler, IPointerExitHandler,
        ISelectHandler, IDeselectHandler, ISubmitHandler
    {
        public static bool ReduceMotion { get; set; }
        private Selectable _selectable;
        private readonly List<RectTransform> _visuals = new List<RectTransform>();
        private readonly List<Vector3> _scales = new List<Vector3>();
        private readonly List<Vector2> _positions = new List<Vector2>();
        private bool _held, _hovered, _focused, _animating;
        private int _pointer = int.MinValue;
        private float _submitUntil, _scale = 1, _depth;

        private void Awake() { _selectable = GetComponent<Selectable>(); }

        private bool CanUse => _selectable != null && _selectable.IsActive() && _selectable.IsInteractable();

        private void CaptureVisuals()
        {
            if (_animating) return;
            _visuals.Clear(); _scales.Clear(); _positions.Clear();
            foreach (Transform child in transform)
            {
                var rect = child as RectTransform;
                if (rect == null || child.GetComponent<Selectable>() != null) continue;
                _visuals.Add(rect); _scales.Add(rect.localScale); _positions.Add(rect.anchoredPosition);
            }
        }

        private void Begin() { CaptureVisuals(); _animating = true; }
        public void OnPointerEnter(PointerEventData e)
        {
            if (!CanUse) return;
            _hovered = e.pointerId < 0;
            Begin();
        }
        public void OnPointerDown(PointerEventData e)
        {
            if (!CanUse || e.button != PointerEventData.InputButton.Left || _held) return;
            _pointer = e.pointerId; _held = true; _focused = false; Begin();
        }
        public void OnPointerUp(PointerEventData e)
        {
            if (e.pointerId != _pointer) return;
            _held = false; _pointer = int.MinValue; Begin();
        }
        public void OnPointerExit(PointerEventData e)
        {
            _hovered = false;
            if (e.pointerId == _pointer) { _held = false; _pointer = int.MinValue; }
            Begin();
        }
        public void OnSelect(BaseEventData e) { _focused = !(e is PointerEventData); Begin(); }
        public void OnDeselect(BaseEventData e) { _focused = false; Begin(); }
        public void OnSubmit(BaseEventData e)
        {
            if (!CanUse) return;
            _submitUntil = Time.unscaledTime + .10f; Begin();
        }

        private void LateUpdate()
        {
            if (!_animating) return;
            if (!CanUse) { _held = _hovered = _focused = false; _submitUntil = 0; }
            bool pressed = _held || Time.unscaledTime < _submitUntil;
            float targetScale = ReduceMotion ? 1 : pressed ? .965f : _hovered || _focused ? 1.008f : 1;
            float targetDepth = ReduceMotion || !pressed ? 0 : -1.5f;
            float k = 1 - Mathf.Exp(-Time.unscaledDeltaTime / (pressed ? .035f : .065f));
            _scale = ReduceMotion ? 1 : Mathf.Lerp(_scale, targetScale, k);
            _depth = ReduceMotion ? 0 : Mathf.Lerp(_depth, targetDepth, k);
            for (int i = 0; i < _visuals.Count; i++)
            {
                var rect = _visuals[i];
                if (rect == null) continue;
                rect.localScale = _scales[i] * _scale;
                var center = Vector2.Scale(new Vector2(.5f, .5f) - rect.pivot, rect.rect.size);
                var offset = Vector2.Scale(center, new Vector2(_scales[i].x, _scales[i].y)) * (1 - _scale);
                var rotated = rect.localRotation * new Vector3(offset.x, offset.y, 0);
                rect.anchoredPosition = _positions[i] + new Vector2(rotated.x, rotated.y + _depth);
            }
            if (!pressed && !_hovered && !_focused && Mathf.Abs(_scale - 1) < .0002f && Mathf.Abs(_depth) < .005f)
                Restore();
        }

        private void Restore()
        {
            for (int i = 0; i < _visuals.Count; i++)
                if (_visuals[i] != null) { _visuals[i].localScale = _scales[i]; _visuals[i].anchoredPosition = _positions[i]; }
            _scale = 1; _depth = 0; _animating = false;
        }
        private void OnDisable() { Restore(); _held = _hovered = _focused = false; _submitUntil = 0; _pointer = int.MinValue; }
        private void OnApplicationFocus(bool focused) { if (!focused) OnDisable(); }

        [RuntimeInitializeOnLoadMethod(RuntimeInitializeLoadType.SubsystemRegistration)]
        private static void ResetStatics() { ReduceMotion = false; }
    }
}
