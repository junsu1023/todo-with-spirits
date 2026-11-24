package com.example.todowithspirits.feature.setting.changepw

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todowithspirits.R
import com.example.todowithspirits.component.FieldHintText
import com.example.todowithspirits.component.LoadingOverlay
import com.example.todowithspirits.component.PasswordField
import com.example.todowithspirits.component.SpiritsTodoPrimaryButton
import com.example.todowithspirits.component.TopNaviBar
import com.example.todowithspirits.feature.setting.changepw.state.ChangePasswordEffect
import com.example.todowithspirits.feature.setting.changepw.state.ChangePasswordEvent
import com.example.todowithspirits.feature.setting.changepw.state.ChangePasswordUiState
import com.example.todowithspirits.feature.setting.changepw.viewmodel.ChangePasswordViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.util.ObserveAsEffect
import com.example.todowithspirits.util.ToastUtil

@Composable
fun ChangePasswordRoute(
    navigateBack: () -> Unit,
    changePasswordViewModel: ChangePasswordViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by changePasswordViewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by changePasswordViewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(changePasswordViewModel) {
        changePasswordViewModel.errorMsg.collect { message -> ToastUtil.show(context, message) }
    }

    ObserveAsEffect(changePasswordViewModel.effect) { effect ->
        when (effect) {
            ChangePasswordEffect.NavigateBack -> navigateBack()
        }
    }

    ChangePasswordScreen(
        uiState = uiState,
        isLoading = isLoading,
        onEvent = changePasswordViewModel::onEvent
    )
}

@Composable
fun ChangePasswordScreen(
    uiState: ChangePasswordUiState,
    isLoading: Boolean,
    onEvent: (ChangePasswordEvent) -> Unit
) {
    val focusManager = LocalFocusManager.current

    val density = LocalDensity.current
    val imeBottomDp = with(density) { WindowInsets.ime.getBottom(density).toDp() }
    val bottomSpacerHeight = (60.dp - imeBottomDp).coerceAtLeast(1.dp)

    BackHandler { onEvent(ChangePasswordEvent.BackClicked) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .background(SpiritTodoTheme.color.back01)
    ) {
        TopNaviBar(
            leftIconRes = R.drawable.widgetly_back_24,
            onLeftIconClick = { onEvent(ChangePasswordEvent.BackClicked) },
            title = stringResource(R.string.change_password),
            modifier = Modifier.padding(horizontal = 10.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .padding(top = 34.dp)
        ) {
            FieldLabel(text = stringResource(R.string.current_password))

            Spacer(Modifier.height(6.dp))

            PasswordField(
                value = uiState.currentPassword,
                onValueChange = { onEvent(ChangePasswordEvent.CurrentPasswordChanged(it)) },
                placeholder = stringResource(R.string.password_placeholder),
                modifier = Modifier.background(SpiritTodoTheme.color.white, RoundedCornerShape(6.dp)),
                isError = uiState.fieldErrors["currentPassword"] != null
            )

            uiState.fieldErrors["currentPassword"]?.let { FieldHintText(text = it, isError = true) }

            Spacer(Modifier.height(42.dp))

            FieldLabel(text = stringResource(R.string.new_password))

            Spacer(Modifier.height(6.dp))

            PasswordField(
                value = uiState.newPassword,
                onValueChange = { onEvent(ChangePasswordEvent.NewPasswordChanged(it)) },
                placeholder = stringResource(R.string.password_placeholder),
                modifier = Modifier.background(SpiritTodoTheme.color.white, RoundedCornerShape(6.dp)),
                isError = uiState.fieldErrors["newPassword"] != null
            )

            FieldHintText(
                text = uiState.fieldErrors["newPassword"] ?: stringResource(R.string.password_format_hint),
                isError = uiState.fieldErrors["newPassword"] != null
            )

            AnimatedVisibility(
                visible = uiState.newPassword.length >= 2,
                enter = fadeIn(animationSpec = tween(700)) +
                        slideInVertically(
                            animationSpec = tween(700),
                            initialOffsetY = { -it / 2 }
                        ),
                exit = fadeOut(animationSpec = tween(700)) +
                        slideOutVertically(
                            animationSpec = tween(700),
                            targetOffsetY = { -it / 2 }
                        )
            ) {
                Column {
                    Spacer(Modifier.height(10.dp))

                    PasswordField(
                        value = uiState.confirmPassword,
                        onValueChange = { onEvent(ChangePasswordEvent.ConfirmPasswordChanged(it)) },
                        placeholder = stringResource(R.string.password_confirm_placeholder),
                        modifier = Modifier.background(SpiritTodoTheme.color.white, RoundedCornerShape(6.dp)),
                        isError = uiState.fieldErrors["confirmPassword"] != null
                    )

                    uiState.fieldErrors["confirmPassword"]?.let { FieldHintText(text = it, isError = true) }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
        ) {
            SpiritsTodoPrimaryButton(
                text = stringResource(R.string.next),
                enabled = uiState.canProceed,
                modifier = Modifier.imePadding(),
                onClick = {
                    focusManager.clearFocus()
                    onEvent(ChangePasswordEvent.NextClicked)
                }
            )

            Spacer(Modifier.height(bottomSpacerHeight))
        }
    }

    LoadingOverlay(isLoading = isLoading)
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = SpiritTodoTheme.color.font02,
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp)
    )
}
