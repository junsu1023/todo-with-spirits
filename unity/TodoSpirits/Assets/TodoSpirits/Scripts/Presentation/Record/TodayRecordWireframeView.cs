using System;
using System.Collections.Generic;
using System.Text;
using UnityEngine;
using UnityEngine.UI;

namespace TodoSpirits.Presentation.Record
{
    [DisallowMultipleComponent]
    public sealed class TodayRecordWireframeView : MonoBehaviour
    {
        [Header("Authored header")]
        [SerializeField]
        private Button backButton;

        [SerializeField]
        private Text titleText;

        [SerializeField]
        private Text dateText;

        [Header("Authored readable record skeleton")]
        [SerializeField]
        private ScrollRect recordScroll;

        [SerializeField]
        private Text todayIHeadingText;

        [SerializeField]
        private Text completedTasksText;

        [SerializeField]
        private Text todaySpiritHeadingText;

        [SerializeField]
        private Text taskActionLinksText;

        [SerializeField]
        private Text causalityHintText;

        private bool _initialized;

        public GameObject Root => gameObject;

        public bool Initialize(Font runtimeFont, Action goBack)
        {
            if (_initialized)
            {
                return true;
            }

            if (!ValidateReferences(out string error))
            {
                Debug.LogError($"{nameof(TodayRecordWireframeView)} prefab wiring is invalid. {error}", this);
                enabled = false;
                return false;
            }

            ApplyRuntimeFont(runtimeFont);
            backButton.onClick.AddListener(() => goBack?.Invoke());
            titleText.text = "오늘의 기록";
            todayIHeadingText.text = "오늘 나는";
            todaySpiritHeadingText.text = "오늘 정령은";
            causalityHintText.text = "↳ 내가 살아낸 하루가 정령의 하루가 됩니다.";
            _initialized = true;
            return true;
        }

        public bool ValidateReferences(out string error)
        {
            if (backButton == null || titleText == null || dateText == null)
            {
                error = "BackButton, TitleText, and DateText references are required.";
                return false;
            }

            if (recordScroll == null || recordScroll.content == null)
            {
                error = "A ScrollRect with an authored Content RectTransform is required.";
                return false;
            }

            if (todayIHeadingText == null || completedTasksText == null || todaySpiritHeadingText == null ||
                taskActionLinksText == null || causalityHintText == null)
            {
                error = "Both record headings, content Texts, and the causality hint must be authored and assigned.";
                return false;
            }

            error = string.Empty;
            return true;
        }

        public void Render(
            string date,
            IReadOnlyList<string> completedTasks,
            IReadOnlyList<string> taskActionLinks)
        {
            dateText.text = string.IsNullOrWhiteSpace(date) ? "오늘" : date;
            completedTasksText.text = FormatCompletedTasks(completedTasks);
            taskActionLinksText.text = FormatTaskActionLinks(taskActionLinks);

            Canvas.ForceUpdateCanvases();
            LayoutRebuilder.ForceRebuildLayoutImmediate(recordScroll.content);
            recordScroll.verticalNormalizedPosition = 1f;
        }

        public void ApplyRuntimeFont(Font runtimeFont)
        {
            if (runtimeFont == null)
            {
                return;
            }

            Text[] texts = GetComponentsInChildren<Text>(true);
            for (int i = 0; i < texts.Length; i++)
            {
                texts[i].font = runtimeFont;
            }
        }

        private static string FormatCompletedTasks(IReadOnlyList<string> completedTasks)
        {
            if (completedTasks == null || completedTasks.Count == 0)
            {
                return "완료한 일이 없습니다.";
            }

            var builder = new StringBuilder();
            for (int i = 0; i < completedTasks.Count; i++)
            {
                if (i > 0)
                {
                    builder.AppendLine();
                }

                builder.Append("• ").Append(completedTasks[i]);
            }

            return builder.ToString();
        }

        private static string FormatTaskActionLinks(IReadOnlyList<string> taskActionLinks)
        {
            if (taskActionLinks == null || taskActionLinks.Count == 0)
            {
                return "• 오늘의 기록을 기다리며 쉼터에서 쉬었다.";
            }

            var builder = new StringBuilder();
            for (int i = 0; i < taskActionLinks.Count; i++)
            {
                if (i > 0)
                {
                    builder.AppendLine().AppendLine();
                }

                builder.Append(taskActionLinks[i]);
            }

            return builder.ToString();
        }
    }
}
