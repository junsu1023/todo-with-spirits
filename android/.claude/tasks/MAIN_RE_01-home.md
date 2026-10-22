# MAIN_RE_01 / home1 — Today → Home 전환, 홈 레이아웃, 정령 모션

- 피그마: https://www.figma.com/design/jx4S6NqQs4g5s15fG9zSuR/OOW?node-id=2616-20545
  - 노드 `2616:20545`, 이름 **`home1`** (MAIN_RE_01 묶음 중 하나), 프레임 360 × 791 → 1px = 1dp
  - 프레임 밖에 걸친 요소(`Group 90` 캘린더, `Rectangle 33`, `Rectangle 4337`)는 무시한다.
- 관련 규칙: [CLAUDE.md](../CLAUDE.md), [rules/architecture-mvi.md](../rules/architecture-mvi.md), 커맨드 `.claude/commands/figma-ui-refactor.md`, `.claude/commands/feature-rename.md`

## 작업 범위

| # | 내용 | 상태 |
| --- | --- | --- |
| 1 | `today` → `home` 이름 변경 (패키지, 파일, 클래스, 라우트) | 완료 (기기 확인 전) |
| 2 | home1 레이아웃 반영 (헤더, 오늘 일정 카드, 상태 칩, 말풍선, 정령, 언덕 배경) | 완료 (기기 확인 전) |
| 3 | 정령 좌우 이동 + 끝에서 좌우 반전 | 완료 (기기 확인 전) |
| 4 | 정령 클릭 시 바운스 | 완료 (기기 확인 전) |
| 5 | 오늘 일정 카드 펼침 (화살표) + 임시 데이터 | 완료 (기기 확인 전) — 펼친 상태 프레임 받으면 디자인 교체 |
| 6 | 하단 탭바 제거 → 홈 화면 안의 임시 버튼 3개 (홈 / 정령의숲 / 캘린더) | 완료 (기기 확인 전) |
| 8 | 빠른 추가 FAB 제거 (피그마에서 빠짐) | 완료 (기기 확인 전) |
| 7 | Home 화면 MVI 전환 (Event / Effect) | 완료 (기기 확인 전) |

`:data`, `:domain` 수정 금지.

## 1. 이름 변경 매핑

`git mv`로 옮겨 히스토리를 유지한다.

| 기존 | 변경 |
| --- | --- |
| 패키지 `feature.today` (main, test) | `feature.home` |
| `TodayScreen.kt` / `TodayScreen` | `HomeScreen.kt` / `HomeScreen` |
| `TodayViewModel` | `HomeViewModel` |
| `TodayUiState` | `HomeUiState` |
| `TodayViewModelPostponeTest` | `HomeViewModelPostponeTest` |
| `Screen.Today` (`"today"`) | `Screen.Home` (`"home"`) |
| `navigateToToday` | `navigateToHome` |
| `ForestScreen(onReturnToToday)` | `onReturnToHome` |
| `movedToToday` (`SpiritsTodoApp.kt`) | `movedToHome` |
| `R.string.today` = "Today" | `R.string.home` = **"홈"** (home1 하단 라벨) |
| 주석 속 "Today 화면" | "Home 화면" |

**유지 (화면이 아니라 '오늘 날짜'를 뜻함)**: `TodayPlanSection`, `TodayListItem`, `todayAchievementRate`, `loadTodayAchievement`, `isToday`, `TodayRewardCard`, `today_*` 문자열, 지역 변수 `today`.

## 2. 레이아웃 (피그마 → 적용, 단위 dp)

화면 배경 `surfaceColor1`(흰색). 스크롤 없음.

### 헤더 — `TitleHeader` 재사용 (높이 60, 좌우 패딩 16)
| 요소 | 피그마 | 적용 |
| --- | --- | --- |
| 날짜 텍스트 `M월 d일 (E)` | x 18, 20sp Medium `#EFA4C6` | 좌측 16 (TitleHeader 기본), 신규 토큰 `pointPink` |
| 설정 아이콘 | 26, `24_TODO_Setting` | `R.drawable.todo_setting`, 26 |
| 알림 아이콘 + 99+ 배지 | 26, 배지 `#EFA4C6` | `AlarmIconSection` + `R.drawable.todo_alarm`, 26, 배지 `pointPink` |
| 아이콘 간격 | (40-26)/2 × 2 + gap 2 = 16 | `spacedBy(16)` |
| 우측 여백 | 10 + 7 = 17 | 16 (TitleHeader 기본) |

