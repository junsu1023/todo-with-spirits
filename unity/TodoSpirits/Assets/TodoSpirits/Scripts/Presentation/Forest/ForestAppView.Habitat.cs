using System;
using System.Linq;
using TodoSpirits.Core;
using TodoSpirits.Runtime;
using UnityEngine;
using UnityEngine.UI;

namespace TodoSpirits.Presentation.Forest
{
    public sealed partial class ForestAppView
    {
        private ForestSpiritAnimator Actor(Transform parent, float x, float y, float size, CompanionLife companion = null)
        {
            companion = companion ?? _app.CurrentLife;
            var poses = PosesForStage(companion.Stage);
            var image = _ui.Sprite("WindSpirit", poses[0], parent, x, y, size, size);
            image.raycastTarget = true;
            var actor = image.gameObject.AddComponent<ForestSpiritAnimator>();
            actor.Poses = poses; actor.ReduceMotion = _app.SaveData.Forest.ReduceMotion;
            actor.SetTemperaments(companion.Temperaments);
            var button = image.gameObject.AddComponent<Button>(); button.targetGraphic = image;
            button.transition = Selectable.Transition.None;
            button.onClick.AddListener(() => TalkToSpirit(actor));
            return actor;
        }

        private float Forest()
        {
            var life = _app.CurrentLife;
            var today = _app.GetForestTodaySummary();
            float height = _pageScroll.viewport.rect.height;
            var forestArt = ArtSprite("bg_forest_storybook_fallback") ?? forestBackground;
            float artAspect = (float)forestArt.rect.width / forestArt.rect.height;
            float artWidth = Mathf.Max(_width, height * artAspect);
            float artHeight = Mathf.Max(height, _width / artAspect);
            var background = _ui.Sprite("ForestArt", forestArt, _content,
                (_width - artWidth) * .5f, (height - artHeight) * .5f, artWidth, artHeight);
            background.preserveAspect = false;
            var fade = _ui.Sprite("CanopyLight", SceneFade, _content, 0, 0, _width, 236);
            fade.preserveAspect = false;
            _ui.Text(PrototypeBootstrap.Current.IsDemo ? "예시 숲 · 별도의 체험 기록" : "바람이 머무는 숲", _content,
                24, 19, _width - 170, 30, 19, ForestUi.Moss, true);
            var wallet = _ui.Button("Essence", "", _content, _width - 145, 12, 82, 48,
                () => Message("함께 쌓인 정수", "완료한 약속에 따라 하루 최대 25정수가 기록돼요. 오늘의 우리에서 누적 획득을 확인할 수 있어요."));
            _ui.Icon(ForestIcon.Leaf, wallet.transform, 10, 13, 22, 22, ForestUi.Moss);
            _ui.Text(_app.EssenceWallet.Balance.ToString(), wallet.transform, 36, 6, 42, 34, 20, ForestUi.Moss, true, TextAnchor.MiddleLeft);
            StyleArtButton(wallet, "paper");
            _ui.IconButton("Settings", ForestIcon.Settings, _content, _width - 60, 12, 48, 48, Settings, ForestUi.Moss);

            var name = _ui.Button("CompanionName", "", _content, 21, 67, _width - 110, 49, EarlyNameDialog);
            name.GetComponent<Image>().color = Color.clear;
            var edge = name.GetComponent<Outline>(); if (edge != null) edge.enabled = false;
            _ui.Text(life.DisplayName, name.transform, 3, 0, _width - 117, 47, 31, ForestUi.Ink, true);
            string chapter = life.Stage == CompanionStage.Adult ? CompanionLifeRules.RouteName(life.Route) + " · 함께하는 계절" :
                "함께한 " + _app.ForestActiveDays + "일 · " + (life.Story.Choices.Count == 0 ? "작은 시작" : ForestStoryRules.VisiblePropName(life));
            _ui.Text(chapter, _content, 25, 118, _width - 104, 33, 18, ForestUi.Moss);
            var dayButton = _ui.Button("OpenToday", "", _content, 24, 166, _width - 48, 62, TodaySummary);
            dayButton.GetComponent<Image>().color = new Color(1, .99f, .94f, .96f);
            _ui.Icon(ForestIcon.Book, dayButton.transform, 15, 19, 25, 25, ForestUi.Moss);
            SheetLabel(_app.CurrentDate.ToString("M월 d일") + " · 오늘의 우리", dayButton.transform, 52, 5, _width - 162, 20, ForestUi.Ink, true);
            SheetLabel(today.HasConfirmedData ? today.Todos.Count(t => t.IsCompleted) + "/" + today.Todos.Count + " 약속 · " + today.TotalEssence + " 정수" : "오늘 기록 연결 확인하기",
                dayButton.transform, 53, 33, _width - 160, 16, ForestUi.Muted);
            _ui.Icon(ForestIcon.ChevronRight, dayButton.transform, _width - 79, 23, 18, 18, ForestUi.Moss);

            var activity = today.Activities.FirstOrDefault();
            string activityKey = activity?.ActivityKey ?? "companion";
            if (_app.PendingTrip != null) activityKey = "walk";
            Text statusLabel = null, keepsakeLabel = null;
            float statusOffset = 12, lowerSpace = 227;
            if (life.Farewell != FarewellStep.Independent)
            {
                string status = _app.PendingTrip != null ? "숲길에서 새로운 소식을 찾는 중" : activity == null ? "숲에서 너를 기다리는 시간" :
                    activity.Location + " · " + (activity.IsAutonomous ? "정령의 시간" : activity.IsCompleted ? "함께 한 일" : "준비 중");
                statusLabel = SheetLabel(status, _content, 24, 0, _width - 48, 19, ForestUi.Ink, true, TextAnchor.MiddleCenter, 31);
                if (life.Story.Choices.Count > 0)
                {
                    keepsakeLabel = SheetLabel(ForestStoryRules.VisiblePropName(life), _content, _width - 150, 0, 144, 16,
                        ForestUi.Ink, true, TextAnchor.MiddleCenter);
                    statusOffset = 8 + keepsakeLabel.rectTransform.rect.height + 8;
                }
                // Reserve both measured labels above the fixed invitation, including wrapped copy.
                lowerSpace = Mathf.Max(lowerSpace, 174 + 16 + statusOffset + statusLabel.rectTransform.rect.height);
            }
            float foot = Mathf.Min(height - lowerSpace, Mathf.Max(490, height * .69f));
            float size = Mathf.Clamp(_width * .43f + life.AppearancePhase * 5, 168, 236);
            size = Mathf.Min(size, Mathf.Max(140, foot - 337));
            DrawDecorations(_content, foot);
            if (life.Farewell != FarewellStep.Independent)
            {
                DrawLivingStage(activityKey, foot, size);
                statusLabel.rectTransform.anchoredPosition = new Vector2(24, -(foot + statusOffset));
                statusLabel.transform.SetAsLastSibling();
                float bubbleY = Mathf.Max(239, foot - size - 144);
                var bubble = _ui.Button("SpiritThought", "", _content, 32, bubbleY, _width - 64, 124, () => TalkToSpirit(_actor));
                UseArt(bubble.GetComponent<Image>(), "bubble_body");
                _spiritSpeech = _ui.Text(_app.NextForestDialogue(), bubble.transform, 23, 12, _width - 110, 100, 20, ForestUi.Ink, false, TextAnchor.MiddleCenter);
                var tail = ArtSprite("bubble_tail");
                if (tail != null) _ui.Sprite("SpeechTail", tail, bubble.transform, (_width - 64) * .5f - 12, 119, 24, 19);
                DrawKeepsake(_content, life, foot, keepsakeLabel);
            }
            else
            {
                JourneyArt(8, _content, (_width - 160) * .5f, foot - 167, 160);
                _ui.Text("우리의 다음 이야기를 기다려요", _content, 24, foot + 8, _width - 48, 34, 22, ForestUi.Moss, true, TextAnchor.MiddleCenter);
            }

            string title, detail; Action open; ForestIcon icon;
            if (_app.ReunionGuest != null)
            { title = "반가운 편지가 왔어요"; detail = _app.ReunionGuest.DisplayName + "의 소식 펼치기"; open = Reunion; icon = ForestIcon.Mail; }
            else if (life.Farewell != FarewellStep.Growing)
            { title = life.Farewell == FarewellStep.Independent ? "다음 친구를 만나 볼까?" : "우리의 다음 계절"; detail = "함께 키운 꿈과 새로운 만남"; open = FarewellDialogue; icon = ForestIcon.Leaf; }
            else if (_app.PendingTrip != null)
            { title = _app.CanClaimTrip(DateTime.UtcNow) ? "나들이에서 돌아왔어!" : "숲길의 소식 듣기"; detail = "발견한 이야기를 함께 펼쳐요"; open = Travel; icon = ForestIcon.Compass; }
            else if (_app.HasForestChoice)
            { title = "있잖아, 같이 골라 줄래?"; detail = "우리 숲에 남을 작은 선택"; open = StoryDialogue; icon = ForestIcon.Mail; }
            else
            { title = "잠깐, 같이 있을까?"; detail = "인사하고 오늘의 마음 나누기"; open = Care; icon = ForestIcon.Heart; }
            var invitation = _ui.Button("ForestInvitation", "", _content, 24, height - 174, _width - 48, 82, open, true);
            _ui.Icon(icon, invitation.transform, 18, 26, 28, 28, ForestUi.Paper);
            SheetLabel(title, invitation.transform, 60, 6, _width - 142, 23, ForestUi.Paper, true);
            _ui.Text(detail, invitation.transform, 60, 48, _width - 142, 26, 17, ForestUi.Paper);
            _ui.Icon(ForestIcon.ChevronRight, invitation.transform, _width - 81, 31, 18, 18, ForestUi.Paper);
            float cell = (_width - 64) / 3;
            ForestAction("Decorate", "숲 꾸미기", ForestIcon.Flower, 24, height - 78, cell, Decorations);
            ForestAction("Travel", "나들이", ForestIcon.Compass, 32 + cell, height - 78, cell, Travel);
            ForestAction("Journey", PrototypeBootstrap.Current.IsDemo ? "체험 안내" : "발자국", ForestIcon.Book, 40 + cell * 2,
                height - 78, cell, PrototypeBootstrap.Current.IsDemo ? PrototypeExperience : JourneyProgress);
            return height;
        }

