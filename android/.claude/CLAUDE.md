# todo-with-spirits (Android)

모듈 구성과 커밋 컨벤션은 [README.md](../README.md) 참고. 커밋 태그 뒤에는 `(android)` 스코프를 붙인다. (예: `refactor(android): ...`)

## 화면 구조

MVVM에 Event / Effect를 더한 MVI 단방향 흐름을 쓴다. 세부 규칙은 [rules/architecture-mvi.md](rules/architecture-mvi.md) (feature 코드 작업 시 자동 로드).

## 현재 작업: 디자인/기획 변경에 따른 UI 리팩토링

피그마 디자인을 코드에 반영할 때는 `figma-ui-refactor` 커맨드(`.claude/commands/figma-ui-refactor.md`)의 절차를 따른다.

화면별 작업 명세는 `.claude/tasks/`에 둔다. 해당 화면을 작업할 때는 먼저 명세를 읽고, "결정 필요" 항목이 남아 있으면 사용자에게 확인한 뒤 진행한다. 화면 구현이 끝나면 명세는 지운다.

| 작업 | 명세 |
| --- | --- |
| Calendar1~3 — Plan → Calendar 전환, 캘린더 화면 리뉴얼 | [tasks/CALENDAR-calendar.md](tasks/CALENDAR-calendar.md) |

### 1. 수정 범위 — UI만

- **`:data`, `:domain` 모듈은 수정하지 않는다.** (Repository, UseCase, Entity, DTO, DI 포함)
- 수정 대상은 `:app` 모듈의 UI 코드(`feature/**`, `component/`, `theme/`, `navigation/`)로 한정한다.
- ViewModel / UiState는 기존 데이터를 화면에 맞게 가공·표시하는 수준의 변경만 허용한다. 새 API나 새 도메인 데이터가 필요해 보이면 구현하지 말고 사용자에게 먼저 알린다.
- 기존 동작(클릭 콜백, 네비게이션, 상태 처리)은 디자인 변경이 요구하지 않는 한 그대로 유지한다.
- 구현 과정에서 쓰이지 않게 된 메서드·Composable·상태 필드·파일·리소스(drawable, `strings.xml` 문자열 등)는 남겨두지 말고 같은 작업에서 제거한다. (나중에 필요하면 git 히스토리에서 가져온다)
  - 리소스는 `R.drawable.x` / `@drawable/x`, `R.string.x` / `@string/x` 참조를 `app`, `core`, `data` 전체에서 검색해 확인하고, 지운 뒤 다시 검색해 연쇄적으로 안 쓰이게 된 것까지 정리한다.

### 2. 기존 Composable 우선 사용

새 Composable을 만들기 전에 아래 순서로 재사용 가능한 것이 있는지 먼저 찾는다.

1. `app/src/main/java/com/example/todowithspirits/component/` — 앱 공통 컴포넌트
   (예: `SpiritsTodoPrimaryButton`, `SpiritsTodoSwitch`, `SpiritsTodoCheckbox`, `TitleHeader`, `SelectionTabs`, `PillBadge`, `SettingsRow`, `SettingActionRow`, `PlainTextField`, `PasswordField`, `SpiritsTodoDropdown`, `CalendarView`, `MonthlyCalendarView`, `TimeWheelPicker`, `SpeechBubble`, `LoadingOverlay` 등)
2. `feature/<화면>/component/` — 해당 화면 전용 컴포넌트
3. 다른 feature의 `component/` — 비슷한 UI가 있으면 공통 `component/`로 옮겨 함께 사용하는 것을 고려

재사용 규칙:
- 기존 Composable로 표현 가능하면 **그대로 사용**한다.
- 조금만 다르면 **기본값이 있는 파라미터를 추가**해 확장한다. 기존 호출부의 동작/모양이 바뀌지 않아야 한다.
- 거의 같은 Composable을 복사해서 새로 만들지 않는다.
- 새로 만들 때는 2곳 이상에서 쓰이면 `component/`, 한 화면 전용이면 `feature/<화면>/component/`에 둔다.
- 색상은 `SpiritTodoTheme.color.*` 토큰을 사용한다. 하드코딩 `Color(0x...)`를 화면 코드에 직접 쓰지 않는다.

### 3. 간격(dp) 정규화

피그마의 간격 값은 정리되지 않아 소수점이거나 좌우가 일정하지 않을 수 있다. **피그마 값을 그대로 옮기지 말고 근사값으로 정규화**한다.

- 소수점 값은 정수로 반올림한다. (예: `15.67` → `16`, `7.5` → `8`)
- 정수 값은 짝수(2dp 단위)로 맞추고, ±1 이내면 4의 배수를 우선한다. (예: `13` → `12`, `17` → `16`, `23` → `24`, `10` → `10`)
- 좌우(또는 상하) 간격 차이가 2dp 이하이면 같은 값으로 통일하고 `padding(horizontal = ...)` / `padding(vertical = ...)`를 사용한다. (예: 좌 `15`, 우 `17` → `horizontal = 16.dp`)
- 같은 화면에서 반복되는 요소(리스트 아이템, 카드 사이 간격 등)는 하나의 값으로 통일하고, 가능하면 `Arrangement.spacedBy(...)`를 사용한다.
- 같은 역할의 간격은 화면 간에도 같은 값을 쓴다. (예: 화면 좌우 기본 여백)
- 차이가 크거나(3dp 이상) 의도된 비대칭으로 보이면 임의로 맞추지 말고 사용자에게 확인한다.
- 아이콘/이미지 크기, 1dp 보더처럼 시각적으로 민감한 값은 정규화 대상에서 제외하고 피그마 값(반올림)을 따른다.

### 참고: dp 스케일링

`SpiritTodoTheme`이 `LocalDensity`를 화면 폭 / 360dp 비율로 스케일한다(`theme/SpiritsTodoTheme.kt`). 따라서 **피그마 프레임 폭이 360일 때 1 피그마 px = 1dp**로 그대로 옮긴다. 프레임 폭이 360이 아니면(예: 375, 390) `값 × 360 / 프레임 폭`으로 환산한 뒤 정규화한다.
