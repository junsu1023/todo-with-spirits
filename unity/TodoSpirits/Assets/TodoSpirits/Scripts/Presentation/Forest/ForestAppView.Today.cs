using System;
using System.Collections;
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
        private bool _todoCommitPending;
        private bool _addingSuggestedTodo;

        private float Today()
        {
            TodayWeek();
            var todos = _app.TodayTodos;
            var pending = todos.Where(t => !t.CompletedDates.Contains(_app.CurrentDateKey)).ToList();
            var completed = todos.Where(t => t.CompletedDates.Contains(_app.CurrentDateKey)).ToList();
            int done = completed.Count;
            float y = TodayCompanionMemo(79, done) + 19;
            if (_app.CurrentLife.Farewell == FarewellStep.Independent) y = TodayNextCompanionNotice(y);
            var heading = TodayLabel("오늘의 약속", _content, 24, y, _width - 140, 24, ForestUi.Ink, true);
            TodayLabel(done + " / " + todos.Count, _content, _width - 108, y + 3, 84, 19, ForestUi.Muted, false, TextAnchor.UpperRight);
            y += heading.rectTransform.rect.height + 13;
            _ui.Divider(_content, 24, y, _width - 48, new Color32(205, 215, 201, 255));
            y += 6;
            if (_app.IsHostConnected)
                y += CopyHeight("TODO 앱에서 수정 · 마지막 연결 " + _app.SaveData.Forest.HostDate, _content, 24, y + 10, _width - 48, 16, ForestUi.Muted) + 24;

            if (todos.Count == 0) return TodayEmpty(y + 10);

            foreach (var todo in pending) y = TodoRow(todo, y, false);
            if (pending.Count == 0)
            {
                _ui.Icon(ForestIcon.Sun, _content, 25, y + 19, 28, 28, ForestUi.Moss);
                float textHeight = CopyHeight("오늘의 약속을 모두 마쳤어요", _content, 65, y + 16, _width - 89, 20, ForestUi.Moss, true);
                y += textHeight + 25;
                y += CopyHeight("남은 시간은 편안하게 보내도 좋아요.", _content, 65, y, _width - 89, 17, ForestUi.Muted) + 28;
            }

            if (completed.Count > 0)
            {
                y += 14;
                var toggle = PlainTodayButton("CompletedToggle", _content, 20, y, _width - 40, 58, () =>
                {
                    _showCompleted = !_showCompleted;
                    Render();
                });
                TodayLabel("마친 약속  " + done, toggle.transform, 4, 15, _width - 102, 18, ForestUi.Muted, true);
                var arrow = _ui.Icon(ForestIcon.ChevronRight, toggle.transform, _width - 77, 19, 20, 20, ForestUi.Muted);
                arrow.rectTransform.pivot = new Vector2(.5f, .5f);
                arrow.rectTransform.anchoredPosition += new Vector2(10, -10);
                arrow.rectTransform.localRotation = Quaternion.Euler(0, 0, _showCompleted ? -90 : 0);
                y += 59;
                if (_showCompleted) foreach (var todo in completed) y = TodoRow(todo, y, true);
            }

            if (_app.HasForestChoice)
            {
                y += 22;
                var invitation = PlainTodayButton("StoryFromToday", _content, 24, y, _width - 48, 80, () => ShowTab(1));
                _ui.Icon(ForestIcon.Mail, invitation.transform, 4, 11, 28, 28, ForestUi.Rose);
                float titleHeight = CopyHeight("정령이 나누고 싶은 이야기가 있어요", invitation.transform, 47, 1, _width - 120, 18, ForestUi.Rose, true);
                float subtitleHeight = CopyHeight("숲으로 잠깐 놀러 갈까요?", invitation.transform, 47, titleHeight + 7, _width - 120, 16, ForestUi.Muted);
                float invitationHeight = titleHeight + subtitleHeight + 18;
                invitation.GetComponent<RectTransform>().sizeDelta = new Vector2(_width - 48, Mathf.Max(64, invitationHeight));
                _ui.Icon(ForestIcon.ChevronRight, invitation.transform, _width - 71, 14, 20, 20, ForestUi.Rose);
                y += Mathf.Max(64, invitationHeight);
            }
            return y + 12;
        }

        private void TodayWeek()
        {
            var day = _app.CurrentDate;
            var monday = day.AddDays(-((int)day.DayOfWeek + 6) % 7);
            string[] dayNames = { "월", "화", "수", "목", "금", "토", "일" };
            float inset = Mathf.Clamp((_width - 308) / 2, 6, 24);
            float cell = (_width - inset * 2) / 7;
            for (int i = 0; i < 7; i++)
            {
                var date = monday.AddDays(i);
                bool current = date.Date == day.Date;
                bool future = date.Date > day.Date;
                var button = PlainTodayButton("JournalDay-" + date.ToString("yyyy-MM-dd", CultureInfo.InvariantCulture), _content,
                    inset + cell * i, 0, cell, 72, () => OpenDayJournal(date));
                button.interactable = !future;
                Color muted = future ? (Color)new Color32(168, 176, 166, 255) : ForestUi.Muted;
                TodayLabel(dayNames[i], button.transform, 0, 0, cell, 15, muted, false, TextAnchor.UpperCenter);
                if (current) _ui.Panel("CurrentDay", button.transform, (cell - 37) / 2, 25, 37, 36, ForestUi.Moss);
                TodayLabel(date.Day.ToString(), button.transform, 0, 29, cell, 20,
                    current ? ForestUi.Paper : future ? muted : ForestUi.Ink, current, TextAnchor.UpperCenter);
                bool recorded = _app.SaveData.Records.Any(r => r.Date == date.ToString("yyyy-MM-dd", CultureInfo.InvariantCulture) && r.CompletedTasks.Count > 0);
                if (recorded) _ui.Panel("RecordedDay", button.transform, cell / 2 - 2, 67, 4, 4, ForestUi.Moss).raycastTarget = false;
            }
        }

        private float TodayCompanionMemo(float y, int done)
        {
            string text;
            var known = _app.CurrentCompletedTasks.Where(t => t.UserCategory != UserTaskCategory.Unspecified)
                .GroupBy(t => t.UserCategory).OrderByDescending(g => g.Count()).FirstOrDefault();
            bool independent = _app.CurrentLife.Farewell == FarewellStep.Independent;
            if (independent) text = "함께한 시간은 앨범에 남아 있어요.";
            else if (_app.HasForestChoice) text = "함께 고르고 싶은 게 생겼어. 숲에서 만나!";
            else if (done == 0) text = "작은 약속 하나, 나도 함께할게.";
            else if (known == null) text = "이름 붙이지 않은 하루도 기억할게.";
            else text = ForestStoryRules.CategoryName(known.Key) + "에 쓴 시간, 나도 기억하고 있어.";

            var memo = _ui.Rect("CompanionMemo", _content, 24, y, _width - 48, 66);
            if (independent) _ui.Icon(ForestIcon.Mail, memo, 13, 15, 37, 37, ForestUi.Rose);
            else _actor = Actor(memo, -7, -5, 74);
            float textHeight = CopyHeight(text, memo, 80, 12, _width - 135, 17, ForestUi.Moss);
            float height = Mathf.Max(64, textHeight + 24);
            memo.sizeDelta = new Vector2(_width - 48, height);
            return y + height;
        }

        private float TodayNextCompanionNotice(float y)
        {
            y += CopyHeight("다음 친구와 약속을 이어가요", _content, 24, y, _width - 48, 21, ForestUi.Moss, true) + 9;
            y += CopyHeight("아래의 약속과 지난 기록은 그대로예요.\n새 정령을 맞이하면 다시 적고 체크할 수 있어요.", _content, 24, y, _width - 48, 17, ForestUi.Muted) + 13;
            var welcome = PlainTodayButton("TodayWelcomeCompanion", _content, 24, y, _width - 48, 58, Eggs);
            TodayLabel("새로운 알 고르기", welcome.transform, 0, 13, _width - 91, 19, ForestUi.Moss, true);
            _ui.Icon(ForestIcon.ArrowRight, welcome.transform, _width - 79, 16, 24, 24, ForestUi.Moss);
            return y + 78;
        }

        private float TodayEmpty(float y)
        {
            y += 18;
            y += CopyHeight(_app.IsHostConnected ? "오늘의 기록을 기다리고 있어요" : "가볍게 시작해 볼까요?", _content, 24, y, _width - 48, 23, ForestUi.Ink, true) + 12;
            if (_app.IsHostConnected)
                return y + CopyHeight("연결된 TODO 앱에서 일정을 보내면\n나의 약속과 정령의 하루가 이곳에 함께 남아요.", _content, 24, y, _width - 48, 18, ForestUi.Muted) + 28;
            if (_app.CurrentLife.Farewell == FarewellStep.Independent)
                return y + CopyHeight("다음 친구를 만난 뒤\n오늘의 첫 약속을 함께 적어 보세요.", _content, 24, y, _width - 48, 18, ForestUi.Muted) + 28;

            y += CopyHeight("하나를 누르면 오늘의 약속에 추가돼요.\n완료는 직접 해낸 뒤 표시해 주세요.", _content, 24, y, _width - 48, 17, ForestUi.Muted) + 23;
            string[] suggestions = { "물 한 잔 마시기", "5분 산책하기", "책 한 쪽 읽기" };
            var categories = new[] { UserTaskCategory.Health, UserTaskCategory.Health, UserTaskCategory.WorkStudy };
            var icons = new[] { ForestIcon.Leaf, ForestIcon.Compass, ForestIcon.Book };
            for (int i = 0; i < suggestions.Length; i++)
            {
                string title = suggestions[i]; var category = categories[i];
                var button = PlainTodayButton("SuggestedTodo" + i, _content, 24, y, _width - 48, 67,
                    () => AddSuggestedTodayTodo(title, category));
                _ui.Icon(icons[i], button.transform, 3, 19, 26, 26, ForestUi.Moss);
                var label = TodayLabel(title, button.transform, 48, 17, _width - 147, 20, ForestUi.Ink);
                float height = Mathf.Max(67, label.rectTransform.rect.height + 34);
                button.GetComponent<RectTransform>().sizeDelta = new Vector2(_width - 48, height);
                _ui.Icon(ForestIcon.Plus, button.transform, _width - 77, (height - 22) / 2, 22, 22, ForestUi.Moss);
                _ui.Divider(button.transform, 47, height - 1, _width - 95);
                y += height;
            }
            y += 20;
            return y + CopyHeight("나만의 약속은 아래에서 직접 적을 수 있어요.", _content, 24, y, _width - 48, 16, ForestUi.Muted) + 18;
        }

        private void AddSuggestedTodayTodo(string title, UserTaskCategory category)
        {
            if (_addingSuggestedTodo || _todoCommitPending) return;
            _addingSuggestedTodo = true;
            try
            {
                _app.SynchronizeLifeDate(PrototypeBootstrap.Current.ResolveToday());
                if (_app.TodayTodos.Any(t => t.Title == title && !t.Habit)) return;
                _app.AddForestTodo(title, category, false);
                ResetPageScroll();
                Render();
                Toast("오늘의 약속에 더했어요. 해낸 뒤 체크해 주세요.");
            }
            catch (Exception exception) { Toast(exception.Message); }
            finally { _addingSuggestedTodo = false; }
        }

        private float TodoRow(ForestTodo todo, float y, bool complete)
        {
            float rowWidth = _width - 48, textWidth = rowWidth - 68;
            var row = _ui.Panel("Todo-" + todo.Id, _content, 24, y, rowWidth, 84, Color.clear, false);
            var title = TodayLabel(todo.Title, row.transform, 62, 13, textWidth, 21, complete ? ForestUi.Muted : ForestUi.Ink);
            float titleHeight = title.rectTransform.rect.height;
            float metaY = 13 + titleHeight + 7;
            string category = ForestStoryRules.CategoryName(todo.Category) + (todo.Habit ? " · 매일 습관" : "");
            var meta = TodayLabel(category, row.transform, 77, metaY, rowWidth - 83, 16, ForestUi.Muted);
            _ui.Panel("CategoryDot", row.transform, 63, metaY + 7, 7, 7, CategoryTint(todo.Category)).raycastTarget = false;
            float rowHeight = Mathf.Max(84, metaY + meta.rectTransform.rect.height + 16);
            row.rectTransform.sizeDelta = new Vector2(rowWidth, rowHeight);
            var edit = row.gameObject.AddComponent<Button>();
            edit.targetGraphic = row;
            edit.transition = Selectable.Transition.None;
            edit.onClick.AddListener(() =>
            {
                if (_todoCommitPending) return;
                if (_app.IsHostConnected) Toast("연결된 TODO 앱에서 일정을 수정해 주세요.");
                else if (_app.CurrentLife.Farewell == FarewellStep.Independent) Toast("다음 친구를 맞이하면 약속을 다시 적고 체크할 수 있어요.");
                else EditTodo(todo);
            });
            _ui.Divider(row.transform, 62, rowHeight - 1, rowWidth - 62, new Color32(225, 230, 219, 255));

            Button check = null;
            string shownDate = _app.CurrentDateKey;
            var checkRoot = _ui.Rect("CheckVisual", row.transform, 13, (rowHeight - 29) / 2, 29, 29);
            var box = _ui.Panel("Checkbox", checkRoot, 0, 0, 29, 29, complete ? ForestUi.Moss : ForestUi.Paper);
            var border = box.gameObject.AddComponent<Outline>();
            border.effectColor = complete ? ForestUi.Moss : new Color32(160, 179, 158, 255);
            border.effectDistance = new Vector2(.9f, -.9f);
            var mark = _ui.Icon(ForestIcon.Check, checkRoot, 5, 5, 19, 19, ForestUi.Paper);
            mark.gameObject.SetActive(complete);
            box.raycastTarget = false;
            check = PlainTodayButton("Complete", row.transform, -2, (rowHeight - 62) / 2, 60, 62,
                () => CompleteTodayTodo(todo.Id, shownDate, complete, check, checkRoot, box, border, mark, title));
            check.interactable = !_app.IsHostConnected && _app.CurrentLife.Farewell != FarewellStep.Independent;
            if (!check.interactable)
            {
                var disabled = checkRoot.gameObject.AddComponent<CanvasGroup>(); disabled.alpha = .62f;
            }
            return y + rowHeight;
        }

        private void CompleteTodayTodo(string id, string shownDate, bool wasComplete, Button check, RectTransform visual, Image box, Outline border, ForestIconGraphic mark, Text title)
        {
            if (_todoCommitPending) return;
            _todoCommitPending = true;
            try
            {
                bool newDay = _app.SynchronizeLifeDate(PrototypeBootstrap.Current.ResolveToday());
                if (newDay || shownDate != _app.CurrentDateKey)
                {
                    _todoCommitPending = false;
                    Render();
                    Toast("새로운 하루가 시작됐어요. 오늘의 약속에서 다시 체크해 주세요.");
                    return;
                }
                _app.ToggleForestTodo(id);
            }
            catch (Exception exception) { _todoCommitPending = false; Toast(exception.Message); return; }

            check.interactable = false;
            box.color = !wasComplete ? ForestUi.Moss : ForestUi.Paper;
            border.effectColor = !wasComplete ? ForestUi.Moss : new Color32(160, 179, 158, 255);
            mark.gameObject.SetActive(!wasComplete);
            title.color = !wasComplete ? ForestUi.Muted : ForestUi.Ink;
            if (_app.SaveData.Forest.ReduceMotion)
            {
                _todoCommitPending = false;
                Render();
                _actor?.Perform(wasComplete ? "curious" : "happy");
                return;
            }
            StartCoroutine(CompleteTodayPulse(_page, visual, !wasComplete));
        }

        private IEnumerator CompleteTodayPulse(RectTransform originalPage, RectTransform visual, bool completed)
        {
            float started = Time.unscaledTime;
            Vector2 origin = visual == null ? Vector2.zero : visual.anchoredPosition;
            const float duration = .22f;
            while (Time.unscaledTime - started < duration)
            {
                if (visual != null)
                {
                    float t = Mathf.Clamp01((Time.unscaledTime - started) / duration);
                    float scale = 1 + Mathf.Sin(t * Mathf.PI) * .12f;
                    visual.localScale = Vector3.one * scale;
                    visual.anchoredPosition = origin + new Vector2(-14.5f, 14.5f) * (scale - 1);
                }
                yield return null;
            }
            if (visual != null) { visual.localScale = Vector3.one; visual.anchoredPosition = origin; }
            _todoCommitPending = false;
            while (_page == originalPage && _modal != null) yield return null;
            if (_page == originalPage && _tab == 0)
            {
                Render();
                _actor?.Perform(completed ? "happy" : "curious");
            }
        }

        private void TodayActionDock()
        {
            var dock = _ui.Panel("TodayActionDock", _page, 0, 0, _width, 80, ForestUi.Paper, false).rectTransform;
            dock.anchorMin = dock.anchorMax = new Vector2(.5f, 0);
            dock.pivot = new Vector2(.5f, 0);
            dock.anchoredPosition = new Vector2(0, 88);
            _ui.Divider(dock, 24, 0, _width - 48, new Color32(232, 233, 222, 255));
            bool independent = _app.CurrentLife.Farewell == FarewellStep.Independent;
            var button = _ui.Button("AddTodo", independent ? "다음 친구 맞이하기" : "약속 더하기", dock, 24, 10, _width - 48, 60,
                () => { if (independent) Eggs(); else AddTodo(); }, true, 21);
            _ui.Icon(independent ? ForestIcon.Leaf : ForestIcon.Plus, button.transform, 21, 18, 24, 24, ForestUi.Paper);
            var label = button.GetComponentInChildren<Text>();
            label.rectTransform.anchoredPosition = new Vector2(56, 0);
            label.rectTransform.sizeDelta = new Vector2(_width - 124, 60);
        }

        private Button PlainTodayButton(string name, Transform parent, float x, float y, float width, float height, Action action)
        {
            var image = _ui.Panel(name, parent, x, y, width, height, Color.clear, false);
            var button = image.gameObject.AddComponent<Button>();
            button.targetGraphic = image;
            button.transition = Selectable.Transition.None;
            button.onClick.AddListener(() => action?.Invoke());
            return button;
        }

        private Text TodayLabel(string text, Transform parent, float x, float y, float width, int size, Color color, bool bold = false, TextAnchor align = TextAnchor.UpperLeft)
        {
            var label = _ui.Text(text, parent, x, y, width, 300, size, color, bold, align);
            label.lineSpacing = 1.13f;
            label.rectTransform.sizeDelta = new Vector2(width, Mathf.Ceil(label.preferredHeight) + 3);
            return label;
        }
    }
}
