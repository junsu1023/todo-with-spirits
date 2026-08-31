using System.Collections;
using TodoSpirits.Core;
using UnityEngine;
using UnityEngine.UI;

namespace TodoSpirits.Presentation.Main
{
    [DisallowMultipleComponent]
    public sealed class SpiritLivingActor : MonoBehaviour
    {
        [Header("Required authored references")]
        [SerializeField]
        private RectTransform actorRoot;

        [SerializeField]
        private RectTransform visualRoot;

        [SerializeField]
        private Text actionIcon;

        [SerializeField]
        private CanvasGroup reactionBubble;

        [SerializeField]
        private Text reactionText;

        [Header("Optional wireframe styling")]
        [SerializeField]
        private Image bodyImage;

        [Header("Prototype motion")]
        [SerializeField]
        [Min(0.05f)]
        private float moveDuration = 0.8f;

        [SerializeField]
        [Min(0f)]
        private float reactionDuration = 1.1f;

        [SerializeField]
        [Min(0f)]
        private float idleAmplitude = 6f;

        [SerializeField]
        [Min(0.1f)]
        private float idleFrequency = 2.2f;

        private Coroutine _presentationRoutine;
        private Vector3 _baseVisualPosition;
        private Quaternion _baseVisualRotation;
        private Vector3 _baseVisualScale;
        private bool _idlePaused;
        private bool _hasPresentation;
        private SpiritActionId _lastAction;
        private RectTransform _lastDestination;
        private string _lastDialogue;

        public RectTransform ActorRoot => actorRoot;

        private void Awake()
        {
            if (actorRoot == null)
            {
                actorRoot = transform as RectTransform;
            }

            if (visualRoot != null)
            {
                _baseVisualPosition = visualRoot.localPosition;
                _baseVisualRotation = visualRoot.localRotation;
                _baseVisualScale = visualRoot.localScale;
            }

            SetReactionVisible(false);
        }

        private void OnDisable()
        {
            StopPresentation();
        }

        private void OnEnable()
        {
            if (_hasPresentation && _lastDestination != null && _presentationRoutine == null)
            {
                _presentationRoutine = StartCoroutine(
                    PresentRoutine(_lastAction, _lastDestination, _lastDialogue, false, false));
            }
        }

        public bool ValidateReferences(out string error)
        {
            if (actorRoot == null || visualRoot == null || actionIcon == null || reactionBubble == null || reactionText == null)
            {
                error = "SpiritLivingActor requires ActorRoot, VisualRoot, ActionIcon, ReactionBubble, and ReactionText references.";
                return false;
            }

            if (actorRoot.parent == null)
            {
                error = "SpiritLivingActor ActorRoot must be parented below the authored world RectTransform.";
                return false;
            }

            error = string.Empty;
            return true;
        }

        public void Present(
            SpiritActionId action,
            RectTransform destination,
            string dialogue,
            bool animateMovement)
        {
            if (!ValidateReferences(out string error))
            {
                Debug.LogError(error, this);
                enabled = false;
                return;
            }

            if (destination == null)
            {
                Debug.LogError("SpiritLivingActor cannot present an action without a destination.", this);
                return;
            }

            _hasPresentation = true;
            _lastAction = action;
            _lastDestination = destination;
            _lastDialogue = dialogue;
            StopPresentation();
            _presentationRoutine = StartCoroutine(
                PresentRoutine(action, destination, dialogue, animateMovement, true));
        }

        public void SetIdlePaused(bool paused)
        {
            _idlePaused = paused;
            if (paused && visualRoot != null)
            {
                visualRoot.localPosition = _baseVisualPosition;
            }
        }

        public void StopPresentation()
        {
            if (_presentationRoutine != null)
            {
                StopCoroutine(_presentationRoutine);
                _presentationRoutine = null;
            }

            if (visualRoot != null)
            {
                visualRoot.localPosition = _baseVisualPosition;
                visualRoot.localRotation = _baseVisualRotation;
                visualRoot.localScale = _baseVisualScale;
            }

            SetReactionVisible(false);
        }

        private IEnumerator PresentRoutine(
            SpiritActionId action,
            RectTransform destination,
            string dialogue,
            bool animateMovement,
            bool showArrivalReaction)
        {
            PoseStyle pose = GetPose(action);
            actionIcon.gameObject.SetActive(false);
            visualRoot.localRotation = _baseVisualRotation;
            visualRoot.localScale = _baseVisualScale;

            Vector2 targetPosition = ResolveAnchoredPosition(destination);
            if (animateMovement)
            {
                yield return MoveTo(targetPosition);
            }
            else
            {
                actorRoot.anchoredPosition = targetPosition;
            }

            if (showArrivalReaction)
            {
                yield return PlayArrivalReaction(dialogue);
            }

            actionIcon.text = pose.Icon;
            actionIcon.gameObject.SetActive(true);
            visualRoot.localRotation = _baseVisualRotation * Quaternion.Euler(0f, 0f, pose.RotationDegrees);
            visualRoot.localScale = Vector3.Scale(_baseVisualScale, pose.Scale);
            if (bodyImage != null)
            {
                bodyImage.color = pose.BodyColor;
            }

            float idleTime = 0f;
            while (enabled)
            {
                if (_idlePaused)
                {
                    visualRoot.localPosition = _baseVisualPosition;
                    yield return null;
                    continue;
                }

                idleTime += Time.unscaledDeltaTime;
                float wave = Mathf.Sin(idleTime * idleFrequency * pose.IdleSpeedMultiplier);
                visualRoot.localPosition = _baseVisualPosition +
                    new Vector3(0f, wave * idleAmplitude * pose.IdleAmplitudeMultiplier, 0f);
                yield return null;
            }
        }

