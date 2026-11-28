# 정령의 하루 프로토타입 수락 기준과 검증 경로

작성: 2026-09-18. 이번 구현의 **검증 계획과 착수 시점 기준선**이다. 체크가 없는 항목은 미충족 또는 아직 검증하지 않은 항목이며 완료를 의미하지 않는다. 구현 중인 다른 파일은 최종 검증 때 다시 확인한다.

**검증 기준:** A03 컴파일·149개 테스트·닫기6경계·세 해상도30장, 새 APK/export/WebGL 빌드와 실제 브라우저 스모크를 확인했다. 사용자 요청으로 추가 작업을 중단했다. 전체 제품 완료 판정은 아니며 아래 중단 시점과 미실행 범위를 따른다.


## 사용자 요청에 따른 중단 — 2026-09-18

- [A03 최종 보고서](../Validation/ForestPrototype/A03/review-report.md): APK199.644초/오류0/경고971·70,594,523바이트·v2서명 PASS, library28.988초/오류0/경고971, WebGL607.843초/오류0/경고1. 이전 산출물은 BeforeA03에 보존했다.
- [실제 브라우저](../Validation/ForestPrototype/A03/web-browser-smoke.md): 기존 이름/날짜/정수28/수첩/휴식 복원, 오늘 요약 끝 스크롤과 footer·닫기, 새로고침 뒤 동일 기록·오류0을 확인했다. 탭/루프백 서버 종료.
- 빌드 후 소스·아트8개 및 원본·메타7개 해시 동일. [최종 Editor 상태](../Validation/ForestPrototype/life-ui-20260918-131648-06316bbc/final-editor-state.json)는13:18:43 UTC Play종료/compiling=false/Android/QA빈값/Prepared·Running=false/00_Boot dirty=false다.
- 이어 시작한 생애 UI 검증은 [STOPPED INCOMPLETE](../Validation/ForestPrototype/life-ui-20260918-131648-06316bbc/report.json). 첫 Meeting 화면1장만 확인했고 첫 재시작 후 검증은 하지 않았다. L01/L02의 세 진로·배웅·세 알·두 번째 생애 전체 UI 완주는 NOT RUN으로 유지한다.
- 남은 큰 작업: Android 호스트 전체 빌드·실계정/기기 검증, 성장·배웅 UI 완주, 아트·움직임·표현 개선과 실제 재미 평가. 사용자 요청으로 이 검토본에서 멈췄으며 자동으로 추가 작업을 이어가지 않는다.


## 목표 범위의 근거

- 사용자 확정: 당일 첫 입장에 원래 일정·습관·TODO와 정령의 하루·실제 획득을 표시하고, 카드/버튼/배경 터치 시 아래로 내려가는 팝업을 거쳐 숲을 보여준다. 메인 정령 터치는 실제 오늘 기록·정령의 기분·기질에 맞게 반응한다.
- [상세 기획 v4](ForestDailyExperiencePlanV4.md): P0 실제 데이터와 진입, P1 생활 장면·대사, P2 세 입력 유형의 세 장면 사건, P3 월별 당시 기억, P4 장기 성장. 단일 독서 장면 제작은 첫 통합 확인이며 전체 완료 기준을 대신하지 않는다.
- [Prototype GDD v2.1](../../Docs/TODO_SPIRIT/TODO-SPIRIT%20ProtoType%20기획서.md) §20·24: 첫 정령·이름 제안(건너뛰기 가능)→현실 TODO→하루 팝업·행동→기록→성장→성체/진로→계속 생활 또는 선택한 마지막 동행→독립→알 3개 중 선택→두 번째 정령의 완주. 기본 공간 1개, 활동 장소 5개, 행동 5종 이상, 기질 조합 2~3개, 외형 3단계, 진로 3종, 선물 6종, 여행 1지역/준비물 3종.
- 사용자 위임에 따른 이번 결정(v4 §9): 첫날 이름(건너뛰기·나중 변경), 1·4·7 활동일 사건, 21 활동일 성체, 성체 이후 계속 생활, 사용자가 원할 때 배웅/새 동행. 기존 14일 시험 규칙을 대체한다. 정책 결정은 구현·완주 검증 완료를 뜻하지 않는다.
- 범위 제외: 가챠·경쟁·미니게임·자유 배치 하우징·생성형 자유 대화·장기 부재 페널티. 서버 클라우드 저장/다중 기기 동기화는 제품 결정 전 필수라고 추가하지 않는다.

## 기능 수락표

