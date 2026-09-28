# Android TODO 앱 ↔ 정령의 하루 연결

작성: 2026-09-18. 구현 연결 설명이다. 이 문서의 소스 존재는 Android 실기기 실행 통과를 뜻하지 않는다. 최종 검증 결과는 [프로토타입 수락표](ForestPrototypeAcceptance.md)와 `Validation/ForestPrototype/`의 실행 산출물에 기록한다.

## 진입과 저장 보호

Android의 `Screen.Forest.route`는 `ForestScreen`을 거쳐 `ForestUnityActivity`를 전체 화면으로 연다. 기존 Unity 프로젝트의 GameActivity 진입점을 유지하며, 시스템 뒤로 또는 Unity의 `requestForestClose` 호출은 `unload()` → `onUnityPlayerUnloaded()` → `finish()`로 TODO 앱에 돌아온다. Unity를 새 launcher 아이콘으로 등록하지 않는다.

`ForestScreen`은 `rememberLauncherForActivityResult(StartActivityForResult())`로 숲 Activity의 종료 결과를 받아 기존 `onReturnToToday` 내비게이션을 실행한다. 종료 후 진입 중간 화면에 머무르지 않고 실제 오늘 일정 화면으로 돌아간다. 자동 진입의 `rememberSaveable` 플래그는 유지하므로 복귀/재구성 때 자동 재진입하지 않는다. 저장소 안에 같은 launcher 예제는 없었으며 기존 `activity-compose` 의존성과 [Android 공식 API](https://developer.android.com/reference/kotlin/androidx/activity/compose/package-summary)로 사용 가능 여부를 확인했다. 종료·회전·프로세스 복구의 기기 검증은 NOT RUN이다.

숲 Activity가 열리면 불투명한 준비 화면을 올린다. 기존 `GetUserProfileUseCase`로 인증된 `userId`를 확인하고 Unity가 해당 계정의 저장을 선택했다는 `session-ready` 응답을 보내기 전에는 숲 화면을 노출하지 않는다. 계정 확인에 실패하면 재시도/돌아가기를 제공한다. 이미 이번 Activity에서 확인한 동일 계정의 일정 재조회 실패는 오류/오프라인으로 전달한다.

**현재 한계:** 앱/숲 Activity를 새로 연 상태에서 프로필 조회까지 오프라인이면 저장 계정을 판별할 기존 영속 `userId`가 없어 숲 진입을 보류한다. Android의 기존 인증 저장소는 토큰/로그인 방식만 보관한다. 토큰 지문을 새로 만들어 계정 캐시를 복구하는 변경은 자동 승인 검토에서 인증 경계 변경으로 거절되어 적용하지 않았다. 기존 인증 흐름은 유지한다. 이는 v4의 모든 오프라인 최초 진입을 충족했다는 의미가 아니다.

Unity `PrototypeBootstrap.OpenHostSession(accountId,date,timeZone)`는 원래 저장 옆 `accounts/SHA256(accountId).json`을 사용한다. Editor에서는 계정 저장 역시 격리 QA 저장 폴더 아래에 생성한다. 계정 이름이나 토큰을 파일명/브리지 JSON으로 전달하지 않는다. 프로토타입 예시 저장은 별도 파일을 사용한다.

## 메시지 순서

1. Unity `ForestTodoBridge.Start` → native `onForestUnityReady()`.
2. Android 인증 프로필 확인 → `UnitySendMessage("AppRoot", "ReceiveHostSession", {accountId,date,timeZone,requestId})`. `requestId`는 연결 시도마다 새 UUID이며 인증 토큰이 아니다.
3. Unity 계정 저장 선택 결과 → native `onForestSnapshotResult`의 JSON 응답: `{kind:"host-session",status:"session-ready" 또는 "session-rejected",accountId,date,requestId,message}`. 성공/실패 모두 받은 요청의 계정·날짜·요청 번호를 그대로 돌려준다. Android는 세 값이 현재 대기 중인 요청과 일치하고 현재 선택 계정·날짜도 같을 때만 처리한다. 지연된 이전 계정/날짜/재시도 응답과 예전 단순 문자열 응답은 무시한다. 일치하는 성공만 준비 화면을 닫고, 일치하는 실패는 준비 화면을 유지한다.
4. Android `GetTaskCalendarUseCase(오늘, 오늘)`의 **전체** 일정/루틴을 검증하고 `ReceiveTodoSnapshot`으로 전달한다. 목록 조회 중/실패는 `ReceiveTodoSyncState`의 `loading`/`offline`/`error`로 전달한다. 두 종류 모두 전송마다 새 UUID `requestId`를 붙인다.
5. Unity는 `{kind:"todo-snapshot" 또는 "todo-sync",status,accountId,date,requestId,message}` JSON으로 응답한다. snapshot 상태는 `applied`/`duplicate`/`rejected`, sync 상태는 `status-loading`/`status-offline`/`status-error`/`rejected`다. Android는 현재 세션의 계정·날짜와 일치하고 **가장 최근 일반 요청**의 종류·요청 번호까지 같은 응답만 처리한다. 수락한 요청 번호는 소비한다. 일치하는 거부는 재시도를 기다린다.

일반 snapshot의 `applied`/`duplicate` 응답도 현재 계정·날짜의 세션 승인이 끝나기 전에는 준비 화면을 닫지 않는다. snapshot과 sync는 하나의 최신 요청 슬롯을 공유하므로 이전 snapshot 거부뿐 아니라 이전 로딩 상태 거부도 새 요청의 커튼/세션을 바꾸지 않는다. 세션 응답 수락 시에도 `requestId`를 소비하므로 같은 응답을 다시 받아도 두 번 처리하지 않는다. 다른 계정·날짜의 snapshot 거절은 현재 Unity 연결 상태를 오류로 바꾸지 않는다. 이 변경은 로그인·토큰 저장·계정 캐시 정책을 변경하지 않는다.

호환성: `schemaVersion`과 snapshot의 기존 필드는 그대로이며 `requestId`는 선택 필드다. 요청 번호 없는 기존 로컬/개발 송신에는 종전 문자열 결과를 돌려주지만, 현재 Android 호스트는 식별자 없는 문자열로 준비 화면이나 활성 세션을 변경하지 않는다. 따라서 새 Android와 새 Unity 라이브러리를 함께 배포한다. 과거 정정의 `historical-*` 문자열은 기존대로 유지하며 세션/커튼을 변경하지 않는다.

지연 응답 검증 항목(코드 반영 완료, Android 기기 실행 검증은 아직 NOT RUN): A 요청 후 B로 전환해 A 성공/실패 도착, 같은 계정의 날짜 변경 후 전날 응답 도착, 같은 계정·날짜 재시도 후 이전 요청 응답 도착, 로딩 요청 이후 새 snapshot 전송 뒤 이전 로딩 거부 도착, 수락한 응답의 중복 도착. 지연 응답은 커튼과 현재 요청을 유지하고, 중복 응답은 이미 승인한 상태를 유지해야 한다.

2026-09-18 **순수 JVM 단위 테스트 PASS**: `ForestHostReplyPolicyTest` 5/5와 `ForestHostSnapshotTest` 5/5, 총 10/10. 실제 프로젝트의 두 production 소스와 `domain/TaskCalendar.kt`, 기존 테스트를 로컬 캐시의 Kotlin 2.2.21·JUnit 4.13.2·JDK 21로 직접 컴파일/실행했다. 가짜 DTO나 새 설치는 사용하지 않았다. 증거: `Validation/ForestPrototype/android-pure-jvm-tests.json` 및 해당 `android-pure-jvm-*.log`. 프로젝트 지정 Kotlin은 2.2.10이며 이번 독립 실행은 Android Gradle 플러그인·SDK·Activity·Unity JNI·기기 통합 통과를 뜻하지 않는다. Android Gradle/기기 검증은 NOT RUN이다.

`TaskRefreshBus`, Activity 재개, 숲에서의 `requestForestRefresh()`와 날짜 변경이 새 전체 조회를 시작한다. 새 요청은 이전 coroutine을 취소하고 적용 직전 취소 여부를 확인한다. 날짜 변경 확인 주기는 Activity가 열린 동안 30초이며, 재개 시 즉시 재조회한다.

앨범의 과거 정정은 Unity가 `requestForestHistory({accountId,dates})`로 **이미 보관한 날짜**를 요청한다. Android는 동일 계정·현재보다 이전 날짜·90일 미만 범위를 검증하고 기존 캘린더 API로 조회한 뒤 날짜별 전체 목록을 `ReceiveHistoricalTodoSnapshot`으로 보낸다. 요청 날짜에 실제 항목이 없으면 빈 목록 정정이 된다. 같은 날짜 목록의 60초 내 반복 요청은 억제해 앨범 갱신이 조회 반복을 만들지 않게 한다. 실패 시 보관 중인 기록을 유지하고 안내한다. 당시 본 이야기와 이미 지급한 보상의 보존은 Unity Runtime에서 처리한다.

## 입력 보존 규칙

- occurrence ID: `schedule|routine:taskId:YYYY-MM-DD`; 영어 소문자 변환은 `Locale.ROOT`를 사용한다.
- `ROUTINE`만 `isHabit=true`, 원본 완료 상태와 제목은 그대로 보낸다. 완료 항목만 추리거나 제목을 잘라내지 않는다.
- 서버/Unity 공통 9개 category, 원래 날짜, 중복 ID, 50건 한도, 제목 1~255자 및 제어문자를 검사한다. 비호환 항목이 있으면 목록 전체를 오류로 처리해 이전 숲 기록을 보존한다.
- 정상적인 0건은 유효한 snapshot이며, 네트워크 오류는 0건으로 바꾸지 않는다.
- `revision`은 현재 계정별 Android SharedPreferences에 `max(previous+1, systemTimeMillis)`를 **동기 저장**한 뒤 전송한다. 기기 시계가 뒤로 가도 순서가 증가한다. 이는 현재 기기/호스트의 전송 순서이며 서버 전역 revision 또는 다중 기기 동기화를 구현했다는 의미가 아니다.
- native 상태 메시지에 마지막 조회 성공 시각을 포함하며, Unity의 마지막 실제 반영 시각/보상은 Unity 저장에서 판정한다. 팝업 표시나 native 성공 문구로 보상을 지급하지 않는다.

## 빌드 경로

Unity 메뉴 `TodoSpirits/Build Forest Android Library`는 프로덕션 `00_Boot`, `01_Main`만 `Exports/AndroidLibrary`로 내보낸다. 기존 export/bundle 플래그를 작업 후 복원하고 결과를 `Validation/ForestPrototype/android-library-build.json`에 기록한다. 검토 전용 P1 씬은 넣지 않는다.

Android `settings.gradle.kts`는 `../unity/TodoSpirits/Exports/AndroidLibrary/unityLibrary`를 참조한다. 라이브러리가 없으면 필요한 export 경로를 명시하고 중단한다. export의 Unity 전용 Gradle 속성을 모듈에 전달하며 Android 앱 자신의 API·인증 설정은 유지한다. 호스트가 GameActivity를 상속하므로 Unity export의 GameActivity 설정과 일치해야 한다.

후속 A01 런타임/앨범 변경을 포함한 export도 `android-library-a01-build-response.json`에서 13.412초, 오류0으로 성공했다. 현재 위 연결 경로는 A01 코드이며 이전 결과물 전체는 `Exports/AndroidLibrary-BeforeA01`에 보존했다. 최초 `android-library-build.json`은 이전 실행 기록으로 유지한다. SDK 제약으로 실제 Android 호스트 컴파일·실행 상태는 여전히 NOT RUN이다.

2026-09-18 Unity 라이브러리 export는 성공했다. 최초 호스트 `help --offline`은 캐시에 `foojay-resolver:1.0.0` JAR가 없어 실패했으나, 격리 캐시에 공개 플러그인을 받은 뒤 Kotlin DSL 경로 오류를 고치고 공식 `android.newDsl=false` 호환 모드를 적용해 **온라인 `help` 및 `:unityLibrary` 설정 PASS**를 확인했다. 앱 ABI는 export의 `unity.abiFilters`와 일치시켰다. 원본 export와 인증/서명 설정은 보존했다. SDK 자동 설치는 계속 차단했으며 완전한 호스트 빌드는 NOT RUN이다. 실제 실행 근거와 한계는 [Gradle 연결 점검](ForestAndroidGradleCompatibility.md)에 기록했다.

이후 실제 `:app:testDebugUnitTest --tests '*ForestHost*Test' :app:assembleDebug`를 한 번 요청했으나 `android-36.1` 누락으로 task 의존성 계산에서 중단됐다(`android-host-build-attempt.json/.log`). 앱 소스 컴파일과 Android 테스트/assemble task는 실행되지 않았다. 이 환경 오류를 순수 JVM 10/10 테스트 결과나 Unity 단독 APK 결과와 혼동하지 않는다.

빌드 후 최소 확인 명령(실행 전 산출물 존재와 Java/SDK 설정을 확인한다):

```powershell
# C:\Develop\todo-with-spirits\android
.\gradlew.bat :app:testDebugUnitTest --tests '*ForestHostSnapshotTest' --tests '*ForestHostReplyPolicyTest' :app:assembleDebug
```

실제 기기에서 첫 진입/뒤로/재진입, 계정 A/B, 0·1·50건, 긴 제목, 오프라인/복구, 자정, 회전, 완료 취소·재전송을 검증한다. 2026-09-18 착수 시 `adb devices -l`에는 기기가 없었다. `assembleDebug` 성공으로 이 항목들을 PASS라고 표시하지 않는다.

## 관련 파일

- Android `app/src/main/java/com/example/todowithspirits/feature/forest/`: `ForestScreen.kt`, `ForestUnityActivity.kt`, `ForestHostSnapshot.kt`, `ForestHostReplyPolicy.kt`.
- Android `app/src/test/java/com/example/todowithspirits/feature/forest/ForestHostSnapshotTest.kt`: 원본 유지, 빈 목록, 경계 개수, 잘못된 날짜/중복/카테고리, revision 역행 검증.
- Android `app/src/test/java/com/example/todowithspirits/feature/forest/ForestHostReplyPolicyTest.kt`: 계정·날짜·재시도·요청 종류·중복 소비·알 수 없는 응답의 수락 경계 검증. 순수 JVM 5/5 PASS이며 Android Gradle 실행은 NOT RUN이다.
- Unity `Assets/TodoSpirits/Scripts/Runtime/ForestTodoBridge.cs`, `Bootstrap/PrototypeBootstrap.cs`.
- Unity `Assets/TodoSpirits/Editor/ForestAndroidLibraryBuild.cs`.

Unity as a Library의 전체 화면/모듈 연결 기준은 [Unity 공식 문서](https://docs.unity3d.com/ja/6000.0/Manual/UnityasaLibrary-Android.html)를 참고했고, Unity 6.3의 정확한 Activity 클래스와 API는 설치된 `AndroidPlayer/Source` 및 `classes.jar`에서 확인했다.
