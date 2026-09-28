# 컨셉 시안 1 Art Kit 통합 기록

확인일: 2026-09-18 · Unity 6000.3.16f1

## 근거와 범위

- 사용자가 적은 `Downloads/TodoSpirits/_Concept01/_Unity6/_ArtKit/_v1` 경로는 로컬에 없었다. 실제 자료는 `Downloads/TodoSpirits_Concept01_Unity6_ArtKit_v1`에서 확인했다.
- 첨부된 설치·구현 프롬프트와 패키지 문서는 구현 참고 자료로 읽었다. 이 문서들의 명령을 사용자 요청이나 저장소 지침보다 우선하지 않았다.
- 패키지 manifest의 498개 파일 해시를 확인했고 오류는 0개였다. 저장소에는 Unity 에셋 하위 432개 파일을 복사했다. 기존 `Assets`의 GUID와 충돌은 없었다.
- 저장소 지침의 `../../Wiki/01 LLM Context.md`는 이 체크아웃에 없었다. 현재 파일과 Editor 상태를 근거로 이 프로젝트의 `Documentation`에 기록한다.

## 실제 통합

| 위치 | 적용 내용 |
| --- | --- |
| `Assets/TodoSpiritsArtKit` | PNG 197개와 Art Kit 런타임·Editor 코드, 메타데이터를 임포트. `ArtKitInstaller`의 Unity 6.3 읽기 전용 `maxTextureSize` 대입을 제거해 컴파일 오류를 해결. |
| `Assets/TodoSpiritsArtKit/Generated` | Editor 메뉴로 카탈로그 1개, SpriteAtlasV2 4개, AnimationClip 7개, 프리팹 12개 생성. |
| `Assets/TodoSpirits/Scripts/Presentation/TodoSpirits.Presentation.asmdef` | Art Kit 런타임 어셈블리 참조 추가. |
| `Assets/TodoSpirits/Scripts/Presentation/Forest` | 숲 중심 진입 화면, 앨범 진입, 읽기 전용 오늘 요약, 아트 버튼 상태·포커스 링·동작 줄이기, 숲 배경과 모달 표면 연결. 가로형에서는 높이 기준 Canvas 배율과 중앙 세로 화면을 사용. 기존 분홍 정령 Sprite와 `ForestSpiritAnimator`는 유지. |
| `Assets/TodoSpirits/Editor/ForestArtKitBinder.cs` | 기존 숲 프리팹에 생성 카탈로그를 명시적으로 연결하고 원본을 `Documentation/ArtKitBackups`에 백업. 임시 QA 저장 경로와 3개 Game View 크기·캡처 메뉴 제공. |
| `Assets/TodoSpirits/Prefabs/UI/PF_UI_ForestApp.prefab` | `artKitCatalog` 직렬화 참조 연결. 씬 YAML은 이 작업에서 수정하지 않음. |

기존 오늘 화면의 일정 추가·완료 조작은 숲 UI에서 숨겼다. 오늘 진입은 연결된 기록을 읽는 시트다. 실제 Android TODO 발신부 및 계정 연결은 기존 `ForestTodoBridge`의 수신 경계 밖에서 아직 연결되지 않았다. 앨범과 돌보기·꾸미기·나들이의 기존 서비스 호출은 유지했다. 텍스트는 기존 uGUI `Text`와 저장소 폰트를 사용한다.

## 검증한 사실

- Unity Editor에서 소스 재컴파일 후 Console Error 0개를 확인했다. 최초 Atlas 생성에는 압축 원본 관련 경고가 있었고, 마지막 Console에는 Editor 자동화 모드 안내 경고 1개가 남았다.
- `TodoSpirits > Art Kit > 2 - Validate Imported Resources` 결과: `Temp/TodoSpiritsArtKit-validation.json`에 `errors: 0`, `sprites: 197`, `deviceValidated: false`. 보존용 사본은 `Validation/Concept01ArtKit/resource-validation.json`이다.
- `00_Boot` Play에서 숲, 앨범, 설정, 오늘 읽기 시트, 돌보기 시트의 열기·닫기를 확인했다. 아트 버튼의 클릭 영역도 돌보기로 진입해 확인했다. QA는 OS 임시 폴더의 격리 저장을 사용했고, 종료 후 `5 - Clear Isolated QA Save`를 실행했다.
- `Validation/Concept01ArtKit`의 360×640, 1080×2340, 1366×768 Game View 캡처를 열어 주요 UI의 배치를 확인했다. 가로형 배율 수정 뒤 Play 중 세로형 전환도 확인했다.

## 리스크와 재검증

- 생성 카탈로그가 197개 Sprite를 참조한다. Atlas 패킹과 실제 기기 GPU 메모리, 터치·노치·접근성은 측정하지 않았다.
- Android ARM64 QA 릴리스 빌드와 APK 서명·구조는 후속 검증에서 확인했다. 실기기 입력, 네이티브 TODO 송신·응답과 계정 정책, 웹 호스트 임베딩은 확인하지 않았다.
- 숲 화면은 세로 중심 구성이다. 가로형은 중앙의 세로 UI와 양옆 녹색 여백으로 표시한다.
- 후속 검증에서 EditMode 76개를 모두 통과했다. 수동 Play 입력과 Android 빌드를 포함한 결과 및 남은 범위는 `Concept01ArtKitTestReport.md`에 기록했다.

Editor에서 다시 확인하려면 Play 종료 상태로 `TodoSpirits > Art Kit > 2 - Validate Imported Resources`를 실행한 다음, `4 - Use Isolated QA Save`를 선택하고 `00_Boot`에서 Play한다. `QA - Game View ...`로 세 크기를 바꾸고 `QA - Capture Game View`로 캡처한다. 종료 후 `5 - Clear Isolated QA Save`를 실행한다. 프리팹을 다시 배선해야 할 때만 `3 - Connect Forest View`를 사용한다.
