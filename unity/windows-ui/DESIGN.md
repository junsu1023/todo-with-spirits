# Windows 앱 초기 화면 사양

2026-11-25 초안. 현재 화면 시안은 [design/index.html](design/index.html)을 기준으로 한다.

- **기준:** Figma `OOW` › "TODO 최종 와이어 & UI" › **Mobile_GUI** (확정된 모바일 UI)
- **목표:** 모바일 화면 구성과 스타일은 유지하고, 윈도우 데스크톱 앱다운 형태로 옮긴다. 넓은 창, 사이드바, 마우스 hover와 우클릭, 키보드 단축키, 다이얼로그·플라이아웃, 트레이와 알림이 해당한다.
- **참고 이미지:** [`ref/`](ref/) 폴더. 01–15는 Figma 모바일 캡처, 16–19는 숲 프로토타입 캡처다.
- **표기:**
  - **[Figma]** Mobile_GUI에 있는 기능
  - **[Windows]** 윈도우 형태로 바꾸면서 추가한 동작
  - **[미정]** Figma에서 확정되지 않아 그대로 둔 부분

---

## 1. 공통 스타일

```
BRAND STYLE (from the mobile app, must match):
- App background #FDFBFA. Secondary background #F6F3F1. Cards #FFFFFF with 1px border #ECEAE4, corner radius 12px, almost no shadow.
- Main text and icons warm brown #857563. Secondary labels light brown #CFB6A0. Disabled/inactive #C9C9C2.
- Primary action: lime #C1DD00 with white text; pressed #A3B90E; light lime tint #EDFCB7 for selection and today.
- Accent: pink #FF96DE for "important" stars, schedule dots and badges; strong pink #F168C6; light pink #FFE6F6 for "중요" tags.
- Selected tab/segment: filled brown #857563 with white text.
- Thin outline icons (Tabler-style, 1.5px stroke). Font: NanumSquareRound (나눔스퀘어라운드), weights 400 / 700 / 800. Soft, cozy, airy, generous whitespace.
- Spirit characters are soft pastel illustrations (pink fluffy cloud, green sprout with pink petals, orange flame, light-blue water drop, yellow star child).

WINDOWS FORM:
- Window 1280x800 with a 32px Windows title bar: small app icon + app name on the left, minimize/maximize/close on the right, title bar color #FDFBFA.
- Left navigation pane 240px, background #F6F3F1, collapsible to a 64px icon rail with a hamburger button. Items: "홈", "캘린더", "정령 도감", "정령의 숲" (lock icon). Bottom of pane: user avatar + nickname "댕트리버", and "설정".
- Content top bar (56px): page title on the left; on the right a search box "일정 검색" and a bell icon with pink badge "99+". No settings gear in the top bar (settings lives only in the navigation pane).
- The window is a FIXED 1280x800 frame. The whole page must never scroll; title bar, navigation pane and top bar always stay visible. Only inner lists and panels scroll, with a thin overlay scrollbar.
- Never duplicate navigation: no shortcut tiles or buttons that repeat navigation pane items.
- Do not bake hover states into the default frame. Hover actions (edit/delete icons) appear only on hover; draw hover in a separate frame.
- Keyboard shortcuts appear only in tooltips and menus, never as text inside buttons or placeholders.
- Use #857563 for main text exactly as given (do not darken it). Avatar is a round initial badge "댕" on #FFE6F6; no external images.
- Use Windows patterns: hover highlight #F6F3F1 on rows, visible keyboard focus ring (2px #857563), right-click context menus with rounded 8px corners, tooltips, modal dialogs centered over a dimmed window, side flyouts sliding in from the right, thin overlay scrollbars.
- Desktop density: 14px body text, 40px row height, 8px spacing grid.
```

---

## 2. 모바일 → 윈도우 변환 규칙

