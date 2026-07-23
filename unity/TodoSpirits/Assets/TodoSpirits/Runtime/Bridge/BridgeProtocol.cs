using System;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;

namespace TodoSpirits.Unity.Runtime.Bridge
{
    public static class BridgeProtocol
    {
        public const string Name = "todo-spirits.unity";
        public const int Version = 1;
        public const int MaxIdentifierLength = 128;
    }

    public enum BridgeMessageType
    {
        SessionInitialize,
        SessionUpdate,
        SpiritSetState,
        SpiritPlayInteraction,
        AppearanceSetTheme,
        AudioSetState,
        LifecyclePause,
        LifecycleResume,
        RuntimeRequestExit,
        RuntimeReady,
        SceneLoadProgress,
        ContentReady,
        SpiritInteracted,
        AnimationCompleted,
        ExitRequested,
        DiagnosticMetric,
        ErrorOccurred,
    }

    [JsonConverter(typeof(StringEnumConverter))]
    public enum BridgeErrorCode
    {
        BRIDGE_INVALID_JSON,
        BRIDGE_PROTOCOL_MISMATCH,
        BRIDGE_VERSION_UNSUPPORTED,
        BRIDGE_MESSAGE_ID_MISSING,
        BRIDGE_SESSION_ID_MISSING,
        BRIDGE_SESSION_MISMATCH,
        BRIDGE_UNKNOWN_MESSAGE_TYPE,
        BRIDGE_INVALID_PAYLOAD,
        BRIDGE_RUNTIME_NOT_READY,
        BRIDGE_DUPLICATE_MESSAGE,
        BRIDGE_INTERNAL_ERROR,
    }

    public static class BridgeMessageTypes
    {
        private static readonly IReadOnlyDictionary<string, BridgeMessageType> FromWire =
            new Dictionary<string, BridgeMessageType>(StringComparer.Ordinal)
            {
                ["session.initialize"] = BridgeMessageType.SessionInitialize,
                ["session.update"] = BridgeMessageType.SessionUpdate,
                ["spirit.setState"] = BridgeMessageType.SpiritSetState,
                ["spirit.playInteraction"] = BridgeMessageType.SpiritPlayInteraction,
                ["appearance.setTheme"] = BridgeMessageType.AppearanceSetTheme,
                ["audio.setState"] = BridgeMessageType.AudioSetState,
                ["lifecycle.pause"] = BridgeMessageType.LifecyclePause,
                ["lifecycle.resume"] = BridgeMessageType.LifecycleResume,
                ["runtime.requestExit"] = BridgeMessageType.RuntimeRequestExit,
                ["runtime.ready"] = BridgeMessageType.RuntimeReady,
                ["scene.loadProgress"] = BridgeMessageType.SceneLoadProgress,
                ["content.ready"] = BridgeMessageType.ContentReady,
                ["spirit.interacted"] = BridgeMessageType.SpiritInteracted,
                ["animation.completed"] = BridgeMessageType.AnimationCompleted,
                ["exit.requested"] = BridgeMessageType.ExitRequested,
                ["diagnostic.metric"] = BridgeMessageType.DiagnosticMetric,
                ["error.occurred"] = BridgeMessageType.ErrorOccurred,
            };

        private static readonly IReadOnlyDictionary<BridgeMessageType, string> ToWire =
            new Dictionary<BridgeMessageType, string>
            {
                [BridgeMessageType.SessionInitialize] = "session.initialize",
                [BridgeMessageType.SessionUpdate] = "session.update",
                [BridgeMessageType.SpiritSetState] = "spirit.setState",
                [BridgeMessageType.SpiritPlayInteraction] = "spirit.playInteraction",
                [BridgeMessageType.AppearanceSetTheme] = "appearance.setTheme",
                [BridgeMessageType.AudioSetState] = "audio.setState",
                [BridgeMessageType.LifecyclePause] = "lifecycle.pause",
                [BridgeMessageType.LifecycleResume] = "lifecycle.resume",
                [BridgeMessageType.RuntimeRequestExit] = "runtime.requestExit",
                [BridgeMessageType.RuntimeReady] = "runtime.ready",
                [BridgeMessageType.SceneLoadProgress] = "scene.loadProgress",
                [BridgeMessageType.ContentReady] = "content.ready",
                [BridgeMessageType.SpiritInteracted] = "spirit.interacted",
                [BridgeMessageType.AnimationCompleted] = "animation.completed",
                [BridgeMessageType.ExitRequested] = "exit.requested",
                [BridgeMessageType.DiagnosticMetric] = "diagnostic.metric",
                [BridgeMessageType.ErrorOccurred] = "error.occurred",
            };

        public static bool TryParse(string wireValue, out BridgeMessageType messageType) =>
            FromWire.TryGetValue(wireValue, out messageType);

        public static string ToWireValue(BridgeMessageType messageType) => ToWire[messageType];
    }
}

