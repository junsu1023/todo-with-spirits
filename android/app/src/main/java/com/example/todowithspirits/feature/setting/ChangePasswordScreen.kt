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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.component.SpiritsTodoPrimaryButton
import com.example.todowithspirits.component.TitleHeader
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit = {},
    onConfirm: (currentPassword: String, newPassword: String) -> Unit = { _, _ -> }
) {
    var currentPassword by remember { mutableStateOf("********") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpiritTodoTheme.color.surfaceColor1)
    ) {
        TitleHeader(
            leftIconRes = R.drawable.todo_back1,
            onLeftIconClick = onBack,
            title = stringResource(R.string.change_password)
        )

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
        ) {
            Text(
                text = stringResource(R.string.current_password),
                fontSize = 14.sp,
                color = SpiritTodoTheme.color.systemGrey
            )

            Spacer(Modifier.height(4.dp))

            PasswordField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                placeholder = stringResource(R.string.current_password),
                fontSize = 16
            )

            Spacer(Modifier.height(30.dp))

            Text(
                text = stringResource(R.string.new_password),
                fontSize = 14.sp,
                color = SpiritTodoTheme.color.systemGrey
            )

            Spacer(Modifier.height(4.dp))

            PasswordField(
                value = newPassword,
                onValueChange = { newPassword = it },
                placeholder = stringResource(R.string.new_password_placeholder),
                fontSize = 14
            )

            Spacer(Modifier.height(12.dp))

            PasswordField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = stringResource(R.string.confirm_password_placeholder),
                fontSize = 14
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        SpiritsTodoPrimaryButton(
            text = stringResource(R.string.check),
            onClick = { onConfirm(currentPassword, newPassword) },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(21.dp))
    }
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    fontSize: Int
) {
    var isFocused by remember { mutableStateOf(false) }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        textStyle = TextStyle(
            fontSize = 16.sp,
            color = SpiritTodoTheme.color.todoTextMain
        ),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
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
        },
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = 1.dp,
                color = if(isFocused) SpiritTodoTheme.color.mainTextAndStroke else SpiritTodoTheme.color.systemArea,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 14.dp)
            .padding(top = 15.dp, bottom = 12.dp)
    )
}