| 모바일 (Figma) | 윈도우 | 이유 |
|---|---|---|
| 상단 상태바·하단 홈 인디케이터 | 32px 제목 표시줄 (최소화·최대화·닫기) | 윈도우 창 형태 |
| 홈 하단 바로가기 3개 (정령 도감·정령의 숲·캘린더) | 왼쪽 내비게이션 (홈·캘린더·정령 도감·정령의 숲) | 데스크톱 기본 내비게이션 |
| 뒤로가기 `<` 헤더 | 사이드바 선택 + 상단 페이지 제목. 하위 화면만 브레드크럼 | 화면 이동은 사이드바가 맡음 |
| 홈 "오늘 일정" 접힘 카드 | 왼쪽에 펼쳐진 오늘 목록, 오른쪽에 정령 | 넓은 화면 |
| 일정 탭 → 삭제/수정 팝업 | hover 시 수정·삭제 아이콘, 우클릭 메뉴, 클릭 시 오른쪽 상세 패널 | 마우스 조작 |
| 일정 추가 전체 화면 | 가운데 모달 다이얼로그 (480px) | 맥락 유지 |
| 캘린더 위 달력 + 아래 목록 | 왼쪽 큰 달력 + 오른쪽 선택한 날짜 목록 | 2단 |
| 시간 휠 피커 | 시·분 스핀 입력 + 드롭다운 (15분 단위) | 마우스·키보드 입력 |
| 도감 3열 + 힌트 팝업 | 5열 그리드 + 오른쪽 상세 패널 | 넓은 화면 |
| 알림 전체 화면 | 벨 아이콘 → 오른쪽 플라이아웃 | 어느 화면에서나 확인 |
| 설정 목록 → 하위 화면 | 2단 설정 (왼쪽 항목, 오른쪽 내용) | Windows 설정 앱 관례 |
| 로그인 세로 화면 | 창 가운데 카드 (왼쪽 일러스트, 오른쪽 폼) | 데스크톱 비율 |
| 모바일 키보드 | 키보드 단축키, Enter 제출, Esc 닫기 | 데스크톱 입력 |
| 푸시 알림 | Windows 토스트 알림 + 트레이 아이콘 | OS 관례 |

### 키보드 단축키 [Windows]

| 키 | 동작 |
|---|---|
| Ctrl+N | 일정 추가 다이얼로그 |
| Ctrl+F | 일정 검색 |
| Ctrl+1 / 2 / 3 / 4 | 홈 / 캘린더 / 정령 도감 / 정령의 숲 |
| Ctrl+, | 설정 |
| ← / → (캘린더) | 이전·다음 날짜, PageUp/PageDown은 이전·다음 달 |
| Space (일정 선택 시) | 완료 체크 |
| Delete (일정 선택 시) | 삭제 확인 |
| Enter / Esc (다이얼로그) | 확인 / 닫기 |

### 창 크기 기준 [Windows]

| 너비 | 레이아웃 |
|---|---|
| 1280 이상 | 사이드바 240px, 2단 콘텐츠 |
| 960–1279 | 사이드바 64px 아이콘, 2단 콘텐츠 (오른쪽 패널 좁힘) |
| 960 미만 (최소 800×600) | 사이드바 64px, 오른쪽 패널은 오버레이로 열림 |

---

## 3. 화면별 구성과 기능

### W01. 홈 (오늘)

**참고 이미지:** `01_home.png`, `02_home_expanded.png`

```
[Paste section 1 common instructions above this line]

Screen: Home. Navigation item "홈" is selected. Fixed 1280x800 frame, no page scroll.

Top bar: date title "10월 13일 (화)" in brown on the left; search box and bell with "99+" badge on the right.

The content area below the top bar is split into two columns that both fill the full remaining height (24px padding, 24px gap):

LEFT (480px wide, full height): one white card "오늘 일정".
- Card header: "오늘 일정" on the left; on the right big number "8" with smaller "/ 10".
- Chips row: pink-tint toggle chip "★ 중요 일정이 3건 있어요!" and lime-tint toggle chip "✓ 완료 (2)".
- Task list that scrolls inside the card when long. Unfinished tasks first, sorted by time ascending: "디자인 시스템 토큰 정리 ★ 14시 30분", "성과 보고서 초안 제출 ★ 16시 00분", "민지랑 저녁 ★ 17시 50분", "필라테스 수업 20시 00분", "고양이 간식 주문하기 21시 15분". Each 44px row: round brown-outline checkbox, title, pink ★ if important, light-brown time right-aligned. No hover icons in this default frame.
- Then a collapsible divider "완료 2 ⌄" followed by completed rows (filled brown check, strikethrough gray title): "비타민 챙겨 먹기 09시 00분", "오전 스크럼 회의 10시 00분".
- Card footer pinned to the bottom of the card: lime full-width button "+ 일정 추가".

RIGHT (remaining width, full height): the spirit stage, with no card or box around it. The pink fluffy cloud spirit is large (about 320px), centered slightly below the middle, on a transparent background with a very soft round pink shadow on the floor under it. A white speech bubble with a small tail pointing down at the spirit sits just above its head: "얼마 안남았어요!", with two tiny pink hearts floating at the bubble's top-right corner. Small caption under the spirit: "정령을 눌러 말을 걸어보세요". No shortcut tiles anywhere.
```

