using System;
using System.Collections.Generic;

namespace TodoSpirits.Core
{
    public sealed class TaskClassifier
    {
        private sealed class KeywordRule
        {
            public readonly UserTaskCategory Category;
            public readonly SpiritActionId Action;
            public readonly string Subcategory;
            public readonly string[] Keywords;

            public KeywordRule(
                UserTaskCategory category,
                SpiritActionId action,
                string subcategory,
                params string[] keywords)
            {
                Category = category;
                Action = action;
                Subcategory = subcategory;
                Keywords = keywords;
            }
        }

        private static readonly KeywordRule[] KeywordRules =
        {
            new KeywordRule(
                UserTaskCategory.WorkStudy,
                SpiritActionId.CraftRepair,
                "업무·제작",
                "기획", "수정", "보고", "과제", "업무", "프로젝트", "작성"),
            new KeywordRule(
                UserTaskCategory.WorkStudy,
                SpiritActionId.ReadRecords,
                "학습·기록",
                "공부", "학습", "시험", "독서", "읽기", "책", "정리"),
            new KeywordRule(
                UserTaskCategory.Health,
                SpiritActionId.WalkForest,
                "움직임·산책",
                "산책", "걷기", "걸음", "운동", "러닝", "달리기", "요가"),
            new KeywordRule(
                UserTaskCategory.Health,
                SpiritActionId.Rest,
                "회복·휴식",
                "수면", "낮잠", "휴식", "회복", "병원"),
            new KeywordRule(
                UserTaskCategory.Relationships,
                SpiritActionId.SocialTea,
                "만남·대화",
                "친구", "저녁", "약속", "만남", "가족", "대화", "통화")
        };

        public TaskClassification Classify(CompletedTask task)
        {
            if (task == null)
            {
                throw new ArgumentNullException(nameof(task));
            }

            KeywordRule matchedRule;
            if (IsPrototypeCategory(task.UserCategory))
            {
                matchedRule = FindKeywordRule(task.Title, task.UserCategory, true);
                if (matchedRule != null)
                {
                    return CreateClassification(
                        task,
                        matchedRule,
                        0.95f,
                        "사용자 지정 카테고리를 우선하고 제목 키워드 '" +
                        FindMatchedKeyword(task.Title, matchedRule) + "'로 세부 행동을 골랐습니다.");
                }

                return CreateCategoryDefault(task);
            }

            matchedRule = FindKeywordRule(task.Title, task.UserCategory, false);
            if (matchedRule != null)
            {
                return CreateClassification(
                    task,
                    matchedRule,
                    0.72f,
                    "지원 카테고리가 없어 제목 키워드 '" +
                    FindMatchedKeyword(task.Title, matchedRule) + "'로 행동을 추론했습니다.");
            }

            return new TaskClassification(
                task.TaskId,
                task.UserCategory,
                "기본 휴식",
                0.35f,
                new[] { SpiritActionId.Rest },
                "지원되는 명시 카테고리와 키워드가 없어 안전한 기본 행동인 휴식을 사용했습니다.");
        }

        public List<TaskClassification> Classify(IEnumerable<CompletedTask> tasks)
        {
            var results = new List<TaskClassification>();
            if (tasks == null)
            {
                return results;
            }

            foreach (var task in tasks)
            {
                if (task != null)
                {
                    results.Add(Classify(task));
                }
            }

            return results;
        }

        public List<TaskClassification> ClassifyAll(IEnumerable<CompletedTask> tasks)
        {
            return Classify(tasks);
        }

        private static TaskClassification CreateCategoryDefault(CompletedTask task)
        {
            SpiritActionId action;
            string subcategory;

            switch (task.UserCategory)
            {
                case UserTaskCategory.WorkStudy:
                    action = SpiritActionId.CraftRepair;
                    subcategory = "업무·학업 기본";
                    break;
                case UserTaskCategory.Health:
                    action = SpiritActionId.WalkForest;
                    subcategory = "건강 기본";
                    break;
                case UserTaskCategory.Relationships:
                    action = SpiritActionId.SocialTea;
                    subcategory = "관계 기본";
                    break;
                default:
                    action = SpiritActionId.Rest;
                    subcategory = "기본 휴식";
                    break;
            }

            return new TaskClassification(
                task.TaskId,
                task.UserCategory,
                subcategory,
                0.82f,
                new[] { action },
                "사용자 지정 카테고리를 우선했으며 일치 키워드가 없어 해당 카테고리의 기본 행동을 사용했습니다.");
        }

        private static TaskClassification CreateClassification(
            CompletedTask task,
            KeywordRule rule,
            float confidence,
            string reason)
        {
            return new TaskClassification(
                task.TaskId,
                task.UserCategory,
                rule.Subcategory,
                confidence,
                new[] { rule.Action },
                reason);
        }

        private static KeywordRule FindKeywordRule(
            string title,
            UserTaskCategory category,
            bool restrictToCategory)
        {
            for (var i = 0; i < KeywordRules.Length; i++)
            {
                var rule = KeywordRules[i];
                if (restrictToCategory && rule.Category != category)
                {
                    continue;
                }

                if (!string.IsNullOrEmpty(FindMatchedKeyword(title, rule)))
                {
                    return rule;
                }
            }

            return null;
        }

        private static string FindMatchedKeyword(string title, KeywordRule rule)
        {
            if (string.IsNullOrWhiteSpace(title))
            {
                return string.Empty;
            }

            for (var i = 0; i < rule.Keywords.Length; i++)
            {
                var keyword = rule.Keywords[i];
                if (title.IndexOf(keyword, StringComparison.OrdinalIgnoreCase) >= 0)
                {
                    return keyword;
                }
            }

            return string.Empty;
        }

        private static bool IsPrototypeCategory(UserTaskCategory category)
        {
            return category == UserTaskCategory.WorkStudy ||
                   category == UserTaskCategory.Health ||
                   category == UserTaskCategory.Relationships;
        }
    }
}
