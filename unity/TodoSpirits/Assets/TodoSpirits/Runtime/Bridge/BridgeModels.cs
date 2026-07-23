using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace TodoSpirits.Unity.Runtime.Bridge
{
    [JsonObject(MemberSerialization.OptIn)]
    public sealed class BridgeEnvelope
    {
        [JsonProperty("protocol", Required = Required.Always)]
        public string Protocol { get; set; } = string.Empty;

        [JsonProperty("version", Required = Required.Always)]
        public int Version { get; set; }

        [JsonProperty("messageId", Required = Required.Always)]
        public string MessageId { get; set; } = string.Empty;

        [JsonProperty("sessionId", Required = Required.Always)]
        public string SessionId { get; set; } = string.Empty;

        [JsonProperty("type", Required = Required.Always)]
        public string Type { get; set; } = string.Empty;

        [JsonProperty("timestampUtc", Required = Required.Always)]
        public string TimestampUtc { get; set; } = string.Empty;

        [JsonProperty("payload", Required = Required.Always)]
        public JObject Payload { get; set; } = new JObject();
    }

    [JsonObject(MemberSerialization.OptIn)]
    public sealed class BridgeErrorPayload
    {
        public BridgeErrorPayload()
        {
        }

        public BridgeErrorPayload(BridgeErrorCode code, string message, bool recoverable)
        {
            Code = code;
            Message = message;
            Recoverable = recoverable;
        }

        [JsonProperty("code", Required = Required.Always)]
        public BridgeErrorCode Code { get; set; }

        [JsonProperty("message", Required = Required.Always)]
        public string Message { get; set; } = string.Empty;

        [JsonProperty("recoverable", Required = Required.Always)]
        public bool Recoverable { get; set; }

        [JsonProperty("details", NullValueHandling = NullValueHandling.Ignore)]
        public JObject Details { get; set; }
    }

    [JsonObject(MemberSerialization.OptIn)]
    public sealed class InteractionPayload
    {
        [JsonProperty("interactionId", Required = Required.Always)]
        public string InteractionId { get; set; } = string.Empty;
    }

    [JsonObject(MemberSerialization.OptIn)]
    public sealed class ThemePayload
    {
        [JsonProperty("theme", Required = Required.Always)]
        public string Theme { get; set; } = string.Empty;
    }

    [JsonObject(MemberSerialization.OptIn)]
    public sealed class ExitPayload
    {
        [JsonProperty("reason", NullValueHandling = NullValueHandling.Ignore)]
        public string Reason { get; set; }
    }

    [JsonObject(MemberSerialization.OptIn)]
    public sealed class RuntimeReadyPayload
    {
        [JsonProperty("runtimeState", Required = Required.Always)]
        public string RuntimeState { get; set; } = string.Empty;

        [JsonProperty("buildId", NullValueHandling = NullValueHandling.Ignore)]
        public string BuildId { get; set; }
    }

    [JsonObject(MemberSerialization.OptIn)]
    public sealed class SceneProgressPayload
    {
        [JsonProperty("sceneId", Required = Required.Always)]
        public string SceneId { get; set; } = string.Empty;

        [JsonProperty("progress", Required = Required.Always)]
        public double Progress { get; set; }
    }

    [JsonObject(MemberSerialization.OptIn)]
    public sealed class ContentReadyPayload
    {
        [JsonProperty("sceneId", Required = Required.Always)]
        public string SceneId { get; set; } = string.Empty;
    }

    [JsonObject(MemberSerialization.OptIn)]
    public sealed class AnimationPayload
    {
        [JsonProperty("animationId", Required = Required.Always)]
        public string AnimationId { get; set; } = string.Empty;
    }

    [JsonObject(MemberSerialization.OptIn)]
    public sealed class MetricPayload
    {
        [JsonProperty("name", Required = Required.Always)]
        public string Name { get; set; } = string.Empty;

        [JsonProperty("value", Required = Required.Always)]
        public double Value { get; set; }

        [JsonProperty("unit", NullValueHandling = NullValueHandling.Ignore)]
        public string Unit { get; set; }
    }
}

