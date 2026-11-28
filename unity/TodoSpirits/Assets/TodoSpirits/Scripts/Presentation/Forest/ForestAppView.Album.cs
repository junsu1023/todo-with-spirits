using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using TodoSpirits.Core;
using TodoSpirits.Runtime;
using UnityEngine;
using UnityEngine.UI;

namespace TodoSpirits.Presentation.Forest
{
    public sealed partial class ForestAppView
    {
        private static readonly Color AlbumPaper = new Color32(246, 238, 220, 255);
        private static readonly Color AlbumRule = new Color32(222, 212, 186, 255);

        private float Album()
        {
            var latest = new DateTime(_app.CurrentDate.Year, _app.CurrentDate.Month, 1);
            if (_month > latest) _month = latest;
            var selected = _app.SaveData.Companions.Find(l => l.SpiritId == _selectedSpirit) ?? _app.CurrentLife;
            _selectedSpirit = selected.SpiritId;
            string month = _month.ToString("yyyy-MM", CultureInfo.InvariantCulture);
            string until = _month.AddMonths(1).ToString("yyyy-MM-dd", CultureInfo.InvariantCulture);
            var memories = AlbumMemories(selected.SpiritId).Where(d => d.Date.StartsWith(month, StringComparison.Ordinal)).ToList();
            var records = AlbumFactRecords(selected.SpiritId, month);
            var tasks = records.SelectMany(r => r.CompletedTasks).ToList();
            var groups = tasks.GroupBy(t => AlbumCategory(t.UserCategory)).OrderByDescending(g => g.Count()).ThenBy(g => (int)g.Key).ToList();
            int days = records.Where(r => r.CompletedTasks.Count > 0).Select(r => r.Date).Distinct().Count();
            bool independent = !string.IsNullOrEmpty(selected.IndependentOn) && string.CompareOrdinal(selected.IndependentOn, until) < 0;
            bool notMet = string.CompareOrdinal(selected.StartedOn, until) >= 0;
            var previous = _ui.IconButton("PreviousMonth", ForestIcon.ChevronLeft, _content, 16, 0, 60, 60,
                () => ChangeAlbumMonth(_month.AddMonths(-1)));
            previous.interactable = _month > AlbumFirstMonth();
            var title = _ui.Button("ChooseMemoryMonth", "", _content, 84, 0, _width - 168, 60, AlbumMonths);
            AlbumPlainButton(title);
            _ui.Text(_month.ToString("yyyy년 M월"), title.transform, 0, 0, _width - 192, 60, 23, ForestUi.Ink, true, TextAnchor.MiddleCenter);
            var down = _ui.Icon(ForestIcon.ChevronRight, title.transform, _width - 192, 23, 15, 15, ForestUi.Muted);
            down.rectTransform.localRotation = Quaternion.Euler(0, 0, -90);
            var next = _ui.IconButton("NextMonth", ForestIcon.ChevronRight, _content, _width - 76, 0, 60, 60,
                () => ChangeAlbumMonth(_month.AddMonths(1)), _month < latest ? ForestUi.Ink : new Color(.45f, .48f, .44f, .3f));
            next.interactable = _month < latest;
            AlbumCompanionTabs(until);
            float y = AlbumWaitingMemoryEntry(140);

            if (notMet)
            {
                var paper = _ui.Panel("BeforeMeeting", _content, 24, y, _width - 48, 280, AlbumPaper);
                _ui.Icon(ForestIcon.Leaf, paper.transform, 42, 45, 49, 49, ForestUi.Muted);
                float textX = _width < 400 ? 24 : 128;
                float titleY = _width < 400 ? 136 : 32;
                float bodyY = titleY + AlbumCopy("아직 만나기 전의 계절", paper.transform, textX, titleY, _width - 72 - textX, 22, ForestUi.Ink, true) + 12;
                bodyY += AlbumCopy("우리의 첫 만남은 " + AlbumDate(selected.StartedOn) + ".\n그날부터 이 페이지를 함께 채워요.", paper.transform, textX, bodyY, _width - 72 - textX, 17, ForestUi.Muted) + 22;
                _ui.Button("FirstMeetingMonth", "처음 만난 달 펼치기", paper.transform, 20, bodyY, _width - 88, 60,
                    () => { if (DateTime.TryParse(selected.StartedOn, out var date)) ChangeAlbumMonth(new DateTime(date.Year, date.Month, 1)); }, false, 18);
                paper.rectTransform.sizeDelta = new Vector2(_width - 48, bodyY + 80);
                return y + bodyY + 100;
            }

            y = AlbumCover(memories, groups.Count == 0 ? UserTaskCategory.Unspecified : groups[0].Key, days, tasks.Count, y);
            if (tasks.Count > 0)
            {
                var colorButton = _ui.Button("MonthCategoryDetails", "", _content, 24, y, _width - 48, 78,
                    () => AlbumCategories(records));
                AlbumPlainButton(colorButton);
                _ui.Text("이달의 생활색", colorButton.transform, 0, 0, _width - 110, 28, 16, ForestUi.Muted);
                _ui.Icon(ForestIcon.ChevronRight, colorButton.transform, _width - 74, 5, 18, 18, ForestUi.Muted);
                float x = 0, totalWidth = _width - 48;
                foreach (var group in groups)
                {
                    float width = totalWidth * group.Count() / tasks.Count;
                    _ui.Panel("CategoryShare-" + (int)group.Key, colorButton.transform, x, 34, width, 8, AlbumCategoryColor(group.Key), false);
                    x += width;
                }
                string summary = string.Join("  ·  ", groups.Take(2).Select(g => ForestStoryRules.CategoryName(g.Key) + " " + g.Count() + "회"));
                if (groups.Count > 2) summary += "  외 " + (groups.Count - 2);
                float colorHeight = 49 + AlbumCopy(summary, colorButton.transform, 0, 49, totalWidth, 15, ForestUi.Muted);
                colorButton.GetComponent<RectTransform>().sizeDelta = new Vector2(_width - 48, colorHeight);
                y += colorHeight + 18;
            }
            else y += 12;

            y = AlbumCalendar(selected, records, y);
            y += 22;
            _ui.Text("함께 남긴 장면", _content, 24, y, _width - 48, 40, 23, ForestUi.Ink, true);
            y += 50;
            var choices = selected.Story.Choices.Where(c => !string.IsNullOrEmpty(c.Date) && c.Date.StartsWith(month, StringComparison.Ordinal)).OrderBy(c => c.Date).ThenBy(c => c.Episode).ToList();
            if (choices.Count == 0)
                y += AlbumCopy("작은 선택이 생기면,\n이 계절의 장면으로 남겨 둘게요.", _content, 24, y, _width - 48, 17, ForestUi.Muted) + 22;
            foreach (var choice in choices) y = AlbumChoice(choice, y, memories.FirstOrDefault(d => d.Date == choice.Date));

            if (!string.IsNullOrEmpty(selected.IndependentOn) && selected.IndependentOn.StartsWith(month, StringComparison.Ordinal))
            {
                var departure = _ui.Panel("IndependenceMemory", _content, 24, y, _width - 48, 130, AlbumPaper);
                _ui.Icon(ForestIcon.Leaf, departure.transform, 19, 24, 30, 30, ForestUi.Moss);
                float dy = 16 + AlbumCopy(AlbumDate(selected.IndependentOn) + " · 새로운 숲으로", departure.transform, 64, 16, _width - 134, 18, ForestUi.Moss, true) + 8;
                dy += AlbumCopy("함께 고른 꿈을 품고 독립했어요.\n이곳의 생활은 앨범에 남겨 두고요.", departure.transform, 64, dy, _width - 134, 16, ForestUi.Muted) + 20;
                departure.rectTransform.sizeDelta = new Vector2(_width - 48, dy); y += dy + 20;
            }
            if (selected.Story.ReunionClaimed && !string.IsNullOrEmpty(selected.Story.ReunionDate) && selected.Story.ReunionDate.StartsWith(month, StringComparison.Ordinal))
            {
                var letter = _ui.Button("ReadReunionLetter", "", _content, 24, y, _width - 48, 128, () => AlbumLetter(selected));
                letter.GetComponent<Image>().color = new Color32(247, 231, 224, 255);
                AlbumJourneySprite(3 + (int)selected.Route, letter.transform, 12, 15, 91);
                float lx = 116;
                _ui.Text(AlbumDate(selected.Story.ReunionDate) + " · 도착한 편지", letter.transform, lx, 14, _width - 184, 28, 14, ForestUi.Rose);
                float ly = 49 + AlbumCopy("다시 찾아온 마음", letter.transform, lx, 49, _width - 188, 21, ForestUi.Ink, true) + 5;
                ly += AlbumCopy("편지와 선물 펼치기", letter.transform, lx, ly, _width - 188, 15, ForestUi.Muted) + 16;
                _ui.Icon(ForestIcon.ChevronRight, letter.transform, _width - 83, 49, 18, 18, ForestUi.Rose);
                letter.GetComponent<RectTransform>().sizeDelta = new Vector2(_width - 48, Mathf.Max(128, ly));
                y += Mathf.Max(128, ly) + 20;
            }
            else if (independent && !selected.Story.ReunionClaimed && _month == latest)
                y += AlbumCopy("다음 친구와 이틀의 활동이 쌓이면,\n이 숲으로 첫 편지가 찾아와요.", _content, 24, y, _width - 48, 17, ForestUi.Muted) + 24;
            return y + 12;
        }