- `TitleHeader`에 `leftComposable` 파라미터, `AlarmIconSection`에 `badgeColor`·`iconSize` 파라미터 추가 (기본값으로 기존 화면 유지).
- 설정 아이콘 클릭 → 마이페이지(`Screen.MyPage`)로 이동. 탭바 개편 후 마이페이지 진입 경로가 여기만 남을 수 있다.

### 오늘 일정 카드 — 신규 `HomeTodaySummaryCard`
| 요소 | 피그마 | 적용 |
| --- | --- | --- |
| 카드 | x 18, w 324, h 94, radius 6, `#FAFAFA` | 좌우 18, radius 6, `systemBackground` |
| 헤더 → 카드 | 106 → 116 | 10 |
| 안쪽 패딩 | 좌 14 / 우 14 / 상 12 / 하 12 | horizontal 14, vertical 12 |
| "오늘 일정" | 16sp Regular `todoTextMain` | 그대로 |
| 제목 → 숫자 | 12 | 12 |
| 완료 수 | 53.2sp SemiBold `#857563`, 박스 높이 39 | 53sp, `todoTextMain`. 폰트 위아래 여백 때문에 박스가 커지거나(카드 늘어남) lineHeight로 줄이면 글자가 잘리므로, 텍스트는 원래 크기로 그리고 레이아웃에는 기준선을 하단에 맞춘 39만 차지 (`baselineAtBottom`) |
| `/ 전체` | 20.267sp Regular `systemGrey`, 박스 24, 하단 정렬 | 20sp, `baselineAtBottom(24)` → 두 숫자 기준선이 맞음 |
| 숫자 사이 | 3 | 4 |
| 화살표 | 28, `fi-rr-angle-small-down` | `R.drawable.fi_rr_angle_small_down`, 28, 숫자 줄 세로 가운데 |

- 값: 오늘(`selectedDate`) 일정 + 루틴의 완료 수 / 전체 수.
- 화살표 클릭 시 아래로 펼쳐진다. → "7. 오늘 일정 펼침"

### 상태 칩 — 신규 `HomeStatusChip`
| 요소 | 피그마 | 적용 |
| --- | --- | --- |
| 카드 → 칩 | 210 → 220 | 10 |
| 칩 | h 26, radius 4, px 8, gap 4, 아이콘 14, 12sp Regular | 그대로 (폭은 내용에 맞춤) |
| 칩 사이 | 166 → 172 | 6 |
| 중요 칩 | bg `mainBackground`, 별 `#D7BFFF` | `R.drawable.fi_rr_color_star` |
| 완료 칩 | bg `#F0FFD4`, 체크 `#B2F042` | 신규 토큰 `routineBackground`, 신규 `R.drawable.ic_check_routine` |

- 중요 칩: 오늘 할 일 중 `isImportant && !isDone` 개수, 0이면 숨김.
- 완료 칩: 오늘 일정 + 루틴 완료 수.

### 정령 영역 (남은 높이 전체)
| 요소 | 피그마 | 적용 |
| --- | --- | --- |
| 언덕 배경 | 360 × 296, y 495 ~ 프레임 하단, `#F2FFCF` | 신규 `R.drawable.home_hill`, **화면 맨 아래**(하단 버튼·시스템 내비게이션 바 뒤까지)에 맞춘다. 기존의 "탭바 높이만큼 내려 그리기"는 제거 |
| 정령 | 150 박스, 이미지 126 × 142 (inset 좌우 12, 상 4.5, 하 3.5) | `R.drawable.wind_spirit` (피그마 에셋과 동일), 박스 150 / 패딩 좌우 12, 상하 4 |
| 정령 위치 | 정령 하단이 언덕 위 50 | 언덕 상단 + 50 |
| 말풍선 | `Union` 173 × 72, `#ECECEC`, 꼬리 높이 30.5, 꼬리 끝이 밑변보다 오른쪽 | `SpeechBubble` 재사용 (`systemArea`), 꼬리 끝 오프셋 파라미터 추가 |
| 말풍선 텍스트 | "오늘 하루도 힘차게!" 14sp Regular `#857563` | `todoTextMain` |
| 말풍선 ↔ 정령 | 말풍선 x = 정령 x − 50, 꼬리 끝이 정령 상단과 6 겹침 | 정령을 따라 이동, 화면 밖으로 나가면 좌우 여백 18 안으로 고정 |

