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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.SocialProvider
import com.example.todowithspirits.BuildConfig
import com.example.todowithspirits.R
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.feature.login.viewmodel.LoginViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.util.ToastUtil
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = hiltViewModel(),
    onSignUpClick: () -> Unit,
    onEmailLoginClick: () -> Unit,
    onLoginSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isLoading by loginViewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(loginViewModel) {
        loginViewModel.errorMsg.collect { message -> ToastUtil.show(context, message) }
    }

    val onKakaoLoginClick: () -> Unit = {
        scope.launch {
            runCatching { KakaoLoginClient.login(context) }
                .onSuccess { kakaoUser ->
                    loginViewModel.socialLogin(
                        provider = SocialProvider.KAKAO,
                        providerUserId = kakaoUser.kakaoUserId,
                        providerAccessToken = kakaoUser.accessToken,
                        email = kakaoUser.email,
                        onSuccess = onLoginSuccess
                    )
                }
                .onFailure { error ->
                    ToastUtil.show(context, error.localizedMessage ?: "카카오 로그인에 실패했습니다")
                }
        }
    }

    val onGoogleLoginClick: () -> Unit = {
        scope.launch {
            runCatching { GoogleLoginClient.login(context, BuildConfig.GOOGLE_WEB_CLIENT_ID) }
                .onSuccess { googleUser ->
                    loginViewModel.socialLogin(
                        provider = SocialProvider.GOOGLE,
                        providerUserId = googleUser.googleUserId,
                        providerAccessToken = googleUser.idToken,
                        email = googleUser.email,
                        onSuccess = onLoginSuccess
                    )
                }
                .onFailure { error ->
                    ToastUtil.show(context, error.localizedMessage ?: "구글 로그인에 실패했습니다")
                }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpiritTodoTheme.color.surfaceColor1)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.4f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(SpiritTodoTheme.color.surfaceColor15),
                    contentAlignment = Alignment.Center
                ) { }

                Spacer(Modifier.height(15.dp))

                Text(
                    text = stringResource(R.string.app_name),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SpiritTodoTheme.color.mainTextAndStroke
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
                    .padding(horizontal = 18.dp)
                    .padding(bottom = 94.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SocialLoginButton(
                    text = stringResource(R.string.login_with_kakao),
                    iconRes = R.drawable.todo_kakao,
                    backgroundColor = SpiritTodoTheme.color.kakaoBg,
                    textColor = SpiritTodoTheme.color.kakaoText,
                    borderColor = null,
                    onClick = onKakaoLoginClick
                )

                Spacer(modifier = Modifier.height(14.dp))

                SocialLoginButton(
                    text = stringResource(R.string.login_with_google),
                    iconRes = R.drawable.todo_google,
                    backgroundColor = SpiritTodoTheme.color.surfaceColor1,
                    textColor = SpiritTodoTheme.color.googleText,
                    borderColor = SpiritTodoTheme.color.systemGrey,
                    onClick = onGoogleLoginClick
                )

                Spacer(modifier = Modifier.height(14.dp))

                SocialLoginButton(
                    text = stringResource(R.string.login_with_email),
                    iconRes = R.drawable.todo_email,
                    backgroundColor = SpiritTodoTheme.color.mainArea,
                    textColor = SpiritTodoTheme.color.surfaceColor1,
                    borderColor = null,
                    onClick = onEmailLoginClick
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = stringResource(R.string.no_account_question),
                    fontSize = 13.sp,
                    color = SpiritTodoTheme.color.systemGrey
                )

                Spacer(Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .background(
                            color = SpiritTodoTheme.color.systemBackground,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .noRippleClickable(onClick = onSignUpClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.sign_up_with_email),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = SpiritTodoTheme.color.systemGrey
                    )
                }
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
            .height(44.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(6.dp))
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
                contentDescription = null
            )

            Spacer(Modifier.width(if(iconRes == R.drawable.todo_google) 10.dp else 8.dp))

            Text(
                text = text,
                fontSize = if(iconRes == R.drawable.todo_google) 14.sp else 15.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}