        private void ForestAction(string name, string title, ForestIcon icon, float x, float y, float width, Action action)
        {
            var button = _ui.Button(name, "", _content, x, y, width, 59, action);
            button.GetComponent<Image>().color = new Color(1, .99f, .94f, .93f);
            _ui.Icon(icon, button.transform, 12, 17, 23, 23, ForestUi.Moss);
            _ui.Text(title, button.transform, 40, 0, width - 43, 59, 18, ForestUi.Ink, true, TextAnchor.MiddleCenter);
        }

        private void DrawLivingStage(string key, float foot, float size)
        {
            float center = _width * .5f;
            var glow = ArtSprite("spirit_glow");
            if (glow != null) { var light = _ui.Sprite("SpiritLight", glow, _content, center - size * .85f, foot - size * 1.26f, size * 1.7f, size * 1.7f); light.color = new Color(1, .96f, .8f, .38f); }
            var shadow = _ui.Sprite("SpiritShadow", ArtSprite("shadow_soft"), _content, center - size * .45f, foot - 22, size * .9f, 30);
            shadow.color = new Color(.25f, .32f, .19f, .35f);
            if (key == "walk")
            {
                var stones = ArtSprite("stepping_stones");
                if (stones != null) _ui.Sprite("WalkingPath", stones, _content, center - 144, foot - 51, 300, 106);
            }
            if (key == "rest" || key == "companion")
            {
                if (restCushion != null)
                {
                    // The cushion is a square transparent sprite. A wide, shallow Image with
                    // a top-left pivot letterboxed it against the left edge of the spirit.
                    float cushionSize = size * 1.14f;
                    _ui.Sprite("RestingCushion", restCushion, _content, center - cushionSize * .5f,
                        foot - cushionSize * .78f, cushionSize, cushionSize);
                }
                else { var grass = ArtSprite("grass_c");
                    if (grass != null) _ui.Sprite("RestingGrass", grass, _content, center - size * .67f, foot - 39, size * 1.35f, 83); }
            }
            _actor = Actor(_content, center - size * .5f, foot - size, size);
            _actor.Activity = key == "companion" ? "rest" : key;
            _actor.ReadingPose = _app.CurrentLife.AppearancePhase == 0 ? readingSpirit : null;
            _actor.BindShadow(shadow.rectTransform);
            RectTransform prop = null;
            if (key == "read" || key == "tea" || key == "craft")
            {
                if (readingDesk != null) _ui.Sprite("LivingDesk", readingDesk, _content, center - size * .71f, foot - size * .78f, size * 1.42f, size * 1.06f);
                if (key == "read")
                {
                    // The actual single reading candidate supplies the held book. No duplicated frames.
                    if (_actor.ReadingPose == null) prop = _ui.Sprite("OpenNotebook", JourneyArt(0), _content, center - 47, foot - size * .43f, 94, 84).rectTransform;
                    var books = ArtSprite("books");
                    if (books != null) _ui.Sprite("ReadingBooks", books, _content, center + size * .30f, foot - 65, 62, 65);
                }
                else if (key == "craft") prop = _ui.Sprite("HandmadePinwheel", JourneyArt(2), _content, center - 41, foot - size * .45f, 83, 84).rectTransform;
                else prop = _ui.Sprite("TeaForTwo", teaCup ?? FurnishingArt(2), _content, center - 42, foot - size * .41f, 85, 78).rectTransform;
            }
            else if (key == "walk") prop = _ui.Sprite("WalkingBag", JourneyArt(1), _content, center + size * .20f, foot - 97, 86, 101).rectTransform;
            if (prop != null) _actor.BindProp(prop);
        }

