# Calendar1~3 — Plan → Calendar 전환 및 캘린더 화면 리뉴얼

- 피그마 (프레임 360 × 814 → 1px = 1dp)
  - `2631:27785` **Calendar1**: 캘린더 + 일정 목록 (완료 제외, 정렬 드롭다운, 펼친 카드)
  - `2633:31002` **Calendar2**: 일정 없음
  - `2633:31962` **Calendar3**: 검색 (검색창, 최근 검색, 딤)
  - 프레임 밖 요소(`Group 90`, `Rectangle 33`)는 무시.
- 관련 규칙: [CLAUDE.md](../CLAUDE.md), [rules/architecture-mvi.md](../rules/architecture-mvi.md)
- 단계마다 커밋한다.

## 작업 범위

| # | 단계 | 상태 |
| --- | --- | --- |
| 1 | `plan` → `calendar` 이름 변경 (패키지, `CalendarScreen`/`CalendarViewModel`/`CalendarUiState`, `Screen.Calendar("calendar")`) | 완료 `c6eafbc` |
| 2 | 일정 카드 교체(`CalendarTaskItem`) + 디자인에서 빠진 기능 제거 | 완료 |
| 3 | Calendar 화면 MVI 전환 (Event / Effect, Route / Screen 분리) | 완료 |
| 4 | 나머지 Calendar1·2 디자인 (헤더, 캘린더, 일정 추가, 완료 제외/정렬, 일정 없음) | 진행 |
| 5 | Calendar3 검색 | 대기 |

`:data`, `:domain` 수정 금지. (`PlanSortOption`의 표시 이름은 domain 값이라 UI에서 따로 매핑)

## 결정 사항 (사용자 확인 완료)

- **검색**: domain에 검색 API가 없어 UI + **불러온 이번 달 일정에서 제목 검색**. 최근 검색어는 앱 실행 중에만 유지(ViewModel 메모리).
- **빠진 기능**: 스와이프 미루기, D-Day, 카테고리 표시, 상세 화면(`PlanDetailScreen`/`PlanDetailViewModel`, `Screen.PlanDetail`) 제거. 미루기 테스트(`CalendarViewModelPostponeTest`)도 함께 정리.
- 할 일 / 루틴 탭(`UnderlinePlanTabs`), 기존 검색창(`PlanSearchArea`) 제거.
- `PlanItemData`의 안 쓰이게 된 필드(`memo`, `category`, `repeatInfo`, `isPublic`, `notificationMinutes`)와 리소스(`fi_rr_arrow_right`, `fi_rr_pencil`, `fi_rr_trash`, `todo_important`, `todo_quote_open_2(_color)`, 문자열 `search_plan`) 제거.
- `fi_rr_search`는 4·5단계 헤더/검색창에서 다시 쓴다.

## Calendar1·2 레이아웃

### 헤더 — `TitleHeader` 재사용
| 요소 | 피그마 | 적용 |
| --- | --- | --- |
| 뒤로가기 | `c-1` 26, `#857563` | `todo_back1` 26 → `popBackStack` |
| 제목 | "캘린더" 18sp Medium `#857563` | `R.string.calendar`, TitleHeader 기본 |
| 검색 | `fi-rr-search` 24, `#857563` | `fi_rr_search` 24, `todoTextMain` 틴트 → 검색 열기 |

