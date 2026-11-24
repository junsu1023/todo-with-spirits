# 알려진 버그 및 보류 항목

- 작성: 2026-11-22, `df81705` + 커밋 전 `EmailVerificationStep` onEvent 전환 기준
- 확인 방법: **코드 읽기 기준이며 기기·서버에서 재현하지 않았다.** (단위 테스트 실패인 BUG-14만 실제로 재현됨)
- 경로: 저장소 루트 기준. `app/…`은 `android/app/src/main/java/com/example/todowithspirits/…`, `back/…`은 `back/src/main/java/com/oow/todowithspirit/…`
- 줄 번호는 바뀌므로 함수명으로 찾는다.
- [CLAUDE.md](../.claude/CLAUDE.md)의 현재 수정 범위는 UI다. 아래는 범위를 벗어나므로 **착수 전에 사용자 승인을 받는다.**
  - `core`: BUG-10
  - Web·Server: BUG-09
  - 기존 동작·검증 로직 변경: BUG-04 ~ BUG-08
- 고치면 요약 표의 상태를 바꾸고, 전부 끝나면 이 문서는 지운다.

## 요약

심각도: **높음** 가입이 막히거나 잘못된 데이터 저장 / **중간** 특정 상황에서 오동작 / **낮음** 사용성·테스트

| ID | 심각도 | 영역 | 내용 | 먼저 정할 것 | 상태 |
| --- | --- | --- | --- | --- | --- |
| BUG-01 | 높음 | Android, Web | `0`으로 시작하는 인증번호는 인증 불가 | - | 완료 |
| BUG-02 | 높음 | Android | 비밀번호 검증이 서버 규칙과 달라 인증 후에야 가입 거부 | - | 수정 안 함 |
| BUG-03 | 높음 | Android | 닉네임 단계에서 뒤로가면 진행 불가 | - | 완료 |
| BUG-04 | 높음 | Android | 인증 후 가입 요청이 실패하면 확인 재시도 불가 | - | 대기 |
| BUG-05 | 높음 | Android | 가입 후 자동 로그인이 실패하면 인증 화면에 갇힘 | - | 대기 |
| BUG-06 | 중간 | Android | 인증 화면에서 재발송의 필드 오류가 안 보임 | - | 대기 |
| BUG-07 | 중간 | Android | 화면에 없는 필드 오류가 오면 `다음` 버튼이 계속 비활성 | - | 대기 |
| BUG-08 | 중간 | Android | 인증 요청 중 시간 초과·뒤로가기와 응답이 경합 | - | 대기 |
| BUG-09 | 중간 | Server | 메일 발송에 실패해도 성공 응답 | 서버 담당 협의 | 대기 |
| BUG-10 | 중간 | Android | 요청이 겹치면 로딩과 터치 차단이 일찍 풀림 | `core` 수정 | 대기 |
| BUG-11 | 중간 | Android | 알림 설정 화면에 로딩·터치 차단 없음 | - | 대기 |
| BUG-12 | 낮음 | Android | 로그인 실패 팝업이 떠도 키보드와 입력이 남음 | 기기 확인 | 대기 |
| BUG-13 | 낮음 | Android | 홈 상세 팝업만 작게 보임 | 카드 폭 Figma 확인 | 대기 |
| BUG-14 | 낮음 | Test | `HomeViewModelEventTest` 1개 실패 | 올바른 이동 목적지 | 대기 |
| BUG-15 | 낮음 | Android | 인증 중 Activity가 재생성되면 소셜 로그인 취소 | 현재는 유지 권장 | 보류 |

추천 순서: **01 → 02 → (03·04·05·06·08 한 번에) → 07 → 10·11 → 나머지**

---

## 회원가입

03~08의 대상은 주로 `app/feature/signup/viewmodel/SignUpViewModel.kt`이다.

### BUG-01. `0`으로 시작하는 인증번호는 인증 불가

- **현상**: 정확히 입력해도 실패한다. 발급되는 코드의 약 10%가 해당한다.
- **원인**
  - 서버 `back/service/EmailVerificationService.java`의 `generateCode()`는 `%06d` 문자열로 발급한다.
  - 앱 `verifyEmailCode()`는 `toIntOrNull()`로 바꿔 보내서 `012345` → `12345`가 된다.
  - 서버 `back/dto/user/EmailVerifyRequest.java`는 `\d{6}`만 받는다.
