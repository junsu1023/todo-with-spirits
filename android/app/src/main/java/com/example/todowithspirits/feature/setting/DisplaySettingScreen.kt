package com.example.todowithspirits.feature.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.todowithspirits.R
import com.example.todowithspirits.component.TitleHeader
import com.example.todowithspirits.feature.setting.component.DisplaySelectorRow
import com.example.todowithspirits.feature.setting.component.DisplayToggleRow
import com.example.todowithspirits.feature.setting.state.LanguageOptions
import com.example.todowithspirits.feature.setting.state.PlanSortOptions
import com.example.todowithspirits.feature.setting.state.ThemeOptions
import com.example.todowithspirits.feature.setting.viewmodel.DisplaySettingViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun DisplaySettingScreen(
    displaySettingViewModel: DisplaySettingViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by displaySettingViewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpiritTodoTheme.color.surfaceColor1)
            .verticalScroll(rememberScrollState())
    ) {
        TitleHeader(
            leftIconRes = R.drawable.todo_back1,
            title = stringResource(R.string.display_setting),
            onLeftIconClick = onBack
        )

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(30.dp)
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
                label = stringResource(R.string.plan_sort),
                value = uiState.planSortOption,
                options = PlanSortOptions,
                onOptionSelected = { displaySettingViewModel.setPlanSortOption(it) }
            )

            DisplaySelectorRow(
                label = stringResource(R.string.theme),
                value = uiState.themeOption,
                options = ThemeOptions,
                onOptionSelected = { displaySettingViewModel.setThemeOption(it) }
            )

            DisplaySelectorRow(
                label = stringResource(R.string.language),
                value = uiState.languageOption,
                options = LanguageOptions,
                onOptionSelected = { displaySettingViewModel.setLanguageOption(it) }
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}
