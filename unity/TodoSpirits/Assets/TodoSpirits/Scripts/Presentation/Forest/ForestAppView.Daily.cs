using System;
using System.Collections;
using System.Linq;
using TodoSpirits.Core;
using TodoSpirits.Runtime;
using UnityEngine;
using UnityEngine.UI;

namespace TodoSpirits.Presentation.Forest
{
    public sealed partial class ForestAppView
    {
        private bool _dailyRefreshPending;
        private int _demoFamily;
        private float _nextSpiritTap;

        private IEnumerator OpenFirstDay()
        {
            if (_openingToday) yield break;
            _openingToday = true;
            yield return null;
            yield return new WaitForSecondsRealtime(.15f);
            _openingToday = false;
            if (_app == null || _modal != null) yield break;
            var today = _app.GetForestTodaySummary();
            string key = today.AccountId + "/" + today.Date + "/" + today.SpiritId;
            if (today.IsFirstVisit && _openedDay != key) { _openedDay = key; TodaySummary(); }
        }

        private void OnApplicationChanged()
        {
            if (_app != null) _app.ForestTodayChanged -= OnTodayChanged;
            _app = PrototypeBootstrap.Current.Application;
            _app.ForestTodayChanged += OnTodayChanged;
            _openedDay = null; _displayedToday = null; _dailyRefreshPending = false;
            _selectedSpirit = _app.CurrentLife.SpiritId;
            _month = new DateTime(_app.CurrentDate.Year, _app.CurrentDate.Month, 1);
            _tab = 1; Render();
            StartCoroutine(OpenFirstDay());
        }

        private void OnTodayChanged()
        {
            if (_safe == null) return;
            if (_modalMotion != null && _modalMotion.IsClosing) { _dailyRefreshPending = true; return; }
            if (_showingToday)
            {
                _dailyRefreshPending = true;
                float offset = 0;
                var old = _modal.GetComponentInChildren<ScrollRect>();
                if (old != null) offset = old.content.anchoredPosition.y;
                TodaySummary();
                var scroll = _modal.GetComponentInChildren<ScrollRect>();
                Canvas.ForceUpdateCanvases();
                if (scroll != null) scroll.content.anchoredPosition = new Vector2(0, Mathf.Clamp(offset, 0,
                    Mathf.Max(0, scroll.content.rect.height - scroll.viewport.rect.height)));
            }
            else if (_modal == null)
            {
                Render();
                Toast("오늘 기록이 갱신됐어요. 오늘의 우리에서 확인해 보세요.");
                StartCoroutine(OpenFirstDay());
            }
            else { _dailyRefreshPending = true; Toast("오늘 기록이 갱신됐어요."); }
        }