- **수정**: `code`를 `Int` → `String`으로 바꾸고, 앱에서는 숫자 6자리인지만 검사한다.
  - `android/data`: `VerifyEmailCodeRequest`, `AuthRemoteDataSource`, `AuthRepositoryImpl`
  - `android/domain`: `AuthRepository`, `VerifyEmailCodeUseCase`
  - `android/app`: `SignUpViewModel`, 테스트용 `FakeAuthRepository`
  - Web도 동일: `web/src/feature/auth/ui/VerificationStep.tsx`의 `parseInt`, `web/src/feature/auth/api/mutate.ts`의 `code: number`
- **완료 조건**: `012345`와 `000000`이 문자열 그대로 전송된다.
- **상태**: 완료 (2026-11-24). Android는 `012345` 전송 테스트로 확인했고, Web은 빌드·기기 확인 전이다.

### BUG-02. 비밀번호 검증이 서버 규칙과 다름

- **현상**: 앱 검증을 통과한 비밀번호가 이메일 인증까지 끝낸 뒤 가입 요청에서 거부되어 첫 단계로 돌아온다. 다시 진행하려면 인증 메일도 새로 받아야 한다.
- **원인**: `f18bc14`에서 `app/util/SignUpValidation.kt`의 조건이 `length < 8`(거부) → `length > 20`(거부)으로 바뀌면서 최소 길이가 사라졌다. 문자 조합 규칙은 그 전부터 서버와 달랐다.

| 규칙 | Android | Server (`back/dto/auth/SignupRequest.java`) |
| --- | --- | --- |
| 길이 | 1~20 | **8~20** |
| 조합 | 글자·숫자·특수문자 중 2가지 | **영문과 숫자 모두 필수** |
| 글자 | 한글 등 모든 문자 | 영문만 |
| 특수문자 | 모두 | `!@#$%^&*()_+=-`만 |

- **앱은 통과, 서버는 거부되는 예**: `a1`, `abc1234`, `abcdefg!`, `1234567!`, `password1.`, `비밀번호1234`
- **수정**: 서버 정규식과 동일하게 검증한다. `password_format_hint`("영문/숫자/특수문자 중 2가지 포함 20자 내")와 `validateCredentials()`의 오류 문구도 고친다. Web 안내는 "영문 + 숫자 조합 8~20자"이다.
- **완료 조건**: 위 예시가 첫 단계에서 거부되고 `password1`, `password1!`은 통과한다.
- **결정 (2026-11-24)**: 클라이언트는 수정하지 않는다. 형식이 틀리면 서버가 에러 문구를 내려주므로 그 문구를 보여준다. 단, 서버 검증은 이메일 인증 뒤 가입 요청 때 일어난다.

### BUG-03. 닉네임 단계에서 뒤로가면 진행 불가

- **재현**: 이메일 인증 → 닉네임 화면 → 시스템 뒤로가기.
- **현상**: 인증 화면으로 돌아가고 타이머는 멈춰 있다. 확인과 재발송 모두 실패한다.
- **원인**: 이 시점에는 가입과 자동 로그인이 끝났다. 서버는 가입 시 `consumeVerification()`으로 코드를 삭제하고, 재발송은 이미 가입된 이메일이라 `DUPLICATE_EMAIL`로 거부한다.
- **수정**: `goBack()`의 `NICKNAME` 분기에서 홈으로 보낸다(추천). 또는 뒤로가기를 막는다. `goBackToEmailVerification()`은 삭제한다.
- **완료 조건**: 닉네임 단계에서 인증 단계로 돌아가는 경로가 없다.
- **상태**: 완료 (2026-11-24). 닉네임 단계에서 뒤로가면 로그인 화면(이전 화면)으로 나간다. 뒤로가기 차단은 강제 종료로 우회되므로 택하지 않았다.

### BUG-04. 인증 후 가입 요청이 실패하면 확인 재시도 불가

