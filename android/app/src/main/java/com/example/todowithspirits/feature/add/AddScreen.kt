package com.example.todowithspirits.feature.add

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todowithspirits.R
import com.example.todowithspirits.component.LoadingOverlay
import com.example.todowithspirits.component.SpiritsTodoPrimaryButton
import com.example.todowithspirits.component.TitleHeader
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.feature.add.component.TaskTitleField
import com.example.todowithspirits.feature.add.state.AddEffect
import com.example.todowithspirits.feature.add.state.AddEvent
import com.example.todowithspirits.feature.add.state.AddUiState
import com.example.todowithspirits.feature.add.viewmodel.AddViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.util.ObserveAsEffect
import com.example.todowithspirits.util.ToastUtil

private val AddHorizontalMargin = 18.dp

@Composable
fun AddRoute(
    taskId: Long?,
    navigateBack: () -> Unit,
    addViewModel: AddViewModel = hiltViewModel()
) {
    val uiState by addViewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by addViewModel.isLoading.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(taskId) {
        taskId?.let { addViewModel.loadTaskForEdit(it) }
    }

    LaunchedEffect(addViewModel) {
        addViewModel.errorMsg.collect { message -> ToastUtil.show(context, message) }
    }

    ObserveAsEffect(addViewModel.effect) { effect ->
        when (effect) {
            AddEffect.NavigateBack -> navigateBack()
        }
    }

    AddScreen(
        uiState = uiState,
        isLoading = isLoading,
        onEvent = addViewModel::onEvent
    )
}

@Composable
fun AddScreen(
    uiState: AddUiState,
    isLoading: Boolean,
    onEvent: (AddEvent) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpiritTodoTheme.color.surfaceColor1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            TitleHeader(
                title = stringResource(if (uiState.isEditMode) R.string.edit_todo else R.string.add_todo),
                leftComposable = {
                    Image(
                        painter = painterResource(R.drawable.todo_back1),
                        contentDescription = null,
                        modifier = Modifier
                            .size(26.dp)
                            .noRippleClickable { onEvent(AddEvent.BackClicked) }
                    )
                }
            )

            // 시간 휠·캘린더가 펼쳐지면 길어지므로 스크롤한다. 확인 버튼은 아래에 고정되고 키보드 위로 따라 올라간다.
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AddHorizontalMargin)
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                TaskTitleField(
                    title = uiState.title,
                    onTitleChange = { onEvent(AddEvent.TitleChanged(it)) }
                )

                Spacer(modifier = Modifier.height(22.dp))

                TodoForm(uiState = uiState, onEvent = onEvent)

                Spacer(modifier = Modifier.height(24.dp))
            }

            SpiritsTodoPrimaryButton(
                text = stringResource(R.string.check),
                onClick = { onEvent(AddEvent.ConfirmClicked) },
                enabled = uiState.canConfirm,
                containerColor = SpiritTodoTheme.color.pointPink,
                modifier = Modifier.padding(start = AddHorizontalMargin, end = AddHorizontalMargin, bottom = 24.dp)
            )
        }

        LoadingOverlay(isLoading = isLoading)
    }
}