| ID | 완료에 필요한 결과 | 요구 증거 | 착수 시점 상태 |
| --- | --- | --- | --- |
| D01 | Android 숲 진입점에서 Unity가 열리고 인증 계정의 오늘 **전체** occurrence를 받는다 | 실제 호스트 빌드 및 실행, 일정·루틴/0·1·여러 건 수신 | Forest route 비어 있음, Unity 라이브러리/발신부 없음 |
| D02 | 계정·날짜·순서 번호 검증, 계정별 저장, 역전 응답/재전송 안전 | 계정 A/B 전환, 역순·동일 revision, 재시작 테스트 | 수신 검증은 있으나 다른 계정을 거부만 함 |
| D03 | 로딩·정상 0건·오프라인·오류를 구분하며 마지막 확인 시각/재시도를 제공 | 실패 후 기존 기록 유지 및 정상 빈 목록 화면 | 명시적 상태 계약 없음 |
| D04 | 긴 제목/50개/미정/완료 취소/삭제/날짜 전환에서도 원래 사실을 유지 | 입력 경계 테스트와 스크롤 화면 | 제목 80자 제한, 오늘 날짜만 허용 |
| E01 | 계정+날짜별 최초 입장 팝업, 같은 날 재입장 생략, 수동 재열기 | 새 저장·재시작·다음 날짜·계정 전환 Play 검증 | 수동 TodaySummary만 존재 |
| E02 | 원래 제목·일정/습관·완료 상태 → 정령 예정/완료·장소·이유 → 오늘 누적/신규 획득 | 0/미완료/완료 1·2·4개 화면 및 저장 대조 | 정령 일정/획득 결합 화면 없음 |
| E03 | 카드 빈 곳/확인/바깥 배경이 같은 하향 닫힘, 스크롤/재시도는 오작동 없음 | 세 진입의 영상/실제 입력, 연속 탭·회전·백그라운드 검증 | 즉시 CloseModal |
| E04 | 정수 0/10/18/25와 최고 지급 차액만 반영, 저장 성공 후 표시 | 완료 취소/재완료/팝업 재열기/저장 실패 테스트 | 기존 경제 테스트는 있으나 새 팝업 연계 검증 없음 |
| V01 | 정령 중심 구도, 읽히는 현재 행동/한 줄 대사, 보조 메뉴 위계 | 360×640/1080×2340/1366×768 실제 게임 캡처 | 품질 검토에서 작고 떠 있는 정령/작은 글씨 확인 |
| V02 | 장소 5개, 행동 5종 이상과 idle/이동이 실제 장면으로 구별 | 카테고리별 장면 Play 및 소품/몸/그림자 동작 검토 | 배경 1장·기존 4포즈의 절차적 움직임 |
| V03 | 정령 터치의 오늘 상태·기질 2개 이상·기분별 대사와 몸짓, 반복 2~3문장 | 동일 입력/다른 기질과 예정/완료/오류 비교 | 고정 인사 대사 |
| V04 | 버튼 누름/해제/비활성·팝업 전환의 일관된 반응, 동작 줄이기 | 실 입력과 설정 재시작 검증 | 축소와 이미지 교체 중심 |
| S01 | 집중/움직임/혼합·미정 각각 발견→시도→작은 완성 | 세 경로 전체 진행 기록 | 고정 잎 이야기 3회 |
| S02 | A/B 선택마다 즉시 문장·화면 소품·다음 방문 결과가 다름 | 같은 입력의 A/B 저장과 다시 열기 비교 | 일부 문구·소품 분기만 있음 |
| M01 | 월별 당시 외형/소품/관심/사건과 실제 TODO 연결 기록 | 서로 다른 두 달과 두 정령의 앨범 화면 | 현재 포즈로 초상 표시, 당시 스냅샷 없음 |
| M02 | 과거 정정은 사실 집계를 갱신하고 당시 이야기 보존, 버전/시각 구분 | 늦은 동기화·과거 완료 취소 회귀 테스트 | 오늘 날짜만 수신 |
| L01 | 여러 활동일→단계 A~F/외형 3단계→진로 3종과 근거 3개 | 격리 저장의 전체 성장 Play 및 재로드 | 기존 14일 시험 규칙/완주 로직 존재; 새 경험으로 재검증 필요 |
| L02 | 첫날 이름 제안/변경·선택한 마지막 동행·독립·알 3개·두 번째 정령·이전 기록 | 전체 완주 Play, 단계마다 저장/재시작 | 기존 생애 로직 존재; 정책 미결정 및 시각 품질 미검증 |
| A01 | 선물 6종/여행 1지역·준비물 3종이 실제 반응/기억으로 이어짐 | 지급·선택·귀환·재시작·반복 수령 검증 | 기존 UI/로직 존재; 새 경험 연계 검증 필요 |
| Q01 | 컴파일 오류 없음, 새 경계/저장/통합 테스트와 기존 테스트 통과 | 변경 이후 실행한 Test Runner 결과 | 과거 76개 결과는 새 구현의 근거 아님 |
| Q02 | Android/Web 빌드 및 가능 환경에서 실제 실행 | 현재 빌드 로그/플레이 결과; 못한 항목 NOT RUN+이유 | 모듈은 설치됨; 연결 Android 기기 없음 |
| Q03 | 실제 사용자 저장·기존 .meta/GUID·기존 미커밋 작업 보존 | 격리 저장 경로, diff와 meta 확인, 종료 시 override 해제 | 저장 보호 메뉴/SessionState override 있음 |

## 현재 사용할 수 있는 검증 도구

프로젝트 루트는 `C:\Develop\todo-with-spirits\unity\TodoSpirits`, Unity는 `6000.3.16f1`이다. `ProjectSettings/EditorBuildSettings.asset`에는 `00_Boot.unity`, `01_Main.unity` 두 씬이 활성 상태다. 별도 P1 검토 씬은 제품 빌드 씬이 아니다.

2026-09-18 실제 `mcp__unity__editor_status({})` 호출에서 `ready`, `compiling=false`, `playMode=stopped`, 위 프로젝트 경로를 확인했다. 도구는 세션별로 노출이 다를 수 있으므로 호출 성공을 확인한 담당자가 사용한다.

