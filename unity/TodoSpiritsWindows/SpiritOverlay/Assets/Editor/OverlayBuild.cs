#if UNITY_EDITOR
using System;
using System.IO;
using TodoSpirits.Spirit;
using Kirurobo;
using UnityEditor;
using UnityEditor.Build;
using UnityEditor.Build.Reporting;
using UnityEditor.SceneManagement;
using UnityEngine;
using UnityEngine.Rendering;
using UnityEngine.Rendering.Universal;

namespace TodoSpirits.SpiritEditor
{
    public static class OverlayBuild
    {
        const string Scene = "Assets/Scenes/SpiritOverlay.unity";
        [MenuItem("TodoSpirits/Configure Spirit Overlay")]
        public static void Configure()
        {
            PlayerSettings.companyName = "TodoWithSpirits";
            PlayerSettings.productName = "TodoSpirit";
            PlayerSettings.runInBackground = true;
            PlayerSettings.fullScreenMode = FullScreenMode.Windowed;
            PlayerSettings.resizableWindow = false;
            PlayerSettings.defaultScreenWidth = 1280;
            PlayerSettings.defaultScreenHeight = 720;
            PlayerSettings.SplashScreen.show = false;
            PlayerSettings.visibleInBackground = true;
            PlayerSettings.preserveFramebufferAlpha = true;
            PlayerSettings.SetUseDefaultGraphicsAPIs(BuildTarget.StandaloneWindows64, false);
            PlayerSettings.SetGraphicsAPIs(BuildTarget.StandaloneWindows64, new[] { GraphicsDeviceType.Direct3D11 });
            PlayerSettings.SetScriptingBackend(NamedBuildTarget.Standalone, ScriptingImplementation.Mono2x);
            var settings = new SerializedObject(AssetDatabase.LoadAllAssetsAtPath("ProjectSettings/ProjectSettings.asset")[0]);
            Require(settings, "useFlipModelSwapchain").boolValue = false;
            settings.ApplyModifiedPropertiesWithoutUndo();

            var pipeline = AssetDatabase.LoadAssetAtPath<UniversalRenderPipelineAsset>("Assets/Settings/UniversalRP.asset");
            if (!pipeline) throw new InvalidOperationException("URP 2D pipeline missing.");
            pipeline.supportsHDR = false;
            var urp = new SerializedObject(pipeline);
            Require(urp, "m_AllowPostProcessAlphaOutput").boolValue = true;
            urp.ApplyModifiedPropertiesWithoutUndo();
            EditorUtility.SetDirty(pipeline);
            GraphicsSettings.defaultRenderPipeline = pipeline;
            for (int i = 0; i < QualitySettings.names.Length; i++)
            {
                QualitySettings.SetQualityLevel(i, false);
                QualitySettings.renderPipeline = pipeline;
            }
            var importer = (TextureImporter)AssetImporter.GetAtPath("Assets/Art/spirit_neutral.png");
            importer.isReadable = true;
            importer.textureCompression = TextureImporterCompression.Uncompressed;
            var textureSettings = new TextureImporterSettings();
            importer.ReadTextureSettings(textureSettings);
            textureSettings.spriteMeshType = SpriteMeshType.FullRect;
            importer.SetTextureSettings(textureSettings);
            importer.SaveAndReimport();

            var scene = EditorSceneManager.NewScene(NewSceneSetup.EmptyScene, NewSceneMode.Single);
            var camera = new GameObject("Main Camera") { tag = "MainCamera" };
            var cam = camera.AddComponent<Camera>();
            cam.orthographic = true;
            cam.orthographicSize = 360;
            cam.clearFlags = CameraClearFlags.SolidColor;
            cam.backgroundColor = Color.clear;
            cam.allowHDR = false;
            camera.transform.position = new Vector3(0, 0, -10);
            var data = camera.AddComponent<UniversalAdditionalCameraData>();
            data.renderPostProcessing = false;
            var body = new GameObject("Spirit").AddComponent<SpriteRenderer>();
            body.sprite = AssetDatabase.LoadAssetAtPath<Sprite>("Assets/Art/spirit_neutral.png");
            var window = new GameObject("Native Window").AddComponent<UniWindowController>();
            window.currentCamera = cam;
            window.isHitTestEnabled = false;
            window.hitTestType = UniWindowController.HitTestType.None;
            window.isTransparent = true;
            window.isTopmost = true;
            window.isClickThrough = true;
            var overlay = new GameObject("Overlay").AddComponent<SpiritOverlay>();
            overlay.Camera = cam;
            overlay.Body = body;
            overlay.Window = window;
            Directory.CreateDirectory("Assets/Scenes");
            EditorSceneManager.SaveScene(scene, Scene);
            EditorBuildSettings.scenes = new[] { new EditorBuildSettingsScene(Scene, true) };
            AssetDatabase.SaveAssets();
            Debug.Log("[Spirit] D3D11, flip model off, HDR off, alpha processing on; overlay scene saved.");
        }

        static SerializedProperty Require(SerializedObject obj, string name) => obj.FindProperty(name) ?? throw new InvalidOperationException("Required Unity setting missing: " + name);

        [MenuItem("TodoSpirits/Build Spirit Overlay")]
        public static void BuildWindows()
        {
            Configure();
            string output = Path.GetFullPath("../spirit-player/TodoSpirit.exe"); // unity/TodoSpiritsWindows/spirit-player (git 에 올리지 않음)
            Directory.CreateDirectory(Path.GetDirectoryName(output));
            var report = BuildPipeline.BuildPlayer(new BuildPlayerOptions
            {
                scenes = new[] { Scene }, locationPathName = output, target = BuildTarget.StandaloneWindows64, options = BuildOptions.None
            });
            Debug.Log($"[Spirit] build={report.summary.result}, bytes={report.summary.totalSize}, seconds={report.summary.totalTime.TotalSeconds:F1}");
            if (report.summary.result != BuildResult.Succeeded) throw new InvalidOperationException("Windows overlay build failed.");
        }
    }
}
#endif
