# 숲 UI / 정령 동행 클라이언트 구현 기록

> 2026-09-18 Art Kit 통합 이후의 화면 구성과 검증 결과는 [Concept01ArtKitIntegration.md](Concept01ArtKitIntegration.md)에 기록했다. 아래 내용은 이전 구현의 기록이다.

확인일: 2026-09-17 · Unity 6000.3.16f1

이 문서는 실제 구현과 연결 방법을 설명한다. 장기 기획 문서는 기존 `/unity/Docs/` 로컬 전용 규칙을 유지한다. 저장소 지침이 가리키는 `Wiki/01 LLM Context.md`는 현재 체크아웃에서 발견되지 않았다.

## 실행과 씬 배선

- `Assets/TodoSpirits/Scenes/00_Boot.unity`에서 Play. 부트스트랩이 `01_Main`을 연다.
- `01_Main/Canvas/PF_UI_ForestApp` 프리팹이 폰트 2개, 배경 Sprite, 정령 Sprite 4개를 직렬화 참조한다.
- 기존 `MainSceneRoot`는 비활성 상태로 남아 있다. 기존 프리팹 참조와 레거시 성장 규칙을 삭제하지 않았다.
- `TodoSpirits > Install Forest UI` 메뉴는 에셋 임포트·프리팹·씬 배선을 재생성한다. 씬에 미저장 변경이 있으면 중단한다.
- 한국어는 저장소의 NotoSansKR 폰트를 사용한다. 화면은 세로 모드, 안전 영역 및 세로 스크롤을 지원한다.

## 구현한 플레이 흐름

1. 오늘 탭에서 할 일 또는 매일 습관을 추가하고 카테고리를 선택한다.
2. 완료·취소는 즉시 저장된다. 오늘의 정령 반응, 분류, 정수와 기록이 갱신된다. 일일 정수는 기존 최대 지급량 방식으로 중복 지급을 막는다.
3. 미정도 완료와 활동일을 인정한다. 미정의 제목에서 휴식·건강 같은 생활 유형을 추론하지 않는다.
4. 함께한 활동 1/4/7일에 세 번의 선택이 열린다. 첫 선택은 수첩/가방, 다음 선택은 관계/표식, 마지막 선택은 기록가/탐방가/공예가 특기를 남긴다. 앞 선택이 후속 질문과 재회 편지에 반영된다.
5. 실제 완료가 있던 하루가 날짜 전환 시 성장에 반영된다. 빈 날·미접속 기간에는 가짜 활동을 만들지 않는다. 현재 튜닝은 14활동일 + 세 번의 선택 뒤 성체다. 최종 밸런스가 아니다.
6. 성체 공개 → 회고 → 이름 → 마지막 동행 → 배웅 → 다음 알로 이어진다. 완료가 없는 당일은 새 정령이 바로 동행하고, 같은 날짜의 완료 기록은 두 정령에게 나누지 않는다.
7. 이전 정령은 앨범에 남는다. 다음 정령과 활동한 이틀 뒤 첫 재회 편지가 열린다. 이전 특기와 선택을 반영한 편지·선물을 한 번 지급하고 다시 읽을 수 있게 저장한다.
8. 돌보기에는 인사·기쁨·휴식이 있다. 꾸미기 5종은 구매와 장착을 저장하고 숲에 표시한다. 나들이는 준비물 3종과 실제 UTC 4시간 대기를 사용한다.

정령은 4포즈 Sprite를 사용해 부유, 눈 깜박임, 기쁨, 호기심, 인사, 수면을 재생한다. 산책의 좌우 이동, 만들기의 기울임, 읽기의 자세를 생활 행동에 연결했다. `동작 줄이기`는 위치·회전·크기 애니메이션을 멈춘다.

## 저장과 호환성

