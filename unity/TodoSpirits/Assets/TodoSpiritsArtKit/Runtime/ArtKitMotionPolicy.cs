using UnityEngine;

namespace TodoSpirits.ArtKit
{
    // Presentation only. Bind this to the EXISTING settings service; no save format is introduced.
    public static class ArtKitMotionPolicy
    {
        public static bool ReduceMotion { get; set; }
        [RuntimeInitializeOnLoadMethod(RuntimeInitializeLoadType.SubsystemRegistration)]
        private static void ResetStatics() { ReduceMotion = false; }
    }
}
