package com.example.todowithspirits.feature.setting.display

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
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.todowithspirits.R
import com.example.todowithspirits.component.LoadingOverlay
import com.example.todowithspirits.component.SpiritsTodoPrimaryButton
import com.example.todowithspirits.component.TopNaviBar
import com.example.todowithspirits.feature.setting.component.DisplaySelectorRow
import com.example.todowithspirits.feature.setting.component.DisplayToggleRow
import com.example.todowithspirits.feature.setting.state.LanguageOptions
import com.example.todowithspirits.feature.setting.display.viewmodel.DisplaySettingViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.util.ToastUtil

@Composable
fun DisplaySettingScreen(
    displaySettingViewModel: DisplaySettingViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by displaySettingViewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by displaySettingViewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(displaySettingViewModel) {
        displaySettingViewModel.errorMsg.collect { message -> ToastUtil.show(context, message) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .background(SpiritTodoTheme.color.back01)
            .verticalScroll(rememberScrollState())
    ) {
        TopNaviBar(
            leftIconRes = R.drawable.widgetly_back_24,
            title = stringResource(R.string.display_setting),
            onLeftIconClick = onBack,
            modifier = Modifier.padding(horizontal = 17.dp)
        )

        Spacer(Modifier.height(4.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            DisplayToggleRow(
                label = stringResource(R.string.dark_mode),
                checked = uiState.isDarkMode,
                onCheckedChange = { displaySettingViewModel.setDarkMode(it) }
            )

            DisplayToggleRow(
                label = stringResource(R.string.show_plan_dday),
                checked = uiState.isShowPlanDday,
                onCheckedChange = { displaySettingViewModel.setShowPlanDday(it) }
            )

            DisplaySelectorRow(
                label = stringResource(R.string.language),
                value = uiState.languageOption,
                options = LanguageOptions,
                onOptionSelected = { displaySettingViewModel.setLanguageOption(it) }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        SpiritsTodoPrimaryButton(
            text = stringResource(R.string.check),
            onClick = onBack,
            modifier = Modifier.padding(horizontal = 14.dp)
        )

        Spacer(Modifier.height(48.dp))
    }

    LoadingOverlay(isLoading = isLoading)
}