| 목적 | 실제 도구/메뉴/경로 |
| --- | --- |
| Editor 상태/컴파일 | `mcp__unity__editor_status`, `mcp__unity__recompile`, `mcp__unity__console` |
| 테스트 | `mcp__unity__run_tests({mode:"editor",async_tests:true})` 후 `mcp__unity__test_status`로 같은 실행 관찰 |
| 격리 Play | `TodoSpirits/Art Kit/4 - Use Isolated QA Save` → 00_Boot Play → Stop → `5 - Clear Isolated QA Save` |
| QA 저장 | `SessionState["TodoSpirits.ForestVerificationSave"]`; OS Temp의 `TodoSpirits-ArtKit-QA/forest-시간.json` |
| 실제 저장 | `Application.persistentDataPath/TodoSpirits/forest-save.json`; 날짜 가속/샘플 주입에 사용하지 않음 |
| 세 해상도 | `TodoSpirits/Art Kit/QA - Game View 360x640`, `1080x2340`, `1366x768` |
| 합성 UI 캡처 | `mcp__unity__capture_game_view({source:"screen",...})`; camera 소스는 Overlay Canvas를 놓침. 또는 `QA - Capture Game View` |
| Android APK | `TodoSpirits/Build Forest Android ArtKit QA APK` → `Validation/Concept01ArtKit/android-qa-build-report.json` 및 별도 APK |
| 범용 빌드 | `mcp__unity__build`의 target/scenes/outputPath로 구성 검증 후 실제 빌드; `build_status` 관찰 |
| Android 연결 | Unity 번들 SDK `platform-tools/adb.exe devices -l` 이번 실행: 연결 기기 **0개** |

Unity 설치 경로 `C:\Program Files\Unity\Hub\Editor\6000.3.16f1\Editor\Data\PlaybackEngines`에서 AndroidPlayer, WebGLSupport, windowsstandalonesupport 모듈을 확인했다. WebGL 빌드 지원 존재는 브라우저 실행 성공과 다르다. 저장소 `web`은 Vite/React 앱이고 현재 Unity 호스트가 없다.

## 착수 시점 호스트 계약과 파일 경계

- Android: `C:\Develop\todo-with-spirits\android`. 기존 `GetUserProfileUseCase`의 `userId`와 `GetTaskCalendarUseCase(오늘,오늘)`을 사용할 수 있다. `TaskRefreshBus`는 TODO 갱신 성공을 알린다.
- 수신: `Assets/TodoSpirits/Scripts/Runtime/ForestTodoBridge.cs`의 `UnitySendMessage("AppRoot","ReceiveTodoSnapshot",json)`. 착수 시 계약은 schemaVersion=1, accountId/date/revision, items ≤50, title ≤80. 발신자는 전체 목록을 전송해야 하며 실패를 빈 목록으로 바꾸면 안 된다.
- Android의 `settings.gradle.kts`/app 의존성에 Unity 모듈이 없고 `SpiritsTodoNavigation.kt`의 Forest route가 비어 있다. Unity export 및 Activity 수명/준비 handshake/계정 전환/재시도까지 연결해야 D01~D03를 검증할 수 있다.
- Android/back/web/Wiki는 현재 명시된 writable roots 밖이다. 필요한 구현은 대상 경로를 지정한 승인된 도구 작업으로 진행해야 한다. 전체 저장소 권한이 있다고 가정하지 않는다.
- 프로젝트 AGENTS.md를 읽었으며, 지정된 `../../Wiki/01 LLM Context.md`는 이 체크아웃에 없다. 문서 부재를 위키 확인 완료로 보고하지 않는다.

## 아트 제한

`Assets/ForestDailyP1Art`에는 `spirit_read_01`과 `station_desk` 후보만 있다. 두 번째 독서 프레임과 나머지 8종이 존재한다고 가정하지 않는다. `GeneratedReview/SC_P1_ReadingReview.unity`의 표시 성공은 프로덕션 연결이나 아트 승인 근거가 아니다. 분홍 바람 정령을 유지하고 후보 리소스는 독서 활동의 국소 배치에 사용한다. 없는 프레임을 복제해 애니메이션 납품 완료라고 기록하지 않는다.

최종 보고에서 PASS는 변경 이후 직접 실행한 검증에만 사용한다. 소스 검토는 STATIC CHECK, 빌드만 성공했으면 BUILD PASS, 실행하지 못했으면 NOT RUN과 이유를 기록한다. 한 장면/한 화면/단위 테스트만으로 전체 프로토타입 완료를 선언하지 않는다.

## 최신 A03 검증 — 2026-09-18 12:53 UTC 실행 증거 반영

[A03 변경·검증 보고서](../Validation/ForestPrototype/A03/review-report.md)를 기준으로 아래 범위만 갱신했다. 수락 항목은 기존 D01~Q03의22개를 유지한다. 정지 화면 배치 검토를 전체 미술 품질·실기기 입력·사용자 재미의 통과로 세지 않는다.

