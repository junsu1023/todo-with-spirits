package com.example.todowithspirits.feature.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.component.TitleHeader
import com.example.todowithspirits.feature.setting.component.InfoRow
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun AccountSettingScreen(
    onBack: () -> Unit = {},
    onNicknameClick: () -> Unit = {},
    onModifyPasswordClick: () -> Unit = {},
    onWithdrawClick: () -> Unit = {}
) {
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

        Spacer(Modifier.height(24.dp))

        InfoRow(
            label = stringResource(R.string.nickname),
            value = "댕트리버",
            onClick = onNicknameClick
        )

        Spacer(Modifier.height(24.dp))

        InfoRow(
            label = stringResource(R.string.linked_account),
            value = "wish0221@gmail.com",
            showChevron = false
        )

        Spacer(modifier = Modifier.height(20.dp))

        HorizontalDivider(
            thickness = 6.dp,
            color = SpiritTodoTheme.color.surfaceColor10
        )

        Spacer(modifier = Modifier.height(24.dp))

        ActionRow(
            label = stringResource(R.string.modify_password),
            onClick = onModifyPasswordClick
        )

        Spacer(modifier = Modifier.height(30.dp))

        ActionRow(
            label = stringResource(R.string.withdraw),
            onClick = onWithdrawClick
        )

        Spacer(Modifier.height(21.dp))
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
