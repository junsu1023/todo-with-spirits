package com.example.todowithspirits.feature.add.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

// 일정 제목 입력. 입력 중(포커스)에는 밑줄이 분홍색으로 바뀐다.
@Composable
fun TaskTitleField(
    title: String,
    onTitleChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        BasicTextField(
            value = title,
            onValueChange = onTitleChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused },
            textStyle = TextStyle(
                color = SpiritTodoTheme.color.todoTextMain,
                fontSize = 18.sp
            ),
            singleLine = true,
            decorationBox = { innerTextField ->
                if (title.isEmpty()) {
                    Text(
                        text = stringResource(R.string.task_title_placeholder),
                        style = TextStyle(
                            color = SpiritTodoTheme.color.onSurfaceColor2,
                            fontSize = 18.sp
                        )
                    )
                }

                innerTextField()
            }
        )

        HorizontalDivider(
            modifier = Modifier.padding(top = 10.dp),
            thickness = if (isFocused) 1.5.dp else 1.dp,
            color = if (isFocused) SpiritTodoTheme.color.pointPink else SpiritTodoTheme.color.onSurfaceColor2
        )
    }
}
