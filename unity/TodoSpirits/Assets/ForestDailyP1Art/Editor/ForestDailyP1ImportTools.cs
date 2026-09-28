using System;
using System.Collections.Generic;
using System.IO;
using System.Security.Cryptography;
using UnityEditor;
using UnityEditor.SceneManagement;
using UnityEditor.U2D;
using UnityEngine;
using UnityEngine.SceneManagement;
using UnityEngine.U2D;
using UnityEngine.UI;
using Object = UnityEngine.Object;

namespace TodoSpirits.ForestDailyP1.Editor
{
    /// <summary>
    /// Explicit review-only tools. No startup hooks, automatic scene changes,
    /// package upgrades, save writes, runtime scripts, or generated animation frames.
    /// Source API target: Unity 6.3. Compilation in the receiving project is required.
    /// </summary>
    public static class ForestDailyP1ImportTools
    {
        private const string Root = "Assets/ForestDailyP1Art";
        private const string Output = Root + "/GeneratedReview";
        private const string ReadPath = Root + "/Sprites/Spirit/spirit_read_01.png";
        private const string DeskPath = Root + "/Sprites/Stations/station_desk.png";
        private const string AtlasPath = Output + "/SA_ForestDailyP1_Review.spriteatlasv2";
        private const float PixelsPerUnit = 100f;
        // Source pixel-edge coordinate (256,476), top-left origin -> Unity bottom-left origin.
        private static readonly Vector2 Pivot = new Vector2(0.5f, 36f / 512f);
        private static readonly string[] Paths = { ReadPath, DeskPath };
        private static readonly string[] Hashes = {
            "f5989a67c574cc433dfa7373b240c45b41de5916235a7f85e71d921184328931",
            "b3680ace800387c1a2ef638cbdcf2e0ee9e74d8fa1cbaabf1f147909e428d7d4"
        };

        private static void CheckEditor()
        {
            if (EditorApplication.isPlayingOrWillChangePlaymode || EditorApplication.isCompiling)
                throw new InvalidOperationException("Stop Play Mode and finish compilation first.");
        }

        private static void CheckSources()
        {
            for (int i = 0; i < Paths.Length; i++)
            {
                if (!File.Exists(Paths[i])) throw new FileNotFoundException(Paths[i]);
                using (var sha = SHA256.Create())
                using (var input = File.OpenRead(Paths[i]))
                {
                    string actual = BitConverter.ToString(sha.ComputeHash(input)).Replace("-", "").ToLowerInvariant();
                    if (actual != Hashes[i])
                        throw new InvalidDataException("Source differs from the supplied candidate: " + Paths[i] +
                            ". Do not overwrite it. Review the art and update the manifest/tools deliberately.");
                }
            }
        }

        [MenuItem("TodoSpirits/Forest Daily P1/1 - Configure Two Sprites")]
        public static void ConfigureTwoSprites()
        {
            CheckEditor(); CheckSources();
            // Preserve a copy before changing existing importer settings. Nothing in this backup is imported.
            string backup = Path.Combine(Directory.GetParent(Application.dataPath).FullName,
                "ArtHandoffBackups", "ForestDailyP1", Guid.NewGuid().ToString("N"));
            foreach (string path in Paths)
            {
                string meta = path + ".meta";
                if (!File.Exists(meta)) continue;
                string dest = Path.Combine(backup, meta);
                Directory.CreateDirectory(Path.GetDirectoryName(dest));
                File.Copy(meta, dest, false);
            }
            foreach (string path in Paths)
            {
                AssetDatabase.ImportAsset(path, ImportAssetOptions.ForceSynchronousImport);
                var importer = AssetImporter.GetAtPath(path) as TextureImporter;
                if (!importer) throw new InvalidOperationException("Missing texture importer: " + path);
                var settings = new TextureImporterSettings();
                importer.ReadTextureSettings(settings);
                settings.textureType = TextureImporterType.Sprite;
                settings.spriteMode = (int)SpriteImportMode.Single;
                settings.spriteMeshType = SpriteMeshType.FullRect;
                settings.spriteAlignment = (int)SpriteAlignment.Custom;
                settings.spritePivot = Pivot;
                settings.spritePixelsPerUnit = PixelsPerUnit;
                settings.spriteBorder = Vector4.zero;
                settings.spriteGenerateFallbackPhysicsShape = false;
                settings.alphaSource = TextureImporterAlphaSource.FromInput;
                settings.alphaIsTransparency = true;
                settings.sRGBTexture = true;
                settings.readable = false;
                settings.mipmapEnabled = false;
                settings.npotScale = TextureImporterNPOTScale.None;
                importer.SetTextureSettings(settings);
                importer.wrapMode = TextureWrapMode.Clamp;
                importer.filterMode = FilterMode.Bilinear;
                importer.maxTextureSize = 512;
                importer.textureCompression = TextureImporterCompression.Uncompressed;
                importer.SetPlatformTextureSettings(Platform("Android", 512, true));
                importer.SetPlatformTextureSettings(Platform("WebGL", 512, true));
                importer.SaveAndReimport();
            }
            Debug.Log("[ForestDailyP1] Configured exactly two sprites. Previous metas: " + backup);
        }