- 실제 로컬 저장: `Application.persistentDataPath/TodoSpirits/forest-save.json`.
- 기존 `companion-save.json`은 덮어쓰거나 자동 이관하지 않는다. 이전 파일에는 샘플 할 일 기록이 들어 있을 수 있기 때문이다.
- JSON 임시 파일/원자적 교체/백업 복구를 재사용한다. 실패한 변경은 메모리의 날짜·기록·보상까지 롤백한다.
- Editor 검증만 `SessionState`의 `TodoSpirits.ForestVerificationSave`로 임시 저장 경로를 지정할 수 있다. 플레이어 빌드에는 이 경로 기능이 포함되지 않는다.
- 날짜 가속은 Editor 또는 Development Build의 설정에서만 표시한다.
- 월별 집계는 실제 저장된 카테고리와 완료 날짜를 기준으로 한다. 선택 내용도 날짜로 필터링한다. 선택은 이미 읽은 이야기로 보존되며, 이후 완료 취소로 소급 삭제하지 않는다.

## Android TODO 연결 계약

수신부는 `00_Boot/AppRoot`의 `ForestTodoBridge.ReceiveTodoSnapshot(string)`이다. Android 발신부/Unity as a Library 임베딩은 아직 Android 프로젝트에 구현되어 있지 않다. 이번 APK는 Unity 단독 실행용이다.

호스트에서 Unity 초기화 후 다음 형태로 **현재 날짜의 전체 목록**을 보낸다. 네트워크 자격 증명은 필요 없으며 전송하지 않는다.

```csharp
UnityPlayer.UnitySendMessage("AppRoot", "ReceiveTodoSnapshot", json);
```

```json
{
  "schemaVersion": 1,
  "accountId": "opaque-account-id",
  "revision": 1,
  "date": "2026-09-17",
  "items": [
    { "id": "routine-123", "title": "저녁 산책", "category": "HEALTH", "isHabit": true, "isCompleted": true }
  ]
}
```

- `revision`은 계정 전체에서 단조 증가해야 한다. 같은 값/이전 값은 중복으로 처리한다. 다음 날에도 증가시킨다.
- 카테고리: `NONE`, `WORK_STUDY`, `HEALTH`, `LIFE`, `RELATIONSHIP`, `GROWTH`, `HOBBY`, `REST`, `FINANCE`. 실제 Android `CategoryOption`을 확인했다. `FINANCE`는 Unity의 Assets로 매핑한다.
- 제목 1~80자, 전체 0~50개, 중복 없는 ID, 정확한 현재 날짜를 요구한다. 잘못된 입력은 일부만 반영하지 않고 거부한다.
- 다른 계정 전환 및 기존 로컬 일기 위에 호스트 기록 덮어쓰기를 거부한다. 계정별 저장 파일 분리/로그아웃 정책은 네이티브 통합 시 필요하다.
- 연결 뒤 Unity의 로컬 수정은 잠긴다. 네이티브 TODO 앱이 수정의 원본이다.
- 결과는 `LastResult` (`applied`, `duplicate`, `rejected: ...`)로 읽을 수 있고 C# `SnapshotApplied` 이벤트가 UI를 갱신한다. 네이티브 응답 콜백은 아직 연결하지 않았다.
- 중간에 놓친 날짜의 서버 기록을 소급 동기화하는 기능은 이번 수신 계약에 포함하지 않는다. 현재 날짜 전체 스냅샷만 받는다.

## 변경 영역과 이유

| 영역 | 변경 목적 |
| --- | --- |
| Core/ForestStory, CompanionLife, CompanionLifeRules | 레거시 규칙을 유지하며 새 선택·성장·독립 규칙을 명시적으로 활성화 |
| Runtime/ForestJournal, ApplicationService.Forest, ForestTodoBridge | 실제 로컬 TODO, 일일 재계산, 선택·재회 저장, 네이티브 수신 경계 |
| Runtime/Bootstrap, Persistence/PrototypeSaveData, ApplicationService/LifeCycle | 샘플 입력 제거, 새 저장 분리, 실제 날짜와 새 정령 연결 |
| Presentation/Forest | 숲 아트 기반 오늘·숲·앨범 UI와 정령 동작 |
| Art/Forest, PF_UI_ForestApp, 00_Boot, 01_Main | 생성 아트 임포트, 명시적 에셋 참조, Editor를 통한 실제 씬 배선 |
| Editor/ForestSceneInstaller, ForestEditorPreview, ForestAndroidBuild | 재현 가능한 설치·화면 검증·개발용 APK 빌드 |
| Tests/RuntimeEditMode/ForestSessionTests | 중복 보상, 미정, 날짜, 실패 롤백, 전체 생애, 재회, 호스트 재전송·계정 혼합 검증 |