        private void TodaySummary()
        {
            var today = _app.GetForestTodaySummary();
            var content = Modal(_app.CurrentDate.ToString("M월 d일") + " · 오늘의 우리", 660);
            _showingToday = true; _displayedToday = today;
            _beforeModalClose = () => _app.AcknowledgeForestToday(_displayedToday);
            if (!_app.CurrentLife.NamingOffered && string.IsNullOrEmpty(_app.CurrentLife.Name)) _afterModalClose = EarlyNameDialog;
            var sheet = (RectTransform)content.parent.parent;
            sheet.GetComponent<Button>().onClick.AddListener(CloseModal);
            var scroll = content.parent.GetComponent<ScrollRect>();
            // Rows have their own click target; dragging remains the ScrollRect's gesture.
            float y = 4;
            bool fresh = today.SyncState == ForestSyncState.Confirmed || today.SyncState == ForestSyncState.Local;
            var status = _ui.Panel("TodayConnection", content, SheetInset, y, SheetInnerWidth, 50,
                fresh ? new Color32(227, 237, 215, 255) : new Color32(248, 235, 211, 255));
            _ui.Icon(fresh ? ForestIcon.Check : ForestIcon.Today, status.transform, 12, 14, 21, 21, ForestUi.Moss);
            _ui.Text(PrototypeBootstrap.Current.IsDemo ? "예시 하루 · 실제 일정과 별도로 저장돼요" : today.SyncMessage,
                status.transform, 44, 8, SheetInnerWidth - 56, 38, 17, ForestUi.Ink, false, TextAnchor.MiddleLeft);
            y += 66;
            if (!fresh)
            {
                string message = today.HasConfirmedData ? "마지막으로 확인한 기록을 보여드려요. 최신 완료 상태는 아직 확인하지 못했어요." :
                    "아직 오늘의 목록을 받지 못했어요. 연결 전에도 정령을 만나고 숲을 둘러볼 수 있어요.";
                y = SheetParagraph(content, message, y, 19, ForestUi.Ink) + 10;
                if (!string.IsNullOrEmpty(today.LastConfirmedAtUtc) && DateTime.TryParse(today.LastConfirmedAtUtc, out var confirmed))
                    y = SheetParagraph(content, "마지막 확인 · " + confirmed.ToLocalTime().ToString("M/d HH:mm"), y, 16) + 8;
                _ui.Button("RetryToday", "오늘 기록 다시 확인", content, SheetInset, y, SheetInnerWidth, 56,
                    () => _app.RequestForestSnapshot(), false, 19);
                y += 70;
                if (!_app.IsHostConnected && !PrototypeBootstrap.Current.IsDemo)
                {
                    _ui.Button("BeginExampleDay", "예시 하루 체험하기", content, SheetInset, y, SheetInnerWidth, 58,
                        () => PrototypeBootstrap.Current.BeginPrototypeDemo(), true, 20);
                    y += 75;
                }
            }

            y = DailyHeading(content, "내 오늘", today.HasConfirmedData ? today.Todos.Count(t => t.IsCompleted) + " / " + today.Todos.Count + " 완료" : "확인 대기", y);
            if (today.Todos.Count == 0)
                y = SheetParagraph(content, today.HasConfirmedData ? "오늘 등록된 일정은 없어요. 정령은 숲에서 자기만의 하루를 보내요." :
                    "연결된 일정이 도착하면 원래 제목과 완료 상태를 여기에 보여드릴게요.", y, 20, ForestUi.Ink) + 20;
            foreach (var todo in today.Todos)
            {
                var row = _ui.Button("TodayTodo-" + todo.Id, "", content, SheetInset, y, SheetInnerWidth, 100,
                    () => DailyTodoDetail(todo), false, 20);
                row.GetComponent<Image>().color = todo.IsCompleted ? new Color32(235, 241, 224, 255) : Color.white;
                _ui.Icon(todo.IsCompleted ? ForestIcon.Check : ForestIcon.Today, row.transform, 14, 18, 24, 24, ForestUi.Moss);
                float rowY = 13 + CopyHeight(todo.Title, row.transform, 52, 13, SheetInnerWidth - 70, 22, ForestUi.Ink, true);
                rowY += 5 + CopyHeight((todo.IsHabit ? "습관" : "일정") + " · " + ForestStoryRules.CategoryName(todo.Category) +
                    " · " + DailyTodoState(todo), row.transform, 52, rowY + 5, SheetInnerWidth - 70, 17, ForestUi.Muted);
                rowY += 14;
                row.GetComponent<RectTransform>().sizeDelta = new Vector2(SheetInnerWidth, rowY);
                y += rowY + 10;
            }
            y += 12;
            y = DailyHeading(content, "정령의 하루", "내 기록과 이어지는 생활", y);
            foreach (var activity in today.Activities)
            {
                var row = _ui.Panel("SpiritSchedule-" + activity.ActivityKey, content, SheetInset, y, SheetInnerWidth, 100, SheetBlush);
                _ui.Icon(ActivityIcon(activity.ActivityKey), row.transform, 15, 18, 30, 30, ForestUi.Rose);
                float rowY = 13 + CopyHeight(activity.Location + " · " + (activity.IsAutonomous ? "정령의 시간" : activity.IsCompleted ? "함께 한 일" : "준비 중"),
                    row.transform, 57, 13, SheetInnerWidth - 74, 18, ForestUi.Rose, true);
                rowY += 7 + CopyHeight(activity.Activity, row.transform, 57, rowY + 7, SheetInnerWidth - 74, 22, ForestUi.Ink, true);
                rowY += 7 + CopyHeight(activity.Reason, row.transform, 57, rowY + 7, SheetInnerWidth - 74, 18, ForestUi.Muted);
                rowY += 16; row.rectTransform.sizeDelta = new Vector2(SheetInnerWidth, rowY); y += rowY + 12;
            }
            y += 8;
            y = DailyHeading(content, "오늘 획득", "기록에 이미 담아 두었어요", y);
            var reward = _ui.Panel("DailyRewards", content, SheetInset, y, SheetInnerWidth, 98, new Color32(239, 232, 209, 255));
            _ui.Icon(ForestIcon.Leaf, reward.transform, 17, 20, 30, 30, ForestUi.Moss);
            SheetLabel("오늘 누적 " + today.TotalEssence + " 정수", reward.transform, 60, 12, SheetInnerWidth - 76, 25, ForestUi.Moss, true);
            _ui.Text(today.NewEssence > 0 ? "이번에 새로 +" + today.NewEssence : "새로 획득한 정수는 없어요", reward.transform,
                60, 56, SheetInnerWidth - 76, 29, 19, ForestUi.Ink);
            y += 115;
            foreach (string acquisition in today.Acquisitions) y = SheetParagraph(content, "• " + acquisition, y, 20, ForestUi.Ink) + 10;
            y = SheetParagraph(content, "오늘의 기록은 언제든 다시 펼칠 수 있어요.", y + 8, 17) + 12;
            SheetContentHeight(content, y);
            SheetPrimaryAction(content, "EnterForest", "숲에서 정령 만나기", CloseModal);
        }

