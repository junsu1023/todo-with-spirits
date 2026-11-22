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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun PlainTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false
) {
    val textFieldState = rememberTextFieldState(value)
    var isFocused by remember { mutableStateOf(false) }
    val currentOnValueChange by rememberUpdatedState(onValueChange)

    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text.toString() }
            .collect { text -> currentOnValueChange(text) }
    }

    LaunchedEffect(value) {
        if(value != textFieldState.text.toString()) {
            textFieldState.setTextAndPlaceCursorAtEnd(value)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
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
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            lineLimits = TextFieldLineLimits.SingleLine,
            decorator = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if(value.isEmpty()) {
                        Text(
                            text = placeholder,
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

        if(isFocused && value.isNotEmpty()) {
            Image(
                painter = painterResource(R.drawable.widgetly_cross_20),
                contentDescription = null,
                modifier = Modifier.noRippleClickable { textFieldState.clearText() }
            )
        }
    }
}
