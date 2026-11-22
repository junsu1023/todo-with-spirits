package com.example.todowithspirits.feature.login

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todowithspirits.R
import com.example.todowithspirits.client.SocialAuthClient
import com.example.todowithspirits.component.LoadingOverlay
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.feature.login.state.LoginEffect
import com.example.todowithspirits.feature.login.state.LoginEvent
import com.example.todowithspirits.feature.login.state.LoginUiState
import com.example.todowithspirits.feature.login.viewmodel.LoginViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.util.ObserveAsEffect
import com.example.todowithspirits.util.ToastUtil
import kotlinx.coroutines.launch

private const val SOCIAL_LOGIN_TOP_RATIO = 388f / 800f
private const val SIGN_UP_BOTTOM_RATIO = 102f / 800f

@Composable
fun LoginRoute(
    navigateToHome: () -> Unit,
    navigateToEmailLogin: () -> Unit,
    navigateToSignUp: () -> Unit,
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by loginViewModel.isLoading.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(loginViewModel) {
        loginViewModel.errorMsg.collect { message -> ToastUtil.show(context, message) }
    }

    ObserveAsEffect(loginViewModel.effect) { effect ->
        when (effect) {
            // 수집 블록은 STOPPED에서 취소된다. 카카오톡으로 넘어가도 인증이 이어지도록 화면 scope에서 실행한다.
            is LoginEffect.StartSocialAuth -> scope.launch {
                val result = runCatching { SocialAuthClient.login(context, effect.provider) }
                loginViewModel.onEvent(LoginEvent.SocialAuthFinished(effect.provider, result))
            }
            LoginEffect.NavigateToHome -> navigateToHome()
            LoginEffect.NavigateToEmailLogin -> navigateToEmailLogin()
            LoginEffect.NavigateToSignUp -> navigateToSignUp()
        }
    }

    LoginScreen(
        uiState = uiState,
        isLoading = isLoading,
        onEvent = loginViewModel::onEvent
    )
}

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    isLoading: Boolean,
    onEvent: (LoginEvent) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LoginContent(
            onKakaoLoginClick = { onEvent(LoginEvent.KakaoLoginClicked) },
            onGoogleLoginClick = { onEvent(LoginEvent.GoogleLoginClicked) },
            onEmailLoginClick = { onEvent(LoginEvent.EmailLoginClicked) },
            onSignUpClick = { onEvent(LoginEvent.SignUpClicked) }
        )

        LoadingOverlay(isLoading = isLoading || uiState.isAuthenticating)
    }
}

@Composable
private fun LoginContent(
    onKakaoLoginClick: () -> Unit,
    onGoogleLoginClick: () -> Unit,
    onEmailLoginClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(SpiritTodoTheme.color.white)
    ) {
        val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val navigationBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val screenHeight = maxHeight + statusBarHeight

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BrandArea(modifier = Modifier.height(screenHeight * SOCIAL_LOGIN_TOP_RATIO - statusBarHeight))

            SocialLoginArea(
                onKakaoLoginClick = onKakaoLoginClick,
                onGoogleLoginClick = onGoogleLoginClick,
                onEmailLoginClick = onEmailLoginClick
            )

            Spacer(Modifier.weight(1f))

            SignUpArea(onSignUpClick = onSignUpClick)

            Spacer(Modifier.height(maxOf(screenHeight * SIGN_UP_BOTTOM_RATIO, navigationBarHeight)))
        }
    }
}

@Composable
private fun BrandArea(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 위아래 여백은 피그마의 137 : 66 비율로 나눈다.
        Spacer(Modifier.weight(137f))

        Box(
            modifier = Modifier
                .size(100.dp)
                .background(SpiritTodoTheme.color.temporaryColor1)
        )

        Spacer(Modifier.height(15.dp))

        Text(
            text = stringResource(R.string.app_name),
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = SpiritTodoTheme.color.font01
        )

        Spacer(Modifier.weight(66f))
    }
}

@Composable
private fun SocialLoginArea(
    onKakaoLoginClick: () -> Unit,
    onGoogleLoginClick: () -> Unit,
    onEmailLoginClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SocialLoginButton(
            text = stringResource(R.string.login_with_kakao),
            iconRes = R.drawable.widgetly_kakao,
            backgroundColor = SpiritTodoTheme.color.kakaoBg,
            textColor = SpiritTodoTheme.color.kakaoText,
            iconSpacing = 8.dp,
            fontWeight = FontWeight.SemiBold,
            onClick = onKakaoLoginClick
        )

        SocialLoginButton(
            text = stringResource(R.string.login_with_google),
            iconRes = R.drawable.widgetly_google,
            backgroundColor = SpiritTodoTheme.color.white,
            textColor = SpiritTodoTheme.color.googleText,
            borderColor = SpiritTodoTheme.color.stroke01,
            fontSize = 14.sp,
            onClick = onGoogleLoginClick
        )

        SocialLoginButton(
            text = stringResource(R.string.login_with_email),
            iconRes = R.drawable.widgetly_email,
            backgroundColor = SpiritTodoTheme.color.key02,
            textColor = SpiritTodoTheme.color.white,
            onClick = onEmailLoginClick
        )
    }
}

@Composable
private fun SignUpArea(onSignUpClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.no_account_question),
            fontSize = 14.sp,
            color = SpiritTodoTheme.color.fontGrey
        )

        Spacer(Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(SpiritTodoTheme.color.back02, RoundedCornerShape(6.dp))
                .noRippleClickable(onClick = onSignUpClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.sign_up_with_email),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.color.font01
            )
        }
    }
}

@Composable
private fun SocialLoginButton(
    text: String,
    @DrawableRes iconRes: Int,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    borderColor: Color? = null,
    iconSpacing: Dp = 10.dp,
    fontSize: TextUnit = 15.sp,
    fontWeight: FontWeight = FontWeight.Medium
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(backgroundColor, RoundedCornerShape(6.dp))
            .then(
                if (borderColor != null) {
                    Modifier.border(1.dp, borderColor, RoundedCornerShape(6.dp))
                } else {
                    Modifier
                }
            )
            .noRippleClickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(iconSpacing, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null
        )

        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = textColor
        )
    }
}
