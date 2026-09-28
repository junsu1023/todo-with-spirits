# Forest Daily P1 후보 2종 설치·검토 기록

검토일: 2026-09-18. 이 문서는 `spirit_read_01`과 `station_desk` 두 후보의 Unity 인계 결과다. 아트 승인이나 P1 10종 완료를 뜻하지 않는다.

## 대상과 보존 범위

- ZIP: `C:\Users\wigon\Downloads\ForestDaily_P1_Unity6_Handoff_v2.zip`
- 검증한 해제본: `C:\Users\wigon\Downloads\ForestDaily_P1_Unity6_Handoff_v2`
- Unity 프로젝트: `C:\Develop\todo-with-spirits\unity\TodoSpirits` (`6000.3.16f1`, `com.unity.ugui` 2.0.0)
- 작업 시작 시 브랜치 `develop`, HEAD `3d9ecc8a40c47faddf9bca297f59be40841550f9`. 다수의 기존 미커밋 파일이 있었으며 reset/stash/clean/commit하지 않았다. 기존 씬, 세이브, 브리지, 게임 코드와 Build Settings를 이번 설치로 수정하지 않았다.
- 프로젝트 `AGENTS.md`를 확인했다. 지정된 `../../Wiki/01 LLM Context.md`는 이 체크아웃에 없어 읽거나 갱신할 수 없었다.

## 실행 결과

| 항목 | 결과 | 근거 |
| --- | --- | --- |
| ZIP·해제본 | PASS | ZIP 39개 엔트리의 CRC와 경로 안전성 확인, 해제본 39개 파일과 바이트 일치. 정상 해제본을 재사용했다. |
| 패키지 검증 | PASS | `Tools/verify_package.py`: manifest 38개 파일, 11 GUID, 두 PNG의 SHA·512×512 RGBA·외곽 알파 검사 통과. |
| 설치 dry-run | PASS | 처음 `COPY_NEW` 16개, 충돌 0. `--apply`로 신규 16개만 설치했다. 재실행 dry-run에서 신규 0개, 동일 파일 14개, 설정이 바뀐 기존 Sprite `.meta` 2개 보존을 확인했다. |
| Unity 컴파일·Console | PASS | 열린 Unity 6.3에서 도구 메뉴가 로드되어 실행됐다. 격리 씬 Play Mode 중 Console 오류 0. 에디터가 자동화 모드가 아니라는 기존 경고 1개가 표시됐다. |
| Sprite 임포트 | PASS | `TodoSpirits/Forest Daily P1/1 - Configure Two Sprites`, `5 - Validate Imports` 실행. `Logs/ForestDailyP1/import_20260918_093321_919.json`의 `textureImportStatus=PASS`, 오류 없음. 변경 전 `.meta` 2개는 `ArtHandoffBackups/ForestDailyP1/a8921e872f164861b144adcfba64e6e9/`에 백업했다. GUID 유지. |
| 검토 프리팹 | PASS | 메뉴 2 실행. `Assets/ForestDailyP1Art/GeneratedReview/Prefabs/`에 UI·World 단일/조합 프리팹 6개와 `.meta` 생성. 프리팹 YAML의 두 Sprite GUID 참조 확인. |
| 격리 배치 | PASS | 메뉴 4의 QA Canvas를 `Assets/ForestDailyP1Art/GeneratedReview/SC_P1_ReadingReview.unity`로 **해당 씬만** 저장하고 QA 전용 Camera를 추가했다. 기존 00_Boot를 에디터에서 언로드한 뒤 이 씬만 Play Mode로 확인했다. 얼굴·책·손을 책상이 가리지 않고, 3개 크기에서 정령/책상 비율이 유지됐다. |
| 360×640 | PASS | Play Mode 캡처 `Validation/Concept01ArtKit/game-view-360x640-20260918-183929.png`를 직접 확인했다. |
| 1080×2340 | PASS | Play Mode 캡처 `Validation/Concept01ArtKit/game-view-1080x2340-20260918-183958.png`를 직접 확인했다. |
| 1366×768 | PASS | Play Mode 캡처 `Validation/Concept01ArtKit/game-view-1366x768-20260918-184031.png`를 직접 확인했다. |
| Atlas | NOT RUN | 기존 ArtKit Atlas에 두 새 Sprite가 들어 있지 않다. 두 장 모두 개별 Sprite로 정상 표시되어 선택적인 중복 검토 Atlas는 생성·패킹하지 않았다. |
| Android 기기 | NOT RUN | AndroidPlayer 모듈과 SDK는 있으나 번들 `adb devices -l`에 연결된 기기가 없다. APK 빌드·기기 실행은 수행하지 않았다. |
| Web 런타임 | NOT RUN | WebGLSupport 모듈은 있으나 QA 씬은 Build Settings에 넣지 않았고, 저장소 `web/`은 Vite/React 앱으로 새 Unity Sprite와 연결돼 있지 않다. WebGL 빌드·브라우저 실행은 수행하지 않았다. |
| 아트 승인·애니메이션 | NOT RUN | 정령과 책상은 후보이며 최종 승인 기록이 없다. `spirit_read_02`가 없어 두 프레임 애니메이션은 검증할 수 없다. |

## 시각 검토와 다음 연결 위치

검토 캡처에서 독서 정령·책상은 읽히지만, QA 배경이 2:3 비율로 표시되어 360×640과 1080×2340에는 상하 여백, 1366×768에는 좌우 여백이 생긴다. 이 씬의 여백과 검정/짙은 녹색 영역은 QA 표시 결과이며 제품 Safe Area 정책의 검증 결과가 아니다. 밝고 평면적인 분홍 정령 및 책상 명암이 회화풍 숲 배경과 어울리는지도 아트 담당자가 승인해야 한다.

현재 제품 메인의 정령은 `Assets/TodoSpirits/Scripts/Presentation/Forest/ForestAppView.Habitat.cs`의 `Actor()`에서 `spiritPoses[0]`으로 동적 생성되고 `ForestSpiritAnimator`가 포즈를 제어한다. 이번 두 후보는 해당 슬롯이나 프로덕션 프리팹에 연결하지 않았다. 읽기 활동의 별도 표시 정책과 두 번째 프레임이 정해진 뒤 국소적으로 연결해야 한다.

P1 10종 중 이번 패키지에 포함된 것은 `spirit_read_01.png`, `station_desk.png` 두 개다. 미제작 8종은 `spirit_read_02`, `spirit_move_01`, `spirit_move_02`, `spirit_make_01`, `spirit_make_02`, `spirit_tea_01`, `station_workbench`, `station_tea_table`이다. 복제 파일로 완료 처리하지 않았다.

패키지의 References·Previews·Tools·Documentation은 다운로드 해제본에만 두었고 Unity `Assets`에 넣지 않았다.