### 캘린더 카드 — 공통 `CalendarView`에 스타일 파라미터 추가 (기본값은 기존 모양 유지, 일정 추가 화면 영향 없음)
| 요소 | 피그마 | 적용 |
| --- | --- | --- |
| 카드 | x 18, w 324, `#FAFAFA`, radius 8, 상단 10 | 좌우 18, `systemBackground`, 상단 패딩 10 |
| 헤더 → 카드 | 106 → 110 | 4 |
| 월 | "10월" 20sp Medium `#EFA4C6`, 왼쪽 14 | `pointPink` |
| 이전/다음 | `fi-rr-angle-small-left/right` 20, `#8F8170`, 사이 14, 오른쪽 7 | 20, `onSurfaceColor5` 틴트, 사이 14, 오른쪽 8 |
| 월 → 요일 | 6 | 6 |
| 요일 | 12sp Medium `#C7C7C7`, 높이 25 | `onSurfaceColor8` |
| 날짜 줄 | 높이 54, 줄 사이 6 | 54 / 6 |
| 날짜 | 12sp Medium `#857563` (상단 2) / 다른 달 10sp `#D9D9D9` | `todoTextMain` / `surfaceColor15` |
| 선택한 날짜 | 분홍 원 14 + 흰 글자 | `pointPink` 원 14, `onSurfaceColor3` |
| 일정 점 | 5, `#EFA4C6`, 날짜 아래 | 할 일/루틴 구분 없이 `pointPink` 점 1개 |
| 중요 라벨 | "중요 N" 10sp, `#F3E8FF`, radius 2, h 13, px 4 | 기존 라벨 그대로 (`mainBackground`) |

- 날짜 줄 수는 기존처럼 6줄 고정 (달마다 높이가 바뀌면 페이저가 흔들림).

### 일정 추가 버튼 — 기존 `AddPlanButton` 수정
| 요소 | 피그마 | 적용 |
| --- | --- | --- |
| 버튼 | h 43, radius 6, `#EFA4C6`, 좌우 18 | `pointPink` |
| 내용 | + 아이콘 14(흰색) + "일정 추가" 16sp Medium 흰색, 사이 8 | 신규 `ic_plus` 14, `add_plan` = "일정 추가" |
| 카드 → 버튼 | 16 | 16 |

### 완료 제외 / 정렬
| 요소 | 피그마 | 적용 |
| --- | --- | --- |
| 버튼 → 줄 | 27 | 28 |
| 완료 제외 | 체크박스 14 (테두리 1 `#BCBCBC`, radius 2) + 사이 6 + 14sp Medium `#BCBCBC` | 켜짐 상태 디자인 없음 → 분홍 채움 + 흰 체크 (`ic_checkbox_square_on`) |
| 정렬 | "시간 순" 14sp Medium `#857563` + 화살표 16 | `fi_rr_angle_small_down` 16 |
| 드롭다운 | w 96, 흰색 radius 8, 그림자, 안쪽 10, 선택 `#EFA4C6`, 구분선 `#F6F6F6` | `SpiritsTodoDropdown` + 선택 색 파라미터(`pointPink`), 항목 여백 10, 앵커와 간격 6 |
| 정렬 이름 | 시간 순 / 중요 순 / 완료 순 | `DEADLINE` / `IMPORTANT` / `COMPLETE` 에 UI 문자열 매핑 |
| 줄 → 목록 | 16 | 16 |

### 일정 카드 — `PlanListItem`을 새 디자인으로 교체 (`CalendarTaskItem`)
| 요소 | 피그마 | 적용 |
| --- | --- | --- |
| 카드 | `#F7F7F7`, radius 8, 패딩 12, 카드 사이 10 | `surfaceColor16` |
| 체크 | 18, 미완료 테두리 1.25 `#BCBCBC` 원 / 완료 분홍 원 + 흰 체크 | 신규 `ic_check_circle_off_18`, `ic_check_circle_on_18` |
| 체크 → 제목 | 10 | 10 |
| 제목 | 16sp SemiBold `#8F8170`, 완료: `#BCBCBC` + 취소선 | `onSurfaceColor5` / `systemGrey` |
| 별 | 제목 뒤 6, 14 | `fi_rr_color_star` |
| 펼침 화살표 | 18 `#BCBCBC`, 펼치면 180° | `fi_rr_angle_small_down` 틴트 |
| 날짜 | 제목 아래 4, 12sp `#BCBCBC`, 들여쓰기 26 | 제목 시작선에 맞춤(28), `M월 d일 (E) H시 m분`, 종일이면 날짜만 |
| 펼친 버튼 | 날짜 아래 10, h 32, 흰색 radius 6, 사이 8, 14sp | 삭제 `systemRed` / 수정 `systemGrey` |
| 아래 여백 | 접힘 12 / 펼침 10 | 12로 통일 |

