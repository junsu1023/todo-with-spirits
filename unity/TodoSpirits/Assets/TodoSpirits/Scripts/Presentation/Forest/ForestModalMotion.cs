using System;
using UnityEngine;

namespace TodoSpirits.Presentation.Forest
{
    /// <summary>A sheet moves down to dismiss; its blocker stays in place until completion.</summary>
    [DisallowMultipleComponent]
    public sealed class ForestModalMotion : MonoBehaviour
    {
        public bool IsClosing => _state == State.Closing;
        public bool IsAnimating => _state == State.Opening || _state == State.Closing;
        private enum State { Closed, Opening, Open, Closing }
        private State _state;
        private RectTransform _sheet;
        private CanvasGroup _dim, _sheetGroup, _shield;
        private Vector2 _openPosition, _fromPosition, _targetPosition;
        private float _openDimAlpha, _fromDimAlpha, _fromSheetAlpha, _elapsed, _duration;
        private bool _reduced, _ready;
        private Action _onClosed;

        /// <param name="dim">CanvasGroup on the backdrop only, not an ancestor of the sheet.</param>
        /// <param name="inputShield">Optional root CanvasGroup. Its raycast blocking persists during exit.</param>
        public void Configure(RectTransform sheet, CanvasGroup dim, CanvasGroup inputShield = null)
        {
            if (sheet == null) throw new ArgumentNullException(nameof(sheet));
            if (dim == null) throw new ArgumentNullException(nameof(dim));
            CompleteImmediately();
            _sheet = sheet;
            _dim = dim;
            _shield = inputShield;
            _sheetGroup = sheet.GetComponent<CanvasGroup>();
            if (_sheetGroup == null) _sheetGroup = sheet.gameObject.AddComponent<CanvasGroup>();
            _openPosition = sheet.anchoredPosition;
            _openDimAlpha = dim.alpha;
            _ready = true;
            _state = State.Closed;
        }

        public void Present(bool reducedMotion = false)
        {
            if (!_ready || _state == State.Closing) return;
            _reduced = reducedMotion || ForestUiMotion.ReduceMotion;
            _state = State.Opening;
            _elapsed = 0;
            _duration = _reduced ? .10f : .28f;
            _fromPosition = _openPosition + Vector2.down * (_reduced ? 0 : 24);
            _targetPosition = _openPosition;
            _fromDimAlpha = 0;
            _fromSheetAlpha = 0;
            SetBlocked(true);
            Apply(0);
        }

        /// <summary>Returns false for repeated close requests. Completion is invoked at most once.</summary>
        public bool Dismiss(Action onClosed, bool reducedMotion = false)
        {
            if (!_ready || _state == State.Closing || _state == State.Closed) return false;
            _reduced = reducedMotion || ForestUiMotion.ReduceMotion;
            _state = State.Closing;
            _onClosed = onClosed;
            _elapsed = 0;
            _duration = _reduced ? .10f : .30f;
            _fromPosition = _sheet.anchoredPosition;
            _targetPosition = _openPosition + Vector2.down * (_reduced ? 0 : ExitDistance());
            _fromDimAlpha = _dim.alpha;
            _fromSheetAlpha = _sheetGroup.alpha;
            SetBlocked(true);
            return true;
        }

        public void CompleteImmediately()
        {
            if (!_ready || !IsAnimating) return;
            Apply(1);
            Finish();
        }

        private void Update()
        {
            if (!IsAnimating) return;
            _elapsed += Time.unscaledDeltaTime;
            float t = Mathf.Clamp01(_elapsed / _duration);
            Apply(t);
            if (t >= 1) Finish();
        }

        private void Apply(float t)
        {
            if (_sheet == null || _dim == null || _sheetGroup == null) return;
            bool closing = _state == State.Closing;
            float move = closing ? t * t * t : 1 - Mathf.Pow(1 - t, 3);
            float fade = t * t * (3 - 2 * t);
            _sheet.anchoredPosition = Vector2.LerpUnclamped(_fromPosition, _targetPosition, move);
            _dim.alpha = Mathf.Lerp(_fromDimAlpha, closing ? 0 : _openDimAlpha, fade);
            // Full sheets remain readable while leaving. Reduced motion uses a short fade only.
            float sheetFade = closing && !_reduced ? Mathf.InverseLerp(.55f, 1, t) : fade;
            _sheetGroup.alpha = Mathf.Lerp(_fromSheetAlpha, closing ? 0 : 1, sheetFade);
        }

        private float ExitDistance()
        {
            var parent = _sheet.parent as RectTransform;
            if (parent == null) return _sheet.rect.height + 48;
            var corners = new Vector3[4];
            _sheet.GetWorldCorners(corners);
            float top = parent.InverseTransformPoint(corners[1]).y;
            return Mathf.Max(80, top - parent.rect.yMin + 24);
        }

        private void SetBlocked(bool transitioning)
        {
            if (_shield != null)
            {
                _shield.blocksRaycasts = true;
                _shield.interactable = !transitioning;
            }
            if (_sheetGroup != null)
            {
                _sheetGroup.blocksRaycasts = true;
                _sheetGroup.interactable = !transitioning;
            }
            if (_dim != null) { _dim.blocksRaycasts = true; _dim.interactable = !transitioning; }
        }

        private void Finish()
        {
            bool wasClosing = _state == State.Closing;
            _state = wasClosing ? State.Closed : State.Open;
            if (wasClosing)
            {
                // Blocking remains until the transition finishes, then the owner may remove the overlay.
                if (_shield != null) { _shield.blocksRaycasts = false; _shield.interactable = false; }
                if (_sheetGroup != null) { _sheetGroup.blocksRaycasts = false; _sheetGroup.interactable = false; }
                if (_dim != null) { _dim.blocksRaycasts = false; _dim.interactable = false; }
                var completed = _onClosed;
                _onClosed = null;
                completed?.Invoke();
            }
            else SetBlocked(false);
        }

        private void OnApplicationPause(bool paused) { if (paused) CompleteImmediately(); }
        private void OnApplicationFocus(bool focused) { if (!focused) CompleteImmediately(); }
        private void OnRectTransformDimensionsChange()
        {
            // A resized/rotated overlay must not leave its blocker waiting for a lost animation.
            if (_ready && IsClosing) CompleteImmediately();
        }
        private void OnDisable() { CompleteImmediately(); }
    }
}
