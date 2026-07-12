package com.example.todowithspirits.feature.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.todowithspirits.R
import com.example.todowithspirits.feature.mypage.component.MySpiritCard
import com.example.todowithspirits.feature.mypage.component.ProfileSection
import com.example.todowithspirits.feature.mypage.component.SettingRow
import com.example.todowithspirits.feature.mypage.component.StatusRow
import com.example.todowithspirits.theme.SpiritTodoTheme

data class SettingItem(
    val iconRes: Int,
    val titleRes: Int,
    val descRes: Int
)

val settingItems = listOf(
    SettingItem(R.drawable.fi_rr_user, R.string.account_setting, R.string.account_setting_desc),
    SettingItem(R.drawable.alarm_icon, R.string.alarm_setting, R.string.alarm_setting_desc),
    SettingItem(R.drawable.fi_rr_computer, R.string.display_setting, R.string.display_setting_desc),
    SettingItem(R.drawable.fi_rr_cloud, R.string.data_setting, R.string.data_setting_desc),
    SettingItem(R.drawable.fi_rr_info, R.string.customer_support, R.string.customer_support_desc)
)

@Composable
fun MyPageScreen(
    onBack: () -> Unit = {},
    navigateToAccountSetting: () -> Unit = {},
    navigateToAlarmSetting: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpiritTodoTheme.colors.white)
            .verticalScroll(rememberScrollState())
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.fi_rr_back),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterStart)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onBack() }
                )
            }

            Spacer(Modifier.height(10.dp))

            ProfileSection()

            Spacer(Modifier.height(15.dp))

            StatusRow()

            Spacer(Modifier.height(8.dp))

            MySpiritCard()
        }

        Spacer(Modifier.height(20.dp))

        HorizontalDivider(
            thickness = 6.dp,
            color = SpiritTodoTheme.colors.surfaceColor4
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 5.dp)
        ) {
            settingItems.forEachIndexed { index, item ->
                SettingRow(
                    item = item,
                    onClick = when (index) {
                        0 -> navigateToAccountSetting
                        1 -> navigateToAlarmSetting
                        else -> ({})
                    }
                )
            }
        }

        Spacer(Modifier.height(21.dp))
    }
}