namespace TodoSpirits.Unity.Runtime.Bridge
{
    public interface IHostBridgeTransport
    {
        BridgeSendResult Send(string encodedEnvelope);
    }

    public abstract class BridgeSendResult
    {
        private BridgeSendResult()
        {
        }

        public sealed class Sent : BridgeSendResult
        {
        }

        public sealed class Failure : BridgeSendResult
        {
            public Failure(string code, string message)
            {
                Code = code;
                Message = message;
            }

            public string Code { get; }
            public string Message { get; }
        }
    }

    public sealed class NotIntegratedHostBridgeTransport : IHostBridgeTransport
    {
        public BridgeSendResult Send(string encodedEnvelope) => new BridgeSendResult.Failure(
            "UNITY_TRANSPORT_NOT_INTEGRATED",
            "A production host transport has not been integrated.");
    }
}

