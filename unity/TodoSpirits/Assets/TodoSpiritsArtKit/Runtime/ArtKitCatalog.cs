using System;
using System.Collections.Generic;
using UnityEngine;

namespace TodoSpirits.ArtKit
{
    [CreateAssetMenu(menuName = "TodoSpirits/Art Kit/Catalog")]
    public sealed class ArtKitCatalog : ScriptableObject
    {
        [Serializable] public sealed class Entry { public string id; public Sprite sprite; }
        [Serializable] public sealed class Sequence
        {
            public string id;
            public Sprite[] frames = Array.Empty<Sprite>();
            [Min(1)] public float fps = 20;
            public bool loop;
        }
        public Entry[] sprites = Array.Empty<Entry>();
        public Sequence[] sequences = Array.Empty<Sequence>();
        private Dictionary<string, Sprite> lookup;
        private void OnEnable() { lookup = null; }
        public void InvalidateLookup() { lookup = null; }
        public Sprite GetSprite(string id)
        {
            if (lookup == null)
            {
                lookup = new Dictionary<string, Sprite>(StringComparer.Ordinal);
                foreach (var entry in sprites)
                    if (entry != null && !string.IsNullOrEmpty(entry.id)) lookup[entry.id] = entry.sprite;
            }
            if (!lookup.TryGetValue(id, out var value) || value == null)
                throw new InvalidOperationException($"ArtKit sprite is missing: {id}");
            return value;
        }
        public Sequence GetSequence(string id)
        {
            foreach (var sequence in sequences)
                if (sequence != null && sequence.id == id) return sequence;
            throw new InvalidOperationException($"ArtKit sequence is missing: {id}");
        }
    }
}
