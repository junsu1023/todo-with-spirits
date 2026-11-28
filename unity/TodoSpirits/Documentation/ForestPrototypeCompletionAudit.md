# 정령의 하루 — 완료 판정 감사

**검증 기준:** A03 컴파일·149개 테스트·닫기6경계·세 해상도30장, 새 APK/export/WebGL 빌드와 실제 브라우저 스모크를 확인했다. 사용자 요청으로 추가 작업을 중단했다. 전체 제품 완료 판정은 아니며 아래 중단 시점과 미실행 범위를 따른다.


## 사용자 요청에 따른 중단 — 2026-09-18

- [A03 최종 보고서](../Validation/ForestPrototype/A03/review-report.md): APK199.644초/오류0/경고971·70,594,523바이트·v2서명 PASS, library28.988초/오류0/경고971, WebGL607.843초/오류0/경고1. 이전 산출물은 BeforeA03에 보존했다.
- [실제 브라우저](../Validation/ForestPrototype/A03/web-browser-smoke.md): 기존 이름/날짜/정수28/수첩/휴식 복원, 오늘 요약 끝 스크롤과 footer·닫기, 새로고침 뒤 동일 기록·오류0을 확인했다. 탭/루프백 서버 종료.
- 빌드 후 소스·아트8개 및 원본·메타7개 해시 동일. [최종 Editor 상태](../Validation/ForestPrototype/life-ui-20260918-131648-06316bbc/final-editor-state.json)는13:18:43 UTC Play종료/compiling=false/Android/QA빈값/Prepared·Running=false/00_Boot dirty=false다.
- 이어 시작한 생애 UI 검증은 [STOPPED INCOMPLETE](../Validation/ForestPrototype/life-ui-20260918-131648-06316bbc/report.json). 첫 Meeting 화면1장만 확인했고 첫 재시작 후 검증은 하지 않았다. L01/L02의 세 진로·배웅·세 알·두 번째 생애 전체 UI 완주는 NOT RUN으로 유지한다.
- 남은 큰 작업: Android 호스트 전체 빌드·실계정/기기 검증, 성장·배웅 UI 완주, 아트·움직임·표현 개선과 실제 재미 평가. 사용자 요청으로 이 검토본에서 멈췄으며 자동으로 추가 작업을 이어가지 않는다.


## A03 footer·소품·결과 닫기 증거 갱신 — 2026-09-18 12:53 UTC 실행 증거 반영

- [A03 보고서](../Validation/ForestPrototype/A03/review-report.md), 아래 T149-A03/CLOSE-A03/LAYOUT-A03의 실제 결과를 읽었다. 컴파일 오류0, **EditMode149/149 PASS(4.44초)**다.
- 결과 팝업을2초 이상 읽는 동안 반응이 시작되지 않고, 모달 제거 후 callback이 정확히1회 실행되는 **6경계**를 확인했다. normal은 몸/표정→idle, reduced는 몸 고정/표정→idle이며 중복 닫기에도 지갑·선택 수가 변하지 않는다. Focus/확인·Movement/닫기·Everyday/배경 각각 normal/reduced로, 모든 계열×닫기 방식의18조합이 아니다.
- 360×640/1080×2340/1366×768에서 각각10장, 총30장 구조 검사와 담당자 직접 시각 검토를 마쳤다. 수첩·우체통·바람개비 결과/메인, 오늘/선물/나들이 footer, 긴 라벨+나들이 안내에서 흰 footer 제거·라벨/활동 안내 분리·명백한 겹침/잘림/얼굴 가림 없음을 확인했다. 긴 라벨+여행은 배치 전용 격리 fixture다.
- 수첩·바람개비의 약한 받침/그림자에 따른 떠 보이는 인상, 그림의 의미 재사용, 화풍 차이는 남는다. 정지 캡처에서 viewport 밖인 선물 마지막 행과 오늘 생활 전체의 스크롤 접근을 새로 검증한 것은 아니다.
- 닫기 및 배치 QA의 개별 Stop/cleanup은 완료했으나 **A03 APK/export/WebGL·브라우저·최종 Editor 복구는 위 중단 절의 범위로 완료**했다. 아래 A02 검증은 해당 시점의 완료 이력으로 보존한다.

## A02 입력·모션·빌드·이야기 UI 증거 갱신 — 2026-09-18 12:38 UTC 마감

- [MotionInput 실행 기록](../Validation/ForestPrototype/MotionInput/README.md), [경계별 결과](../Validation/ForestPrototype/MotionInput/result.json), [설정 재시작 복원](../Validation/ForestPrototype/MotionInput/reduced-after-restart.json), [Editor 반환 상태](../Validation/ForestPrototype/MotionInput/final-state.json)를 읽고 E03/V02/V04/S02 및 품질 행을 갱신한 A02 이력이다. 현재 수락표는 상단 A03의 확인 범위까지 반영했다.
- 다섯 생활 장면은 실제 카테고리 snapshot→메인, idle은 Animator를 명시적으로 설정한 컴포넌트 검증이다. 버튼/팝업은 Unity 이벤트 호출이며 OS·실기기 입력으로 세지 않는다.
- 설정은 실제 버튼으로 변경한 뒤 **Editor Play 종료→재시작→같은 격리 저장 재로드**하여 복원을 확인했다. OS 프로세스 종료, 화면 회전, 백그라운드 복귀는 NOT RUN이다.
- **A02 APK/export/WebGL BUILD PASS 및 실제 브라우저 스모크 PASS(명시 범위)**를 확인했다. [A02 변경·검증 보고서](../Validation/ForestPrototype/a02-review-report.md)와 아래 BUILD-A02/WEB-A02의 실제 결과를 근거로 한다. Android 호스트 앱 컴파일·기기 실행은 여전히 NOT RUN이다.
- 브라우저는 이전 예시 저장·중앙 방석·동작 줄이기 켬의 새로고침 복원을 실제 버튼으로 확인한 뒤 끔으로 복구하고 탭·루프백 서버를 종료했다. 추가 두 해상도의 방석·성장 v2 배치도 아래 LAYOUT-A02에서 완료했다.
- **이야기 UI36경로×3사건=108선택 PASS:** [최종 report.json](../Validation/ForestPrototype/story-ui-20260918-123029-8722d978/report.json)의108행 전부 programmatic UI binding PASS, 대표25캡처 실파일,12:36:31 완료를 확인했다. 선택 전에 화면/마스크/raycast를 검사한360×640 Editor Button.onClick 실행이며 OS 입력이 아니다. 다음날 예시 저장 파일을 다시 열어 실제 Sprite 연결도 대조했다. [후속 보고서](../Validation/ForestPrototype/story-ui-20260918-123029-8722d978/README.md)의 대표25장 직접 검토에 더해 root3장·감사4장 독립 검토를 반영했다.
- **A02 최종 복구 PASS:** [Editor 최종 조회](../Validation/ForestPrototype/editor-a02-final-state.json)의 내부 success=true,12:38:40 UTC playing/compiling=false,Android,QAoverride="",export=true,bundle=false를 확인했다. 이야기 cleanup12:37:07 및 준비/실행 flag 해제·깨끗한00_Boot·360×640 반환도 보고서에 기록됐다. 이는 A02 종료 상태이며 이후 A03 작업과 구분한다.

