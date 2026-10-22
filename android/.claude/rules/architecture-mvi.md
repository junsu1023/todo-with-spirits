---
paths:
  - "app/src/main/java/**/feature/**"
  - "app/src/test/java/**/feature/**"
---

# 화면 구조: MVVM + MVI 단방향 이벤트 흐름

기존 MVVM(ViewModel + `StateFlow<UiState>`)에 **Event**, **Effect**를 더해 단방향으로 흐르게 한다.

```
사용자 행동 ─ Event ─▶ ViewModel.onEvent ─▶ UiState 갱신 ─▶ Screen 다시 그림
                                          └▶ Effect 전송 ─▶ Route가 1회 처리 (이동, 토스트 등)
```

| 구성 | 역할 | 형태 |
| --- | --- | --- |
| UiState | 화면에 그릴 상태 전체 | `@Immutable data class`, 모든 필드 `val`, `StateFlow`로 노출 |
| Event | 사용자 행동 / 화면 진입 같은 입력 | `sealed interface XxxEvent` |
| Effect | 한 번만 처리할 출력 (네비게이션, 토스트, 시트 닫기 등) | `sealed interface XxxEffect`, `Channel` → `Flow`로 노출 |

## 적용 범위

- 리팩토링하면서 **수정하는 화면부터** 적용한다. 손대지 않는 화면을 일부러 바꾸지 않는다.
- `:core`의 `BaseViewModel`은 수정하지 않는다. 로딩(`isLoading`)과 공통 에러(`errorMsg`)는 지금처럼 `BaseViewModel`을 그대로 쓴다.

## 파일 구성

```
feature/<name>/
├── <Name>Screen.kt          # <Name>Route + <Name>Screen
├── state/
│   ├── <Name>UiState.kt
│   ├── <Name>Event.kt
│   └── <Name>Effect.kt
├── viewmodel/<Name>ViewModel.kt
└── component/
```

## 규칙

### Event
- Composable은 ViewModel 함수를 직접 부르지 않고 `onEvent(XxxEvent)`만 호출한다.
- 이름은 일어난 일을 과거형으로 쓴다. 예: `SettingClicked`, `TaskCompleted(taskId, date)`, `DateSelected(date)`.
- ViewModel의 공개 진입점은 `fun onEvent(event: XxxEvent)` 하나다. 처리 함수는 `private`으로 둔다.

### UiState
- 상태는 ViewModel 안에서 `_uiState.update { it.copy(...) }`로만 바꾼다.
- 화면에서 계산하던 파생 값(개수, 포맷된 문자열 등)도 가능하면 UiState에 담아 Screen을 단순하게 둔다.

### Effect
- 네비게이션, 토스트처럼 **다시 그려도 반복되면 안 되는 동작**만 Effect로 보낸다. 화면에 계속 보여야 하는 값은 UiState에 둔다.
- `Channel(Channel.BUFFERED)` + `receiveAsFlow()`로 노출한다. (`SharedFlow`는 수집자가 없을 때 유실된다)
- 수집은 Route에서 `ObserveAsEffect`(`util/ObserveAsEffect.kt`)로 한다. 생명주기가 `STARTED`일 때만 처리한다.

### Route / Screen 분리
- `XxxRoute`: `hiltViewModel()`, 상태 수집, Effect 처리, 네비게이션 람다를 받는다. 네비게이션 그래프에서는 Route를 호출한다.
- `XxxScreen(uiState, onEvent)`: ViewModel을 모르는 stateless Composable. `@Preview` 대상이다.

### Composable 로컬 상태로 두는 것
- ViewModel이 알 필요 없는 **순수 화면 표현 상태**는 `remember`로 Composable 안에 둔다. 예: 애니메이션 위치, 바운스, 스크롤 상태, 일시적인 펼침 애니메이션 진행도.
- 판단 기준: 화면을 다시 만들었을 때(회전 등) 유지돼야 하거나 데이터 요청·저장에 영향을 주면 UiState / Event로 올린다.

### 테스트
- ViewModel 테스트는 `onEvent`를 호출하고 `uiState.value` 또는 `effect.first()`를 검증한다.

## 템플릿

```kotlin
// state/HomeEvent.kt
sealed interface HomeEvent {
    data object SettingClicked : HomeEvent
    data class TaskCompleted(val taskId: Long, val date: LocalDate) : HomeEvent
}

// state/HomeEffect.kt
sealed interface HomeEffect {
    data object NavigateToMyPage : HomeEffect
    data class ShowToast(val message: String) : HomeEffect
}

// viewmodel/HomeViewModel.kt
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val completeTaskUseCase: CompleteTaskUseCase
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)
    val effect: Flow<HomeEffect> = _effect.receiveAsFlow()

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.SettingClicked -> sendEffect(HomeEffect.NavigateToMyPage)
            is HomeEvent.TaskCompleted -> completeTask(event.taskId, event.date)
        }
    }

    private fun completeTask(taskId: Long, date: LocalDate) { /* _uiState.update { ... } */ }

    private fun sendEffect(effect: HomeEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}

// HomeScreen.kt
@Composable
fun HomeRoute(
    navigateToMyPage: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ObserveAsEffect(viewModel.effect) { effect ->
        when (effect) {
            HomeEffect.NavigateToMyPage -> navigateToMyPage()
            is HomeEffect.ShowToast -> ToastUtil.show(context, effect.message)
        }
    }

    HomeScreen(uiState = uiState, isLoading = isLoading, onEvent = viewModel::onEvent)
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    isLoading: Boolean,
    onEvent: (HomeEvent) -> Unit
) { /* ... */ }

// util/ObserveAsEffect.kt (처음 적용하는 화면에서 생성)
@Composable
fun <T> ObserveAsEffect(flow: Flow<T>, onEffect: suspend (T) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnEffect by rememberUpdatedState(onEffect)

    LaunchedEffect(flow, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                flow.collect { currentOnEffect(it) }
            }
        }
    }
}
```