- **재현**: 인증 성공 직후 가입 요청이 네트워크 등 필드 오류가 아닌 이유로 실패 → 확인을 다시 누름.
- **원인**: `verifyEmailCode()`가 항상 인증부터 다시 요청한다. 서버는 이미 쓴 코드라 `ALREADY_COMPLETED`로 거부한다. 타이머도 이미 멈춰 있다.
- **수정**: 인증 완료 여부를 상태로 보관하고, 재시도 때는 `performSignUp()`만 실행한다. 이메일 변경·재발송 시 초기화한다.
- **완료 조건**: 가입 1회 실패 후 재시도하면 인증 API를 다시 부르지 않고 성공한다.

### BUG-05. 가입 후 자동 로그인이 실패하면 인증 화면에 갇힘

- **현상**: "다시 로그인해주세요" 토스트만 뜨고 인증 화면에 남는다. 코드는 소비되었고 재발송도 거부된다.
- **원인**: `performSignUp()`의 `loginUseCase` 실패 분기가 메시지만 보내고 이동하지 않는다.
- **수정**: 로그인 화면으로 보내는 Effect를 추가한다.
- **완료 조건**: 자동 로그인 실패 시 로그인 화면으로 이동한다.

### BUG-06. 재발송의 필드 오류가 안 보임

- **현상**: 재발송이 `email` 필드 오류로 실패하면 아무 반응이 없다.
- **원인**: `sendVerificationEmail()`은 필드 오류를 `fieldErrors`에만 넣는데, 인증 화면은 `verificationCode` 키만 보여준다.
- **수정**: 인증 단계에서의 발송 실패는 `emitErrorMsg()`로도 알린다.
- **완료 조건**: 재발송 실패 시 항상 메시지가 보인다.

### BUG-07. `다음` 버튼이 계속 비활성

- **현상**: 서버가 첫 화면에 없는 키(`nickname`, `code` 등)로 오류를 주면 오류는 안 보이고 버튼도 켜지지 않는다.
- **원인**: `canProceedCredentials`가 `fieldErrors.isEmpty()`를 요구하는데, 입력 시에는 자기 키만 지운다.
- **수정**: 이 단계의 키(`email`, `password`, `confirmPassword`)만 활성 조건에 쓰고, 나머지 오류는 토스트로 알린다.
- **완료 조건**: 알 수 없는 키가 와도 메시지가 보이고 다시 시도할 수 있다.

### BUG-08. 인증 요청과 시간 초과·뒤로가기의 경합

- **재현**: 만료 직전에 확인을 누름, 또는 확인 후 응답 전에 시스템 뒤로가기(`LoadingOverlay`는 이를 막지 않음).
- **현상**: 첫 단계로 돌아갔다가 응답이 오면 닉네임 단계로 튄다.
- **원인**: 타이머는 응답을 받은 뒤에 취소되고, 뒤로가기는 진행 중인 요청을 취소하지 않는다.
- **수정**: 인증·가입 요청 중에는 타이머 만료 처리와 뒤로가기를 보류한다. 가입 요청은 취소해도 서버에서 처리될 수 있으므로 취소로 해결하지 않는다.
- **완료 조건**: 요청 중에는 단계가 바뀌지 않는다.

### BUG-09. 메일 발송에 실패해도 성공 응답 (Server)

- **현상**: 메일이 안 갔는데 앱은 인증 화면으로 넘어가거나 재발송 성공 토스트를 띄운다.
- **원인**: `back/service/EmailVerificationService.java`의 `sendMail()`이 `MailException`을 로그만 남기고 삼킨다.
- **수정**: 발송 실패를 오류 응답으로 돌려준다.
- **완료 조건**: SMTP 실패 시 API가 실패를 반환한다.

---

## 공통·기타

### BUG-10. 로딩과 터치 차단이 일찍 풀림

- **원인**: `android/core`의 `BaseViewModel.launchWithLoading`이 Boolean 하나를 쓴다. 요청 A, B가 겹치면 먼저 끝난 쪽이 `false`로 만든다.
- **수정**: 진행 중인 요청 수를 세고 0일 때만 해제한다.
- **완료 조건**: 겹친 요청이 모두 끝날 때까지 `isLoading`이 `true`이다.

