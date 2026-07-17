package com.example.todowithspirits.feature.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.component.SpiritsTodoPrimaryButton
import com.example.todowithspirits.component.TitleHeader
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun EditProfileScreen(
    nickname: String = "댕트리버",
    onBack: () -> Unit = {},
    onConfirm: (String) -> Unit = {}
) {
    var value by remember { mutableStateOf(nickname) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpiritTodoTheme.color.surfaceColor1)
    ) {
        TitleHeader(
            leftIconRes = R.drawable.todo_back1,
            onLeftIconClick = onBack,
            title = stringResource(R.string.modify_profile)
        )

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
        ) {
            Text(
                text = stringResource(R.string.nickname),
                fontSize = 14.sp,
                color = SpiritTodoTheme.color.systemGrey
            )

            Spacer(Modifier.height(6.dp))

            NicknameField(
                value = value,
                onValueChange = { value = it }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        SpiritsTodoPrimaryButton(
            text = stringResource(R.string.check),
            onClick = { onConfirm(value) },
            modifier = Modifier.padding(horizontal = 18.dp)
        )

        Spacer(Modifier.height(21.dp))
    }
}

@Composable
private fun NicknameField(value: String, onValueChange: (String) -> Unit) {
    var isFocused by remember { mutableStateOf(false) }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(
            fontSize = 16.sp,
            color = SpiritTodoTheme.color.todoTextMain
        ),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(
                        text = stringResource(R.string.nickname),
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
                color = if(isFocused) SpiritTodoTheme.color.mainTextAndStroke else SpiritTodoTheme.color.systemArea,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    )
}
