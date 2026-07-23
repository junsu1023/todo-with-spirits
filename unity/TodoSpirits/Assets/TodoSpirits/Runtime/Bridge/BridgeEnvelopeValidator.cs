using System;
using System.Collections.Generic;
using System.Globalization;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using TodoSpirits.Unity.Runtime.Spirit;

namespace TodoSpirits.Unity.Runtime.Bridge
{
    public sealed class BridgeValidationContext
    {
        public string ActiveSessionId { get; set; }
        public bool RuntimeReady { get; set; } = true;
        public ISet<string> ProcessedMessageIds { get; set; } = new HashSet<string>(StringComparer.Ordinal);
    }

    public abstract class BridgeValidationResult
    {
        private BridgeValidationResult()
        {
        }

        public sealed class Valid : BridgeValidationResult
        {
            public Valid(BridgeEnvelope envelope, BridgeMessageType messageType)
            {
                Envelope = envelope;
                MessageType = messageType;
            }

            public BridgeEnvelope Envelope { get; }
            public BridgeMessageType MessageType { get; }
        }

        public sealed class Invalid : BridgeValidationResult
        {
            public Invalid(BridgeErrorPayload error) => Error = error;
            public BridgeErrorPayload Error { get; }
        }
    }

    public sealed class BridgeEnvelopeValidator
    {
        public BridgeValidationResult Validate(BridgeEnvelope envelope, BridgeValidationContext context = null)
        {
            context ??= new BridgeValidationContext();

            if (!string.Equals(envelope.Protocol, BridgeProtocol.Name, StringComparison.Ordinal))
                return Invalid(BridgeErrorCode.BRIDGE_PROTOCOL_MISMATCH, "Unsupported bridge protocol.");
            if (envelope.Version != BridgeProtocol.Version)
                return Invalid(BridgeErrorCode.BRIDGE_VERSION_UNSUPPORTED, "Unsupported bridge version.");
            if (!IsValidIdentifier(envelope.MessageId))
                return Invalid(BridgeErrorCode.BRIDGE_MESSAGE_ID_MISSING, "messageId is required.");
            if (!IsValidIdentifier(envelope.SessionId))
                return Invalid(BridgeErrorCode.BRIDGE_SESSION_ID_MISSING, "sessionId is required.");
            if (!BridgeMessageTypes.TryParse(envelope.Type, out var messageType))
                return Invalid(BridgeErrorCode.BRIDGE_UNKNOWN_MESSAGE_TYPE, $"Unknown message type: {envelope.Type}");
            if (context.ProcessedMessageIds.Contains(envelope.MessageId))
                return Invalid(BridgeErrorCode.BRIDGE_DUPLICATE_MESSAGE, "messageId was already processed.");
            if (context.ActiveSessionId != null && !string.Equals(context.ActiveSessionId, envelope.SessionId, StringComparison.Ordinal))
                return Invalid(BridgeErrorCode.BRIDGE_SESSION_MISMATCH, "Message belongs to a different session.");
            if (!context.RuntimeReady && RequiresReadyRuntime(messageType))
                return Invalid(BridgeErrorCode.BRIDGE_RUNTIME_NOT_READY, "Unity runtime is not ready for this command.");
            if (!IsUtcTimestamp(envelope.TimestampUtc))
                return Invalid(BridgeErrorCode.BRIDGE_INVALID_PAYLOAD, "timestampUtc must be a UTC ISO 8601 value.");

            var payloadError = ValidatePayload(messageType, envelope.Payload);
            return payloadError != null
                ? (BridgeValidationResult)payloadError
                : new BridgeValidationResult.Valid(envelope, messageType);
        }

