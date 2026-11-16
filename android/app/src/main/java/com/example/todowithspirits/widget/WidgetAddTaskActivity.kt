package com.example.todowithspirits.widget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.lifecycleScope
import com.example.todowithspirits.feature.add.AddRoute
import com.example.todowithspirits.theme.SpiritTodoTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/*
위젯 '+' 버튼으로 여는 일정 추가 화면.
앱(MainActivity)과 다른 태스크(taskAffinity)로 떠서, 저장하거나 뒤로 가면 이 화면만 닫히고 홈 화면으로 돌아간다.
저장 결과는 TaskRefreshBus → 위젯 갱신으로 반영된다.
*/
@AndroidEntryPoint
class WidgetAddTaskActivity : ComponentActivity() {
    @Inject
    lateinit var todoWidgetRepository: TodoWidgetRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Splash를 거치지 않으므로 직접 세션을 복원해야 일정 추가 API가 AuthInterceptor에서 멈추지 않는다.
        lifecycleScope.launch { todoWidgetRepository.restoreSession() }

        setContent {
            SpiritTodoTheme {
                val focusManager = LocalFocusManager.current
                val keyboardController = LocalSoftwareKeyboardController.current

                // 앱(SpiritsTodoApp)과 같이 빈 곳을 누르면 키보드를 내린다.
                Scaffold(
                    modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            }
                        )
                    },
                    containerColor = SpiritTodoTheme.color.surfaceColor1
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        AddRoute(
                            taskId = null,
                            navigateBack = ::finish
                        )
                    }
                }
            }
        }
    }
}