        private void JourneyArt(int index, Transform parent, float x, float y, float size)
        {
            if (journeyProps != null && index >= 0 && index < journeyProps.Length && journeyProps[index] != null)
                _ui.Sprite("JourneyArt-" + index, journeyProps[index], parent, x, y, size, size);
            else _ui.Icon(index == 1 ? ForestIcon.Compass : ForestIcon.Leaf, parent, x + size * .25f, y + size * .25f, size * .5f, size * .5f, ForestUi.Moss);
        }

        private void DrawKeepsake(Transform parent, CompanionLife life, float foot, Text label)
        {
            if (life.Story.Choices.Count == 0) return;
            var choice = life.Story.Choices.Last();
            var sprite = StoryPropSprite(life, choice.Episode, choice.Option);
            if (label != null)
            {
                label.rectTransform.anchoredPosition = new Vector2(_width - 150, -(foot + 8));
                label.transform.SetAsLastSibling();
            }
            if (sprite == null) return;
            var visible = KeepsakeArtBounds(sprite);
            float scale = Mathf.Min(74 / (sprite.rect.width * visible.width), 86 / (sprite.rect.height * visible.height));
            float artWidth = sprite.rect.width * scale, artHeight = sprite.rect.height * scale;
            float center = _width - 78;
            var shadowSprite = ArtSprite("shadow_soft");
            if (shadowSprite != null)
            {
                float shadowWidth = Mathf.Max(30, artWidth * visible.width * .72f);
                var shadow = _ui.Sprite("KeepsakeGroundShadow", shadowSprite, parent, center - shadowWidth * .5f, foot - 6, shadowWidth, 12);
                shadow.preserveAspect = false;
                shadow.color = new Color(.25f, .32f, .19f, .38f);
            }
            // Place the painted bottom on the ground, not the transparent grid-cell edge.
            var prop = _ui.Sprite("OurStoryKeepsake", sprite, parent,
                center - (visible.x + visible.width * .5f) * artWidth,
                foot - (visible.y + visible.height) * artHeight, artWidth, artHeight);
            prop.raycastTarget = true;
            var button = prop.gameObject.AddComponent<Button>(); button.targetGraphic = prop;
            button.onClick.AddListener(() => Message(ForestStoryRules.VisiblePropName(life), choice.Caption));
        }

