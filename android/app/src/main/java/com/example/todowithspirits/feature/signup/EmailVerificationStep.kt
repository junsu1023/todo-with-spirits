package com.example.todowithspirits.feature.signup

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.component.SpiritsTodoPrimaryButton
import com.example.todowithspirits.component.TopNaviBar
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.feature.signup.component.SignUpUiState
import com.example.todowithspirits.feature.signup.state.SignUpEvent
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun EmailVerificationStep(
    uiState: SignUpUiState,
    onEvent: (SignUpEvent) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val imeBottomDp = with(density) { WindowInsets.ime.getBottom(density).toDp() }
    val bottomSpacerHeight = (60.dp - imeBottomDp).coerceAtLeast(1.dp)
    val emailHighlightColor = SpiritTodoTheme.color.key02
    val descriptionSuffix = stringResource(R.string.verification_code_description_suffix)
    val descriptionBody = stringResource(R.string.verification_code_description_body)
    val description = remember(uiState.email, descriptionSuffix, descriptionBody) {
        buildAnnotatedString {
            withStyle(SpanStyle(color = emailHighlightColor)) {
                append(uiState.email)
            }
            append(descriptionSuffix)
            append("\n")
            append(descriptionBody)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        VerificationHeader(onBack = { onEvent(SignUpEvent.BackClicked) })

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(top = 21.dp)
        ) {
            Text(
                text = stringResource(R.string.verification_code_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.color.font01
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = description,
                fontSize = 14.sp,
                color = SpiritTodoTheme.color.fontGrey
            )

            Spacer(Modifier.height(26.dp))

            VerificationCodeField(
                value = uiState.verificationCode,
                onValueChange = { onEvent(SignUpEvent.VerificationCodeChanged(it)) },
                remainingSeconds = uiState.verificationRemainingSeconds,
                isError = uiState.fieldErrors["verificationCode"] != null
            )

            uiState.fieldErrors["verificationCode"]?.let { FieldHintText(text = it, isError = true) }
        }

        Spacer(Modifier.weight(1f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.verification_email_not_received),
                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
                color = SpiritTodoTheme.color.fontGrey
            )

            Row {
                Text(
                    text = stringResource(R.string.verification_email_resend_hint_prefix),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    color = SpiritTodoTheme.color.fontGrey
                )

                Spacer(Modifier.width(4.dp))

                Text(
                    text = stringResource(R.string.verification_email_resend_action),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SpiritTodoTheme.color.font01,
                    modifier = Modifier.noRippleClickable { onEvent(SignUpEvent.ResendClicked) }
                )
            }

            Spacer(Modifier.height(20.dp))

            SpiritsTodoPrimaryButton(
                text = stringResource(R.string.check),
                enabled = uiState.canVerifyCode,
                modifier = Modifier.imePadding(),
                onClick = {
                    focusManager.clearFocus()
                    onEvent(SignUpEvent.VerifyClicked)
                }
            )

            Spacer(Modifier.height(bottomSpacerHeight))
        }
    }
}

@Composable
private fun VerificationHeader(onBack: () -> Unit) {
    TopNaviBar(
        leftIconRes = R.drawable.widgetly_back_24,
        onLeftIconClick = onBack,
        title = stringResource(R.string.sign_up),
        modifier = Modifier.padding(horizontal = 17.dp)
    )
}

@Composable
fun VerificationCodeField(
    value: String,
    onValueChange: (String) -> Unit,
    remainingSeconds: Int,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    val textFieldState = rememberTextFieldState(value)
    var isFocused by remember { mutableStateOf(false) }
    val currentOnValueChange by rememberUpdatedState(onValueChange)

    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text.toString() }
            .collect { text -> currentOnValueChange(text) }
    }

    LaunchedEffect(value) {
        if (value != textFieldState.text.toString()) {
            textFieldState.setTextAndPlaceCursorAtEnd(value)
        }
    }

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .border(
                width = 1.dp,
                color = when {
                    isFocused -> SpiritTodoTheme.color.font01
                    isError -> SpiritTodoTheme.color.colorSub02
                    else -> SpiritTodoTheme.color.stroke01
                },
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            state = textFieldState,
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { isFocused = it.isFocused },
            textStyle = TextStyle(
                fontSize = 14.sp,
                color = SpiritTodoTheme.color.font01
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            lineLimits = TextFieldLineLimits.SingleLine,
            decorator = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = stringResource(R.string.verification_code_placeholder),
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = SpiritTodoTheme.color.fontGrey
                            )
                        )
                    }

                    innerTextField()
                }
            }
        )

        if (value.isNotEmpty()) {
            Image(
                painter = painterResource(R.drawable.widgetly_cross_20),
                contentDescription = null,
                modifier = Modifier.noRippleClickable { textFieldState.clearText() }
            )
        }

        Spacer(Modifier.width(10.dp))

        Text(
            text = "%02d:%02d".format(minutes, seconds),
            fontSize = 14.sp,
            color = SpiritTodoTheme.color.key01B
        )
    }
}