        private float AlbumCover(List<ForestDailyMemory> memories, UserTaskCategory leading, int days, int taskCount, float y)
        {
            var snapshot = memories.Where(d => !string.IsNullOrEmpty(d.ObservedAtUtc)).OrderByDescending(d => d.Date, StringComparer.Ordinal).FirstOrDefault()
                ?? memories.Where(d => d.Revision >= 0).OrderByDescending(d => d.Date, StringComparer.Ordinal).FirstOrDefault();
            float width = _width - 48;
            float artWidth = _width < 400 ? 106 : 150;
            var cover = _ui.Panel("MonthMemoryCover", _content, 24, y, width, 214, AlbumPaper);
            _ui.Panel("AlbumSpine", cover.transform, 8, 12, 2, 190, AlbumRule, false);
            var photo = _ui.Panel("MiniPortraitFrame", cover.transform, 20, 23, artWidth, 157, Color.white, false);
            var scenery = _ui.Panel("PortraitWindow", photo.transform, 5, 5, artWidth - 10, 120, ForestUi.Pale, false);
            scenery.gameObject.AddComponent<RectMask2D>();
            AlbumPortrait(snapshot, scenery.transform, artWidth - 10, 120);
            _ui.Text(snapshot == null ? "남겨진 초상 없음" : AlbumDate(snapshot.Date), photo.transform, 5, 128, artWidth - 10, 24, 13, ForestUi.Muted, false, TextAnchor.MiddleCenter);
            photo.rectTransform.localRotation = Quaternion.Euler(0, 0, 3);
            var tape = _ui.Panel("PaperTape", cover.transform, artWidth / 2 - 4, 16, 57, 16, new Color(.73f, .78f, .62f, .8f), false);
            tape.rectTransform.localRotation = Quaternion.Euler(0, 0, -6);
            float tx = artWidth + 35, tw = width - tx - 16;
            float cy = 21;
            cy += AlbumCopy(snapshot == null ? "이달의 기록" : "그날의 작은 정령", cover.transform, tx, cy, tw, 18, ForestUi.Moss, true) + 8;
            string line = taskCount == 0 ? snapshot == null ? "아직 쓰지 않은\n우리의 한 페이지" : "준비와 쉼도\n우리의 한 페이지" : leading == UserTaskCategory.Unspecified
                ? "이름 붙이지 않은\n날들도 소중하게" : ForestStoryRules.CategoryName(leading) + "의 약속이\n많이 남은 달";
            cy += AlbumCopy(line, cover.transform, tx, cy, tw, 19, ForestUi.Ink, true) + 10;
            cy += AlbumCopy(days + "일  ·  " + taskCount + "개", cover.transform, tx, cy, tw, 23, ForestUi.Moss, true) + 3;
            cy += AlbumCopy("기록한 날 · 마친 약속", cover.transform, tx, cy, tw, 12, ForestUi.Muted) + 16;
            float height = Mathf.Max(205, cy);
            if (snapshot != null)
            {
                string state = AlbumStage(snapshot.AppearancePhase) + " · " + AlbumActivityName(snapshot.PrimaryActivity);
                height += AlbumCopy(state, cover.transform, 20, height, width - 40, 16, ForestUi.Moss, true) + 6;
                string source = string.IsNullOrEmpty(snapshot.ObservedAtUtc)
                    ? "이날 저장된 장면 · 아직 감상한 기록은 없어요"
                    : "이날 감상한 장면 · " + AlbumTimestamp(snapshot.ObservedAtUtc);
                height += AlbumCopy(source, cover.transform, 20, height, width - 40, 13, ForestUi.Muted) + 15;
                if (!string.IsNullOrEmpty(snapshot.VisiblePropName))
                    height += AlbumCopy("이날 남은 소품 · " + snapshot.VisiblePropName, cover.transform, 20, height, width - 40, 15, ForestUi.Moss) + 15;
            }
            else height += AlbumCopy("당시 모습이 저장되기 전의 기록이에요.", cover.transform, 20, height, width - 40, 14, ForestUi.Muted) + 15;
            cover.rectTransform.sizeDelta = new Vector2(width, height);
            return y + height + 22;
        }

