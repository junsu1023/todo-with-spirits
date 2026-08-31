using System;
using System.Collections;
using TodoSpirits.Presentation.Common;
using UnityEngine;
using UnityEngine.UI;

namespace TodoSpirits.Presentation.Reward
{
    internal sealed class RewardCardView
    {
        private const float TransitionSeconds = 0.24f;
        private const float VisibleSeconds = 1.8f;

        private readonly RectTransform _rectTransform;
        private readonly Text _label;
        private readonly Vector2 _visiblePosition;
        private readonly Vector2 _hiddenPosition;
        private readonly Action _onTap;
        private bool _dismissRequested;

        public RewardCardView(WireframeUi ui, Transform safeAreaRoot, Action onTap)
        {
            Button card = ui.CreateButton(
                safeAreaRoot,
                "RewardCard",
                "오늘 함께한 기록\n완료한 일 0개  ·  정수 +0",
                HandleTap,
                new Vector2(0.08f, 0.82f),
                new Vector2(0.92f, 0.91f),
                Vector2.zero,
                Vector2.zero,
                WireframePalette.AccentDark,
                27);
            Root = card.gameObject;
            _rectTransform = (RectTransform)card.transform;
            _label = card.transform.Find("Label").GetComponent<Text>();
            _visiblePosition = _rectTransform.anchoredPosition;
            _hiddenPosition = _visiblePosition + new Vector2(0f, 230f);
            _rectTransform.anchoredPosition = _hiddenPosition;
            _onTap = onTap;
            Root.SetActive(false);
        }

        public GameObject Root { get; }

        public IEnumerator Play(int completedTaskCount, int reward)
        {
            _dismissRequested = false;
            _label.text = $"오늘 함께한 기록\n완료한 일 {Mathf.Max(0, completedTaskCount)}개  ·  정수 +{Mathf.Max(0, reward)}";
            Root.SetActive(true);
            _rectTransform.anchoredPosition = _hiddenPosition;

            yield return Move(_hiddenPosition, _visiblePosition, TransitionSeconds);

            float elapsed = 0f;
            while (elapsed < VisibleSeconds && !_dismissRequested)
            {
                elapsed += Time.unscaledDeltaTime;
                yield return null;
            }

            yield return Move(_rectTransform.anchoredPosition, _hiddenPosition, TransitionSeconds);
            Root.SetActive(false);
        }

        public void HideImmediately()
        {
            _dismissRequested = true;
            _rectTransform.anchoredPosition = _hiddenPosition;
            Root.SetActive(false);
        }

        private IEnumerator Move(Vector2 from, Vector2 to, float duration)
        {
            float elapsed = 0f;
            while (elapsed < duration)
            {
                elapsed += Time.unscaledDeltaTime;
                float t = Mathf.Clamp01(elapsed / duration);
                t = t * t * (3f - (2f * t));
                _rectTransform.anchoredPosition = Vector2.LerpUnclamped(from, to, t);
                yield return null;
            }

            _rectTransform.anchoredPosition = to;
        }

        private void HandleTap()
        {
            _dismissRequested = true;
            _onTap?.Invoke();
        }
    }
}
