using System;
using System.Collections.Generic;
using System.Globalization;
using TodoSpirits.Core;

namespace TodoSpirits.Runtime
{
    public enum MockTodoPreset
    {
        StandardThree = 0,
        Empty = 1,
        Single = 2,
        FourTasks = 3,
        DemoDay1WorkFocus = 4,
        DemoDay2HealthRelationship = 5,
        DemoDay3Rest = 6
    }

    /// <summary>
    /// Prototype-only completed TODO source with deterministic, date-scoped task IDs.
    /// </summary>
    public sealed class MockTodoCompletionSource : ITodoCompletionSource
    {
        private const int PresetTotal = (int)MockTodoPreset.DemoDay3Rest + 1;

        public MockTodoPreset CurrentPreset { get; private set; }

        public int CurrentPresetIndex => (int)CurrentPreset;

        public int PresetCount => PresetTotal;

        public string CurrentPresetName
        {
            get
            {
                switch (CurrentPreset)
                {
                    case MockTodoPreset.Empty:
                        return "0개";
                    case MockTodoPreset.Single:
                        return "1개";
                    case MockTodoPreset.FourTasks:
                        return "4개";
                    case MockTodoPreset.DemoDay1WorkFocus:
                        return "Demo Day 1 · 업무 집중";
                    case MockTodoPreset.DemoDay2HealthRelationship:
                        return "Demo Day 2 · 건강과 관계";
                    case MockTodoPreset.DemoDay3Rest:
                        return "Demo Day 3 · 휴식";
                    default:
                        return "기본 3개";
                }
            }
        }

        public MockTodoCompletionSource(MockTodoPreset initialPreset = MockTodoPreset.StandardThree)
        {
            CurrentPreset = IsValidPreset(initialPreset)
                ? initialPreset
                : MockTodoPreset.StandardThree;
        }

        public IReadOnlyList<CompletedTask> GetCompletedTasks(string date)
        {
            var dateKey = NormalizeDate(date);
            var tasks = new List<CompletedTask>(4);

            if (CurrentPreset == MockTodoPreset.Empty ||
                CurrentPreset == MockTodoPreset.DemoDay3Rest)
            {
                return tasks;
            }

            if (CurrentPreset == MockTodoPreset.DemoDay1WorkFocus)
            {
                tasks.Add(CreateTask(
                    dateKey,
                    "planning-document",
                    "기획서 수정",
                    UserTaskCategory.WorkStudy,
                    "09:30:00"));
                tasks.Add(CreateTask(
                    dateKey,
                    "development-document",
                    "개발 문서 정리",
                    UserTaskCategory.WorkStudy,
                    "14:00:00"));
                tasks.Add(CreateTask(
                    dateKey,
                    "code-review",
                    "코드 리뷰",
                    UserTaskCategory.WorkStudy,
                    "18:00:00"));
                return tasks;
            }

            if (CurrentPreset == MockTodoPreset.DemoDay2HealthRelationship)
            {
                tasks.Add(CreateTask(
                    dateKey,
                    "thirty-minute-walk",
                    "30분 산책",
                    UserTaskCategory.Health,
                    "17:30:00"));
                tasks.Add(CreateTask(
                    dateKey,
                    "dinner-with-friend",
                    "친구와 저녁",
                    UserTaskCategory.Relationships,
                    "20:00:00"));
                return tasks;
            }

            tasks.Add(CreateTask(
                dateKey,
                "planning-document",
                "기획서 수정",
                UserTaskCategory.WorkStudy,
                "09:30:00"));

            if (CurrentPreset == MockTodoPreset.Single)
            {
                return tasks;
            }

            tasks.Add(CreateTask(
                dateKey,
                "thirty-minute-walk",
                "30분 산책",
                UserTaskCategory.Health,
                "17:30:00"));
            tasks.Add(CreateTask(
                dateKey,
                "dinner-with-friend",
                "친구와 저녁",
                UserTaskCategory.Relationships,
                "20:00:00"));

            if (CurrentPreset == MockTodoPreset.FourTasks)
            {
                tasks.Add(CreateTask(
                    dateKey,
                    "organize-work-notes",
                    "업무 노트 정리",
                    UserTaskCategory.WorkStudy,
                    "21:00:00"));
            }

            return tasks;
        }

        public MockTodoPreset CyclePreset()
        {
            CurrentPreset = (MockTodoPreset)((CurrentPresetIndex + 1) % PresetTotal);
            return CurrentPreset;
        }

        public void SetPreset(MockTodoPreset preset)
        {
            if (!IsValidPreset(preset))
            {
                throw new ArgumentOutOfRangeException(nameof(preset), preset, "Unknown mock TODO preset.");
            }

            CurrentPreset = preset;
        }

        private static CompletedTask CreateTask(
            string date,
            string stableSuffix,
            string title,
            UserTaskCategory category,
            string completedTime)
        {
            return new CompletedTask(
                $"mock:{date}:{stableSuffix}",
                title,
                category,
                $"{date}T{completedTime}");
        }

        private static string NormalizeDate(string date)
        {
            if (DateTime.TryParseExact(
                    date,
                    "yyyy-MM-dd",
                    CultureInfo.InvariantCulture,
                    DateTimeStyles.None,
                    out var parsedDate))
            {
                return parsedDate.ToString("yyyy-MM-dd", CultureInfo.InvariantCulture);
            }

            throw new ArgumentException("Date must use yyyy-MM-dd format.", nameof(date));
        }

        private static bool IsValidPreset(MockTodoPreset preset)
        {
            var value = (int)preset;
            return value >= (int)MockTodoPreset.StandardThree &&
                   value <= (int)MockTodoPreset.DemoDay3Rest;
        }
    }
}
