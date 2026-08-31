using System;
using System.IO;
using System.Text;
using UnityEngine;

namespace TodoSpirits.Runtime
{
    /// <summary>
    /// Prototype-only local JSON persistence. This is not a server synchronization format.
    /// </summary>
    public sealed class JsonSpiritSaveRepository : ISpiritSaveRepository
    {
        private const string SaveDirectoryName = "TodoSpirits";
        private const string SaveFileName = "prototype-save.json";

        public string SavePath { get; }

        public static string DefaultSavePath => Path.Combine(
            Application.persistentDataPath,
            SaveDirectoryName,
            SaveFileName);

        public JsonSpiritSaveRepository()
            : this(DefaultSavePath)
        {
        }

        public JsonSpiritSaveRepository(string savePath)
        {
            if (string.IsNullOrWhiteSpace(savePath))
            {
                throw new ArgumentException("A save path is required.", nameof(savePath));
            }

            SavePath = savePath;
        }

        public PrototypeSaveData Load()
        {
            if (!File.Exists(SavePath))
            {
                return new PrototypeSaveData();
            }

            try
            {
                var json = File.ReadAllText(SavePath, Encoding.UTF8);
                var saveData = JsonUtility.FromJson<PrototypeSaveData>(json);
                if (saveData == null)
                {
                    Debug.LogWarning($"Prototype save was empty or invalid: {SavePath}");
                    return new PrototypeSaveData();
                }

                saveData.EnsureCollections();
                return saveData;
            }
            catch (Exception exception)
            {
                Debug.LogWarning($"Could not load prototype save at {SavePath}. A new in-memory save will be used.\n{exception}");
                return new PrototypeSaveData();
            }
        }

        public void Save(PrototypeSaveData saveData)
        {
            if (saveData == null)
            {
                throw new ArgumentNullException(nameof(saveData));
            }

            saveData.EnsureCollections();
            var directory = Path.GetDirectoryName(SavePath);
            if (!string.IsNullOrEmpty(directory))
            {
                Directory.CreateDirectory(directory);
            }

            var json = JsonUtility.ToJson(saveData, true);
            File.WriteAllText(SavePath, json, new UTF8Encoding(false));
        }
    }
}
