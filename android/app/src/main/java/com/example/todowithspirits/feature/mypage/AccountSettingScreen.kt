package com.example.todowithspirits.feature.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.component.TitleHeader
import com.example.todowithspirits.feature.mypage.component.AvatarSection
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun AccountSettingScreen(onBack: () -> Unit = {}) {
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

        AvatarSection()

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
        ) {
            ProfileInfoRow(
                label = stringResource(R.string.name),
                value = "일하기 ***"
            )

            ProfileInfoRow(
                label = stringResource(R.string.birthday),
                value = "2005. 06. 21."
            )

            ProfileInfoRow(
                label = stringResource(R.string.gender),
                value = "남자"
            )

            ProfileInfoRow(
                label = stringResource(R.string.linked_account),
                value = "wish0221@gmail.com"
            )

            Spacer(modifier = Modifier.height(5.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .padding(horizontal = 16.dp)
                .background(SpiritTodoTheme.color.surfaceColor4, RoundedCornerShape(6.dp))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.modify_profile),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.color.onSurfaceColor1
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        HorizontalDivider(
            thickness = 6.dp,
            color = SpiritTodoTheme.colors.surfaceColor4
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
private fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = SpiritTodoTheme.color.onSurfaceColor1
        )

        Text(
            text = value,
            fontSize = 16.sp,
            color = SpiritTodoTheme.color.onSurfaceColor1
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
