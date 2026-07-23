package com.example.todowithspirits.feature.signup

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
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
import com.example.todowithspirits.feature.signup.viewmodel.SignUpViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.util.ToastUtil

@Composable
fun SignUpScreen(
    signUpViewModel: SignUpViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onSignUpSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val fieldErrors by signUpViewModel.fieldErrors.collectAsStateWithLifecycle()
    val isLoading by signUpViewModel.isLoading.collectAsStateWithLifecycle()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var showValidationErrors by remember { mutableStateOf(false) }

    val isEmailBlank = email.isBlank()
    val isPasswordBlank = password.isBlank()
    val isConfirmPasswordBlank = confirmPassword.isBlank()
    val isPasswordMismatch = !isConfirmPasswordBlank && password != confirmPassword

    val emailError = if (showValidationErrors && isEmailBlank) {
        stringResource(R.string.required_field_error)
    } else {
        fieldErrors["email"]
    }

    val passwordError = if (showValidationErrors && isPasswordBlank) {
        stringResource(R.string.required_field_error)
    } else {
        fieldErrors["password"]
    }

    LaunchedEffect(signUpViewModel) {
        signUpViewModel.errorMsg.collect { message ->
            ToastUtil.show(context, message)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SpiritTodoTheme.color.surfaceColor6,
                        SpiritTodoTheme.color.surfaceColor1
                    )
                )
            )
    ) {
        Image(
            painter = painterResource(R.drawable.temp_spirit),
            contentDescription = null,
            modifier = Modifier
                .size(72.dp)
                .align(Alignment.TopEnd)
                .offset(x = 16.dp, y = 90.dp)
                .rotate(16f)
                .alpha(0.25f)
        )

        Image(
            painter = painterResource(R.drawable.temp_spirit),
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-26).dp, y = 30.dp)
                .rotate(-10f)
                .alpha(0.2f)
        )

        Column(modifier = Modifier.fillMaxSize()) {
            TitleHeader(
                leftIconRes = R.drawable.todo_back1,
                onLeftIconClick = onBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.sign_up),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = SpiritTodoTheme.color.mainTextAndStroke
                )

                Spacer(Modifier.height(36.dp))

                Text(
                    text = stringResource(R.string.email),
                    fontSize = 14.sp,
                    color = SpiritTodoTheme.color.systemGrey,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(4.dp))

                PlainTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = stringResource(R.string.email_placeholder),
                    keyboardType = KeyboardType.Email
                )

                if (emailError != null) {
                    FieldErrorText(emailError)
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.password),
                    fontSize = 14.sp,
                    color = SpiritTodoTheme.color.systemGrey,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(4.dp))

                PasswordField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = stringResource(R.string.new_password_placeholder)
                )

                if (passwordError != null) {
                    FieldErrorText(passwordError)
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.password_confirm),
                    fontSize = 14.sp,
                    color = SpiritTodoTheme.color.systemGrey,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(4.dp))

                PasswordField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = stringResource(R.string.password_confirm_placeholder)
                )

                if (showValidationErrors && isConfirmPasswordBlank) {
                    FieldErrorText(stringResource(R.string.required_field_error))
                } else if (isPasswordMismatch) {
                    FieldErrorText(stringResource(R.string.password_mismatch_error))
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.nickname_optional),
                    fontSize = 14.sp,
                    color = SpiritTodoTheme.color.systemGrey,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(4.dp))

                PlainTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    placeholder = stringResource(R.string.nickname_optional_placeholder)
                )

                if (fieldErrors["nickname"] != null) {
                    FieldErrorText(fieldErrors.getValue("nickname"))
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.nickname_auto_generate_hint),
                    fontSize = 12.sp,
                    color = SpiritTodoTheme.color.systemGrey
                )

                Spacer(Modifier.height(32.dp))

                SpiritsTodoPrimaryButton(
                    text = stringResource(R.string.sign_up_button),
                    onClick = {
                        showValidationErrors = true

                        if (!isEmailBlank && !isPasswordBlank && !isConfirmPasswordBlank && !isPasswordMismatch) {
                            signUpViewModel.signUp(
                                email = email,
                                password = password,
                                nickname = nickname.ifBlank { null },
                                onSuccess = {
                                    ToastUtil.show(context, "회원가입이 완료되었습니다")
                                    onSignUpSuccess()
                                }
                            )
                        }
                    }
                )

                Spacer(Modifier.height(40.dp))
            }
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
private fun FieldErrorText(message: String) {
    Spacer(Modifier.height(4.dp))

    Text(
        text = message,
        fontSize = 12.sp,
        color = SpiritTodoTheme.color.onSurfaceColor7
    )
}