        private static Rect KeepsakeArtBounds(Sprite sprite)
        {
            // Normalized top-left bounds measured from the current source PNGs at alpha >= 32.
            // These presentation values avoid runtime texture reads or importer changes.
            switch (sprite.name)
            {
                case "journey_notebook": return new Rect(51f / 418, 75f / 418, 353f / 418, 325f / 418);
                case "journey_backpack": return new Rect(54f / 418, 35f / 418, 341f / 418, 356f / 418);
                case "journey_pinwheel": return new Rect(81f / 418, 54f / 418, 259f / 418, 352f / 418);
                case "gift_map": return new Rect(31f / 418, 49f / 418, 361f / 418, 325f / 418);
                case "mailbox": return new Rect(6f / 129, 4f / 175, 118f / 129, 166f / 175);
                case "sign_blank": return new Rect(5f / 187, 5f / 174, 177f / 187, 165f / 174);
                case "sign_arrow": return new Rect(5f / 165, 4f / 183, 156f / 165, 175f / 183);
                case "lantern": return new Rect(5f / 119, 4f / 166, 110f / 119, 157f / 166);
                case "wind_chime": return new Rect(6f / 80, 4f / 139, 68f / 80, 129f / 139);
                default: return new Rect(0, 0, 1, 1);
            }
        }

