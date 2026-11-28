# 숲 UI 출시 완성도 개선 — 2026-09-17

## 이번 변경의 목적과 근거

이전 화면은 큰 일러스트 아래에 설명 카드와 행동 버튼을 쌓아, 돌보기·꾸미기·나들이가 목록형 설정처럼 보였다. 앨범은 요약만 보여 주어 실제 생활을 다시 읽기 어려웠다. 이번 변경은 세 화면의 목적을 분명히 나누고, 선택 결과가 물건·사진·편지로 보이도록 구현했다.

다음 공식 자료와 공개 스크린샷을 확인했다. 화면의 상호작용 원리를 참고했으며 해당 앱의 그림·로고·UI 파일을 복제하거나 프로젝트에 다운로드하지 않았다.

| 레퍼런스 | 확인한 특징 | 이번 적용 |
| --- | --- | --- |
| [Finch 홈 안내](https://help.finchcare.com/hc/en-us/articles/37780000231309-Exploring-the-Finch-Home-Page), [공식 소개](https://finchcare.com/about-finch) | 목표에서 바로 행동하고, 돌아온 동료와 이야기하며, 소품을 고르는 구조 | 오늘의 약속에 바로 체크, 작은 정령 메모, 별도 교감·선물 시트 |
| [Forest](https://www.forestapp.cc/) | 활동이 눈에 보이는 누적 기록으로 남음 | 실제 완료 날짜 달력, 이달의 생활색, 날짜별 원래 제목·분류·습관 확인 |
| [Usagi Shima 공식 프레스킷](https://usagishima.net/presskit/) | 같은 그림체의 생활 공간, 소품, 직접 교감 | 화면 전체 숲, 아래 고정 행동, 물건 중심 보관함, 정령과 같은 공간의 선물·나들이 |

Forest의 실패 처벌은 도입하지 않았다. 쉬는 날의 손실은 없고, 미정은 그대로 미정으로 남는다.

## 실제 변경 파일

- `ForestAppView.cs`: 숲 탭의 이중 헤더 제거, 오늘의 고정 추가 버튼, 탭별 스크롤, 나들이 귀환 시점 자동 갱신, 새 그림 직렬화 필드.
- `ForestAppView.Habitat.cs` (신규): 전체 높이 생활 공간, 크기가 조정되는 정령, 소품·재회 선물, 고정 행동 세 개, 발자국 안내, 선택 대화·결과, 독립의 단계별 화면.
- `ForestAppView.Today.cs` (신규): 주간 날짜 기록, 노트형 일정 행, 명시적인 추천 추가, 완료 반응, 접을 수 있는 완료 목록, 독립 상태 안내, 자정 직후 체크 보호.
- `ForestAppView.Album.cs`: 월별 사진 표지, 정령·월 선택, 생활색, 날짜별 기록 읽기, 선택·독립·편지 타임라인. 과거 기록은 해당 월에 알려진 정보로 표시한다.
- `ForestAppView.Dialogs.cs`: 스크롤 본문과 고정 하단 버튼, 세션 중 입력 초안 보존, 펼치는 카테고리, 교감 반응, 개별 선물 6종, 구매와 배치를 분리한 보관함, 실제 알 3종, 편지지, 나들이 준비·시간·귀환 엽서.
- `ForestUiMotion.cs`: 버튼 중심을 유지하는 눌림 효과. 동작 줄이기 설정을 존중한다.
- `ForestSceneInstaller.cs`, `PF_UI_ForestApp.prefab`, `00_Boot.unity`, `01_Main.unity`: Editor API로 임포트·슬라이스·참조 배선·저장. 씬 YAML을 손으로 수정하지 않았다.
- `PrototypeApplicationService.LifeCycle.cs`, `ForestSessionTests.cs`: 새 정령 선택으로 날짜가 이동할 때 그날 남은 로컬 일회성 약속만 이어 준다. 완료 기록·습관·서버 소유 날짜는 유지하며 저장 실패 시 원복한다.
- 신규 그림: `forest-background-storybook.png`, `forest-journey-props.png` (9개), `forest-gifts.png` (6개), `forest-lunch.png`. 기존 그림은 보존했다. 정확한 생성 요청은 `ForestArtProvenanceV3.md`, `ForestGiftArtProvenance.md`에 있다.

## 검증한 사실

- Unity 6000.3.16f1의 실제 Game View에서 540×1000, 360×640으로 실행했다.
- 최신 소스 컴파일 성공 및 UI 검증 후 Console Error 0개를 확인했다.
- EditMode 테스트 76개 통과, 실패·건너뜀 0개. 추가 4개는 미완료 약속 이월·장기 미접속·서버 데이터 권한·저장 실패 원복을 검사한다.
- 별도 `Temp/forest-polish-v3-qa.json`을 지정한 상태에서만 날짜 가속과 테스트 일정을 만들었다. 사용자 정상 저장은 수정하지 않았다. QA 종료 후 SessionState의 임시 저장 선택을 해제했다.
- 빈 오늘 화면에서 추천을 누르기 전 일정이 생성되지 않음을 확인했다.
- 실제 일정 완료, 이야기 선택 저장, 개별 소품, 실제 날짜 기록의 제목·분류·습관 표시를 확인했다.
- 입력창 닫기→다시 열기에서 작성 중 제목 복원, 카테고리 펼치기, 저장을 확인했다. 이 초안은 앱 재시작까지 보존하는 영구 초안은 아니다.
- 검증 저장에서 14일 성장, 긴 이름(12글자), 나들이 중 독립 안내, 귀환 수령, 배웅, 다음 알, 두 번째 동행 이틀 뒤 재회·편지 수령을 확인했다.
- 재시작 후 정령 2명, 장식 2개, 재회 수령, 추가 일정이 유지됨을 확인했다.
- 선물 6종의 실제 알파 배경과 선택 그림을 확인했고, 담요 선물 후 15 정수가 차감되어 저장됐다.
- 나들이 준비 화면의 활성 Text를 검사했을 때 `preferredHeight > allocatedHeight + 3`인 항목은 0개였다.
- 캡처는 `Validation/ReleaseUiV3/`에 있다. `v3-album-history.png`는 통계 겹침 보정 후 재촬영했고, `v3-trip-memory-final.png`는 실제 숲·정령이 담긴 최종 엽서이다. `v3-trip-memory-small.png`는 보정 전 중간 QA 캡처이다.

## 최종 Android 빌드

`Builds/Forest/TodoSpirits-Forest-release.apk` 빌드 성공: `BuildOptions.None`, ARM64, 오류 0개, 130.68초. 실제 APK 파일 크기는 65,920,371바이트(약 62.9 MiB)이다. BuildReport의 `totalSize`는 실제 APK 길이와 달라 파일 길이를 별도로 확인했다.

`aapt`로 `com.DefaultCompany.TodoSpirits`, 버전 1.0/1, min SDK 25, target SDK 36, `arm64-v8a`, `application-debuggable` 미포함을 확인했다. SHA-256: `9C84E5FBA6C9413EB2F71F55A277197DFDB893F3695F7E623EA4D6790F50EB6E`.

빌드 경고 971개는 셰이더 경고 970개와 Player 빌드에서 Editor Pipeline이 꺼진다는 안내 1개이다. 이 경고를 오류 0개와 혼동하지 않는다. 상세 결과는 `Validation/forest-v3-android-build.json`과 `Validation/forest-v3-apk.json`에 있다.

## Editor에서 다시 확인하는 방법

1. `00_Boot`에서 실행한다. 프리팹을 다시 설치할 경우 씬 변경을 저장하고 Play를 종료한 뒤 `TodoSpirits > Install Forest UI`를 사용한다.
2. 오늘에서 일정 추가→완료→숲의 이야기 선택→앨범 날짜를 연다. 미정으로 만든 일정은 미정으로 남아야 한다.
3. 꾸미기에서 미리 보기→구매→배치의 두 단계를 확인한다. 선택만으로 정수가 차감되면 안 된다.
4. 나들이 준비물 3개·고정 출발 버튼·남은 시간·귀환 엽서를 확인한다. 시간 가속은 격리된 Editor 테스트 저장에서만 사용한다.
5. EditMode에서 `TodoSpirits` 테스트를 실행한다. Android 비개발 검증 APK는 `TodoSpirits > Build Forest Android Release APK`로 만든다.

## 남은 출시 검증과 범위

여기서 확인한 결과는 Unity 독립 실행 UI와 로컬 저장, 브리지 수신 코드까지이다. 기존 Android TODO 앱의 Unity as a Library 실제 삽입·송신 연결, 실기기의 뒤로 가기·IME 키보드·노치·절전/복귀·성능은 아직 통합 검증하지 않았다. Editor의 키보드 영역 대응 코드가 실제 기기 검증을 대신하지 않는다.

현재 Android 패키지 ID·버전·서명은 저장소의 기존 값이며, Google Play용 최종 패키지·릴리즈 서명·AAB 업로드는 이번 UI 작업에서 변경하거나 수행하지 않았다. APK 빌드 성공과 스토어 출시 완료를 구분한다.

정령의 성체는 현재 표정·크기·꿈의 소품으로 차이를 표현한다. 별도 성체 실루엣과 반복 재회 콘텐츠, 2주 이상 사용 시 재미/피로도는 후속 제작 및 사용자 테스트 대상이다. 이번 작업으로 리텐션이나 시장 성과가 검증된 것은 아니다.

요구된 `../../Wiki/01 LLM Context.md`는 실제 경로에 없었으므로, 확인 사실과 변경 근거는 이 프로젝트의 Documentation에 남겼다.