## 후속 코드·아트 검증 — 2026-09-18 12:04 UTC

- **S02 저장 분기 검증 확대:** [전체149건 결과](../Validation/ForestPrototype/motion-art-tests-response.json)의 내부JSON에서149/149 PASS, 실패·건너뜀0, 3.85초 확인. 새39건은 세 계열×첫A/B×둘째A/B×마지막3선택의36경로를1/4/7일 선택 직후와2/5/8일 방문 후 JSON저장/서비스 재생성으로 검증한다. 각 기대caption/소품/다음질문/공개터치대사를 비교하고 동일revision재전송으로 보상이 늘지 않음을 확인한다.
- **과거 장면의 정확성과 보존:** 추가3건은 최초 기억의 선택·소품·외형·TODO연결을 기대값과 먼저 비교한 뒤, 성체 성장 및 과거 원본 제목/분류/완료정정 후에도 관찰한 장면이 유지되는지 검사한다. 이 시점의 서비스 테스트는 실제 Sprite UI 검증이 아니며, 그 공백은 후속 STORY-UI에서 별도로 확인했다.
- **아트 정리:** [알파 정리 기록](../Validation/ForestPrototype/growth-alpha-cleanup.md)과 [Unity 연결](../Validation/ForestPrototype/growth-v2-prepare-final.json). 사용자 허용 후 기존 본체RGBA를 유지하고 낮은알파 잡점만 제거한v2를 연결했다. 기존v1/P1/어린정령 PNG·메타7개 해시 보존. 이 시점의 새 아트/모션이 이전 A01 APK/WebGL에 들어 있다고 해석하지 않는다.

## 후속 검증 갱신 — 2026-09-18 11:53 UTC

다음 목록은 최초11:39 UTC 감사 이후 root가11:53 UTC까지 완료한 A01 실행 이력이다. 현재 수락표는 상단 A03의 확인 범위까지 갱신했다. Android 호스트/실기기·전체 UI 조합·사용자 재미 평가의 미확인 범위는 남는다.

- **최신 A01 APK BUILD PASS:** [빌드 결과](../Validation/ForestPrototype/android-a01-build-response.json), 115.008초/오류0. [실제 APK v2 서명 검사](../Validation/ForestPrototype/android-apk-a01-verification.json) PASS, 70,607,939바이트. TODO 호스트 APK가 아니다.
- **최신 A01 Unity export BUILD PASS:** [결과](../Validation/ForestPrototype/android-library-a01-build-response.json), 13.412초/오류0. 기존 경로 `Exports/AndroidLibrary` 갱신, 이전 전체 export는 `AndroidLibrary-BeforeA01`에 보존했다.
- **최신 A01 WebGL BUILD PASS 및 실제 브라우저 스모크 PASS:** [빌드 결과](../Validation/ForestPrototype/webgl-a01-build-response.json), 375.043초/오류0/경고1. [브라우저 기록](../Validation/ForestPrototype/web-browser-a01-smoke.md)에 이전 저장 호환, 담요 구매·다음 날 생활·원본 TODO/보상 구분, 새로고침 뒤 날짜/지갑28/휴식/선물 대사 보존과 오류0을 기록했다. 나들이의 Web 전체 왕복은 NOT RUN이다.
- **최종 정리:** [Editor 상태](../Validation/ForestPrototype/editor-a01-final-state.json)에 Play종료·Android·QAoverride없음·기존 export/bundle설정 복원 확인. [핵심 메타3개](../Validation/ForestPrototype/preserved-meta-a01.json) SHA256 동일. 브라우저 탭/로컬 서버 종료.

최초 감사는 **2026-09-18 11:39 UTC / 20:39 KST**, 현재 반영한 실행 증거는 **12:53 UTC / 21:53 KST**까지다. [수락표](ForestPrototypeAcceptance.md)의 **D01~Q03 22개 항목**을 유지하며 실행된 후속 검증만 반영한다. 이번 문서 갱신 과정에서 새 테스트·빌드·게임 실행은 하지 않았다.

## 1. 판정

**전체 프로토타입 완료는 미달이다.** Unity149개 서비스·저장·분기 회귀, 이야기36경로108선택의 실제 Sprite 연결, Editor 생활 장면·입력·설정 재시작·방석과 성장 v2의 세 해상도 배치, A02 APK/export/WebGL 빌드·브라우저 스모크·최종 Editor 복구 증거는 있다. A03도 별도 컴파일/149개 테스트·결과 닫기6경계·세 해상도30장 배치 검토를 통과했으나 새 빌드/브라우저도 위 중단 절의 범위로 통과했다. 실제 Android 호스트의 컴파일·계정 연동·기기 실행과 사용자 재미 평가는 아직 없다.

| 표기 | 의미 |
| --- | --- |
| PASS | 명시한 범위의 실행 결과와 검증 내용이 요구를 뒷받침한다. 다른 플랫폼이나 모든 UI 조합으로 확대하지 않는다. |
| 부분 증거 | 구현 또는 일부 실행은 확인했으나 수락표에 명시된 결과·경계·표현 중 구체적인 미확인 범위가 남는다. |
| NOT RUN | 해당 실행을 하지 못했다. 환경 부족, 아직 진행 중 등의 이유를 함께 적는다. 구현이 없다는 뜻은 아니다. |
| 미구현 | 실제 필요한 연결/기능이 소스에 없다고 확인한 경우에만 사용한다. |

기능마다 사람의 별도 승인을 완료 조건으로 추가하지 않았다. 보상 계산이나 저장 회귀는 자동 검증으로 판단할 수 있다. **재미·애착·설명 없이 행동을 알아보는 정도**는 자동 검증으로 증명되지 않는 별도 제품 기준이다. 완료율을 임의의 백분율로 환산하지 않는다.

## 2. 증거 인덱스와 최신성

아래 `V/`는 `Validation/ForestPrototype/`, `R/`는 `Assets/TodoSpirits/Scripts/Runtime/`, `P/`는 `Assets/TodoSpirits/Scripts/Presentation/Forest/`를 뜻한다. 테스트 함수명은 단순 이름 검색에 그치지 않고 해당 소스의 assertion을 확인했다.

