#if UNITY_EDITOR
using System;
using System.IO;
using System.Text;
using UnityEditor;
using UnityEditor.Build;
using UnityEditor.Build.Reporting;

namespace TodoSpirits.Editor
{
    /// <summary>Persists the JSON save files written by Web players across page reloads.</summary>
    public sealed class ForestWebPersistenceBuild : IPostprocessBuildWithReport
    {
        private const string DisabledSetting = "// config.autoSyncPersistentDataPath = true;";
        private const string EnabledSetting = "config.autoSyncPersistentDataPath = true;";

        public int callbackOrder => 100;

        public void OnPostprocessBuild(BuildReport report)
        {
            if (report.summary.platform == BuildTarget.WebGL)
                ConfigureOutput(report.summary.outputPath);
        }

        public static void ConfigureOutput(string outputPath)
        {
            var path = Path.Combine(outputPath, "index.html");
            var html = File.ReadAllText(path);
            if (html.Contains(DisabledSetting))
            {
                File.WriteAllText(path, html.Replace(DisabledSetting, EnabledSetting), new UTF8Encoding(false));
                return;
            }

            // A custom template must explicitly enable the same setting. Do not ship a
            // successful build whose file saves would disappear on the next visit.
            foreach (var line in html.Split('\n'))
                if (string.Equals(line.Trim(), EnabledSetting, StringComparison.Ordinal)) return;
            throw new BuildFailedException("Web template must enable config.autoSyncPersistentDataPath = true before createUnityInstance().");
        }
    }
}
#endif
