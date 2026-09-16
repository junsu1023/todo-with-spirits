using System;
using System.Collections.Generic;
using TodoSpirits.Core;
using UnityEngine;
using UnityEngine.UI;

namespace TodoSpirits.Presentation.Main
{
    [DisallowMultipleComponent]
    public sealed class MainWireframeView : MonoBehaviour
    {
        [Header("Concept wireframe information hierarchy")]
        [SerializeField]
        private Text essenceText;

        [SerializeField]
        private Text actionText;

        [SerializeField]
        private Text speechText;

        [SerializeField]
        private Text growthText;

        [Header("Touch targets")]
        [SerializeField]
        private Button todayRecordButton;

        [SerializeField]
        private Button giftButton;

        [SerializeField]
        private Button travelButton;

        [SerializeField]
        private Button decorateButton;

        [SerializeField]
        private Button debugButton;

        [Header("Authored living scene")]
        [SerializeField]
        private RectTransform worldRoot;

        [SerializeField]
        private SpiritLivingActor spiritActor;

        [SerializeField]
        private List<ActivitySpotBinding> activitySpots = new List<ActivitySpotBinding>(5);

        private readonly Dictionary<ActivitySpotId, ActivitySpotBinding> _spotsById =
            new Dictionary<ActivitySpotId, ActivitySpotBinding>(5);

        private bool _initialized;
        private readonly Dictionary<int, GameObject> _decorations = new Dictionary<int, GameObject>();

        public void RenderCompanionWorld(CompanionLife life, CompanionInterventions interventions, bool traveling)
        {
            spiritActor.PresentLife(life);
            bool present = life.Farewell != FarewellStep.Independent && !traveling;
            spiritActor.gameObject.SetActive(present);
            if (!present)
            {
                foreach(var pair in _spotsById) pair.Value.SetSelected(false);
                actionText.text = traveling ? "지금 · 이슬 숲을 여행하는 중" : "새로운 동행을 기다리는 집";
                speechText.text = traveling ? "준비물을 챙겨 숲 너머를 둘러보고 있어요." : life.DisplayName + "의 기록은 이곳에 남아 있어요.";
            }
            ActivitySpotId[] slots = { ActivitySpotId.Desk, ActivitySpotId.Workshop, ActivitySpotId.TeaTable, ActivitySpotId.Yard, ActivitySpotId.Rest };
            for(int i=0;i<slots.Length;i++)
            {
                bool equipped = interventions.EquippedDecorations.Contains(i.ToString(System.Globalization.CultureInfo.InvariantCulture));
                if (!_decorations.TryGetValue(i,out var prop) && equipped && _spotsById.TryGetValue(slots[i],out var spot))
                {
                    prop = CompanionWorldArt.Decoration(spot.PropAnchor != null ? spot.PropAnchor : spot.Destination,i);
                    _decorations.Add(i,prop);
                }
                if (prop != null) prop.SetActive(equipped);
            }
        }

        public GameObject Root => gameObject;

        public RectTransform WorldRoot => worldRoot;

        public SpiritLivingActor SpiritActor => spiritActor;

        public void ReserveCompanionNavigation()
        {
            if (todayRecordButton == null) return;
            var rect = (RectTransform)todayRecordButton.transform;
            rect.anchorMin = new Vector2(rect.anchorMin.x, .205f);
            rect.anchorMax = new Vector2(rect.anchorMax.x, .295f);
        }

        public bool Initialize(
            Font runtimeFont,
            Action openTodayRecord,
            Action openGift,
            Action openTravel,
            Action openDecorate,
            Action toggleDebug,
            bool debugAvailable)
        {
            if (_initialized)
            {
                return true;
            }

            if (!ValidateReferences(out string error))
            {
                Debug.LogError($"{nameof(MainWireframeView)} prefab wiring is invalid. {error}", this);
                enabled = false;
                return false;
            }

            ApplyRuntimeFont(runtimeFont);
            todayRecordButton.onClick.AddListener(() => openTodayRecord?.Invoke());
            giftButton.onClick.AddListener(() => openGift?.Invoke());
            travelButton.onClick.AddListener(() => openTravel?.Invoke());
            decorateButton.onClick.AddListener(() => openDecorate?.Invoke());
            debugButton.onClick.AddListener(() => toggleDebug?.Invoke());
            debugButton.gameObject.SetActive(debugAvailable);
            SetActivityDebugVisible(false);
            CompanionWorldArt.BuildLivingSpace(worldRoot, activitySpots);
            _initialized = true;
            return true;
        }

