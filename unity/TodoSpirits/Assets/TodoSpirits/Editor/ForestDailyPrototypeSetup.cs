#if UNITY_EDITOR
using System;
using System.IO;
using System.Linq;
using TodoSpirits.Presentation.Forest;
using UnityEditor;
using UnityEditor.U2D.Sprites;
using UnityEngine;

namespace TodoSpirits.Editor
{
    /// <summary>Imports the new props and growth sheets; preserves the original poses and P1 imports.</summary>
    public static class ForestDailyPrototypeSetup
    {
        private const string PrefabPath = "Assets/TodoSpirits/Prefabs/UI/PF_UI_ForestApp.prefab";
        private static readonly string[] Fields = { "readingSpirit", "readingDesk", "teaCup", "restCushion" };
        private static readonly string[] Paths = {
            "Assets/ForestDailyP1Art/Sprites/Spirit/spirit_read_01.png",
            "Assets/ForestDailyP1Art/Sprites/Stations/station_desk.png",
            "Assets/TodoSpirits/Art/ForestDaily/tea_cup.png",
            "Assets/TodoSpirits/Art/ForestDaily/rest_cushion.png"
        };
        private static readonly string[] GrowthFields = { "adolescentSpiritPoses", "adultSpiritPoses" };
        private static readonly string[] GrowthPaths = {
            "Assets/TodoSpirits/Art/ForestDaily/wind-spirit-adolescent-v2.png",
            "Assets/TodoSpirits/Art/ForestDaily/wind-spirit-adult-v2.png"
        };
        // Only the previously shipped four-pose sheets may migrate to their cleaned copies.
        // The original PNGs and metadata remain untouched for rollback and reference safety.
        private static readonly string[] PreviousGrowthPaths = {
            "Assets/TodoSpirits/Art/ForestDaily/wind-spirit-adolescent-v1.png",
            "Assets/TodoSpirits/Art/ForestDaily/wind-spirit-adult-v1.png"
        };
        private static readonly string[] GrowthPrefixes = { "wind_adolescent_", "wind_adult_" };
        private static readonly string[] PoseNames = { "idle", "blink", "happy", "curious" };
        private const int GrowthSheetSize = 1254;