        private IEnumerator MoveTo(Vector2 targetPosition)
        {
            Vector2 startPosition = actorRoot.anchoredPosition;
            float elapsed = 0f;
            while (elapsed < moveDuration)
            {
                elapsed += Time.unscaledDeltaTime;
                float t = Mathf.Clamp01(elapsed / moveDuration);
                float eased = t * t * (3f - (2f * t));
                Vector2 position = Vector2.LerpUnclamped(startPosition, targetPosition, eased);
                position.y += Mathf.Sin(t * Mathf.PI * 4f) * 12f;
                actorRoot.anchoredPosition = position;
                yield return null;
            }

            actorRoot.anchoredPosition = targetPosition;
        }

        private IEnumerator PlayArrivalReaction(string dialogue)
        {
            if (string.IsNullOrWhiteSpace(dialogue) || reactionDuration <= 0f)
            {
                SetReactionVisible(false);
                yield break;
            }

            reactionText.text = dialogue;
            SetReactionVisible(true);
            RectTransform bubbleRect = reactionBubble.transform as RectTransform;
            float elapsed = 0f;
            while (elapsed < reactionDuration)
            {
                elapsed += Time.unscaledDeltaTime;
                float normalized = Mathf.Clamp01(elapsed / Mathf.Max(0.01f, reactionDuration));
                reactionBubble.alpha = Mathf.Clamp01(normalized * 5f) * Mathf.Clamp01((1f - normalized) * 5f);
                if (bubbleRect != null)
                {
                    float pulse = 0.94f + (Mathf.Sin(normalized * Mathf.PI) * 0.06f);
                    bubbleRect.localScale = new Vector3(pulse, pulse, 1f);
                }

                yield return null;
            }

            SetReactionVisible(false);
        }

        private Vector2 ResolveAnchoredPosition(RectTransform destination)
        {
            var parent = actorRoot.parent as RectTransform;
            if (parent == null)
            {
                return destination.anchoredPosition;
            }

            Vector3 localPosition = parent.InverseTransformPoint(destination.position);
            return new Vector2(localPosition.x, localPosition.y);
        }

        private void SetReactionVisible(bool visible)
        {
            if (reactionBubble == null)
            {
                return;
            }

            reactionBubble.alpha = visible ? 1f : 0f;
            reactionBubble.blocksRaycasts = false;
            reactionBubble.interactable = false;
            reactionBubble.gameObject.SetActive(visible);
            if (!visible && reactionBubble.transform is RectTransform bubbleRect)
            {
                bubbleRect.localScale = Vector3.one;
            }
        }

        private static PoseStyle GetPose(SpiritActionId action)
        {
            switch (action)
            {
                case SpiritActionId.ReadRecords:
                    return new PoseStyle("책", new Vector3(0.98f, 1.02f, 1f), -2f, 0.65f, 0.8f, new Color(0.88f, 0.84f, 0.66f, 1f));
                case SpiritActionId.CraftRepair:
                    return new PoseStyle("도구", new Vector3(1.04f, 0.98f, 1f), 2f, 0.8f, 1.1f, new Color(0.88f, 0.77f, 0.58f, 1f));
                case SpiritActionId.WalkForest:
                    return new PoseStyle("발걸음", new Vector3(1.02f, 1f, 1f), 0f, 1.2f, 1.35f, new Color(0.76f, 0.87f, 0.64f, 1f));
                case SpiritActionId.SocialTea:
                    return new PoseStyle("차", Vector3.one, -1f, 0.7f, 0.9f, new Color(0.91f, 0.75f, 0.68f, 1f));
                default:
                    return new PoseStyle("Zzz", new Vector3(1.08f, 0.88f, 1f), 0f, 0.35f, 0.55f, new Color(0.74f, 0.81f, 0.78f, 1f));
            }
        }

        private readonly struct PoseStyle
        {
            public PoseStyle(
                string icon,
                Vector3 scale,
                float rotationDegrees,
                float idleAmplitudeMultiplier,
                float idleSpeedMultiplier,
                Color bodyColor)
            {
                Icon = icon;
                Scale = scale;
                RotationDegrees = rotationDegrees;
                IdleAmplitudeMultiplier = idleAmplitudeMultiplier;
                IdleSpeedMultiplier = idleSpeedMultiplier;
                BodyColor = bodyColor;
            }

            public string Icon { get; }

            public Vector3 Scale { get; }

            public float RotationDegrees { get; }

            public float IdleAmplitudeMultiplier { get; }

            public float IdleSpeedMultiplier { get; }

            public Color BodyColor { get; }
        }
    }
}
