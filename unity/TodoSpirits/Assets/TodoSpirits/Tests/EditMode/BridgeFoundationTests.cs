using NUnit.Framework;
using TodoSpirits.Unity.Runtime.Bridge;
using TodoSpirits.Unity.Runtime.Diagnostics;
using TodoSpirits.Unity.Runtime.Spirit;

namespace TodoSpirits.Unity.Tests.EditMode
{
    public sealed class BridgeFoundationTests
    {
        private readonly BridgeEnvelopeValidator _validator = new BridgeEnvelopeValidator();

        [Test]
        public void ValidEnvelopeParsesAndValidates()
        {
            var envelope = ValidEnvelope();
            var decoded = BridgeJsonCodec.DecodeEnvelope(BridgeJsonCodec.EncodeEnvelope(envelope));

            Assert.That(decoded, Is.TypeOf<BridgeDecodeResult.Success>());
            var parsed = ((BridgeDecodeResult.Success)decoded).Envelope;
            Assert.That(_validator.Validate(parsed), Is.TypeOf<BridgeValidationResult.Valid>());
        }

        [TestCase("wrong.protocol", 1, BridgeErrorCode.BRIDGE_PROTOCOL_MISMATCH)]
        [TestCase(BridgeProtocol.Name, 2, BridgeErrorCode.BRIDGE_VERSION_UNSUPPORTED)]
        public void ProtocolAndVersionMismatchAreRejected(string protocol, int version, BridgeErrorCode expected)
        {
            var envelope = ValidEnvelope();
            envelope.Protocol = protocol;
            envelope.Version = version;

            AssertError(envelope, expected);
        }

        [Test]
        public void BlankMessageIdIsRejected()
        {
            var envelope = ValidEnvelope();
            envelope.MessageId = " ";
            AssertError(envelope, BridgeErrorCode.BRIDGE_MESSAGE_ID_MISSING);
        }

        [Test]
        public void BlankSessionIdIsRejected()
        {
            var envelope = ValidEnvelope();
            envelope.SessionId = string.Empty;
            AssertError(envelope, BridgeErrorCode.BRIDGE_SESSION_ID_MISSING);
        }

        [Test]
        public void UnknownMessageTypeIsRejected()
        {
            var envelope = ValidEnvelope();
            envelope.Type = "unknown.message";
            AssertError(envelope, BridgeErrorCode.BRIDGE_UNKNOWN_MESSAGE_TYPE);
        }

        [Test]
        public void InvalidPayloadIsRejected()
        {
            var envelope = ValidEnvelope();
            envelope.Payload = BridgeJsonCodec.ToPayload(new { schemaVersion = 1 });
            AssertError(envelope, BridgeErrorCode.BRIDGE_INVALID_PAYLOAD);
        }

        [Test]
        public void SessionMismatchIsRejected()
        {
            AssertError(
                ValidEnvelope(),
                BridgeErrorCode.BRIDGE_SESSION_MISMATCH,
                new BridgeValidationContext { ActiveSessionId = "another-session" });
        }

        [Test]
        public void InteractionBeforeReadyIsRejected()
        {
            var envelope = ValidEnvelope();
            envelope.Type = BridgeMessageTypes.ToWireValue(BridgeMessageType.SpiritPlayInteraction);
            envelope.Payload = BridgeJsonCodec.ToPayload(new InteractionPayload { InteractionId = "head-pat" });

            AssertError(
                envelope,
                BridgeErrorCode.BRIDGE_RUNTIME_NOT_READY,
                new BridgeValidationContext { RuntimeReady = false });
        }

        [Test]
        public void DuplicateMessageIdIsRejected()
        {
            AssertError(
                ValidEnvelope(),
                BridgeErrorCode.BRIDGE_DUPLICATE_MESSAGE,
                new BridgeValidationContext { ProcessedMessageIds = new System.Collections.Generic.HashSet<string> { "message-1" } });
        }

        [Test]
        public void SpiritSessionValuesAreValidated()
        {
            var session = ValidSession();
            session.Spirit.Stage = 101;
            Assert.That(SpiritSessionValidator.Validate(session), Does.Contain("stage"));

            session = ValidSession();
            session.Spirit.Exp = 601;
            Assert.That(SpiritSessionValidator.Validate(session), Does.Contain("maxExp"));

            session = ValidSession();
            session.Audio.MasterVolume = 1.1;
            Assert.That(SpiritSessionValidator.Validate(session), Does.Contain("masterVolume"));
        }

        [Test]
        public void DispatcherRejectsDuplicateAndNotReadyCommands()
        {
            var dispatcher = new UnityBridgeDispatcher { State = UnityRuntimeState.Ready };
            var encoded = BridgeJsonCodec.EncodeEnvelope(ValidEnvelope());

            Assert.That(dispatcher.Dispatch(encoded), Is.TypeOf<BridgeDispatchResult.Accepted>());
            var duplicate = dispatcher.Dispatch(encoded) as BridgeDispatchResult.Rejected;
            Assert.That(duplicate.Error.Code, Is.EqualTo(BridgeErrorCode.BRIDGE_DUPLICATE_MESSAGE));
        }

        [Test]
        public void StructuredErrorEnvelopeSerializes()
        {
            var error = new BridgeErrorPayload(BridgeErrorCode.BRIDGE_INVALID_PAYLOAD, "invalid", false);
            var envelope = BridgeErrorEnvelopeFactory.Create(error, "session-1", "error-1", "2026-07-23T00:00:00Z");
            var encoded = BridgeJsonCodec.EncodeEnvelope(envelope);

            Assert.That(encoded, Does.Contain("error.occurred"));
            Assert.That(encoded, Does.Contain("BRIDGE_INVALID_PAYLOAD"));
        }

        [Test]
        public void ProductionTransportDoesNotPretendSuccess()
        {
            var result = new NotIntegratedHostBridgeTransport().Send("{}");
            Assert.That(result, Is.TypeOf<BridgeSendResult.Failure>());
        }

        private void AssertError(
            BridgeEnvelope envelope,
            BridgeErrorCode expected,
            BridgeValidationContext context = null)
        {
            var result = _validator.Validate(envelope, context) as BridgeValidationResult.Invalid;
            Assert.That(result, Is.Not.Null);
            Assert.That(result.Error.Code, Is.EqualTo(expected));
        }

        private static BridgeEnvelope ValidEnvelope() => new BridgeEnvelope
        {
            Protocol = BridgeProtocol.Name,
            Version = BridgeProtocol.Version,
            MessageId = "message-1",
            SessionId = "session-1",
            Type = BridgeMessageTypes.ToWireValue(BridgeMessageType.SessionInitialize),
            TimestampUtc = "2026-07-23T00:00:00Z",
            Payload = BridgeJsonCodec.ToPayload(ValidSession()),
        };

        private static SpiritSession ValidSession() => new SpiritSession
        {
            SchemaVersion = 1,
            Locale = "ko-KR",
            Theme = "light",
            Spirit = new SpiritStateSnapshot
            {
                Id = "lumi",
                Name = "루미",
                Stage = 2,
                Exp = 430,
                MaxExp = 600,
                Attributes = new SpiritAttributes { Focus = 68, Vitality = 55, Consistency = 80, Creativity = 42 },
            },
            Appearance = new SpiritAppearanceSnapshot
            {
                ModelId = "lumi-stage-02",
                SkinId = "default",
                GrowthStageId = "stage-02",
                EnvironmentId = "spirit-room-default",
            },
            Audio = new SpiritAudioSnapshot { MasterVolume = 1.0, Muted = false },
        };
    }
}