추가 프레임 (첫 결과가 맞으면 이어서 요청):
```
Add three more frames of the same Home screen:
1) Hover: mouse over the "민지랑 저녁" row. Row background #F6F3F1; small pencil and trash icons appear before the time; tooltip "수정" above the pencil.
2) Right-click menu on that row: "완료 표시", "수정", "중요 표시 해제", divider, "삭제" in soft red. Shortcut hints right-aligned in light gray: "Space", "Enter", "", "Del".
3) Empty day: the card shows a small spirit line icon and "오늘은 아직 일정이 없어요" with the lime "+ 일정 추가" button; the speech bubble says "오늘은 천천히 쉬어가요".
```

**기능 명세**
- [Figma] 날짜 표시 (예: `10월 13일 (화)`)
- [Figma] 오늘 일정 개수 `8 / 10`
- [미정] 큰 숫자 8이 남은 일정인지 완료한 일정인지. 같은 화면의 "완료 (2)" 칩과 함께 보면 남은 일정으로 읽힌다. 일정을 체크하면 숫자와 칩이 함께 바뀌어야 한다.
- [Figma] 칩 "중요 일정이 N건 있어요!"를 누르면 중요 일정만 보여 준다. 칩 "완료 (N)"를 누르면 완료 구역을 펼치거나 접는다.
- [Figma] 일정 체크로 완료/완료 취소, 중요 일정에 ★ 표시
- [Figma] 정령 말풍선: 상황 문구를 보여 준다 (예: "얼마 안남았어요!")
- [Figma] 벨 배지는 99개를 넘으면 `99+`
- [Windows] 모바일 홈의 바로가기 3개(정령 도감·정령의 숲·캘린더)는 사이드바가 대신한다. 홈에 다시 넣지 않는다.
- [Windows] 목록은 접지 않는다. 미완료 일정을 시간순으로 먼저 보여 주고, 완료한 일정은 아래 "완료" 구역에 모은다. 목록이 길면 카드 안에서만 스크롤한다.
- [Windows] 창 전체는 스크롤하지 않는다.
- [Windows] 일정에 마우스를 올리면 수정·삭제 아이콘이 나온다. 클릭하면 W02 상세 패널이 열리고, 우클릭하면 W02 메뉴가 열린다.
- [Windows] 정령을 클릭하면 다른 대사를 보여 준다 (대사 목록은 [미정])
- [Windows] Ctrl+N으로 W04를 연다.

### W02. 일정 상세 · 우클릭 메뉴

**참고 이미지:** `02_home_expanded.png`

```
Same design system and shell as the Home screen.

Show the Home screen with a task row "민지랑 저녁 ★" selected.

1) A right-click context menu next to the row (white, radius 8px, soft shadow), items with icons and shortcut hints: "완료 표시  Space", "수정", "중요 표시 해제", divider, "삭제  Del" in soft red.

2) As a second frame: a right-side detail flyout (360px, white, left border #ECEAE4) for the selected task: title "민지랑 저녁" with pink ★, subtitle "10월 17일 (토) 17시 50분", rows "반복: 없음", "알림: 없음", and two buttons at the bottom like the mobile popup: outline soft-red button with trash icon "삭제" and outline brown button with pencil icon "수정". Close X at the top right.

3) As a third frame: delete confirmation dialog centered over the dimmed window: "이 일정을 삭제할까요?", body "삭제한 일정은 되돌릴 수 없어요.", buttons "취소" and soft-red "삭제".
```

**기능 명세**
- [Figma] 일정 팝업: 제목, ★, 날짜·시간, 삭제, 수정
- [Windows] 우클릭 메뉴 항목: 완료 표시, 수정, 중요 표시/해제, 삭제
- [Windows] 삭제는 확인 다이얼로그를 거친다. 되돌리기(Ctrl+Z)는 [미정]이다.
- [Windows] 수정을 누르면 W04가 수정 모드로 열린다 (제목 "일정 수정").

### W03. 캘린더

**참고 이미지:** `03_calendar.png`, `04_calendar_empty.png`