        private static TextureImporterPlatformSettings Platform(string name, int maxSize, bool overridden)
        {
            return new TextureImporterPlatformSettings {
                name = name, maxTextureSize = maxSize, overridden = overridden,
                format = TextureImporterFormat.RGBA32,
                textureCompression = TextureImporterCompression.Uncompressed,
                crunchedCompression = false, allowsAlphaSplitting = false
            };
        }

        private static Sprite LoadSprite(string path)
        {
            var sprite = AssetDatabase.LoadAssetAtPath<Sprite>(path);
            if (!sprite) throw new InvalidOperationException("Run Configure Two Sprites first: " + path);
            return sprite;
        }

        private static void EnsureFolder(string path)
        {
            if (AssetDatabase.IsValidFolder(path)) return;
            int split = path.LastIndexOf('/');
            string parent = path.Substring(0, split);
            EnsureFolder(parent);
            if (string.IsNullOrEmpty(AssetDatabase.CreateFolder(parent, path.Substring(split + 1))))
                throw new IOException("Cannot create folder: " + path);
        }

        private static RectTransform Rect(string name, Transform parent, Vector2 size)
        {
            var rt = (RectTransform)new GameObject(name, typeof(RectTransform)).transform;
            if (parent) rt.SetParent(parent, false);
            rt.sizeDelta = size;
            return rt;
        }

        private static void Stretch(RectTransform rt)
        {
            rt.anchorMin = Vector2.zero; rt.anchorMax = Vector2.one;
            rt.offsetMin = Vector2.zero; rt.offsetMax = Vector2.zero;
        }

        private static GameObject UISprite(string name, Sprite sprite)
        {
            var rt = Rect(name, null, new Vector2(512f, 512f));
            rt.pivot = Pivot;
            var image = rt.gameObject.AddComponent<Image>();
            image.sprite = sprite;
            image.type = Image.Type.Simple;
            image.preserveAspect = true;
            image.raycastTarget = false;
            image.useSpriteMesh = false;
            return rt.gameObject;
        }

        private static GameObject WorldSprite(string name, Sprite sprite)
        {
            var obj = new GameObject(name, typeof(SpriteRenderer));
            var renderer = obj.GetComponent<SpriteRenderer>();
            renderer.sprite = sprite;
            renderer.drawMode = SpriteDrawMode.Simple;
            // The receiving project's pipeline material is deliberately not overridden.
            return obj;
        }

        private static GameObject ReadingPair(bool ui, Sprite read, Sprite desk)
        {
            GameObject pair = ui
                ? Rect("P1_ReadingStation_UI_REVIEW", null, new Vector2(560f, 680f)).gameObject
                : new GameObject("P1_ReadingStation_World_REVIEW");
            if (ui) ((RectTransform)pair.transform).pivot = new Vector2(0.5f, 0.25f);
            GameObject actor = ui ? UISprite("Spirit", read) : WorldSprite("Spirit", read);
            GameObject table = ui ? UISprite("Desk", desk) : WorldSprite("Desk", desk);
            actor.transform.SetParent(pair.transform, false);
            table.transform.SetParent(pair.transform, false);
            if (ui)
            {
                var a = (RectTransform)actor.transform;
                var b = (RectTransform)table.transform;
                SetDesignAnchors(a, 0f);
                SetDesignAnchors(b, -148f);
            }
            else
            {
                table.transform.localPosition = new Vector3(0f, -148f / PixelsPerUnit, 0f);
                actor.GetComponent<SpriteRenderer>().sortingOrder = 0;
                table.GetComponent<SpriteRenderer>().sortingOrder = 1;
            }
            // -148px is a REVIEW layout proposal: table top stays below the book/face.
            // Do not treat it as a final world position or production sorting policy.
            return pair;
        }

