# 컨셉 시안 1 Art Kit 검증 경과 보고

검증일: 2026-09-18 · Unity 6000.3.16f1 · Android 대상

## 확인한 결과

| 항목 | 결과 | 근거와 범위 |
| --- | --- | --- |
| Art Kit 리소스 | 통과 | Editor 검증 결과 오류 0개, Sprite 197개. `Validation/Concept01ArtKit/resource-validation.json`. |
| EditMode 테스트 | 76/76 통과 | Unity Test Runner의 Core 36개, Runtime 40개. 실패·무시 0개. |
| Play 진입 및 입력 | 통과 | `00_Boot`에서 `01_Main`으로 진입. 360×640 Game View에서 설정, 앨범, 오늘 요약, 돌보기, 선물 선택, 꾸미기, 숲 나들이를 열고 닫았다. 정수 0일 때 선물·꾸미기·나들이 구매 버튼이 비활성화된 상태를 확인했다. |
| 설정 저장 | 통과 | OS 임시 폴더의 격리 QA 저장을 사용했다. `동작 줄이기`를 켜고 Play를 종료·재시작한 뒤 켜진 상태가 유지됨을 확인했다. QA 저장 경로는 종료 후 해제했다. |
| 화면 크기 | 확인 | 360×640, 1080×2340, 1366×768 Game View 캡처를 확인했다. 캡처는 `Validation/Concept01ArtKit/game-view-*.png`에 있다. |
| Android APK 빌드 | 성공 | 별도 경로에 ARM64 릴리스 설정으로 빌드. `Validation/Concept01ArtKit/android-qa-build-report.json`: 오류 0, 경고 971, 소요 126.97초. APK는 `artkit-qa-20260918.apk`이며 크기 69,267,725바이트. 기존 `Builds/Forest` 산출물은 덮어쓰지 않았다. |
| APK 형식·서명 | 통과 | ZIP 엔트리 753개, `AndroidManifest.xml`, Unity Data 304개, `lib/arm64-v8a` 파일 7개를 확인했다. `aapt dump badging`은 패키지 `com.DefaultCompany.TodoSpirits`, versionCode 1, targetSdk 36을 표시했다. `apksigner verify`에서 v2 서명이 검증됐다. |

APK SHA-256: `84939B515C61C2D21F314A53B9E45D81D2F471BA6FC5C019FEBFACA34D253ACA`

## 아직 확인하지 못한 항목과 발견 사항

- `adb devices -l`에 연결된 기기가 없어 실기기 실행, 터치·노치, GPU 메모리, 성능, 앱 재설치 후 저장 복원은 확인하지 못했다. APK 생성과 서명 검증만으로 이 항목들이 통과했다고 볼 수 없다.
- 빌드 경고 971개의 전체 내용을 분류하지 않았다. Console에서는 `Packages/com.unity.ai.inference`의 셰이더 경고를 확인했다. 경고 수는 기존 릴리스 보고서의 971개와 같지만, 같은 경고의 집합인지 비교하지는 않았다.
- APK의 Android 패키지 ID가 `com.DefaultCompany.TodoSpirits`로 설정돼 있다. 배포용 식별자와 서명 정책은 출시 전에 결정·검증해야 한다.
- 숲 화면의 정령은 기존 분홍 Sprite를 사용한다. 컨셉 이미지의 새싹 정령과 시각적으로 일치하지 않는다. 배경·소품과 UI 흐름은 표시됐다.
- 실제 Android TODO 발신·응답 및 계정 연결은 현재 구현 경계 밖이다. `web` 프로젝트에는 Unity/WebGL 호스트 참조가 없고 의존성도 설치돼 있지 않아 브라우저 통합 실행은 확인하지 못했다.
- 오늘 기록 생성, 정수 획득, 구매 성공, 나들이 완료까지의 장기 게임 흐름은 이번 수동 Play 점검 범위에 포함하지 않았다. EditMode 테스트가 해당 도메인 규칙 일부를 검증하지만 실기기 전체 흐름의 대체는 아니다.

## 재현

Unity Editor의 Test Runner에서 EditMode 전체를 실행한다. `TodoSpirits > Art Kit > 2 - Validate Imported Resources`로 리소스를 검사한다. Play 입력 검증에는 `4 - Use Isolated QA Save`를 먼저 선택하고 `00_Boot`에서 Play한 뒤 `5 - Clear Isolated QA Save`를 실행한다. Android QA 빌드는 `TodoSpirits > Build Forest Android ArtKit QA APK`를 사용한다. 이 명령은 `Validation/Concept01ArtKit`에 별도 APK와 JSON 보고서를 작성한다.
