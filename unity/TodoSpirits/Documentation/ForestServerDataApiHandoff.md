# 숲 연동용 서버 데이터·API 인수 문서

확인일: 2026-09-18 · 대상: `back` Spring 서버, `android` TODO 앱, Unity 숲 화면

> 첫 입장 팝업과 정령 반응의 **목표 데이터 흐름**은 [정령의 하루 상세 기획 v4](ForestDailyExperiencePlanV4.md)를 따른다. 아래는 현재 코드에서 확인한 API와 추가 협의 사항이다.

## 현재 데이터 경계

`back`에는 인증·프로필·일정/루틴·기록·기존 정령 API가 **이미 있다**. Android `TaskApi`도 일정/루틴 캘린더와 완료/취소를 호출한다. Unity `ForestTodoBridge`는 HTTP 요청을 하지 않는다. 이번 구현의 Android `ForestUnityActivity`가 기존 프로필·캘린더 use case를 호출한 뒤 `UnityPlayer.UnitySendMessage("AppRoot", "ReceiveTodoSnapshot", json)`으로 전달한다. 계정 세션의 요청 ID 확인, 상태·결과 콜백, 과거 정정도 연결했다. 연결 소스의 존재와 실제 기기 통과는 구분하며 [연결 설명](ForestAndroidHostIntegration.md)과 [검증 기록](ForestPrototypeAcceptance.md)을 함께 읽는다. 숲의 정령·정수·선물·장식·나들이·앨범은 현재 Unity 기기에만 저장된다.

따라서 아래 `기존`은 코드에서 확인한 API, `필수 협의`는 Android→Unity의 실제 연결에 필요한 데이터/정책, `선택`은 클라우드 동기화나 다중 기기 운영을 채택할 때 필요한 신규 API 제안이다. 이 문서의 제안 URL은 구현된 엔드포인트가 아니다.

## 기존 서버 API와 숲에서 쓸 수 있는 데이터

| 기존 API | 확인한 주요 반환/동작 | 숲 연동 판단 |
| --- | --- | --- |
| `GET /api/user/me` | 인증 사용자 `userId`, 닉네임, 대표 정령 ID 등. 응답은 `{result, detail}`로 감싼다. | 현재 호스트가 `userId.toString()`을 계정 식별자로 사용한다. 시간대 필드는 없다. |
| `GET /api/task/calendar?from=YYYY-MM-DD&to=YYYY-MM-DD` | 해당 기간의 일정·루틴 occurrence 통합 목록 및 건수. 항목: `taskId`, `taskType`, `occurrenceDate`, `title`, `category`, `isCompleted`, `completedAt`, `updatedAt` 등. 서버 검사는 `from`~`to`의 경과일 90일 이하이며 양끝 포함 최대 91개 날짜다. | **오늘 전체 목록의 기본 데이터 원천**. `from=to=오늘`, 필터 없이 조회해 Android에서 Unity JSON으로 변환한다. 완료 목록만 보내면 안 된다. 호스트의 과거 정정 요청은 이보다 좁게 경과일 90일 미만·최대 90개 날짜로 제한한다. |
| `POST /api/task/{taskId}/complete?date=…`, `DELETE /api/task/{taskId}/complete?date=…` | 해당 날짜 완료/완료 취소. 생성·수정·삭제·미루기 API도 `TaskController`에 있다. | Android에서 변경 성공 후 오늘 전체 목록을 재조회해 전송하는 흐름이 필요하다. |
| `GET /api/record/today`, `/weekly?date=…`, `/monthly?date=…` | TODO 집계, 성장 수치, 일·주·월 통계. `/today`는 서버 `LocalDate.now()`를 쓴다. | 참고 통계. Unity의 정수 지갑/숲 앨범과 같은 모델이 아니므로 바로 대체할 수 없다. |
| `GET /api/spirit`, `GET/PATCH /api/spirit/current` | 서버 정령 ID·이름·단계·경험치·대표 정령. | 현재 Unity `CompanionLife`/앨범과 별개 데이터 모델이다. 동일 정령으로 취급하거나 자동 병합하지 않는다. |

