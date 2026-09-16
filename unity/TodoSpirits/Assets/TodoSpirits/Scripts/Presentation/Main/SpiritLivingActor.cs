using System.Collections;
using TodoSpirits.Core;
using UnityEngine;
using UnityEngine.UI;

namespace TodoSpirits.Presentation.Main
{
    [DisallowMultipleComponent]
    public sealed partial class SpiritLivingActor : MonoBehaviour
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
        private Vector3 _authoredVisualScale;
        private Color _lifeTint = Color.white;
        private GameObject _lifeAccessories;
        private int _appearanceKey = -1;
        private bool _idlePaused;
        private bool _hasPresentation;
        private SpiritActionId _lastAction;
        private RectTransform _lastDestination;
        private string _lastDialogue;
        private int _presentationVariant;
        private float _traitTempo = 1f;
        private CompanionSoftShape _bodySilhouette;

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
                _authoredVisualScale = _baseVisualScale;
                if (bodyImage != null)
                {
                    var body = CompanionWorldArt.Soft(bodyImage.transform,"SoftBody",new Vector2(.5f,.5f),Vector2.zero,bodyImage.color,.7f);
                    body.anchorMin = Vector2.zero; body.anchorMax = Vector2.one;
                    body.offsetMin = body.offsetMax = Vector2.zero; body.SetAsFirstSibling();
                    _bodySilhouette = body.GetComponent<CompanionSoftShape>();
                    bodyImage.enabled = false;
                }
            }

            BuildExpressionArt();
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
            bool animateMovement,
            int presentationVariant = 0)
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
            _presentationVariant = Mathf.Clamp(presentationVariant,0,3);
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

        public void PresentLife(CompanionLife life)
        {
            if (life == null || visualRoot == null) return;
            _traitTempo = life.Temperaments.Contains(SpiritTemperament.Active) ? 1.3f :
                life.Temperaments.Contains(SpiritTemperament.Relaxed) ? .7f :
                life.Temperaments.Contains(SpiritTemperament.Cautious) ? .85f : 1f;
            int phase = life.AppearancePhase;
            var previewRoute = life.Stage == CompanionStage.Preparing ? CompanionLifeRules.PreviewRoute(life) : life.Route;
            int key = (int)life.Stage * 10 + (life.Stage >= CompanionStage.Preparing ? (int)previewRoute : 0);
            if (_appearanceKey == key) return;
            _appearanceKey = key;
            if (_lifeAccessories != null) { _lifeAccessories.SetActive(false); Destroy(_lifeAccessories); }
            _baseVisualScale = _authoredVisualScale * (phase == 0 ? .85f : phase == 1 ? 1f : 1.12f);
            _lifeTint = phase == 0 ? new Color(.84f,1f,.8f) : phase == 1 ? new Color(.65f,.94f,.65f) : new Color(.55f,.83f,.69f);
            _lifeAccessories = new GameObject("LifeAccessories", typeof(RectTransform));
            var root = (RectTransform)_lifeAccessories.transform;
            root.SetParent(visualRoot, false);
            root.anchorMin = Vector2.zero; root.anchorMax = Vector2.one; root.offsetMin = root.offsetMax = Vector2.zero;
            for (int i = 0; i <= phase; i++)
            {
                var leaf = CompanionWorldArt.Soft(root, "CrownLeaf" + i, new Vector2(.4f + i * .13f, .9f), new Vector2(25,48), new Color(.27f,.53f,.34f));
                leaf.localRotation = Quaternion.Euler(0,0,-35 + i*35);
            }
            if (life.Stage == CompanionStage.Preparing)
            {
                var cream = new Color(.97f,.91f,.69f);
                if (previewRoute == AdultRoute.Recorder)
                    CompanionWorldArt.Shape(root,"FutureNotebook",new Vector2(.85f,.28f),new Vector2(24,34),cream);
                else if (previewRoute == AdultRoute.Explorer)
                    CompanionWorldArt.Shape(root,"FuturePouch",new Vector2(.91f,.32f),new Vector2(26,31),new Color(.53f,.37f,.22f));
                else
                    CompanionWorldArt.Shape(root,"FutureApronCloth",new Vector2(.7f,.22f),new Vector2(30,24),new Color(.68f,.42f,.27f));
            }
            if (phase == 2)
            {
                if (life.Route == AdultRoute.Recorder)
                {
                    CompanionWorldArt.Shape(root,"RecorderScroll",new Vector2(.86f,.28f),new Vector2(35,60),new Color(.97f,.91f,.69f));
                    CompanionWorldArt.Shape(root,"ScrollRibbon",new Vector2(.86f,.28f),new Vector2(40,8),new Color(.5f,.3f,.22f));
                }
                else if (life.Route == AdultRoute.Explorer)
                {
                    CompanionWorldArt.Shape(root,"ExplorerBackpack",new Vector2(.94f,.38f),new Vector2(40,72),new Color(.53f,.37f,.22f));
                    CompanionWorldArt.Shape(root,"ExplorerHat",new Vector2(.5f,.91f),new Vector2(120,14),new Color(.79f,.64f,.37f));
                }
                else
                {
                    CompanionWorldArt.Shape(root,"ArtisanApron",new Vector2(.5f,.24f),new Vector2(80,49),new Color(.68f,.42f,.27f));
                    CompanionWorldArt.Shape(root,"ApronPocket",new Vector2(.5f,.23f),new Vector2(30,22),new Color(.86f,.66f,.41f));
                }
            }
            if (_hasPresentation && gameObject.activeInHierarchy) Present(_lastAction,_lastDestination,_lastDialogue,false,_presentationVariant);
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
            SetActionArt(action);
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
            actionIcon.gameObject.SetActive(false);
            SetActionArt(action);
            float variantTilt = _presentationVariant == 0 ? 0 : (_presentationVariant - 2) * 5f;
            visualRoot.localRotation = _baseVisualRotation * Quaternion.Euler(0f, 0f, pose.RotationDegrees + variantTilt);
            visualRoot.localScale = Vector3.Scale(_baseVisualScale, pose.Scale);
            if (bodyImage != null)
            {
                bodyImage.color = Color.Lerp(pose.BodyColor, _lifeTint, .5f);
                if (_bodySilhouette != null) _bodySilhouette.color = bodyImage.color;
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
                float wave = Mathf.Sin(idleTime * idleFrequency * pose.IdleSpeedMultiplier * _traitTempo);
                visualRoot.localPosition = _baseVisualPosition +
                    new Vector3(_presentationVariant == 2 ? wave * 3f : 0f, wave * idleAmplitude * pose.IdleAmplitudeMultiplier, 0f);
                AnimateActionArt(action, idleTime * _traitTempo, wave);
                yield return null;
            }
        }

        private IEnumerator MoveTo(Vector2 targetPosition)
        {
            Vector2 startPosition = actorRoot.anchoredPosition;
            SetExpression(SpiritExpression.Neutral);
            foreach (var prop in _heldProps) if (prop != null) prop.SetActive(false);
            FaceTravelDirection(targetPosition - startPosition);
            float elapsed = 0f;
            while (elapsed < moveDuration)
            {
                elapsed += Time.unscaledDeltaTime;
                float t = Mathf.Clamp01(elapsed / moveDuration);
                float eased = t * t * (3f - (2f * t));
                Vector2 position = Vector2.LerpUnclamped(startPosition, targetPosition, eased);
                position.y += Mathf.Sin(t * Mathf.PI * 4f) * 12f;
                actorRoot.anchoredPosition = position;
                AnimateFeet(Mathf.Sin(t * Mathf.PI * 8));
                yield return null;
            }

            actorRoot.anchoredPosition = targetPosition;
            FaceTravelDirection(Vector2.zero);
            AnimateFeet(0);
        }

        private IEnumerator PlayArrivalReaction(string dialogue)
        {
            if (string.IsNullOrWhiteSpace(dialogue) || reactionDuration <= 0f)
            {
                SetReactionVisible(false);
                yield break;
            }

            reactionText.text = dialogue;
            SetExpression(SpiritExpression.Happy);
            SetReactionVisible(true);
            RectTransform bubbleRect = reactionBubble.transform as RectTransform;
            float elapsed = 0f;
            while (elapsed < reactionDuration)
            {
                elapsed += Time.unscaledDeltaTime;
                float normalized = Mathf.Clamp01(elapsed / Mathf.Max(0.01f, reactionDuration));
                visualRoot.localPosition = _baseVisualPosition + Vector3.up * Mathf.Sin(normalized * Mathf.PI) * 9f;
                reactionBubble.alpha = Mathf.Clamp01(normalized * 5f) * Mathf.Clamp01((1f - normalized) * 5f);
                if (bubbleRect != null)
                {
                    float pulse = 0.94f + (Mathf.Sin(normalized * Mathf.PI) * 0.06f);
                    bubbleRect.localScale = new Vector3(pulse, pulse, 1f);
                }

                yield return null;
            }

            SetReactionVisible(false);
            visualRoot.localPosition = _baseVisualPosition;
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