        private float DailyHeading(RectTransform content, string title, string detail, float y)
        {
            y += CopyHeight(title, content, SheetInset, y, SheetInnerWidth, 26, ForestUi.Ink, true) + 4;
            y = SheetParagraph(content, detail, y, 17) + 14;
            return y;
        }

        private void DailyTodoDetail(ForestDailyTodo todo)
        {
            var content = Modal("나의 약속, 정령의 생활", 400);
            _afterModalClose = TodaySummary;
            float y = SheetParagraph(content, todo.Title, 5, 25, ForestUi.Ink) + 15;
            if (todo.CompletedWithPreviousSpirit || todo.AwaitingCompanion)
                y = SheetParagraph(content, DailyTodoState(todo) + ". 원래 완료 기록과 정수는 유지돼요.", y, 19, ForestUi.Moss) + 12;
            y = SheetParagraph(content, (todo.IsCompleted ? "완료한 " : "예정된 ") + (todo.IsHabit ? "습관" : "일정") + " · " + ForestStoryRules.CategoryName(todo.Category), y, 18) + 25;
            y = SheetParagraph(content, todo.Location + "에서\n" + todo.Activity, y, 24, ForestUi.Moss) + 18;
            y = SheetParagraph(content, todo.Reason, y, 20, ForestUi.Ink) + 20;
            SheetContentHeight(content, y);
            SheetPrimaryAction(content, "BackToToday", "오늘의 우리로 돌아가기", TodaySummary);
        }

        private static ForestIcon ActivityIcon(string key)
        {
            switch (key) { case "read": return ForestIcon.Book; case "walk": return ForestIcon.Compass;
                case "craft": return ForestIcon.Flower; case "tea": return ForestIcon.Heart; default: return ForestIcon.Leaf; }
        }

        private static string DailyTodoState(ForestDailyTodo todo)
        {
            if (!todo.IsCompleted) return "오늘의 계획";
            if (todo.CompletedWithPreviousSpirit) return "이전 정령과 한 일";
            if (todo.AwaitingCompanion) return "완료 · 다음 동행을 기다려요";
            return "함께 해냈어요";
        }

        private void TalkToSpirit(ForestSpiritAnimator actor)
        {
            if (Time.unscaledTime < _nextSpiritTap) return;
            _nextSpiritTap = Time.unscaledTime + .45f;
            string line = _app.NextForestDialogue();
            actor.RespondToTouch(_app.GetForestTodaySummary().Mood);
            if (_spiritSpeech != null) _spiritSpeech.text = line;
            else Toast(line);
        }