근거: `back/.../controller/{User,Task,Record,Spirit}Controller.java`, `back/.../dto/task/{CalendarTasksResponse,TaskOccurrenceResponse}.java`, `android/data/.../api/TaskApi.kt`. 실제 서버 실행·HTTP 응답은 이 문서 작성 과정에서 재시험하지 않았다.

## 즉시 필요한 데이터와 소유자

| 우선순위 | 데이터/정책 | 현재 공급원 | 협의·구현할 내용 |
| --- | --- | --- | --- |
| P0 | 사용자 식별자 | `GET /api/user/me`의 `userId` | Android가 문자열 `accountId`로 변환하고 Unity는 계정별 저장을 연다. 세션 응답의 계정·날짜·requestId가 모두 일치해야 준비 화면을 해제한다. 토큰은 Unity JSON에 넣지 않는다. 신규 오프라인 진입의 인증 계정 판별은 여전히 협의 항목이다. |
| P0 | 오늘 날짜·시간대 | Android `LocalDate.now()` / `ZoneId.systemDefault()` | Android가 세션의 날짜·시간대를 전달하고 Unity는 그 날짜를 따른다. 다른 날짜의 실시간 스냅샷은 거부한다. 서버 `/record/today`의 시간대와 일치하는지는 별도 합동 검증이 필요하다. |
| P0 | 오늘 **전체** 일정·루틴 occurrence | 기존 `/api/task/calendar` | Android가 필터 없이 하루 범위를 조회해 stable ID, 제목, 카테고리, `taskType→isHabit`, 완료 상태로 변환. 빈 목록과 요청 실패를 구분한다. |
| P0 | 스냅샷 순서 번호 `revision` | 기존 캘린더 응답에 없음 | 현 Android는 계정별 `max(previous+1, clockMillis)`를 영속 저장한 뒤 전송한다. 현재 기기의 응답 순서 보장이며 서버 전역 버전은 아니다. 다중 기기 지원 시 서버가 일관된 목록과 버전을 제공해야 한다. |
| P0 | 입력 호환성 | 제목 1~255자, 9개 카테고리 코드, 전체 0~50건 | Android·Unity가 검증한다. 제목을 임의로 자르지 않는다. null/알 수 없는 카테고리와 51건 이상 목록의 제품 정책은 서버·클라이언트가 합의해야 한다. 비호환 목록은 이전 정상 기록을 유지하며 오류로 표시한다. |
| P0 | 첫 입장 팝업용 동기화 상태 | Android HTTP 결과 + Unity 수신 결과 | 로딩·정상적인 0건·오프라인/실패를 구분해 전달한다. 확인되지 않은 빈 목록으로 팝업을 채우거나 정수를 지급하지 않는다. 당일 첫 표시 상태는 계정·날짜별로 관리한다. |
| P1 | 과거 날짜 재동기화/정정 | 기존 캘린더 기간 조회 | 앨범이 이미 보관한 날짜를 `requestForestHistory`로 요청하고 호스트가 `ReceiveHistoricalTodoSnapshot`으로 보낸다. 실제 TODO 집계만 고치고 감상한 장면·선택·이미 지급한 보상은 다시 만들지 않는다. 미방문 날짜의 보상 백필 API는 구현하지 않았다. |
| P1 | 실제 일정·완료 시각(있는 경우) | 서버 occurrence에 `endDate/endTime`, `completedAt` 존재 | 현재 Unity 스냅샷에는 시각 필드가 없다. 첫 입장 팝업에 실제 시각을 쓰려면 Android→Unity 계약을 확장한다. 필드가 없으면 정령 일정에 임의 시각을 만들지 않는다. |
| P1 | 서버 기준 시각 | 현재 서버 UTC 시각 전용 계약 없음 | 나들이 4시간 판정에 기기 `DateTime.UtcNow` 사용. 기기 시각 조작 방지가 필요하면 서버 기준 시각/종료 시각 계약을 추가한다. |

### Android → Unity 현재 수신 형식 (HTTP API 아님)