        private static void SetDesignAnchors(RectTransform child, float offsetY)
        {
            // Map a square 512px child into the 560x680 design rectangle.
            // Both axes receive the same scale when the parent aspect is preserved.
            child.anchorMin = new Vector2(24f / 560f, (134f + offsetY) / 680f);
            child.anchorMax = new Vector2(536f / 560f, (646f + offsetY) / 680f);
            child.offsetMin = child.offsetMax = Vector2.zero;
        }

        private static void SaveNewPrefab(GameObject obj, string filename)
        {
            string path = Output + "/Prefabs/" + filename + ".prefab";
            try
            {
                if (File.Exists(path)) { Debug.Log("Preserved existing prefab: " + path); return; }
                bool success;
                PrefabUtility.SaveAsPrefabAsset(obj, path, out success);
                if (!success) throw new IOException("Prefab save failed: " + path);
            }
            finally { Object.DestroyImmediate(obj); }
        }

        [MenuItem("TodoSpirits/Forest Daily P1/2 - Create Review Prefabs")]
        public static void CreateReviewPrefabs()
        {
            CheckEditor(); CheckSources();
            Sprite read = LoadSprite(ReadPath), desk = LoadSprite(DeskPath);
            EnsureFolder(Output + "/Prefabs");
            SaveNewPrefab(UISprite("P1_SpiritRead01_UI", read), "PF_P1_SpiritRead01_UI");
            SaveNewPrefab(UISprite("P1_Desk_UI", desk), "PF_P1_Desk_UI");
            SaveNewPrefab(WorldSprite("P1_SpiritRead01_World", read), "PF_P1_SpiritRead01_World");
            SaveNewPrefab(WorldSprite("P1_Desk_World", desk), "PF_P1_Desk_World");
            SaveNewPrefab(ReadingPair(true, read, desk), "PF_P1_ReadingStation_UI_REVIEW");
            SaveNewPrefab(ReadingPair(false, read, desk), "PF_P1_ReadingStation_World_REVIEW");
            Debug.Log("[ForestDailyP1] Review prefabs prepared. Existing prefabs preserved. No scene was replaced.");
        }

        [MenuItem("TodoSpirits/Forest Daily P1/3 - Create Optional Review Atlas")]
        public static void CreateOptionalReviewAtlas()
        {
            CheckEditor(); CheckSources();
            string mode = EditorSettings.spritePackerMode.ToString();
            if (mode.IndexOf("V2", StringComparison.OrdinalIgnoreCase) < 0)
                throw new InvalidOperationException("Current Sprite Atlas mode is " + mode +
                    ". V2 is required for this optional tool. No project setting was changed; use individual sprites meanwhile.");
            Sprite read = LoadSprite(ReadPath), desk = LoadSprite(DeskPath);
            if (File.Exists(AtlasPath))
            {
                Debug.Log("Preserved existing atlas: " + AtlasPath);
                Selection.activeObject = AssetDatabase.LoadMainAssetAtPath(AtlasPath);
                return;
            }
            // Avoid packing either candidate into two independently maintained atlases.
            foreach (string guid in AssetDatabase.FindAssets("t:SpriteAtlas"))
            {
                string existing = AssetDatabase.GUIDToAssetPath(guid);
                if (existing == AtlasPath) continue;
                var other = AssetDatabase.LoadAssetAtPath<SpriteAtlas>(existing);
                if (!other) continue;
                foreach (Object packable in SpriteAtlasExtensions.GetPackables(other))
                {
                    string path = AssetDatabase.GetAssetPath(packable);
                    foreach (string png in Paths)
                        if (path == png || (!string.IsNullOrEmpty(path) &&
                            AssetDatabase.IsValidFolder(path) && png.StartsWith(path + "/", StringComparison.Ordinal)))
                            throw new InvalidOperationException("Existing atlas already covers this sprite: " + existing +
                                ". Keep that atlas; do not create a second one.");
                }
            }
            EnsureFolder(Output);
            var source = new SpriteAtlasAsset();
            try
            {
                source.Add(new Object[] { read, desk });
                SpriteAtlasAsset.Save(source, AtlasPath);
            }
            finally { Object.DestroyImmediate(source); }
            AssetDatabase.ImportAsset(AtlasPath, ImportAssetOptions.ForceSynchronousImport);
            var importer = AssetImporter.GetAtPath(AtlasPath) as SpriteAtlasImporter;
            if (!importer) throw new InvalidOperationException("SpriteAtlas V2 importer is not available.");
            var packing = importer.packingSettings;
            packing.enableRotation = false; packing.enableTightPacking = false; packing.padding = 4;
            importer.packingSettings = packing;
            var texture = importer.textureSettings;
            texture.filterMode = FilterMode.Bilinear; texture.readable = false;
            texture.generateMipMaps = false; texture.sRGB = true;
            importer.textureSettings = texture;
            // Max size belongs to platform settings, not the read-only texture maxTextureSize accessor.
            importer.SetPlatformSettings(Platform("DefaultTexturePlatform", 2048, false));
            importer.SetPlatformSettings(Platform("Android", 2048, true));
            importer.SetPlatformSettings(Platform("WebGL", 2048, true));
            importer.includeInBuild = false; // Review only. Production promotion requires an explicit decision.
            importer.SaveAndReimport();
            Selection.activeObject = AssetDatabase.LoadMainAssetAtPath(AtlasPath);
            Debug.Log("[ForestDailyP1] Created a review-only V2 atlas. Inspect Pack Preview for the active target. Android/Web builds are NOT certified.");
        }