        private float AlbumCalendar(CompanionLife life, List<DailyCompanionRecord> records, float y)
        {
            var memories = AlbumMemories(life.SpiritId);
            _ui.Text("날짜를 펼쳐 보면", _content, 24, y, _width - 48, 40, 23, ForestUi.Ink, true);
            y += 43;
            y += AlbumCopy("내 약속과 정령의 하루를 함께 펼쳐요", _content, 24, y, _width - 48, 15, ForestUi.Muted) + 16;
            var calendar = _ui.Panel("MemoryCalendar", _content, 24, y, _width - 48, 370, new Color32(253, 250, 241, 255));
            string[] weekdays = { "월", "화", "수", "목", "금", "토", "일" };
            float cell = (_width - 76) / 7;
            for (int i = 0; i < 7; i++) _ui.Text(weekdays[i], calendar.transform, 14 + i * cell, 12, cell, 27, 14, ForestUi.Muted, false, TextAnchor.MiddleCenter);
            int offset = ((int)_month.DayOfWeek + 6) % 7;
            int count = DateTime.DaysInMonth(_month.Year, _month.Month);
            for (int day = 1; day <= count; day++)
            {
                DateTime date = new DateTime(_month.Year, _month.Month, day);
                string dateKey = date.ToString("yyyy-MM-dd", CultureInfo.InvariantCulture);
                var recorded = records.Where(r => r.Date == dateKey).SelectMany(r => r.CompletedTasks).ToList();
                int index = offset + day - 1;
                float x = 14 + index % 7 * cell, cy = 45 + index / 7 * 58;
                var button = _ui.Button("JournalDay-" + dateKey, "", calendar.transform, x + 2, cy, cell - 4, 56, () => OpenDayJournal(date, life.SpiritId));
                // Preserve seven non-overlapping date targets on the narrowest supported canvas.
                button.GetComponent<RectTransform>().sizeDelta = new Vector2(cell - 4, 56);
                AlbumPlainButton(button);
                if (recorded.Count > 0)
                {
                    var category = recorded.GroupBy(t => AlbumCategory(t.UserCategory)).OrderByDescending(g => g.Count()).ThenBy(g => (int)g.Key).First().Key;
                    _ui.Panel("RecordedDayColor", button.transform, 4, 0, Mathf.Max(28, cell - 12), 52, Color.Lerp(AlbumCategoryColor(category), Color.white, .7f));
                    _ui.Panel("RecordedDayDot", button.transform, cell / 2 - 5, 42, 5, 5, AlbumCategoryColor(category));
                }
                else if (memories.Any(d => d.Date == dateKey && (d.Revision >= 0 || !string.IsNullOrEmpty(d.ObservedAtUtc))))
                    _ui.Panel("PlannedDayDot", button.transform, cell / 2 - 5, 42, 5, 5, ForestUi.Muted);
                _ui.Text(day.ToString(), button.transform, 0, 2, cell - 4, 38, 18, date > _app.CurrentDate.Date ? new Color(.60f, .64f, .59f, .45f) : ForestUi.Ink,
                    recorded.Count > 0, TextAnchor.MiddleCenter);
                button.interactable = date <= _app.CurrentDate.Date;
            }
            float height = 51 + Mathf.CeilToInt((offset + count) / 7f) * 58;
            calendar.rectTransform.sizeDelta = new Vector2(_width - 48, height);
            return y + height;
        }

