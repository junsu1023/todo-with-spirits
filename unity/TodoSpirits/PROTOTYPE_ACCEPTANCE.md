# 클라이언트 프로토타입 완료 점검

기준: `../Docs/TODO_SPIRIT/TODO-SPIRIT ProtoType 기획서.md` v2.0. 서버 API·인증·서버 저장 연동은 사용자 요청에 따라 제외한다. 기능 구현, Editor 검증, 실제 기기 검증을 구분한다.

## 현재 판정

**서버 연동을 제외한 기본 클라이언트 프로토타입 제작 완료.** 판정 근거는 GDD 20장의 P0~P3 구현과 24장의 전체 흐름을 최신 Editor에서 완주한 결과, 58개 테스트 통과, Android 내보내기 및 APK 빌드 성공이다. 아래에 적은 실제 기기·사용성 조사·아트 고도화는 확인한 범위를 넘는 품질 검증/후속 작업이며, 검증 완료라고 주장하지 않는다.

## 커밋 전 재검증 — 2026-09-16

- Unity 6000.3.16f1에서 TodoSpirits 테스트 어셈블리의 EditMode 테스트 58개 통과, 실패·건너뜀 0개.
- 재검증 직후 Editor는 Play 정지·컴파일 완료 상태이며, MCP 콘솔 오류 조회 결과 0개.
- TodoSpirits 스크립트의 누락된 meta 및 해당 폴더의 중복 GUID 없음. `git diff --check` 통과.
- 이번 확인은 자동 테스트와 파일 검사 범위다. 아래 2026-09-09의 전체 UI 검증과 Android 빌드를 다시 수행한 것은 아니다.
- 현재 입력은 로컬 샘플이며 서버 TODO 연동, 새 기획 방향 및 '미정' 카테고리 개선은 이번 구현에 포함되지 않는다. 기획서 원문은 기존 Git 제외 규칙에 따라 로컬에 유지한다.

## 필수 기능별 근거

