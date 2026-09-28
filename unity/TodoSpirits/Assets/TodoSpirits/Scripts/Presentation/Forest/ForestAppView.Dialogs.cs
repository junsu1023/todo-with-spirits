using System;
using System.Collections;
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
        private static readonly Color SheetSage = new Color32(221, 232, 217, 255);
        private static readonly Color SheetBlush = new Color32(247, 232, 235, 255);
        private static readonly Color SheetError = new Color32(143, 62, 73, 255);
        private const float SheetInset = 22;
        private readonly Dictionary<string, TodoSheetDraft> _todoSheetDrafts = new Dictionary<string, TodoSheetDraft>();
        private int _selectedDecoration;
        private ForestModalMotion _modalMotion;
        private Action _beforeModalClose;
        private Action _afterModalClose;

        private sealed class TodoSheetDraft
        {
            public string Title;
            public UserTaskCategory Category;
            public bool Habit;
            public bool CategoriesExpanded;
        }

        private float SheetWidth => _width - 24;
        private float SheetInnerWidth => SheetWidth - SheetInset * 2;

        // The header remains reachable while the body scrolls; text is never scaled down to fit.
        private RectTransform Modal(string title, float contentHeight)
        {
            ClearModalImmediately();
            _modal = _ui.Rect("Modal", _safe, 0, 0, 0, 0);
            _ui.Fill(_modal);
            var inputShield = _modal.gameObject.AddComponent<CanvasGroup>();
            var dim = _ui.Panel("Backdrop", _modal, 0, 0, 0, 0, new Color(.06f, .12f, .09f, .54f), false);
            _ui.Fill(dim.rectTransform);
            var dimGroup = dim.gameObject.AddComponent<CanvasGroup>();
            var dismiss = dim.gameObject.AddComponent<Button>();
            dismiss.targetGraphic = dim;
            dismiss.transition = Selectable.Transition.None;
            dismiss.onClick.AddListener(CloseModal);

            var panel = _ui.Panel("Dialog", _modal, 0, 0, SheetWidth, 600, ForestUi.Paper).rectTransform;
            UseArt(panel.GetComponent<Image>(), "panel_wood");
            panel.anchorMin = panel.anchorMax = new Vector2(.5f, 0);
            panel.pivot = new Vector2(.5f, 0);
            panel.anchoredPosition = new Vector2(0, 12);
            var paper = _ui.Panel("DialogPaper", panel, 0, 0, SheetWidth - 14, 586, ForestUi.Paper).rectTransform;
            paper.anchorMin = Vector2.zero;
            paper.anchorMax = Vector2.one;
            paper.offsetMin = new Vector2(7, 7);
            paper.offsetMax = new Vector2(-7, -7);
            UseArt(paper.GetComponent<Image>(), "panel_paper");
            paper.GetComponent<Image>().raycastTarget = false;
            // A button on the sheet consumes taps on empty paper instead of bubbling to the scrim.
            var surface = panel.gameObject.AddComponent<Button>();
            surface.transition = Selectable.Transition.None;
            surface.targetGraphic = panel.GetComponent<Image>();
            _ui.Panel("SheetHandle", panel, (SheetWidth - 34) / 2, 10, 34, 4, SheetSage);
            var heading = _ui.Text(title, panel, SheetInset, 28, SheetWidth - 94, 180, 24, ForestUi.Ink, true, TextAnchor.MiddleLeft);
            float headingHeight = Mathf.Max(54, heading.preferredHeight + 6);
            heading.rectTransform.sizeDelta = new Vector2(SheetWidth - 94, headingHeight);
            _ui.IconButton("Close", ForestIcon.Close, panel, SheetWidth - 78, 23, 60, 60, CloseModal);
            float dividerY = 34 + headingHeight;
            float viewportTop = dividerY + 14;
            _ui.Divider(panel, SheetInset, dividerY, SheetInnerWidth);

            var viewport = _ui.Rect("SheetViewport", panel, 0, viewportTop, SheetWidth, 400);
            viewport.anchorMin = Vector2.zero;
            viewport.anchorMax = Vector2.one;
            viewport.offsetMin = new Vector2(0, 18);
            viewport.offsetMax = new Vector2(0, -viewportTop);
            var viewportGraphic = viewport.gameObject.AddComponent<Image>();
            viewportGraphic.color = new Color(1, 1, 1, .001f);
            viewport.gameObject.AddComponent<RectMask2D>();
            var scroll = viewport.gameObject.AddComponent<ScrollRect>();
            scroll.viewport = viewport;
            scroll.horizontal = false;
            scroll.movementType = ScrollRect.MovementType.Clamped;
            scroll.scrollSensitivity = 32;
            scroll.decelerationRate = .08f;
            var content = _ui.Rect("SheetContent", viewport, 0, 0, SheetWidth, contentHeight);
            scroll.content = content;
            _modalMotion = _modal.gameObject.AddComponent<ForestModalMotion>();
            _modalMotion.Configure(panel, dimGroup, inputShield);
            panel.GetComponent<CanvasGroup>().alpha = 0;
            dimGroup.alpha = 0;
            StartCoroutine(FitSheetToAvailableSpace(_modal, panel, content, viewport));
            StartCoroutine(PresentSheet(_modal, _modalMotion));
            return content;
        }

        private IEnumerator PresentSheet(RectTransform modal, ForestModalMotion motion)
        {
            yield return null;
            if (modal != null && modal == _modal) motion.Present(_app.SaveData.Forest.ReduceMotion);
        }

        private IEnumerator FitSheetToAvailableSpace(RectTransform modal, RectTransform sheet, RectTransform content, RectTransform viewport)
        {
            while (modal != null && modal == _modal)
            {
                float keyboardHeight = 0;
                if (TouchScreenKeyboard.visible && Screen.height > 0)
                {
                    var canvas = _safe.GetComponentInParent<Canvas>();
                    float scale = canvas == null ? 1 : canvas.rootCanvas.scaleFactor;
                    // Keyboard height is in screen pixels; subtract the device's bottom safe inset.
                    keyboardHeight = Mathf.Max(0, TouchScreenKeyboard.area.height - Screen.safeArea.yMin) / Mathf.Max(.01f, scale);
                }
                float available = Mathf.Max(190, _safe.rect.height - keyboardHeight - 24);
                float chromeHeight = viewport.offsetMin.y - viewport.offsetMax.y;
                sheet.sizeDelta = new Vector2(SheetWidth, Mathf.Min(content.sizeDelta.y + chromeHeight, available));
                if (_modalMotion == null || !_modalMotion.IsAnimating)
                    sheet.anchoredPosition = new Vector2(0, keyboardHeight + 12);
                yield return null;
            }
        }

        // A persistent action belongs to the sheet chrome, never to its scrollable body.
        private Button SheetPrimaryAction(RectTransform content, string name, string label, Action action)
        {
            var viewport = (RectTransform)content.parent;
            var sheet = (RectTransform)viewport.parent;
            var previous = sheet.Find("SheetFooter") as RectTransform;
            if (previous != null) Clear(previous);
            viewport.offsetMin = new Vector2(viewport.offsetMin.x, 94);
            // The sheet already owns its paper and wooden edge. Keep the input surface,
            // but let that artwork show through instead of painting a square white strip.
            var footer = _ui.Panel("SheetFooter", sheet, 0, 0, SheetWidth, 88, Color.clear, false).rectTransform;
            footer.anchorMin = footer.anchorMax = new Vector2(0, 0);
            footer.pivot = Vector2.zero;
            footer.anchoredPosition = Vector2.zero;
            _ui.Divider(footer, SheetInset, 0, SheetInnerWidth);
            return _ui.Button(name, label, footer, SheetInset, 14, SheetInnerWidth, 60, action, true, 18);
        }

        private Sprite JourneyArt(int index) => journeyProps != null && index >= 0 && index < journeyProps.Length ? journeyProps[index] : null;
        private Sprite FurnishingArt(int index) => forestProps != null && index >= 0 && index < forestProps.Length ? forestProps[index] : null;

        private void SheetArt(string name, Sprite sprite, Transform parent, float x, float y, float width, float height, ForestIcon fallback)
        {
            if (sprite != null) _ui.Sprite(name, sprite, parent, x, y, width, height);
            else _ui.Icon(fallback, parent, x + width * .25f, y + height * .25f, width * .5f, height * .5f, ForestUi.Moss);
        }

        private Text SheetLabel(string value, Transform parent, float x, float y, float width, int size = 18,
            Color? color = null, bool bold = false, TextAnchor alignment = TextAnchor.UpperLeft, float minimumHeight = 0)
        {
            var label = _ui.Text(value, parent, x, y, width, 500, size, color, bold, alignment);
            label.rectTransform.sizeDelta = new Vector2(width, Mathf.Max(minimumHeight, label.preferredHeight + 5));
            return label;
        }

        private ForestSpiritAnimator SheetActor(Transform parent, float x, float y, float size, string motion = "idle", CompanionLife companion = null)
        {
            companion = companion ?? _app.CurrentLife;
            var poses = PosesForStage(companion.Stage);
            var sprite = _ui.Sprite("SheetSpirit", poses[0], parent, x, y, size, size);
            var actor = sprite.gameObject.AddComponent<ForestSpiritAnimator>();
            actor.Poses = poses;
            actor.ReduceMotion = _app.SaveData.Forest.ReduceMotion;
            actor.SetTemperaments(companion.Temperaments);
            actor.Perform(motion);
            return actor;
        }

        private RectTransform SheetStage(RectTransform content, float y, float height)
        {
            var stage = _ui.Panel("IllustratedStage", content, SheetInset, y, SheetInnerWidth, height, SheetSage).rectTransform;
            stage.gameObject.AddComponent<Mask>().showMaskGraphic = true;
            float artHeight = SheetInnerWidth * 1.5f;
            var background = _ui.Sprite("StageForest", forestBackground, stage, 0, -(artHeight - height) * .45f, SheetInnerWidth, artHeight);
            background.preserveAspect = false;
            _ui.Panel("StageWash", stage, 0, 0, SheetInnerWidth, height, new Color(.99f, .98f, .94f, .16f), false).raycastTarget = false;
            return stage;
        }

        private void SetSheetSelection(Button button, bool selected)
        {
            button.GetComponent<Image>().color = selected ? SheetSage : Color.white;
            var edge = button.GetComponent<Outline>();
            if (edge != null)
            {
                edge.effectColor = selected ? new Color(ForestUi.Moss.r, ForestUi.Moss.g, ForestUi.Moss.b, .6f) : ForestUi.Line;
                edge.effectDistance = selected ? new Vector2(1.3f, -1.3f) : new Vector2(.6f, -.6f);
            }
        }

        private void CloseModal()
        {
            if (_modal == null || (_modalMotion != null && _modalMotion.IsClosing)) return;
            try { _beforeModalClose?.Invoke(); }
            catch (Exception exception) { Toast(exception.Message); return; }
            _beforeModalClose = null;
            var closing = _modal;
            var after = _afterModalClose;
            _afterModalClose = null;
            Action complete = () => {
                Clear(closing);
                if (_modal == closing) { _modal = null; _modalMotion = null; _showingToday = false; after?.Invoke(); }
            };
            if (_modalMotion == null || !_modalMotion.Dismiss(complete, _app.SaveData.Forest.ReduceMotion)) complete();
        }

        private void ClearModalImmediately()
        {
            _beforeModalClose = null;
            _afterModalClose = null;
            var previous = _modal;
            _modal = null; _modalMotion = null; _showingToday = false;
            Clear(previous);
        }

        private float SheetParagraph(RectTransform parent, string text, float y, int size = 18, Color? color = null)
        {
            var label = _ui.Text(text, parent, SheetInset, y, SheetInnerWidth, 400, size, color ?? ForestUi.Muted);
            float height = Mathf.Max(size + 8, label.preferredHeight + 5);
            label.rectTransform.sizeDelta = new Vector2(SheetInnerWidth, height);
            return y + height;
        }

        private void SheetContentHeight(RectTransform content, float height)
        {
            content.sizeDelta = new Vector2(SheetWidth, height + 4);
        }

        private Text SheetValidation(RectTransform parent, float y, float height = 54)
        {
            return _ui.Text("", parent, SheetInset, y, SheetInnerWidth, height, 16, SheetError);
        }

        private bool CommitDialog(Action action, Text error, string message = null, string motion = null)
        {
            try
            {
                // Keep an open draft at midnight, then reconcile its date before committing.
                _app.SynchronizeLifeDate(PrototypeBootstrap.Current.ResolveToday());
                action();
                Render();
                if (!string.IsNullOrEmpty(message)) Toast(message);
                if (motion != null) _actor?.Perform(motion);
                return true;
            }
            catch (Exception exception)
            {
                if (error != null)
                {
                    error.gameObject.SetActive(true);
                    error.text = exception.Message;
                    error.color = SheetError;
                    float errorHeight = Mathf.Max(24, error.preferredHeight + 6);
                    error.rectTransform.sizeDelta = new Vector2(error.rectTransform.sizeDelta.x, errorHeight);
                    var scroll = error.GetComponentInParent<ScrollRect>();
                    if (scroll != null)
                    {
                        float bottom = -error.rectTransform.anchoredPosition.y + errorHeight + 8;
                        scroll.content.sizeDelta = new Vector2(scroll.content.sizeDelta.x, Mathf.Max(scroll.content.sizeDelta.y, bottom));
                        Canvas.ForceUpdateCanvases();
                        scroll.verticalNormalizedPosition = 0;
                    }
                }
                else Toast(exception.Message);
                return false;
            }
        }

        private Button SheetRow(string name, string title, string description, ForestIcon icon,
            RectTransform parent, float y, Action action, bool selected = false, string trailing = null)
        {
            var button = _ui.Button(name, "", parent, SheetInset, y, SheetInnerWidth, 88, action, false);
            button.GetComponent<Image>().color = selected ? SheetSage : Color.white;
            _ui.Icon(icon, button.transform, 16, 27, 28, 28, ForestUi.Moss);
            float textWidth = SheetInnerWidth - 104;
            _ui.Text(title, button.transform, 58, 8, textWidth, 30, 18, ForestUi.Ink, true, TextAnchor.MiddleLeft);
            _ui.Text(description, button.transform, 58, 39, textWidth, 44, 14, ForestUi.Muted);
            if (trailing == null)
                _ui.Icon(selected ? ForestIcon.Check : ForestIcon.ChevronRight, button.transform, SheetInnerWidth - 35, 34, 18, 18, ForestUi.Moss);
            else
                _ui.Text(trailing, button.transform, SheetInnerWidth - 52, 16, 44, 56, 15, ForestUi.Moss, true, TextAnchor.MiddleCenter);
            return button;
        }

        private void Message(string title, string body)
        {
            var content = Modal(title, 320);
            _ui.Icon(ForestIcon.Leaf, content, SheetInset, 5, 29, 29, ForestUi.Moss);
            float y = SheetParagraph(content, body, 52, 20, ForestUi.Ink) + 28;
            _ui.Button("Okay", "마음에 담아 둘게", content, SheetInset, y, SheetInnerWidth, 56, CloseModal, true, 19);
            SheetContentHeight(content, y + 56);
        }

        private void Confirm(string title, string body, Action action)
        {
            var content = Modal(title, 310);
            float y = SheetParagraph(content, body, 8, 19) + 20;
            var error = SheetValidation(content, y);
            y += 58;
            var remove = _ui.Button("Confirm", "목록에서 지우기", content, SheetInset, y, SheetInnerWidth, 56,
                () => CommitDialog(action, error, "목록에서 지웠어요. 지난 기록은 앨범에 남아 있어요."), true, 19);
            remove.GetComponent<Image>().color = SheetError;
            _ui.Button("Cancel", "계속 함께하기", content, SheetInset, y + 66, SheetInnerWidth, 52, CloseModal, false, 18);
            SheetContentHeight(content, y + 118);
        }

        private void AddTodo() => TodoEditor(null);

        private void EditTodo(ForestTodo todo) => TodoEditor(todo);

        private void TodoEditor(ForestTodo todo)
        {
            bool editing = todo != null;
            string draftKey = editing ? todo.Id : "new-todo";
            bool resumed = _todoSheetDrafts.TryGetValue(draftKey, out var draft);
            if (!resumed)
            {
                draft = new TodoSheetDraft { Title = editing ? todo.Title : "", Habit = editing && todo.Habit,
                    Category = editing ? todo.Category : UserTaskCategory.Unspecified };
                _todoSheetDrafts.Add(draftKey, draft);
            }
            var content = Modal(editing ? "약속 다듬기" : "오늘의 작은 약속", 330);
            var introduction = SheetLabel(resumed && !string.IsNullOrEmpty(draft.Title) ? "적어 두었던 마음을 이어서 남겨요." : "작은 일 하나면 충분해요.",
                content, SheetInset, 3, SheetInnerWidth, 16, ForestUi.Muted);
            float y = introduction.rectTransform.sizeDelta.y + 19;
            var input = _ui.Input(content, SheetInset, y, SheetInnerWidth, "오늘 무엇을 함께할까요?", 80);
            input.text = draft.Title;
            y += 65;
            var counter = _ui.Text(input.text.Length + " / 80", content, SheetInset, y, SheetInnerWidth, 25, 13, ForestUi.Muted, false, TextAnchor.MiddleRight);
            input.onValueChanged.AddListener(value => { draft.Title = value; counter.text = value.Length + " / 80"; });
            y += 40;
            float half = (SheetInnerWidth - 8) / 2;
            var typeButtons = new Button[2];
            Action refreshTypes = () =>
            {
                for (int i = 0; i < typeButtons.Length; i++)
                {
                    bool selected = (i == 1) == draft.Habit;
                    SetSheetSelection(typeButtons[i], selected);
                    var label = typeButtons[i].GetComponentInChildren<Text>();
                    label.color = selected ? ForestUi.Moss : ForestUi.Muted;
                    label.text = i == 0 ? "오늘 할 일" : "매일 습관";
                }
            };
            typeButtons[0] = _ui.Button("OneTime", "", content, SheetInset, y, half, 56, () => { draft.Habit = false; refreshTypes(); }, false, 16);
            typeButtons[1] = _ui.Button("Habit", "", content, SheetInset + half + 8, y, half, 56, () => { draft.Habit = true; refreshTypes(); }, false, 16);
            refreshTypes();
            y += 77;
            var categoryButton = _ui.Button("CategoryPicker", "", content, SheetInset, y, SheetInnerWidth, 63,
                () => { draft.CategoriesExpanded = !draft.CategoriesExpanded; TodoEditor(todo); }, false);
            _ui.Icon(ForestIcon.Leaf, categoryButton.transform, 16, 20, 23, 23, ForestUi.Moss);
            _ui.Text("카테고리", categoryButton.transform, 50, 0, 75, 63, 15, ForestUi.Muted, false, TextAnchor.MiddleLeft);
            _ui.Text(ForestStoryRules.CategoryName(draft.Category), categoryButton.transform, 128, 0, SheetInnerWidth - 167, 63, 16, ForestUi.Ink, true, TextAnchor.MiddleRight);
            var categoryArrow = _ui.Icon(ForestIcon.ChevronRight, categoryButton.transform, SheetInnerWidth - 33, 22, 18, 18, ForestUi.Moss);
            categoryArrow.rectTransform.localRotation = Quaternion.Euler(0, 0, draft.CategoriesExpanded ? 90 : -90);
            y += 75;
            if (draft.CategoriesExpanded)
            {
                int columns = SheetInnerWidth < 310 ? 2 : 3;
                float cellWidth = (SheetInnerWidth - (columns - 1) * 8) / columns;
                for (int i = 0; i <= 9; i++)
                {
                    int index = i;
                    var button = _ui.Button("Category" + i, ForestStoryRules.CategoryName((UserTaskCategory)i), content,
                        SheetInset + i % columns * (cellWidth + 8), y + i / columns * 64, cellWidth, 56,
                        () => { draft.Category = (UserTaskCategory)index; draft.CategoriesExpanded = false; TodoEditor(todo); }, false, 15);
                    SetSheetSelection(button, (int)draft.Category == i);
                }
                y += Mathf.CeilToInt(10f / columns) * 64 + 4;
            }
            y = SheetParagraph(content, draft.Category == UserTaskCategory.Unspecified ? "아직 모르겠다면 ‘미정’으로 남겨도 좋아요." : "내 생활의 조각이 정령의 하루에 스며들어요.", y, 14) + 5;
            if (editing)
            {
                _ui.Button("ArchiveTodo", "이 약속 지우기", content, SheetInset, y + 8, SheetInnerWidth, 48, () =>
                    Confirm("이 약속을 지울까요?", "목록에서 지워지고 오늘의 완료 표시는 취소돼요. 지난 날짜의 생활 기록과 정령의 추억은 그대로 남아요.",
                        () => { _app.ArchiveForestTodo(todo.Id); _todoSheetDrafts.Remove(draftKey); }), false, 15);
                y += 64;
            }
            var error = SheetValidation(content, y + 2, 24);
            error.gameObject.SetActive(false);
            SheetPrimaryAction(content, "SaveTodo", editing ? "변경한 약속 저장" : "오늘의 약속 남기기", () =>
            {
                if (CommitDialog(() =>
                {
                    if (editing) _app.EditForestTodo(todo.Id, draft.Title, draft.Category, draft.Habit);
                    else _app.AddForestTodo(draft.Title, draft.Category, draft.Habit);
                }, error, editing ? "오늘의 약속을 바꿨어요. 지난 기록은 그대로 남아요." : "오늘의 작은 약속을 남겼어요."))
                    _todoSheetDrafts.Remove(draftKey);
            });
            SheetContentHeight(content, y + 9);
        }

        private void Care()
        {
            var content = Modal("너와 나의 작은 쉼", 445);
            var stage = SheetStage(content, 4, 202);
            var companion = SheetActor(stage, (SheetInnerWidth - 180) / 2, 12, 180);
            var speech = _ui.Panel("CareSpeech", content, SheetInset + 6, 222, SheetInnerWidth - 12, 90, Color.white);
            var words = _ui.Text("왔구나.\n오늘은 어떤 하루였어?", speech.transform, 16, 8, SheetInnerWidth - 44, 74, 18,
                ForestUi.Ink, false, TextAnchor.MiddleCenter);
            string[] labels = { "인사하기", "이야기", "함께 쉬기" };
            string[] descriptions = { "널 만나니 기분이 좋아!\n오늘도 와 줘서 고마워.", "그랬구나!\n이야기해 줘서 고마워.", "잠깐 아무것도 하지 말고,\n바람 소리를 함께 듣자." };
            string[] motions = { "wave", "happy", "sleep" };
            ForestIcon[] icons = { ForestIcon.Sun, ForestIcon.Heart, ForestIcon.Moon };
            string initialWords = words.text;
            float speechHeight = words.preferredHeight + 8;
            foreach (string reaction in descriptions) { words.text = reaction; speechHeight = Mathf.Max(speechHeight, words.preferredHeight + 8); }
            words.text = initialWords;
            words.rectTransform.sizeDelta = new Vector2(SheetInnerWidth - 44, speechHeight);
            speech.rectTransform.sizeDelta = new Vector2(SheetInnerWidth - 12, speechHeight + 16);
            float actionsY = 254 + speechHeight;
            float cell = (SheetInnerWidth - 16) / 3;
            var buttons = new Button[3];
            for (int i = 0; i < labels.Length; i++)
            {
                int index = i;
                buttons[i] = _ui.Button("Care" + i, "", content, SheetInset + i * (cell + 8), actionsY, cell, 91, () =>
                {
                    companion.Perform(motions[index]);
                    words.text = descriptions[index];
                    for (int n = 0; n < buttons.Length; n++) SetSheetSelection(buttons[n], n == index);
                });
                _ui.Icon(icons[i], buttons[i].transform, (cell - 27) / 2, 14, 27, 27, ForestUi.Moss);
                _ui.Text(labels[i], buttons[i].transform, 4, 49, cell - 8, 32, 15, ForestUi.Ink, true, TextAnchor.MiddleCenter);
            }
            bool given = _app.Interventions.Gifts.Any(g => g.SpiritId == _app.CurrentLife.SpiritId && g.Date == _app.CurrentDateKey);
            float y = SheetParagraph(content, "할 일을 끝내지 않은 날도, 너와 함께라서 좋아.", actionsY + 111, 15);
            SheetArt("WrappedGift", FurnishingArt(5), content, SheetInset, y + 10, 60, 60, ForestIcon.Gift);
            var giftHint = SheetLabel(given ? "오늘 전한 선물을\n소중히 살펴보고 있어요." : "작은 선물에\n마음을 담아 볼까요?", content,
                SheetInset + 73, y + 13, SheetInnerWidth - 73, 16, ForestUi.Muted);
            y += Mathf.Max(80, giftHint.rectTransform.sizeDelta.y + 24);
            SheetPrimaryAction(content, "ChooseGift", given ? "오늘 건넨 마음 보기" : "마음을 담은 선물 고르기", Gifts);
            SheetContentHeight(content, y);
        }

        private void Gifts()
        {
            var content = Modal("작은 선물 가게", 550);
            var todayGift = _app.Interventions.Gifts.LastOrDefault(g => g.SpiritId == _app.CurrentLife.SpiritId && g.Date == _app.CurrentDateKey);
            ForestIcon[] icons = { ForestIcon.Book, ForestIcon.Moon, ForestIcon.Flower, ForestIcon.Leaf, ForestIcon.Heart, ForestIcon.Sun };
            string[] descriptions = { "이야기에 빠져드는 시간", "느긋하게 쉬어 가는 시간", "손끝으로 상상하는 시간", "새싹을 살피는 시간", "도란도란 이야기하는 시간", "새로운 길이 궁금해지는 시간" };
            Sprite[] artworks = { JourneyArt(0) ?? FurnishingArt(5), FurnishingArt(5), FurnishingArt(5), FurnishingArt(3), FurnishingArt(2), FurnishingArt(5) };
            if (giftProps != null && giftProps.Length >= artworks.Length)
                for (int i = 0; i < artworks.Length; i++) if (giftProps[i] != null) artworks[i] = giftProps[i];
            int selected = todayGift?.GiftId ?? 0;
            _ui.Badge(_app.EssenceWallet.Balance + " 정수", content, SheetWidth - 132, 2, 110, 33, SheetSage);
            _ui.Text("하루에 한 번", content, SheetInset, 7, SheetInnerWidth - 120, 32, 14, ForestUi.Muted);
            var preview = _ui.Sprite("SelectedGift", artworks[selected], content, (SheetWidth - 128) / 2, 54, 128, 116);
            var giftName = _ui.Text(CompanionItemCatalog.Gifts[selected], content, SheetInset, 180, SheetInnerWidth, 42, 23, ForestUi.Ink, true, TextAnchor.MiddleCenter);
            var description = _ui.Text(descriptions[selected], content, SheetInset, 229, SheetInnerWidth, 58, 16, ForestUi.Muted, false, TextAnchor.MiddleCenter);
            float y = 302;
            float cell = (SheetInnerWidth - 10) / 2;
            var choices = new Button[CompanionItemCatalog.Gifts.Length];
            Button send = null;
            Action refresh = () =>
            {
                preview.sprite = artworks[selected];
                giftName.text = CompanionItemCatalog.Gifts[selected];
                description.text = descriptions[selected];
                for (int n = 0; n < choices.Length; n++) SetSheetSelection(choices[n], n == selected);
                if (send != null) send.GetComponentInChildren<Text>().text = todayGift != null ? "마음에 담아 둘게" : "이 선물 전하기 · 15 정수";
            };
            for (int i = 0; i < CompanionItemCatalog.Gifts.Length; i++)
            {
                int gift = i;
                choices[i] = _ui.Button("Gift" + i, "", content, SheetInset + i % 2 * (cell + 10), y + i / 2 * 147,
                    cell, 137, () => { selected = gift; refresh(); }, false);
                SheetArt("GiftPicture", artworks[i], choices[i].transform, (cell - 84) / 2, 6, 84, 84, icons[i]);
                _ui.Text(CompanionItemCatalog.Gifts[i], choices[i].transform, 6, 94, cell - 12, 36, 15, ForestUi.Ink, false, TextAnchor.MiddleCenter);
                choices[i].interactable = todayGift == null;
            }
            y += 449;
            if (todayGift != null) y = SheetParagraph(content, todayGift.Reaction, y, 18, ForestUi.Ink) + 12;
            string hint = todayGift != null ? "다음 날, 또 다른 마음을 전할 수 있어요." : _app.PendingTrip != null ? "나들이에서 돌아오면 전해 주세요." :
                _app.EssenceWallet.Balance < CompanionItemCatalog.GiftPrice ? "지금 가진 정수가 조금 부족해요. 인사와 이야기는 언제나 나눌 수 있어요." : "선물은 앞으로 며칠의 관심사에 작은 영향을 줘요.";
            y = SheetParagraph(content, hint, y, 14) + 7;
            var error = SheetValidation(content, y, 24); error.gameObject.SetActive(false);
            send = SheetPrimaryAction(content, "GiveGift", "", () =>
            {
                if (todayGift != null) { CloseModal(); return; }
                string reaction = "";
                if (CommitDialog(() => reaction = _app.GiveGift(selected), error, null, "happy"))
                    GiftDelivered(artworks[selected], CompanionItemCatalog.Gifts[selected], reaction);
            });
            send.interactable = todayGift != null || (_app.PendingTrip == null && _app.EssenceWallet.Balance >= CompanionItemCatalog.GiftPrice);
            refresh();
            SheetContentHeight(content, y + 5);
        }

        private void GiftDelivered(Sprite picture, string name, string reaction)
        {
            var content = Modal("마음이 전해졌어요", 380);
            var stage = SheetStage(content, 3, 193);
            SheetActor(stage, (SheetInnerWidth - 166) / 2 - 20, 10, 166, "happy");
            SheetArt("GivenGift", picture, stage, SheetInnerWidth - 95, 103, 80, 80, ForestIcon.Gift);
            _ui.Badge(name, content, (SheetWidth - 174) / 2, 212, 174, 34, SheetBlush, ForestUi.Rose);
            float y = SheetParagraph(content, reaction, 263, 19, ForestUi.Ink) + 16;
            SheetPrimaryAction(content, "Okay", "네가 좋아하니 나도 좋아", CloseModal);
            SheetContentHeight(content, y);
        }

        private void Settings()
        {
            var content = Modal("나에게 편안한 숲", 510);
            _ui.Text("화면과 움직임", content, SheetInset, 5, SheetInnerWidth, 27, 15, ForestUi.Muted, true);
            bool reduced = _app.SaveData.Forest.ReduceMotion;
            SheetRow("ReduceMotion", "동작 줄이기", reduced ? "잔잔한 화면으로 함께해요" : "정령의 움직임과 반응을 보여요", ForestIcon.Pause,
                content, 47, () =>
                {
                    if (CommitDialog(() => _app.SetReducedMotion(!reduced), null)) Settings();
                }, reduced, reduced ? "켬" : "끔");
            _ui.Divider(content, SheetInset, 161, SheetInnerWidth);
            _ui.Text("기록과 연결", content, SheetInset, 181, SheetInnerWidth, 28, 15, ForestUi.Muted, true);
            float y = SheetParagraph(content, _app.IsHostConnected
                ? "연결된 TODO 앱의 일정을 함께하고 있어요.\n마지막 기록 날짜 · " + _app.SaveData.Forest.HostDate
                : "일정과 정령의 추억을 이 기기에 저장해요.\n앱을 지우면 이 기기의 기록도 사라질 수 있어요.", 220, 17) + 24;
            var balance = _ui.Panel("EssenceBalance", content, SheetInset, y, SheetInnerWidth, 64, SheetSage);
            _ui.Icon(ForestIcon.Leaf, balance.transform, 16, 21, 22, 22, ForestUi.Moss);
            _ui.Text("함께 모은 정수", balance.transform, 48, 0, SheetInnerWidth - 126, 64, 16, ForestUi.Moss, false, TextAnchor.MiddleLeft);
            _ui.Text(_app.EssenceWallet.Balance.ToString(), balance.transform, SheetInnerWidth - 75, 0, 58, 64, 22, ForestUi.Moss, true, TextAnchor.MiddleRight);
            y += 88;
            _ui.Button("RenameSpirit", "정령 이름 바꾸기", content, SheetInset, y, SheetInnerWidth, 56, EarlyNameDialog, false, 19);
            y += 70;
            if (_app.CurrentLife.Stage == CompanionStage.Adult && _app.CurrentLife.Farewell == FarewellStep.Growing)
            {
                _ui.Button("NextCompanionship", "다음 동행 이야기", content, SheetInset, y, SheetInnerWidth, 56, BeginNextCompanionship, false, 19);
                y += 70;
            }
            _ui.Button("PrototypeExperience", "예시 하루 체험", content, SheetInset, y, SheetInnerWidth, 56, PrototypeExperience, false, 19);
            y += 70;
            _ui.Button("ReturnToTodo", "TODO 앱으로 돌아가기", content, SheetInset, y, SheetInnerWidth, 56, ReturnToTodoApp, false, 19);
            y += 70;
            SheetContentHeight(content, y);
        }

        private void DevelopmentTools()
        {
            var content = Modal("개발용 검증", 300);
            float y = SheetParagraph(content, "다음 날로 이동하면 현재 저장의 날짜와 성장 기록이 바뀝니다. 테스트용 저장에서 사용해 주세요.", 8, 18) + 18;
            var error = SheetValidation(content, y);
            y += 60;
            _ui.Button("DebugNextDay", "검증용 · 다음 날로 이동", content, SheetInset, y, SheetInnerWidth, 58,
                () => CommitDialog(() => _app.AdvanceLifeDay(), error, "검증을 위해 다음 날로 이동했어요."), true, 18);
            SheetContentHeight(content, y + 58);
        }

        private void Eggs()
        {
            var content = Modal("다음 계절의 첫 만남", 380);
            string[] names = { "햇살을 닮은 알", "이슬을 닮은 알", "바람을 닮은 알" };
            string[] shortNames = { "햇살", "이슬", "바람" };
            string[] descriptions = { "가만히 손을 대니\n따뜻한 기운이 톡톡 느껴져요.", "작은 풀잎 사이로\n잔잔하고 맑은 기운이 흘러요.", "어디선가 불어온 바람처럼\n작은 호기심이 살랑거려요." };
            ForestIcon[] icons = { ForestIcon.Sun, ForestIcon.Leaf, ForestIcon.Compass };
            int selected = 0;
            var options = new Button[3];
            var pictures = new RectTransform[3];
            float cell = (SheetInnerWidth - 12) / 3;
            var title = _ui.Text(names[0], content, SheetInset, 217, SheetInnerWidth, 43, 24, ForestUi.Ink, true, TextAnchor.MiddleCenter);
            var words = _ui.Text(descriptions[0], content, SheetInset, 269, SheetInnerWidth, 90, 18, ForestUi.Muted, false, TextAnchor.MiddleCenter);
            float descriptionHeight = 0;
            foreach (string description in descriptions) { words.text = description; descriptionHeight = Mathf.Max(descriptionHeight, words.preferredHeight + 6); }
            words.rectTransform.sizeDelta = new Vector2(SheetInnerWidth, descriptionHeight);
            Action refresh = () =>
            {
                for (int i = 0; i < options.Length; i++)
                {
                    SetSheetSelection(options[i], i == selected);
                    pictures[i].localScale = Vector3.one * (i == selected ? 1.12f : .87f);
                }
                title.text = names[selected];
                words.text = descriptions[selected];
            };
            for (int i = 0; i < names.Length; i++)
            {
                int egg = i;
                options[i] = _ui.Button("Egg" + i, "", content, SheetInset + i * (cell + 6), 8, cell, 180, () => { selected = egg; refresh(); });
                var artRoot = _ui.Rect("EggPicture", options[i].transform, cell / 2, 76, cell, 126);
                artRoot.pivot = new Vector2(.5f, .5f);
                SheetArt("Egg", JourneyArt(6 + i), artRoot, 0, 0, cell, 126, icons[i]);
                pictures[i] = artRoot;
                _ui.Text(shortNames[i], options[i].transform, 4, 141, cell - 8, 32, 16, ForestUi.Ink, true, TextAnchor.MiddleCenter);
            }
            refresh();
            float y = SheetParagraph(content, "어떤 미래로 자랄지는,\n우리가 함께한 날과 선택이 알려 줄 거예요.", 291 + descriptionHeight, 15) + 8;
            var error = SheetValidation(content, y, 24); error.gameObject.SetActive(false);
            SheetPrimaryAction(content, "WelcomeEgg", "이 친구와 다음 계절 시작하기",
                () => {
                    if (CommitDialog(() => _app.SelectNextEgg(selected, PrototypeBootstrap.Current.ResolveToday()), error, "새로운 동행이 시작됐어요.", "wave"))
                        EarlyNameDialog();
                });
            SheetContentHeight(content, y + 4);
        }

        private void NameCompanionDialog()
        {
            var content = Modal("오래 불러 줄 이름", 430);
            var photograph = _ui.Panel("NamingPortrait", content, (SheetWidth - 160) / 2, 7, 160, 163, Color.white);
            photograph.rectTransform.localRotation = Quaternion.Euler(0, 0, -3);
            var backdrop = _ui.Panel("PortraitBacking", photograph.transform, 8, 8, 144, 119, SheetBlush);
            _ui.Sprite("RememberedSpirit", PosesForStage(_app.CurrentLife.Stage)[2], backdrop.transform, 18, 0, 108, 115);
            _ui.Text("우리의 " + _app.ForestActiveDays + "일", photograph.transform, 10, 126, 140, 32, 15, ForestUi.Muted, false, TextAnchor.MiddleCenter);
            float y = SheetParagraph(content, "함께한 날을 떠올리면,\n어떤 이름이 가장 먼저 생각나나요?", 192, 19, ForestUi.Ink) + 19;
            var input = _ui.Input(content, SheetInset, y, SheetInnerWidth, "정령의 이름 · 1~12자", 80);
            y += 70;
            var count = _ui.Text("0 / 12", content, SheetInset, y, SheetInnerWidth, 26, 14, ForestUi.Muted, false, TextAnchor.MiddleRight);
            input.onValueChanged.AddListener(value =>
            {
                int length = new StringInfo((value ?? "").Trim()).LengthInTextElements;
                count.text = length + " / 12";
                count.color = length > 12 ? SheetError : ForestUi.Muted;
            });
            y += 39;
            y = SheetParagraph(content, "앨범에도, 다시 만날 날의 편지에도\n이 이름을 소중히 담아 둘게요.", y, 15) + 7;
            var error = SheetValidation(content, y, 24); error.gameObject.SetActive(false);
            SheetPrimaryAction(content, "NameCompanion", "이 이름으로 불러 줄게",
                () => CommitDialog(() => _app.NameCurrentCompanion(input.text), error, "우리만의 이름을 마음에 새겼어요.", "happy"));
            SheetContentHeight(content, y + 5);
        }

        private void Reunion()
        {
            var guest = _app.ReunionGuest;
            if (guest == null) return;
            var content = Modal("익숙한 바람이 불어와요", 390);
            var stage = SheetStage(content, 3, 201);
            SheetArt("SealedLetter", FurnishingArt(5), stage, (SheetInnerWidth - 165) / 2, 26, 165, 164, ForestIcon.Mail);
            _ui.Sprite("ReturningPortrait", PosesForStage(guest.Stage)[2], stage, SheetInnerWidth - 83, 102, 80, 83);
            var from = SheetLabel(guest.DisplayName + "에게서 온 편지", content, SheetInset, 227, SheetInnerWidth,
                24, ForestUi.Ink, true, TextAnchor.MiddleCenter, 42);
            float y = 246 + from.rectTransform.sizeDelta.y;
            y = SheetParagraph(content, "네가 응원해 준 " + CompanionLifeRules.RouteName(guest.Route) + "로 지내고 있어.\n들려주고 싶은 이야기가 있어서 왔어.", y, 18, ForestUi.Muted) + 16;
            var error = SheetValidation(content, y, 24); error.gameObject.SetActive(false);
            SheetPrimaryAction(content, "OpenReunionLetter", "편지 열어 보기", () =>
            {
                if (CommitDialog(() => _app.ClaimForestReunion(), error, null, "wave")) ShowMemoryLetter(guest);
            });
            SheetContentHeight(content, y + 6);
        }

        private void ShowMemoryLetter(CompanionLife companion)
        {
            if (companion == null || string.IsNullOrEmpty(companion.Story.ReunionLetter)) return;
            var content = Modal("오래 간직할 편지", 680);
            var portrait = _ui.Panel("LetterPortrait", content, SheetInset, 5, 83, 100, Color.white);
            portrait.rectTransform.localRotation = Quaternion.Euler(0, 0, -4);
            _ui.Panel("PortraitTint", portrait.transform, 5, 5, 73, 74, SheetBlush);
            _ui.Sprite("SenderPortrait", PosesForStage(companion.Stage)[2], portrait.transform, 9, 4, 68, 77);
            _ui.Text("다시 만난 날", portrait.transform, 2, 77, 79, 22, 11, ForestUi.Muted, false, TextAnchor.MiddleCenter);
            var sender = SheetLabel(companion.DisplayName + "의 안부", content, SheetInset + 102, 15, SheetInnerWidth - 102, 22, ForestUi.Ink, true);
            float senderBottom = 18 + sender.rectTransform.sizeDelta.y;
            var date = SheetLabel(companion.Story.ReunionDate, content, SheetInset + 102, senderBottom, SheetInnerWidth - 102, 14, ForestUi.Muted);
            float y = Mathf.Max(125, senderBottom + date.rectTransform.sizeDelta.y + 20);
            var paper = _ui.Panel("MemoryLetterPaper", content, SheetInset, y, SheetInnerWidth, 400, new Color32(255, 252, 240, 255));
            _ui.Icon(ForestIcon.Leaf, paper.transform, SheetInnerWidth - 43, 16, 23, 23, ForestUi.Moss);
            _ui.Text("나의 소중한 친구에게", paper.transform, 20, 18, SheetInnerWidth - 72, 37, 15, ForestUi.Rose, true);
            string paragraphs = companion.Story.ReunionLetter.Replace(". ", ".\n\n");
            var letter = SheetLabel(paragraphs, paper.transform, 20, 69, SheetInnerWidth - 40, 19, ForestUi.Ink);
            float paperHeight = 69 + letter.rectTransform.sizeDelta.y + 22;
            _ui.Divider(paper.transform, 20, paperHeight, SheetInnerWidth - 40, new Color32(228, 218, 194, 255));
            var signature = SheetLabel(companion.DisplayName + "가", paper.transform, 20, paperHeight + 15,
                SheetInnerWidth - 40, 18, ForestUi.Moss, true, TextAnchor.MiddleRight);
            paperHeight += signature.rectTransform.sizeDelta.y + 40;
            paper.rectTransform.sizeDelta = new Vector2(SheetInnerWidth, paperHeight);
            y += paperHeight + 26;
            SheetArt("ReunionGiftArt", JourneyArt(3 + (int)companion.Route), content, (SheetWidth - 118) / 2, y, 118, 119, ForestIcon.Gift);
            y += 128;
            var giftTitle = SheetLabel(companion.Story.ReunionGift, content, SheetInset, y, SheetInnerWidth, 20, ForestUi.Ink, true, TextAnchor.MiddleCenter);
            y += giftTitle.rectTransform.sizeDelta.y + 8;
            y = SheetParagraph(content, "너를 생각하며 가져왔어.\n우리의 편지와 선물은 앨범에 간직해 둬.", y, 15) + 10;
            SheetPrimaryAction(content, "Okay", "다시 와 줘서 고마워", CloseModal);
            SheetContentHeight(content, y);
        }

        private void Decorations()
        {
            var content = Modal("작은 취향을 놓는 곳", 860);
            _selectedDecoration = Mathf.Clamp(_selectedDecoration, 0, CompanionItemCatalog.Decorations.Length - 1);
            string[] descriptions = { "한 장씩 모아 둔 숲의 이야기", "만들고 고치는 작은 즐거움", "따뜻한 차 한 잔의 여유", "매일 조금씩 자라는 초록", "마음 놓고 쉬는 포근한 자리" };
            ForestIcon[] icons = { ForestIcon.Book, ForestIcon.Gift, ForestIcon.Heart, ForestIcon.Leaf, ForestIcon.Moon };
            var stage = SheetStage(content, 2, 190);
            _ui.Badge("미리 보기", stage, 12, 12, 88, 29, ForestUi.Paper, ForestUi.Moss);
            _ui.Badge(_app.EssenceWallet.Balance + " 정수", stage, SheetInnerWidth - 121, 12, 109, 29, ForestUi.Paper, ForestUi.Moss);
            SheetActor(stage, 14, 39, 136);
            var preview = _ui.Sprite("DecorationPreview", FurnishingArt(_selectedDecoration), stage, SheetInnerWidth - 174, 43, 162, 145);
            var heading = _ui.Text("", content, SheetInset, 211, SheetInnerWidth, 45, 23, ForestUi.Ink, true, TextAnchor.MiddleCenter);
            var caption = _ui.Text("", content, SheetInset, 261, SheetInnerWidth, 52, 16, ForestUi.Muted, false, TextAnchor.MiddleCenter);
            _ui.Text("우리의 보관함", content, SheetInset, 324, SheetInnerWidth, 33, 17, ForestUi.Ink, true);
            float y = 368;
            float cell = (SheetInnerWidth - 12) / 2;
            var tiles = new Button[CompanionItemCatalog.Decorations.Length];
            Button primary = null;
            var stateText = _ui.Text("", content, SheetInset, y + 3 * 212 + 10, SheetInnerWidth, 64, 15, ForestUi.Muted);
            var error = SheetValidation(content, y + 3 * 212 + 82, 24); error.gameObject.SetActive(false);
            Action refresh = () =>
            {
                string id = _selectedDecoration.ToString(CultureInfo.InvariantCulture);
                bool owned = _app.Interventions.OwnedDecorations.Contains(id);
                bool equipped = _app.Interventions.EquippedDecorations.Contains(id);
                preview.sprite = FurnishingArt(_selectedDecoration);
                heading.text = CompanionItemCatalog.Decorations[_selectedDecoration];
                caption.text = descriptions[_selectedDecoration];
                for (int n = 0; n < tiles.Length; n++) SetSheetSelection(tiles[n], n == _selectedDecoration);
                stateText.text = equipped ? "우리 숲에 놓여 있어요. 언제든 보관함으로 다시 가져올 수 있어요." : owned ? "이미 가진 물건이에요. 정수 없이 숲에 놓을 수 있어요." :
                    _app.EssenceWallet.Balance < CompanionItemCatalog.DecorationPrice ? "이 물건은 30 정수예요. 지금은 정수가 조금 더 필요해요." : "30 정수로 가져온 뒤, 원할 때 숲에 놓을 수 있어요.";
                stateText.rectTransform.sizeDelta = new Vector2(SheetInnerWidth, stateText.preferredHeight + 5);
                error.rectTransform.anchoredPosition = new Vector2(SheetInset, stateText.rectTransform.anchoredPosition.y - stateText.rectTransform.sizeDelta.y - 8);
                if (primary != null)
                {
                    primary.GetComponentInChildren<Text>().text = equipped ? "보관함에 담기" : owned ? "우리 숲에 놓기" : "내 물건으로 가져오기 · 30";
                    primary.interactable = owned || _app.EssenceWallet.Balance >= CompanionItemCatalog.DecorationPrice;
                }
            };
            for (int i = 0; i < CompanionItemCatalog.Decorations.Length; i++)
            {
                int item = i;
                string id = i.ToString(CultureInfo.InvariantCulture);
                bool owned = _app.Interventions.OwnedDecorations.Contains(id);
                bool equipped = _app.Interventions.EquippedDecorations.Contains(id);
                tiles[i] = _ui.Button("Decoration" + i, "", content, SheetInset + i % 2 * (cell + 12), y + i / 2 * 212,
                    cell, 200, () => { _selectedDecoration = item; refresh(); });
                SheetArt("InventoryItem", FurnishingArt(i), tiles[i].transform, (cell - 102) / 2, 6, 102, 100, icons[i]);
                SheetLabel(CompanionItemCatalog.Decorations[i], tiles[i].transform, 8, 111, cell - 16, 15, ForestUi.Ink, true, TextAnchor.MiddleCenter, 48);
                _ui.Text(equipped ? "숲에 놓인 물건" : owned ? "내가 가진 물건" : "30 정수", tiles[i].transform, 8, 169, cell - 16, 25, 14,
                    equipped ? ForestUi.Moss : ForestUi.Muted, false, TextAnchor.MiddleCenter);
            }
            primary = SheetPrimaryAction(content, "PlaceDecoration", "", () =>
            {
                int item = _selectedDecoration;
                string id = item.ToString(CultureInfo.InvariantCulture);
                bool owned = _app.Interventions.OwnedDecorations.Contains(id);
                bool equipped = _app.Interventions.EquippedDecorations.Contains(id);
                if (CommitDialog(() =>
                {
                    if (owned) _app.EquipDecoration(item, !equipped);
                    else _app.BuyDecoration(item);
                }, error, owned ? equipped ? "물건을 보관했어요." : "숲에 작은 취향이 더해졌어요." : "새 물건이 보관함에 들어왔어요.")) Decorations();
            });
            refresh();
            SheetContentHeight(content, -error.rectTransform.anchoredPosition.y + 7);
        }

        private void Travel()
        {
            if (_app.PendingTrip != null) { ShowJourneyProgress(); return; }
            var sheet = Modal("숲 너머로 작은 모험", 520);
            _ui.Badge("4시간의 나들이", sheet, SheetInset, 2, 127, 32, SheetSage);
            _ui.Badge(_app.EssenceWallet.Balance + " 정수", sheet, SheetWidth - 129, 2, 107, 32, SheetBlush, ForestUi.Rose);
            var stage = SheetStage(sheet, 51, 186);
            SheetActor(stage, (SheetInnerWidth - 155) / 2 - 35, 15, 155, "curious");
            Sprite[] pictures = { tripLunchSprite ?? JourneyArt(1), JourneyArt(1), JourneyArt(0) };
            var packed = _ui.Sprite("PackedItem", pictures[0], stage, SheetInnerWidth - 114, 70, 102, 102);
            string[] descriptions = { "처음 만난 이웃과 나누면\n새로운 이야기가 시작될지도 몰라요.", "숲길에서 마음에 드는 것을 만나면\n조심조심 담아 올 수 있어요.", "지나치기 쉬운 나뭇잎과 바람도\n한 장의 기록으로 남길 수 있어요." };
            string[] shortNames = { "도시락", "빈 가방", "관찰 수첩" };
            ForestIcon[] icons = { ForestIcon.Heart, ForestIcon.Gift, ForestIcon.Book };
            int selected = 0;
            var choices = new Button[3];
            var title = _ui.Text(CompanionItemCatalog.Preparations[0], sheet, SheetInset, 256, SheetInnerWidth, 43, 23, ForestUi.Ink, true, TextAnchor.MiddleCenter);
            var detail = _ui.Text(descriptions[0], sheet, SheetInset, 306, SheetInnerWidth, 100, 17, ForestUi.Muted, false, TextAnchor.MiddleCenter);
            Action refresh = () =>
            {
                for (int i = 0; i < choices.Length; i++) SetSheetSelection(choices[i], selected == i);
                packed.sprite = pictures[selected];
                title.text = CompanionItemCatalog.Preparations[selected];
                detail.text = descriptions[selected];
            };
            float rowY = 424;
            float cell = (SheetInnerWidth - 16) / 3;
            for (int i = 0; i < 3; i++)
            {
                int preparation = i;
                choices[i] = _ui.Button("Trip" + i, "", sheet, SheetInset + i * (cell + 8), rowY, cell, 118,
                    () => { selected = preparation; refresh(); }, false);
                SheetArt("Preparation", pictures[i], choices[i].transform, (cell - 66) / 2, 6, 66, 66, icons[i]);
                if (i == 0 && tripLunchSprite == null) _ui.Icon(ForestIcon.Heart, choices[i].transform, cell - 26, 51, 17, 17, ForestUi.Rose);
                _ui.Text(shortNames[i], choices[i].transform, 3, 80, cell - 6, 31, 14, ForestUi.Ink, true, TextAnchor.MiddleCenter);
            }
            refresh();
            rowY += 140;
            bool affordable = _app.EssenceWallet.Balance >= CompanionItemCatalog.TravelPrice;
            rowY = SheetParagraph(sheet, affordable ? "돌아올 때까지 앱을 닫아도 괜찮아요.\n네가 일상을 보내는 동안 숲을 둘러볼게요." : "나들이에는 40 정수가 필요해요.\n지금은 우리 숲에서 함께 쉬어 가도 좋아요.", rowY, 15) + 7;
            var tripError = SheetValidation(sheet, rowY, 24); tripError.gameObject.SetActive(false);
            var start = SheetPrimaryAction(sheet, "StartTrip", "이렇게 챙겨 보내기 · 40 정수", () =>
            {
                if (CommitDialog(() => _app.StartTrip(selected, DateTime.UtcNow), tripError)) ShowJourneyProgress();
            });
            start.interactable = affordable;
            SheetContentHeight(sheet, rowY + 5);
        }

        private void ShowJourneyProgress()
        {
            var trip = _app.PendingTrip;
            if (trip == null) return;
            var content = Modal("작은 모험의 한가운데", 400);
            var stage = SheetStage(content, 3, 198);
            SheetArt("JourneyMap", JourneyArt(4), stage, SheetInnerWidth - 135, 42, 122, 142, ForestIcon.Compass);
            var actor = SheetActor(stage, 23, 31, 151, "wave");
            actor.Activity = "walk";
            _ui.Badge("우리 숲", content, SheetInset, 221, 75, 31, SheetSage);
            _ui.Badge("숲 너머", content, SheetWidth - 97, 221, 75, 31, SheetBlush, ForestUi.Rose);
            var track = _ui.Panel("JourneyTrack", content, SheetInset + 10, 269, SheetInnerWidth - 20, 8, SheetSage);
            var fill = _ui.Panel("JourneyProgress", track.transform, 0, 0, 1, 8, ForestUi.Moss).rectTransform;
            var status = _ui.Text("숲길을 걷고 있어요", content, SheetInset, 301, SheetInnerWidth, 55, 22, ForestUi.Ink, true, TextAnchor.MiddleCenter);
            var clock = _ui.Text("", content, SheetInset, 364, SheetInnerWidth, 65, 16, ForestUi.Muted, false, TextAnchor.MiddleCenter);
            float y = SheetParagraph(content, "잠시 일상을 보내고 와도 괜찮아요.\n발견한 이야기를 소중히 챙겨 올게요.", 448, 16) + 8;
            var error = SheetValidation(content, y, 24); error.gameObject.SetActive(false);
            var action = SheetPrimaryAction(content, "TripReturn", "우리 숲으로 돌아가기", () =>
            {
                if (!_app.CanClaimTrip(DateTime.UtcNow)) { CloseModal(); return; }
                CompanionTrip completed = null;
                if (CommitDialog(() => completed = _app.ClaimTrip(DateTime.UtcNow), error, null, "happy")) ShowJourneyMemory(completed);
            });
            SheetContentHeight(content, y + 5);
            StartCoroutine(UpdateJourneyProgress(_modal, trip, fill, status, clock, action, actor));
        }

        private IEnumerator UpdateJourneyProgress(RectTransform modal, CompanionTrip trip, RectTransform fill, Text status, Text clock, Button action, ForestSpiritAnimator actor)
        {
            bool greeted = false;
            while (modal != null && modal == _modal)
            {
                var returns = DateTime.Parse(trip.ReturnAtUtc, CultureInfo.InvariantCulture, DateTimeStyles.RoundtripKind);
                TimeSpan remaining = returns - DateTime.UtcNow;
                bool ready = remaining <= TimeSpan.Zero;
                int minutes = Mathf.Max(1, Mathf.CeilToInt((float)remaining.TotalMinutes));
                string duration = minutes >= 60 ? minutes / 60 + "시간" + (minutes % 60 == 0 ? "" : " " + minutes % 60 + "분") : minutes + "분";
                float progress = Mathf.Clamp01(1 - (float)(remaining.TotalHours / 4));
                fill.sizeDelta = new Vector2((SheetInnerWidth - 20) * progress, 8);
                status.text = ready ? "다시 돌아왔어요" : "숲길을 걷는 중";
                clock.text = ready ? "작은 발견을 챙겨 돌아왔어요." :
                    duration + " 뒤 만나요\n" + returns.ToLocalTime().ToString("M월 d일 HH:mm", CultureInfo.InvariantCulture) + " 도착 예정";
                action.GetComponentInChildren<Text>().text = ready ? "나들이 이야기 듣기" : "우리 숲으로 돌아가기";
                if (ready && !greeted) { actor.Activity = "idle"; actor.Perform("happy"); greeted = true; }
                yield return new WaitForSecondsRealtime(1);
            }
        }

        private void ShowJourneyMemory(CompanionTrip trip, Action returnToMemory = null, ForestDailyMemory memory = null)
        {
            if (trip == null || !trip.Claimed) return;
            var content = Modal("숲길에서 가져온 하루", 480);
            var postcard = _ui.Panel("JourneyPostcard", content, SheetInset, 5, SheetInnerWidth, 199, Color.white);
            postcard.rectTransform.localRotation = Quaternion.Euler(0, 0, -2);
            var landscape = _ui.Panel("PostcardLandscape", postcard.transform, 8, 8, SheetInnerWidth - 16, 148, SheetSage);
            landscape.gameObject.AddComponent<RectMask2D>();
            float landscapeWidth = SheetInnerWidth - 16;
            float landscapeHeight = landscapeWidth * 1.5f;
            var forest = _ui.Sprite("PostcardForest", forestBackground, landscape.transform, 0,
                -(landscapeHeight - 148) * .45f, landscapeWidth, landscapeHeight);
            forest.preserveAspect = false;
            float portraitSize = Mathf.Min(114, landscapeWidth * .46f);
            var traveler = _app.SaveData.Companions.Find(life => life.SpiritId == trip.SpiritId) ?? _app.CurrentLife;
            var appearance = memory != null ? memory.AppearancePhase : traveler.Stage;
            _ui.Sprite("TravelPortrait", PosesForStage(appearance)[2], landscape.transform, landscapeWidth * .18f - 20,
                139 - portraitSize, portraitSize, portraitSize);
            float mapSize = Mathf.Min(101, landscapeWidth * .4f);
            SheetArt("ForestMapMemory", JourneyArt(4), landscape.transform,
                landscapeWidth - mapSize - 13, 139 - mapSize, mapSize, mapSize, ForestIcon.Compass);
            _ui.Text(trip.StartedOn + " · 작은 나들이", postcard.transform, 12, 160, SheetInnerWidth - 24, 30, 15, ForestUi.Muted, false, TextAnchor.MiddleCenter);
            string preparation = trip.Preparation >= 0 && trip.Preparation < CompanionItemCatalog.Preparations.Length
                ? CompanionItemCatalog.Preparations[trip.Preparation] : "준비물 기록 없음";
            float y = SheetParagraph(content, traveler.DisplayName + " · " + preparation + "과 함께한 나들이", 224, 16, ForestUi.Moss) + 16;
            y = SheetParagraph(content, trip.EventText, y, 20, ForestUi.Ink) + 25;
            var discovery = _ui.Panel("JourneyDiscovery", content, SheetInset, y, SheetInnerWidth, 120, SheetBlush);
            _ui.Icon(ForestIcon.Leaf, discovery.transform, 17, 20, 26, 26, ForestUi.Moss);
            _ui.Text("소중히 챙겨 온 것", discovery.transform, 56, 15, SheetInnerWidth - 72, 29, 14, ForestUi.Muted);
            var found = SheetLabel(trip.Discovery, discovery.transform, 56, 49, SheetInnerWidth - 72, 20, ForestUi.Ink, true);
            float discoveryHeight = 65 + found.rectTransform.sizeDelta.y;
            discovery.rectTransform.sizeDelta = new Vector2(SheetInnerWidth, discoveryHeight);
            y += discoveryHeight + 17;
            SheetPrimaryAction(content, "Okay", returnToMemory == null ? "이 이야기도 함께 기억할게" : "이날의 기억으로 돌아가기",
                returnToMemory ?? (Action)CloseModal);
            SheetContentHeight(content, y);
        }
    }
}