        // Latest stored facts and previously observed scenes are intentionally separate layers.
        private void OpenDayJournal(DateTime date, string spiritId = null)
        {
            string key = date.ToString("yyyy-MM-dd", CultureInfo.InvariantCulture);
            spiritId = spiritId ?? _selectedSpirit ?? _app.CurrentLife.SpiritId;
            var memory = AlbumMemories(spiritId).FirstOrDefault(d => d.Date == key);
            if (date.Date <= _app.CurrentDate.Date && memory != null && (memory.Revision >= 0 || !string.IsNullOrEmpty(memory.ObservedAtUtc)))
            {
                OpenDailyMemory(date, memory);
                return;
            }
            var records = _app.SaveData.Records.Where(r => r.Date == key && (spiritId == null || r.SpiritId == spiritId)).ToList();
            var sheet = Modal(date.ToString("M월 d일") + "의 생활 기록", 450);
            int count = records.Sum(r => r.CompletedTasks.Count);
            float y = 4;
            if (date.Date > _app.CurrentDate.Date)
                y = SheetParagraph(sheet, "아직 오지 않은 하루예요.\n완료한 약속이 생기면 이곳에 남겨 둘게요.", y, 19, ForestUi.Ink) + 24;
            else if (count == 0)
            {
                _ui.Icon(ForestIcon.Leaf, sheet, SheetInset, y + 6, 35, 35, ForestUi.Moss);
                y += 58;
                y = SheetParagraph(sheet, "이날 완료로 남긴 약속이 없어요.\n비어 있는 날도 우리 달력의 한 부분이에요.", y, 19, ForestUi.Ink) + 25;
            }
            else
            {
                y = SheetParagraph(sheet, count + "개의 약속을 마친 날\n이 기록에는 완료한 항목만 남아 있어요. 당시의 예정·초상·감상 시각은 저장되지 않았어요.", y, 17) + 22;
                foreach (var record in records.Where(r => r.CompletedTasks.Count > 0))
                {
                    var companion = _app.SaveData.Companions.Find(l => l.SpiritId == record.SpiritId);
                    if (companion != null)
                    {
                        string rememberedName = !string.IsNullOrEmpty(companion.IndependentOn) && string.CompareOrdinal(companion.IndependentOn, key) <= 0 ? companion.DisplayName : "작은 정령";
                        _ui.Icon(ForestIcon.Book, sheet, SheetInset, y + 6, 32, 32, ForestUi.Moss);
                        y += AlbumCopy(rememberedName + "과 함께", sheet, SheetInset + 59, y + 5, SheetInnerWidth - 59, 18, ForestUi.Moss, true) + 25;
                    }
                    foreach (var task in record.CompletedTasks)
                    {
                        UserTaskCategory category = AlbumCategory(task.UserCategory);
                        var entry = _ui.Rect("JournalTask-" + task.TaskId, sheet, SheetInset, y, SheetInnerWidth, 100);
                        _ui.Icon(ForestIcon.Check, entry, 0, 7, 21, 21, ForestUi.Moss);
                        float bodyY = AlbumCopy(task.Title, entry, 34, 0, SheetInnerWidth - 34, 20, ForestUi.Ink, true) + 8;
                        string details = ForestStoryRules.CategoryName(category) + (string.IsNullOrEmpty(task.RoutineId) ? " · 일정" : " · 습관");
                        _ui.Panel("CategoryDot", entry, 36, bodyY + 10, 7, 7, AlbumCategoryColor(category));
                        bodyY += AlbumCopy(details, entry, 52, bodyY, SheetInnerWidth - 52, 15, ForestUi.Muted) + 18;
                        _ui.Divider(entry, 34, bodyY, SheetInnerWidth - 34, AlbumRule);
                        entry.sizeDelta = new Vector2(SheetInnerWidth, bodyY + 18);
                        y += bodyY + 18;
                    }
                }
            }
            var rememberedChoices = _app.SaveData.Companions.Where(l => spiritId == null || l.SpiritId == spiritId)
                .SelectMany(l => l.Story.Choices).Where(c => c.Date == key).ToList();
            if (date.Date <= _app.CurrentDate.Date && rememberedChoices.Count > 0)
            {
                y += 8;
                y += AlbumCopy("이날 함께 고른 마음", sheet, SheetInset, y, SheetInnerWidth, 20, ForestUi.Moss, true) + 12;
                foreach (var choice in rememberedChoices)
                    y = SheetParagraph(sheet, choice.Caption, y, 18, ForestUi.Ink) + 18;
            }
            y = SheetParagraph(sheet, "완료한 생활을 읽는 페이지예요. 여기서는 지난 기록을 바꾸지 않아요.", y + 8, 14) + 20;
            _ui.Button("CloseDayJournal", "이날의 기억 덮기", sheet, SheetInset, y, SheetInnerWidth, 60, CloseModal, false, 18);
            SheetContentHeight(sheet, y + 60);
        }

        private List<ForestDailyMemory> AlbumMemories(string spiritId)
        {
            string account = _app.IsHostConnected ? _app.SaveData.Forest.HostAccountId : "local";
            return (_app.SaveData.Forest.Days ?? new List<ForestDailyMemory>())
                .Where(d => d != null && d.AccountId == account && d.SpiritId == spiritId && !string.IsNullOrEmpty(d.Date))
                .GroupBy(d => d.Date).Select(g => g.OrderByDescending(d => d.Revision).First()).ToList();
        }

        private List<DailyCompanionRecord> AlbumFactRecords(string spiritId, string month)
        {
            var records = _app.SaveData.Records.Where(r => r.SpiritId == spiritId && r.Date != null && r.Date.StartsWith(month, StringComparison.Ordinal)).ToList();
            foreach (var memory in AlbumMemories(spiritId).Where(d => d.Revision >= 0 && d.Date.StartsWith(month, StringComparison.Ordinal)))
            {
                // UI projection only: corrections replace the displayed facts, never rewrite a saved scene.
                records.RemoveAll(r => r.Date == memory.Date);
                records.Add(new DailyCompanionRecord {
                    Date = memory.Date, SpiritId = spiritId,
                    CompletedTasks = (memory.Todos ?? new List<ForestDailyTodo>()).Where(t => t != null && t.IsCompleted && !t.CompletedWithPreviousSpirit && !t.AwaitingCompanion)
                        .Select(t => new CompletedTask(t.Id, t.Title, AlbumCategory(t.Category), "")).ToList()
                });
            }
            return records;
        }

        private Sprite AlbumPortraitSprite(ForestDailyMemory memory)
        {
            if (memory.PrimaryActivity == "read" && GrowthPhase(memory.AppearancePhase) == 0 && readingSpirit != null) return readingSpirit;
            return PosesForStage(memory.AppearancePhase)[memory.PrimaryActivity == "rest" ? 1 : 0];
        }

        private void AlbumPortrait(ForestDailyMemory memory, Transform parent, float width, float height)
        {
            if (memory == null)
            {
                _ui.Icon(ForestIcon.Book, parent, (width - 38) / 2, (height - 38) / 2, 38, 38, ForestUi.Muted);
                return;
            }
            var background = _ui.Sprite("RememberedForest", forestBackground, parent, 0, -height * .5f, width, height * 1.5f);
            background.preserveAspect = false;
            float bodySize = height * .84f;
            bool reading = memory.PrimaryActivity == "read";
            if (reading && readingDesk != null)
                _ui.Sprite("RememberedDesk", readingDesk, parent, width * .08f, height * .43f, width * .84f, height * .52f);
            _ui.Sprite("RememberedSpirit", AlbumPortraitSprite(memory), parent, (width - bodySize) * .5f,
                reading ? height * .01f : height * .13f, bodySize, bodySize);
            if (reading && (GrowthPhase(memory.AppearancePhase) > 0 || readingSpirit == null))
                SheetArt("RememberedNotebook", JourneyArt(0), parent, width * .28f, height * .47f, width * .44f, height * .39f, ForestIcon.Book);
            if (!reading)
            {
                ForestIcon icon = memory.PrimaryActivity == "walk" ? ForestIcon.Compass : memory.PrimaryActivity == "craft" ? ForestIcon.Flower
                    : memory.PrimaryActivity == "tea" ? ForestIcon.Heart : ForestIcon.Leaf;
                _ui.Icon(icon, parent, width - 29, height - 31, 21, 21, ForestUi.Moss);
            }
            AlbumSnapshotKeepsake(memory, parent, 0, height - 43, 42);
        }

