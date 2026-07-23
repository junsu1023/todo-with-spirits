using System;
using System.Collections.Generic;
using TodoSpirits.Unity.Runtime.Diagnostics;

namespace TodoSpirits.Unity.Runtime.Bridge
{
    public abstract class BridgeDispatchResult
    {
        private BridgeDispatchResult()
        {
        }

        public sealed class Accepted : BridgeDispatchResult
        {
            public Accepted(BridgeEnvelope envelope, BridgeMessageType messageType)
            {
                Envelope = envelope;
                MessageType = messageType;
            }

            public BridgeEnvelope Envelope { get; }
            public BridgeMessageType MessageType { get; }
        }

        public sealed class Rejected : BridgeDispatchResult
        {
            public Rejected(BridgeErrorPayload error) => Error = error;
            public BridgeErrorPayload Error { get; }
        }
    }

    public sealed class UnityBridgeDispatcher
    {
        private readonly BridgeEnvelopeValidator _validator = new BridgeEnvelopeValidator();
        private readonly HashSet<string> _processedMessageIds = new HashSet<string>(StringComparer.Ordinal);

        public UnityRuntimeState State { get; set; } = UnityRuntimeState.Created;
        public string ActiveSessionId { get; private set; }

        public BridgeDispatchResult Dispatch(string rawJson)
        {
            var decoded = BridgeJsonCodec.DecodeEnvelope(rawJson);
            if (decoded is BridgeDecodeResult.Failure decodeFailure)
                return new BridgeDispatchResult.Rejected(decodeFailure.Error);

            var envelope = ((BridgeDecodeResult.Success)decoded).Envelope;
            var validation = _validator.Validate(envelope, new BridgeValidationContext
            {
                ActiveSessionId = ActiveSessionId,
                RuntimeReady = IsReady(State),
                ProcessedMessageIds = _processedMessageIds,
            });

            if (validation is BridgeValidationResult.Invalid invalid)
                return new BridgeDispatchResult.Rejected(invalid.Error);

            var valid = (BridgeValidationResult.Valid)validation;
            if (valid.MessageType == BridgeMessageType.SessionInitialize && ActiveSessionId == null)
                ActiveSessionId = envelope.SessionId;
            _processedMessageIds.Add(envelope.MessageId);
            return new BridgeDispatchResult.Accepted(envelope, valid.MessageType);
        }

        private static bool IsReady(UnityRuntimeState state) =>
            state == UnityRuntimeState.Ready || state == UnityRuntimeState.SceneLoading || state == UnityRuntimeState.Interactive;
    }

    public static class BridgeErrorEnvelopeFactory
    {
        public static BridgeEnvelope Create(
            BridgeErrorPayload error,
            string sessionId,
            string messageId,
            string timestampUtc)
        {
            return new BridgeEnvelope
            {
                Protocol = BridgeProtocol.Name,
                Version = BridgeProtocol.Version,
                MessageId = messageId,
                SessionId = sessionId,
                Type = BridgeMessageTypes.ToWireValue(BridgeMessageType.ErrorOccurred),
                TimestampUtc = timestampUtc,
                Payload = BridgeJsonCodec.ToPayload(error),
            };
        }
    }
}