| 항목 | 실제 결과 | 근거와 한계 |
| --- | --- | --- |
| A03 컴파일 / EditMode | **PASS / 149/149 PASS** | [컴파일](../Validation/ForestPrototype/A03/recompile-result.json) completed/failed=false/errors=[], [테스트](../Validation/ForestPrototype/A03/tests-response.json) completed, 실패·건너뜀·inconclusive0, **4.44초**. A02의3.85초 결과와 별도 실행이다. Android 호스트 컴파일 결과가 아님 |
| 결과창 닫힘 이후 반응 | **PASS — Editor360×640 6경계** | [계측](../Validation/ForestPrototype/story-close-a03-20260918-124712-16e719d8/report.json), [실행·10장 시각 검토](../Validation/ForestPrototype/story-close-a03-20260918-124712-16e719d8/README.md). Focus/확인·Movement/우상단닫기·Everyday/배경을 각각 normal/reduced로 실행했다. 실제 선택 후2초 이상 결과창을 읽는 동안 반응0회→모달 제거 후 callback1회→표정/동작 후 idle 복귀. 연타/닫힌 뒤 재호출에도 추가 반응·보상 없음. normal은 위치/크기 변화, reduced는 몸 고정·표정 변화를 확인. **3계열×3경로×2모드 전체18조합이나 OS 터치는 아님** |
| footer·메인 소품/라벨·활동 안내 | **PASS — 세 해상도30장 구조/시각 범위** | [30행 구조 결과](../Validation/ForestPrototype/A03/layout-20260918-125152-5f4f7047/report.json), [360검토](../Validation/ForestPrototype/A03/layout-20260918-125152-5f4f7047/visual-review-360.md), [1080검토](../Validation/ForestPrototype/A03/layout-20260918-125152-5f4f7047/visual-review-1080.md), [1366검토](../Validation/ForestPrototype/A03/layout-20260918-125152-5f4f7047/visual-review-1366.md). 각각10장 전부 직접 검토했다. 수첩·우체통·바람개비의 결과/메인, 오늘/선물/나들이 footer, 긴 소품명+나들이 안내에 명백한 겹침·잘림·얼굴 가림 없음. 흰 footer 띠가 사라지고16단위 메인 라벨·활동 안내가 분리된다. 마지막 긴 이름/여행은 격리 데이터의 세 번째 선택을 제거한 **배치 전용 fixture**이며 정상 성장 증거가 아님 |
| A03 검토의 남은 한계 | **일부 NOT RUN / 미적 한계 유지** | 정지 캡처의 viewport 밖 선물 마지막 행·오늘 생활 전체의 스크롤 접근을 새로 검증하지 않았다. 360의 작은 보조 글씨, 수첩·바람개비의 약한 그림자/받침으로 독립 그림처럼 보이는 인상, 편지→우체통의 의미 재사용, 화풍 차이는 남는다. 좌표 배치 PASS를 자연스러운 환경 소품 완성으로 확대하지 않음 |
| A03 빌드·브라우저·최종 복구 | **PASS — 위 중단 절의 명시 범위** | 새 APK/export/WebGL·실제 브라우저와13:18:43 최종 Editor 상태를 위 링크로 확인했다. 후속 생애 QA는 사용자 요청에 따라 INCOMPLETE로 안전 종료. 아래 A02는 과거 이력 |

## 이전 A02 검증 — 2026-09-18 12:38 UTC 마감

아래는 착수 시점 상태와 A01 검증 이후의 실제 증거다. 상세 범위는 [완료 감사](ForestPrototypeCompletionAudit.md), [모션·입력 실행 기록](../Validation/ForestPrototype/MotionInput/README.md), [A02 변경·검증 보고서](../Validation/ForestPrototype/a02-review-report.md)를 따른다. **A02 APK/export/WebGL은 BUILD PASS이고, 새 WebGL의 실제 브라우저 스모크도 명시 범위 PASS**다. 추가 이야기 UI108선택과 A02 최종 Editor 복구까지 아래 범위에서 확인했다. Android 호스트 컴파일·기기 실행과는 구분한다.