```json
{
  "schemaVersion": 1,
  "accountId": "42",
  "revision": 1024,
  "date": "2026-09-18",
  "timeZone": "Asia/Seoul",
  "requestId": "example-snapshot-request-1",
  "items": [
    {"id": "schedule:130:2026-09-18", "title": "물 마시기", "category": "HEALTH", "isHabit": false, "isCompleted": true},
    {"id": "routine:241:2026-09-18", "title": "책 읽기", "category": "GROWTH", "isHabit": true, "isCompleted": false}
  ]
}
```

값은 예시다. 실제 Android ID 규칙은 `schedule|routine:taskId:YYYY-MM-DD`다. Unity는 `schemaVersion=1`, 비어 있지 않은 `accountId`(128자 이하), 0 이상의 `revision`, Unity의 현재 날짜와 같은 `date`, `items` 0~50개, 목록 안에서 고유한 `id`(128자 이하), 제어문자 없는 제목 1~255자를 요구한다. JSON 전체는 128,000자 이하. 허용 `category`: `NONE`, `WORK_STUDY`, `HEALTH`, `LIFE`, `RELATIONSHIP`, `GROWTH`, `HOBBY`, `REST`, `FINANCE`. `ROUTINE`은 `isHabit=true`, `SCHEDULE`은 `false`다. 로컬 개발용 TODO 입력 폼의 80자 제한과 호스트 수신 계약의 255자 제한을 혼동하지 않는다.

`requestId`는 Android가 Unity 전송마다 발급하는 UUID이며 서버의 데이터 `revision`과 별개다. Unity는 요청 번호가 있으면 `onForestSnapshotResult`로 `{kind:"todo-snapshot",status:"applied" 또는 "duplicate" 또는 "rejected",accountId,date,requestId,message}`를 돌려준다. 연결 상태 메시지도 같은 방식으로 `kind:"todo-sync"` 응답을 받는다. Android는 현재 계정·날짜 및 가장 최근 일반 요청의 종류·요청 번호가 모두 같은 응답만 수락하고 수락한 번호를 소비한다. 이전 실패 응답이 새 세션/커튼을 바꾸지 않는다. 요청 번호를 생략한 기존 개발 송신에는 문자열 결과를 유지하지만 현재 Android 호스트는 그 문자열로 활성 세션을 변경하지 않는다. 서버에 이 UUID 발급 API를 추가할 필요는 없다.

스냅샷은 **차분이 아닌 전체 목록 교체**다. 동일/낮은 revision은 중복 처리한다. 현재 반영된 날짜·revision과 모두 같은 응답은 목록을 다시 적용하지 않고 확인 시각·연결 상태만 갱신하며, 낮은 revision은 최신 확인으로 인정하지 않는다. 다른 계정·다른 날짜의 실시간 목록은 거부한다. 계정 저장을 처음 연결하는 `BindForestHostAccount`는 기존 로컬 TODO 또는 완료 기록이 있으면 연결을 거부한다. `onForestSnapshotResult`로 성공/중복/거부를 Android에 알린다. 배웅 대기 중에도 원본 사실을 받을 수 있으며, 날짜·occurrence별 최초 성장 소유권으로 이전 정령과 새 정령의 성장을 분리한다. 첫 표시·보상 확인은 계정의 날짜 기준이다.

## 서버 개발자가 결정할 API 작업

### 1. 오늘 스냅샷의 일관성 보장 (필수 협의)

기존 `/api/task/calendar`를 재사용한다면 다음을 보장/검증한다: `from=to=계정 시간대의 오늘`, 수정/완료 직후의 최신 전체 occurrence, 루틴의 해당 날짜별 `isCompleted`, 삭제 항목의 제거, 안정된 `taskId`·`taskType`·카테고리. 응답에 전역 `revision`이 없으므로 재연결·병렬 요청·재시작에도 순서를 판정할 수 있는 메타데이터 또는 별도 숲용 스냅샷 엔드포인트를 협의한다. revision은 **계정 단위로 날짜가 바뀌어도 증가**해야 한다. 날짜마다 0으로 초기화하면 Unity가 다음 날 데이터를 중복으로 버릴 수 있다.