## 3. 정령 이동

- 이동 범위: 좌우 여백 18 안쪽 (x 18 ~ 360 − 18 − 150 = 192).
- 시작: 가로 가운데, 오른쪽으로 먼저 이동.
- 끝에 닿으면 `scaleX` 반전 후 반대 방향. 무한 반복.
- 속도 **30dp/s** 일정 (`LinearEasing`). 상수로 둔다.
- `wind_spirit`은 꼬리가 오른쪽이라 기본 방향은 **왼쪽**을 본다 → 오른쪽으로 갈 때 `scaleX = -1`.
- 가로 이동은 람다 `Modifier.offset { }`(터치 영역이 따라오도록), 반전·바운스는 `graphicsLayer { }`.
- 위치는 `Animatable` + `LaunchedEffect` 루프. 화면을 떠나면 자동으로 멈춘다.

## 4. 정령 바운스

- 클릭 시 `translationY` 0 → **−20dp** `tween(150ms, FastOutSlowInEasing)` → 0 `spring(MediumBouncy, StiffnessMedium)`.
- 바운스 중에도 좌우 이동은 계속된다. 다시 클릭하면 현재 위치에서 다시 시작한다.
- 클릭은 `noRippleClickable`.

## 5. 하단 임시 버튼

기존 Material `NavigationBar` 형식(아이콘, 선택 상태, 5개 탭)을 없애고 home1의 회색 임시 버튼 3개로 바꾼다. **차후 아이콘으로 교체**할 예정이라 구조는 단순하게 둔다.

- 버튼은 **홈 화면에서만** 보이므로 앱 공통 `Scaffold.bottomBar`가 아니라 `HomeScreen` 안에 둔다. (`feature/home/component/HomeBottomButtons.kt`, 화면 맨 아래에 언덕 위로 겹쳐 띄움)
- 기존 `component/SpiritsTodoBottomBar.kt`(`bottomNavItems`, `BottomBarHeight`)는 삭제.

| 버튼 | 동작 (MVI) |
| --- | --- |
| 홈 | 이미 홈이라 동작 없음 |
| 정령의숲 | `ForestButtonClicked` → `NavigateToForest` → `Screen.Forest` |
| 캘린더 | `CalendarButtonClicked` → `NavigateToCalendar` → `Screen.Plan` |

| 요소 | 피그마 | 적용 |
| --- | --- | --- |
| 버튼 | 71 × 71, `#D9D9D9`, 모서리 없음 | 71 × 71, `surfaceColor15` |
| 라벨 | 16sp Regular, 검정, 가운데 | 16sp, `surfaceColor5` (임시 버튼이라 새 토큰을 만들지 않음) |
| 좌우 여백 | 좌 29 / 우 30 | horizontal 30 |
| 버튼 사이 | 45 / 43 | `SpaceBetween` (≈ 44) |
| 아래 여백 | 버튼 하단 → 프레임 하단 51 (iOS 홈 인디케이터 34 포함) | 16 + `navigationBarsPadding()` |
| 배경 | 없음 (언덕이 비침) | 투명 |

- 홈에서 이동할 때는 `navigateFromHome(route)` (홈을 백스택에 남기고 상태 저장/복원). 정령의숲·캘린더에서 뒤로가기 시 홈으로 돌아온다 (`SpiritsTodoApp`의 `homeButtonRoutes`).
- 홈은 콘텐츠를 시스템 내비게이션 바 뒤까지 그린다 (Scaffold `innerPadding`의 bottom을 적용하지 않음).
- 문자열: `home`(홈), `forest`(정령의숲), `calendar`(캘린더).