        [MenuItem("TodoSpirits/Forest Daily P1/4 - Open Isolated UI Review Scene")]
        public static void OpenIsolatedUIReviewScene()
        {
            CheckEditor(); CheckSources();
            if (Application.isBatchMode)
                throw new InvalidOperationException("Open the visual review scene interactively; use ValidateImports for batch checks.");
            Sprite read = LoadSprite(ReadPath), desk = LoadSprite(DeskPath);
            Scene previous = SceneManager.GetActiveScene();
            Scene review = EditorSceneManager.NewScene(NewSceneSetup.EmptyScene, NewSceneMode.Additive);
            SceneManager.SetActiveScene(review);
            try
            {
                var canvasObject = new GameObject("ForestDailyP1_REVIEW_ONLY", typeof(RectTransform), typeof(Canvas), typeof(CanvasScaler));
                Undo.RegisterCreatedObjectUndo(canvasObject, "Create ForestDaily P1 Review");
                var canvas = canvasObject.GetComponent<Canvas>();
                canvas.renderMode = RenderMode.ScreenSpaceOverlay; canvas.sortingOrder = 20000;
                var scaler = canvasObject.GetComponent<CanvasScaler>();
                scaler.uiScaleMode = CanvasScaler.ScaleMode.ScaleWithScreenSize;
                scaler.referenceResolution = new Vector2(1080f, 1920f);
                scaler.screenMatchMode = CanvasScaler.ScreenMatchMode.MatchWidthOrHeight;
                scaler.matchWidthOrHeight = 0.5f;
                var matte = Rect("ReviewMatte", canvasObject.transform, Vector2.zero);
                Stretch(matte);
                var matteImage = matte.gameObject.AddComponent<Image>();
                matteImage.color = new Color(0.10f, 0.14f, 0.12f, 1f); matteImage.raycastTarget = false;
                var viewport = Rect("PortraitReviewViewport_9x16", canvasObject.transform, Vector2.zero);
                viewport.anchorMin = viewport.anchorMax = new Vector2(0.5f, 0.5f);
                var fit = viewport.gameObject.AddComponent<AspectRatioFitter>();
                fit.aspectMode = AspectRatioFitter.AspectMode.FitInParent; fit.aspectRatio = 9f / 16f;
                var paper = Rect("ExistingForestOrSolidFallback", viewport, Vector2.zero);
                Stretch(paper);
                var background = paper.gameObject.AddComponent<Image>(); background.raycastTarget = false;
                var existingBackground = AssetDatabase.LoadAssetAtPath<Sprite>(
                    "Assets/TodoSpiritsArtKit/Backgrounds/bg_forest_storybook_fallback.png");
                if (existingBackground)
                {
                    background.sprite = existingBackground; background.preserveAspect = true;
                }
                else background.color = new Color(0.93f, 0.93f, 0.84f, 1f);
                // Keep one square pixel grid for both sprites: no separate aspect stretching.
                var pairArea = Rect("PairArea", viewport, Vector2.zero);
                pairArea.anchorMin = new Vector2(0.12f, 0.16f);
                pairArea.anchorMax = new Vector2(0.88f, 0.71f);
                pairArea.offsetMin = pairArea.offsetMax = Vector2.zero;
                var pair = ReadingPair(true, read, desk);
                pair.transform.SetParent(pairArea, false);
                ((RectTransform)pair.transform).pivot = new Vector2(0.5f, 0.5f);
                var pairFit = pair.AddComponent<AspectRatioFitter>();
                pairFit.aspectMode = AspectRatioFitter.AspectMode.FitInParent;
                pairFit.aspectRatio = 560f / 680f;
                // This rig tests visual scaling only. Existing production Safe Area rules are not modified.
                Selection.activeGameObject = canvasObject;
                Debug.Log("[ForestDailyP1] Opened an unsaved additive review scene. Save it only to a QA path. Existing scenes and build settings were not saved or changed. Close this review scene after inspection.");
            }
            finally { if (previous.IsValid() && previous.isLoaded) SceneManager.SetActiveScene(previous); }
        }

