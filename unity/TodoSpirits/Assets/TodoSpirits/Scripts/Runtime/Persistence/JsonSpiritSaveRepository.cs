using System;
using System.IO;
using System.Text;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
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
        private bool _loadedFromBackup;

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
            _loadedFromBackup = false;
            string backup = SavePath + ".bak";
            if (!File.Exists(SavePath) && !File.Exists(backup)) return new PrototypeSaveData();
            if (TryRead(SavePath, out var data)) return data;
            if (TryRead(backup, out data)) { _loadedFromBackup = true; return data; }
            throw new IOException("동행 저장과 백업을 읽을 수 없습니다. 기존 파일을 보존했습니다.");
        }

        private static bool TryRead(string path, out PrototypeSaveData data)
        {
            data = null;
            if (!File.Exists(path)) return false;
            try
            {
                var json = File.ReadAllText(path, Encoding.UTF8);
                var document = JObject.Parse(json);
                if (!(document["EssenceWallet"] is JObject) || !(document["Records"] is JArray)) return false;
                data = JsonUtility.FromJson<PrototypeSaveData>(json);
                if (data == null || data.EssenceWallet == null || data.Records == null) return false;
                data.EnsureCollections();
                return true;
            }
            catch (ArgumentException) { return false; }
            catch (JsonException) { return false; }
            catch (IOException) { return false; }
            catch (UnauthorizedAccessException) { return false; }
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
            var temporaryPath = SavePath + ".tmp";
            try
            {
                using (var stream = new FileStream(temporaryPath, FileMode.Create, FileAccess.Write, FileShare.None))
                {
                    var bytes = new UTF8Encoding(false).GetBytes(json);
                    stream.Write(bytes, 0, bytes.Length);
                    stream.Flush(true);
                }
                if (File.Exists(SavePath))
                    File.Replace(temporaryPath, SavePath, _loadedFromBackup ? null : SavePath + ".bak");
                else File.Move(temporaryPath, SavePath);
                _loadedFromBackup = false;
            }
            finally
            {
                if (File.Exists(temporaryPath)) File.Delete(temporaryPath);
            }
        }
    }
}