        private void AlbumSnapshotKeepsake(ForestDailyMemory memory, Transform parent, float x, float y, float size)
        {
            if (memory == null || string.IsNullOrEmpty(memory.VisiblePropName) || memory.LastChoiceOption < 0 || memory.ChoiceCount <= 0) return;
            int option = memory.LastChoiceOption;
            string art = null;
            ForestIcon icon = ForestIcon.Leaf;
            if (memory.ChoiceCount == 1)
            {
                if (memory.StoryFamily == ForestStoryFamily.Focus || memory.StoryFamily == ForestStoryFamily.Legacy)
                { AlbumJourneySprite(option, parent, x, y, size); return; }
                if (memory.StoryFamily == ForestStoryFamily.Movement)
                { if (option == 1) art = "sign_arrow"; else icon = ForestIcon.Compass; }
                if (memory.StoryFamily == ForestStoryFamily.Everyday) art = option == 0 ? "lantern" : "wind_chime";
            }
            else if (memory.ChoiceCount == 2)
            { if (option == 0) icon = ForestIcon.Mail; else art = "sign_blank"; }
            else if (memory.ChoiceCount == 3)
                icon = option == 0 ? ForestIcon.Book : option == 1 ? ForestIcon.Compass : ForestIcon.Flower;
            var sprite = art == null ? null : ArtSprite(art);
            if (sprite != null) _ui.Sprite("StoredKeepsake", sprite, parent, x, y, size, size);
            else _ui.Icon(icon, parent, x + size * .18f, y + size * .18f, size * .64f, size * .64f, ForestUi.Moss);
        }

