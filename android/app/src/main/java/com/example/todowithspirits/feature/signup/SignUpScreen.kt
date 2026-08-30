package com.example.todowithspirits.feature.signup

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todowithspirits.component.LoadingOverlay
import com.example.todowithspirits.feature.signup.viewmodel.SignUpViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme
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

    LaunchedEffect(signUpViewModel) {
        signUpViewModel.errorMsg.collect { message -> ToastUtil.show(context, message) }
    }

    BackHandler(enabled = uiState.step != SignUpStep.CREDENTIALS) {
        when (uiState.step) {
            SignUpStep.EMAIL_VERIFICATION -> signUpViewModel.goBackToCredentials()
            SignUpStep.NICKNAME -> signUpViewModel.goBackToEmailVerification()
            SignUpStep.CREDENTIALS -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpiritTodoTheme.color.surfaceColor1)
    ) {
        when(uiState.step) {
            SignUpStep.CREDENTIALS -> CredentialsStep(
                uiState = uiState,
                onEmailChange = signUpViewModel::setEmail,
                onPasswordChange = signUpViewModel::setPassword,
                onConfirmPasswordChange = signUpViewModel::setConfirmPassword,
                onBack = onBack,
                onNext = signUpViewModel::validateCredentials
            )

            SignUpStep.EMAIL_VERIFICATION -> EmailVerificationStep(
                uiState = uiState,
                onCodeChange = signUpViewModel::setVerificationCode,
                onBack = signUpViewModel::goBackToCredentials,
                onVerify = signUpViewModel::verifyEmailCode
            )

            SignUpStep.NICKNAME -> NicknameStep(
                uiState = uiState,
                onNicknameChange = signUpViewModel::setNickname,
                onSignUpClick = {
                    ToastUtil.show(context, "회원가입이 완료되었습니다")
                    /* TODO 프로필 수정 api 구현 이후 추가하기 > 닉네임 수정 용 */
                    onSignUpSuccess()
                }
            )
        }

        LoadingOverlay(isLoading = isLoading)
    }
}

@Composable
fun FieldHintText(text: String, isError: Boolean) {
    Spacer(Modifier.height(6.dp))

    Text(
        text = text,
        fontSize = 12.sp,
        color = if(isError) SpiritTodoTheme.color.systemRed else SpiritTodoTheme.color.systemGrey
    )
}
