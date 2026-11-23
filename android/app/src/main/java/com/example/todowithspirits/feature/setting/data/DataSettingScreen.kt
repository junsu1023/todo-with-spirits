package com.example.todowithspirits.feature.setting.data

import androidx.activity.compose.BackHandler
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.todowithspirits.R
import com.example.todowithspirits.component.SettingActionRow
import com.example.todowithspirits.component.SpiritsTodoPrimaryButton
import com.example.todowithspirits.component.TopNaviBar
import com.example.todowithspirits.feature.setting.data.state.DataSettingEffect
import com.example.todowithspirits.feature.setting.data.state.DataSettingEvent
import com.example.todowithspirits.feature.setting.data.viewmodel.DataSettingViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.util.ObserveAsEffect

@Composable
fun DataSettingRoute(
    navigateBack: () -> Unit,
    dataSettingViewModel: DataSettingViewModel = hiltViewModel()
) {
    ObserveAsEffect(dataSettingViewModel.effect) { effect ->
        when (effect) {
            DataSettingEffect.NavigateBack -> navigateBack()
        }
    }

    DataSettingScreen(onEvent = dataSettingViewModel::onEvent)
}

@Composable
fun DataSettingScreen(onEvent: (DataSettingEvent) -> Unit) {
    BackHandler { onEvent(DataSettingEvent.BackClicked) }

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
            onLeftIconClick = { onEvent(DataSettingEvent.BackClicked) },
            modifier = Modifier.padding(horizontal = 17.dp)
        )

        Spacer(Modifier.height(14.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 14.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SettingActionRow(label = stringResource(R.string.backup), onClick = { onEvent(DataSettingEvent.BackupClicked) })

            SettingActionRow(label = stringResource(R.string.sync), onClick = { onEvent(DataSettingEvent.SyncClicked) })

            SettingActionRow(label = stringResource(R.string.export_data), onClick = { onEvent(DataSettingEvent.ExportDataClicked) })
        }

        Spacer(modifier = Modifier.weight(1f))

        SpiritsTodoPrimaryButton(
            text = stringResource(R.string.check),
            onClick = { onEvent(DataSettingEvent.ConfirmClicked) },
            modifier = Modifier.padding(horizontal = 14.dp)
        )

        Spacer(Modifier.height(48.dp))
    }
}
