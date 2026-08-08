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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.ColorFilter
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
import com.example.todowithspirits.component.TitleHeader
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.feature.signup.component.SignUpUiState
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun EmailVerificationStep(
    uiState: SignUpUiState,
    onCodeChange: (String) -> Unit,
    onBack: () -> Unit,
    onVerify: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    val density = LocalDensity.current
    val imeBottomDp = with(density) { WindowInsets.ime.getBottom(density).toDp() }
    val bottomSpacerHeight = (60.dp - imeBottomDp).coerceAtLeast(1.dp)

    val emailHighlightColor = SpiritTodoTheme.color.mainTextAndStroke
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
                text = stringResource(R.string.verification_code_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = SpiritTodoTheme.color.todoTextMain
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = description,
                fontSize = 14.sp,
                color = SpiritTodoTheme.color.systemGrey
            )

            Spacer(Modifier.height(20.dp))

            VerificationCodeField(
                value = uiState.verificationCode,
                onValueChange = onCodeChange,
                remainingSeconds = uiState.verificationRemainingSeconds,
                isError = uiState.fieldErrors["verificationCode"] != null
            )

            uiState.fieldErrors["verificationCode"]?.let { FieldHintText(text = it, isError = true) }
        }

        Spacer(Modifier.weight(1f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
        ) {
            SpiritsTodoPrimaryButton(
                text = stringResource(R.string.check),
                enabled = uiState.verificationCode.isNotBlank(),
                modifier = Modifier.imePadding(),
                onClick = {
                    focusManager.clearFocus()
                    onVerify()
                }
            )

            Spacer(Modifier.height(bottomSpacerHeight))
        }
    }
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
                    isFocused -> SpiritTodoTheme.color.mainTextAndStroke
                    isError -> SpiritTodoTheme.color.systemRed
                    else -> SpiritTodoTheme.color.systemArea
                },
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            state = textFieldState,
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { isFocused = it.isFocused },
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = SpiritTodoTheme.color.todoTextMain
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            lineLimits = TextFieldLineLimits.SingleLine,
            decorator = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = stringResource(R.string.verification_code_placeholder),
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = SpiritTodoTheme.color.systemGrey
                            )
                        )
                    }

                    innerTextField()
                }
            }
        )

        if (value.isNotEmpty()) {
            Spacer(Modifier.width(8.dp))

            Image(
                painter = painterResource(R.drawable.todo_cross),
                contentDescription = null,
                colorFilter = ColorFilter.tint(SpiritTodoTheme.color.systemGrey),
                modifier = Modifier
                    .size(16.dp)
                    .noRippleClickable { textFieldState.clearText() }
            )
        }

        Spacer(Modifier.width(8.dp))

        Text(
            text = "%02d:%02d".format(minutes, seconds),
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
            color = SpiritTodoTheme.color.mainTextAndStroke
        )
    }
}