```
Same design system and shell. Navigation item "캘린더" selected. Top bar title "캘린더".

LEFT (about 60%): white card. Header "2026 10월" with ‹ › buttons and a small "오늘" button. Weekday row SUN MON TUE WED THU FRI SAT in light brown. 6-week month grid with large clickable cells. Today (1st) shown as a small lime filled circle behind the number. Days with tasks show a small pink dot under the number. Some days show a tiny pink tag "중요 1" (#FFE6F6). Previous/next month dates in #C9C9C2. The selected day (17th) has a #EDFCB7 rounded highlight. One cell shows hover state.

RIGHT (about 40%): panel for the selected date "10월 17일 (토)". Lime full-width button "+ 일정 추가". Filter row: checkbox "완료 제외" left, dropdown "시간 순 ⌄" right. List of task cards: round checkbox, title "민지랑 저녁" + pink ★, subtitle "10월 17일 (토) 17시 50분", chevron ⌄ on the right to expand details. First card completed (brown filled check, strikethrough).

Second frame: same screen where the selected date has no tasks — the right panel shows a sad-face line icon and "예정된 일정이 없습니다." in light brown, with the "+ 일정 추가" button still on top.
```

**기능 명세**
- [Figma] 월 이동 ‹ ›, 오늘 날짜 표시, 일정이 있는 날 핑크 점, 중요 일정 개수 태그 `중요 N`
- [Figma] 일정 추가 버튼, "완료 제외" 체크, 정렬 `시간 순` (다른 정렬 옵션은 [미정])
- [Figma] 일정 카드: 체크, 제목, ★, 날짜·시간, 펼치기
- [Figma] 빈 상태 문구 "예정된 일정이 없습니다."
- [Figma] 검색: 모바일 캘린더 상단의 돋보기. 윈도우에서는 상단 검색창이 맡는다.
- [Windows] 날짜를 더블클릭하면 그 날짜로 W04를 연다.
- [Windows] 키보드: ←/→ 날짜 이동, PageUp/PageDown 월 이동
- [Windows] 일정 카드를 달력의 다른 날짜로 끌어 놓으면 날짜가 바뀐다. 모바일에는 없는 제안이다.

### W04. 일정 추가 · 수정 다이얼로그

**참고 이미지:** `05_add.png`, `06_add_time.png`

```
Same design system. Show the Calendar screen dimmed behind a centered modal dialog (480px wide, radius 16px, white, soft shadow). Dialog title "일정 추가" with close X (Esc).

Content:
- Text input with underline only, placeholder "일정 입력", autofocused (second frame filled with "성과 보고서 제출 마감").
- Card row: outline star icon + "중요" label, checkbox on the right (checked state: pink #FF96DE square with white check).
- Grouped card with four rows separated by thin lines, each with a light-brown outline icon and label:
  "날짜" → value "2026. 06. 01 (월)" (clicking opens a small calendar dropdown);
  "시간" → toggle switch (on = brown #857563). When on, show inline time inputs "23 : 59" with up/down spin buttons;
  "반복" → dropdown "없음 ⌃⌄";
  "알림" → dropdown "없음 ⌃⌄".
- Footer: "취소" outline button and lime "확인  Enter" button (disabled light gray until a title is entered).

Third frame: the date dropdown open, showing a compact month calendar with today in lime.
Fourth frame: edit mode — title "일정 수정", fields filled, plus a soft-red text button "삭제" at the bottom left.
```

**기능 명세**
- [Figma] 입력 필드: 제목, 중요 체크, 날짜, 시간(토글을 켜면 시·분 선택), 반복, 알림. 확인 버튼.
- [Figma] 기본 날짜는 선택한 날짜, 없으면 오늘. 날짜 형식은 `2026. 06. 01 (월)`.
- [미정] 반복 옵션 목록 (Figma는 "없음"만 보임)
- [미정] 알림 옵션 목록 (Figma는 "없음"만 보임)
- [Windows] 모달 다이얼로그. Enter로 확인, Esc로 닫기. 제목이 비어 있으면 확인 비활성.
- [Windows] 시간은 15분 단위 스핀 입력과 직접 타이핑을 둘 다 지원한다.

### W05. 정령 도감

**참고 이미지:** `07_dex.png`, `08_dex_hint.png`

