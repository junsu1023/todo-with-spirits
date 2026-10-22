# 일정 추가(Add) — 루틴 제거 및 디자인 반영

- 디자인: 사용자 캡처 3장 (빈 화면 / 입력 중 + 키보드 / 입력 완료 + 중요 체크). Figma 호출 한도로 캡처 환산(프레임 폭 506px → 360dp, × 0.711).
- 관련 규칙: [CLAUDE.md](../CLAUDE.md), [rules/architecture-mvi.md](../rules/architecture-mvi.md)
- **커밋하지 않는다.** 사용자가 테스트 후 커밋하라고 알려주면 커밋한다.

## 작업 범위

| # | 내용 | 상태 |
| --- | --- | --- |
| 1 | 할 일 / 루틴 구분 제거 → 일정(Todo)만 | 완료 (테스트 전) |
| 2 | 루틴 관련 코드 제거 (app + 안 쓰이게 되는 data/domain) | 완료 (테스트 전) |
| 3 | 디자인 반영 (제목 입력, 중요, 날짜/시간/반복/알림, 확인 버튼) | 완료 (테스트 전) |
| 4 | Add 화면 MVI 전환 (`AddRoute`/`AddScreen`, `AddEvent`/`AddEffect`) | 완료 (테스트 전) |

## 결정 사항 (사용자 확인 완료)

- **반복**: Todo API(`NewTodo`)에 반복 필드가 없어 **UI에서 선택만 하고 값은 상태에 보관**, API로 보내지 않는다. 서버 지원 시 data/domain에 필드 추가 후 연결.
- **루틴 data/domain**: 앱에서 안 쓰이게 되면 함께 제거. 일정 조회 응답의 `taskType`(ROUTINE) 구분처럼 다른 곳에서 쓰는 공용 부분은 남긴다.

## 제거 대상

- app: 탭(`SelectionTabs` 사용), `RoutineForm`, 루틴 등록/수정, 루틴 전용 상태(요일·날짜 선택, 공휴일 제외), `DayOfWeekSelector`, 루틴에서만 쓰던 `MonthlyCalendarView`
- 디자인에서 빠진 항목: 카테고리, 공개 여부, 메모
- domain: `CreateRoutineUseCase`, `UpdateRoutineUseCase`, `NewRoutine`, `Routine`, `TaskRepository.createRoutine/updateRoutine`
- data: 루틴 생성/수정 API·데이터소스·요청 DTO·응답 DTO·매퍼, URL 상수
- 위 제거로 안 쓰이게 되는 문자열·drawable·컴포넌트 (지운 뒤 재검사)
  - 결과: `MonthlyCalendarView`, `SelectionTabs`(`TabItems`), domain `PublicStateOption`, drawable `todo_category`/`todo_check_pp`/`todo_check_up`/`todo_private`, 문자열 `title`/`todo`/`routine`/`category`/`public_state`/`register`/`modify_register`/`exclude_holidays`
  - 남김: 서버 에러 문구 번역(`ApiErrorMessageTranslator`)의 루틴 문구, 일정 조회의 `TaskType.ROUTINE`·루틴 개수 (기존 루틴 일정이 조회되므로)
- 이름 변경: 제목 입력 `SearchArea` → `TaskTitleField` (검색이 아니라 제목 입력)

## 레이아웃 (캡처 → 적용, dp)