### BUG-11. 알림 설정 화면에 로딩·터치 차단 없음

- **원인**: `app/feature/setting/AlarmSettingScreen.kt`에 `LoadingOverlay`가 없다. ViewModel은 `launchWithLoading`을 6곳에서 쓴다.
- **수정**: `isLoading`을 수집해 `LoadingOverlay`를 최상단에 둔다.
- **완료 조건**: 요청 중 다른 설정을 누를 수 없다.

### BUG-12. 실패 팝업이 떠도 키보드와 입력이 남음

- **현상**: 확인 버튼이 키보드에 가릴 수 있고 뒤 입력란에 계속 입력된다.
- **원인**: `app/component/DimPopup.kt`는 별도 window가 아니라서 포커스가 그대로다.
- **수정**: `DimPopup`이 나타날 때 `LocalFocusManager.current.clearFocus()`를 호출한다.
- **완료 조건**: 팝업이 뜨면 키보드가 내려간다.

### BUG-13. 홈 상세 팝업만 작게 보임

- **원인**: `app/feature/home/component/TodoDetailPopup.kt`의 `Dialog`가 `LocalDensity`를 기기 기본값으로 되돌려 테마의 360dp 기준 스케일이 빠진다.
- **수정**: `Dialog` 내용을 `SpiritTodoTheme`으로 감싼다. 현재 카드 폭 274dp와 테두리는 Figma에서 다시 확인한다.
- **완료 조건**: 같은 dp 값이 본 화면과 팝업에서 같은 크기로 보인다.

### BUG-14. `HomeViewModelEventTest` 실패

- **재현**: `./gradlew :app:testDebugUnitTest`
- **실패**: `spiritMenuAddClickedClosesMenuAndSendsNavigateToCalendar`가 `NavigateToCalendar`를 기대하지만 실제는 `NavigateToAdd`.
- **원인**: `e4c27ff`에서 목적지가 바뀌었고 테스트는 그대로다.
- **수정**: 일정 추가로 가는 것이 맞으면 테스트 이름과 기대값을 고친다.
- **완료 조건**: 전체 테스트가 통과한다.

### BUG-15. 인증 중 Activity 재생성 시 소셜 로그인 취소

- **재현**: 인증 중 다크 모드·글자 크기 변경, 폴드 접기·펼치기.
- **원인**: SDK 호출이 `LoginRoute`의 `rememberCoroutineScope`에서 돌아 재생성 때 취소된다. 회전은 세로 고정으로 해결됨.
- **판단**: 로딩이 풀려 다시 누를 수 있으므로 유지를 권장한다. ViewModel에 Activity `Context`를 넘기는 방식으로 고치지 않는다.

---

## 버그는 아니지만 잊으면 안 되는 것

| 항목 | 내용 | 시점 |
| --- | --- | --- |
| 비밀번호 찾기 | 화면이 없어 눌러도 반응 없음. `EmailLoginRoute`에 `navigateToFindPassword`만 넘기면 연결됨 | 화면이 생길 때 |
| 회원가입 구조 마무리 | 세 단계 모두 onEvent 전환 완료. `SignUpRoute`/`SignUpScreen` 분리, `SignUpUiState`의 `state/` 이동이 남음 | BUG-03과 함께 |
| 세로 고정 예외 | 600dp 이상 화면용 `PROPERTY_COMPAT_ALLOW_RESTRICTED_RESIZABILITY`는 targetSdk 37부터 무효 | targetSdk 37로 올릴 때 |

## 확인 필요 (버그인지 미확정)

- **기본 닉네임**: `SignUpUiState.nickname`이 `"행복한돼지123"`으로 고정이다. 수정하지 않고 확인을 누르면 서버가 만든 `정령유저_####`를 덮어써 모두 같은 닉네임이 된다. 임시 값인지 확인한다.
- **인증번호 시도 제한**: `/api/user/email/verify`는 비로그인 허용인데 `back/`에서 시도 횟수·요청 빈도 제한을 찾지 못했다. 인프라에서 막고 있는지 서버 담당과 확인한다.