| 증거 | 직접 확인한 내용 | 사용 한계 |
| --- | --- | --- |
| **T149-A03** — [컴파일](../Validation/ForestPrototype/A03/recompile-result.json), [tests-response.json](../Validation/ForestPrototype/A03/tests-response.json) | A03 코드 컴파일 completed/failed=false/errors=[] 및 전체149/149 PASS, 실패·건너뜀·inconclusive0, **4.44초**. 기존 T149의 서비스/저장/분기 회귀를 새 코드에서 재실행했다. | Android 호스트 컴파일이나 A03 빌드/브라우저 실행 결과가 아니다. 최신149개 실행과 아래 A02의3.85초를 구분한다. |
| **CLOSE-A03** — [report.json](../Validation/ForestPrototype/story-close-a03-20260918-124712-16e719d8/report.json), [실행·10장 검토](../Validation/ForestPrototype/story-close-a03-20260918-124712-16e719d8/README.md) | 6/6 PASS. Focus/확인·Movement/Close·Everyday/Backdrop 각각 normal/reduced. 실제 첫 사건A 선택 후 결과창2초 유지 중 happy0회→모달 제거 후 callback1회→표정/동작→idle. 닫힘 중/뒤 중복 호출에도 반응 추가·지갑/선택 변화 없음. normal 위치 변화 폭 약13.69~13.71 UI단위, reduced 위치/회전/크기 변화0. 대표10장 직접 검토에서 얼굴/팔 가림 없음. | Editor360×640의 가시/mask/raycast 검사 후 Button.onClick이다. OS/Android/Web 입력·모든 계열×닫기 방식×모드18조합·모든 성장/기질은 NOT RUN. 프레임 측정과 정지10장은 움직임 자연스러움 수락을 대신하지 않는다. |
| **LAYOUT-A03** — [30행 결과](../Validation/ForestPrototype/A03/layout-20260918-125152-5f4f7047/report.json), [360](../Validation/ForestPrototype/A03/layout-20260918-125152-5f4f7047/visual-review-360.md), [1080](../Validation/ForestPrototype/A03/layout-20260918-125152-5f4f7047/visual-review-1080.md), [1366](../Validation/ForestPrototype/A03/layout-20260918-125152-5f4f7047/visual-review-1366.md) | 12:53:00 구조 검사30행 PASS 및30장 모두 직접 시각 검토. 각 비율에서 수첩/우체통/바람개비 결과/메인6장, 오늘/선물/나들이 footer3장, 긴 라벨+여행1장. footerAlpha0·16단위 메인 라벨/활동 안내 분리·버튼 화면 범위·명백한 겹침/잘림/얼굴 가림 없음. | 마지막 장면은 세 번째 선택을 제거한 배치 전용 fixture다. 정상 성장·전체36분기 새 재완주가 아니다. viewport 밖 마지막 선물/오늘 생활 전체의 스크롤 접근은 이번 정지 검토에서 NOT RUN. 약한 소품 접지·그림 의미 재사용·화풍/재미 한계는 유지한다. |
| **T149** — [motion-art-tests-response.json](../Validation/ForestPrototype/motion-art-tests-response.json) | 내부 JSON completed, **149/149 Passed**, failed/skipped/inconclusive 0, 3.85초. T110의 검사를 재실행하고 36개 선택 경로+3개 과거 장면 성장/정정 검사를 추가했다. 최초 저장 memory의 소품/선택/이야기 계열/실제 TODO 연결부터 기대값과 비교한다. | JSON 저장·서비스 재생성 검증이다. 36개 실제 Sprite 화면·OS 재시작·Android Activity를 실행한 결과는 아니다. |
| **STORY-UI** — [report.json](../Validation/ForestPrototype/story-ui-20260918-123029-8722d978/report.json), [실행·시각 검토 README](../Validation/ForestPrototype/story-ui-20260918-123029-8722d978/README.md), [실행 스크립트](../Validation/ForestPrototype/qa-scripts/forest-story-ui-run.cs), [대표25캡처](../Validation/ForestPrototype/screens/story-ui-20260918-123029-8722d978) | 12:36:31 완료. 세 계열×첫A/B×둘째A/B×마지막3선택의36경로,각3사건108행 모두 PASS. 360×640에서 화면/마스크/중앙 raycast 확인 후 Button.onClick. 선택 문구·각 선택의 서로 다른 실제 Sprite·결과 caption/Sprite·다음날 저장 파일 재개방 뒤 메인 소품/라벨을 비교했다. 담당25장·root3장·감사4장 후속 시각 검토에서 질문/선택/결과/주 버튼의 명백한 잘림·겹침은 발견하지 못했다. | 프로그램 방식 Editor UI다. OS/모바일 입력·프로세스 재시작·다른 해상도 및 Android/Web의36경로는 미실행. 대표25장의 시각 검토이지108선택의 모든 프레임 육안 검사는 아니다. JSON의 visualReview=NOT RUN은 자동 검사 한계로 보존되고 README가 후속 육안 검토 완료를 기록한다. 흰 footer·작은 라벨·의미 재사용·떠 보이는 소품은 A02 품질 한계다. |
| **T110** — [editmode-interventions-response.json](../Validation/ForestPrototype/editmode-interventions-response.json) | `result`의 JSON 문자열을 다시 파싱하면 completed, **110/110 Passed**, failed/skipped/inconclusive 0, 0.92초. 실행 11:31:20 UTC. 기존 102개와 A01 신규 8개가 포함된다. | Unity EditMode다. Android Activity/Compose 컴파일, JNI 호출, GPU·터치·HTTP를 실행한 결과가 아니다. |
| **T102** — [editmode-post-fixes.json](../Validation/ForestPrototype/editmode-post-fixes.json) | 102/102, 0.96초. 계정 데이터·소유 귀속·세 진로 완주 회귀의 이전 실행. | 최신 자동 테스트 실행은 T149-A03이다. 과거 94/94·102/102·110/110 및 초기 실패를 최신 실행으로 쓰지 않는다. |
| **J10** — [android-pure-jvm-tests.json](../Validation/ForestPrototype/android-pure-jvm-tests.json)와 두 JUnit 로그 | 실제 DTO/변환기/응답 정책 소스의 독립 JVM **5+5 PASS**. 감사 시 기록된 5개 소스 SHA256과 현재 파일이 모두 일치했다. | 캐시 Kotlin 2.2.21로 실행했다. 호스트 설정의 Kotlin 2.2.10/Android Gradle 빌드를 대신하지 않는다. |
| **U1** — [manual-ui-first-pass.md](../Validation/ForestPrototype/manual-ui-first-pass.md) | Editor 360×640의 실제 클릭/스크롤. 첫 요약, 확인/빈 곳/배경 닫기, 이름 건너뛰기, 터치, 집중 첫 사건 A, 앨범. | 날짜/계정 변경 및 모든 사건 분기, 모바일 입력까지 실행한 것은 아니다. |
| **U3** — [scripted-ui-qa.md](../Validation/ForestPrototype/scripted-ui-qa.md), [screens](../Validation/ForestPrototype/screens) | 프로그램 방식 Editor UI 조작·캡처. 세 해상도, 실제 날짜 진행 성장, 두 달·두 정령, 긴 제목 50개, 다음 알, 대기 기록. | Android 입력이 아니다. A01 최신 UI 변경 전에 수행했다. 메인은 `final-` 접두사 파일을 사용해야 한다. |
| **MOTION** — [motion-sampled.json](../Validation/ForestPrototype/motion-sampled.json) | 서로 다른 두 기질 조합의 관찰/활동 동작 좌표·포즈 샘플. 동작 줄이기는 첫 Update 뒤 위치·크기 변화가 없다. | actor의 기질을 주입한 fixture다. 모든 행동·기분·버튼 상태나 설정 저장 재시작을 검증하지 않았다. |
| **MI** — [MotionInput/README.md](../Validation/ForestPrototype/MotionInput/README.md), [result.json](../Validation/ForestPrototype/MotionInput/result.json), [input.json](../Validation/ForestPrototype/MotionInput/input.json) | 5카테고리 실제 snapshot→메인 각 8.2초 이상 몸/소품/그림자와 캡처. 두 production 버튼 이벤트 down/up/disabled, disabled callback 0. 닫기 시작+20회 반복 및 ScrollRect 입력에서 확인/종료 각1회·지갑10→10·재열기 PASS. | Editor 360×640이다. `activities.json`은 발견한 결함을 포함한 수정 전 기록이며 산책/휴식의 현재 증거는 `fixed-activities.json`/`screens/fixed-*`다. idle만 Animator 주입이다. |
| **MI-RESTART** — [reduced-after-restart.json](../Validation/ForestPrototype/MotionInput/reduced-after-restart.json), [final-state.json](../Validation/ForestPrototype/MotionInput/final-state.json) | 실제 설정 버튼으로 reduced=true 저장 후 Play 종료/재시작. 파일/서비스/두 UI 정책/actor 모두 true, 12샘플 좌표·회전·크기 동일, down scale1. 최신 final-state는12:29:43 UTC 추가 해상도 검증 뒤 Play종료·compiling=false·Android·QAoverride 빈 문자열. | Editor Play 재시작이며 Android 프로세스 수명 검증은 아니다. 이야기 UI 이후의 A02 최종 정리는 FINAL-A02에서 확인했다. |
| **LAYOUT-A02** — [layout-followup.json](../Validation/ForestPrototype/MotionInput/layout-followup.json), [layout-png-dimensions.json](../Validation/ForestPrototype/MotionInput/layout-png-dimensions.json), [실행·시각 검토 기록](../Validation/ForestPrototype/MotionInput/README.md) | completed=true,1080×2340/1366×768 각각 REST snapshot 휴식·v2중간·v2성체 총6장. 담당자6장·root대표2장 시각 검토에서 방석 중앙 배치와 외형 잘림 없음을 확인. 기존360×640과 합쳐 해당 배치 세 해상도 PASS. | v2 성장 외형은 실제 actor Sprite 배열 fixture이며 저장된 성장 단계를 바꾸거나 성장 과정을 완주하지 않았다. 산책 가방 모션의 추가 두 비율·다섯 행동 모든 해상도·실기기는 NOT RUN이다. |
| **ART2** — [growth-alpha-cleanup.md](../Validation/ForestPrototype/growth-alpha-cleanup.md), [growth-v2-prepare-final.json](../Validation/ForestPrototype/growth-v2-prepare-final.json), [growth-v2-preserve-after.json](../Validation/ForestPrototype/growth-v2-preserve-after.json) | 낮은 알파/본체 외 잡점만 새 v2 사본에서 정리, 유지 본체 RGBA 동일, 네 포즈/캔버스 유지. Prepare/Validate 성공, 실제 actor의 v2 참조와 화면 확인. 기존 v1 PNG2/meta2·P1meta2·어린meta1의 **7파일** 해시 보존. | 매우 약한 가장자리 안티앨리어싱은 달라진다. 모든 기기 압축/Atlas/외곽 픽셀·컨셉 품질 PASS는 아니다. P1 누락8종/독서2프레임 제작도 아니다. |
| **WEB** — [web-browser-smoke.md](../Validation/ForestPrototype/web-browser-smoke.md), [webgl-persistence-postprocess.json](../Validation/ForestPrototype/webgl-persistence-postprocess.json) | 실제 브라우저 예시 입력·이름·사건·하강 닫힘·당일 재열람. 저장 첫 실패를 보존하고 HTML 후처리 후 이름/선택/10정수 재로드 복원 PASS. | A01 이전 WASM이다. 실제 계정, 모바일 브라우저, 저장 차단·용량 초과는 NOT RUN. |
| **A-GIFT** — [intervention-gift-ui-resumed.json](../Validation/ForestPrototype/intervention-gift-ui-resumed.json) | 내부 실행도 success=true/PASS. 부족 금액 비활성, 담요 1회 결제, 당일 재열람 무과금, 다음 날 실제 휴식 장면. | [첫 파일](../Validation/ForestPrototype/intervention-gift-ui.json)은 내부 NullReference 실패다. 원인은 검증 코드가 public Render를 NonPublic reflection으로 찾은 것. 복구 후 검사한 범위만 PASS다. |
| **A-START** — [intervention-trip-start-ui.json](../Validation/ForestPrototype/intervention-trip-start-ui.json) | 내부 PASS. 장식 구매 30, 장착/해제 무과금, 준비물 버튼 3개 선택 가능, 관찰 수첩으로 출발해 40을 한 번 지불. | 준비물 3개 각각의 전체 여행 UI 완주·앨범 재열람은 이 파일의 검사 항목이 아니다. |
| **A-RETURN** — [intervention-trip-claim-album.json](../Validation/ForestPrototype/intervention-trip-claim-album.json), [intervention-album-reload.json](../Validation/ForestPrototype/intervention-album-reload.json) | 두 파일 모두 내부 PASS. 4시간 전 수령 불가, Editor 전용 시간 가속 뒤 귀환 사건/발견물, 앨범 재열람과 원래 날짜로 돌아가기, 저장 reload 후 동일 여행/잔액40, 재지급·중복소비 없음. | 실제 4시간 대기나 기기 재시작 결과가 아니다. 관찰 수첩 1종의 UI 경로이며 과거 성장 단계 초상 비교는 검사 항목에 없다. |
| **A-REPORT** — [intervention-ui-qa.md](../Validation/ForestPrototype/intervention-ui-qa.md), [intervention-trip-reload.json](../Validation/ForestPrototype/intervention-trip-reload.json) | 진행 여행·선물·장식 소유/해제·잔액 저장 재로드 및 위 UI 결과를 종합한다. 나들이 재열람은 애니메이션 후 `intervention-trip-album-settled-360x640.png`를 시각 근거로 사용한다. | 전체 장식/각 준비물 UI 왕복은 NOT RUN, 새 재열람 화면은 360×640만 확인했다. 진행 여행 reload는 격리 서비스 재생성이며 OS 프로세스 종료가 아니다. |
| **BUILD-OLD** — [android-library-build.json](../Validation/ForestPrototype/android-library-build.json), [android-apk-build.json](../Validation/ForestPrototype/android-apk-build.json), [webgl-build.json](../Validation/ForestPrototype/webgl-build.json) | Unity library export / 단독 ARM64 APK / WebGL 본체 BUILD PASS. 각각 errors 0. APK v2 서명도 [검사 PASS](../Validation/ForestPrototype/android-apk-verification.json). | 세 결과는 A01 이전 소스다. 상단11:53의 A01 및 아래 BUILD-A02와 구별한다. 단독 Unity APK는 TODO Android 호스트 APK가 아니다. |
| **BUILD-A02** — [APK 빌드](../Validation/ForestPrototype/android-a02-build-response.json), [APK 서명·크기](../Validation/ForestPrototype/android-apk-a02-verification.json), [library 빌드](../Validation/ForestPrototype/android-library-a02-build-response.json), [WebGL 빌드](../Validation/ForestPrototype/webgl-a02-build-response.json) | 내부 JSON 모두 completed/Succeeded. APK154.936초·오류0/경고971·실제70,595,495바이트·v2서명 PASS. export19.929초·오류0/경고971. WebGL536.386초·오류0/경고371·44,508,555바이트. 이전 export/WebGL은 각각 BeforeA02 디렉터리에 보존했다. | Android BuildReport의 누적 파일 크기는 실제 APK 크기가 아니다. shader/Pipeline 경고가 남아 있으며 실제 TODO 호스트 컴파일·기기 설치/실행·GPU 검증은 NOT RUN이다. |
| **WEB-A02** — [web-browser-a02-smoke.md](../Validation/ForestPrototype/web-browser-a02-smoke.md) | IAB viewport1280×720/Unity canvas960×600. 화면 버튼만으로 A01 예시의 WebSaved/9월19일/2활동일/수첩/완료2/4/오늘18/지갑28 및 휴식·중앙 방석 복원 확인. 설정 켬→브라우저 새로고침→켬 복원, error로그0. 검증 후 다시 끔·탭/루프백 서버 종료. | 실제 브라우저 좌표 입력이며 Editor 이벤트 주입과 구별한다. 전체 이야기·나들이·성장3단계·모바일 Web·실제 계정·성능 측정은 NOT RUN이다. |
| **HOST** — [android-host-online-help.json](../Validation/ForestPrototype/android-host-online-help.json), [android-host-build-attempt.json](../Validation/ForestPrototype/android-host-build-attempt.json) | Gradle 설정 `help` PASS. 전체 호스트 test/assemble 실제 시도는 android-36.1 부재로 의존성 계산 중 exit 1, 실행 task 0. | 앱 컴파일·Android Gradle 테스트·APK 조립 **NOT RUN**. 설치·라이선스 자동 수락은 하지 않았다. |
| **PROTECT** — [preserved-meta-after-builds.json](../Validation/ForestPrototype/preserved-meta-after-builds.json), [editor-final-state.json](../Validation/ForestPrototype/editor-final-state.json), [intervention-editor-stop.json](../Validation/ForestPrototype/intervention-editor-stop.json), [intervention-build-setup.json](../Validation/ForestPrototype/intervention-build-setup.json) | 기존 어린 정령/P1 독서/P1 책상 **3개 메타** SHA256 전후 동일. 이전 정리에 더해 A01 Play 종료 11:38:20, QA override 빈 문자열/Android 대상 11:38:46의 후속 증거가 있다. | 전체 미커밋 파일의 전후 동일 증명이 아니다. 이 행은 이전 실행 이력이며 A02 마감은 FINAL-A02를 따른다. |
| **PROTECT-A02** — [a02-post-build-source-check.json](../Validation/ForestPrototype/a02-post-build-source-check.json), [a02-preserved-originals.json](../Validation/ForestPrototype/a02-preserved-originals.json) | 빌드 전 manifest에 기록한 소스/아트/프리팹9파일이 모두 unchanged=true. 기존 v1 PNG/meta·P1 meta·어린 정령meta7파일도 before/current SHA256 동일·preserved=true. | 기록된16파일의 검사 범위다. 전체 미커밋 작업의 전후 동일 증명으로 확대하지 않는다. |
| **FINAL-A02** — [editor-a02-final-state.json](../Validation/ForestPrototype/editor-a02-final-state.json), [이야기 UI 복구 기록](../Validation/ForestPrototype/story-ui-20260918-123029-8722d978/README.md) | root의 실제12:38:40 조회 내부 success=true: playing=false,compiling=false,Android,QAoverride="",export=true,bundle=false. 이야기 cleanup12:37:07 및 Prepared/Running=false,00_Boot dirty=false,360×640 복구도 확인했다. | A02의 마감 상태다. 이후 A03 개선 작업의 진행/종료 상태나 검증 결과를 나타내지 않는다. |

