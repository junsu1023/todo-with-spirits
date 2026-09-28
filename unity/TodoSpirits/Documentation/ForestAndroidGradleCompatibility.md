# Android 호스트 Gradle 연결 점검

확인: 2026-09-18. Unity Editor 조작·SDK 설치·라이선스 수락 없이 수행했다.

## 실제 실행 결과

| 항목 | 결과 | 근거 |
| --- | --- | --- |
| Unity Android Library export | PASS, 오류 0·경고 971·49.66초 | `Validation/ForestPrototype/android-library-build.json`; 다른 담당자가 실행한 Unity 결과 |
| 호스트 Gradle 설정 점검 `help --offline` | FAIL | `foojay-resolver:1.0.0` JAR 캐시 부재로 settings classpath 해석에서 중단 |
| 온라인 `help` 초기 실행 | FAIL | 필요한 플러그인을 받은 뒤 `:app` Kotlin DSL의 `projectDir` 참조 오류 확인·수정 |
| 온라인 `help` 재실행·AGP 9.1.1 Unity 모듈 DSL 평가 | PASS | `:app`, `:core`, `:data`, `:unityLibrary` 설정 완료 및 `:help` 성공; 약 1분 |
| 실제 호스트 테스트·APK 빌드 요청 1회 | FAIL, 실행 전 환경 오류 | `:app:testDebugUnitTest --tests '*ForestHost*Test' :app:assembleDebug` 요청이 `android-36.1` 누락으로 의존성 계산 중 중단; 약 32초 |
| 호스트 소스 컴파일·Android Gradle 테스트·APK 산출·기기 실행 | NOT RUN | 위 시도에 `> Task` 실행 기록이 0개. SDK 36.1 라이선스/설치 대기 |
| 숲 순수 JVM 단위 테스트 | PASS, 10/10 | 별도 `android-pure-jvm-tests.json`; Android 설정·JNI·기기를 검증한 결과가 아님 |

호스트 설정 검증은 wrapper가 지정한 것과 같은 기존 Gradle 9.3.1 배포본의 `gradle.bat`로 `help --offline --no-daemon --console=plain --no-configuration-cache --no-watch-fs`를 실행했다. 별도 `GRADLE_USER_HOME` 및 project cache를 `Temp/ForestHostGradleQa` 안에 두고, 기존 의존 캐시에서 lock/gc 파일을 제외해 복사한 읽기 전용 캐시를 사용했다. 개인 Gradle 설정/init 스크립트를 로드하지 않았다. JDK 21과 기존 Unity SDK 경로는 프로세스 환경변수로만 지정했다.

SDK 자동 설치는 `-Pandroid.builder.sdkDownload=false`, JDK 자동 설치는 `-Porg.gradle.java.installations.auto-download=false` 및 대응 시스템 속성으로 차단했다. 새 다운로드는 수행하지 않았다. 실행 스크립트는 `Temp/ForestHostGradleQa/run-offline-help.ps1`, 실제 명령 인수·시각·종료 코드는 `Validation/ForestPrototype/android-host-offline-help.json`, 원문 출력은 같은 이름의 `.log`에 있다.

이후 공개 Maven/Gradle 플러그인의 다운로드를 허용한 온라인 `help`를 같은 격리 캐시로 실행했다. SDK/JDK 자동 설치 차단과 개인 설정 제외는 유지했다. 초기 실패는 `android-host-online-help-initial.json/.log`, 수정 후 성공은 `android-host-online-help.json/.log`에 보존했다. 실행 스크립트는 `Temp/ForestHostGradleQa/run-online-help.ps1`이다. SDK 라이선스 수락·설치는 하지 않았다.

같은 격리 환경에서 실제 테스트/빌드를 한 번 요청한 결과는 `Validation/ForestPrototype/android-host-build-attempt.json/.log`에 있다. 최종 오류는 `Could not determine the dependencies of task ':app:testDebugUnitTest'` → `Failed to find target with hash string 'android-36.1'`다. 자동 설치를 켜거나 라이선스를 수락하지 않고 중단했다. AGP가 기존 SDK의 일부 `package.xml`을 갱신하려던 동작은 읽기 전용 SDK로 인해 거부됐으며, SDK 파일을 쓰도록 권한을 확대하지 않았다. 실제 앱 컴파일 오류가 있는지는 아직 판단할 수 없다.

