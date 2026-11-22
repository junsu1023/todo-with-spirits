package com.example.todowithspirits.feature.signup

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.component.PlainTextField
import com.example.todowithspirits.component.SpiritsTodoPrimaryButton
import com.example.todowithspirits.feature.signup.component.SignUpUiState
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun NicknameStep(
    uiState: SignUpUiState,
    onNicknameChange: (String) -> Unit,
    onSignUpClick: () -> Unit
) {
    val density = LocalDensity.current
    val statusBarTopDp = with(density) { WindowInsets.statusBars.getTop(density).toDp() }
    val imeBottomDp = with(density) { WindowInsets.ime.getBottom(density).toDp() }
    val isKeyboardVisible = imeBottomDp > 0.dp

    val buttonBottomMargin by animateDpAsState(
        targetValue = if(isKeyboardVisible) 0.dp else 24.dp,
        label = "buttonBottomMargin"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = statusBarTopDp)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))

        Column(
            modifier = Modifier.padding(horizontal = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.temp_signup),
                contentDescription = null,
                modifier = Modifier.size(120.dp)
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.signup_welcome_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.color.font01,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.signup_welcome_subtitle),
                fontSize = 14.sp,
                color = SpiritTodoTheme.color.fontGrey,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(26.dp))

            PlainTextField(
                value = uiState.nickname,
                onValueChange = onNicknameChange,
                placeholder = stringResource(R.string.nickname_optional_placeholder),
                isError = uiState.fieldErrors["nickname"] != null
            )
        }

        Spacer(Modifier.weight(2f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
        ) {
            SpiritsTodoPrimaryButton(
                text = stringResource(R.string.check),
                onClick = onSignUpClick
            )

            Spacer(Modifier.height(buttonBottomMargin))
        }
    }
}
