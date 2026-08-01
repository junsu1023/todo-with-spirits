package com.example.todowithspirits.feature.login

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
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
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.feature.login.component.LoginFailureDialog
import com.example.todowithspirits.feature.login.viewmodel.EmailLoginViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun EmailLoginScreen(
    emailLoginViewModel: EmailLoginViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onLoginSuccess: () -> Unit = {},
    onFindPasswordClick: () -> Unit = {}
) {
    val uiState by emailLoginViewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by emailLoginViewModel.isLoading.collectAsStateWithLifecycle()
    var loginErrorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(emailLoginViewModel.errorMsg) {

    }

    LaunchedEffect(emailLoginViewModel) {
        emailLoginViewModel.errorMsg.collect { message -> loginErrorMessage = message }
    }

    LaunchedEffect(Unit) {
        delay(50L.milliseconds)
        loginErrorMessage = "test"
    }

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
                leftIconRes = R.drawable.todo_back1,
                onLeftIconClick = onBack,
                title = stringResource(R.string.login)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .padding(top = 34.dp)
            ) {
                Text(
                    text = stringResource(R.string.email),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = SpiritTodoTheme.color.systemGrey,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(6.dp))

                PlainTextField(
                    value = uiState.email,
                    onValueChange = { emailLoginViewModel.setEmail(it) },
                    placeholder = stringResource(R.string.email_placeholder),
                    keyboardType = KeyboardType.Email
                )

                Spacer(Modifier.height(36.dp))

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
                    onValueChange = { emailLoginViewModel.setPassword(it) },
                    placeholder = stringResource(R.string.password_placeholder)
                )
            }

            Spacer(Modifier.height(32.dp))
            Spacer(Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SpiritsTodoPrimaryButton(
                    text = stringResource(R.string.login),
                    enabled = uiState.email.isNotBlank() && uiState.password.isNotBlank(),
                    onClick = { emailLoginViewModel.login(onSuccess = onLoginSuccess) }
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.find_password),
                    fontSize = 13.sp,
                    color = SpiritTodoTheme.color.systemGrey,
                    modifier = Modifier.noRippleClickable { onFindPasswordClick() }
                )

                Spacer(Modifier.height(63.dp))
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

        loginErrorMessage?.let { message ->
            LoginFailureDialog(
                message = message,
                onConfirm = { loginErrorMessage = null }
            )
        }
    }
}
