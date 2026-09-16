using System;
using System.Collections;
using System.Globalization;
using TodoSpirits.Core;
using TodoSpirits.Presentation.Common;
using UnityEngine;

namespace TodoSpirits.Presentation.Main
{
    public sealed partial class MainSceneController
    {
        private CompanionMenuView _companionMenu;
        private void BuildCompanionMenu(WireframeUi ui, RectTransform parent)
        {
            _companionMenu = new CompanionMenuView(ui, parent);
            _mainView.ReserveCompanionNavigation();
            // These controls belong to the authored main screen, so other pages hide them automatically.
            ui.CreateButton(_mainView.transform, "CompanionLifeButton", "이 아이와 함께", OpenLife,
                new Vector2(.04f,.147f), new Vector2(.47f,.195f), Vector2.zero, Vector2.zero, fontSize: 26);
            ui.CreateButton(_mainView.transform, "CompanionArchiveButton", "함께한 기록", OpenArchive,
                new Vector2(.53f,.147f), new Vector2(.96f,.195f), Vector2.zero, Vector2.zero, fontSize: 26);
        }
        private void Page(string title, Action back = null)
        {
            _companionMenu.Clear(title, back ?? PopScreen);
            PushScreen(_companionMenu.Root);
        }
        private void Act(Action mutation, Action redraw, string success = null)
        {
            try { mutation(); RefreshViews(_application.CurrentRecord, false); redraw?.Invoke(); if (success != null) _companionMenu.Notice(success); }
            catch (Exception error) { _companionMenu.Notice(error.Message); }
        }
        private void OpenLife()
        {
            if (_application == null) return;
            var life = _application.CurrentLife;
            Page(life.DisplayName);
            _companionMenu.Portrait(mainScreenPrefab.SpiritActor, life);
            _companionMenu.Text(CompanionLifeRules.StageText(life), 100);
            _companionMenu.Text(CompanionStagePresentation.Hint(life), 130);
            foreach (var trait in life.Temperaments) _companionMenu.Text(CompanionTemperamentText.Describe(trait), 90);
            if (life.ActiveProject != null) _companionMenu.Text("요즘 이어가는 일 · " + life.ActiveProject.Title, 100);
            foreach (var project in life.Projects)
                if (project.IsComplete) _companionMenu.Text("마무리한 일 · " + project.Title, 85);
            switch (life.Farewell)
            {
                case FarewellStep.Growing:
                    _companionMenu.Text(life.Stage == CompanionStage.Meeting
                        ? "현실에서 마친 일들이 이 아이의 하루가 돼요. 집에서 무엇을 하는지 지켜보고, 오늘의 기록에서 두 하루를 함께 읽어 보세요. 쉬는 날에도 이 아이는 괜찮아요."
                        : "익숙한 장소와 좋아하는 일이 생기고 있어요. 매일 다른 행동과 작은 반응을 지켜봐 주세요.", 230);
                    _companionMenu.Button(life.StageEventSeen ? "함께 생활하러 돌아가기" : "새로운 모습 기억하기", () => Act(() => _application.ObserveLifeStage(), PopScreen));
                    break;
                case FarewellStep.AdultReveal:
                    _companionMenu.Text("이 아이가 찾은 길은 " + CompanionLifeRules.RouteName(life.Route) + "예요.", 110);
                    foreach (var reason in life.RouteReasons) _companionMenu.Text(reason, 100);
                    _companionMenu.Button("함께한 날 돌아보기", () => Act(_application.AdvanceLifeFarewell, OpenLife));
                    break;
                case FarewellStep.Reflection:
                    _companionMenu.Text($"{life.StartedOn}에 만나 함께한 하루들이 이 아이만의 삶이 되었어요. 이제 앞으로 기억할 이름을 선물해 주세요.", 190);
                    foreach (var reason in life.RouteReasons) _companionMenu.Text(reason, 100);
                    _companionMenu.Button("이름 선물하기", () => Act(_application.AdvanceLifeFarewell, OpenLife));
                    break;
                case FarewellStep.Naming:
                    _companionMenu.Text("앞으로 기억할 이름을 하나 선물해 주세요. 이름은 1~12자로 입력할 수 있어요.", 150);
                    var input = _companionMenu.NameInput();
                    _companionMenu.Button("이 이름으로 부르기", () => Act(() => _application.NameCurrentCompanion(input.text), PopScreen));
                    break;
                case FarewellStep.LastCompanionship:
                    _companionMenu.Text(life.Name + "와 집에서 마지막 하루를 보내 주세요.", 140);
                    _companionMenu.Button("집으로 돌아가기", PopScreen);
                    break;
                case FarewellStep.ReadyToLeave:
                    _companionMenu.Text(life.Name + "는 " + CompanionLifeRules.RouteName(life.Route) + "의 길을 떠날 준비가 되었어요. 함께한 기록은 계속 남아요.", 180);
                    _companionMenu.Button("독립을 배웅하기", () => Act(_application.LetCompanionLeave, OpenLife), _application.PendingTrip == null);
                    break;
                case FarewellStep.Independent:
                    _companionMenu.Text(life.Name + "가 숲 너머로 떠났어요. 함께한 시간이 남긴 세 개의 알 중 하나를 골라 새로운 동행을 시작해 보세요.", 210);
                    string[] hints = { "안에서 무언가 자주 움직인다.", "손에 올리면 포근한 온기가 느껴진다.", "가끔 표면의 무늬가 달라진다." };
                    for (int i = 0; i < 3; i++) { int egg = i; _companionMenu.Button(hints[i], () => Act(() => _application.SelectNextEgg(egg), OpenLife)); }
                    break;
            }
        }
        private IEnumerator ObserveLastCompanionship()
        {
            string id = _application.CurrentLife.SpiritId;
            yield return new WaitForSecondsRealtime(2f);
            if (_mainView.Root.activeInHierarchy && _application.CurrentLife.SpiritId == id &&
                _application.CurrentLife.Farewell == FarewellStep.LastCompanionship)
            {
                try { _application.ObserveNamedCompanion(); }
                catch (Exception error) { Debug.LogError(error); }
            }
        }
        private void OpenGifts()
        {
            Page("선물 · 정수 " + _application.EssenceWallet.Balance);
            _companionMenu.Text("새로운 물건을 경험하게 해 주세요. 선물은 다음 날부터 잠시 행동 후보에 영향을 줘요.", 150);
            for (int i = 0; i < CompanionItemCatalog.Gifts.Length; i++)
            {
                int gift = i;
                _companionMenu.Button(CompanionItemCatalog.Gifts[i] + " · 정수 " + CompanionItemCatalog.GiftPrice,
                    () => { string reaction = ""; Act(() => reaction = _application.GiveGift(gift), OpenGifts); if (reaction.Length > 0) _companionMenu.Notice(reaction); });
            }
        }
        private void OpenTravel()
        {
            Page("이슬 숲으로 여행 · 정수 " + _application.EssenceWallet.Balance);
            var trip = _application.PendingTrip;
            if (trip == null)
            {
                _companionMenu.Text("준비물 하나를 챙겨 4시간 동안 숲을 둘러봐요. 정수 40이 필요해요. 늦게 돌아와도 발견물은 사라지지 않아요.", 200);
                for (int i = 0; i < 3; i++) { int preparation = i; _companionMenu.Button(CompanionItemCatalog.Preparations[i], () => Act(() => _application.StartTrip(preparation, DateTime.UtcNow), OpenTravel)); }
            }
            else
            {
                DateTime end = DateTime.Parse(trip.ReturnAtUtc, CultureInfo.InvariantCulture, DateTimeStyles.RoundtripKind);
                _companionMenu.Text("숲을 여행하고 있어요.\n귀환 예정: " + end.ToLocalTime().ToString("MM/dd HH:mm"), 150);
                _companionMenu.Button("돌아온 정령 맞이하기", () => Act(() => _application.ClaimTrip(DateTime.UtcNow), OpenTravel), _application.CanClaimTrip(DateTime.UtcNow));
                _companionMenu.Button("귀환 확인", OpenTravel);
            }
            foreach (var past in _application.Interventions.Trips)
                if (past.Claimed) _companionMenu.Text(past.StartedOn + "의 여행\n" + past.EventText + "\n발견물: " + past.Discovery, 220);
        }
        private void OpenDecorations()
        {
            Page("집 꾸미기 · 정수 " + _application.EssenceWallet.Balance);
            for (int i = 0; i < CompanionItemCatalog.Decorations.Length; i++)
            {
                int decoration = i; string id = i.ToString(CultureInfo.InvariantCulture);
                bool owned = _application.Interventions.OwnedDecorations.Contains(id);
                bool equipped = _application.Interventions.EquippedDecorations.Contains(id);
                string title = CompanionItemCatalog.Slots[i] + " · " + CompanionItemCatalog.Decorations[i];
                _companionMenu.Text(title, 65);
                _companionMenu.Button(owned ? (equipped ? "장식 치우기" : "장식 놓기") : "정수 30으로 구매",
                    () => Act(() => { if (!owned) _application.BuyDecoration(decoration); else _application.EquipDecoration(decoration, !equipped); }, OpenDecorations));
            }
        }
        private void OpenArchive()
        {
            Page("함께한 정령과 기록");
            foreach (var life in _application.SaveData.Companions)
            {
                var selected = life;
                _companionMenu.Button(life.DisplayName + (life.Farewell == FarewellStep.Independent ? " · 독립한 정령" : " · 동행 중"), () => OpenCompanionArchive(selected));
            }
        }
        private void OpenCompanionArchive(CompanionLife life)
        {
            Page(life.DisplayName + "의 기록", OpenArchive);
            _companionMenu.Portrait(mainScreenPrefab.SpiritActor, life);
            foreach (var trait in life.Temperaments) _companionMenu.Text(CompanionTemperamentText.Describe(trait), 90);
            _companionMenu.Text(life.StartedOn + "부터 " + (life.Farewell == FarewellStep.Independent ? life.IndependentOn + "까지 함께했어요." : "함께하고 있어요."), 110);
            if (life.Stage == CompanionStage.Adult) _companionMenu.Text("찾은 길: " + CompanionLifeRules.RouteName(life.Route), 80);
            _companionMenu.Text(life.Letters.Count == 0 ? "편지함 · 아직 도착한 편지가 없어요." : string.Join("\n", life.Letters), 120);
            foreach (var trip in _application.Interventions.Trips)
                if (trip.SpiritId == life.SpiritId && trip.Claimed)
                    _companionMenu.Text("여행에서 가져온 기억 · " + trip.Discovery + "\n" + trip.EventText, 200);
            foreach (var record in _application.SaveData.Records)
                if (record.SpiritId == life.SpiritId)
                {
                    var selected = record;
                    _companionMenu.Button(record.Date + " · " + SpiritActionCatalog.Get(record.SpiritDay.PrimaryAction).DisplayName,
                        () => { Page(life.DisplayName + " · " + selected.Date, () => OpenCompanionArchive(life));
                            _companionMenu.Text("오늘 나는\n" + PresentationTextFormatter.CompletedTaskTitles(selected), 300);
                            _companionMenu.Text("오늘 " + life.DisplayName + "는\n" + PresentationTextFormatter.TaskActionLinks(selected), 500);
                            if (!string.IsNullOrEmpty(selected.LifeActivityText)) _companionMenu.Text(selected.LifeActivityText, 150); });
                }
        }
        private void OpenLifeDebug()
        {
            if (!Application.isEditor && !Debug.isDebugBuild) return;
            Page("개발자 · 동행 검증");
            _companionMenu.Button("분류 수정 기록 검증", OpenClassificationDebug);
            if (_application.PendingTrip != null)
                _companionMenu.Button("여행 귀환 시각으로 가속", () => Act(_application.FinishTripForDebug, OpenTravel));
            _companionMenu.Text(_application.CurrentDateKey + " · 활동일 " + _application.CurrentLife.ActivityDays + " · " + _application.CurrentLife.Stage, 120);
            _companionMenu.Button("다음 샘플 활동일 진행", () => Act(() => _application.AdvanceLifeDay(), () => { PopScreen(); OpenLife(); }));
            _companionMenu.Button("성장 단계까지 활동일 진행", () => Act(() => {
                var before = _application.CurrentLife.Stage;
                for (int i = 0; i < 10 && _application.CurrentLife.Stage == before && before != CompanionStage.Adult; i++) _application.AdvanceLifeDay();
            }, () => { PopScreen(); OpenLife(); }));
            _companionMenu.Text(_application.CurrentDebugInfo, 1500);
        }

        private void OpenClassificationDebug()
        {
            if (!Application.isEditor && !Debug.isDebugBuild) return;
            Page("개발자 · 분류 기억", OpenLifeDebug);
            _companionMenu.Text("오늘의 결과는 유지합니다. 선택한 분류는 이후 같은 할 일 또는 같은 카테고리의 같은 제목에 우선 적용됩니다.", 170);
            foreach (var task in _application.CurrentCompletedTasks)
            {
                var selected = task;
                _companionMenu.Button(task.Title, () => {
                    Page("이후 분류 선택", OpenClassificationDebug);
                    _companionMenu.Text(selected.Title);
                    foreach (SpiritActionId action in Enum.GetValues(typeof(SpiritActionId)))
                    {
                        var chosen = action;
                        _companionMenu.Button(SpiritActionCatalog.Get(chosen).DisplayName,
                            () => Act(() => _application.RememberTaskClassification(selected.TaskId, chosen),
                                OpenClassificationDebug, "분류를 기억했습니다. 이후 새 하루부터 적용합니다."));
                    }
                });
            }
            if (_application.CurrentCompletedTasks.Count == 0) _companionMenu.Text("오늘은 완료한 일이 없습니다.");
        }
    }
}