## 3. 수락 항목별 판정

### 실제 데이터와 진입

| ID | 현재 판정 | 확인한 구현과 실행 근거 | 남은 검증 / 미달 범위 |
| --- | --- | --- | --- |
| **D01** | **부분 증거 / 통합 NOT RUN** | Android `ForestScreen`→`ForestUnityActivity`와 Unity library 의존성, 인증 profile→calendar 전체 목록→`ReceiveTodoSnapshot` 발신 코드가 있다. J10은 SCHEDULE/ROUTINE, 0·50건, title 255 보존을 검사한다. HOST 설정은 통과했다. | SDK 36.1 라이선스 동의 대기·미설치로 앱 컴파일부터 NOT RUN. 실제 로그인/오늘 0·1·여러 일정·루틴 수신, TODO 수정 후 복귀, Activity unload→Today 복귀는 기기에서 미실행. |
| **D02** | **부분 증거** | `R/Bootstrap/PrototypeBootstrap.OpenHostSession`의 계정 SHA256 별 저장 파일 및 앱 재연결을 정적 확인. T110의 `HostRevisionReplayCancellationAndReloadAreIdempotent`, `HostRejectsCrossAccountUnknownCategoryAndWrongDateWithoutMutation`은 재전송·취소/재완료·재로드·다른 계정 거부/무변경을 검사. J10은 A/B·날짜·requestId·callback kind 불일치와 이전 rejected 무시를 실제 검사한다. | **다른 계정 거부 테스트는 A→B→A 정상 전환과 파일 복원을 검증하지 않는다.** Bootstrap 계정 전환/재시작 및 실제 JNI 지연 콜백은 미실행. 기존 revision 테스트는 동일 revision 재전송이며 엄격히 더 낮은 revision 입력의 실행 assertion은 찾지 못했다. |
| **D03** | **부분 증거** | T110의 `AwaitingErrorOfflineAndConfirmedEmptyAreDifferentAndNeverInventRewards`, `FailedConnectionUsesCachedFactsWithoutClaimingLatestAndNotifiesObservers`는 상태 구분·0보상·확인 시각·캐시 사실·관찰자 갱신을 검사. U1/WEB은 연결 대기를 정상 0건으로 표현하지 않는 화면을 확인. 재시도 버튼/호스트 refresh 경로가 있다. | 실제 통신 실패/복구 UI, 정상 0건과 오프라인의 화면 비교는 전체 증거가 없다. **새 Activity의 첫 진입에서 profile을 못 받는 오프라인**은 알려진 계정을 안전하게 재확인할 로컬 경로가 없어 가림막/재시도에 머문다. 이 경우의 숲 진입은 미지원이며, 같은 Activity에서 확인한 계정의 캐시 재사용과 구분한다. |
| **D04** | **PASS — 입력/사실 보존 범위** | T110의 `FiftyOriginalTitles...`, `UnspecifiedKeepsItsCategory...`, `DismissingAnOlderOpenSummary...`, `MidnightDoesNotCarryYesterday...`, host 취소/재전송·수정 테스트가 50건/255자/NONE/완료 취소/전체 빈 목록/날짜 전환과 지갑 보존을 검사한다. J10은 51건·256자·중복·틀린 날짜/분류/타입/개행을 거부한다. U3에 50×255자 상단/하단 실제 캡처와 부족한 Text 높이 0 기록이 있다. | 실제 서버의 삭제·반복 일정 occurrence 갱신까지 보장하는 판정은 아니다. 그 발신·실기기 통합은 D01에 남는다. 로컬 TODO 편집의 기존 80자 제한 테스트와 호스트 입력 255자 제한은 서로 다른 경로다. |

