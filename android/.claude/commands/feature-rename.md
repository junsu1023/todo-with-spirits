---
name: feature-rename
description: :app 모듈의 feature 화면 이름을 바꿀 때 사용 (예: today → home). 패키지, 파일, 클래스, 네비게이션 라우트, 테스트, 문자열 리소스까지 일관되게 변경한다. "화면 이름 변경", "패키지명 변경", "~를 ~로 바꿔줘" 같은 요청에 사용.
---

# Feature 화면 이름 변경

`:app` 모듈 안에서만 진행한다. `:data`, `:domain`은 수정하지 않는다.

## 1. 영향 범위 조사

```bash
OLD=today; Old=Today
grep -rln "feature\.$OLD" app/src
grep -rnoE "\b[A-Za-z]*$Old[A-Za-z]*\b" app/src --include='*.kt' | sort | uniq -c
grep -rn "\b$OLD\b" app/src/main/res
```

결과를 두 그룹으로 나눈다.
- **변경**: 화면(feature)을 가리키는 이름 — 패키지, Screen/ViewModel/UiState 클래스, 라우트, 네비게이션 콜백, 화면 테스트, 탭 라벨 리소스
- **유지**: 같은 단어지만 다른 의미인 이름 — 예: `today`가 '오늘 날짜'를 뜻하는 `isToday`, `todayAchievementRate`, `TodayRewardCard`

애매한 이름은 표로 정리해 사용자에게 확인한다. 작업 문서(`.claude/tasks/*.md`)에 매핑이 있으면 그대로 따른다.

## 2. 파일/패키지 이동

- `git mv`로 디렉터리와 파일을 옮긴다. (main, test 모두)
- 이동한 모든 파일의 `package` 선언과, 프로젝트 전체의 `import`를 새 패키지로 바꾼다.

## 3. 식별자 변경

- 클래스/함수 이름, 파일 이름을 맞춘다. (`XxxScreen.kt` 안에 `XxxScreen`)
- `navigation/Screen.kt`의 객체와 route 문자열을 바꾼다.
- 라우트를 쓰는 곳(`SpiritsTodoNavigation.kt`, `SpiritsTodoApp.kt`, 하단 버튼, 시작 화면 분기)을 모두 바꾼다.
- 다른 화면의 콜백 파라미터 이름(`onReturnToXxx`, `navigateToXxx`)도 맞춘다.
- 문자열 리소스 키를 바꾸면 사용처를 함께 바꾼다. 다른 언어 `values-*`가 있으면 같이 바꾼다.
- 주석 속 화면 이름도 바꾼다.

## 4. 검증

```bash
grep -rn "feature\.$OLD\|${Old}Screen\|${Old}ViewModel\|${Old}UiState\|Screen\.$Old" app/src   # 결과 없어야 함
./gradlew :app:compileDebugKotlin
./gradlew :app:testDebugUnitTest
```

- 라우트 문자열이 바뀌면 저장된 back stack / deep link가 있는지 확인한다.

## 5. 보고

- 변경한 이름 매핑표, 의도적으로 유지한 이름과 이유
- 이동한 파일 목록
- 빌드/테스트 결과