        [Serializable] private sealed class ValidationReport
        {
            public string unityVersion;
            public string activeBuildTarget;
            public string textureImportStatus;
            public string artApproval = "NOT_RECORDED";
            public string androidBuild = "NOT_RUN_BY_THIS_TOOL";
            public string webBuild = "NOT_RUN_BY_THIS_TOOL";
            public string animation = "NOT_APPLICABLE_SECOND_FRAME_MISSING";
            public string atlasStatus;
            public string[] errors;
        }

        [MenuItem("TodoSpirits/Forest Daily P1/5 - Validate Imports")]
        public static void ValidateImports()
        {
            CheckEditor(); CheckSources();
            var errors = new List<string>();
            foreach (string path in Paths)
            {
                var importer = AssetImporter.GetAtPath(path) as TextureImporter;
                var sprite = AssetDatabase.LoadAssetAtPath<Sprite>(path);
                if (!importer || !sprite) { errors.Add("Missing Sprite/importer: " + path); continue; }
                var settings = new TextureImporterSettings(); importer.ReadTextureSettings(settings);
                if (sprite.rect.width != 512f || sprite.rect.height != 512f) errors.Add("Sprite rect is not 512x512: " + path);
                if (Vector2.Distance(sprite.pivot, new Vector2(256f, 36f)) > 0.01f) errors.Add("Unexpected pivot: " + path);
                if (Mathf.Abs(sprite.pixelsPerUnit - PixelsPerUnit) > 0.01f) errors.Add("Unexpected PPU: " + path);
                if (settings.spriteMeshType != SpriteMeshType.FullRect) errors.Add("Not FullRect: " + path);
                if (importer.isReadable || importer.mipmapEnabled || !importer.alphaIsTransparency) errors.Add("Unexpected import flags: " + path);
                if (importer.filterMode != FilterMode.Bilinear || importer.wrapMode != TextureWrapMode.Clamp) errors.Add("Unexpected sampling: " + path);
                if (importer.spriteImportMode != SpriteImportMode.Single || !importer.sRGBTexture) errors.Add("Unexpected Sprite/color mode: " + path);
                foreach (string target in new[] { "Android", "WebGL" })
                {
                    var platform = importer.GetPlatformTextureSettings(target);
                    if (!platform.overridden || platform.maxTextureSize != 512 || platform.format != TextureImporterFormat.RGBA32)
                        errors.Add("Unexpected review platform baseline: " + target + " / " + path);
                }
            }
            var atlas = AssetDatabase.LoadAssetAtPath<SpriteAtlas>(AtlasPath);
            var report = new ValidationReport {
                unityVersion = Application.unityVersion,
                activeBuildTarget = EditorUserBuildSettings.activeBuildTarget.ToString(),
                textureImportStatus = errors.Count == 0 ? "PASS" : "FAIL",
                atlasStatus = atlas ? "ASSET_PRESENT_PACK_PREVIEW_AND_TARGET_CHECK_REQUIRED" : "NOT_CREATED_OPTIONAL",
                errors = errors.ToArray()
            };
            string folder = Path.Combine(Directory.GetParent(Application.dataPath).FullName, "Logs", "ForestDailyP1");
            Directory.CreateDirectory(folder);
            string result = Path.Combine(folder, "import_" + DateTime.UtcNow.ToString("yyyyMMdd_HHmmss_fff") + ".json");
            File.WriteAllText(result, JsonUtility.ToJson(report, true));
            if (errors.Count > 0) throw new InvalidOperationException("Import validation failed: " + result + "\n" + string.Join("\n", errors));
            Debug.Log("[ForestDailyP1] Texture importer checks PASS. Not a release certification. Report: " + result);
        }
    }
}