```
Same design system and shell. Navigation item "정령 도감" selected. Top bar title "정령 도감" with a pencil icon button "편집" on the right.

Toolbar: segmented control "전체" (selected, brown fill, white text) / "컬렉션 별". Below: checkbox "미보유만" on the left, dropdown "호감도 순 ⌄" on the right.

Grid of spirit cards, 5 columns, each card white with 1px border and radius 12px, square illustration area:
1) pink cloud spirit with a lime ★ badge in the top-left corner (representative spirit),
2) green sprout with pink petals, 3) orange flame spirit, 4) light-blue water drop spirit, 5) yellow star child, 6) undiscovered: gray silhouette with "?" and name "???".
Under each card: name "텍스트" and a row of 5 small hearts (pink filled = affinity, brown = empty), e.g. 5/5, 4/5, 3/5, 2/5, 1/5.

RIGHT detail panel (320px) for the selected spirit: large illustration, name, 5 hearts, short description placeholder, lime button "대표 정령으로 설정".

Second frame: the undiscovered card selected — the detail panel shows title "힌트", a gray sketch of the silhouette with "?", and text "작심삼일을 무사히 넘긴 날에 나타나요."
```

**기능 명세**
- [Figma] 탭: 전체 / 컬렉션 별 (두 번째 시안 화면에는 "카테고리별"로 표기됨, [미정])
- [Figma] 필터 `미보유만`, 정렬 `호감도 순` (다른 정렬 옵션은 [미정])
- [Figma] 카드: 일러스트, 이름, 호감도 하트 5개, 대표 정령 ★ 배지
- [Figma] 미보유 정령은 실루엣 + `???`. 선택하면 획득 조건 힌트를 보여 준다.
- [Figma] 편집(연필) 버튼: 동작은 [미정]
- [Windows] 모바일 힌트 팝업을 오른쪽 상세 패널로 바꿨다.
- [Windows] 카드를 더블클릭하면 대표 정령으로 설정한다 (제안).
- [미정] 정령 이름과 설명 (Figma는 "텍스트" 자리표시자)
- [미정] 옷장 기능 (Figma 섹션 제목이 "도감 & 옷장 (만드는중)")

### W06. 정령 도감 — 카테고리별

**참고 이미지:** `09_dex_category.png`

```
Same screen as Spirit Collection, but the second segment is selected and styled pink: "전체" outline with brown text / "카테고리별" filled pink #FF96DE with white text.

The grid is grouped into collapsible sections, each a rounded card with a colored header bar:
- "자연" (light green header): 구름이 (selected, brown outline + small lavender bookmark ribbon), 꽃잎이, 불꽃이, 물순이, 별아기 (empty placeholder box), ??? (gray silhouette). Each with name and 5 small hearts.
- "동물" (light yellow header): empty card area.
- "우주" (light blue header): empty card area.
Keep the right detail panel from the previous screen.
```

**기능 명세**
- [Figma] 카테고리: 자연, 동물, 우주. 섹션별로 색 헤더가 있다.
- [Figma] 대표 정령은 리본으로 표시한다 (전체 탭의 ★와 표기가 달라 [미정]).
- [미정] 동물·우주 카테고리의 정령 목록

### W07. 알림 플라이아웃

**참고 이미지:** `10_alarm.png`

```
Same design system and shell, showing the Home screen with a right flyout panel (380px, full content height, white, left border #ECEAE4, soft shadow) opened from the bell icon. The bell icon shows pressed state.

Panel header: "알림" on the left, gear icon (opens notification settings) and close X on the right.

- Banner: brown #857563 rounded bar with info icon, white text "알림을 설정하고 중요한 플랜을 놓치지 마세요!" and chevron ›.
- Section "새로운 알림" with cards (white, 1px border, radius 10px). Each card: category label colored by type, time on the right, message below:
  "시스템" (gray) · 1시간 전 · "오늘 마감인 할 일이 {N}개 남아있어요. 차근차근 끝내볼까요? 💪"
  "이벤트" (pink) · 1시간 전 · "🎄✨ Merry Christmas ✨🎄 7일 연속 클리어하고 크리스마스 한정 정령을 만나봐요!"
  "정령의숲" (lime) · 1시간 전 · "새로운 정령을 발견했어요!"
- Section "지난 알림" with quieter cards:
  "시스템" · 1일 전 · "중요한 일정이 아직 완료되지 않았어요...🥹"
  "정령의숲" · 2일 전 · "루미가 모험을 마치고 돌아왔어요! 어떤 보상을 가져왔는지 확인하러 가보세요."
  "이벤트" · 2주 전 · "[EVENT] 루틴 5연속 성공 시 특성 경험치가 2배 !!"
  "정령의숲" · 1달 전 · "새로운 정령을 발견했어요!"
One card shows hover state.
```

