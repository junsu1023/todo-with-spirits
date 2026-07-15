package com.example.todowithspirits.feature.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.component.TitleHeader
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun AccountSettingScreen(onBack: () -> Unit = {}) {
    var nickname by remember { mutableStateOf("댕트리버") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpiritTodoTheme.color.surfaceColor1)
            .verticalScroll(rememberScrollState())
    ) {
        TitleHeader(
            leftIconRes = R.drawable.todo_back1,
            onLeftIconClick = onBack,
            title = stringResource(R.string.account_management)
        )

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
        ) {
            FieldLabel(text = stringResource(R.string.nickname))

            Spacer(Modifier.height(4.dp))

            NicknameField(
                value = nickname,
                onValueChange = { nickname = it }
            )

            Spacer(Modifier.height(20.dp))

            FieldLabel(text = stringResource(R.string.linked_account))

            Spacer(Modifier.height(4.dp))

            LinkedAccountRow(email = "wish0221@gmail.com")

            Spacer(modifier = Modifier.height(24.dp))
        }

        HorizontalDivider(
            thickness = 6.dp,
            color = SpiritTodoTheme.color.surfaceColor10
        )

        Spacer(modifier = Modifier.height(24.dp))

        ActionRow(
            label = stringResource(R.string.modify_password),
            onClick = {}
        )

        Spacer(modifier = Modifier.height(30.dp))

        ActionRow(
            label = stringResource(R.string.withdraw),
            onClick = {}
        )

        Spacer(Modifier.height(21.dp))
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        color = SpiritTodoTheme.color.systemGrey
    )
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
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = 1.dp,
                color = if (isFocused) SpiritTodoTheme.color.mainTextAndStroke else SpiritTodoTheme.color.onSurfaceColor9,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    )
}

@Composable
private fun LinkedAccountRow(email: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SpiritTodoTheme.color.systemArea, RoundedCornerShape(6.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {}
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = email,
            fontSize = 16.sp,
            color = SpiritTodoTheme.color.todoTextMain,
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .background(SpiritTodoTheme.color.surfaceColor3, RoundedCornerShape(50))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = stringResource(R.string.reauth_required),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.color.onSurfaceColor3
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Image(
            painter = painterResource(R.drawable.todo_arrow2_20),
            contentDescription = null
        )
    }
}

@Composable
private fun ActionRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = SpiritTodoTheme.color.systemGrey
        )

        Image(
            painter = painterResource(R.drawable.todo_arrow2_20),
            contentDescription = null
        )
    }
}
