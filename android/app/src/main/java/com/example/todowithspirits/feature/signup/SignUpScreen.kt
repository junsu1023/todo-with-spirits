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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todowithspirits.R
import com.example.todowithspirits.component.PasswordField
import com.example.todowithspirits.component.PlainTextField
import com.example.todowithspirits.component.SpiritsTodoPrimaryButton
import com.example.todowithspirits.component.TitleHeader
import com.example.todowithspirits.feature.signup.component.SignUpUiState
import com.example.todowithspirits.feature.signup.viewmodel.SignUpViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.util.ToastUtil

enum class SignUpStep { CREDENTIALS, NICKNAME }

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

    BackHandler(enabled = uiState.step == SignUpStep.NICKNAME) {
        signUpViewModel.goBackToCredentials()
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

            SignUpStep.NICKNAME -> NicknameStep(
                uiState = uiState,
                onNicknameChange = signUpViewModel::setNickname,
                onBack = signUpViewModel::goBackToCredentials,
                onSignUpClick = {
                    signUpViewModel.signUp(
                        onSuccess = {
                            ToastUtil.show(context, "회원가입이 완료되었습니다")
                            onSignUpSuccess()
                        }
                    )
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
private fun CredentialsStep(
    uiState: SignUpUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val canProceed = uiState.email.isNotBlank() &&
        uiState.password.isNotBlank() &&
        uiState.confirmPassword.isNotBlank() &&
        uiState.fieldErrors.isEmpty()

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        TitleHeader(
            leftIconRes = R.drawable.todo_back1,
            onLeftIconClick = onBack,
            title = stringResource(R.string.sign_up)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 34.dp)
        ) {
            Text(
                text = stringResource(R.string.email),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.color.systemGrey,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 2.dp)
            )

            Spacer(Modifier.height(6.dp))

            PlainTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                placeholder = "example@email.com",
                keyboardType = KeyboardType.Email,
                isError = uiState.fieldErrors["email"] != null
            )

            uiState.fieldErrors["email"]?.let { FieldHintText(text = it, isError = true) }

            Spacer(Modifier.height(if(uiState.fieldErrors["email"] != null) 46.dp else 26.dp))

            Text(
                text = stringResource(R.string.password),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.color.systemGrey,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(6.dp))

            PasswordField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                placeholder = stringResource(R.string.password_simple_placeholder),
                isError = uiState.fieldErrors["password"] != null
            )

            FieldHintText(
                text = uiState.fieldErrors["password"] ?: stringResource(R.string.password_format_hint),
                isError = uiState.fieldErrors["password"] != null
            )

            Spacer(Modifier.height(16.dp))

            PasswordField(
                value = uiState.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                placeholder = stringResource(R.string.password_confirm_placeholder),
                isError = uiState.fieldErrors["confirmPassword"] != null
            )

            uiState.fieldErrors["confirmPassword"]?.let { FieldHintText(text = it, isError = true) }
        }

        Spacer(Modifier.weight(1f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
        ) {
            SpiritsTodoPrimaryButton(
                text = stringResource(R.string.next),
                enabled = canProceed,
                onClick = {
                    focusManager.clearFocus()
                    onNext()
                }
            )

            Spacer(Modifier.height(60.dp))
        }
    }
}

@Composable
private fun NicknameStep(
    uiState: SignUpUiState,
    onNicknameChange: (String) -> Unit,
    onBack: () -> Unit,
    onSignUpClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        TitleHeader(
            leftIconRes = R.drawable.todo_back1,
            onLeftIconClick = onBack,
            title = stringResource(R.string.sign_up)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(top = 34.dp)
        ) {
            Text(
                text = stringResource(R.string.nickname_optional),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.color.systemGrey,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(6.dp))

            PlainTextField(
                value = uiState.nickname,
                onValueChange = onNicknameChange,
                placeholder = stringResource(R.string.nickname_optional_placeholder),
                isError = uiState.fieldErrors["nickname"] != null
            )

            FieldHintText(
                text = uiState.fieldErrors["nickname"] ?: stringResource(R.string.nickname_auto_generate_hint),
                isError = uiState.fieldErrors["nickname"] != null
            )
        }

        Spacer(Modifier.height(32.dp))
        Spacer(Modifier.weight(1f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
        ) {
            SpiritsTodoPrimaryButton(
                text = stringResource(R.string.check),
                onClick = onSignUpClick
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FieldHintText(text: String, isError: Boolean) {
    Spacer(Modifier.height(6.dp))

    Text(
        text = text,
        fontSize = 12.sp,
        color = if(isError) SpiritTodoTheme.color.onSurfaceColor7 else SpiritTodoTheme.color.systemGrey
    )
}
