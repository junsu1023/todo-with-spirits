#if UNITY_EDITOR
using System;
using System.IO;
using System.Linq;
using UnityEditor;
using UnityEditor.Build.Reporting;
using UnityEngine;

namespace TodoSpirits.Editor
{
    /// <summary>Exports the same production scenes consumed by the native TODO Android host.</summary>
    public static class ForestAndroidLibraryBuild
    {
        public const string Menu = "TodoSpirits/Build Forest Android Library";
        public const string OutputDirectory = "Exports/AndroidLibrary";
        public const string ReportPath = "Validation/ForestPrototype/android-library-build.json";

        [MenuItem(Menu)]
        public static void Build()
        {
            if (EditorApplication.isPlayingOrWillChangePlaymode)
                throw new InvalidOperationException("Play 모드를 종료한 뒤 Android 호스트용 라이브러리를 내보내 주세요.");
            if (EditorUserBuildSettings.activeBuildTarget != BuildTarget.Android)
                throw new InvalidOperationException("Android를 활성 빌드 대상으로 선택해 주세요.");
            if (!BuildPipeline.IsBuildTargetSupported(BuildTargetGroup.Android, BuildTarget.Android))
                throw new InvalidOperationException("Android Build Support가 필요합니다.");
            if (!PlayerSettings.Android.applicationEntry.ToString().Contains("GameActivity"))
                throw new InvalidOperationException("Android 호스트는 현재 GameActivity 진입점을 사용합니다.");

            string[] scenes = { "Assets/TodoSpirits/Scenes/00_Boot.unity", "Assets/TodoSpirits/Scenes/01_Main.unity" };
            if (scenes.Any(path => AssetDatabase.LoadAssetAtPath<SceneAsset>(path) == null))
                throw new InvalidOperationException("프로덕션 부트/메인 씬을 확인해 주세요.");
            Directory.CreateDirectory(Path.GetDirectoryName(ReportPath));
            var result = new LibraryBuildResult { status = "building", outputPath = Path.GetFullPath(OutputDirectory), startedAtUtc = DateTime.UtcNow.ToString("O") };
            File.WriteAllText(ReportPath, JsonUtility.ToJson(result, true));
            bool previousExport = EditorUserBuildSettings.exportAsGoogleAndroidProject;
            bool previousBundle = EditorUserBuildSettings.buildAppBundle;
            try
            {
                EditorUserBuildSettings.exportAsGoogleAndroidProject = true;
                EditorUserBuildSettings.buildAppBundle = false;
                BuildReport report = BuildPipeline.BuildPlayer(new BuildPlayerOptions
                {
                    scenes = scenes,
                    locationPathName = result.outputPath,
                    target = BuildTarget.Android,
                    options = BuildOptions.DetailedBuildReport
                });
                result.status = report.summary.result.ToString();
                result.errors = report.summary.totalErrors;
                result.warnings = report.summary.totalWarnings;
                result.seconds = report.summary.totalTime.TotalSeconds;
                result.messages = report.steps.SelectMany(step => step.messages)
                    .Where(message => message.type == LogType.Error || message.type == LogType.Exception)
                    .Select(message => message.content).ToArray();
                if (report.summary.result != BuildResult.Succeeded)
                    throw new InvalidOperationException("Android 라이브러리 내보내기에 실패했습니다. 검증 보고서를 확인해 주세요.");
                Debug.Log("Android 숲 라이브러리 내보내기 완료: " + result.outputPath);
            }
            catch (Exception error)
            {
                if (result.status == "building") result.status = "Exception";
                result.messages = (result.messages ?? Array.Empty<string>()).Concat(new[] { error.Message }).ToArray();
                throw;
            }
            finally
            {
                EditorUserBuildSettings.exportAsGoogleAndroidProject = previousExport;
                EditorUserBuildSettings.buildAppBundle = previousBundle;
                result.finishedAtUtc = DateTime.UtcNow.ToString("O");
                File.WriteAllText(ReportPath, JsonUtility.ToJson(result, true));
            }
        }

        [Serializable]
        private sealed class LibraryBuildResult
        {
            public string status, outputPath, startedAtUtc, finishedAtUtc;
            public int errors, warnings;
            public double seconds;
            public string[] messages;
        }
    }
}
#endif
