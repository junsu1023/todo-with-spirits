#if UNITY_EDITOR
using System;
using System.IO;
using TodoSpirits.ArtKit;
using TodoSpirits.Presentation.Forest;
using UnityEditor;
using UnityEngine;

namespace TodoSpirits.Editor
{
    public static class ForestArtKitBinder
    {
        private const string CatalogPath = "Assets/TodoSpiritsArtKit/Generated/ArtKitCatalog.asset";
        private const string PrefabPath = "Assets/TodoSpirits/Prefabs/UI/PF_UI_ForestApp.prefab";
        private const string QaSaveKey = "TodoSpirits.ForestVerificationSave";

        [MenuItem("TodoSpirits/Art Kit/4 - Use Isolated QA Save")]
        public static void UseIsolatedQaSave()
        {
            if (EditorApplication.isPlayingOrWillChangePlaymode)
                throw new InvalidOperationException("Stop Play Mode before choosing a QA save.");
            string path = Path.Combine(Path.GetTempPath(), "TodoSpirits-ArtKit-QA",
                "forest-" + DateTime.Now.ToString("yyyyMMdd-HHmmss") + ".json");
            SessionState.SetString(QaSaveKey, path);
            Debug.Log("Art Kit QA save selected: " + path);
        }

        [MenuItem("TodoSpirits/Art Kit/5 - Clear Isolated QA Save")]
        public static void ClearIsolatedQaSave()
        {
            if (EditorApplication.isPlayingOrWillChangePlaymode)
                throw new InvalidOperationException("Stop Play Mode before clearing the QA save.");
            SessionState.EraseString(QaSaveKey);
            Debug.Log("Art Kit QA save override cleared.");
        }

        [MenuItem("TodoSpirits/Art Kit/QA - Game View 360x640")]
        public static void QaSmallView() { ForestEditorPreview.SetSize(360, 640); }

        [MenuItem("TodoSpirits/Art Kit/QA - Game View 1080x2340")]
        public static void QaTallView() { ForestEditorPreview.SetSize(1080, 2340); }

        [MenuItem("TodoSpirits/Art Kit/QA - Game View 1366x768")]
        public static void QaWideView() { ForestEditorPreview.SetSize(1366, 768); }

        [MenuItem("TodoSpirits/Art Kit/QA - Capture Game View")]
        public static void CaptureQaView()
        {
            if (!EditorApplication.isPlaying)
                throw new InvalidOperationException("Capture the QA Game View while Play Mode is running.");
            const string folder = "Validation/Concept01ArtKit";
            Directory.CreateDirectory(folder);
            string path = Path.Combine(folder, "game-view-" + Screen.width + "x" + Screen.height + "-" +
                DateTime.Now.ToString("yyyyMMdd-HHmmss") + ".png");
            ScreenCapture.CaptureScreenshot(path);
            Debug.Log("Art Kit Game View capture requested: " + path);
        }

        [MenuItem("TodoSpirits/Art Kit/3 - Connect Forest View")]
        public static void Connect()
        {
            if (EditorApplication.isPlayingOrWillChangePlaymode)
                throw new InvalidOperationException("Stop Play Mode before connecting the forest view.");
            var catalog = AssetDatabase.LoadAssetAtPath<ArtKitCatalog>(CatalogPath);
            if (catalog == null) throw new FileNotFoundException("Build Art Kit resources first.", CatalogPath);
            if (!File.Exists(PrefabPath)) throw new FileNotFoundException("Forest prefab is missing.", PrefabPath);

            var root = PrefabUtility.LoadPrefabContents(PrefabPath);
            try
            {
                var view = root.GetComponent<ForestAppView>();
                if (view == null) throw new InvalidOperationException("Forest prefab has no ForestAppView.");
                var serialized = new SerializedObject(view);
                var field = serialized.FindProperty("artKitCatalog");
                if (field == null) throw new InvalidOperationException("ForestAppView.artKitCatalog is missing.");
                if (field.objectReferenceValue == catalog) return;
                if (field.objectReferenceValue != null)
                    throw new InvalidOperationException("A different Art Kit catalog is already assigned.");

                const string backupFolder = "Documentation/ArtKitBackups";
                Directory.CreateDirectory(backupFolder);
                string backupPath = Path.Combine(backupFolder,
                    "PF_UI_ForestApp-" + DateTime.Now.ToString("yyyyMMdd-HHmmss") + ".prefab");
                File.Copy(PrefabPath, backupPath);
                field.objectReferenceValue = catalog;
                serialized.ApplyModifiedPropertiesWithoutUndo();
                PrefabUtility.SaveAsPrefabAsset(root, PrefabPath);
                AssetDatabase.SaveAssets();
                Debug.Log("Forest Art Kit catalog connected. Backup: " + backupPath);
            }
            finally { PrefabUtility.UnloadPrefabContents(root); }
        }
    }
}
#endif