        private Sprite StoryPropSprite(CompanionLife life, int episode, int option)
        {
            if (episode >= 2) return option == 0 ? JourneyArt(0) : option == 1 ? JourneyArt(4) : JourneyArt(2);
            if (episode == 1) return option == 0 ? ArtSprite("mailbox") : ArtSprite("sign_blank");
            switch (life.Story.Family)
            {
                case ForestStoryFamily.Movement: return option == 0 ? JourneyArt(4) : ArtSprite("sign_arrow");
                case ForestStoryFamily.Everyday: return option == 0 ? ArtSprite("lantern") : ArtSprite("wind_chime");
                default: return option == 0 ? JourneyArt(0) : JourneyArt(1);
            }
        }

        private void DrawDecorations(Transform parent, float foot)
        {
            foreach (string id in _app.Interventions.EquippedDecorations)
            {
                if (!int.TryParse(id, out int item) || item < 0 || item >= 5 || forestProps == null || forestProps.Length < 6) continue;
                Vector2[] positions = { new Vector2(12, foot - 94), new Vector2(_width - 111, foot - 125), new Vector2(_width - 119, foot - 22), new Vector2(8, foot - 7), new Vector2(_width - 191, foot - 15) };
                var prop = _ui.Sprite("EquippedDecoration-" + id, forestProps[item], parent, positions[item].x, positions[item].y, 105, 105);
                prop.raycastTarget = true;
                var button = prop.gameObject.AddComponent<Button>(); button.targetGraphic = prop; button.onClick.AddListener(Decorations);
            }
            var guest = _app.SaveData.Companions.LastOrDefault(l => l.Story.ReunionClaimed);
            if (guest != null) JourneyArt(3 + (int)guest.Route, parent, _width - 95, foot - 219, 81);
        }