        private void OpenDailyMemory(DateTime date, ForestDailyMemory memory)
        {
            var sheet = Modal(date.ToString("M월 d일") + "의 우리", 530);
            var todos = (memory.Todos ?? new List<ForestDailyTodo>()).Where(t => t != null).ToList();
            bool observed = !string.IsNullOrEmpty(memory.ObservedAtUtc);
            float y = 6;
            var portrait = _ui.Panel("DayMemoryPortrait", sheet, SheetInset, y, SheetInnerWidth, 164, AlbumPaper);
            var picture = _ui.Rect("StoredPortrait", portrait.transform, 8, 8, 140, 148);
            picture.gameObject.AddComponent<RectMask2D>();
            AlbumPortrait(memory, picture, 140, 148);
            float tx = 160, tw = SheetInnerWidth - 172;
            float py = 14 + AlbumCopy(AlbumStage(memory.AppearancePhase), portrait.transform, tx, 14, tw, 18, ForestUi.Moss, true) + 5;
            py += AlbumCopy(AlbumActivityName(memory.PrimaryActivity), portrait.transform, tx, py, tw, 17, ForestUi.Ink, true) + 8;
            py += AlbumCopy(observed ? "그날 감상한 모습" : "이날 저장된 모습", portrait.transform, tx, py, tw, 14, ForestUi.Muted) + 10;
            if (!string.IsNullOrEmpty(memory.VisiblePropName))
                py += AlbumCopy("이날 남은 소품\n" + memory.VisiblePropName, portrait.transform, tx, py, tw, 14, ForestUi.Moss) + 10;
            portrait.rectTransform.sizeDelta = new Vector2(SheetInnerWidth, Mathf.Max(164, py));
            y += Mathf.Max(164, py) + 22;

            y += AlbumCopy("내 생활 기록", sheet, SheetInset, y, SheetInnerWidth, 22, ForestUi.Ink, true) + 5;
            y = SheetParagraph(sheet, "최종 확인 " + AlbumTimestamp(memory.LastConfirmedAtUtc) + "\n기록 버전 " + AlbumRevision(memory.Revision), y, 13) + 14;
            if (todos.Count == 0)
                y = SheetParagraph(sheet, memory.Revision >= 0 ? "확인된 일정이 없는 날이에요. 완료나 보상을 만들어 기록하지 않아요."
                    : "이날의 전체 일정은 아직 확인되지 않았어요.", y, 17) + 14;
            for (int i = 0; i < todos.Count; i++)
            {
                var todo = todos[i];
                var entry = _ui.Panel("RememberedTodo-" + i, sheet, SheetInset, y, SheetInnerWidth, 150, AlbumPaper);
                float ey = 12;
                string status = todo.IsCompleted ? "완료" : "예정";
                ey += AlbumCopy((i + 1) + ". " + todo.Title, entry.transform, 14, ey, SheetInnerWidth - 28, 19, ForestUi.Ink, true) + 5;
                ey += AlbumCopy(status + " · " + ForestStoryRules.CategoryName(AlbumCategory(todo.Category)) + (todo.IsHabit ? " · 습관" : " · 일정"),
                    entry.transform, 14, ey, SheetInnerWidth - 28, 14, todo.IsCompleted ? ForestUi.Moss : ForestUi.Muted) + 10;
                ey += AlbumCopy("→ " + todo.Location + " · " + todo.Activity, entry.transform, 14, ey, SheetInnerWidth - 28, 16, ForestUi.Ink) + 6;
                if (!string.IsNullOrEmpty(todo.Reason)) ey += AlbumCopy(todo.Reason, entry.transform, 14, ey, SheetInnerWidth - 28, 13, ForestUi.Muted) + 4;
                entry.rectTransform.sizeDelta = new Vector2(SheetInnerWidth, ey + 10);
                y += ey + 22;
            }

            y += 8;
            y += AlbumCopy("그때 본 정령의 하루", sheet, SheetInset, y, SheetInnerWidth, 22, ForestUi.Ink, true) + 8;
            if (!observed)
                y = SheetParagraph(sheet, "이날 감상한 장면은 아직 남아 있지 않아요. 위의 예정과 완료는 저장된 생활 기록이에요.", y, 17) + 18;
            else
            {
                y = SheetParagraph(sheet, "감상 " + AlbumTimestamp(memory.ObservedAtUtc) + "\n감상한 기록 버전 " + AlbumRevision(memory.ObservedRevision), y, 13) + 14;
                if (memory.ObservedRevision != memory.Revision)
                    y = SheetParagraph(sheet, "감상 뒤 생활 기록이 갱신되었어요. 위 목록은 최근 사실이고, 아래는 그때 본 장면 그대로예요.", y, 16, ForestUi.Moss) + 16;
                foreach (var activity in (memory.ObservedActivities ?? new List<ForestDailyActivity>()).Where(a => a != null))
                {
                    string state = activity.IsAutonomous ? "자율 생활" : activity.IsCompleted ? "함께 한 장면" : "준비한 장면";
                    y += AlbumCopy(activity.Location + " · " + state, sheet, SheetInset, y, SheetInnerWidth, 17, ForestUi.Moss, true) + 5;
                    y = SheetParagraph(sheet, activity.Activity, y, 18, ForestUi.Ink) + 5;
                    if (!string.IsNullOrEmpty(activity.Reason)) y = SheetParagraph(sheet, activity.Reason, y, 14) + 5;
                    var ids = activity.SourceTodoIds ?? new List<string>();
                    if (ids.Count > 0)
                    {
                        var indices = ids.Select(id => todos.FindIndex(t => t.Id == id)).ToList();
                        string links = string.Join(", ", indices.Where(i => i >= 0).Distinct().Select(i => (i + 1) + "번"));
                        string source = string.IsNullOrEmpty(links) ? "연결 원본은 최신 목록에 남아 있지 않아요."
                            : "연결 원본 · 위 최신 목록의 " + links;
                        if (!string.IsNullOrEmpty(links) && indices.Any(i => i < 0)) source += "\n일부 원본은 이후 목록에서 변경되었어요.";
                        y = SheetParagraph(sheet, source, y, 13) + 8;
                    }
                    y += 12;
                }
            }

            y += AlbumCopy("이날의 획득", sheet, SheetInset, y, SheetInnerWidth, 22, ForestUi.Ink, true) + 8;
            string essence = "이날 저장된 누적 정수 " + memory.TotalEssence;
            y = SheetParagraph(sheet, essence, y, 18, ForestUi.Moss) + 8;
            var acquisitions = (memory.Acquisitions ?? new List<string>()).Where(a => !string.IsNullOrEmpty(a)).Distinct().ToList();
            if (acquisitions.Count == 0) y = SheetParagraph(sheet, "저장된 새 소품이나 발견물은 없어요.", y, 16) + 14;
            else foreach (string acquisition in acquisitions) y = SheetParagraph(sheet, "· " + acquisition, y, 17, ForestUi.Ink) + 10;
            y = SheetParagraph(sheet, "획득은 이미 저장된 결과예요. 앨범을 다시 펼쳐도 정수가 추가되지 않아요.", y + 5, 14) + 18;
            var journeys = _app.Interventions.Trips.Where(t => t != null && t.Claimed && t.SpiritId == memory.SpiritId &&
                (string.IsNullOrEmpty(t.ClaimedOn) ? t.StartedOn : t.ClaimedOn) == memory.Date).ToList();
            foreach (var journey in journeys)
            {
                _ui.Button("ReopenJourney-" + journey.TripId, "나들이 이야기 다시 읽기\n" + journey.Discovery,
                    sheet, SheetInset, y, SheetInnerWidth, 88,
                    () => ShowJourneyMemory(journey, () => OpenDailyMemory(date, memory), memory), false, 18);
                y += 100;
            }
            _ui.Button("CloseDailyMemory", "이날의 기억 덮기", sheet, SheetInset, y, SheetInnerWidth, 60, CloseModal, false, 18);
            SheetContentHeight(sheet, y + 72);
        }

        private static string AlbumRevision(long revision) => revision < 0 ? "미확인" : revision.ToString(CultureInfo.InvariantCulture);
        private static string AlbumTimestamp(string value) => DateTimeOffset.TryParse(value, CultureInfo.InvariantCulture, DateTimeStyles.None, out var timestamp)
            ? timestamp.ToLocalTime().ToString("yyyy.MM.dd HH:mm", CultureInfo.InvariantCulture) + " (기기 시각)" : "확인 시각 없음";
        private static string AlbumStage(CompanionStage stage)
        {
            switch (stage)
            {
                case CompanionStage.Meeting: return "첫 만남의 모습";
                case CompanionStage.Adapting: return "숲에 익숙해진 모습";
                case CompanionStage.Interests: return "관심이 자라난 모습";
                case CompanionStage.OwnWay: return "취향을 찾아가는 모습";
                case CompanionStage.Preparing: return "새 길을 준비하는 모습";
                case CompanionStage.Adult: return "자신의 길을 찾은 모습";
                default: return "저장된 성장 단계 없음";
            }
        }
        private static string AlbumActivityName(string activity)
        {
            switch (activity)
            {
                case "read": return "책상에서 보낸 하루";
                case "walk": return "숲길에서 보낸 하루";
                case "craft": return "공작대에서 보낸 하루";
                case "tea": return "티테이블에서 보낸 하루";
                case "rest": return "쉼터에서 보낸 하루";
                case "companion": return "공터에서 보낸 하루";
                default: return "생활 장면 기록 없음";
            }
        }