## 빠른 추가 버튼(FAB) 제거

피그마 디자인 가이드에서 빠져 제거했다. FAB가 유일한 진입점이던 빠른 추가 팝업도 함께 제거.

- 삭제: `component/FloatingButton.kt`, `component/QuickAddSharedKeys.kt`, `feature/home/component/QuickAddBottomPopup.kt`, `feature/home/viewmodel/QuickAddViewModel.kt`
- `MainViewModel`: `isBottomSheetVisible`, `setSBottomSheetVisible` 삭제 (시스템 상태 확인은 유지)
- `SpiritsTodoApp`: FAB / 빠른 추가 오버레이 삭제
- 일정 추가는 Plan 화면의 추가 버튼(`navigateToAdd`)으로 계속 가능하다.

## 6. MVI 적용

[rules/architecture-mvi.md](../rules/architecture-mvi.md)를 따른다.

- `state/HomeEvent.kt`: `SettingClicked`, `AlarmClicked`, `ForestButtonClicked`, `CalendarButtonClicked`, `TodayTasksToggled`, `TodayTaskCompletionChanged(id, isCompleted)`
- `state/HomeEffect.kt`: `NavigateToMyPage`, `NavigateToAlarm`, `NavigateToForest`, `NavigateToCalendar`
- 카드의 완료 수 / 전체 수 / 중요 일정 수는 `HomeScreen`에서 계산하지 않고 `HomeUiState`에 담는다.
- `HomeRoute`(ViewModel 연결, Effect 처리) / `HomeScreen(uiState, isLoading, onEvent)`로 나눈다. 네비게이션 그래프는 `HomeRoute`를 호출한다.
- 정령 이동·바운스, 말풍선 위치는 화면 표현 상태라 `SpiritStage` 안의 로컬 상태로 둔다.
- `util/ObserveAsEffect.kt`를 이번에 만든다.
- `HomeRoute`는 쓰지 않게 된 `navigateToEditTask`, `navigateToPlan`을 받지 않는다.
- 테스트: `HomeViewModelEventTest` (펼침 토글, 완료 변경과 개수, 설정·알림 Effect).

## 7. 오늘 일정 펼침 (임시 데이터)

펼친 상태 디자인 프레임이 아직 없어 기존 컴포넌트로 단순하게 만들고, 프레임을 받으면 교체한다.

- **데이터**: 실제 API 대신 임시 데이터로 채운다. 필드는 우선 `title`, `isCompleted`, `isImportant` (+ 목록 key와 완료 토글용 `id`).
  - 모델 `HomeTodayTask` (`state/HomeUiState.kt`), 임시 목록은 `HomeViewModel`의 `TempTodayTasks` 상수 (`// TODO: 임시 데이터` 표시).
  - 카드의 완료 수 / 전체 수, 상태 칩의 중요·완료 수도 같은 임시 목록에서 계산한다. (펼친 목록과 숫자가 어긋나지 않도록)
  - 실제 데이터로 바꿀 때는 `HomeViewModel`에서 `GetTaskCalendarUseCase`로 오늘 일정/루틴을 불러와 `HomeTodayTask`로 매핑한다. (`TaskSummary`의 `title`, `isCompleted`, `isImportant`, 루틴은 `isImportant = false`)
- **동작 (MVI)**
  - 화살표 클릭 → `HomeEvent.TodayTasksToggled` → `HomeUiState.isTodayTasksExpanded` 반전.
  - 체크박스 클릭 → `HomeEvent.TodayTaskCompletionChanged(id, isCompleted)` → 임시 목록의 `isCompleted` 갱신 (서버 호출 없음).
