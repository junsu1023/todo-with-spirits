# Windows 앱 API 연동

[화면별 API 연동표](docs/windows-gui-api.html)에서 화면 캡처와 연동 상태를 확인할 수 있다. 화면 이미지는 `docs/gui/`에 있으며, 표에서 상태별 필터와 이미지 확대를 지원한다.

표의 연동 상태는 Windows 앱 구현 기준이다. 서버 계약은 `develop`(`efa0984`)의 컨트롤러와 DTO를 기준으로 기록했다. [Confluence API DB](https://wegeon.atlassian.net/wiki/spaces/TO/database/7340080) · [API Docs](https://wegeon.atlassian.net/wiki/spaces/TO/pages/6717652/API+Docs)

## 기존 API

| 작업 | 기존 API | Windows 상태 |
| --- | --- | --- |
| 이메일 로그인과 만료 토큰 재발급 | `POST /api/auth/login`, `POST /api/auth/reissue` | 연결. 토큰은 실행 중에만 보관한다. |
| 이메일 회원가입 | `GET /api/auth/check-email`, `POST /api/user/email/verify/send`, `POST /api/user/email/verify`, `POST /api/auth/signup` | 연결. 설정의 프로필에서 인증번호를 받고 가입한다. |
| Google·카카오 로그인 | `POST /api/auth/social/login` | 서버 API 있음. Windows용 OAuth 클라이언트·브라우저 복귀 주소와 제공자 토큰 발급 방식이 필요하다. |
| 내 정보와 닉네임 | `GET/PATCH /api/user/me` | 연결. 이메일을 표시하고 닉네임을 수정한다. |
| 이메일 변경과 로그인한 계정 재인증 | `PATCH /api/user/me/email`, `POST /api/user/me/email/verify/send`, `POST /api/user/email/verify` | 연결. 프로필에서 연동 이메일을 바꾸고 새 주소로 인증번호를 받아 확인한다. |
| 로그아웃과 회원탈퇴 | `POST /api/auth/logout`, `DELETE /api/user/me` | 연결. 탈퇴는 앱에서 별도 확인을 받는다. 현재 로그아웃은 계정의 리프레시 토큰을 모두 삭제한다. |
| 홈·바탕화면 정령·주간·월간·연간 캘린더 | `GET /api/task?from=…&to=…` | 연결. 연간 범위는 최대 90개 날짜씩 나눠 조회한다. 지난 미완료 단발 일정은 30일 범위에서 표시한다. |
| 일정·루틴 상세 | `GET /api/task/{taskId}?date=…` | 연결. 선택한 날짜의 회차를 조회한다. |
| 일정·루틴 추가·수정·중요 표시 | `POST /api/task`, `PATCH /api/task/{taskId}` | 연결. 계정 일정은 저장 버튼으로 반영한다. 반복 회차 조회에 원래 시작일이 없어 기존 반복 일정의 시간만 바꾸는 동작은 제한된다. |
| 날짜별 완료와 완료 취소 | `POST/DELETE /api/task/{taskId}/complete?date=…` | 연결. 완료 시 받은 정수를 표시한다. |
| 오늘 하기·다른 날로 이동·내일로 미루기 | `PATCH /api/task/{taskId}/postpone`, `PATCH /api/task/{taskId}` | 연결. 단발 일정을 옮긴다. ‘모두 오늘 하기’는 단건 요청을 순차 처리하고 실패하면 성공한 건수를 표시한다. |
| 일정·루틴 삭제 | `DELETE /api/task` | 연결. 서버 삭제 후 복구 API가 필요하다. |
| 동기화와 화면 갱신 | 기존 조회 API | 연결. 설정의 데이터에서 직접 동기화하고, 앱 활성화와 조회 기간 변경 시 일정을 다시 읽는다. |
| 디스플레이 설정 | `GET/PATCH /api/setting/display` | 연결. 다크 모드·디데이·언어 설정을 공유한다. 현 Windows UI 문구는 한국어다. |
| 알림 설정 | `GET/PATCH /api/setting/notification` | 연결. 계정 설정값을 공유하고 일정 알림은 Windows가 울린다. |
| 알림 목록·읽음 처리 | `GET /api/notification`, `PATCH /api/notification/{id}/read`, `PATCH /api/notification/read` | 연결. 다음 페이지와 분류 탭을 지원한다. 서버의 더미 알림 제거 일정은 확인이 필요하다. |
| 정령 도감·대표 정령 | `GET /api/spirits`, `GET/PATCH /api/spirits/current` | 연결. 보유 여부·호감도·대표 정령을 반영한다. |
| 정령 이름·즐겨찾기 | `PATCH /api/spirits/{spiritId}/name`, `PATCH /api/spirits/{spiritId}/important` | 연결. 계정 도감에서 보유 정령의 이름과 즐겨찾기를 변경한다. 단계별 진행률은 기존 도감 응답의 affection 값을 사용한다. |
| 숲 정수·새 정령·장식·배치·일지 | `GET /api/currency`, `POST /api/spirits/meet`, `/api/decorations`, `/api/spirit/home-objects`, `GET /api/spirit/{spiritId}/daily-logs` 등 | 서버 API 있음. Windows 숲 화면 개발 시 사용한다. 현 숲 화면은 출시 안내를 표시한다. |
| 구독 상태 | `GET /api/user/me/subscription` | 연결. 프로필의 내 구독 조회에서 요금제와 만료일을 표시한다. |

일정 목록의 `from`과 `to`는 모두 필수다. 생성·수정 요청의 매주 반복 키는 `repeatDaysOfWeek`다. 완료 요청이 이미 처리된 경우 서버는 `409 ALREADY_COMPLETED`를 반환할 수 있다. [목록 명세](https://wegeon.atlassian.net/wiki/spaces/TO/pages/10256406) · [수정 명세](https://wegeon.atlassian.net/wiki/spaces/TO/pages/36306966) · [완료 명세](https://wegeon.atlassian.net/wiki/spaces/TO/pages/13434903)

## 백엔드 요청과 정책 결정

| 작업 | 확인한 서버 제약 | 요청 또는 결정 |
| --- | --- | --- |
| 기기별 로그아웃 | 현재 계정의 모든 리프레시 토큰을 무효화한다. | Windows만 로그아웃하고 싶다면 세션 하나를 종료하는 계약이 필요하다. |
| 이번 회차와 반복 시작일 구분 | 일정 조회 응답은 `occurrenceDate`를 주고 수정 요청의 `endDateTime`은 반복 시작일도 바꾼다. | 반복 일정의 `startDate`를 조회 응답에 넣거나 시각만 수정할 수 있게 한다. |
| 월간 반복 경계 | 서버는 지정한 날짜가 없는 달을 건너뛴다. 로컬 Windows는 해당 달의 말일을 쓴다. | 기기 간 동일한 반복 정책을 정한다. 온라인 모드는 서버가 반환한 회차를 표시한다. |
| 지난 미완료 일정·일괄 미루기 | 기간 조회와 단건 날짜 변경 API가 있다. | 30일보다 오래된 미완료 일정까지 보여줘야 하면 목록 API를 추가한다. 전체가 한 번에 성공해야 한다면 일괄 변경 API가 필요하다. |
| 정각 알림 | 현재 알림 옵션은 없음·10분·30분·1시간 전이다. | 계정 일정에 정각 알림이 필요하면 0분 옵션을 추가한다. |
| 일정 삭제 취소 | 삭제한 일정을 기존 ID나 완료 이력과 함께 복구할 API를 확인하지 못했다. | 삭제 취소가 요구되면 휴지통과 복구 계약을 정한다. |
| 도감의 카테고리·만난 날짜·획득 힌트 | 기존 도감 응답은 종류·개체·대표·호감도 정보를 제공한다. | 분류·날짜 순서·힌트가 필요하면 종류/개체 필드를 추가한다. |
| 비밀번호 변경과 재설정 | API Docs에 비밀번호 변경은 Todo로 남아 있다. 재설정 API도 확인되지 않았다. | 이메일 계정 비밀번호 변경과 로그인 전 재설정 흐름을 정의한다. |
| 공지·고객 지원 | Windows 시안에 공지사항과 QnA가 있다. 대응 API는 확인되지 않았다. | 목록·상세 API 또는 팀에서 관리하는 공식 페이지 URL을 제공한다. |
| 서비스 이용약관·개인정보 처리방침 | Windows에서 열 문서 위치가 필요하다. | 최신 공식 URL을 제공한다. 버전별 동의 수집이 필요하면 계약을 추가한다. |
| Windows 업데이트 확인 | Windows 최신 버전과 설치 위치를 조회할 계약이 확인되지 않았다. | 배포 채널이 정해지면 버전 정보와 설치 파일 URL을 제공한다. |
| 계정 알림 삭제·시스템 푸시 | 서버 알림 읽음 처리만 확인됐다. Windows는 목록에서만 숨길 수 있다. | 기기 간 삭제나 실시간 푸시가 필요하면 알림 삭제·푸시 계약을 정한다. |
| 기존 로컬 일정 가져오기·오프라인 편집 | 온라인 모드와 로컬 모드는 데이터를 별도로 사용한다. | 가져오기와 다중 기기 오프라인 편집을 지원하면 중복 방지와 충돌 처리 정책을 정한다. |

단축키, 창 위치와 트레이, 자동 실행, 로컬 백업·CSV, 바탕화면 정령 실행 및 IPC는 Windows에서 처리한다.

## 실행과 확인

인증메일 수신과 실계정 쓰기 작업은 배포 환경에서 확인한다.

1. 앱을 실행하고 **설정 → 프로필**에서 로그인한다. 기본 주소는 웹 클라이언트와 같은 Render 환경이다. 다른 환경을 사용할 때는 실행 전 `TODOSPIRITS_API_URL`에 팀이 지정한 HTTPS 주소를 넣는다. 루프백 HTTP는 로컬 테스트에 쓸 수 있다.
2. 홈과 캘린더에서 단발 일정과 반복 일정을 추가하고 다른 기기에서 반영을 확인한다. 같은 계정으로 날짜별 완료·취소와 대표 정령 변경을 확인한다.
3. 설정에서 닉네임과 알림·디스플레이 값을 바꾸고 **데이터 → 동기화**에서 다시 읽는다. 로그아웃한 뒤에는 이 PC에 저장했던 일정이 돌아온다.