        private float AlbumChoice(ForestChoice choice, float y, ForestDailyMemory memory)
        {
            string[] titles = { "처음 발견한 작은 취향", "선택을 이어 간 하루", "우리만의 작은 완성" };
            var scene = _ui.Rect("ChoiceMemory-" + choice.Episode, _content, 24, y, _width - 48, 120);
            if (memory != null && !string.IsNullOrEmpty(memory.VisiblePropName) && memory.ChoiceCount == choice.Episode + 1 && memory.LastChoiceOption == choice.Option)
                AlbumSnapshotKeepsake(memory, scene, -4, 8, 83);
            else _ui.Icon(choice.Option == 0 ? ForestIcon.Heart : ForestIcon.Leaf, scene, 19, 26, 40, 40, ForestUi.Rose);
            float tx = 94, tw = _width - 166;
            _ui.Text(AlbumDate(choice.Date), scene, tx, 0, tw, 27, 15, ForestUi.Rose);
            float sy = 33 + AlbumCopy(titles[Mathf.Clamp(choice.Episode, 0, 2)], scene, tx, 33, tw, 20, ForestUi.Ink, true) + 8;
            sy += AlbumCopy(choice.Caption, scene, tx, sy, tw, 17, ForestUi.Muted) + 21;
            _ui.Divider(scene, tx, sy, tw, AlbumRule);
            scene.sizeDelta = new Vector2(_width - 48, sy + 20);
            return y + sy + 20;
        }

        private void AlbumCompanionTabs(string until)
        {
            var viewport = _ui.Rect("AlbumCompanions", _content, 24, 66, _width - 48, 66);
            viewport.gameObject.AddComponent<Image>().color = new Color(1, 1, 1, .001f);
            viewport.gameObject.AddComponent<RectMask2D>();
            var scroll = viewport.gameObject.AddComponent<ScrollRect>(); scroll.horizontal = true; scroll.vertical = false;
            scroll.viewport = viewport; scroll.movementType = ScrollRect.MovementType.Clamped;
            var row = _ui.Rect("CompanionPortraits", viewport, 0, 0, _width - 48, 62); scroll.content = row;
            float x = 0, selectedX = 0; int index = 1;
            foreach (var companion in _app.SaveData.Companions)
            {
                var captured = companion;
                bool independent = !string.IsNullOrEmpty(companion.IndependentOn) && string.CompareOrdinal(companion.IndependentOn, until) < 0;
                string monthKey = _month.ToString("yyyy-MM", CultureInfo.InvariantCulture);
                var memory = AlbumMemories(companion.SpiritId).Where(d => d.Date.StartsWith(monthKey, StringComparison.Ordinal)
                    && (d.Revision >= 0 || !string.IsNullOrEmpty(d.ObservedAtUtc))).OrderByDescending(d => d.Date, StringComparer.Ordinal).FirstOrDefault();
                string name = independent ? companion.DisplayName : "작은 정령 " + index;
                float width = Mathf.Clamp(75 + name.Length * 16, 142, 272);
                bool active = companion.SpiritId == _selectedSpirit;
                var button = _ui.Button("AlbumSpirit-" + companion.SpiritId, "", row, x, 0, width, 62,
                    () => { _selectedSpirit = captured.SpiritId; ResetPageScroll(); Render(); });
                button.GetComponent<Image>().color = active ? AlbumPaper : Color.clear;
                if (button.TryGetComponent<Outline>(out var outline)) outline.enabled = active;
                if (memory != null)
                    _ui.Sprite("CompanionPortrait", AlbumPortraitSprite(memory), button.transform, 6, 4, 50, 50);
                else _ui.Icon(ForestIcon.Leaf, button.transform, 17, 16, 27, 27, ForestUi.Muted);
                _ui.Text(name, button.transform, 65, 4, width - 78, 54, 16, active ? ForestUi.Ink : ForestUi.Muted, active, TextAnchor.MiddleLeft);
                if (active) selectedX = x;
                x += width + 8; index++;
            }
            row.sizeDelta = new Vector2(Mathf.Max(_width - 48, x - 8), 62);
            row.anchoredPosition = new Vector2(-Mathf.Clamp(selectedX - 8, 0, Mathf.Max(0, x - 8 - (_width - 48))), 0);
        }

        private void AlbumMonths()
        {
            var life = _app.SaveData.Companions.Find(l => l.SpiritId == _selectedSpirit) ?? _app.CurrentLife;
            var dates = new List<string> { _app.CurrentDateKey, life.StartedOn, life.IndependentOn, life.Story.ReunionDate };
            dates.AddRange(_app.SaveData.Records.Where(r => r.SpiritId == life.SpiritId && r.CompletedTasks.Count > 0).Select(r => r.Date));
            dates.AddRange(AlbumMemories(life.SpiritId).Where(d => d.Revision >= 0 || !string.IsNullOrEmpty(d.ObservedAtUtc)).Select(d => d.Date));
            dates.AddRange(life.Story.Choices.Select(c => c.Date));
            var months = dates.Where(d => DateTime.TryParse(d, out _)).Select(DateTime.Parse)
                .Where(d => d.Date <= _app.CurrentDate.Date).Select(d => new DateTime(d.Year, d.Month, 1)).Distinct().OrderByDescending(d => d).ToList();
            var sheet = Modal("기억할 달 고르기", 400);
            float y = SheetParagraph(sheet, "생활이나 이야기가 남아 있는 달을 펼쳐요.", 4, 17) + 20;
            foreach (var date in months)
            {
                var chosen = date;
                string month = date.ToString("yyyy-MM", CultureInfo.InvariantCulture);
                var records = AlbumFactRecords(life.SpiritId, month);
                int days = records.Where(r => r.CompletedTasks.Count > 0).Select(r => r.Date).Distinct().Count();
                int count = records.Sum(r => r.CompletedTasks.Count);
                var row = _ui.Button("MemoryMonth-" + month, "", sheet, SheetInset, y, SheetInnerWidth, 84, () => ChangeAlbumMonth(chosen));
                row.GetComponent<Image>().color = _month == date ? AlbumPaper : Color.clear;
                _ui.Text(date.ToString("yyyy년 M월"), row.transform, 16, 9, SheetInnerWidth - 62, 38, 21, ForestUi.Ink, true);
                bool hasStory = life.StartedOn != null && life.StartedOn.StartsWith(month, StringComparison.Ordinal)
                    || life.Story.Choices.Any(c => c.Date != null && c.Date.StartsWith(month, StringComparison.Ordinal))
                    || life.IndependentOn != null && life.IndependentOn.StartsWith(month, StringComparison.Ordinal)
                    || life.Story.ReunionDate != null && life.Story.ReunionDate.StartsWith(month, StringComparison.Ordinal);
                bool hasSnapshot = AlbumMemories(life.SpiritId).Any(d => d.Date.StartsWith(month, StringComparison.Ordinal) && d.Revision >= 0);
                float rowHeight = 49 + AlbumCopy(days == 0 ? hasSnapshot ? "예정과 정령의 하루가 남은 페이지" : hasStory ? "만남과 이야기의 페이지" : "아직 채우지 않은 페이지" : days + "일의 생활 · " + count + "개의 약속",
                    row.transform, 16, 49, SheetInnerWidth - 62, 15, ForestUi.Muted) + 10;
                _ui.Icon(_month == date ? ForestIcon.Check : ForestIcon.ChevronRight, row.transform, SheetInnerWidth - 36, 28, 21, 21, ForestUi.Moss);
                row.GetComponent<RectTransform>().sizeDelta = new Vector2(SheetInnerWidth, Mathf.Max(84, rowHeight));
                y += Mathf.Max(84, rowHeight) + 10;
            }
            SheetContentHeight(sheet, y);
        }