| 항목 | 실제 결과 | 근거와 한계 |
| --- | --- | --- |
| A02 Unity EditMode / S02 서비스 | **PASS 149/149** | `motion-art-tests-response.json` 내부 JSON completed, 실패·건너뜀0,3.85초. 기존110건+36분기(3계열×첫A/B×둘째A/B×최종3선택)+과거 장면3건. 1/4/7일 선택 및2/5/8일 재방문 후 JSON 저장/서비스 재생성, 기대 caption·소품·다음 질문·공개터치대사, 동일revision 무중복을 검사. 최초 memory의 선택/소품/TODO연결을 기대값과 먼저 비교해 잘못된 값의 보존만 검사하지 않는다. 실제 Sprite UI는 다음의 별도 실행으로 검증 |
| S01/S02 세 계열·36경로 실제 UI 연결 | **PASS(Editor360×640 프로그램 조작 범위)** | [최종 결과](../Validation/ForestPrototype/story-ui-20260918-123029-8722d978/report.json):12:36:31 UTC,36경로×3사건 **108/108행 PASS**, 대표25캡처 실파일 확인. 화면/마스크/중앙 raycast 검사 후 Button.onClick으로 선택하고 실제 Text·선택/결과/다음날 소품 Sprite 참조를 기대값과 대조했다. 각 다음날에 예시 저장 파일을 다시 열어 장면을 검사. OS 입력·Android/Web 실행·프로세스 재시작 검증은 아님. 세 계열 fixture는 WORK_STUDY/HEALTH/RELATIONSHIP이며 혼합·NONE의 UI 입력 조합 전체는 미실행 |
| 이야기 대표 캡처 시각 검토 | **검토 완료(대표25장)** | [후속 검토 보고서](../Validation/ForestPrototype/story-ui-20260918-123029-8722d978/README.md), [대표 캡처](../Validation/ForestPrototype/screens/story-ui-20260918-123029-8722d978): 담당25장·root3장·감사4장 독립 검토. 질문/선택/결과와 주 버튼에서 명백한 잘림·겹침은 발견하지 못했다. 흰 footer 띠의 시트 분리감, 작은 메인 소품 라벨, 소품이 떠 보이는 배치, 편지에 우체통을 쓰는 의미 재사용은 A02 품질 한계다. JSON의 visualReview는 자동 검사하지 않는다는 NOT RUN 기본값이며 README가 후속 육안 검토 완료 기록이다. 아트·재미 승인으로 확대하지 않음 |
| V02 생활·몸·소품·그림자 | **PASS(Editor360×640 범위)** | `MotionInput/activities.json`: 실제 WORK_STUDY/HEALTH/LIFE/RELATIONSHIP/REST snapshot→메인, 각8.2초 이상 좌표·회전·크기/그림자 샘플과3장씩 캡처. idle은 명시적으로 Animator.Activity를 설정한 컴포넌트 검증. 정상 사용자 입력 전체 경로로 확대하지 않음 |
| V02 산책 가방·방석 수정 후 | **PASS(Editor360×640 재검증)** | 최초 기록의 가방X정지/방석왼쪽 배치를 발견해 승인된 `ForestSpiritAnimator.cs`, `ForestAppView.Habitat.cs`만 국소수정. 현재 증거는 `fixed-activities.json`, `screens/fixed-walk-*`/`fixed-rest-*`. 가방/그림자 이동 차이 최대0.000202, 방석235.296 정사각·정령 중앙 발밑. **산책 가방 모션의 다른 비율·모바일은 NOT RUN**이며 방석 배치는 다음 행에서 보완 |
| V01/V02 방석·성장 v2 추가 두 해상도 | **PASS(Editor 배치 범위)** | [layout-followup.json](../Validation/ForestPrototype/MotionInput/layout-followup.json) completed와 [PNG 크기](../Validation/ForestPrototype/MotionInput/layout-png-dimensions.json):1080×2340/1366×768 각각 휴식·v2중간·v2성체 총6장. 담당자6장·root대표2장 시각 검토에서 중앙 방석/외형 잘림 없음을 확인. 기존360×640과 합쳐 해당 배치 세 해상도 PASS. 휴식은 실제 REST snapshot, 성장 외형은 actor Sprite 배열 fixture로 **성장 완주나 다섯 행동 전 해상도 검증은 아님** |
| V04 두 버튼 구현 | **PASS(Editor EventSystem 범위)** | `MotionInput/input.json`: ForestUiMotion/ArtKitButtonMotion down 약.965→up 약1, disabled 약1·click callback0, hit target scale1 유지, disabled sprite 확인. OS 마우스/실기기 터치를 수행한 결과가 아님 |
| V04 동작 줄이기 저장·재시작 | **PASS(Editor Play 재시작)** | `reduced-before-restart.json`/`reduced-after-restart.json`: 실제 Settings 버튼으로 켜기→Play종료→재시작→같은 격리 저장. 파일/서비스/두 UI 정책/actor 모두 true, 첫 Update 뒤12샘플 고정, button down scale1. OS 프로세스 종료/Android lifecycle 검증은 NOT RUN |
| E03 반복 닫기·스크롤 | **PASS(이벤트 주입 경계)** | `MotionInput/input.json`: 실제 오늘 팝업 닫기 시작+20회 같은 이벤트/닫는 중 ScrollRect 호출, 확인1회·닫힌 뒤 처리1회, 약.30초 하강·제거, 지갑10→10, 재열기/닫기 정상. 실기기 연속탭·다중터치·회전·백그라운드 복귀·재시도 네트워크 경합은 NOT RUN |
| 성장 v2 알파 정리·연결 | **PASS(픽셀·참조·Editor 화면 범위)** | `growth-alpha-cleanup.md`, `growth-v2-prepare-final.json`. 기존 본체 RGBA/네 포즈/캔버스 유지, 낮은알파 및 본체 외 잡점을 새 사본에서만 제거. Prepare/Validate 성공. `MotionInput/restart-and-fixes-complete.json`과 growth-v2 캡처에서 실제 actor의 v2 연결 확인. 약한 안티앨리어싱 일부 변화가 있고 기기 압축/Atlas·최종 아트 품질 검증을 대신하지 않음 |
| 기존 파일·검토 저장 보호 | **PASS(검사 범위)** | [빌드 후 원본 보존](../Validation/ForestPrototype/a02-preserved-originals.json): 기존 v1 PNG2/meta2·P1meta2·어린 정령meta1 **7파일** 해시 보존. [빌드 기준 유지](../Validation/ForestPrototype/a02-post-build-source-check.json): 빌드 전 manifest의 소스/아트/프리팹9파일 모두 동일. MotionInput 및 이야기 UI는 고유 Temp 예시 저장만 사용했고 cleanup 완료. 전체 미커밋 파일의 전후 동일 증명으로 확대하지 않음 |
| A02 최종 Editor 복구 | **PASS(12:38:40 UTC 상태)** | [최종 실제 조회](../Validation/ForestPrototype/editor-a02-final-state.json): 내부 success=true, playing=false, compiling=false, Android, QAoverride="", export=true, bundle=false. 이야기 README의12:37:07 cleanup·준비/실행 flag 해제·깨끗한00_Boot/360×640 반환과 일치. 이후 A03 작업의 종료 상태를 뜻하지 않음 |
| A02 Android 단독 검토 APK | **BUILD PASS / 서명 PASS** | [실제 빌드](../Validation/ForestPrototype/android-a02-build-response.json):154.936초, 오류0/경고971. [APK 검사](../Validation/ForestPrototype/android-apk-a02-verification.json):70,595,495바이트·v2서명 PASS. 빌드 보고서의 누적 파일 크기와 실제 APK 크기를 구분한다. TODO 호스트 APK 및 기기 설치/실행은 NOT RUN |
| A02 Android 호스트용 Unity export | **BUILD PASS** | [실제 빌드](../Validation/ForestPrototype/android-library-a02-build-response.json):19.929초, 오류0/경고971. `Exports/AndroidLibrary` 갱신, 이전 export는 `Exports/AndroidLibrary-BeforeA02`에 보존. SDK36.1이 필요한 실제 TODO 호스트의 컴파일을 통과한 결과는 아님 |
| A02 WebGL | **BUILD PASS** | [실제 빌드](../Validation/ForestPrototype/webgl-a02-build-response.json):536.386초, 오류0/경고371,44,508,555바이트. 이전 산출물은 `Build/ForestPrototype/WebGL-BeforeA02`에 보존. A01의 경고1건 결과와 구분 |
| A02 실제 브라우저 후속 실행 | **PASS(명시 범위)** | [브라우저 기록](../Validation/ForestPrototype/web-browser-a02-smoke.md): IAB viewport1280×720/Unity canvas960×600에서 화면 버튼만 사용. A01 예시 저장·이름/날짜/지갑28/관찰 수첩/휴식 복원, 중앙 방석 확인, 동작 줄이기 켬→새로고침→켬 복원. error로그0. 다시 끔으로 복구 후 탭·루프백 서버 종료. 모바일·실제 계정·전체 이야기/나들이·성능은 NOT RUN |