**기능 명세**
- [Figma] 상단 안내 배너: 누르면 알림 설정으로 이동
- [Figma] 구역: 새로운 알림 / 지난 알림
- [Figma] 카테고리: 시스템, 이벤트, 정령의숲. 카테고리마다 색이 다르다.
- [Figma] 상대 시간 표시: 1시간 전, 1일 전, 2주 전, 1달 전
- [Windows] 벨 아이콘을 누르면 오른쪽 플라이아웃이 열린다. 바깥을 클릭하거나 Esc를 누르면 닫힌다.
- [Windows] 같은 알림을 Windows 토스트로도 보낸다 (W14).
- [미정] 알림을 클릭했을 때 이동할 화면, 읽음 처리 방식

### W08. 설정

**참고 이미지:** `11_settings.png`

```
Same design system and shell. Navigation item "설정" selected. Two-pane settings layout inside the content area.

Inner left list (260px), grouped with small section headers in light brown:
- "프로필": "닉네임", "연동 계정"
- "시스템 설정": "알림", "디스플레이", "데이터"
- "고객 지원": "공지사항", "이용 안내", "QnA", "약관 및 정책"
- "계정": "비밀번호 변경", "회원탈퇴" (both in gray #C9C9C2 text)
Selected item uses #EDFCB7 background with brown text.

Right pane showing "프로필" selected: a grouped card with rows "닉네임" → "댕트리버 ›" and "연동 계정" → "spirit@example.com" (gray, not clickable).

Second frame: "알림" selected — right pane shows a grouped card with toggle rows (placeholders): "일정 알림", "정령의숲 소식", "이벤트 소식", and "Windows 시작 시 자동 실행" toggle.
```

**기능 명세**
- [Figma] 프로필: 닉네임 (수정 가능, `>`), 연동 계정 (표시만)
- [Figma] 시스템 설정: 알림, 디스플레이, 데이터
- [Figma] 고객 지원: 공지사항, 이용 안내, QnA, 약관 및 정책
- [Figma] 계정: 비밀번호 변경(W09), 회원탈퇴
- [미정] 알림·디스플레이·데이터의 세부 항목 (Mobile_GUI에 하위 화면 없음. 예전 Mobile_Wire에 시안만 있음)
- [미정] 회원탈퇴 화면 (Figma에 "탈퇴 서운 정령 이미지 만드는중")
- [Windows] 2단 설정 레이아웃. "Windows 시작 시 자동 실행" 토글을 추가한다.

### W09. 비밀번호 변경

**참고 이미지:** `15_password_change.png`

```
Same design system. Settings two-pane layout with "비밀번호 변경" selected. Right pane, max width 480px:
- Label "기존 비밀번호" (light brown) and a password field showing "**********".
- Label "새로운 비밀번호" and a password field, helper text below "영문/숫자/특수문자 중 2가지 이상 포함 20자 내" in light gray.
- A third field in focused state (2px brown border) with an eye-off icon and a clear X icon inside on the right.
- Lime button "다음" (full width of the form).
```

**기능 명세**
- [Figma] 기존 비밀번호 → 새 비밀번호 순서로 입력하고 "다음"
- [Figma] 규칙: 영문/숫자/특수문자 중 2가지 이상, 20자 이내
- [Figma] 입력 중에는 비밀번호 보기 토글과 지우기 버튼
- [미정] "다음" 이후 단계 (새 비밀번호 확인 화면)

### W10. 로그인 시작

**참고 이미지:** `12_login.png`

```
Same brand colors. A separate login window 960x640 (title bar only, no navigation pane), background #FDFBFA. Centered white card (800x480, radius 16px) split in two halves:

LEFT: soft pastel illustration area with the pink cloud spirit and sparkles, and below it the app name placeholder "{APP_NAME}" in brown.

RIGHT, vertically centered, buttons 48px high, radius 8px, full width 320px:
- Yellow #FEE500 "카카오 로그인" with black speech-bubble icon, black text.
- White with 1px border "Google로 로그인" with Google G icon.
- Pink #FF96DE "이메일로 로그인" with white mail icon, white text.
Then small light text "아직 계정이 없으신가요?" and a beige #F6F3F1 button "이메일로 회원가입".
```

**기능 명세**
- [Figma] 카카오 로그인, Google 로그인, 이메일 로그인, 이메일 회원가입
- [미정] 앱 이름과 로고 (Figma 표기 "TodoWithSprints", 로고는 회색 자리표시자)
- [Windows] 소셜 로그인은 시스템 기본 브라우저에서 진행하고 앱으로 돌아온다.

### W11. 이메일 로그인

**참고 이미지:** `13_login_email.png`

