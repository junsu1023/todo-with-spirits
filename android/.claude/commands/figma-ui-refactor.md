---
name: figma-ui-refactor
description: 변경된 피그마 디자인을 기존 Compose 화면에 반영(UI 리팩토링)할 때 사용. 피그마 URL/노드가 주어지거나 "디자인 변경 반영", "UI 수정", "피그마대로 바꿔줘" 같은 요청에 사용한다. 간격 정규화, 기존 Composable 재사용, data/domain 모듈 수정 금지 규칙을 포함한다.
---

# 피그마 → Compose UI 리팩토링

`CLAUDE.md`의 "현재 작업: UI 리팩토링" 규칙을 전제로 한다. 아래 순서대로 진행한다.

## 1. 디자인 확인

- 피그마 MCP로 대상 노드를 읽는다. (`figma:figma-design-to-code` 스킬을 먼저 로드한 뒤 `get_design_context`, 필요 시 `get_screenshot` / `get_metadata`)
- 프레임 폭을 확인한다. 360이 아니면 이후 모든 수치를 `× 360 / 프레임 폭`으로 환산한다.
- 피그마가 생성해 준 코드(React/Tailwind 등)는 구조 참고용일 뿐, 그대로 옮기지 않는다.

## 2. 기존 코드 파악

- 대상 화면의 `feature/<화면>/` 파일(Screen, component, state, viewmodel)을 읽는다.
- 디자인의 각 요소를 기존 Composable에 매핑한다. 탐색 순서:
  1. `app/.../component/` (공통)
  2. `app/.../feature/<화면>/component/`
  3. 다른 feature의 `component/`
- 매핑 결과를 짧게 정리한다: `디자인 요소 → 재사용 / 파라미터 추가 / 신규`.

## 3. 간격 정규화

피그마의 모든 padding / spacing / gap 값을 `CLAUDE.md`의 정규화 규칙으로 변환한다.

| 피그마 | 적용 | 이유 |
| --- | --- | --- |
| 15.67 | 16 | 소수점 반올림 |
| 13 | 12 | 짝수 + 4의 배수 우선 |
| 좌 15 / 우 17 | horizontal 16 | 좌우 차이 ≤ 2 → 통일 |
| 리스트 간격 11, 12, 13 | spacedBy(12) | 반복 요소 통일 |

- 좌우 차이 3dp 이상, 또는 의도된 비대칭으로 보이면 구현 전에 사용자에게 확인한다.
- 아이콘/이미지 크기, 보더 두께는 정규화하지 않는다.

## 4. 구현

- 수정 범위: `:app` 모듈의 UI 코드만. `:data`, `:domain`은 절대 수정하지 않는다.
- 새 데이터가 필요하면 구현을 멈추고 사용자에게 알린다. (임시로 하드코딩하지 않는다)
- 기존 Composable 확장 시 기본값 파라미터를 사용해 기존 호출부의 모양/동작을 보존한다. 변경한 공통 Composable의 다른 호출부를 grep으로 확인한다.
- 색상은 `SpiritTodoTheme.color.*`를 사용한다. 피그마 색상이 기존 토큰에 없으면 사용자에게 알리고 `theme/Color.kt`에 토큰을 추가한다.
- 수정한 Composable에 `@Preview`가 있으면 함께 갱신한다.

## 5. 검증

- `./gradlew :app:compileDebugKotlin`으로 컴파일을 확인한다.
- 관련 단위 테스트가 있으면 `./gradlew :app:testDebugUnitTest`를 실행한다.

## 6. 결과 보고

작업 후 아래 내용을 짧게 보고한다.
- 재사용한 Composable / 파라미터를 추가한 Composable / 새로 만든 Composable
- 정규화한 간격 목록 (`피그마 값 → 적용 값`)
- 사용자 확인이 필요한 부분 (큰 비대칭, 누락된 데이터, 없는 색상 토큰 등)