`MotionInput/result.json`은 위 경계를 요약한다. `activities.json`과 최초 캡처는 수정 전 결함을 포함하므로 현재 산책·휴식의 근거로 사용하지 않는다. P1 두 후보/독서1프레임 제한도 그대로다. 버튼과 동작의 기능 검증을 실제 사용자의 자연스러움·재미 평가로 환산하지 않는다.

## 후속 A01 변경 — 2026-09-18

선물이 이후 생활에 보이지 않고, 나들이 사건을 앨범에서 다시 읽을 수 없던 연결을 보완했다. 선물 D+1~3 생활·출처·기억, 여행 중 대사, 귀환일 앨범 재열람을 추가했다. 관련 세부 정책은 v4 §9.4와 GDD §10.2~10.3에 반영했다.

- 당시 A01 코드의 EditMode: `Validation/ForestPrototype/editmode-interventions-response.json` 내부 결과 **110/110 PASS**, 실패/건너뜀0. 현재 최신 실행은 위 A03의149/149(4.44초)다.
- 후속 UI: `Validation/ForestPrototype/intervention-ui-qa.md`에 실행 방식·개별 PASS·검증 스크립트 오류와 복구·시각 캡처·NOT RUN 범위를 기록했다. 격리 예시 저장의 Editor 프로그램 UI 검증이며 실기기 검증은 아니다.
- 아래의 이전 102개 테스트 및 Android/Web 빌드 기록은 A01 이전 소스 결과다. 후속 빌드 결과는 완료한 별도 보고서를 근거로 추가하며, 예전 빌드에 새 기능이 포함되었다고 해석하지 않는다.

| A01 이후 빌드 | 실제 결과 | 근거 |
|---|---|---|
| Android 단독 검토 APK | BUILD PASS, 서명 검사 PASS | `android-a01-build-response.json`, 115.008초, 오류0/경고971. `forest-prototype-a01-20260918.apk` 실제 70,607,939바이트. v2 서명과 SHA256은 `android-apk-a01-verification.json`. 기기 설치/실행은 NOT RUN |
| Android 호스트용 Unity export | BUILD PASS | `android-library-a01-build-response.json`, 13.412초, 오류0/경고971. `Exports/AndroidLibrary` 갱신. 이전 export 전체는 `Exports/AndroidLibrary-BeforeA01`에 보존. 실제 TODO 호스트 컴파일 결과가 아님 |
| WebGL A01 코드 | BUILD PASS / 브라우저 실행 PASS(명시 범위) | `webgl-a01-build-response.json`, 375.043초, 오류0/경고1, 44,586,683바이트. `web-browser-a01-smoke.md`: 이전 저장 복원, 실제 버튼으로 담요 구매→다음 예시 날 휴식·출처·보상 구분→새로고침 후 이름/수첩/날짜/지갑28/휴식 보존. error로그0. 모바일/실제 계정 NOT RUN |
| 최종 Editor·핵심 메타 | PASS(명시 범위) | `editor-a01-final-state.json`: Play종료, compiling=false, Android, QAoverride없음, 기존 export=true/bundle=false 복원. `preserved-meta-a01.json`: 기존 핵심 메타3개 해시 보존 |

두 빌드의 경고는 `android-a01-build-summary.json`에 메시지별로 묶었다. 기존 Sentis compute shader 및 Pipeline 실행 구성 경고이며 오류0을 기기 GPU 검증으로 확대하지 않는다. `a01-source-hashes.json`은 이 후속 검증의 주요 코드 기준을 기록한다.

### A01 변경 파일과 재현 방법

| 파일(Assets/TodoSpirits 아래) | 변경 이유 |
|---|---|
| `Scripts/Runtime/ForestDailyModels.cs` | 선물 활동의 출처·날짜를 하루 기록에 보관 |
| `Scripts/Runtime/PrototypeApplicationService.ForestDaily.cs` | D+1~3 선물 생활·현재 정령 귀속·과거 날짜 기준 및 여행 대사 연결 |
| `Scripts/Presentation/Forest/ForestAppView.Album.cs` | 해당 정령의 귀환 날짜에 나들이 재열람 버튼 추가 |
| `Scripts/Presentation/Forest/ForestAppView.Dialogs.cs` | 저장된 사건·준비물·발견물·당시 외형과 날짜 기록 복귀 연결 |
| `Tests/RuntimeEditMode/ForestInterventionExperienceTests.cs` | 선물6종·유효기간·저장·정령 귀속·여행 대사 회귀 검증 |
| `Editor/ForestWebPersistenceBuild.cs` | 이전 브라우저 검증에서 발견한 저장 유실을 막는 HTML 후처리. A01 새 빌드에서도 자동 적용·실제 복원을 확인 |

Unity 재현은 새 격리 QA 저장을 고른 뒤 `00_Boot` Play→예시 체험에서 완료4개→담요 선물→다음 예시 날짜→오늘 요약/휴식 메인→앨범 순서로 진행한다. 나들이는 예시 날짜를 쌓아40정수를 확보한 뒤 출발·귀환·앨범 재열람을 확인한다. 실제4시간 대신 Editor 시간 가속을 썼다면 그 사실을 결과에 적는다. 종료 후 Play를 멈추고 격리 QA 경로를 해제한다.

