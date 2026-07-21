package com.example.todowithspirits.feature.login

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todowithspirits.R
import com.example.todowithspirits.component.PasswordField
import com.example.todowithspirits.component.SpiritsTodoPrimaryButton
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.feature.login.viewmodel.LoginViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onSignUpClick: () -> Unit = {},
    onKakaoLoginClick: () -> Unit = {},
    onGoogleLoginClick: () -> Unit = {}
) {
    val email by loginViewModel.email.collectAsStateWithLifecycle()
    val password by loginViewModel.password.collectAsStateWithLifecycle()
    val isLoading by loginViewModel.isLoading.collectAsStateWithLifecycle()

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
                .size(90.dp)
                .align(Alignment.TopStart)
                .offset(x = (-24).dp, y = 36.dp)
                .rotate(-18f)
                .alpha(0.35f)
        )

        Image(
            painter = painterResource(R.drawable.temp_spirit),
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.TopEnd)
                .offset(x = 20.dp, y = 110.dp)
                .rotate(22f)
                .alpha(0.25f)
        )

        Image(
            painter = painterResource(R.drawable.temp_spirit),
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-30).dp, y = 40.dp)
                .rotate(12f)
                .alpha(0.2f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(72.dp))

            Image(
                painter = painterResource(R.drawable.temp_spirit),
                contentDescription = null,
                modifier = Modifier.size(72.dp)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.app_name),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = SpiritTodoTheme.color.mainTextAndStroke
            )

            Spacer(Modifier.height(48.dp))

            Text(
                text = stringResource(R.string.email),
                fontSize = 14.sp,
                color = SpiritTodoTheme.color.systemGrey,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(4.dp))

            EmailField(
                value = email,
                onValueChange = { loginViewModel.setEmail(it) },
                placeholder = stringResource(R.string.email_placeholder)
            )

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
                onValueChange = { loginViewModel.setPassword(it) },
                placeholder = stringResource(R.string.password_placeholder)
            )

            Spacer(Modifier.height(24.dp))

            SpiritsTodoPrimaryButton(
                text = stringResource(R.string.login),
                onClick = { loginViewModel.login(onSuccess = onLoginSuccess) }
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.sign_up),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.color.mainTextAndStroke,
                modifier = Modifier.noRippleClickable { onSignUpClick() }
            )

            Spacer(Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = SpiritTodoTheme.color.systemArea)

                Text(
                    text = stringResource(R.string.or_divider),
                    fontSize = 12.sp,
                    color = SpiritTodoTheme.color.systemGrey,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                HorizontalDivider(modifier = Modifier.weight(1f), color = SpiritTodoTheme.color.systemArea)
            }

            Spacer(Modifier.height(24.dp))

            SocialLoginButton(
                text = stringResource(R.string.login_with_kakao),
                iconRes = R.drawable.ic_kakao_logo,
                backgroundColor = Color(0xFFFEE500),
                textColor = Color(0xFF191600),
                borderColor = null,
                onClick = onKakaoLoginClick
            )

            Spacer(Modifier.height(12.dp))

            SocialLoginButton(
                text = stringResource(R.string.login_with_google),
                iconRes = R.drawable.ic_google_logo,
                backgroundColor = Color.White,
                textColor = Color(0xFF1F1F1F),
                borderColor = Color(0xFFDADCE0),
                onClick = onGoogleLoginClick
            )

            Spacer(Modifier.height(40.dp))
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
private fun EmailField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    var isFocused by remember { mutableStateOf(false) }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        textStyle = TextStyle(
            fontSize = 16.sp,
            color = SpiritTodoTheme.color.todoTextMain
        ),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = SpiritTodoTheme.color.systemGrey
                        )
                    )
                }

                innerTextField()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = 1.dp,
                color = if (isFocused) SpiritTodoTheme.color.mainTextAndStroke else SpiritTodoTheme.color.systemArea,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 14.dp)
            .padding(top = 15.dp, bottom = 12.dp)
    )
}

@Composable
private fun SocialLoginButton(
    text: String,
    iconRes: Int,
    backgroundColor: Color,
    textColor: Color,
    borderColor: Color?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(backgroundColor, RoundedCornerShape(6.dp))
            .then(
                if (borderColor != null) {
                    Modifier.border(1.dp, borderColor, RoundedCornerShape(6.dp))
                } else {
                    Modifier
                }
            )
            .noRippleClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}
