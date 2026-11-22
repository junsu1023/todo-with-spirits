package com.example.todowithspirits.feature.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todowithspirits.R
import com.example.todowithspirits.component.LoadingOverlay
import com.example.todowithspirits.component.PasswordField
import com.example.todowithspirits.component.PlainTextField
import com.example.todowithspirits.component.SpiritsTodoPrimaryButton
import com.example.todowithspirits.component.TopNaviBar
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.feature.login.component.LoginFailureDialog
import com.example.todowithspirits.feature.login.state.EmailLoginEffect
import com.example.todowithspirits.feature.login.state.EmailLoginEvent
import com.example.todowithspirits.feature.login.state.EmailLoginUiState
import com.example.todowithspirits.feature.login.viewmodel.EmailLoginViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.util.ObserveAsEffect

@Composable
fun EmailLoginRoute(
    navigateBack: () -> Unit,
    navigateToHome: () -> Unit,
    navigateToFindPassword: () -> Unit = {},
    emailLoginViewModel: EmailLoginViewModel = hiltViewModel()
) {
    val uiState by emailLoginViewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by emailLoginViewModel.isLoading.collectAsStateWithLifecycle()

    ObserveAsEffect(emailLoginViewModel.effect) { effect ->
        when (effect) {
            EmailLoginEffect.NavigateBack -> navigateBack()
            EmailLoginEffect.NavigateToHome -> navigateToHome()
            EmailLoginEffect.NavigateToFindPassword -> navigateToFindPassword()
        }
    }

    EmailLoginScreen(
        uiState = uiState,
        isLoading = isLoading,
        onEvent = emailLoginViewModel::onEvent
    )
}

@Composable
fun EmailLoginScreen(
    uiState: EmailLoginUiState,
    isLoading: Boolean,
    onEvent: (EmailLoginEvent) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .background(SpiritTodoTheme.color.white)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            EmailLoginHeader(onBack = { onEvent(EmailLoginEvent.BackClicked) })

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .padding(top = 34.dp)
            ) {
                Text(
                    text = stringResource(R.string.email),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = SpiritTodoTheme.color.font02,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(6.dp))

                PlainTextField(
                    value = uiState.email,
                    onValueChange = { onEvent(EmailLoginEvent.EmailChanged(it)) },
                    placeholder = stringResource(R.string.email_placeholder),
                    keyboardType = KeyboardType.Email
                )

                Spacer(Modifier.height(32.dp))

                Text(
                    text = stringResource(R.string.password),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = SpiritTodoTheme.color.font02,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(6.dp))

                PasswordField(
                    value = uiState.password,
                    onValueChange = { onEvent(EmailLoginEvent.PasswordChanged(it)) },
                    placeholder = stringResource(R.string.password_placeholder)
                )
            }

            Spacer(Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SpiritsTodoPrimaryButton(
                    text = stringResource(R.string.login),
                    enabled = uiState.canLogin,
                    onClick = { onEvent(EmailLoginEvent.LoginClicked) }
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.find_password),
                    fontSize = 14.sp,
                    color = SpiritTodoTheme.color.font01,
                    modifier = Modifier.noRippleClickable { onEvent(EmailLoginEvent.FindPasswordClicked) }
                )

                Spacer(Modifier.height(73.dp))
            }
        }

        LoadingOverlay(isLoading = isLoading)

        uiState.errorMessage?.let { message ->
            LoginFailureDialog(
                message = message,
                onConfirm = { onEvent(EmailLoginEvent.ErrorDialogDismissed) }
            )
        }
    }
}

@Composable
private fun EmailLoginHeader(onBack: () -> Unit) {
    TopNaviBar(
        leftIconRes = R.drawable.widgetly_back_24,
        onLeftIconClick = onBack,
        title = stringResource(R.string.login),
        modifier = Modifier.padding(horizontal = 17.dp)
    )
}