리스크: 선물을3일간 대표 생활로 우선 보여주는 빈도는 플레이 평가로 조정할 수 있다. 구 데이터의 여행 귀환일이 없으면 시작일로 분류하므로 당시 정확한 귀환일을 복구하지 않는다. 신규 필드는 이전 JSON에서 비어 있을 수 있어 필수값으로 취급하지 않는다. 성장 시트의 낮은알파 잡점 정리는 후속 A02에서 수행했으며, 최종 아트 통일감·기기 표현/성능·실제 계정 연결은 남아 있다.

## 구현 후 검증 기록 — 2026-09-18 (A01 이전)

아래는 위 착수 시점 표와 구별한 실행 기록이다. 새 후속 수정이 들어간 경우 해당 수정 이후의 컴파일·검증 결과를 다시 연결한다.

| 검증 | 현재 결과 | 근거와 범위 |
| --- | --- | --- |
| Unity EditMode 1차 | PASS 94/94 | `Validation/ForestPrototype/editmode-final.json`. 이후 성장 귀속·외형·호스트 응답 수정 이전 결과 |
| 최종 게임 런타임 EditMode | PASS 102/102 | `Validation/ForestPrototype/editmode-post-fixes.json`, 런타임 마지막 변경 이후 새 실행 0.96초. 독립 당일 다음 동행, 정령 없는 다음 날, 소유 귀속/복구, 세 진로의 21 활동일 완주 포함. 대기 기록 UI·일반 콜백 식별 수정도 컴파일된 상태. 이후 추가한 WebGL HTML 후처리는 별도 Editor 컴파일·실제 메서드 호출·브라우저 실행으로 검증 |
| 실제 입력 360×640 1차 | PASS(기록된 항목만) | `Validation/ForestPrototype/manual-ui-first-pass.md`. 첫 팝업·스크롤·확인/빈 곳/배경 닫기·같은 날 재입장·터치·첫 집중 사건·앨범. Windows의 Editor 입력이며 Android 터치 검증 아님 |
| 성장 Sprite 설치·참조 | PASS | 두 신규 시트의 네 표정씩 총 8개를 명시적으로 분할하고 프리팹 단계별 배열에 연결. Prepare/Validate 오류 없음. 신규 메타의 자동 노이즈 분할만 백업 후 수정 |
| 기존 핵심 메타 보존 | PASS(검사한 3개) | `preserved-meta-before-growth.json`, `preserved-meta-after-growth.json`. 기존 어린 정령·P1 독서·P1 책상 메타 SHA256 전후 동일. 전체 미커밋 파일 보존의 포괄 검사를 뜻하지 않음 |
| 3개 해상도의 메인/팝업/앨범 | PASS(Editor 프로그램 UI 검증) | `scripted-ui-qa.md`, `screens/`. 오늘 버튼 제목 누락을 고친 `final-` 메인 원본 3장 사용. 성장 3단계, 50개×255자, 기질별 반응/동작 줄이기, 같은 날 새 알/이름, 두 달의 이전 정령 초상, 대기 기록 진입도 실행. Native 입력 1차 및 Android 실기기 결과와 구분 |
| Android SDK 환경 | STATIC CHECK / 설치 NOT RUN | `android-environment.json`, `android-sdkmanager-list.log`. SDK 36.1 공식 목록 확인, 설치는 새 라이선스 수락이 필요해 사용자 응답 대기. 기존 SDK·인증 설정 미변경 |
| Android 순수 Kotlin 계약 테스트 | PASS 10/10(독립 JVM) | `android-pure-jvm-tests.json` 및 관련 로그. 실제 입력 변환 5개+응답 식별 판정 5개. 캐시의 Kotlin 2.2.21/JUnit 4.13.2/JDK 21로 실제 소스와 domain DTO를 직접 컴파일. Android 프로젝트의 Kotlin 2.2.10과 버전이 다르며 Android Gradle/SDK/Unity 실행 검증을 대신하지 않음 |
| Android Unity 라이브러리 export | BUILD PASS | `android-library-build.json`. `Exports/AndroidLibrary`, 49.66초, 오류 0/경고 971. 기존 export 경로가 없음을 확인하고 생성. 호스트 APK 빌드/실행 성공을 뜻하지 않음. 경고는 후속 빌드 보고서에서 분류 검토 |
| Android 단독 Unity APK | BUILD PASS / 서명 검사 PASS | `android-apk-build.json`, `android-apk-verification.json`. ARM64, 147.28초, 오류 0, APK 실제 70,600,927바이트, APK v2 서명 검증 성공. 기존 `com.DefaultCompany.TodoSpirits` 유지, 최소 API25/대상36. 스토어 배포용 승인이나 실제 TODO 호스트 APK가 아님 |
| Android 빌드 경고 검토 | STATIC CHECK | `android-build-warnings.json`: 971건/81개 메시지 그룹. 대부분 기존 Sentis/AI Inference compute shader 지원 조합 경고이며 원격 Pipeline RuntimeManager 부재 안내 포함. 새 게임 코드·성장 Sprite 누락 오류는 확인되지 않음. 기기 GPU 검증은 별도 |
| Android 호스트 Gradle 구성 | PASS | `android-host-online-help.json/.log`. 실제 `:help`가 app/core/data/unityLibrary 설정을 통과. `rootProject.project` 참조 수정, AGP9의 지원되는 기존 DSL 사용 설정, Unity export ABI와 호스트 ABI 일치 반영. export 원본은 보존 |
| Android 호스트 전체 빌드 시도 | 환경 오류로 중단 | `android-host-build-attempt.json/.log`. `:app:testDebugUnitTest --tests '*ForestHost*Test' :app:assembleDebug`를 실제 호출했으나 `android-36.1` SDK 대상이 없어 의존성 계산 단계에서 종료 코드 1. 앱 소스 컴파일·Gradle 단위 테스트·APK 조립 task 실행은 0개로 **NOT RUN**. SDK 라이선스 답변 대기, 자동 설치/수락 없음 |
| Android 호스트 실기기 | NOT RUN | 호스트 SDK/빌드 준비와 연결 기기 필요. 다시 실행한 adb에도 연결 기기 0개. 순수 계약 테스트 PASS와 전체 Activity/실기기 통합 검증은 구분 |
| WebGL 본체 빌드 | BUILD PASS | `webgl-build.json`. 855.19초, 오류 0, 경고 371, 44,630,131바이트. 경고는 Sentis 370건과 Pipeline 1건으로 분류. 후속 브라우저 저장 수정은 HTML 설정이며 WASM/런타임 소스는 변경하지 않음 |
| WebGL 브라우저 첫 실행 | PASS(실행 항목만) | 로컬 IAB에서 첫 입장, 예시 하루, 일정/정령 활동/획득 스크롤, 하강 닫힘, 이름, 터치 대사, 첫 집중 사건 A와 결과 소품, 같은 날 요약 재열람 신규 획득 0 확인 |
| WebGL 브라우저 재접속 저장 | 수정 후 PASS(1차 FAIL 보존) | `web-browser-smoke.md`, `webgl-persistence-postprocess.json`. 기본 HTML의 `autoSyncPersistentDataPath` 비활성으로 이름/선택 유실을 재현한 뒤 빌드 후처리를 추가. Editor 컴파일/메서드 적용/멱등성 확인. 실제 브라우저 재로드 후 WebSaved 이름·관찰 수첩·정수 10 복원, 당일 예시 첫 팝업 생략, 수동 요약 신규 0 확인. WASM/data 해시 보존 |
| Web 실제 계정 호스트 | NOT RUN | 저장소 웹 앱에 Unity 호스트가 없으며 실제 계정 API 연결을 실행하지 않음 |
| 종료 시 Editor 정리 | PASS | `editor-final-state.json`: Play 종료, compiling=false, Android 대상 복구, QA 저장 override 빈 문자열. `preserved-meta-after-builds.json`의 기존 핵심 메타 3개 해시 보존 |

