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
        // These are account records between companions, never part of a companion's growth totals.
        private List<ForestDailyMemory> WaitingMemories()
        {
            string account = _app.IsHostConnected ? _app.SaveData.Forest.HostAccountId : "local";
            return (_app.SaveData.Forest.Days ?? new List<ForestDailyMemory>())
                .Where(d => d != null && d.AccountId == account && string.IsNullOrEmpty(d.SpiritId)
                    && d.Revision >= 0 && DateTime.TryParseExact(d.Date, "yyyy-MM-dd", CultureInfo.InvariantCulture,
                        DateTimeStyles.None, out var date) && date.Date <= _app.CurrentDate.Date)
                .GroupBy(d => d.Date).Select(g => g.OrderByDescending(d => d.Revision).First())
                .OrderByDescending(d => d.Date, StringComparer.Ordinal).ToList();
        }

        private float AlbumWaitingMemoryEntry(float y)
        {
            int count = WaitingMemories().Count;
            if (count == 0) return y;
            var button = _ui.Button("WaitingMemoryEntry", "", _content, 24, y, _width - 48, 100, OpenWaitingMemories);
            button.GetComponent<Image>().color = AlbumPaper;
            _ui.Icon(ForestIcon.Book, button.transform, 16, 20, 30, 30, ForestUi.Moss);
            float copyWidth = _width - 150;
            float height = 12 + AlbumCopy("동행을 기다린 날 · " + count + "일", button.transform, 60, 12,
                copyWidth, 18, ForestUi.Ink, true) + 5;
            height += AlbumCopy("정령 사이에 남겨 둔 나의 생활 기록", button.transform, 60, height,
                copyWidth, 14, ForestUi.Muted) + 14;
            _ui.Icon(ForestIcon.ChevronRight, button.transform, _width - 79, 26, 18, 18, ForestUi.Muted);
            button.GetComponent<RectTransform>().sizeDelta = new Vector2(_width - 48, Mathf.Max(80, height));
            return y + Mathf.Max(80, height) + 18;
        }

        private void OpenWaitingMemories()
        {
            var sheet = Modal("동행을 기다린 날", 420);
            float y = SheetParagraph(sheet, "새 동행을 기다리는 동안에도 내 생활은 이어졌어요. 모든 달의 기록을 모아 두었어요.", 4, 18, ForestUi.Ink) + 20;
            var memories = WaitingMemories();
            if (memories.Count == 0)
                y = SheetParagraph(sheet, "이 계정에 보관된 동행 대기 기록이 아직 없어요.", y, 18) + 18;
            foreach (var memory in memories)
            {
                string dateKey = memory.Date;
                var todos = (memory.Todos ?? new List<ForestDailyTodo>()).Where(t => t != null).ToList();
                int waitingDone = todos.Count(t => t.IsCompleted && t.AwaitingCompanion);
                int planned = todos.Count(t => !t.IsCompleted);
                var row = _ui.Button("WaitingDay-" + dateKey, "", sheet, SheetInset, y, SheetInnerWidth, 92,
                    () => OpenWaitingMemory(dateKey));
                row.GetComponent<Image>().color = AlbumPaper;
                float height = 12 + AlbumCopy(dateKey.Replace('-', '.'), row.transform, 14, 12,
                    SheetInnerWidth - 60, 21, ForestUi.Ink, true) + 5;
                height += AlbumCopy("동행을 기다리는 완료 " + waitingDone + "개 · 예정 " + planned + "개", row.transform, 14, height,
                    SheetInnerWidth - 60, 15, ForestUi.Muted) + 14;
                _ui.Icon(ForestIcon.ChevronRight, row.transform, SheetInnerWidth - 34, 27, 18, 18, ForestUi.Moss);
                row.GetComponent<RectTransform>().sizeDelta = new Vector2(SheetInnerWidth, Mathf.Max(88, height));
                y += Mathf.Max(88, height) + 12;
            }
            SheetContentHeight(sheet, y + 8);
            SheetPrimaryAction(sheet, "CloseWaitingMemories", "앨범으로 돌아가기", CloseModal);
        }

        private void OpenWaitingMemory(string dateKey)
        {
            // Look up the current account again on every click; do not keep a stale account's object.
            var memory = WaitingMemories().FirstOrDefault(d => d.Date == dateKey);
            if (memory == null) { OpenWaitingMemories(); return; }
            DateTime date = DateTime.ParseExact(dateKey, "yyyy-MM-dd", CultureInfo.InvariantCulture);
            var sheet = Modal(date.ToString("M월 d일") + " · 나의 생활 기록", 530);
            var todos = (memory.Todos ?? new List<ForestDailyTodo>()).Where(t => t != null).ToList();
            int waitingDone = todos.Count(t => t.IsCompleted && t.AwaitingCompanion);
            float y = SheetParagraph(sheet, "새 동행을 기다리며 남긴 하루예요. 정령이 함께한 장면은 기록하지 않았어요.", 4, 19, ForestUi.Ink) + 16;
            y = SheetParagraph(sheet, "동행을 기다리는 완료 " + waitingDone + "개\n정령에게 이어진 완료 "
                + todos.Count(t => t.IsCompleted && !t.AwaitingCompanion) + "개", y, 21, ForestUi.Moss) + 10;
            y = SheetParagraph(sheet, "최종 확인 " + AlbumTimestamp(memory.LastConfirmedAtUtc) + "\n기록 버전 " + AlbumRevision(memory.Revision), y, 13) + 20;
            if (todos.Count == 0)
                y = SheetParagraph(sheet, "확인된 일정이 없는 날이에요.", y, 18) + 18;
            foreach (var todo in todos)
            {
                var entry = _ui.Panel("WaitingTodo-" + todo.Id, sheet, SheetInset, y, SheetInnerWidth, 100, AlbumPaper);
                float height = 12 + AlbumCopy(todo.Title, entry.transform, 14, 12, SheetInnerWidth - 28, 19, ForestUi.Ink, true) + 6;
                string status = !todo.IsCompleted ? "예정" : todo.AwaitingCompanion ? "완료 · 동행 대기 중" : "완료 · 정령에게 이어진 기록";
                height += AlbumCopy(status + "\n" + ForestStoryRules.CategoryName(AlbumCategory(todo.Category))
                    + (todo.IsHabit ? " · 습관" : " · 일정"), entry.transform, 14, height, SheetInnerWidth - 28, 15, ForestUi.Muted) + 12;
                if (todo.IsCompleted && !todo.AwaitingCompanion)
                    height += AlbumCopy("이 완료는 정령의 기록에 연결되어 있어요. 현재 동행을 기다리는 완료 수에는 포함하지 않아요.",
                        entry.transform, 14, height, SheetInnerWidth - 28, 14, ForestUi.Moss) + 12;
                entry.rectTransform.sizeDelta = new Vector2(SheetInnerWidth, height);
                y += height + 12;
            }

            y += 10;
            y += AlbumCopy("이날 계정에 보관된 획득", sheet, SheetInset, y, SheetInnerWidth, 22, ForestUi.Ink, true) + 8;
            y = SheetParagraph(sheet, "이날 계정 누적 정수 " + memory.TotalEssence, y, 21, ForestUi.Moss) + 8;
            y = SheetParagraph(sheet, "정령에게 연결된 완료와 동행 대기 중 완료의 정수가 모두 포함된, 당시 저장된 금액이에요.", y, 15) + 12;
            var acquisitions = (memory.Acquisitions ?? new List<string>()).Where(a => !string.IsNullOrEmpty(a)).Distinct().ToList();
            foreach (string acquisition in acquisitions)
                y = SheetParagraph(sheet, "· " + acquisition, y, 17, ForestUi.Ink) + 8;
            y = SheetParagraph(sheet, "보관된 결과를 읽는 페이지예요. 다시 열어도 정수가 추가되지 않아요.", y + 6, 14) + 18;
            SheetContentHeight(sheet, y);
            SheetPrimaryAction(sheet, "BackToWaitingMemories", "다른 날짜 펼치기", OpenWaitingMemories);
            if (!PrototypeBootstrap.Current.IsDemo) _app.RequestForestHistory(date.ToString("yyyy-MM", CultureInfo.InvariantCulture));
        }
    }
}