        [MenuItem("TodoSpirits/Forest Daily Prototype/Prepare Resources")]
        public static void PrepareResources()
        {
            if (EditorApplication.isPlayingOrWillChangePlaymode)
                throw new InvalidOperationException("Stop Play Mode before preparing the forest resources.");
            if (!File.Exists(PrefabPath)) throw new FileNotFoundException("Forest prefab is missing.", PrefabPath);
            foreach (var path in Paths)
                if (!File.Exists(path)) throw new FileNotFoundException("Forest resource is missing.", path);
            foreach (var path in GrowthPaths)
                if (!File.Exists(path)) throw new FileNotFoundException("Growth sheet is missing.", path);
            // P1 imports and GUIDs are deliberately read as-is.
            RequireSprite(Paths[0]);
            RequireSprite(Paths[1]);
            ValidateSourceAlpha(Paths[2]);
            ValidateSourceAlpha(Paths[3]);
            foreach (var path in GrowthPaths) ValidateSourceAlpha(path, GrowthSheetSize);

            var root = PrefabUtility.LoadPrefabContents(PrefabPath);
            try
            {
                var view = root.GetComponent<ForestAppView>();
                if (view == null) throw new InvalidOperationException("Forest prefab has no ForestAppView.");
                var serialized = new SerializedObject(view);
                var fields = new SerializedProperty[Fields.Length];
                for (int i = 0; i < Fields.Length; i++)
                {
                    fields[i] = RequireField(serialized, Fields[i]);
                    var current = fields[i].objectReferenceValue;
                    if (current != null && AssetDatabase.GetAssetPath(current) != Paths[i])
                        throw new InvalidOperationException("Preserving a different assigned resource: " + Fields[i] + ". Review it before installing.");
                }
                var growthFields = new SerializedProperty[GrowthFields.Length];
                for (int i = 0; i < GrowthFields.Length; i++)
                {
                    growthFields[i] = RequireArray(serialized, GrowthFields[i]);
                    if (growthFields[i].arraySize != 0 && growthFields[i].arraySize != 4)
                        throw new InvalidOperationException("Preserving an unexpected growth array: " + GrowthFields[i]);
                    for (int pose = 0; pose < growthFields[i].arraySize; pose++)
                    {
                        var current = growthFields[i].GetArrayElementAtIndex(pose).objectReferenceValue;
                        if (current != null && ((AssetDatabase.GetAssetPath(current) != GrowthPaths[i]
                            && AssetDatabase.GetAssetPath(current) != PreviousGrowthPaths[i]) || current.name != GrowthPrefixes[i] + PoseNames[pose]))
                            throw new InvalidOperationException("Preserving a different assigned growth pose: " + GrowthFields[i] + "[" + pose + "].");
                    }
                    ValidateExistingGrowthSlices(GrowthPaths[i], GrowthPrefixes[i]);
                }

                ConfigureNewProp(Paths[2]);
                ConfigureNewProp(Paths[3]);
                for (int i = 0; i < GrowthPaths.Length; i++) ConfigureGrowth(GrowthPaths[i], GrowthPrefixes[i]);
                bool changed = false;
                for (int i = 0; i < fields.Length; i++)
                {
                    var sprite = RequireSprite(Paths[i]);
                    if (fields[i].objectReferenceValue == sprite) continue;
                    fields[i].objectReferenceValue = sprite;
                    changed = true;
                }
                for (int i = 0; i < growthFields.Length; i++)
                {
                    var sprites = RequireGrowthSprites(GrowthPaths[i], GrowthPrefixes[i]);
                    if (growthFields[i].arraySize != 4) { growthFields[i].arraySize = 4; changed = true; }
                    for (int pose = 0; pose < 4; pose++)
                    {
                        var field = growthFields[i].GetArrayElementAtIndex(pose);
                        if (field.objectReferenceValue == sprites[pose]) continue;
                        field.objectReferenceValue = sprites[pose];
                        changed = true;
                    }
                }
                if (changed)
                {
                    serialized.ApplyModifiedPropertiesWithoutUndo();
                    var saved = PrefabUtility.SaveAsPrefabAsset(root, PrefabPath);
                    if (saved == null) throw new IOException("Could not save the forest prefab.");
                }
                Debug.Log("[ForestDailyPrototype] Prepared two prop imports and two growth sheets; " +
                    (changed ? "connected" : "confirmed") + " four sprite references and two four-pose arrays. Existing P1 imports, original four expression poses, GUIDs and other references preserved.");
            }
            finally { PrefabUtility.UnloadPrefabContents(root); }
            ValidateResources();
        }