**신규 엔드포인트 제안 예**: `GET /api/forest/todo-snapshot?date=YYYY-MM-DD`. 기존 서버의 `{result, detail}` 응답 외피를 따른다면 `detail`에 다음 자료를 담을 수 있다. URL·필드·번호 발급 방식은 설계안이며 현재 서버에 없다.

```json
{
  "result": "success",
  "detail": {
    "accountId": "42",
    "date": "2026-09-18",
    "timeZone": "Asia/Seoul",
    "revision": 1024,
    "items": [
      {"id": "schedule:130", "title": "물 마시기", "category": "HEALTH", "isHabit": false, "isCompleted": true}
    ]
  }
}
```

Android는 성공 응답의 `detail`을 Unity의 `schemaVersion=1` 스냅샷으로 변환해 전달한다. 인증 실패·서버 실패·시간대 불일치 시에는 기존 Unity 목록을 빈 배열로 덮어쓰지 않는다. 날짜의 목록과 revision은 같은 일관된 읽기 결과여야 한다. 반복 루틴의 날짜별 완료 상태와 삭제/미루기 반영도 응답에 포함한다.

### 2. 클라우드 게임 상태 동기화 (선택: 제품 결정 필요)

현재 Unity는 다음 게임 상태를 기기 안의 JSON에 저장하므로 현 로컬 MVP에는 별도 게임 상태 서버 요청이 필요하지 않다. 일반 로컬 저장은 `TodoSpirits/forest-save.json`, Android에서 확인한 계정 저장은 그 옆 `TodoSpirits/accounts/SHA256(accountId).json`이며 예시 체험은 별도 파일을 사용한다. Android 계정 확인 전에는 메모리 저장만 사용한다. 기기 간 이어하기를 원하면 별도 게임 상태 API가 필요하다.

| 묶음 | 서버가 보관해야 할 최소 상태 후보 | 설계 시 주의 |
| --- | --- | --- |
| 정령 생애 | 활성 정령 ID, 만난 날, 활동일/단계, 성향, 이야기 선택, 이름, 독립일, 이전 정령 목록, 재회 편지 | 기존 `/api/spirit`와 의미·ID·성장 규칙이 달라 매핑/이관을 먼저 합의 |
| 일별 숲 기록 | 날짜·정령 ID·그날 완료 항목 스냅샷·행동·분류·확정 여부 | 현재 날짜 이전 수정과 확정일 충돌 처리 필요 |
| 경제 | 정수 잔액, 날짜별 최고 지급 보상, 소비 거래 내역 | 완료 재전송/완료 취소/다중 기기 중복 지급 방지; 거래 ID와 멱등성 키 권장 |
| 선물·장식·나들이 | 선물 날짜/종류/반응, 보유·장착 장식, 나들이 시작/반환 UTC·수령 상태 | 중복 구매/수령 방지, 서버 시각과 동시성 제어 |
| 설정 | `동작 줄이기` | 기기 설정으로 유지할지 계정 설정으로 옮길지 결정 |

**제안 API 형태**: `GET /api/forest/state`, `PUT /api/forest/state` 또는 행동별 명령 API. 그대로 저장 파일 JSON을 서버 계약으로 사용하지 않는다. 버전, 소유 계정, 충돌 감지, 원자적 경제 처리, 일별 멱등성을 먼저 정의한다. 이미지·아트 카탈로그와 선물/장식 가격은 현재 앱 내 상수/에셋이므로 현 버전의 서버 데이터 요구가 아니다. 원격 밸런싱을 채택할 때만 카탈로그 API를 추가한다.

## API/호스트 합동 수락 체크리스트