```
Same login window. Card right half replaced by a form (left illustration stays):
- Back arrow and title "로그인".
- Label "이메일" (light brown) and a focused input (brown border) with value "spirit@example.com" and a clear X.
- Label "비밀번호" and an input with placeholder "비밀번호를 입력해주세요".
- Disabled beige button "로그인" (enabled = lime) and a text link "비밀번호 찾기" below.

Second frame: error state — under the password field red text "비밀번호가 일치하지 않습니다." and the field border in red.
```

**기능 명세**
- [Figma] 이메일, 비밀번호 입력. 둘 다 입력해야 로그인 버튼이 켜진다.
- [Figma] 비밀번호 찾기 링크
- [Figma] 오류 문구 "비밀번호가 일치하지 않습니다." (Mobile_Wire 시안에 있음)
- [Windows] Enter로 로그인, Tab으로 필드 이동

### W12. 회원가입 — 이메일 인증

**참고 이미지:** `14_signup_verify.png`

```
Same login window. Right half form:
- Back arrow and title "회원가입".
- Heading "인증번호를 입력해주세요."
- Text: pink "spirit@example.com" followed by gray "으로 발송 된 인증번호 6자리를 입력해주세요".
- Input with placeholder "인증번호" and a lime countdown "04:59" inside on the right.
- Near the bottom, centered small gray text "이메일을 받지 못하셨나요?" and on the next line "스팸 편지함 확인 또는 " followed by bold brown link "인증 메일 다시 보내기".
- Disabled beige button "확인".
```

**기능 명세**
- [Figma] 인증번호 6자리, 5분 카운트다운, 인증 메일 다시 보내기
- [미정] 회원가입의 나머지 단계 (이메일 입력, 비밀번호 설정, 닉네임, 약관)
- [미정] 온보딩 화면 (Figma에 섹션이 있으나 확인하지 못함)

### W13. 정령의 숲 (잠금 상태)

**참고 이미지:** `02_home_expanded.png` (하단 잠금 아이콘 부분)

```
Same design system and shell. Navigation item "정령의 숲" selected (lock icon in the nav item).

Content: centered empty state on #FDFBFA: the white leafy spirit icon grayed out with a lime padlock in front, title "정령의 숲은 아직 잠겨 있어요" in brown, light text "곧 정령들이 사는 숲이 열려요." Below, a small outline button "홈으로 돌아가기".
```

**기능 명세**
- [Figma] 홈 바로가기에서 정령의 숲은 잠금 아이콘과 흐린 상태로 표시된다.
- [미정] 잠금 해제 조건, 숲 화면 전체 (부록 A 참고)

### W14. 트레이 · 토스트 알림 [Windows 전용]

```
Same brand style, shown over a Windows 11 desktop wallpaper.

1) Windows system tray icon (small pink spirit) with its context menu (radius 8px): "{APP_NAME} 열기", "일정 추가  Ctrl+N", divider, "알림 끄기" (toggle), divider, "종료".

2) A Windows toast notification in the bottom-right: small pink spirit icon, app name, title "곧 시작해요", body "민지랑 저녁 · 17시 50분", buttons "확인" and "열기".

3) A second toast for the "정령의숲" category: title "새로운 정령을 발견했어요!", a small preview illustration of a pastel spirit, button "도감 보기".
```

**기능 명세**
- [Windows] 창을 닫아도 트레이에 남는다. 트레이 메뉴의 "종료"로 끝낸다.
- [Windows] 일정 알림과 W07 알림 카테고리를 토스트로 보낸다.
- [Windows] 토스트를 클릭하면 해당 화면이 열린다.
- [미정] 바탕화면 위에 정령을 띄우는 기능 (모바일 오버레이 펫 실험과 같은 개념, 기획 확정 전)

### W15. 좁은 창 (반응형)

```
Take the Calendar screen and show it at 1024x700: the navigation pane collapses to a 64px icon rail (hamburger, 홈, 캘린더, 정령 도감, 정령의 숲, and 설정 at the bottom, each with a tooltip on hover). Month grid takes the full width; the selected-date panel becomes a right overlay drawer (360px) with a close X over a dimmed grid.

Then show the Home screen at 800x600: icon rail, the today list on top and the spirit stage shrunk below it.
```

### W16. 다크 모드 [미정 — 색은 제안값]

```
Create a dark mode version of this screen with identical layout: background #1E1B18, navigation pane #25211D, cards #2B2622 with 1px #3A342E borders, text #F2EBE4, secondary #BFA993, disabled #6E665E. Keep lime #C1DD00 and pink #FF96DE accents; lime tint #3A4210, pink tint #FFE6F6 → #4A2A3E. Spirit illustrations stay pastel and bright.
```

