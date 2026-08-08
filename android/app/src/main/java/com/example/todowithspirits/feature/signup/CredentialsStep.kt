package com.example.todowithspirits.feature.signup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.component.PasswordField
import com.example.todowithspirits.component.PlainTextField
import com.example.todowithspirits.component.SpiritsTodoPrimaryButton
import com.example.todowithspirits.component.TitleHeader
import com.example.todowithspirits.feature.signup.component.SignUpUiState
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun CredentialsStep(
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

    val density = LocalDensity.current
    val imeBottomDp = with(density) { WindowInsets.ime.getBottom(density).toDp() }
    val bottomSpacerHeight = (60.dp - imeBottomDp).coerceAtLeast(1.dp)

    Column(
        modifier = Modifier.fillMaxSize()
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

            AnimatedVisibility(
                visible = uiState.password.length >= 8,
                enter = fadeIn(animationSpec = tween(700)) +
                        slideInVertically(
                            animationSpec = tween(700),
                            initialOffsetY = { -it / 2 }
                        ),
                exit = fadeOut(animationSpec = tween(700)) +
                        slideOutVertically(
                            animationSpec = tween(700),
                            targetOffsetY = { -it / 2 }
                        )
            ) {
                Column {
                    Spacer(Modifier.height(16.dp))

                    PasswordField(
                        value = uiState.confirmPassword,
                        onValueChange = onConfirmPasswordChange,
                        placeholder = stringResource(R.string.password_confirm_placeholder),
                        isError = uiState.fieldErrors["confirmPassword"] != null
                    )

                    uiState.fieldErrors["confirmPassword"]?.let { FieldHintText(text = it, isError = true) }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
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
                modifier = Modifier.imePadding(),
                onClick = {
                    focusManager.clearFocus()
                    onNext()
                }
            )

            Spacer(Modifier.height(bottomSpacerHeight))
        }
    }
}