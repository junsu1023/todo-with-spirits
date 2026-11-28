# 윈도우 앱 UI 시안

Todo with Spirits 윈도우 데스크톱 앱의 화면 시안입니다. Figma `OOW` › "TODO 최종 와이어 & UI" › **Mobile_GUI**의 색과 구성을 기준으로, 윈도우 창 형태에 맞게 옮겼습니다.

실제로 눌러 볼 수 있는 HTML 시안이며, 앱 코드는 아닙니다. 데이터는 예시 값이고 저장되지 않습니다.

## 보는 방법

레포 최상위에서 아래를 실행한 뒤 http://localhost:8766 을 엽니다.

```powershell
python -m http.server 8766 --bind 127.0.0.1 --directory unity/windows-ui/design
```

파일을 직접 열어도(`design/index.html`) 대부분 동작합니다.

## 폴더

| 경로 | 내용 |
|---|---|
| `design/index.html` | 시안 목록 (여기서 시작) |
| `design/home-v4.html` | 홈: 오늘 일정, 어제 하지 못한 일, 상세·추가 패널 |
| `design/calendar-v4.html` | 캘린더: 주간·월간·연간 |
| `design/dex-v4.html` | 정령 도감 |
| `design/settings-v4.html` | 설정 |
| `design/login-v4.html` | 로그인·회원가입 |
| `design/forest-v4.html` | 정령의 숲 (출시 예정 안내) |
| `design/styles/app.css` | 공용 스타일과 색 토큰 (라이트·대비 높임·다크) |
| `design/scripts/app.js` | 공용 동작: 테마, 사이드바, 창 크기, 알림, 단축키 |
| `design/scripts/task-form.js` | 일정 추가·수정 폼 |
| `design/assets/` | 정령 그림, 숲 배경, 글꼴 |
| `design/CONTRAST.md` | 색 대비 분석 (WCAG 2.2) |
| `design/WEEK-REFERENCES.md` | 캘린더 주간·월간·연간 레퍼런스 조사와 결정 이유 |
| `ref/` | Figma 모바일 화면 캡처(01–15), 숲 프로토타입 캡처(16–19) |
| `DESIGN.md` | 초기 화면 사양(W01–W16) |

## 결정된 것

- 색과 구성은 Figma Mobile_GUI 기준. 글꼴은 나눔스퀘어라운드(OFL 1.1, `design/assets/fonts/OFL.txt`).
- 라이트 모드 기본은 "글자 대비 높이기" 켬(모든 글자 4.5:1 이상). 끄면 Figma 원색.
- 창 크기 1280×800 기준, 최소 960×640.
- 홈에 정령은 두지 않는다(정령은 동반자, 화면은 할 일 중심).
- 어제 하지 못한 일은 자동으로 넘기지 않고 직접 고른다.
- 캘린더 칸에는 날짜와 일정 개수(월간은 제목 한 줄)만 두고, 내용은 목록에서 읽는다.

## 아직 정해지지 않은 것

- 정령의 숲 본 화면(기획 확정 후).
- 도감 정령 그림: Figma 캡처에서 잘라 쓴 것이라 투명 배경 원본으로 바꿔야 한다.
- Windows 고대비 테마(`forced-colors`) 대응.
- 예시 값: 일정, 공지, QnA, 도감의 처음 만난 날·함께한 날.
