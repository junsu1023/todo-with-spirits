package com.example.todowithspirits.feature.setting.data

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.todowithspirits.R
import com.example.todowithspirits.component.SettingActionRow
import com.example.todowithspirits.component.SpiritsTodoPrimaryButton
import com.example.todowithspirits.component.TopNaviBar
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun DataSettingScreen(
    onBack: () -> Unit = {},
    onBackupClick: () -> Unit = {},
    onSyncClick: () -> Unit = {},
    onExportDataClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .background(SpiritTodoTheme.color.back01)
            .verticalScroll(rememberScrollState())
    ) {
        TopNaviBar(
            leftIconRes = R.drawable.widgetly_back_24,
            title = stringResource(R.string.data_setting),
            onLeftIconClick = onBack,
            modifier = Modifier.padding(horizontal = 17.dp)
        )

        Spacer(Modifier.height(14.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 14.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SettingActionRow(label = stringResource(R.string.backup), onClick = onBackupClick)

            SettingActionRow(label = stringResource(R.string.sync), onClick = onSyncClick)

            SettingActionRow(label = stringResource(R.string.export_data), onClick = onExportDataClick)
        }

        Spacer(modifier = Modifier.weight(1f))

        SpiritsTodoPrimaryButton(
            text = stringResource(R.string.check),
            onClick = onBack,
            modifier = Modifier.padding(horizontal = 14.dp)
        )

        Spacer(Modifier.height(48.dp))
    }
}