        private void JourneyProgress()
        {
            var content = Modal("우리의 발자국", 640);
            float y = SheetParagraph(content, "하루에 약속을 하나 이상 마치면, 함께한 하루가 쌓여요. 쉬어 간 날 때문에 잃는 것은 없어요.", 6, 19, ForestUi.Muted) + 27;
            int[] days = { 1, 4, 7, 21 };
            string[] labels = { "처음 고르는 작은 물건", "마음을 표현하는 방법", "정령이 꿈꾸는 특기", "함께 자란 정령과 계속되는 생활" };
            for (int i = 0; i < days.Length; i++)
            {
                bool reached = _app.ForestActiveDays >= days[i];
                _ui.Badge(days[i] + "일", content, 22, y, 54, 40, reached ? ForestUi.Moss : ForestUi.Pale, reached ? ForestUi.Paper : ForestUi.Muted);
                _ui.Text(labels[i], content, 91, y + 4, SheetInnerWidth - 73, 34, 19, reached ? ForestUi.Ink : ForestUi.Muted, reached);
                if (i < 3) _ui.Panel("JourneyLine", content, 48, y + 46, 2, 23, ForestUi.Line, false);
                y += 81;
            }
            y = SheetParagraph(content, "생활의 흔적은 그대로 남고, 이야기 속 선택은 표현 방식과 꿈을 바꿔요.", y, 17, ForestUi.Muted);
            SheetContentHeight(content, y + 20);
        }

        private void StoryDialogue()
        {
            if (!_app.HasForestChoice) { JourneyProgress(); return; }
            var life = _app.CurrentLife;
            var content = Modal("바람의 작은 고민", 760);
            Actor(content, (SheetWidth - 142) / 2, 0, 142).Perform("curious");
            float y = 149 + CopyHeight(ForestStoryRules.Question(life), content, 22, 149, SheetInnerWidth, 24, ForestUi.Ink, true) + 20;
            var options = ForestStoryRules.Options(life);
            for (int i = 0; i < options.Length; i++)
            {
                int option = i;
                var choice = _ui.Button("Choice" + i, "", content, 22, y, SheetInnerWidth, 92, () =>
                {
                    try
                    {
                        string result = _app.ChooseForestStory(option);
                        Render();
                        var resultContent = Modal("우리만의 작은 변화", 575);
                        _ui.Sprite("StoryResultProp", StoryPropSprite(life, life.Story.Choices.Count - 1, option), resultContent, (SheetWidth - 190) / 2, 6, 190, 190);
                        float bottom = 214 + CopyHeight(result, resultContent, 22, 214, SheetInnerWidth, 23, ForestUi.Ink, true);
                        bottom = SheetParagraph(resultContent, "함께 고른 마음을 앨범에 남겼어요.", bottom + 20, 17, ForestUi.Muted) + 38;
                        SheetContentHeight(resultContent, bottom);
                        SheetPrimaryAction(resultContent, "ReturnToForest", "우리의 숲으로", CloseModal);
                        _afterModalClose = () => _actor?.Perform("happy");
                    }
                    catch (Exception exception) { Toast(exception.Message); }
                });
                choice.GetComponent<Image>().color = new Color32(239, 243, 230, 255);
                _ui.Sprite("ChoiceProp", StoryPropSprite(life, life.Story.Choices.Count, i), choice.transform, 6, 4, 80, 80);
                _ui.Text(options[i], choice.transform, 93, 10, SheetInnerWidth - 125, 70, 20, ForestUi.Ink, true, TextAnchor.MiddleLeft);
                _ui.Icon(ForestIcon.ChevronRight, choice.transform, SheetInnerWidth - 26, 35, 17, 22, ForestUi.Muted);
                y += 104;
            }
            y = SheetParagraph(content, "정답은 없어요. 네 생활의 기록은 바뀌지 않아요.", y + 6, 16, ForestUi.Muted) + 26;
            SheetContentHeight(content, y);
        }