`ForestHostSnapshotTest`의 현재 5개 테스트는 입력 변환·목록 경계·revision 발급을 다룬다. `ForestHostReplyPolicyTest`의 5개 테스트는 세션/일반 응답의 계정·날짜·요청 ID·종류·중복 소비 경계를 다룬다. 2026-09-18 실제 프로젝트 소스와 실제 domain 모델을 사용한 **순수 JVM 실행에서 총 10/10 PASS**했다(`Validation/ForestPrototype/android-pure-jvm-tests.json`). 기존 캐시의 Kotlin 2.2.21·JUnit 4.13.2·JDK 21을 사용했으며 프로젝트 지정 Kotlin은 2.2.10이다. Android Gradle/기기 실행은 NOT RUN이며, 이 결과를 Activity 생명주기·Unity JNI·실제 HTTP·기기 통합 동작 검증으로 취급하지 않는다.

- [ ] 인증된 A 사용자만 A의 목록을 받고, 로그아웃/계정 B 전환 시 A 숲 저장이 B에게 보이지 않는다.
- [ ] `session-ready`/`session-rejected` JSON의 `accountId`·`date`·`requestId`가 현재 대기 요청과 모두 일치할 때만 처리한다. 이전 계정·이전 날짜·이전 재시도의 지연 응답과 동일 응답 재전송을 재현한다.
- [ ] 일반 snapshot/status 응답 역시 가장 최근 요청의 계정·날짜·종류·요청 ID와 일치할 때만 처리한다. 새 snapshot 이후 이전 로딩 요청의 거부가 도착해도 현재 커튼·세션을 바꾸지 않는다.
- [ ] 일정/루틴 혼합 0·1·50건과 51건, 제목 255/256자, null/미정/알 수 없는 카테고리를 테스트한다. 51건 이상 및 비호환 필드의 제품 정책을 정한다.
- [ ] 일정 생성·수정·삭제·완료·취소 직후 새 `revision`의 전체 스냅샷이 재전송된다. 네트워크 오류는 기존 화면을 유지하고 빈 목록으로 덮어쓰지 않는다.
- [ ] 병렬 응답 역전, 동일 응답 재전송, 앱 강제 종료/재시작 후 재전송이 정수·기록을 중복 생성하지 않는다.
- [ ] 날짜별 루틴 발생/완료, 자정 경계와 기기 시간대 변경, 서버 날짜와 Unity `CurrentDateKey` 불일치를 재현한다.
- [ ] Unity `LastResult`에서 요청 번호가 있는 JSON 결과의 `kind`·`status`·요청 ID를 네이티브 전송과 대조한다. 번호 없는 개발 송신의 `applied`/`duplicate`/`rejected: …` 문자열과 구분한다.
- [ ] 클라우드 상태를 채택하면 동시 기기 구매·나들이 중복 수령·오프라인 복귀·이관/복원 테스트를 추가한다.

## 소스와 주의 사항

- Unity 계약: `Assets/TodoSpirits/Scripts/Runtime/ForestTodoBridge.cs`, `PrototypeApplicationService.Forest.cs`, `Bootstrap/PrototypeBootstrap.cs`, `Persistence/ForestJournal.cs`.
- 서버 계약: `back/src/main/java/com/oow/todowithspirit/controller/{Task,Record,Spirit,User}Controller.java`, `dto/task/TaskOccurrenceResponse.java`, `domain/task/{CategoryType,TaskRepository}.java`.
- Android 계약: `android/data/src/main/java/com/example/data/api/TaskApi.kt`, `response/TaskCalendarResponse.kt`, `android/app/src/main/java/com/example/todowithspirits/feature/forest/{ForestUnityActivity,ForestHostSnapshot,ForestHostReplyPolicy}.kt`, `android/app/src/test/java/com/example/todowithspirits/feature/forest/{ForestHostSnapshotTest,ForestHostReplyPolicyTest}.kt`.
- 예전 `unity/Docs/TODO_SPIRIT/첫 제작 — 서버 데이터와 하루 플레이.md`는 당시의 제안이다. 이 문서는 `completedTasks`만 받는 구조를 제안했으나, 현재 Unity 브리지는 **미완료 항목을 포함한 전체 오늘 목록**을 받는다. 서버가 없다는 당시 설명도 현재 저장소의 `back` 구현과 맞지 않는다.
