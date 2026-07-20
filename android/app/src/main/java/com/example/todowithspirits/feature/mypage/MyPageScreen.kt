package com.example.todowithspirits.feature.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.todowithspirits.R
import com.example.todowithspirits.component.TitleHeader
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.feature.mypage.component.MySpiritCard
import com.example.todowithspirits.feature.mypage.component.ProfileSection
import com.example.todowithspirits.feature.mypage.component.SettingRow
import com.example.todowithspirits.feature.mypage.component.StatusRow
import com.example.todowithspirits.feature.mypage.viewmodel.MyPageViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme

data class SettingItem(
    val iconRes: Int,
    val titleRes: Int,
    val descRes: Int
)

val settingItems = listOf(
    SettingItem(R.drawable.todo_bottom_nv, R.string.account_setting, R.string.account_setting_desc),
    SettingItem(R.drawable.todo_alarm, R.string.alarm_setting, R.string.alarm_setting_desc),
    SettingItem(R.drawable.todo_computer, R.string.display_setting, R.string.display_setting_desc),
    SettingItem(R.drawable.todo_cloud, R.string.data_setting, R.string.data_setting_desc),
    SettingItem(R.drawable.todo_info, R.string.customer_support, R.string.customer_support_desc)
)

@Composable
fun MyPageScreen(
    myPageViewModel: MyPageViewModel = hiltViewModel(),
    navigateToAccountSetting: () -> Unit = {},
    navigateToAlarmSetting: () -> Unit = {},
    navigateToDisplaySetting: () -> Unit = {},
    navigateToLogout: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpiritTodoTheme.color.surfaceColor1)
            .verticalScroll(rememberScrollState())
    ) {
        TitleHeader(title = stringResource(R.string.my_page_title))

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            ProfileSection(navigateToAccountSetting = navigateToAccountSetting)

            Spacer(Modifier.height(15.dp))

            StatusRow()

            Spacer(Modifier.height(14.dp))

            MySpiritCard()
        }

        Spacer(Modifier.height(20.dp))

        HorizontalDivider(
            thickness = 6.dp,
            color = SpiritTodoTheme.color.surfaceColor10
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
                        2 -> navigateToDisplaySetting
                        else -> ({})
                    }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        HorizontalDivider(
            thickness = 6.dp,
            color = SpiritTodoTheme.color.surfaceColor10
        )

        Spacer(modifier = Modifier.height(24.dp))

        SettingRow2(title = stringResource(R.string.version), desc = "v.0.0.00")

        Spacer(modifier = Modifier.height(30.dp))

        SettingRow2(
            title = stringResource(R.string.logout),
            onClick = { myPageViewModel.logout(onSuccess = navigateToLogout) }
        )

        Spacer(modifier = Modifier.height(26.dp))
    }
}

@Composable
fun SettingRow2(
    title: String,
    desc: String? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .noRippleClickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = SpiritTodoTheme.color.systemGrey
        )

        if(desc != null) {
            Text(
                text = desc,
                fontSize = 14.sp,
                color = SpiritTodoTheme.color.systemGrey
            )
        }
    }
}