## 생성된 export와 호스트에서 확인한 사실

- Unity export는 `Exports/AndroidLibrary/unityLibrary`와 `shared` 스크립트를 사용한다. `libs/unity-classes.jar`가 존재하고 호스트의 `compileOnly` 경로와 일치한다.
- Unity 생성본은 AGP 8.10.0·compileSdk 36·NDK 27.2.12479018·CMake 3.22.1·Java 17·`arm64-v8a`만 지정한다. 호스트는 AGP 9.1.1·compileSdk 36.1이며 생성본의 루트 Gradle 대신 호스트 루트 아래 모듈로 연결한다.
- 호스트 `settings.gradle.kts`는 Unity 모듈에 export의 `unity*` 속성을 전달한다. 상대 경로 `../shared/*.gradle`는 실제 export 구조와 일치한다.
- 호스트 Manifest는 Unity의 기본 launcher Activity를 제거하고 내부 `ForestUnityActivity`를 등록한다. 실제 Manifest merge/패키징 실행은 하지 않았다.
- 원본 Unity export는 수정하지 않았다. 호스트 설정 2개를 아래와 같이 수정했다.

## 적용한 호환 수정과 검증 범위

1. **Kotlin DSL 실제 오류 수정:** `app/build.gradle.kts`의 `dependencies` 안에서 `project(":unityLibrary")`는 프로젝트 의존성이므로 `projectDir`를 참조할 수 없었다. `rootProject.project(":unityLibrary").projectDir`로 수정했고 다음 실행에서 해당 스크립트가 컴파일됐다.
2. **AGP 9 공식 호환 모드:** `android/gradle.properties`에 `android.newDsl=false`를 추가했다. Unity 생성본의 `android.libraryVariants`와 라이브러리의 기존 defaultConfig 필드를 사용하기 위한 공식 opt-out이며, 원본 export를 변환하지 않아 재export에도 유지된다. 이 설정으로 실제 `:unityLibrary`의 debug/release variant 설정이 성공했다. 호스트 Kotlin·인증·서명 설정을 별도로 변경하지 않았다. AGP 10에서는 제거되는 모드이므로 Unity 템플릿의 새 API 전환 또는 AGP 10 업그레이드 전에 재검토한다. [AGP 9 변경 문서](https://developer.android.com/build/releases/agp-9-0-0-release-notes)
3. **ABI 일치:** `app/build.gradle.kts`가 export `gradle.properties`의 `unity.abiFilters`를 읽어 앱의 `defaultConfig.ndk.abiFilters`에도 동일하게 적용한다. 현재는 `arm64-v8a`이며 값이 없으면 설정 단계에서 중단한다. APK 내부 JNI 파일과 실제 기기 실행은 아직 확인하지 않았다.

## 다음 검증 시 적용 원칙

SDK 36.1 승인·설치가 완료되면 호스트 단위 테스트와 debug 앱 빌드를 수행한다. 현재 SDK 36.1 누락을 우회하려고 호스트 compileSdk를 낮추지 않았다. 설정 PASS 이후에도 실제 Kotlin 앱 소스·Compose launcher·Hilt/KSP 생성·Manifest merge·IL2CPP/CMake·JNI 패키징·기기 진입/복귀를 추가 검증해야 한다. 특히 `android.newDsl=false` 경고는 예상된 호환 모드 경고이며 AGP 10 대응 작업을 없애지 않는다.

읽기 전용 캐시 구성 및 offline의 범위는 [Gradle 공식 문서](https://docs.gradle.org/current/userguide/dependency_caching.html)를 참고했다. 최초 offline 실패는 필요한 로컬 캐시가 없었던 결과였으며, 서버·인증 오류나 Unity export 실패가 아니다.