        [MenuItem("TodoSpirits/Forest Daily Prototype/Validate Resources")]
        public static void ValidateResources()
        {
            var prefab = AssetDatabase.LoadAssetAtPath<GameObject>(PrefabPath);
            if (prefab == null) throw new FileNotFoundException("Forest prefab is missing.", PrefabPath);
            var view = prefab.GetComponent<ForestAppView>();
            if (view == null) throw new InvalidOperationException("Forest prefab has no ForestAppView.");
            var serialized = new SerializedObject(view);
            for (int i = 0; i < Fields.Length; i++)
            {
                var sprite = RequireSprite(Paths[i]);
                if (RequireField(serialized, Fields[i]).objectReferenceValue != sprite)
                    throw new InvalidOperationException("Unexpected forest sprite reference: " + Fields[i]);
            }
            for (int i = 2; i < Paths.Length; i++)
            {
                var importer = AssetImporter.GetAtPath(Paths[i]) as TextureImporter;
                if (importer == null || importer.textureType != TextureImporterType.Sprite || importer.spriteImportMode != SpriteImportMode.Single
                    || !importer.alphaIsTransparency || importer.alphaSource != TextureImporterAlphaSource.FromInput || !importer.sRGBTexture
                    || importer.mipmapEnabled || importer.isReadable || importer.filterMode != FilterMode.Bilinear
                    || importer.wrapMode != TextureWrapMode.Clamp || importer.maxTextureSize != 1024)
                    throw new InvalidOperationException("Unexpected new prop import settings: " + Paths[i]);
                ValidatePlatform(importer, "Android", TextureImporterFormat.ASTC_6x6);
                ValidatePlatform(importer, "WebGL", TextureImporterFormat.DXT5);
                ValidateSourceAlpha(Paths[i]);
            }
            for (int i = 0; i < GrowthPaths.Length; i++)
            {
                var importer = AssetImporter.GetAtPath(GrowthPaths[i]) as TextureImporter;
                if (importer == null || importer.textureType != TextureImporterType.Sprite || importer.spriteImportMode != SpriteImportMode.Multiple
                    || !importer.alphaIsTransparency || importer.alphaSource != TextureImporterAlphaSource.FromInput || !importer.sRGBTexture
                    || importer.mipmapEnabled || importer.isReadable || importer.filterMode != FilterMode.Bilinear
                    || importer.wrapMode != TextureWrapMode.Clamp || importer.maxTextureSize != 2048)
                    throw new InvalidOperationException("Unexpected growth import settings: " + GrowthPaths[i]);
                ValidateExistingGrowthSlices(GrowthPaths[i], GrowthPrefixes[i]);
                var sprites = RequireGrowthSprites(GrowthPaths[i], GrowthPrefixes[i]);
                var field = RequireArray(serialized, GrowthFields[i]);
                if (field.arraySize != 4) throw new InvalidOperationException("Growth array must contain four poses: " + GrowthFields[i]);
                for (int pose = 0; pose < 4; pose++)
                    if (field.GetArrayElementAtIndex(pose).objectReferenceValue != sprites[pose])
                        throw new InvalidOperationException("Unexpected growth pose reference: " + GrowthFields[i] + "[" + pose + "].");
                ValidatePlatform(importer, "Android", TextureImporterFormat.ASTC_6x6, 2048);
                ValidatePlatform(importer, "WebGL", TextureImporterFormat.DXT5, 2048);
                ValidateSourceAlpha(GrowthPaths[i], GrowthSheetSize);
            }
            Debug.Log("[ForestDailyPrototype] Resource validation passed: four assigned sprites, two Single Sprite props at 1024, two exact 2x2 growth sheets at 2048; Android ASTC 6x6 / WebGL DXT5 and source alpha inspected. This is resource validation, not device/build verification.");
        }

        private static SerializedProperty RequireArray(SerializedObject serialized, string name)
        {
            var field = serialized.FindProperty(name);
            if (field == null || !field.isArray || field.propertyType == SerializedPropertyType.String)
                throw new InvalidOperationException("ForestAppView Sprite array is missing: " + name);
            return field;
        }

        private static Rect GrowthRect(int pose)
        {
            int cell = GrowthSheetSize / 2;
            return new Rect((pose % 2) * cell, (1 - pose / 2) * cell, cell, cell);
        }

        private static void ValidateExistingGrowthSlices(string path, string prefix)
        {
            var importer = AssetImporter.GetAtPath(path) as TextureImporter;
            if (importer == null) throw new InvalidOperationException("Growth texture importer is missing: " + path);
#pragma warning disable CS0618
            var slices = importer.spritesheet;
#pragma warning restore CS0618
            if (slices == null || slices.Length == 0) return;
            if (slices.Length != 4) throw new InvalidOperationException("Preserving existing non-grid growth slices: " + path);
            for (int pose = 0; pose < 4; pose++)
            {
                var matches = slices.Where(slice => slice.name == prefix + PoseNames[pose]).ToArray();
                if (matches.Length != 1 || matches[0].rect != GrowthRect(pose))
                    throw new InvalidOperationException("Preserving different existing growth slice names or rectangles: " + path);
            }
        }

