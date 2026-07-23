using System;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using Newtonsoft.Json.Linq;

namespace TodoSpirits.Unity.Runtime.Bridge
{
    public abstract class BridgeDecodeResult
    {
        private BridgeDecodeResult()
        {
        }

        public sealed class Success : BridgeDecodeResult
        {
            public Success(BridgeEnvelope envelope) => Envelope = envelope;
            public BridgeEnvelope Envelope { get; }
        }

        public sealed class Failure : BridgeDecodeResult
        {
            public Failure(BridgeErrorPayload error) => Error = error;
            public BridgeErrorPayload Error { get; }
        }
    }

    public static class BridgeJsonCodec
    {
        private static readonly JsonSerializerSettings Settings = new JsonSerializerSettings
        {
            MissingMemberHandling = MissingMemberHandling.Error,
            NullValueHandling = NullValueHandling.Ignore,
            DateParseHandling = DateParseHandling.None,
            FloatParseHandling = FloatParseHandling.Double,
            Formatting = Formatting.None,
            Converters = { new StringEnumConverter() },
        };

        public static JsonSerializer Serializer { get; } = JsonSerializer.Create(Settings);

        public static BridgeDecodeResult DecodeEnvelope(string rawJson)
        {
            if (string.IsNullOrWhiteSpace(rawJson))
            {
                return Failure("Bridge JSON is empty.");
            }

            try
            {
                var envelope = JsonConvert.DeserializeObject<BridgeEnvelope>(rawJson, Settings);
                return envelope == null ? Failure("Bridge JSON produced no envelope.") : new BridgeDecodeResult.Success(envelope);
            }
            catch (JsonException exception)
            {
                return Failure(exception.Message);
            }
            catch (ArgumentException exception)
            {
                return Failure(exception.Message);
            }
        }

        public static string EncodeEnvelope(BridgeEnvelope envelope) => JsonConvert.SerializeObject(envelope, Settings);

        public static string EncodeError(BridgeErrorPayload error) => JsonConvert.SerializeObject(error, Settings);

        public static JObject ToPayload<T>(T value) => JObject.FromObject(value, Serializer);

        private static BridgeDecodeResult.Failure Failure(string message) => new BridgeDecodeResult.Failure(
            new BridgeErrorPayload(BridgeErrorCode.BRIDGE_INVALID_JSON, message, false));
    }
}