        private void PrototypeExperience()
        {
            var content = Modal("예시 하루 체험", 640);
            float y = SheetParagraph(content, "아래 일정은 프로토타입 체험을 위한 예시예요. 실제 계정의 일정·정수·정령과 별도로 저장됩니다.", 4, 20, ForestUi.Ink) + 20;
            if (!PrototypeBootstrap.Current.IsDemo)
            {
                SheetContentHeight(content, y);
                SheetPrimaryAction(content, "StartDemo", "별도의 예시 숲 열기", () => PrototypeBootstrap.Current.BeginPrototypeDemo());
                return;
            }
            string[] families = { "읽고 배우는 하루", "움직이고 정돈하는 하루", "관계와 쉼이 있는 하루" };
            for (int i = 0; i < families.Length; i++)
            {
                int family = i;
                _ui.Button("ExampleFamily" + i, families[i], content, SheetInset, y, SheetInnerWidth, 58,
                    () => { _demoFamily = family; PrototypeBootstrap.Current.ApplyDemoDay(family, 1); Render(); TodaySummary(); }, false, 20);
                y += 70;
            }
            y = SheetParagraph(content, "같은 날의 완료 상태를 바꿔 볼 수 있어요.", y + 4, 18) + 12;
            int[] counts = { 0, 1, 2, 4 };
            float cell = (SheetInnerWidth - 24) / 4;
            for (int i = 0; i < counts.Length; i++)
            {
                int count = counts[i];
                _ui.Button("ExampleCompleted" + count, count + "개", content, SheetInset + i * (cell + 8), y, cell, 56,
                    () => { PrototypeBootstrap.Current.ApplyDemoDay(_demoFamily, count); Render(); TodaySummary(); }, false, 20);
            }
            y += 80;
            _ui.Button("NextExampleDay", "다음 예시 날짜로 · 하루 이어 보기", content, SheetInset, y, SheetInnerWidth, 64,
                () => { PrototypeBootstrap.Current.ApplyDemoDay(_demoFamily, 2, true); _openedDay = null; Render(); TodaySummary(); }, true, 19);
            y += 82;
            _ui.Button("EmptyExampleDay", "정상적인 일정 0개 확인", content, SheetInset, y, SheetInnerWidth, 56,
                () => { PrototypeBootstrap.Current.ApplyDemoDay(_demoFamily, 0, false, 0); Render(); TodaySummary(); }, false, 18);
            y += 70;
            _ui.Button("OfflineExample", "연결이 끊긴 상태 확인", content, SheetInset, y, SheetInnerWidth, 56,
                () => { _app.SetForestSyncState(ForestSyncState.Offline); Render(); TodaySummary(); }, false, 18);
            y += 74;
            SheetContentHeight(content, y);
            SheetPrimaryAction(content, "LeaveDemo", "내 숲으로 돌아가기", () => PrototypeBootstrap.Current.EndPrototypeDemo());
        }

        private void EarlyNameDialog()
        {
            if (_app.CurrentLife.Farewell == FarewellStep.Independent) { Eggs(); return; }
            var content = Modal("너를 뭐라고 부를까?", 490);
            var stage = SheetStage(content, 4, 170);
            SheetActor(stage, (SheetInnerWidth - 136) * .5f, 15, 136, "wave");
            float y = SheetParagraph(content, "오늘부터 함께할 정령에게 이름을 붙여 주세요. 천천히 생각해도 괜찮아요.", 190, 21, ForestUi.Ink) + 18;
            var input = _ui.Input(content, SheetInset, y, SheetInnerWidth, "정령의 이름", 12);
            input.text = _app.CurrentLife.Name;
            y += 79;
            var error = SheetValidation(content, y, 48); y += 56;
            _ui.Button("NameLater", "조금 더 함께한 뒤 정할게", content, SheetInset, y, SheetInnerWidth, 52,
                () => { try { _app.SkipCompanionNaming(); CloseModal(); } catch (Exception e) { error.text = e.Message; } }, false, 18);
            y += 69; SheetContentHeight(content, y);
            SheetPrimaryAction(content, "NameFirstFriend", "이 이름으로 불러 줄게", () =>
            {
                if (CommitDialog(() => _app.SetCompanionNameEarly(input.text), error))
                { _actor?.Perform("happy"); if (_spiritSpeech != null) _spiritSpeech.text = "내 이름은 " + _app.CurrentLife.DisplayName + "! 우리, 천천히 친해지자."; }
            });
        }

        private void BeginNextCompanionship()
        {
            var content = Modal("우리의 다음 계절", 330);
            float y = SheetParagraph(content, "지금의 정령과 계속 지내도 좋아요. 새로운 동행을 시작하면 함께한 날을 돌아보고, 정령을 배웅한 뒤 다음 친구를 만나게 돼요. 추억은 앨범에 남아요.", 8, 22, ForestUi.Ink) + 22;
            _ui.Button("StayTogether", "계속 함께 지낼래", content, SheetInset, y, SheetInnerWidth, 58, CloseModal, false, 20);
            SheetContentHeight(content, y + 78);
            SheetPrimaryAction(content, "BeginNextSeason", "함께한 날을 돌아볼래", () => {
                try { _app.BeginFarewell(); Render(); FarewellDialogue(); } catch (Exception e) { Toast(e.Message); }
            });
        }

        private void ReturnToTodoApp()
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            try { using (var player = new AndroidJavaClass("com.unity3d.player.UnityPlayer"))
                using (var activity = player.GetStatic<AndroidJavaObject>("currentActivity")) activity.Call("requestForestClose"); }
            catch (Exception) { CloseModal(); }
#else
            CloseModal();
#endif
        }
    }
}