### 오늘 요약과 보상

| ID | 현재 판정 | 확인한 구현과 실행 근거 | 남은 검증 / 미달 범위 |
| --- | --- | --- | --- |
| **E01** | **부분 증거** | U1/WEB에서 첫 자동 팝업→닫기→당일 재입장 생략/수동 재열람, WEB 저장 수정 후 실제 브라우저 reload 복원. T110의 오프라인 방문 확인 후 첫 snapshot, 자정 전환, 같은 날 다음 정령 테스트는 방문/보상 플래그의 경계를 검사한다. | 새 저장·다음 날짜·A/B 계정 전환을 실제 UI와 계정 파일로 연결한 전체 Play 증거가 없다. 실제 Android 재실행은 NOT RUN. |
| **E02** | **부분 증거** | `P/ForestAppView.Daily.cs`는 원래 TODO/습관, 완료 상태, 정령 행동·위치·이유, 총/신규 획득을 결합한다. T110은 미완료를 완료 활동으로 바꾸지 않음, 0건/1·2·4 이상 보상, NONE 보존을 검사. U1/WEB은 1/4 완료, U3는 2/4 및25/50 화면/스크롤. A01 브라우저 후속은 선물 생활과 원래 TODO/보상의 구분도 확인했다. | 요구된 **0건·전부 미완료·완료1/2/4건의 화면과 저장 대조**를 한 세트로 모두 실행한 결과는 없다. 이미 확인한 선물 자율 활동의 대표 경로를 미실행으로 되돌리지 않는다. |
| **E03** | **부분 증거 — Editor 반복 입력 PASS** | U1의 카드 빈 곳/확인/외부 배경, WEB 하강 중간 화면에 더해 MI에서 실제 오늘 팝업 이벤트 시작+20회 반복 및 닫는 중 ScrollRect 입력을 실행했다. 확인/종료 callback 각1회, 약0.30초 하강 후 제거, 지갑10→10, 다시 열고 닫기 PASS. CLOSE-A03은 별도 이야기 결과창의 확인/닫기/배경과 중복 호출을6경계에서 검사해 모달 제거 후 반응1회·지갑 불변을 확인했다. | 두 결과 모두 이벤트 주입이며 OS 연속 탭·다중 터치가 아니다. 회전·백그라운드 복귀와 닫는 중 재시도 버튼/네트워크 응답 경합은 NOT RUN. 이야기 결과창6경계를 오늘 팝업의 모든 환경 재검증으로 확대하지 않는다. |
| **E04** | **PASS — 보상/방문 저장 계약** | T110 `EssenceRewardCalculatorTests`의 0/10/18/18/25/25와 최고 차액, `DismissingAnOlderOpenSummary...`의 열린 시트 이후 +8 미확인 유지·취소/재완료 무중복, `FailedAcknowledgeAndFailedSnapshot...`의 실패 시 이전 저장/총10/미확인 방문 복구를 검사. 정령 교체 때도 일일25 상한을 유지한다. U1/WEB은 재열람 총10/신규0, 브라우저 재시작 뒤 동일 지갑을 확인했다. | Android 저장매체 장애·Web 할당량 초과는 별도 미실행이다. 이 행은 서비스의 원자적 상태 복구와 실제 확인한 표시를 수락하며 모바일 저장 장애 전체를 주장하지 않는다. |

