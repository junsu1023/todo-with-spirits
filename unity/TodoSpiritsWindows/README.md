# Todo with Spirits Windows

WPF로 만든 Windows 데스크톱 앱이다. 로그인 전에는 일정과 설정을 %LOCALAPPDATA%\TodoWithSpirits\data.json에 저장한다. 이메일로 로그인하면 계정 일정을 불러오고, 로그아웃하면 이 PC의 일정으로 돌아간다.

[HTML 화면 시안](../windows-ui/README.md) · [화면별 API 연동표](docs/windows-gui-api.html) · [API 및 서버 제약](API-INTEGRATION.md)

## 실행

.NET 10 SDK가 필요하다. 저장소 루트에서 실행한다.

```powershell
dotnet run --project unity/TodoSpiritsWindows/src/TodoSpirits.Windows
```

별도 일정 파일을 써서 확인하려면 실행 전에 TODOSPIRITS_DATA를 설정한다.

```powershell
$env:TODOSPIRITS_DATA = Join-Path $env:TEMP 'todo-spirits-test.json'
dotnet run --project unity/TodoSpiritsWindows/src/TodoSpirits.Windows
```

서버 주소는 실행 전 TODOSPIRITS_API_URL로 바꿀 수 있다. 기본값은 웹 클라이언트에서 사용하는 Render 주소다.

## 주요 기능

| 화면 | 기능 |
|---|---|
| 홈 | 오늘 일정 요약, 어제 미완료 일정 옮기기, 중요·완료 필터, 검색 |
| 캘린더 | 주간·월간·연간 보기, 월간 날짜 패널에서 일정 조회와 추가 |
| 일정 | 날짜·시간·중요 표시, 매일·매주·매월 반복, 날짜별 완료, 삭제 |
| 알림 | Windows 일정 알림, 알림 목록과 읽음 처리, 로그인 시 서버 알림 조회 |
| 정령 도감 | 보유 여부와 호감도, 대표 정령 선택, 로그인 시 이름과 즐겨찾기 변경 |
| 설정 | 이메일 가입·로그인·프로필, 계정 일정 동기화, 테마·글자 대비, 알림, 자동 실행·트레이, 로컬 백업·CSV |

- Ctrl+N으로 일정 추가, Ctrl+F로 검색, Ctrl+B로 사이드바 고정 전환, F1으로 단축키 안내를 연다.
- 반복 일정은 전체 시리즈를 삭제한다. 짧은 달에 해당 날짜가 없으면 로컬 일정은 말일에 표시하고, 계정 일정은 서버가 내려준 회차를 표시한다.
- 일정 알림은 앱이 실행 중일 때만 울린다. 앱을 다시 열면 일정 시각에서 15분이 지나지 않은 알림을 확인한다. 리마인드나 야간 알림 설정에 따라 Windows 풍선 알림을 생략하고 알림 목록에만 남긴다.
- 소셜 로그인, 로컬 일정의 계정 가져오기, 계정 일정 오프라인 편집은 미지원이다. 정령의 숲 메뉴는 출시 안내를 표시한다.

## 바탕화면 정령

정령은 Unity 6000.3.27f2로 만든 별도 프로세스다. 아래 스크립트로 빌드한 다음 Windows 앱의 디스플레이 설정에서 켠다.

```powershell
./unity/TodoSpiritsWindows/Build-Spirit.ps1
```

실행 파일은 spirit-player/TodoSpirit.exe에 생성된다. 빌드 전에는 앱에서 바탕화면 정령을 켤 수 없다. 정령을 클릭하면 오늘 일정을 확인하고 완료할 수 있다. 우클릭 메뉴에서 일정 추가, 앱 열기, 정령 끄기를 선택할 수 있다.

Windows 앱과 정령은 Named Pipe로 통신한다. 정령이 응답하지 않으면 앱이 최대 세 번 다시 실행하며, 앱을 종료하면 정령도 종료된다. 현재 정령 그림은 한 장이다.

## 폴더

- src/TodoSpirits.Windows/ — 앱, 로컬 저장, API 호출, WPF 테마
- SpiritOverlay/ — 바탕화면 정령과 EditMode 테스트
- docs/ — 화면별 API 연동표와 캡처

글꼴은 나눔스퀘어라운드를 사용한다. 라이선스는 [OFL.txt](src/TodoSpirits.Windows/Assets/Fonts/OFL.txt)에 있다.