---

## 4. 미정 항목 (그대로 둠)

| 항목 | 현재 상태 |
|---|---|
| 앱 이름·로고 | Figma 로그인에 "TodoWithSprints"(오타로 보임)와 회색 자리표시자. 초안에서는 `{APP_NAME}`으로 표기 |
| 도감 & 옷장 | "만드는중". 옷장 화면 없음. 탭 이름이 "컬렉션 별"과 "카테고리별"로 섞여 있음 |
| 마이페이지 | Figma에서 "1안 vs 2안" 비교 중 |
| 정령의 숲 | Forest_Wire에 화면 없음. 잠금 상태만 확정 |
| 알림·디스플레이·데이터 설정 | Mobile_GUI에 하위 화면 없음 |
| 회원탈퇴, 온보딩, 회원가입 앞 단계 | 초안에서 확인하지 못함 |
| 반복·알림 옵션, 정렬 옵션 | Figma는 기본값("없음", "시간 순", "호감도 순")만 보임 |
| 폰트 | 나눔스퀘어라운드로 결정 (2026-11-25, OFL 무료). woff2 3종(400·700·800)과 라이선스를 design/assets/fonts/에 동봉. Figma 원본 폰트는 확인하지 못함 |
| 다크 모드 | Figma에 없음. W16은 제안값 |
| 바탕화면 정령 | 기획 확정 전 |

## 부록 A. 정령의 숲 (참고용, [미정])

정령의 숲 초안은 Unity 프로토타입 캡처를 기준으로 한다. 숲을 공개할 때 W13을 대체하는 화면이다. 색상은 캡처 기준 근사값이다.

**참고 이미지:** `16_forest_main.png`, `17_forest_today_popup.png`, `18_forest_gifts.png`, `19_forest_trip.png`

```
Same app shell. Navigation item "정령의 숲" selected (no lock).

The whole content area is one cozy storybook forest scene (placeholder art): round wooden-door house under a big tree on the left, grassy meadow, winding path and pine trees behind, small flowers in front, warm watercolor look.

Center: the pink cloud spirit behind a small wooden desk with green books, label "책상 · 함께 한 일"; a small open notebook prop labeled "관찰 수첩".

Overlays (cream cards #FBF3E2, thin wood border #C9A27A, radius 14px, dark green text #2F5A48):
- Top-left: "작은 정령" bold, caption "함께한 1일 · 관찰 수첩".
- Top-right: leaf icon pill "25" (essence) and round icon button "앨범".
- Under the name: slim card "9월 18일 · 오늘의 우리" / "4/4 약속 · 25 정수" with chevron.
- Above the spirit: white speech bubble "우리에게 새 기억이 생겼어. 오늘 기록에서 함께 살펴보자!"
- Bottom: wide dark green card (#2F5A48, white text) "잠깐, 같이 있을까?" / "인사하고 오늘의 마음 나누기", then three cream buttons "숲 꾸미기", "나들이", "선물".
No bottom tab bar, no EXP bar, no gems.

Second frame: centered modal "9월 24일 · 오늘의 우리" (cream, 560px) with sections "내 오늘" (completed rows on #DFE9DA), "정령의 하루" (small timeline), "오늘 획득" ("오늘 누적 25 정수"), fixed dark green button "숲에서 정령 만나기".

Third frame: right sheet (420px, cream) "작은 선물 가게": pill "175 정수", big preview "작은 책", 2-column gift grid (작은 책 selected, 따뜻한 담요, 그림 도구, 작은 화분, 찻잔, 매끈한 돌), fixed button "이 선물 전하기 · 15 정수".
```

**기능 요약 (프로토타입 문서 기준)**
- 그날 처음 들어오면 "오늘의 우리" 팝업이 자동으로 뜬다 (내 오늘, 정령의 하루, 오늘 획득). 닫으면 아래로 내려가며 숲이 보인다.
- 정령을 누르면 오늘 기록과 정령 기질에 맞는 대사를 한다.
- 보조 행동: 숲 꾸미기(미리 보기 → 구매 → 배치), 나들이(40 정수, 4시간), 선물(15 정수, 하루 1회)
- 앨범: 월별 기록과 그때의 정령 모습
- 보상은 정수뿐이다. EXP 바·보석·가챠·출석 페널티는 없다.
- [미정] 메인 하단 세 번째 버튼: 캡처는 "체험 안내", 문서는 선물
