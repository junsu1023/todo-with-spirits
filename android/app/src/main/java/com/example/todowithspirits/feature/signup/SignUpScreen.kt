package com.example.todowithspirits.feature.signup

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todowithspirits.R
import com.example.todowithspirits.component.LoadingOverlay
import com.example.todowithspirits.feature.signup.state.SignUpEffect
import com.example.todowithspirits.feature.signup.state.SignUpEvent
import com.example.todowithspirits.feature.signup.viewmodel.SignUpViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.util.ObserveAsEffect
import com.example.todowithspirits.util.ToastUtil

enum class SignUpStep { CREDENTIALS, EMAIL_VERIFICATION, NICKNAME }

@Composable
fun SignUpScreen(
    signUpViewModel: SignUpViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onSignUpSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by signUpViewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by signUpViewModel.isLoading.collectAsStateWithLifecycle()
    val resendSuccessMsg = stringResource(R.string.verification_email_resend_success)

    LaunchedEffect(signUpViewModel) {
        signUpViewModel.errorMsg.collect { message -> ToastUtil.show(context, message) }
    }

    ObserveAsEffect(signUpViewModel.effect) { effect ->
        when (effect) {
            SignUpEffect.NavigateBack -> onBack()
            SignUpEffect.ShowResendSucceeded -> ToastUtil.show(context, resendSuccessMsg)
            SignUpEffect.SignUpCompleted -> {
                ToastUtil.show(context, "회원가입이 완료되었습니다")
                onSignUpSuccess()
            }
        }
    }

    BackHandler(enabled = uiState.step != SignUpStep.CREDENTIALS) {
        signUpViewModel.onEvent(SignUpEvent.BackClicked)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .background(SpiritTodoTheme.color.white)
    ) {
        when(uiState.step) {
            SignUpStep.CREDENTIALS -> CredentialsStep(
                uiState = uiState,
                onEvent = signUpViewModel::onEvent
            )

            SignUpStep.EMAIL_VERIFICATION -> EmailVerificationStep(
                uiState = uiState,
                onEvent = signUpViewModel::onEvent
            )

            SignUpStep.NICKNAME -> NicknameStep(
                uiState = uiState,
                onEvent = signUpViewModel::onEvent
            )
        }

        LoadingOverlay(isLoading = isLoading)
    }
}