### 장면·반응·조작

| ID | 현재 판정 | 확인한 구현과 실행 근거 | 남은 검증 / 미달 범위 |
| --- | --- | --- | --- |
| **V01** | **PASS — 기본 화면 세 해상도** | U3의 `final-360x640-main.png`, `final-1080x2340-main.png`, `final-1366x768-main.png` 및 각 요약/앨범이 존재한다. 360/1366 최종 원본을 이번 감사에서도 열어 정령 중심, 현재 활동, 상단 오늘/대사, 하단 보조 진입점을 확인했다. 가로는 중앙 세로 구도다. LAYOUT-A02는 방석·성장 v2를 보완했고 LAYOUT-A03은 세 해상도30장에서 소품/라벨/활동 안내 분리·footer 종이 연결·얼굴/버튼 가림 없음을 확인했다. | A01 여행 기억 재열람의 다른 해상도 및 모든 새 화면/스크롤 전체로 확대하지 않는다. A03의 viewport 밖 선물 마지막 행·오늘 생활 접근은 정지 검토에서 NOT RUN. 수첩/바람개비의 약한 접지, 360의 작은 보조 글씨, 컨셉 원화 수준의 완성도·실기기 safe area는 이 PASS와 구분한다. |
| **V02** | **PASS — Editor 5생활/idle 컴포넌트 범위** | MI의 WORK_STUDY/HEALTH/LIFE/RELATIONSHIP/REST 실제 snapshot→메인에서 독서·산책·만들기·차·휴식 몸/소품/그림자를 8.2초 이상 샘플링하고 각3장 캡처. 산책 가방 X 고정·방석 왼쪽 배치를 발견/수정 후 `fixed-activities.json`과 `fixed-walk/rest-*`로 재검증. 가방/그림자 이동 차이 최대0.000202, 방석235.296 정사각·중앙 발밑. idle은 실제 actor에 Activity=idle을 설정한 별도 컴포넌트 PASS. LAYOUT-A02와 기존360 결과로 방석·성장 v2 배치의 세 해상도를 확인했고 WEB-A02도 휴식·중앙 방석을 확인했다. | 산책 가방 모션의 추가 두 비율·다섯 행동 전 해상도·모바일·idle의 정상 사용자 입력 경로 전체는 NOT RUN이다. 성장 외형 fixture를 성장 과정 재완주로 세지 않는다. 독서 후보1프레임·P1 두 후보 제한과 행동 인식/자연스러움 평가 한계는 유지한다. |
| **V03** | **부분 증거** | T110 `PendingDialogue...`는 원제목/예정 대사 두 개·기대 기분·완료 허위 주장 없음, `FailedConnection...`은 오프라인 대사를 검사. Core는 기질 가중치와 다른 설명을 검사. MOTION은 두 기질의 몸짓 차이를 실제 샘플링. U1/WEB 실제 클릭 반응, 최신 여행 중/귀환 대사 테스트가 있다. | 같은 오늘 입력에서 두 기질×예정/완료/오류×기분의 **문장과 몸짓**을 함께 비교한 전체 Play 결과는 없다. 기질을 actor에 직접 주입한 샘플은 정상 계정/정령 생성부터의 검증이 아니다. |
| **V04** | **PASS — Editor 이벤트/설정 Play·브라우저 재시작 범위** | MI에서 ForestUiMotion/ArtKitButtonMotion 두 production 버튼의 down 약.965→up 약1, disabled 약1·click0, hit target1 유지와 disabled sprite를 검사했다. MI-RESTART는 실제 설정 버튼→저장→Play종료/재시작→같은 저장에서 파일/서비스/UI/actor reduced=true, 몸12샘플 고정과 down scale1을 확인했다. WEB-A02는 실제 설정 버튼으로 켬→브라우저 새로고침 후 켬 복원을 확인하고 끔으로 복구했다. CLOSE-A03은 결과창을 읽는 동안 반응 보류→닫힘 후1회, normal 몸/표정·reduced 고정/표정→idle을6경계에서 확인했다. | OS 프로세스 종료/모바일 터치·진동·입력지연·성능은 NOT RUN. A03의18조합 전체와 모든 성장/기질·새 WebGL 반응은 아직 미실행이다. 브라우저 설정 스모크는 모션 프레임 계측이 아니며 모든 개별 메뉴/hover/focus 조합을 실행한 결과도 아니다. |

