# Todo with Spirits — 윈도우 앱

[윈도우 UI 시안](../windows-ui/README.md)을 기준으로 처음부터 새로 만드는 WPF 데스크톱 앱입니다.

- .NET 10, WPF, 외부 패키지 없음
- 글꼴은 나눔스퀘어라운드(OFL 1.1)를 앱에 넣어 둡니다: `src/TodoSpirits.Windows/Assets/Fonts/`
- 일정은 이 컴퓨터에만 저장합니다: `%LOCALAPPDATA%\TodoWithSpirits\data.json`

## 실행

.NET 10 SDK가 필요합니다. 레포 최상위에서:

```powershell
dotnet run --project unity/TodoSpiritsWindows/src/TodoSpirits.Windows
```

시험할 때 실제 일정 파일을 건드리지 않으려면 저장 위치를 바꿔서 실행합니다.

```powershell
$env:TODOSPIRITS_DATA = "$env:TEMP\todo-test.json"; dotnet run --project unity/TodoSpiritsWindows/src/TodoSpirits.Windows
```

## 바탕화면 정령

정령은 앱 창 밖, 바탕화면 위를 돌아다닙니다. Unity 로 만든 별도 프로그램(`SpiritOverlay/`)이고, 윈도우 앱이 띄우고 감시합니다.

먼저 한 번 빌드합니다 (Unity 6000.3.27f2 필요, 첫 빌드는 몇 분 걸림).

```powershell
unity/TodoSpiritsWindows/Build-Spirit.ps1
```

출력은 `spirit-player/TodoSpirit.exe` 이고 git 에는 올리지 않습니다. 빌드가 없으면 앱은 그대로 동작하고, 설정 › 디스플레이에 "정령 실행 파일이 없어요"라고 나옵니다.

- 정령: 작업 영역 아래를 산책, 끌어서 옮기기, 던지면 떨어져 튕김, 끄는 동안 늘어나고 표정이 바뀜
- 좌클릭: 정령 옆에 오늘 일정(완료 체크, 일정 추가, 앱 열기)
- 우클릭: 메뉴(오늘 일정 보기, 일정 추가, 앱 열기, 정령 쉬게 하기, 앱 종료)
- 설정 › 디스플레이 › 바탕화면 정령 스위치로 켜고 끔
- Unity 시작 로고는 끔. Unity 가 켜질 때 잠깐 띄우는 일반 창은 스스로 화면 밖으로 옮기고(`EarlyWindow.cs`), 첫 장면을 그린 뒤에 정령만 보이게 함. 측정으로는 그 창이 화면에 보이는 시간이 약 0.2초에서 0.03~0.06초로 줄었고, 완전히 없애지는 못함
- 앱과 정령은 이 앱만 접속할 수 있는 Named Pipe 로 이야기합니다. 메시지 형식은 `SpiritOverlay/Assets/Scripts/Shared/SpiritWire.cs` 한 파일을 양쪽이 함께 씁니다.
- 정령이 응답하지 않으면 최대 3번 다시 띄우고, 앱을 끄면 정령도 꺼집니다.

정령 그림은 아직 한 장(`spirit_neutral.png`)입니다. 걷기 · 앉기 같은 애니메이션은 에셋이 준비되면 Unity 프로젝트에 넣습니다.

## 지금 되는 것 (0.1.0)

| 화면 | 내용 |
|---|---|
| 창 | 직접 그린 제목 표시줄, 사이드바, 라이트·다크 전환(기억됨), 최소 960×640 |
| 홈 | 오늘 일정 요약과 다음 일정, 어제 하지 못한 일(오늘 하기 · 모두 오늘 하기), 시간 일정 · 하루 종일 목록 |
| 캘린더 주간 | 한 주 띠(날짜 · 일정 개수) + 고른 날 하루의 목록 |
| 캘린더 월간 | 칸에 날짜 · 첫 일정 제목 한 줄 · "외 n개", 오른쪽에 고른 날 목록, 두 번 눌러 추가 |
| 캘린더 연간 | 12달, 날짜나 달 이름을 누르면 그 달로 이동 |
| 일정 | 추가(Ctrl+N), 제목 · 중요 · 날짜 · 시간 수정(바로 저장), 완료, 삭제, 되돌리기 |
| 정령 도감 | 대표 정령과 만난 정령 수, 정령 8칸, 정령 정보 · 힌트, 대표 정령 바꾸기(기억됨) |
| 설정 | 프로필(닉네임), 디스플레이(다크모드 · Windows 설정 따라가기 · 글자 대비 높이기), 알림(값만 기억), 데이터(저장 폴더 열기 · CSV 내보내기), 이용 안내, 앱 정보 |

## 아직 없는 것

- 로그인, 정령의 숲 화면 (숲은 자리만 있음)
- 정령을 만나는 규칙과 호감도: 도감의 정령 · 호감도 · 처음 만난 날은 시안의 예시 값입니다. 도감 그림 4종은 Figma 캡처에서 잘라 쓴 것이라 투명 배경 원본으로 바꿔야 합니다.
- 설정 중 서버가 필요한 것: 계정(비밀번호 변경 · 로그아웃 · 회원탈퇴), 백업 · 동기화, 공지사항 · QnA · 약관
- 실제 알림: 알림 설정은 고른 값만 저장하고 알림은 오지 않습니다.
- 반복 일정, 알림, 검색, 우클릭 메뉴, 알림 패널
- 날짜 선택 · 체크 상자 · 스크롤 막대의 모양 (지금은 윈도우 기본 모양)
- 사이드바 숨김, 좁은 창에서 패널 겹쳐 열기, 화면 전환 애니메이션
- 서버 연동(계정, 동기화)
- 정령 애니메이션(그림이 한 장뿐), 전체화면 앱을 쓸 때 정령 숨기기, 창을 닫아도 트레이에 남아 정령 유지하기

## 구조

```text
src/TodoSpirits.Windows/
  Themes/      Light.xaml · LightOriginal.xaml · Dark.xaml (색 토큰), Controls.xaml (버튼 · 카드 · 스위치 · 입력 모양)
  Models/      TodoItem, AppData
  Services/    TaskStore (JSON 저장), ThemeService, SpiritHost (정령 띄우기 · 감시 · 통신)
  ViewModels/  MainViewModel (화면 상태 전체, Extras 는 도감 · 설정), Items (화면용 한 줄 · 달력 칸), Spirits (도감 목록), Mvvm (기반 클래스)
  Views/       MainWindow, HomeView, CalendarView, DexView, SettingsView, TaskPanel, SpiritPopup (정령 옆 작은 창), Shared.xaml (일정 카드)
SpiritOverlay/   Unity 프로젝트 (바탕화면 정령)
Build-Spirit.ps1 정령 빌드
```

색 값은 `unity/windows-ui/design/styles/app.css`의 토큰과 같게 맞춥니다. 시안의 색을 바꾸면 `Themes/Light.xaml`(대비 높임), `Themes/LightOriginal.xaml`(Figma 원색), `Themes/Dark.xaml`도 함께 바꿉니다.
