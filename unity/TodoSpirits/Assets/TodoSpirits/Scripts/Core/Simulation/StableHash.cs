using System.Text;

namespace TodoSpirits.Core
{
    internal static class StableHash
    {
        private const uint FnvOffsetBasis = 2166136261u;
        private const uint FnvPrime = 16777619u;

        public static uint Fnv1A(string value)
        {
            var hash = FnvOffsetBasis;
            var bytes = Encoding.UTF8.GetBytes(value ?? string.Empty);

            for (var i = 0; i < bytes.Length; i++)
            {
                hash ^= bytes[i];
                hash *= FnvPrime;
            }

            return hash;
        }

        public static int ToSignedSeed(string value)
        {
            return unchecked((int)Fnv1A(value));
        }
    }
}