        private static Sprite[] RequireGrowthSprites(string path, string prefix)
        {
            var sprites = AssetDatabase.LoadAllAssetsAtPath(path).OfType<Sprite>().ToArray();
            if (sprites.Length != 4) throw new InvalidOperationException("Expected exactly four growth sprites: " + path);
            return PoseNames.Select((name, pose) => {
                var matches = sprites.Where(sprite => sprite.name == prefix + name).ToArray();
                if (matches.Length != 1 || matches[0].rect != GrowthRect(pose))
                    throw new InvalidOperationException("Unexpected growth pose name or rectangle: " + path + " / " + name);
                return matches[0];
            }).ToArray();
        }

        private static void ConfigureGrowth(string path, string prefix)
        {
            var importer = AssetImporter.GetAtPath(path) as TextureImporter;
            if (importer == null) throw new InvalidOperationException("Growth texture importer is missing: " + path);
            string guid = AssetDatabase.AssetPathToGUID(path);
            ValidateExistingGrowthSlices(path, prefix);
            var settings = new TextureImporterSettings();
            importer.ReadTextureSettings(settings);
            settings.textureType = TextureImporterType.Sprite;
            settings.spriteMode = (int)SpriteImportMode.Multiple;
            settings.spriteMeshType = SpriteMeshType.FullRect;
            settings.spriteAlignment = (int)SpriteAlignment.Center;
            settings.spritePivot = Vector2.one * .5f;
            settings.spritePixelsPerUnit = 100;
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
            importer.maxTextureSize = 2048;
            importer.textureCompression = TextureImporterCompression.CompressedHQ;
            importer.SetPlatformTextureSettings(Platform("Android", TextureImporterFormat.ASTC_6x6, 2048));
            importer.SetPlatformTextureSettings(Platform("WebGL", TextureImporterFormat.DXT5, 2048));
            // Exact registered cells only. On rerun keep existing subasset IDs and slice metadata.
            var factories = new SpriteDataProviderFactories();
            factories.Init();
            var provider = factories.GetSpriteEditorDataProviderFromObject(importer);
            if (provider == null) throw new InvalidOperationException("Sprite Editor data provider is missing: " + path);
            provider.InitSpriteEditorDataProvider();
            if (provider.GetSpriteRects().Length == 0)
            {
                var rects = PoseNames.Select((name, pose) => new SpriteRect {
                    name = prefix + name, rect = GrowthRect(pose), alignment = SpriteAlignment.Center,
                    pivot = Vector2.one * .5f, border = Vector4.zero, spriteID = GUID.Generate()
                }).ToArray();
                provider.SetSpriteRects(rects);
                provider.GetDataProvider<ISpriteNameFileIdDataProvider>().SetNameFileIdPairs(rects.Select(rect => new SpriteNameFileIdPair(rect.name, rect.spriteID)));
                provider.Apply();
            }
            importer.SaveAndReimport();
            if (AssetDatabase.AssetPathToGUID(path) != guid) throw new InvalidOperationException("Growth texture GUID changed: " + path);
            RequireGrowthSprites(path, prefix);
        }

        private static SerializedProperty RequireField(SerializedObject serialized, string name)
        {
            var field = serialized.FindProperty(name);
            if (field == null || field.propertyType != SerializedPropertyType.ObjectReference)
                throw new InvalidOperationException("ForestAppView Sprite field is missing: " + name);
            return field;
        }

        private static Sprite RequireSprite(string path)
        {
            var sprite = AssetDatabase.LoadAssetAtPath<Sprite>(path);
            if (sprite == null) throw new InvalidOperationException("Expected an already imported Single Sprite: " + path);
            return sprite;
        }

