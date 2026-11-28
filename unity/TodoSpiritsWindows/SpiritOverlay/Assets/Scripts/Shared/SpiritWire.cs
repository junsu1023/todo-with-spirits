using System;
using System.IO;
using System.Text;

namespace TodoSpirits.Contracts
{
    public enum SpiritMessageKind { Hello = 1, Ready = 2, Heartbeat = 3, Clicked = 4, Shutdown = 5, Visibility = 6, MenuRequested = 7 }
    public sealed class SpiritMessage
    {
        public SpiritMessageKind Kind { get; set; }
        public string SpiritId { get; set; } = "";
        public double X { get; set; }
        public double Y { get; set; }
        public double Width { get; set; }
        public double Height { get; set; }
        public bool Visible { get; set; } = true;
    }
    // C# 7 compatible for Unity Mono. Little-endian length followed by versioned payload.
    public static class SpiritWire
    {
        public const int Version = 1;
        public const int MaxPayloadBytes = 4096;
        public static byte[] Encode(SpiritMessage message)
        {
            using (var stream = new MemoryStream())
            using (var writer = new BinaryWriter(stream, Encoding.UTF8, true))
            {
                writer.Write(Version); writer.Write((int)message.Kind);
                writer.Write(message.SpiritId); writer.Write(message.X); writer.Write(message.Y);
                writer.Write(message.Width); writer.Write(message.Height); writer.Write(message.Visible);
                var payload = stream.ToArray();
                if (payload.Length > MaxPayloadBytes) throw new InvalidDataException("Spirit message too large.");
                using (var framed = new MemoryStream())
                using (var prefix = new BinaryWriter(framed))
                { prefix.Write(payload.Length); prefix.Write(payload); return framed.ToArray(); }
            }
        }
        public static SpiritMessage Decode(byte[] payload)
        {
            if (payload.Length > MaxPayloadBytes) throw new InvalidDataException("Spirit message too large.");
            using (var stream = new MemoryStream(payload))
            using (var reader = new BinaryReader(stream, Encoding.UTF8, true))
            {
                if (reader.ReadInt32() != Version) throw new InvalidDataException("Spirit protocol version mismatch.");
                var message = new SpiritMessage { Kind = (SpiritMessageKind)reader.ReadInt32(),
                    SpiritId = reader.ReadString(), X = reader.ReadDouble(), Y = reader.ReadDouble(),
                    Width = reader.ReadDouble(), Height = reader.ReadDouble(), Visible = reader.ReadBoolean() };
                if (!Enum.IsDefined(typeof(SpiritMessageKind), message.Kind) || message.SpiritId.Length > 128 || stream.Position != stream.Length)
                    throw new InvalidDataException("Invalid spirit message.");
                if (double.IsNaN(message.X) || double.IsInfinity(message.X) || double.IsNaN(message.Y) || double.IsInfinity(message.Y)
                    || !(message.Width >= 0 && message.Width <= 4096) || !(message.Height >= 0 && message.Height <= 4096))
                    throw new InvalidDataException("Invalid spirit coordinates.");
                return message;
            }
        }
    }
}