        private void FarewellDialogue()
        {
            var life = _app.CurrentLife;
            if (life.Farewell == FarewellStep.Independent) { Eggs(); return; }
            if (life.Farewell == FarewellStep.Naming) { NameCompanionDialog(); return; }
            string title, body, label; Action advance;
            switch (life.Farewell)
            {
                case FarewellStep.AdultReveal:
                    title = "너의 하루가 키운 꿈"; body = CompanionLifeRules.RouteName(life.Route) + "가 된 " + life.DisplayName + ".\n\n" + string.Join("\n", life.RouteReasons);
                    label = "함께한 날 돌아보기"; advance = () => _app.AdvanceLifeFarewell(); break;
                case FarewellStep.Reflection:
                    title = "너와 보낸 계절"; body = _app.MonthlyTrace(_app.CurrentDateKey.Substring(0, 7), life.SpiritId);
                    label = "평생 기억할 이름 정하기"; advance = () => _app.AdvanceLifeFarewell(); break;
                case FarewellStep.LastCompanionship:
                    title = "이름을 불러 주던 날"; body = life.DisplayName + ",\n우리 이 숲을 오래 기억하자.\n\n네가 함께한 날들과 마음은 앨범에 남을 거야.";
                    label = "따뜻한 인사 나누기"; advance = () => _app.ObserveNamedCompanion(); break;
                default:
                    title = "다시 만날 약속"; body = "혼자 걸어 볼 준비가 됐어.\n내가 찾은 이야기를 꼭 들려줄게.\n\n다음 정령과 활동한 이틀 뒤, 자신의 길에서 찾은 선물을 들고 돌아와요.";
                    label = "새로운 숲으로 배웅하기"; advance = () => _app.LetCompanionLeave(); break;
            }
            var content = Modal(title, 760);
            var frame = _ui.Panel("FarewellVignette", content, 22, 4, SheetInnerWidth, 211, new Color32(235, 241, 224, 255));
            Actor(frame.transform, (SheetInnerWidth - 165) / 2, 23, 165, life).Perform(life.Farewell == FarewellStep.LastCompanionship ? "wave" : "happy");
            JourneyArt((int)life.Route, frame.transform, SheetInnerWidth - 100, 113, 88);
            float y = SheetParagraph(content, body, 235, 21, ForestUi.Ink);
            if (_app.PendingTrip != null)
            {
                y += 24;
                _ui.Button("FarewellTrip", "나들이 소식 먼저 확인하기", content, 22, y, SheetInnerWidth, 60, Travel, false, 19);
                y += 80;
            }
            SheetContentHeight(content, y + 25);
            bool mustReturn = life.Farewell == FarewellStep.ReadyToLeave && _app.PendingTrip != null;
            SheetPrimaryAction(content, "FarewellNext", mustReturn ? "나들이 소식 먼저 확인하기" : label, () =>
            {
                try { if (mustReturn) { Travel(); return; } advance(); Render(); FarewellDialogue(); }
                catch (Exception exception) { Toast(exception.Message); }
            });
        }

        private static Sprite SceneFade
        {
            get
            {
                if (_sceneFade != null) return _sceneFade;
                var texture = new Texture2D(1, 128, TextureFormat.RGBA32, false) { hideFlags = HideFlags.HideAndDontSave, wrapMode = TextureWrapMode.Clamp };
                var pixels = new Color[128];
                for (int i = 0; i < pixels.Length; i++) pixels[i] = new Color(ForestUi.Paper.r, ForestUi.Paper.g, ForestUi.Paper.b, Mathf.SmoothStep(0, .96f, i / 127f));
                texture.SetPixels(pixels); texture.Apply(false, true);
                _sceneFade = UnityEngine.Sprite.Create(texture, new Rect(0, 0, 1, 128), Vector2.one * .5f, 100);
                _sceneFade.hideFlags = HideFlags.HideAndDontSave; return _sceneFade;
            }
        }
    }
}