        private void AlbumCategories(List<DailyCompanionRecord> records)
        {
            var tasks = records.SelectMany(r => r.CompletedTasks).ToList();
            var sheet = Modal(_month.ToString("M월") + "의 생활색", 450);
            float y = SheetParagraph(sheet, "완료한 약속의 수를 그대로 모았어요.\n투입한 시간이나 성적을 뜻하지는 않아요.", 4, 17) + 22;
            foreach (var group in tasks.GroupBy(t => AlbumCategory(t.UserCategory)).OrderByDescending(g => g.Count()).ThenBy(g => (int)g.Key))
            {
                _ui.Text(ForestStoryRules.CategoryName(group.Key), sheet, SheetInset, y, SheetInnerWidth - 78, 31, 18, ForestUi.Ink, true);
                _ui.Text(group.Count() + "회", sheet, SheetInset + SheetInnerWidth - 68, y, 68, 31, 18, ForestUi.Muted, false, TextAnchor.MiddleRight);
                _ui.Panel("CategoryTrack", sheet, SheetInset, y + 39, SheetInnerWidth, 8, AlbumRule);
                _ui.Panel("CategoryAmount", sheet, SheetInset, y + 39, SheetInnerWidth * group.Count() / Mathf.Max(1, tasks.Count), 8, AlbumCategoryColor(group.Key));
                y += 70;
            }
            y = SheetParagraph(sheet, "미정은 의미를 아직 정하지 않은 기록이에요. 다른 생활 유형으로 바꾸어 해석하지 않아요.", y + 8, 15) + 20;
            _ui.Button("CloseCategoryDetails", "앨범으로 돌아가기", sheet, SheetInset, y, SheetInnerWidth, 60, CloseModal, false, 18);
            SheetContentHeight(sheet, y + 60);
        }

        private DateTime AlbumFirstMonth()
        {
            var dates = _app.SaveData.Companions.Select(l => l.StartedOn).Where(d => DateTime.TryParse(d, out _)).Select(DateTime.Parse).ToList();
            dates.AddRange(_app.SaveData.Companions.SelectMany(l => AlbumMemories(l.SpiritId)).Where(d => d.Revision >= 0)
                .Select(d => d.Date).Where(d => DateTime.TryParse(d, out _)).Select(DateTime.Parse));
            DateTime first = dates.Count == 0 ? _app.CurrentDate : dates.Min();
            return new DateTime(first.Year, first.Month, 1);
        }

        private void ChangeAlbumMonth(DateTime month)
        {
            _month = new DateTime(month.Year, month.Month, 1);
            ResetPageScroll(); Render();
            if (!PrototypeBootstrap.Current.IsDemo) _app.RequestForestHistory(_month.ToString("yyyy-MM"));
        }

        private void AlbumLetter(CompanionLife companion) => ShowMemoryLetter(companion);

        private void AlbumJourneySprite(int index, Transform parent, float x, float y, float size)
        {
            if (journeyProps != null && index >= 0 && index < journeyProps.Length && journeyProps[index] != null)
                _ui.Sprite("MemoryKeepsake-" + index, journeyProps[index], parent, x, y, size, size);
            else _ui.Icon(index == 1 || index == 4 ? ForestIcon.Compass : index == 2 || index == 5 ? ForestIcon.Flower : ForestIcon.Book,
                parent, x + size * .2f, y + size * .2f, size * .6f, size * .6f, ForestUi.Moss);
        }

        private static void AlbumPlainButton(Button button)
        {
            button.GetComponent<Image>().color = Color.clear;
            if (button.TryGetComponent<Outline>(out var outline)) outline.enabled = false;
        }

        private float AlbumCopy(string text, Transform parent, float x, float y, float width, int size, Color color, bool bold = false, TextAnchor align = TextAnchor.UpperLeft)
        {
            var label = _ui.Text(text ?? "", parent, x, y, width, 200, size, color, bold, align);
            label.lineSpacing = 1.18f;
            float height = Mathf.Max(size * 1.65f, label.preferredHeight + 6);
            label.rectTransform.sizeDelta = new Vector2(width, height);
            return height;
        }

        private static string AlbumDate(string value) => DateTime.TryParse(value, out var date) ? date.ToString("M월 d일") : value ?? "";
        private static UserTaskCategory AlbumCategory(UserTaskCategory category) => Enum.IsDefined(typeof(UserTaskCategory), category) ? category : UserTaskCategory.Unspecified;

        private static Color AlbumCategoryColor(UserTaskCategory category)
        {
            switch (category)
            {
                case UserTaskCategory.Unspecified: return new Color32(156, 167, 157, 255);
                case UserTaskCategory.WorkStudy: return new Color32(119, 148, 129, 255);
                case UserTaskCategory.Health: return new Color32(154, 176, 121, 255);
                case UserTaskCategory.Relationships: return new Color32(204, 148, 164, 255);
                case UserTaskCategory.Hobby: return new Color32(176, 160, 191, 255);
                case UserTaskCategory.RestMind: return new Color32(131, 171, 174, 255);
                default: return new Color32(178, 159, 113, 255);
            }
        }
    }
}
