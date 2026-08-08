package com.example.todowithspirits.feature.signup

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todowithspirits.R
import com.example.todowithspirits.component.PlainTextField
import com.example.todowithspirits.component.SpiritsTodoPrimaryButton
import com.example.todowithspirits.component.TitleHeader
import com.example.todowithspirits.feature.signup.component.SignUpUiState
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
            SignUpStep.CREDENTIALS -> NicknameStep(
                uiState = uiState,
                onNicknameChange = signUpViewModel::setNickname,
                onSignUpClick = {
                    ToastUtil.show(context, "회원가입이 완료되었습니다")
                    onSignUpSuccess()
                }
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
                    onSignUpSuccess()
                }
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SpiritTodoTheme.color.dimColor),
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "loading")
                val angle by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "angle"
                )

                Image(
                    painter = painterResource(R.drawable.loading_gray),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .graphicsLayer { rotationZ = angle }
                )
            }
        }
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