- **모양**
  - 카드가 아래로 늘어나며 칩과 정령 영역 **위에 겹쳐서** 펼쳐진다. (밀어내면 정령 영역이 줄어 말풍선이 잘리므로) 접힌 부분 높이는 측정해서 그만큼 아래 콘텐츠 자리를 비운다. (시스템 글자 크기가 커져도 겹치지 않게)
  - 펼침/접힘은 `animateContentSize`, 화살표는 180° 회전.
  - 목록: 숫자 줄 아래 12, 항목 높이 36, 최대 높이 288(8개)을 넘으면 스크롤.
  - 항목: `SpiritsTodoCheckbox`(`todo_check_pp` / `todo_check_up`, 20) + 간격 8 + 제목 14sp `todoTextMain`(완료 시 `systemGrey` + 취소선) + 중요하면 끝에 `fi_rr_color_star` 14.

## 화면에서 빠지는 것 (제거 완료)

- home1에 없는 `SpiritSection`(레벨/경험치), `BadgeSection`(`BadgeAndAchievementRow`), `TodayPlanSection`, `TodoDetailBottomSheet` 파일 삭제.
- `HomeViewModel`: 쓰지 않게 된 `loadTask`, `loadWeekEvents`, `loadTodayAchievement`, `setSelectedDate`, `completeTask`, `cancelTaskCompletion`, `deleteTask`, `postponeTodo`, `TaskRefreshBus` 구독과 UseCase 주입 삭제. `HomeViewModelPostponeTest` 삭제 (미루기는 Plan 화면과 `PlanViewModelPostponeTest`에 남아 있음).
- `HomeUiState`: `todos`, `routines`, `weekEvents`, `todayAchievementRate`, `TodoItem`, `RoutineItem`, `SpiritInfo`의 레벨/경험치 필드 삭제.
- 삭제한 코드만 쓰던 리소스는 다른 화면 디자인에서 쓸 수 있어 남겨둠.
  - 하단 탭바·빠른 추가: `home/plan/forest/record/my_page_selected·status`, `fi_rr_up_down`, `todo_clock`, `todo_important_26`, `todo_plus`, 문자열 `plan`, `record`, `my_page`
  - 이전 홈 구성: `temp_badge`, `todo_arrow`, `todo_arrow2`, `todo_icon_check_bl/gn/pp`, 문자열 `delete`, `modify`, `growth_badge`, `see_all_plan`, `today_completion_rate`

## 결정 필요

1. **펼친 상태 프레임 링크** — 임시 디자인 교체, 실제 데이터 연결 시점.
2. **Record 화면 진입 경로**: 하단 버튼에서 빠져 지금은 들어갈 방법이 없다.
3. **하단 버튼 아이콘** 디자인 (받으면 임시 버튼 교체).
4. 말풍선 문구가 고정("오늘 하루도 힘차게!")인지, 상황에 따라 바뀌는지.

## 검증 체크리스트

- [x] `grep -rn "feature.today\|TodayScreen\|TodayViewModel\|TodayUiState\|Screen.Today" app/src` 결과 없음
- [ ] 로그인/스플래시 후 시작 화면이 Home
- [ ] 하단 임시 버튼 3개: 홈 / 정령의숲 / 캘린더(Plan) 이동, 홈에서만 보임, 언덕이 버튼 뒤로 비침
- [ ] 오늘 일정 숫자(완료 수 / 전체)가 잘리지 않음
- [ ] 빠른 추가 FAB가 사라짐, 정령의숲·캘린더에서 뒤로가기 시 홈
- [ ] 화살표로 오늘 일정 펼침/접힘, 체크 시 완료 표시와 카드·칩 숫자 갱신
- [ ] 설정 아이콘 → 마이페이지 → 뒤로가기 시 홈
- [ ] 뒤로가기 처리(`SpiritsTodoApp.kt`) 정상
- [ ] 정령: 오른쪽 끝 → 반전 → 왼쪽, 왼쪽 끝 → 반전 → 오른쪽, 진행 방향을 바라봄
- [ ] 정령 클릭 시 바운스, 이동 중에도 클릭 가능
- [ ] 다른 화면의 `TitleHeader` / `AlarmIconSection` / `SpeechBubble` 모양 변화 없음
- [x] `./gradlew :app:compileDebugKotlin`, `./gradlew :app:testDebugUnitTest` 통과