| 기획 영역 | 현재 구현 및 근거 | 판정 |
|---|---|---|
| TODO 분리·9개 카테고리 | TaskClassifier와 9종 기본 분류 테스트. Unity는 완료 목록을 입력원 인터페이스로 받음 | 로컬 구현/테스트 확인 |
| 분류 우선순위 | 수정 기록→루틴→키워드/카테고리. 저장 복원·충돌·잘못된 기억 폴백 테스트 | 확인. 유사 제목은 공백/대소문자 범위 |
| 하루 대표1·보조0~2 | SpiritDayGenerator, 동일 입력/순서 무관/기질 차이/빈 입력 테스트 | 구현·기존 테스트 확인 |
| 같은 날 결과 고정·기록 | 서비스 저장 결과 재사용, 날짜별 기록·선택 이유·현실 TODO 연결 | 저장 복원 확인 |
| 정수0/10/18/25·상한·차액 | EssenceRewardCalculatorTests의 0/1/2/3/4/99개와 최고 지급구간 차액 | 테스트 확인 |
| 기질8종 중2~3 | 알 조합·전체 기질 도달성·행동 점수·세 번째 기질 테스트 | 테스트 확인 |
| 집/앞마당·장소5·행동5 | LivingSpace와 SpiritLivingActor. 장소3→5, 표정·소품·읽기/제작/걷기/차/휴식 | Play 화면 및 모션 표본 확인 |
| 단계A~F·활동일·부재 무손실 | CompanionLifeRules. 28/42활동일·빈 날·순서/중복 방지 테스트 | 최신 UI로 A→F 28활동일 확인 |
| 외형3단계·성장 이벤트 | 크기/색/잎 수, 성장 초상화, 확인 버튼, 장소·기질·관심사 힌트 | 최신 UI 첫 만남·성체 초상화 확인 |
| 반복 패턴·진행 활동 | 실제 반복으로 시작하고 대응 활동일에 진척. 기록/후보 반영 테스트 | 로직 확인. 전용 활동 소품 미구현 |
| 진로3·근거3개 | 실제 행동·반복·기질·진행 활동을 반영. 준비 단계 미리보기와 성체 확정 분리 | UI 공예가·근거3개, 성체3종 비교 화면 확인 |
| 회고·이름·마지막 동행·독립 | 실제 UI: 성체→회고→이름 새봄→메인→ReadyToLeave→독립 | 최신 전체 흐름 확인 |
| 알3·두 번째 정령 | 실제 UI로 알3개 확인 후 선택. 기질 Active/Curious/Independent | 확인 |
| 함께한 정령·과거 기록·편지 슬롯 | 동행 목록/날짜별 상세/이름/진로/여행/편지 슬롯, 과거 정령 초상화 추가 | 저장 유지 및 최신 잎새 기록 화면 확인 |
| 선물6·기질 반응·이후 영향 | 목록6개, 기질 설명/반응, 다음3일 후보. 테스트와 UI 구매 | 확인 |
| 여행1·준비물3·4시간·실패없음 | 세 준비물 저장/귀환/중복 수령 테스트. 실제 UI 가속 귀환·발견물 수령 | 확인. 실제4시간 대기/모바일 복귀 미검증 |
| 꾸미기3~5 권장 | 고정5슬롯·소유 검증·구매/장착 저장 | UI 구매/장착·재로드 확인 |
| 개발자 화면 | 입력·분류·기질·후보·이유·최종 행동, 날짜/성장/여행 가속, 분류 수정 검증 | UI 사용 확인 |
| 금지 UI/기능 | 메인에 EXP/성장률/D-day/기질 수치 없음. 수치 디버그는 Editor/Development 전용 | 화면·분기 확인 |
| 빌드 씬 | 00_Boot→01_Main이 EditorBuildSettings에 활성 등록 | 확인 |
| Android 내보내기 | MCP build_3f69f18c1af9, Succeeded, 약244초 | Gradle 프로젝트 확인. APK 아님 |
| Android APK | MCP build_6635460321b5, Succeeded, 오류0 | 실제 APK·DEX·ARM64 런타임 구조 확인 |

## 최신 전체 UI 검증 — 2026-09-09

별도 `Temp/Acceptance-9c53ca5e7de14dfba710e32bb565ff57.json`을 사용했다. 기존 사용자 프로토타입 저장을 덮어쓰지 않았다.

1. 새 로컬 서비스 첫 만남, 기록1개.
2. 실제 개발자 버튼으로 Adapting→Interests→OwnWay→Preparing→Adult. 각 성장 확인 버튼 사용.
3. 성체28활동일·공예가·근거3개·외형 표시.
4. 실제 회고/이름 버튼으로 새봄 명명. 메인에 이름을 가진 정령 표시 후 ReadyToLeave.
5. 독립→알3개→두 번째 정령. 이전 새봄 Independent 유지.
6. 선물 구매(540→525), 여행 시작/개발자 가속/귀환, 작은 마을 엽서 수령, 장식0 구매/장착. 잔액455.
7. 같은 파일을 새로운 서비스로 읽어 전체 SaveData JSON이 이전과 동일함 확인. 정령2명, 날짜 기록30개, 잔액455.
8. Unity Console Error/Exception/Assert 0개. Play 정지.

## 검증 한계와 후속 품질 작업

- 실제 기기 실행/입력/백그라운드 복귀. adb 조회에 연결 기기 없음.
- 대표 화면 전환 중 동작 중단/복귀는 확인했다. 가능한 모든 화면/이벤트 조합을 전수 검사한 것은 아니다.
- 진행 활동 전용 소품은 기본 행동 소품을 사용하는 현재 표현보다 보강 여지가 있다.
- 정면/측면/후면과 성체3종의 실제 렌더 참고 시트를 Documentation에 저장했다. 별도 외주 원화가 아닌 코드 기반 프로토타입 아트다.
- GDD의 사용자 조사 비율은 제품 경험 평가 목표다. 사용성 조사나 수치 달성을 검증한 것은 아니다.
- 이전 저장 복구 테스트에서 파일 교체 IOException이 1회 발생했고 재실행은 통과했다. 재현 원인은 미확정이며 해결 완료로 표시하지 않는다.