- 카드 클릭 → 펼침/접힘 (한 번에 하나), 체크 클릭 → 완료/완료 취소, 삭제 → 삭제, 수정 → 일정 수정 화면.

### 일정 없음 (Calendar2)
- 버튼 아래 56, `todo_empty` 46 + "예정된 일정 없음" 14sp `systemGrey` (`empty_plans` 문자열 값 변경). 완료 제외/정렬 줄은 숨김.

## Calendar3 검색

| 요소 | 피그마 | 적용 |
| --- | --- | --- |
| 헤더 오른쪽 | X 28 `#857563` | `todo_cross` 28 → 검색 닫기 |
| 패널 | 흰색, 헤더 아래 ~ 257, 그림자 (0, 16, 20, 4%) | `surfaceColor1`, 그림자 대신 `shadow(elevation)` 근사 |
| 검색창 | 헤더 아래 10, h 42, 테두리 1 `#D1D1D6`, radius 6, 좌우 12, placeholder "검색어 입력" 14sp `#C7C7C7`, 아이콘 20 | `onSurfaceColor2`, `onSurfaceColor8`, `fi_rr_search` 20 |
| 검색창 → 최근 검색 | 24 | 24 |
| 최근 검색 | 14sp Medium `#BCBCBC`, 왼쪽 20 | `systemGrey`, 18로 맞춤 |
| 전체삭제 | 12sp, 오른쪽 | 색은 스크린샷 기준 `todoTextMain` (Figma 호출 한도로 수치 미확인) |
| 칩 | `#ECECEC`, radius 17, 좌우 10 위아래 8, 12sp `#857563` + X 12 `#BCBCBC`, 사이 8, 칩 사이 6 | `systemArea`, `todo_cross` 12 `systemGrey` 틴트 |
| 최근 검색 → 칩 → 패널 끝 | 8 / 24 | 8 / 24 |
| 딤 | 패널 아래 화면 | `dimColor` (스크린샷 기준) |

- **동작 (MVI)**: 검색 아이콘 → `SearchOpened`, X/딤 클릭/뒤로가기 → `SearchClosed`, 입력 → `SearchQueryChanged`, 키보드 검색 → `SearchSubmitted` (최근 검색 맨 앞에 추가, 중복 제거, 최대 10개), 칩 클릭 → 그 검색어로 검색, 칩 X → 삭제, 전체삭제 → 모두 삭제.
- 검색 결과: 패널을 닫고 목록 자리에 이번 달 일정 중 제목에 검색어가 들어간 일정을 보여준다. 완료 제외/정렬 줄 왼쪽에 `'검색어' 검색 결과`와 해제(X)를 둔다. (결과 화면 디자인 없음 → 기존 스타일 재사용)

## 검증 체크리스트

- [ ] 홈 캘린더 버튼 / + 아이콘 → 캘린더, 뒤로가기 → 홈
- [ ] 날짜 선택 시 목록 갱신, 월 이동 시 점·중요 라벨 갱신
- [ ] 일정 추가 → 추가 화면, 완료 체크·삭제·수정 동작
- [ ] 완료 제외, 정렬 3종 동작 (완료 제외 시 완료 순 숨김)
- [ ] 일정 없을 때 Calendar2 모양
- [ ] 검색 열기/닫기, 검색 결과, 최근 검색 추가·삭제·전체삭제
- [ ] 일정 추가 화면의 날짜 선택 캘린더 모양 변화 없음
- [ ] `./gradlew :app:assembleDebug`, `./gradlew :app:testDebugUnitTest` 통과
