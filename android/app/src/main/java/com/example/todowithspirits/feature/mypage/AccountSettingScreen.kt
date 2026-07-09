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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.feature.mypage.component.AccountSettingHeader
import com.example.todowithspirits.feature.mypage.component.AvatarSection
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun AccountSettingScreen(onBack: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpiritTodoTheme.colors.white)
            .verticalScroll(rememberScrollState())
    ) {
        AccountSettingHeader(onBack = onBack)

        Spacer(Modifier.height(12.dp))

        AvatarSection()

        Spacer(Modifier.height(28.dp))

        // Profile info section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
        ) {
            Text(
                text = stringResource(R.string.profile_info),
                fontSize = 14.sp,
                color = SpiritTodoTheme.colors.onSurfaceColor2,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
            )

            Spacer(modifier = Modifier.height(5.dp))

            ProfileInfoRow(
                label = stringResource(R.string.nickname),
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

        HorizontalDivider(
            thickness = 6.dp,
            color = SpiritTodoTheme.colors.surfaceColor4
        )

        Spacer(modifier = Modifier.height(20.dp))

        ActionRow(
            label = stringResource(R.string.logout),
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
            color = SpiritTodoTheme.colors.mainTextColor
        )

        Text(
            text = value,
            fontSize = 16.sp,
            color = SpiritTodoTheme.colors.mainTextColor
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
            color = SpiritTodoTheme.colors.onSurfaceColor2
        )

        Image(
            painter = painterResource(R.drawable.fi_rr_angle_small_right),
            contentDescription = null,
            colorFilter = ColorFilter.tint(SpiritTodoTheme.colors.onSurfaceColor2),
            modifier = Modifier.size(22.dp)
        )
    }
}