### 현재 구현 연결

- 첫날 이름/건너뛰기/변경, 1·4·7 활동일 분기 사건, **21 활동일+선택3회** 성체와 선택적 배웅을 구현했다. 구체 정책은 v4 §9에 기록했다.
- 새 Android 진입점과 계정 분리·날짜·요청 식별 handshake가 있다. 세부 계약과 오프라인 최초 진입 제한은 [호스트 연결 문서](ForestAndroidHostIntegration.md)를 따른다.
- 입력 제목 한도는 255자이며 50개의 전체 오늘 목록을 받는다. 과거 정정은 사실을 고치고 이미 감상한 장면·지급한 정수를 보존한다.
- 성장 외형 두 시트와 차 컵·휴식 방석은 별도 제작 리소스다. P1 패키지의 누락 8종이나 독서 두 번째 프레임을 완성 처리하지 않았다. [제작 기록](ForestGrowthArtProduction.md)을 참고한다.
- 실제 사용자 저장과 예시 체험은 분리했다. 모든 날짜 가속·샘플 입력은 격리 QA 또는 예시 저장에서만 수행한다.

### 남은 완료 조건

- Android 호스트 전체 컴파일·단위 테스트·APK: SDK 36.1 설치 필요. 공식 SDK 라이선스 수락에 대한 사용자 응답 대기이며 자동 수락하지 않았다.
- 실제 계정 로그인→숲→TODO 수정→복귀, 자정/오프라인/계정 전환, Activity 종료, 기기 성능·터치·진동: 연결 Android 기기가 없어 NOT RUN.
- Web 실제 계정 연결과 모바일 브라우저 입력·성능: NOT RUN. 현재 WebGL은 분리된 예시 체험 검증이다.
- A02 모션/v2 아트의 APK/export/WebGL 빌드와 실제 브라우저 스모크, 방석·성장 v2의 Editor 세 요청 해상도 배치, 이야기36경로108선택의360×640 실제 Sprite 연결 및 최종 Editor 설정/QA override 복구는 위 명시 범위에서 완료했다. 산책 가방 모션의 추가 두 비율은 NOT RUN이다. 후속 A03은 상단의 별도 검증 범위를 따른다.
- 혼합/NONE UI 입력 조합, 세 진로 전체 성장·배웅 UI 완주, 전체 장식·여행 준비물별 UI왕복 등 구체적인 미확인 범위는 [완료 감사](ForestPrototypeCompletionAudit.md)에 남겼다. 36분기 서비스 저장/다음 방문과 실제 Sprite 연결·다섯 생활 장면·두 버튼·설정 Play 재시작·모달 반복 입력의 완료된 범위를 다시 미실행으로 요구하지 않는다. A03에서 흰 footer 제거·메인 라벨/활동 안내 분리는 세 해상도30장, 결과창 이후 반응은6경계에서 확인했다. 360의 작은 보조 글씨·의미 재사용·수첩/바람개비의 약한 접지 인상은 남는다. 스크롤 밖 항목 접근과 전체 시각 품질을 정지 캡처만으로 수락하지 않는다.
- 낮은알파 외곽 잡점 정리는 v2 사본에서 수행하고 원본을 보존했다. 기기 압축·최종 아트 통일감과 실제 사용자의 재미/애착 평가는 남아 있다. 기능·빌드 PASS가 컨셉 원화 수준의 완성도나 재미를 입증하지 않는다.
- 따라서 핵심 프로토타입 구현과 검증 가능한 빌드는 마련했지만 **전체 통합 완료로 판정하지 않는다**.
