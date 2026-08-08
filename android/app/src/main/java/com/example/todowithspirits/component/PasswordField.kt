package com.example.todowithspirits.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicSecureTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextObfuscationMode
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    fontSize: Int = 14,
    isError: Boolean = false
) {
    val textFieldState = rememberTextFieldState(value)
    var isFocused by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }
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
        BasicSecureTextField(
            state = textFieldState,
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { isFocused = it.isFocused },
            textStyle = TextStyle(
                fontSize = fontSize.sp,
                color = SpiritTodoTheme.color.todoTextMain
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            textObfuscationMode = if (isVisible) TextObfuscationMode.Visible else TextObfuscationMode.Hidden,
            decorator = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if(value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = TextStyle(
                                fontSize = fontSize.sp,
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
                painter = painterResource(if (isVisible) R.drawable.todo_show else R.drawable.todo_unshow),
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .noRippleClickable { isVisible = !isVisible }
            )

            if (isFocused) {
                Spacer(Modifier.width(8.dp))

                Image(
                    painter = painterResource(R.drawable.todo_cross),
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .noRippleClickable { textFieldState.clearText() }
                )
            }
        }
    }
}