        public bool ValidateReferences(out string error)
        {
            if (essenceText == null || actionText == null || speechText == null || growthText == null)
            {
                error = "Essence, action, speech, and growth Text references are required.";
                return false;
            }

            if (todayRecordButton == null || giftButton == null || travelButton == null ||
                decorateButton == null || debugButton == null)
            {
                error = "Today Record, Gift, Travel, Decorate, and Debug Button references are required.";
                return false;
            }

            if (worldRoot == null || spiritActor == null)
            {
                error = "WorldRoot and SpiritLivingActor references are required.";
                return false;
            }

            if (!spiritActor.ValidateReferences(out error))
            {
                return false;
            }

            if (activitySpots == null || activitySpots.Count != 5)
            {
                error = "Exactly five ActivitySpotBinding entries are required.";
                return false;
            }

            _spotsById.Clear();
            for (int i = 0; i < activitySpots.Count; i++)
            {
                ActivitySpotBinding spot = activitySpots[i];
                if (spot == null || !spot.IsValid)
                {
                    error = $"Activity spot index {i} requires a stable ID, Destination, and DebugLabel.";
                    return false;
                }

                if (_spotsById.ContainsKey(spot.StableId))
                {
                    error = $"Activity spot ID '{spot.StableId}' is duplicated.";
                    return false;
                }

                _spotsById.Add(spot.StableId, spot);
            }

            ActivitySpotId[] requiredIds =
            {
                ActivitySpotId.Desk,
                ActivitySpotId.Workshop,
                ActivitySpotId.Yard,
                ActivitySpotId.TeaTable,
                ActivitySpotId.Rest
            };
            for (int i = 0; i < requiredIds.Length; i++)
            {
                if (!_spotsById.ContainsKey(requiredIds[i]))
                {
                    error = $"Required activity spot '{requiredIds[i]}' is missing.";
                    return false;
                }
            }

            error = string.Empty;
            return true;
        }

        public void Render(int essenceBalance, string currentAction, string dialogue, string growthState)
        {
            essenceText.text = $"정수 {Mathf.Max(0, essenceBalance)}";
            actionText.text = string.IsNullOrWhiteSpace(currentAction)
                ? "지금 · 조용히 하루를 보내는 중"
                : $"지금 · {currentAction}";
            speechText.text = string.IsNullOrWhiteSpace(dialogue)
                ? "“네 하루가 내 하루에도 닿았어.”"
                : $"“{dialogue}”";
            growthText.text = string.IsNullOrWhiteSpace(growthState)
                ? "아직 어린 정령 · 오늘을 함께 살아가는 중"
                : growthState;
        }

        public void PresentSpiritDay(SpiritActionId action, string dialogue, bool animateMovement, int presentationVariant = 0, string savedLocation = null)
        {
            ActivitySpotId spotId = ResolveSpotId(action);
            // A saved day keeps its actual location even after growth or a rule change.
            if (!string.IsNullOrEmpty(savedLocation))
                foreach (SpiritActionId placeAction in Enum.GetValues(typeof(SpiritActionId)))
                    if (SpiritActionCatalog.Get(placeAction).Location == savedLocation)
                    {
                        spotId = ResolveSpotId(placeAction);
                        break;
                    }
            if (!_spotsById.TryGetValue(spotId, out ActivitySpotBinding selectedSpot))
            {
                Debug.LogError($"No authored ActivitySpotBinding exists for '{spotId}'. Falling back to Rest.", this);
                if (!_spotsById.TryGetValue(ActivitySpotId.Rest, out selectedSpot))
                {
                    return;
                }
            }

            foreach (KeyValuePair<ActivitySpotId, ActivitySpotBinding> pair in _spotsById)
            {
                pair.Value.SetSelected(pair.Key == selectedSpot.StableId);
            }

            spiritActor.Present(action, selectedSpot.Destination, dialogue, animateMovement, presentationVariant);
        }

        public void SetActivityDebugVisible(bool visible)
        {
            if (activitySpots == null)
            {
                return;
            }

            for (int i = 0; i < activitySpots.Count; i++)
            {
                activitySpots[i]?.SetDebugLabelVisible(visible);
            }
        }

        public void ApplyRuntimeFont(Font runtimeFont)
        {
            if (runtimeFont == null)
            {
                return;
            }

            Text[] texts = GetComponentsInChildren<Text>(true);
            for (int i = 0; i < texts.Length; i++)
            {
                texts[i].font = runtimeFont;
            }
        }

        public static ActivitySpotId ResolveSpotId(SpiritActionId action)
        {
            switch (action)
            {
                case SpiritActionId.ReadRecords:
                    return ActivitySpotId.Desk;
                case SpiritActionId.CraftRepair:
                    return ActivitySpotId.Workshop;
                case SpiritActionId.WalkForest:
                    return ActivitySpotId.Yard;
                case SpiritActionId.SocialTea:
                    return ActivitySpotId.TeaTable;
                default:
                    return ActivitySpotId.Rest;
            }
        }
    }
}
