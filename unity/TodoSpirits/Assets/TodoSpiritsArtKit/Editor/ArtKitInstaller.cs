using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using UnityEditor;
using UnityEditor.Events;
using UnityEditor.U2D;
using UnityEngine;
using UnityEngine.UI;

namespace TodoSpirits.ArtKit.Editor
{
    // Explicit menu commands only. No InitializeOnLoad, project setting changes or scene replacement.
    public static class ArtKitInstaller
    {
        private const string Root = "Assets/TodoSpiritsArtKit";
        private const string Generated = Root + "/Generated";
        [Serializable] private sealed class Manifest { public int schemaVersion; public Item[] sprites; public Clip[] clips; }
        [Serializable] private sealed class Item
        {
            public string id, path; public int width, height; public float[] border, pivot; public float pixelsPerUnit;
        }
        [Serializable] private sealed class Clip { public string name; public float fps; public bool loop; public string[] frames; }

        [MenuItem("TodoSpirits/Art Kit/1 - Import and Build Resources")]
        public static void Install()
        {
            if (EditorApplication.isPlayingOrWillChangePlaymode)
                throw new InvalidOperationException("Stop Play Mode before importing the art kit.");
            var manifest = LoadManifest();
            EnsureFolder(Generated); EnsureFolder(Generated + "/Atlases");
            EnsureFolder(Generated + "/Animations"); EnsureFolder(Generated + "/Prefabs");
            try
            {
                for (int i = 0; i < manifest.sprites.Length; ++i)
                {
                    var item = manifest.sprites[i];
                    EditorUtility.DisplayProgressBar("TodoSpirits Art Kit", item.id, (float)i / manifest.sprites.Length);
                    ConfigureTexture(item);
                }
                var catalog = BuildCatalog(manifest);
                BuildAtlas("UI_Core", new[] { Root + "/Sprites/UI_Core", Root + "/Sprites/Decor" });
                BuildAtlas("Spirit", new[] { Root + "/Sprites/Spirit" });
                BuildAtlas("FX", new[] { Root + "/Sprites/FX" });
                BuildAtlas("Props", new[] { Root + "/Sprites/Props" });
                BuildClips(catalog);
                BuildPrefabs(catalog);
                AssetDatabase.SaveAssets();
                Validate();
                Selection.activeObject = catalog;
                Debug.Log("ArtKit import/build finished. Existing kit prefabs/atlases/clips were preserved. This does NOT certify Android/WebGL performance or replace game scenes.");
            }
            finally { EditorUtility.ClearProgressBar(); }
        }
        private static Manifest LoadManifest()
        {
            var text = AssetDatabase.LoadAssetAtPath<TextAsset>(Root + "/Data/artkit_manifest.json");
            if (!text) throw new FileNotFoundException("Copy Assets/TodoSpiritsArtKit to this project's Assets folder first.");
            var data = JsonUtility.FromJson<Manifest>(text.text);
            if (data == null || data.schemaVersion != 1 || data.sprites == null || data.clips == null)
                throw new InvalidDataException("Unsupported or incomplete art-kit manifest.");
            return data;
        }
        private static void ConfigureTexture(Item item)
        {
            if (string.IsNullOrEmpty(item.path) || !item.path.StartsWith(Root + "/", StringComparison.Ordinal))
                throw new InvalidDataException("Out-of-scope texture path in manifest.");
            var importer = AssetImporter.GetAtPath(item.path) as TextureImporter;
            if (!importer) throw new FileNotFoundException(item.path);
            importer.textureType = TextureImporterType.Sprite;
            importer.spriteImportMode = SpriteImportMode.Single;
            importer.spritePixelsPerUnit = item.pixelsPerUnit;
            importer.spritePivot = new Vector2(item.pivot[0], item.pivot[1]);
            importer.spriteBorder = new Vector4(item.border[0], item.border[1], item.border[2], item.border[3]);
            importer.alphaIsTransparency = true; importer.sRGBTexture = true;
            importer.isReadable = false; importer.mipmapEnabled = false;
            importer.wrapMode = TextureWrapMode.Clamp; importer.filterMode = FilterMode.Bilinear;
            importer.npotScale = TextureImporterNPOTScale.None; importer.maxTextureSize = 2048;
            importer.textureCompression = TextureImporterCompression.Uncompressed;
            var settings = new TextureImporterSettings(); importer.ReadTextureSettings(settings);
            settings.spriteMeshType = SpriteMeshType.FullRect;
            settings.spriteAlignment = (int)SpriteAlignment.Custom;
            settings.spriteGenerateFallbackPhysicsShape = false;
            importer.SetTextureSettings(settings);
            // Compatibility baseline, not final memory optimization. No forced ASTC for mobile web.
            importer.SetPlatformTextureSettings(Platform("Android", TextureImporterFormat.ETC2_RGBA8));
            importer.SetPlatformTextureSettings(Platform("WebGL", TextureImporterFormat.RGBA32));
            importer.userData = "TodoSpiritsArtKit/1.0.0";
            importer.SaveAndReimport();
        }
        private static TextureImporterPlatformSettings Platform(string name, TextureImporterFormat format)
        {
            return new TextureImporterPlatformSettings
            {
                name = name, overridden = true, maxTextureSize = 2048, format = format,
                compressionQuality = 80, crunchedCompression = false, allowsAlphaSplitting = false
            };
        }
        private static ArtKitCatalog BuildCatalog(Manifest manifest)
        {
            string path = Generated + "/ArtKitCatalog.asset";
            var catalog = AssetDatabase.LoadAssetAtPath<ArtKitCatalog>(path);
            if (!catalog)
            {
                catalog = ScriptableObject.CreateInstance<ArtKitCatalog>();
                AssetDatabase.CreateAsset(catalog, path);
            }
            var lookup = new Dictionary<string, Sprite>(StringComparer.Ordinal);
            foreach (var item in manifest.sprites)
            {
                var sprite = AssetDatabase.LoadAssetAtPath<Sprite>(item.path);
                if (!sprite) throw new InvalidDataException("Sprite import failed: " + item.path);
                lookup.Add(item.id, sprite);
            }
            catalog.sprites = lookup.Select(p => new ArtKitCatalog.Entry { id = p.Key, sprite = p.Value }).ToArray();
            catalog.sequences = manifest.clips.Select(c => new ArtKitCatalog.Sequence
            {
                id = c.name, fps = c.fps, loop = c.loop, frames = c.frames.Select(id => lookup[id]).ToArray()
            }).ToArray();
            catalog.InvalidateLookup();
            EditorUtility.SetDirty(catalog);
            return catalog;
        }
        private static void BuildAtlas(string name, string[] folders)
        {
            string path = Generated + "/Atlases/SA_" + name + ".spriteatlasv2";
            // Do not replace a user's tuned atlas on rerun.
            if (File.Exists(path)) { Debug.Log("Preserved existing atlas: " + path); return; }
            var inputs = folders.Select(AssetDatabase.LoadAssetAtPath<DefaultAsset>).Cast<UnityEngine.Object>().ToArray();
            if (inputs.Any(x => !x)) throw new InvalidOperationException("Atlas folder is missing.");
            var source = new SpriteAtlasAsset(); source.Add(inputs);
            SpriteAtlasAsset.Save(source, path);
            AssetDatabase.ImportAsset(path, ImportAssetOptions.ForceSynchronousImport);
            var importer = AssetImporter.GetAtPath(path) as SpriteAtlasImporter;
            if (!importer) throw new InvalidOperationException("SpriteAtlas V2 importer not available. Check the Unity version/project atlas mode; do not change global settings silently.");
            var packing = importer.packingSettings;
            packing.enableRotation = false; packing.enableTightPacking = false; packing.padding = 8;
            importer.packingSettings = packing;
            var texture = importer.textureSettings;
            texture.filterMode = FilterMode.Bilinear; texture.readable = false;
            texture.generateMipMaps = false; texture.sRGB = true;
            importer.textureSettings = texture;
            importer.includeInBuild = true;
            importer.SetPlatformSettings(Platform("Android", TextureImporterFormat.ETC2_RGBA8));
            importer.SetPlatformSettings(Platform("WebGL", TextureImporterFormat.RGBA32));
            importer.SaveAndReimport();
        }
        private static void BuildClips(ArtKitCatalog catalog)
        {
            foreach (var sequence in catalog.sequences)
            {
                string path = Generated + "/Animations/" + sequence.id + "_Image.anim";
                if (File.Exists(path)) continue;
                var clip = new AnimationClip { name = sequence.id + "_Image", frameRate = sequence.fps };
                var keys = new ObjectReferenceKeyframe[sequence.frames.Length + 1];
                for (int i = 0; i < sequence.frames.Length; i++)
                    keys[i] = new ObjectReferenceKeyframe { time = i / sequence.fps, value = sequence.frames[i] };
                keys[keys.Length - 1] = new ObjectReferenceKeyframe
                { time = sequence.frames.Length / sequence.fps, value = sequence.frames[sequence.frames.Length - 1] };
                AnimationUtility.SetObjectReferenceCurve(clip,
                    new EditorCurveBinding { path = "", type = typeof(Image), propertyName = "m_Sprite" }, keys);
                var settings = AnimationUtility.GetAnimationClipSettings(clip);
                settings.loopTime = sequence.loop; AnimationUtility.SetAnimationClipSettings(clip, settings);
                AssetDatabase.CreateAsset(clip, path);
            }
        }
        private static RectTransform Rect(string name, Transform parent, Vector2 size)
        {
            var rect = (RectTransform)new GameObject(name, typeof(RectTransform)).transform;
            if (parent) rect.SetParent(parent, false);
            rect.sizeDelta = size; return rect;
        }
        private static void Stretch(RectTransform rect, float inset = 0)
        {
            rect.anchorMin = Vector2.zero; rect.anchorMax = Vector2.one;
            rect.offsetMin = new Vector2(inset, inset); rect.offsetMax = new Vector2(-inset, -inset);
        }
        private static Image Picture(string name, Transform parent, Sprite sprite, Vector2 size, bool sliced = false)
        {
            var rect = Rect(name, parent, size); var image = rect.gameObject.AddComponent<Image>();
            image.sprite = sprite; image.raycastTarget = false;
            image.type = sliced ? Image.Type.Sliced : Image.Type.Simple;
            image.preserveAspect = !sliced; return image;
        }
        private static GameObject ButtonObject(ArtKitCatalog c, string theme, string iconId = null, bool tab = false)
        {
            Vector2 size = iconId == null ? new Vector2(580, 144) : tab ? new Vector2(144, 186) : new Vector2(144, 144);
            var root = Rect("Button", null, size);
            var hit = root.gameObject.AddComponent<Image>(); hit.sprite = c.GetSprite("white_pixel"); hit.color = Color.clear;
            var button = root.gameObject.AddComponent<Button>(); button.targetGraphic = hit; button.transition = Selectable.Transition.None;
            var visual = Rect("Visual", root, size); Stretch(visual, iconId == null ? 0 : 12);
            string prefix = iconId == null ? "btn_" + theme : tab ? "btn_paper" : "circle_paper";
            var surface = Picture("Surface", visual, c.GetSprite(prefix + "_normal"), size, true); Stretch(surface.rectTransform);
            var motion = root.gameObject.AddComponent<ArtKitButtonMotion>();
            motion.owner = button; motion.visual = visual; motion.surface = surface;
            motion.normal = c.GetSprite(prefix + "_normal"); motion.highlighted = c.GetSprite(prefix + "_highlighted");
            motion.pressed = c.GetSprite(prefix + "_pressed"); motion.disabled = c.GetSprite(prefix + "_disabled");
            var focus = Picture("KeyboardFocus", visual, c.GetSprite("focus_ring"), size, true); Stretch(focus.rectTransform);
            focus.enabled = false; motion.keyboardFocusRing = focus;
            if (iconId != null)
            {
                var icon = Picture("Icon", visual, c.GetSprite(iconId), new Vector2(68, 68));
                if (tab) icon.rectTransform.anchoredPosition = Vector2.up * 23;
            }
            // Use the project's licensed Korean TMP font. The kit deliberately contains no font files.
            var label = Rect("Label_TMP_required", visual, Vector2.zero); Stretch(label, 20);
            return root.gameObject;
        }
        private static void SavePrefab(GameObject obj, string name)
        {
            string path = Generated + "/Prefabs/" + name + ".prefab";
            try { if (!File.Exists(path)) PrefabUtility.SaveAsPrefabAsset(obj, path); }
            finally { UnityEngine.Object.DestroyImmediate(obj); }
        }
        private static void BuildPrefabs(ArtKitCatalog c)
        {
            SavePrefab(ButtonObject(c, "primary"), "PF_Button_Primary");
            SavePrefab(ButtonObject(c, "secondary"), "PF_Button_Secondary");
            SavePrefab(ButtonObject(c, "paper", "ic_settings"), "PF_Button_Settings");
            SavePrefab(ButtonObject(c, "paper", "ic_album", true), "PF_Album_SideTab");
            var spiritRoot = Rect("WindSpirit", null, new Vector2(512, 560));
            var shadow = Picture("Shadow", spiritRoot, c.GetSprite("shadow_soft"), new Vector2(270, 100));
            shadow.rectTransform.anchoredPosition = new Vector2(0, -212);
            var body = Picture("Visual", spiritRoot, c.GetSprite("spirit_neutral"), new Vector2(512, 512));
            body.raycastTarget = true;
            var spirit = spiritRoot.gameObject.AddComponent<ArtKitSpiritMotion>();
            spirit.visual = body.rectTransform; spirit.body = body; spirit.shadow = shadow.rectTransform;
            spirit.neutral = c.GetSprite("spirit_neutral"); spirit.halfClosed = c.GetSprite("spirit_half");
            spirit.closed = c.GetSprite("spirit_closed"); spirit.happy = c.GetSprite("spirit_happy"); spirit.sleep = c.GetSprite("spirit_sleep");
            SavePrefab(spiritRoot.gameObject, "PF_WindSpirit");
            var toggleRoot = Rect("Toggle", null, new Vector2(190, 144));
            var hit = toggleRoot.gameObject.AddComponent<Image>(); hit.sprite = c.GetSprite("white_pixel"); hit.color = Color.clear;
            var toggle = toggleRoot.gameObject.AddComponent<Toggle>(); toggle.targetGraphic = hit; toggle.isOn = true; toggle.transition = Selectable.Transition.None;
            var track = Picture("Track", toggleRoot, c.GetSprite("toggle_track_on"), new Vector2(128, 64), true);
            var thumb = Picture("Thumb", toggleRoot, c.GetSprite("thumb_cream_normal"), new Vector2(56, 56));
            var tm = toggleRoot.gameObject.AddComponent<ArtKitToggleMotion>(); tm.owner = toggle; tm.track = track; tm.thumb = thumb.rectTransform;
            tm.onTrack = c.GetSprite("toggle_track_on"); tm.offTrack = c.GetSprite("toggle_track_off");
            SavePrefab(toggleRoot.gameObject, "PF_Toggle");
            var modalRoot = Rect("ModalShell", null, new Vector2(1080, 2340)); Stretch(modalRoot);
            var group = modalRoot.gameObject.AddComponent<CanvasGroup>();
            var scrim = Picture("Scrim", modalRoot, c.GetSprite("white_pixel"), Vector2.zero); Stretch(scrim.rectTransform);
            scrim.color = new Color(.09f, .12f, .09f, .52f); scrim.raycastTarget = true;
            var panel = Rect("Visual", modalRoot, Vector2.zero); panel.anchorMin = new Vector2(.05f, .13f); panel.anchorMax = new Vector2(.95f, .87f);
            panel.offsetMin = panel.offsetMax = Vector2.zero;
            var wood = Picture("Wood", panel, c.GetSprite("panel_wood"), Vector2.zero, true); Stretch(wood.rectTransform);
            var paper = Picture("Paper", panel, c.GetSprite("panel_paper"), Vector2.zero, true); Stretch(paper.rectTransform, 14);
            var content = Rect("ScrollContent_required", panel, Vector2.zero); Stretch(content, 64);
            content.offsetMax = new Vector2(-64, -180);
            var modal = modalRoot.gameObject.AddComponent<ArtKitModalMotion>(); modal.group = group; modal.visual = panel;
            var close = ButtonObject(c, "paper", "ic_close"); close.name = "Close"; close.transform.SetParent(panel, false);
            var closeRect = (RectTransform)close.transform; closeRect.anchorMin = closeRect.anchorMax = Vector2.one;
            closeRect.anchoredPosition = new Vector2(-94, -94);
            UnityEventTools.AddPersistentListener(close.GetComponent<Button>().onClick, modal.Close);
            SavePrefab(modalRoot.gameObject, "PF_Modal_Shell");
            foreach (var sequence in c.sequences.Where(s => s.id != "blink" && s.id != "talk"))
            {
                var image = Picture("FX_" + sequence.id, null, sequence.frames[0], sequence.frames[0].rect.size);
                var fx = image.gameObject.AddComponent<ArtKitFlipbook>(); fx.target = image; fx.Configure(sequence);
                SavePrefab(image.gameObject, "PF_FX_" + sequence.id);
            }
        }
        [MenuItem("TodoSpirits/Art Kit/2 - Validate Imported Resources")]
        public static void Validate()
        {
            var manifest = LoadManifest(); int errors = 0;
            var ids = new HashSet<string>();
            foreach (var item in manifest.sprites)
            {
                var sprite = AssetDatabase.LoadAssetAtPath<Sprite>(item.path);
                var importer = AssetImporter.GetAtPath(item.path) as TextureImporter;
                bool borderOk = sprite && Vector4.Distance(sprite.border, new Vector4(item.border[0], item.border[1], item.border[2], item.border[3])) < .01f;
                bool sizeOk = sprite && Mathf.Abs(sprite.rect.width - item.width) < .1f && Mathf.Abs(sprite.rect.height - item.height) < .1f;
                bool pivotOk = sprite && Vector2.Distance(sprite.pivot, new Vector2(item.pivot[0] * item.width, item.pivot[1] * item.height)) < .1f;
                if (!ids.Add(item.id) || !sprite || !importer || !borderOk || !sizeOk || !pivotOk || importer.isReadable || importer.mipmapEnabled)
                { Debug.LogError("ArtKit validation failed: " + item.id); errors++; }
            }
            foreach (string name in new[] { "UI_Core", "Spirit", "FX", "Props" })
            {
                string path = Generated + "/Atlases/SA_" + name + ".spriteatlasv2";
                var importer = AssetImporter.GetAtPath(path) as SpriteAtlasImporter;
                if (!importer || importer.packingSettings.enableRotation || importer.packingSettings.enableTightPacking || importer.packingSettings.padding < 8)
                { Debug.LogError("ArtKit atlas validation failed: " + path); errors++; }
            }
            string report = "{\"stage\":\"Unity imported resource validation\",\"errors\":" + errors + ",\"sprites\":" + manifest.sprites.Length + ",\"deviceValidated\":false}";
            Directory.CreateDirectory("Temp"); File.WriteAllText("Temp/TodoSpiritsArtKit-validation.json", report);
            if (errors > 0) throw new InvalidOperationException("ArtKit resource validation failed; check Console.");
            Debug.Log("ArtKit resource metadata passed. Layout, input, app integration, GPU memory and release builds still require their own tests.");
        }
        private static void EnsureFolder(string path)
        {
            if (AssetDatabase.IsValidFolder(path)) return;
            string parent = Path.GetDirectoryName(path).Replace('\\', '/');
            EnsureFolder(parent); AssetDatabase.CreateFolder(parent, Path.GetFileName(path));
        }
    }
}