        private static BridgeValidationResult.Invalid ValidatePayload(BridgeMessageType type, JObject payload)
        {
            try
            {
                string error;
                switch (type)
                {
                    case BridgeMessageType.SessionInitialize:
                    case BridgeMessageType.SessionUpdate:
                        error = SpiritSessionValidator.Validate(payload.ToObject<SpiritSession>(BridgeJsonCodec.Serializer));
                        break;
                    case BridgeMessageType.SpiritSetState:
                        error = SpiritSessionValidator.ValidateSpirit(payload.ToObject<SpiritStateSnapshot>(BridgeJsonCodec.Serializer));
                        break;
                    case BridgeMessageType.SpiritPlayInteraction:
                    case BridgeMessageType.SpiritInteracted:
                        error = ValidateRequiredText(payload.ToObject<InteractionPayload>(BridgeJsonCodec.Serializer).InteractionId);
                        break;
                    case BridgeMessageType.AppearanceSetTheme:
                        var theme = payload.ToObject<ThemePayload>(BridgeJsonCodec.Serializer).Theme;
                        error = theme == "light" || theme == "dark" ? null : "theme must be light or dark.";
                        break;
                    case BridgeMessageType.AudioSetState:
                        error = SpiritSessionValidator.ValidateAudio(payload.ToObject<SpiritAudioSnapshot>(BridgeJsonCodec.Serializer));
                        break;
                    case BridgeMessageType.LifecyclePause:
                    case BridgeMessageType.LifecycleResume:
                        error = payload.Count == 0 ? null : "Lifecycle payload must be empty.";
                        break;
                    case BridgeMessageType.RuntimeRequestExit:
                    case BridgeMessageType.ExitRequested:
                        var reason = payload.ToObject<ExitPayload>(BridgeJsonCodec.Serializer).Reason;
                        error = reason != null && reason.Length > 256 ? "reason must not exceed 256 characters." : null;
                        break;
                    case BridgeMessageType.RuntimeReady:
                        var ready = payload.ToObject<RuntimeReadyPayload>(BridgeJsonCodec.Serializer);
                        error = ready.RuntimeState == "Ready" ? ValidateOptionalText(ready.BuildId) : "runtimeState must be Ready.";
                        break;
                    case BridgeMessageType.SceneLoadProgress:
                        var progress = payload.ToObject<SceneProgressPayload>(BridgeJsonCodec.Serializer);
                        error = ValidateRequiredText(progress.SceneId) ??
                                (progress.Progress < 0 || progress.Progress > 1 || double.IsNaN(progress.Progress)
                                    ? "progress must be between 0 and 1."
                                    : null);
                        break;
                    case BridgeMessageType.ContentReady:
                        error = ValidateRequiredText(payload.ToObject<ContentReadyPayload>(BridgeJsonCodec.Serializer).SceneId);
                        break;
                    case BridgeMessageType.AnimationCompleted:
                        error = ValidateRequiredText(payload.ToObject<AnimationPayload>(BridgeJsonCodec.Serializer).AnimationId);
                        break;
                    case BridgeMessageType.DiagnosticMetric:
                        var metric = payload.ToObject<MetricPayload>(BridgeJsonCodec.Serializer);
                        error = ValidateRequiredText(metric.Name) ??
                                (double.IsNaN(metric.Value) || double.IsInfinity(metric.Value) ? "metric value must be finite." : null);
                        break;
                    case BridgeMessageType.ErrorOccurred:
                        error = ValidateRequiredText(payload.ToObject<BridgeErrorPayload>(BridgeJsonCodec.Serializer).Message);
                        break;
                    default:
                        error = "Unsupported payload type.";
                        break;
                }

                return error == null ? null : Invalid(BridgeErrorCode.BRIDGE_INVALID_PAYLOAD, error);
            }
            catch (JsonException exception)
            {
                return Invalid(BridgeErrorCode.BRIDGE_INVALID_PAYLOAD, exception.Message);
            }
            catch (ArgumentException exception)
            {
                return Invalid(BridgeErrorCode.BRIDGE_INVALID_PAYLOAD, exception.Message);
            }
            catch (NullReferenceException)
            {
                return Invalid(BridgeErrorCode.BRIDGE_INVALID_PAYLOAD, "Payload does not match the message type.");
            }
        }