        private static void ConfigureNewProp(string path)
        {
            AssetDatabase.ImportAsset(path, ImportAssetOptions.ForceSynchronousImport);
            var importer = AssetImporter.GetAtPath(path) as TextureImporter;
            if (importer == null) throw new InvalidOperationException("Texture importer is missing: " + path);
            var settings = new TextureImporterSettings();
            importer.ReadTextureSettings(settings);
            settings.textureType = TextureImporterType.Sprite;
            settings.spriteMode = (int)SpriteImportMode.Single;
            settings.spriteMeshType = SpriteMeshType.FullRect;
            settings.spriteAlignment = (int)SpriteAlignment.Center;
            settings.spritePivot = new Vector2(.5f, .5f);
            settings.spritePixelsPerUnit = 100;
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
            importer.maxTextureSize = 1024;
            importer.textureCompression = TextureImporterCompression.CompressedHQ;
            importer.SetPlatformTextureSettings(Platform("Android", TextureImporterFormat.ASTC_6x6));
            importer.SetPlatformTextureSettings(Platform("WebGL", TextureImporterFormat.DXT5));
            importer.SaveAndReimport();
        }

        private static TextureImporterPlatformSettings Platform(string platform, TextureImporterFormat format, int maxSize = 1024) =>
            new TextureImporterPlatformSettings {
                name = platform, overridden = true, maxTextureSize = maxSize, format = format,
                textureCompression = TextureImporterCompression.CompressedHQ, compressionQuality = 100,
                crunchedCompression = false, allowsAlphaSplitting = false
            };

        private static void ValidatePlatform(TextureImporter importer, string name, TextureImporterFormat format, int maxSize = 1024)
        {
            var platform = importer.GetPlatformTextureSettings(name);
            if (!platform.overridden || platform.maxTextureSize != maxSize || platform.format != format || platform.crunchedCompression)
                throw new InvalidOperationException("Unexpected " + name + " import settings: " + importer.assetPath);
        }

        private static void ValidateSourceAlpha(string path, int expectedSize = 0)
        {
            var texture = new Texture2D(2, 2, TextureFormat.RGBA32, false);
            try
            {
                if (!ImageConversion.LoadImage(texture, File.ReadAllBytes(path), false))
                    throw new InvalidDataException("Could not decode prop PNG: " + path);
                if (expectedSize > 0 && (texture.width != expectedSize || texture.height != expectedSize))
                    throw new InvalidDataException("Expected an exact " + expectedSize + "x" + expectedSize + " growth sheet: " + path);
                var pixels = texture.GetPixels32();
                int clear = 0, visible = 0, translucent = 0, edgePixels = 0, edgeMaxAlpha = 0;
                for (int i = 0; i < pixels.Length; i++)
                {
                    byte alpha = pixels[i].a;
                    if (alpha == 0) clear++; else visible++;
                    if (alpha > 0 && alpha < 255) translucent++;
                    int x = i % texture.width, y = i / texture.width;
                    if ((x == 0 || y == 0 || x == texture.width - 1 || y == texture.height - 1) && alpha != 0)
                    { edgePixels++; edgeMaxAlpha = Math.Max(edgeMaxAlpha, alpha); }
                }
                if (clear == 0 || visible == 0) throw new InvalidDataException("Expected visible art and a transparent background: " + path);
                Debug.Log("[ForestDailyPrototype] PNG alpha: " + Path.GetFileName(path) + " " + texture.width + "x" + texture.height
                    + ", transparent=" + clear + ", visible=" + visible + ", partial=" + translucent
                    + ", nonclear border pixels=" + edgePixels + ", border max alpha=" + edgeMaxAlpha + ".");
                if (edgePixels > 0)
                    Debug.LogWarning("[ForestDailyPrototype] Source cutout has residual border alpha; inspect its in-game edge: " + path);
            }
            finally { UnityEngine.Object.DestroyImmediate(texture); }
        }
    }
}
#endif
