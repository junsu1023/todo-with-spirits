using System;
using UnityEngine;
using UnityEngine.UI;

namespace TodoSpirits.Presentation.Main
{
    // Append-only: Unity serializes enum values as integers inside prefabs.
    public enum ActivitySpotId
    {
        Unknown = 0,
        Desk = 1,
        Workshop = 2,
        Yard = 3,
        TeaTable = 4,
        Rest = 5
    }

    [Serializable]
    public sealed class ActivitySpotBinding
    {
        [SerializeField]
        private ActivitySpotId stableId;

        [SerializeField]
        private RectTransform destination;

        [SerializeField]
        [Tooltip("Optional anchor for a future authored prop at this spot.")]
        private RectTransform propAnchor;

        [SerializeField]
        [Tooltip("Optional authored action-icon anchor. It is highlighted for the selected spot.")]
        private RectTransform iconAnchor;

        [SerializeField]
        private Text debugLabel;

        public ActivitySpotId StableId => stableId;

        public RectTransform Destination => destination;

        public RectTransform PropAnchor => propAnchor;

        public RectTransform IconAnchor => iconAnchor;

        public Text DebugLabel => debugLabel;

        public bool IsValid => stableId != ActivitySpotId.Unknown && destination != null && debugLabel != null;

        public void SetSelected(bool selected)
        {
            if (iconAnchor != null)
            {
                iconAnchor.gameObject.SetActive(selected);
            }

            if (debugLabel != null)
            {
                debugLabel.color = selected
                    ? new Color(0.91f, 0.95f, 0.69f, 1f)
                    : new Color(0.80f, 0.82f, 0.78f, 1f);
            }
        }

        public void SetDebugLabelVisible(bool visible)
        {
            if (debugLabel == null)
            {
                return;
            }

            debugLabel.text = stableId.ToString();
            debugLabel.gameObject.SetActive(visible);
        }
    }
}