## Android 내보내기 결과

`build_3f69f18c1af9`는 Succeeded. 기존 `exportAsGoogleAndroidProject=true` 설정 때문에 `Builds/PrototypeAcceptance/TodoSpirits.apk`는 파일이 아닌 Gradle 프로젝트 디렉터리다. 경로 확장자를 근거로 APK 생성 완료라고 주장하지 않는다. 기존 내보내기 설정은 유지했다.

보고 크기 1,110,434,983 bytes, 경고971개, 오류1개. 오류는 `Failed to handle /api/exec request: Main thread operation timed out after 60000ms`로 빌드 중 MCP 조회 지연에 해당한다. 경고는 주로 기존 com.unity.ai.inference의 Sentis 셰이더 성능/미지원 변형 메시지이며, RuntimePipelineManager가 없어 플레이어의 Pipeline 기능이 비활성이라는 안내도 있다. 게임 컴파일 실패로 보고된 것은 아니다. 불필요한 런타임 패키지 포함 여부와 실제 APK 크기는 추가 확인 대상이다.

빌드 후 ProjectSettings의 preloadedAssets에 기존 InputSystem 액션 에셋이 등록되는 변경1건을 확인했다. 그 외 폰트/렌더링 파일은 줄바꿈 경고가 있었으나 해당 diff 통계에서 내용 변경은 없었다.

## 후속 확인

- 2026-09-09 Play에서 기존 독립 정령 잎새 상세를 열어 초상화·기질·기간·공예가 진로·편지 슬롯·날짜 목록을 402×872 화면으로 확인했다.
- 설치용 APK 검증을 위해 exportAsGoogleAndroidProject를 임시 false로 설정하고 `build_6635460321b5`를 시작했다. 출력 예정 경로 `Builds/PrototypeAcceptance/TodoSpirits-device.apk`. 빌드 종료 후 원래 true로 복원해야 한다.
- 해당 빌드 완료: Succeeded, 오류0, 경고971. 실제 파일 크기 **86,675,915 bytes(약82.7MiB)**. 보고서 totalSize는 디버그/빌드 부속 데이터를 포함하므로 APK 파일 크기로 사용하지 않는다.
- ZIP 검사에서 AndroidManifest.xml, classes.dex 및 추가 DEX, lib/arm64-v8a/libil2cpp.so, libunity.so 확인. `exportAsGoogleAndroidProject=true`로 복원 확인. adb devices 결과 연결 기기 없음.

## 최종 확인 — 2026-09-09

- 최신 EditMode **58/58 통과**, 실패0. 이후 런타임 코드 변경 없음.
- `Documentation/AdultReference.png`: 기록가/탐방가/공예가의 세 외형을 실제 렌더로 비교 확인.
- `Documentation/DirectionReference.png`: 기본 정령의 정면/측면/후면 형태를 렌더해 제작 기준에 연결.
- 이동 직후 오늘의 기록 열기→메인 복귀: 숨김 중 coroutine 없음, 복귀 후 coroutine 재시작, actor 활성 모두 true.
- Android 라이브러리 빌드 프로필의 내보내기 설정을 메모리뿐 아니라 에셋 파일에도 저장하여 원본과 diff가 없음을 확인.
- APK 패키지 `com.DefaultCompany.TodoSpirits`, ARM64, 실행 Activity 포함. 개발용 기본 식별자이며 스토어 출시 설정은 별도다.
- APK SHA256: `F85081D9AF61BC6330608CDEF89A3D3C5F8E3E8DCC81CA5F3F5CE7384DBE512A`.
- Unity Play 정지, 컴파일 중 아님. git diff --check 오류 없음. 사용자 기존 TODO/Obsidian 변경 보존.