        private static bool RequiresReadyRuntime(BridgeMessageType type) =>
            type == BridgeMessageType.SessionUpdate ||
            type == BridgeMessageType.SpiritSetState ||
            type == BridgeMessageType.SpiritPlayInteraction ||
            type == BridgeMessageType.AppearanceSetTheme ||
            type == BridgeMessageType.AudioSetState;

        private static bool IsValidIdentifier(string value) =>
            !string.IsNullOrWhiteSpace(value) && value.Length <= BridgeProtocol.MaxIdentifierLength;

        private static bool IsUtcTimestamp(string value) =>
            value != null && value.EndsWith("Z", StringComparison.Ordinal) &&
            DateTimeOffset.TryParse(value, CultureInfo.InvariantCulture, DateTimeStyles.AssumeUniversal, out _);

        private static string ValidateRequiredText(string value) =>
            string.IsNullOrWhiteSpace(value) || value.Length > 128 ? "Required text must contain 1 to 128 characters." : null;

        private static string ValidateOptionalText(string value) =>
            value != null && value.Length > 128 ? "Optional text must not exceed 128 characters." : null;

        private static BridgeValidationResult.Invalid Invalid(BridgeErrorCode code, string message) =>
            new BridgeValidationResult.Invalid(new BridgeErrorPayload(code, message, false));
    }

    public static class SpiritSessionValidator
    {
        public static string Validate(SpiritSession session)
        {
            if (session == null) return "SpiritSession is required.";
            if (session.SchemaVersion != 1) return "SpiritSession schemaVersion must be 1.";
            if (session.Locale == null || session.Locale.Length < 2 || session.Locale.Length > 35) return "locale must contain 2 to 35 characters.";
            if (session.Theme != "light" && session.Theme != "dark") return "theme must be light or dark.";
            return ValidateSpirit(session.Spirit) ?? ValidateAppearance(session.Appearance) ?? ValidateAudio(session.Audio);
        }

        public static string ValidateSpirit(SpiritStateSnapshot spirit)
        {
            if (spirit == null) return "spirit is required.";
            if (string.IsNullOrWhiteSpace(spirit.Id) || spirit.Id.Length > 128) return "spirit.id is required.";
            if (string.IsNullOrWhiteSpace(spirit.Name) || spirit.Name.Length > 80) return "spirit.name is required.";
            if (spirit.Stage < 0 || spirit.Stage > 100) return "spirit.stage must be between 0 and 100.";
            if (spirit.Exp < 0) return "spirit.exp must be non-negative.";
            if (spirit.MaxExp <= 0) return "spirit.maxExp must be positive.";
            if (spirit.Exp > spirit.MaxExp) return "spirit.exp must not exceed maxExp.";
            if (spirit.Attributes == null || !InRange(spirit.Attributes.Focus) || !InRange(spirit.Attributes.Vitality) ||
                !InRange(spirit.Attributes.Consistency) || !InRange(spirit.Attributes.Creativity))
                return "spirit attributes must be between 0 and 100.";
            return null;
        }

        public static string ValidateAudio(SpiritAudioSnapshot audio)
        {
            if (audio == null) return "audio is required.";
            return double.IsNaN(audio.MasterVolume) || double.IsInfinity(audio.MasterVolume) || audio.MasterVolume < 0 || audio.MasterVolume > 1
                ? "audio.masterVolume must be between 0 and 1."
                : null;
        }

        private static string ValidateAppearance(SpiritAppearanceSnapshot appearance)
        {
            if (appearance == null) return "appearance is required.";
            return IsIdentifier(appearance.ModelId) && IsIdentifier(appearance.SkinId) &&
                   IsIdentifier(appearance.GrowthStageId) && IsIdentifier(appearance.EnvironmentId)
                ? null
                : "appearance identifiers are required.";
        }

        private static bool InRange(int value) => value >= 0 && value <= 100;

        private static bool IsIdentifier(string value) => !string.IsNullOrWhiteSpace(value) && value.Length <= 128;
    }
}