### 이야기·기억·성장

| ID | 현재 판정 | 확인한 구현과 실행 근거 | 남은 검증 / 미달 범위 |
| --- | --- | --- | --- |
| **S01** | **부분 증거 — 세 계열3사건 Editor UI PASS** | T110 `EveryInputFamilyCanReachAllThreeScenes...`는 WORK_STUDY/HEALTH/NONE의1/4/7일 선택·서로 다른 결과·재로드를 검사하고 분류 정정 후 계열/선택 보존도 통과했다. STORY-UI는 WORK_STUDY/HEALTH/RELATIONSHIP 완료 입력으로 집중/움직임/일상 세 계열의 발견→시도→완성 및36선택경로를 실제 화면 Text/Sprite와 다음날 파일 재개방까지 대조했다. | 세 계열의3장면 UI 미검증 공백은 해소됐다. **혼합/NONE 입력 조합의 계열 결정 UI 전체**는 미실행이며 NONE의 서비스 테스트와 구분한다. 다른 해상도·OS/모바일 입력·36경로 Android/Web 실행으로 확대하지 않는다. |
| **S02** | **PASS — 36분기 저장·대사·Editor Sprite 연결 범위** | T149 및 최신 T149-A03의36경로는1/4/7일 선택 직후와2/5/8일 방문 후 JSON저장/서비스 재생성, 기대 caption/소품/다음 질문/공개대사 및 무중복을 검사한다. 추가3건은 성장/과거정정 후 관찰 기억 보존 PASS. STORY-UI는108선택의 문구·실제 선택/결과 Sprite와 다음날 파일 재개방 뒤 메인 소품/라벨을 대조했고 대표25캡처도 검토했다. A03은 전체36경로를 반복하지 않고 CLOSE-A03 및 LAYOUT-A03으로 변경한 반응 시점·footer/소품/라벨을 별도 검사했다. | 과거36경로의 실제 Sprite 미검증 공백은 해소됐다. OS 입력·프로세스 재시작·다른 플랫폼 전체 실행은 아니다. 흰 footer 제거와 라벨 분리는 A03 세 해상도에서 확인했지만 편지→우체통의 의미 재사용·수첩/바람개비의 약한 접지 인상·전체 미술 품질까지 수락한 것은 아니다. |
| **M01** | **부분 증거** | U3와 `album-month-selection.json`/`album-old-september.json`은 이전 정령 9월 청소년/10월 성체, 새 정령의 만남 전 빈 달을 확인. T110은 두 달의 TODO·ObservedActivities/ObservedRevision 보존, `ForestDailyMemory`와 Album 코드는 과거 외형/소품·출처 링크를 사용한다. A-RETURN은 최신 여행의 저장/reload/앨범 재열람을 검사했다. | 두 달·두 정령별 **관심/사건/소품/TODO 연결 상세 전부**를 화면으로 대조한 증거는 일부다. 여행이 추가된 과거 날짜의 당시 성장 외형 비교도 남는다. |
| **M02** | **PASS — 과거 사실 정정 서비스** | T110 `TwoMonthsKeepObservedScenesWhileHistoricalFactsAreCorrectedWithoutRewardReplay`는 과거 완료 취소 후 TODO=false, 관찰 장면 JSON 동일, observed revision1/current revision3, 선택·지갑 동일, 월별 사실 집계 변화·재로드를 assertion한다. 같은 날 정령 교체 뒤 과거 취소가 원래 정령에게만 적용되는 회귀도 통과했다. | 실제 서버 과거 calendar 응답 및 JNI→화면 자동 갱신은 D01/D02에 남는다. 서비스의 정정과 당시 이야기 보존은 사용자 승인 없이 자동 검증 가능한 범위다. |
| **L01** | **부분 증거** | 현재 `ForestStoryRules`는 0~1/2~3/4~6/7~9/10+ 활동일 A~E, 21+활동일 **AND** 선택3회 F다. T110의 `AdultCanKeepLiving...` 3건은 21일 입력, Recorder/Explorer/Artisan, 근거3개, 22일에도 같은 성체/Growing, 각 선택 재로드를 검사. U3는 실제 날짜 진행 성장·외형3단계·성체 클릭을 실행했다. | Play 성장 fixture는 한 경로다. **세 진로 모두**의 결과 화면/근거3개 및 각 중간 단계 재시작 화면은 확인되지 않았다. 옛 Core의 28/42활동일 테스트는 구 생애 규칙 회귀이며 현재 숲의 21+3 조건 증명으로 쓰지 않는다. |
| **L02** | **부분 증거** | T110 첫날 이름 skip/change·잘못된 이름/어린 배웅 거부, 세 진로에서 Growing→사용자 BeginFarewell→매 단계 재로드→독립→다음 알→새 완료/옛 생활 JSON 동일을 검사. U3는 배웅 서비스 순서, 실제 WelcomeEgg 버튼→이름 팝업, 이전 기록/대기 기록 화면을 확인. | 첫 정령부터 배웅·독립까지 **모든 단계 실제 UI + 단계마다 앱 재시작**, 세 알 모두의 UI 동행, 두 번째 정령을 성체까지 키우는 전체 완주는 없다. 자동 서비스 호출 완주와 실제 조작 완주를 구분한다. |

### 개입·품질·보호