| 요소 | 캡처 | 적용 |
| --- | --- | --- |
| 헤더 | 뒤로가기 + "일정 추가" 18sp | `TitleHeader`, 뒤로가기 → 이전 화면 |
| 제목 입력 | 헤더 아래 ≈ 20, placeholder "일정 입력", 글자 ≈ 18sp | 좌우 18, 18sp `todoTextMain`, placeholder `onSurfaceColor2` |
| 밑줄 | 글자 아래 ≈ 10, 회색 / 입력 중 분홍 | 1dp `onSurfaceColor2`, 포커스 시 1.5dp `pointPink` |
| 밑줄 → 중요 카드 | ≈ 22 | 22 |
| 카드 | `#FAFAFA`, radius ≈ 6, 좌우 18 | `SettingGroup` (`surfaceColor10`) |
| 행 | 높이 ≈ 50, 좌우 ≈ 12, 아이콘 20 + 사이 ≈ 12 + 라벨 16sp Regular | 세로 패딩 12, 좌우 12, 사이 12, 라벨 Regular |
| 중요 체크 | 20, 미체크 회색 테두리 / 체크 분홍 + 흰 체크 | `ic_checkbox_square_on/off` 20 |
| 중요 카드 → 설정 카드 | ≈ 18.5 | 18 |
| 날짜 | "2026. 06. 01 (월)" 16sp, 누르면 캘린더 펼침 | 기존 `SettingDateItem` |
| 시간 | 스위치 ≈ 45 × 21, 켜면 시간 휠 | `SpiritsTodoSwitch` 44 × 22 (thumb 18), 기존 `TimeWheelPicker` |
| 반복 / 알림 | "없음" ≈ 14sp + 위아래 화살표 | 14sp `todoTextMain`, `expand_icon`, domain `displayName` 그대로 (NONE = "없음") |
| 행 구분선 | 카드 안쪽 좌우 ≈ 10 | `SettingDivider` 좌우 10 |
| 확인 버튼 | h ≈ 43, 분홍, radius ≈ 6, "확인" 흰색 16sp, 화면 아래 (키보드 위로 따라 올라감) | `SpiritsTodoPrimaryButton` + 색 파라미터(`pointPink`), 아래 24 + `imePadding` |

- 반복 옵션: 없음 / 매일 / 매주 / 매월 / 매년, 알림 옵션: 없음 / 10분 전 / 30분 전 / 1시간 전.
- 수정 모드도 같은 화면, 헤더 "일정 수정", 버튼 "확인".

## 동작 (MVI)

- Event: `BackClicked`, `TitleChanged`, `ImportantChanged`, `DateChanged`, `TimeEnabledChanged`, `DueTimeChanged`, `RepeatOptionChanged`, `AlarmOptionChanged`, `ConfirmClicked`
- Effect: `NavigateBack` (뒤로가기, 등록/수정 성공)
- 수정 진입: `taskId`로 일정을 불러온다. **불러온 일정이 루틴이면** 일정 API로 수정할 수 없어 "루틴 일정은 수정할 수 없어요" 안내 후 뒤로 간다.
- 제목이 비어 있으면 확인 버튼 비활성.
- 카테고리: domain `CategoryOption` 제거, `NewTodo.category`는 문자열. 생성·수정 모두 ViewModel에서 `"NONE"`(서버의 '없음' 값)으로 하드코딩해 보낸다. 수정 시 기존 카테고리도 NONE으로 바뀐다.
- 공개 여부·메모는 화면에서 빠졌지만, **수정할 때 기존 값이 지워지지 않도록** 불러온 값을 그대로 보낸다.
- 시간 스위치 켜짐 색: 디자인 없음 → 체크박스와 같은 분홍.
- 에러는 토스트로 표시 (기존 Add 화면은 에러를 보여주지 않았음).
- 테스트: `AddViewModelEventTest` 4개

## 검증 체크리스트

- [ ] 캘린더 "일정 추가" → 빈 화면(캡처 1), 입력 시 밑줄 분홍(캡처 2), 중요 체크(캡처 3)
- [ ] 날짜 펼침·선택, 시간 켜기·휠, 반복/알림 드롭다운
- [ ] 확인 → 일정 생성 후 뒤로, 캘린더에 반영
- [ ] 캘린더 일정 수정 → 기존 값 채워짐 → 확인 시 수정
- [ ] 키보드가 올라오면 확인 버튼이 키보드 위로
- [ ] `./gradlew :domain:compileKotlin :data:compileDebugKotlin :app:assembleDebug`, 세 모듈 단위 테스트 통과
