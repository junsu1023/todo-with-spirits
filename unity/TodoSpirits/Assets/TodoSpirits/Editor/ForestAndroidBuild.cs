#if UNITY_EDITOR
using System;
using System.IO;
using System.Linq;
using UnityEditor;
using UnityEditor.Build.Reporting;
using UnityEngine;

namespace TodoSpirits.Editor
{
    public static class ForestAndroidBuild
    {
        [MenuItem("TodoSpirits/Build Forest Android Development APK")]
        public static void Build() => BuildInternal(true);

        [MenuItem("TodoSpirits/Build Forest Android Release APK")]
        public static void BuildRelease() => BuildInternal(false);

        [MenuItem("TodoSpirits/Build Forest Android ArtKit QA APK")]
        public static void BuildArtKitQa()
        {
            string directory=Path.GetFullPath("Validation/Concept01ArtKit");
            Directory.CreateDirectory(directory);
            BuildInternal(false,Path.Combine(directory,"artkit-qa-20260918.apk"),Path.Combine(directory,"android-qa-build-report.json"));
        }

        private static void BuildInternal(bool development,string apkOverride=null,string reportOverride=null)
        {
            if(EditorApplication.isPlaying)throw new InvalidOperationException("Play 모드를 종료해 주세요.");
            if(!development && (PlayerSettings.Android.targetArchitectures & AndroidArchitecture.ARM64)==0)
                throw new InvalidOperationException("릴리즈 검증 APK에는 기존 Android 빌드 설정의 ARM64 대상이 필요합니다.");
            string directory=Path.GetFullPath("Builds/Forest");Directory.CreateDirectory(directory);
            string reportPath=reportOverride??Path.Combine(directory,development?"android-build-report.json":"android-release-build-report.json");
            string apkPath=apkOverride??Path.Combine(directory,development?"TodoSpirits-Forest.apk":"TodoSpirits-Forest-release.apk");
            File.WriteAllText(reportPath,"{\"status\":\"building\"}");
            bool export=EditorUserBuildSettings.exportAsGoogleAndroidProject;
            bool bundle=EditorUserBuildSettings.buildAppBundle;
            try {
                EditorUserBuildSettings.exportAsGoogleAndroidProject=false;EditorUserBuildSettings.buildAppBundle=false;
                var report=BuildPipeline.BuildPlayer(new BuildPlayerOptions {
                    scenes=new[]{"Assets/TodoSpirits/Scenes/00_Boot.unity","Assets/TodoSpirits/Scenes/01_Main.unity"},
                    locationPathName=apkPath,target=BuildTarget.Android,options=development?BuildOptions.Development:BuildOptions.None
                });
                File.WriteAllText(reportPath,JsonUtility.ToJson(new Result {
                    development=development,apkPath=apkPath,architectures=PlayerSettings.Android.targetArchitectures.ToString(),
                    status=report.summary.result.ToString(),errors=report.summary.totalErrors,warnings=report.summary.totalWarnings,
                    bytes=report.summary.totalSize,seconds=report.summary.totalTime.TotalSeconds,
                    messages=report.steps.SelectMany(s=>s.messages).Where(m=>m.type==LogType.Error||m.type==LogType.Exception).Select(m=>m.content).ToArray()
                },true));
            }catch(Exception error){File.WriteAllText(reportPath,JsonUtility.ToJson(new Result{development=development,apkPath=apkPath,architectures=PlayerSettings.Android.targetArchitectures.ToString(),status="Exception",messages=new[]{error.Message}},true));throw;}
            finally{EditorUserBuildSettings.exportAsGoogleAndroidProject=export;EditorUserBuildSettings.buildAppBundle=bundle;AssetDatabase.SaveAssets();}
        }
        [Serializable] private sealed class Result
        {
            public bool development;public string apkPath,architectures;
            public string status;public int errors,warnings;public ulong bytes;public double seconds;public string[] messages;
        }
    }
}
#endif