| ID | 현재 판정 | 확인한 구현과 실행 근거 | 남은 검증 / 미달 범위 |
| --- | --- | --- | --- |
| **A01** | **부분 증거 — 데이터 전종/대표 UI PASS** | T110의 신규 선물6종은 당일 미반영→D+1~3 자율 생활→D+4 만료, 매일 재로드/관찰 기억, TODO·정수·성장 불증가를 검사. 최신 선물 우선순위/다른 정령 선물 무시와 오프라인 여행 대사도 검사. 기존 여행3종은 비용40/4시간 전 수령 거부/늦은 귀환/재로드/중복수령 거부를 검사. A-GIFT·A-START·A-RETURN·A-REPORT는 담요와 1개 장식, 준비물3개 선택, 수첩 출발/귀환/앨범/뒤로가기/저장 reload/무중복 UI PASS. WEB-A02는 기존 담요 생활의 휴식·대사·중앙 방석 복원을 확인했다. | 보완 담당도 **장식 전체 종류 및 여행3개 준비물 각각의 UI 왕복 NOT RUN**을 명시했다. 선물/꾸미기의 다른 생활 소품이 실제로 구별되는지, 과거 여행의 당시 초상이 맞는지는 현재 파일에 assertion이 없다. 이는 V02/M01의 표현 증거 공백과 겹친다. 기존 여행3종 서비스 검증은 유효하므로 경제·4시간·중복 수령 검사를 또 반복할 필요는 없다. A02 빌드는 BUILD-A02에서 완료 확인했다. |
| **Q01** | **부분 증거 — Unity/JVM PASS** | 최신 T149-A03 149/149·컴파일 오류0, J10 10/10. 새39건의 실제 assertion과 MI 및 A03의 수정 후 Play/입력/배치 검증을 확인했다. 실패 저장/복구·귀속·중복 보상·입력 경계도149건에 포함된다. BUILD-A02의 Unity Android/WebGL 빌드 오류는0이며 A03 빌드는 진행 중이다. | 전체 타깃의 컴파일 오류 없음은 아직 주장할 수 없다. **Android host 앱 소스 컴파일은0 task**이며 SDK36.1 동의·설치 대기다. Unity 단독 APK/export의 성공이 이를 대체하지 않는다. |
| **Q02** | **부분 증거 / A02 BUILD·브라우저 PASS / A03 진행 중·실기기 NOT RUN** | BUILD-A02의 library/APK/WebGL 및 APK v2서명 PASS. WEB-A02는 이전 예시 복원·휴식 중앙 방석·동작 줄이기 켬의 새로고침 복원을 실제 버튼으로 확인했다. Android971/Web371 경고는 A02 결과이며 A01 및 BUILD-OLD와 구별한다. A03 새 소스의 빌드·브라우저도 위 중단 절에서 별도 완료 확인했다. | SDK36.1 동의 대기, adb 기기0으로 호스트/실기기 및 모바일 성능·터치·진동은 NOT RUN. 브라우저 전체 이야기·나들이·성장 UI 검증도 기존 스모크 범위 밖이다. 웹 앱의 실제 계정 Unity 호스트는 **미구현**이나 Android 우선 범위 밖의 후속이며 임의의 추가 완료 조건으로 만들지 않는다. |
| **Q03** | **부분 증거 — 확인 파일·격리 저장·A02 복구 PASS** | PROTECT-A02에서 빌드 전 소스/아트/프리팹9파일 동일·기존 원본/메타7파일 보존을 확인했다. MI/LAYOUT-A02/STORY-UI는 고유 Temp 예시 저장만 사용했다. BUILD-A02는 이전 export/WebGL을 보존했고 WEB-A02는 동작 줄이기 끔 복구·탭/서버 종료 및 예시 기록 보존을 명시한다. FINAL-A02에서 이야기 UI cleanup과 최종 Editor 대상/설정/QA override 복구를 확인했다. `JsonSpiritSaveRepositoryTests`의 교체·복구·손상 보존 검사도 통과했다. | 전체 기존 미커밋 파일의 작업 시작 전 기준 해시가 없어 **모두 보존의 포괄 증명**은 없다. 검사하지 않은 파일까지 PASS를 확대하지 않는다. A02의 완료된 cleanup을 미실행 항목으로 다시 요구하지 않는다. A03 빌드 후 복구와 후속 생애 QA의 안전 종료도 위 최종 Editor 상태로 확인했다. |

## 4. 최신 A01 변경과 기획의 관계

- **정책 일치:** 첫날 이름/건너뛰기/변경, 1·4·7 활동일 사건, **21 활동일+3선택**, 성체 이후 계속 생활, 사용자가 시작하는 배웅은 현재 GDD와 코드가 일치한다. 자동 독립이나 달력 경과만의 성장을 완료 정책으로 사용하지 않는다.
- **선물 표현 변경을 반영:** 감사 도중 초안 GDD의 “행동 후보 추가”와 코드의 확정 대표 생활 차이를 보고했다. root가 수정한 현재 GDD §10.2를 다시 읽어 **D+1~3 최근 유효 선물의 생활 우선 표현**, 일정·보상·성장과 분리, 이전 정령에게서 효과 승계 없음, 나들이 우선을 확인했다. 현재 GDD와 코드의 이 차이는 해소되었다. 이 감사는 다른 문서를 고치지 않았다.
- **과거 나들이 연결:** Album은 `trip.SpiritId == memory.SpiritId` 및 귀환일(구 데이터는 시작일)로 조회하고 `ShowJourneyMemory(..., memory)`에 당시 외형을 전달한다. 재열람은 `ClaimTrip`을 호출하지 않는다. A-RETURN에서 실제 프로그램 UI의 앨범 재열람·복귀·저장 reload·무중복을 확인했고, 과거 외형 대조까지 PASS한 것은 아니다.
- **검증 도구 오류 구분:** 첫 선물 eval의 내부 NullReference를 게임 런타임 고장으로 확정하지 않았다. 후속 파일에서 검사 코드 reflection 문제와 재개 PASS를 확인했다. 바깥 MCP `success=true`만 읽어 첫 파일을 PASS로 세지도 않았다.

## 5. 완료를 막는 주요 잔여 범위

1. **실제 Android 통합:** SDK36.1 신규 라이선스 동의 대기. 호스트 전체 컴파일/테스트/APK를 통과한 뒤 계정/날짜/네트워크/Activity 복귀를 연결 기기에서 실행해야 한다. SDK·기기 부재를 코드 미구현으로 혼동하지 않되 완료로도 세지 않는다.
2. **남은 UI/기기 증거:** S01의 혼합/NONE UI 입력 조합, L01/L02의 세 진로·전체 성장/배웅 UI 완주, E03의 회전/백그라운드/실기기 입력 등이 남는다. S02의36경로108선택 실제 Sprite 연결·다음날 파일 재개방은 STORY-UI에서 확인했고 대표25장도 검토했으므로 같은 공백으로 반복 요구하지 않는다. 다섯 생활 장면·두 버튼·Play 설정 재시작·20회 닫기 callback·방석과 성장 v2의 세 해상도 배치·A02 세 빌드/브라우저 복원도 확인했다. 산책 가방 모션의 다른 두 비율과 다섯 행동 전 해상도 검증은 NOT RUN이다.
3. **재미·표현 평가:** 실제 사용자의 다시 방문/기록과 행동의 연결 이해/선택 의미/새 정령 동행 의사 평가는 **NOT RUN**이다. A02에서 발견한 흰 footer와 작은 메인 소품 라벨은 A03 세 해상도30장 검토에서 흰 띠 제거·16단위 라벨/활동 안내 분리로 개선을 확인했다. 결과를 읽은 뒤 정령 반응이 가려져 소모되던 문제도 CLOSE-A03의6경계에서 해결을 확인했다. 360의 작은 보조 글씨, 수첩·바람개비의 약한 그림자/받침에 따른 떠 보이는 인상, 편지에 우체통을 쓰는 의미 재사용은 남는다. 알파 잡점 정리와 실제 v2 actor 연결도 수행했지만 컨셉 원화 대비 통일감·가장자리의 기기 표현·움직임 자연스러움·재미 전체를 기능 테스트 PASS로 판정할 수 없다.

## 6. 감사 범위

- 프로젝트 `AGENTS.md`를 읽었다. 지정된 `../../Wiki/01 LLM Context.md`는 이 체크아웃에 없어 확인하지 못했다.
- 기준 문서는 [수락표](ForestPrototypeAcceptance.md), [v4](ForestDailyExperiencePlanV4.md), [GDD](../../Docs/TODO_SPIRIT/TODO-SPIRIT%20ProtoType%20기획서.md), [기능 체크리스트](ForestFeatureAndDeveloperChecklist.md), 실제 C#/Kotlin 소스와 위 증거 파일이다.
- 최초 감사는 이 문서만 생성했다. A02/A03 증거 갱신은 이 문서와 `ForestPrototypeAcceptance.md` 두 문서만 수정했다. 감사 담당자는 새 테스트·빌드·설치·라이선스 수락·Editor 상태 변경·사용자 저장/인증 조회를 하지 않았다. A03의1080×2340 PNG10장 육안 검토 기록은 별도 승인된 앞선 검토 작업이다.