## 검증과 남은 범위

- Unity Test Runner: TodoSpirits EditMode 테스트 **72개 통과**. 새 어셈블리의 컴파일 오류 없음 확인 후 실행했다. 결과는 `Validation/forest-editmode-tests.json`에 보존했다.
- 실제 Editor: 입력 필드와 버튼으로 습관 추가·완료, 이야기 선택, 회고·이름·배웅·알 선택·재회 수령을 확인했다.
- 별도 QA JSON 저장으로 재시작 복구를 확인했다. 기존 사용자 저장은 테스트에 사용하지 않았다.
- 빠른 날짜 가속을 연속 실행하는 중 한 차례 파일 교체 오류가 났다. 변경이 롤백되어 같은 상태에서 재시도 후 끝까지 진행했다. 실제 기기의 저장 장치 특성은 별도 검증이 필요하다.
- Android ARM64 / IL2CPP 개발용 APK 빌드 성공, 오류 0개. 결과는 `Builds/Forest/android-build-report.json`에 기록된다. 첫 전체 빌드는 기존 AI 추론 셰이더 경고 970개와 플레이어에서 Pipeline이 비활성이라는 안내 1개가 있었다. 후속 증분 빌드는 Pipeline 안내 1개만 남았다. 추론 패키지를 임의 제거하지 않았다.
- 360×640, 540×1000의 실제 Game View 캡처를 확인했다. 설정·앨범 버튼 중심 좌표의 EventSystem Raycast가 해당 버튼을 가리키는 것을 확인했다. `AppRoot.SendMessage`에 JSON을 전달해 수신 → 저장 → UI 갱신도 실행했다.
- 실기기 터치/키보드/접근성/메모리, 네이티브 앱의 발신 및 수신 응답, 로그인·클라우드 동기화, 오프라인 과거 날짜 병합, 계정 변경 정책은 미검증/미구현 범위다.
- 돌보기 시트에서 선물 6종과 오늘의 선물 반응에 접근할 수 있다. 유료화·푸시·스토어 배포는 수행하지 않았다.
- 성체는 크기와 소품·특기·서사로 구분한다. 별도 성체 원화나 스켈레탈 리깅이 완성된 상태는 아니다. 재회는 첫 1회를 구현했으며 장기 반복 이벤트 확장은 콘텐츠 작업이다.

## Editor 재검증 순서

1. `00_Boot`에서 실행하고 `오늘` 탭에서 습관 1개를 추가·완료한다.
2. `숲`의 첫 선택을 고르고 `설정 > 개발 도구 > 검증용 · 다음 날로 이동`을 누른다.
3. 습관을 매일 한 번 완료하고 4/7활동일의 선택을 진행한다. 14일 완료 뒤 다음 날로 넘긴다.
4. 성체 회고·이름·마지막 동행·독립을 진행한 뒤 새 알을 고른다.
5. 새 정령과 이틀의 활동을 쌓고 이전 정령의 편지를 받는다. 앱을 재시작해 중복 지급이 없는지 확인한다.
6. 앨범의 이전 월을 골라 카테고리 집계·선택 날짜를 확인한다.
7. `TodoSpirits > Build Forest Android Development APK` 메뉴로 단독 테스트 APK를 만든다.

최신 UI 개편·비개발 빌드·화면 검증은 [ForestReleaseUi.md](ForestReleaseUi.md)를 기준으로 확인한다.
