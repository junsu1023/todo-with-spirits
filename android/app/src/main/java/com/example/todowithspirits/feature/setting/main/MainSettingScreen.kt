package com.example.todowithspirits.feature.setting.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todowithspirits.R
import com.example.todowithspirits.component.LoadingOverlay
import com.example.todowithspirits.component.SettingsRow
import com.example.todowithspirits.component.TopNaviBar
import com.example.todowithspirits.feature.setting.main.component.SettingCard
import com.example.todowithspirits.feature.setting.main.component.SettingDivider
import com.example.todowithspirits.feature.setting.main.component.SettingMenuRows
import com.example.todowithspirits.feature.setting.main.component.SettingSection
import com.example.todowithspirits.feature.setting.main.state.MainSettingEffect
import com.example.todowithspirits.feature.setting.main.state.MainSettingEvent
import com.example.todowithspirits.feature.setting.main.state.MainSettingUiState
import com.example.todowithspirits.feature.setting.main.state.SettingMenu
import com.example.todowithspirits.feature.setting.main.viewmodel.MainSettingViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.util.ObserveAsEffect
import com.example.todowithspirits.util.ToastUtil

private val systemMenus = listOf(SettingMenu.ALARM, SettingMenu.DISPLAY, SettingMenu.DATA)
private val supportMenus = listOf(
    SettingMenu.NOTICE,
    SettingMenu.USAGE_GUIDE,
    SettingMenu.QNA,
    SettingMenu.TERMS_AND_POLICY
)

@Composable
fun MainSettingRoute(
    navigateBack: () -> Unit,
    navigateToEditProfile: () -> Unit,
    navigateToAlarmSetting: () -> Unit,
    navigateToDisplaySetting: () -> Unit,
    navigateToDataSetting: () -> Unit,
    navigateToChangePassword: () -> Unit,
    navigateToWithdraw: () -> Unit,
    mainSettingViewModel: MainSettingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by mainSettingViewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by mainSettingViewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(mainSettingViewModel) {
        mainSettingViewModel.errorMsg.collect { message -> ToastUtil.show(context, message) }
    }

    ObserveAsEffect(mainSettingViewModel.effect) { effect ->
        when (effect) {
            MainSettingEffect.NavigateBack -> navigateBack()
            is MainSettingEffect.NavigateToMenu -> when (effect.menu) {
                SettingMenu.NICKNAME -> navigateToEditProfile()
                SettingMenu.ALARM -> navigateToAlarmSetting()
                SettingMenu.DISPLAY -> navigateToDisplaySetting()
                SettingMenu.DATA -> navigateToDataSetting()
                SettingMenu.CHANGE_PASSWORD -> navigateToChangePassword()
                SettingMenu.WITHDRAW -> navigateToWithdraw()
                // TODO 고객 지원 화면이 생기면 연결
                SettingMenu.NOTICE,
                SettingMenu.USAGE_GUIDE,
                SettingMenu.QNA,
                SettingMenu.TERMS_AND_POLICY -> Unit
            }
        }
    }

    MainSettingScreen(
        uiState = uiState,
        isLoading = isLoading,
        onEvent = mainSettingViewModel::onEvent
    )
}

@Composable
fun MainSettingScreen(
    uiState: MainSettingUiState,
    isLoading: Boolean,
    onEvent: (MainSettingEvent) -> Unit
) {
    val onMenuClick: (SettingMenu) -> Unit = { onEvent(MainSettingEvent.MenuClicked(it)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .background(SpiritTodoTheme.color.back01)
    ) {
        TopNaviBar(
            leftIconRes = R.drawable.widgetly_back_24,
            title = stringResource(R.string.setting),
            onLeftIconClick = { onEvent(MainSettingEvent.BackClicked) },
            modifier = Modifier.padding(horizontal = 10.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp)
        ) {
            Spacer(Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                SettingSection(title = stringResource(R.string.profile)) {
                    ProfileRow(
                        label = stringResource(R.string.nickname),
                        value = uiState.nickname,
                        onClick = { onMenuClick(SettingMenu.NICKNAME) }
                    )

                    SettingDivider()

                    ProfileRow(
                        label = stringResource(R.string.linked_account),
                        value = uiState.email ?: "-"
                    )
                }

                SettingSection(title = stringResource(R.string.system_setting)) {
                    SettingMenuRows(menus = systemMenus, onMenuClick = onMenuClick)
                }

                SettingSection(title = stringResource(R.string.customer_support)) {
                    SettingMenuRows(menus = supportMenus, onMenuClick = onMenuClick)
                }
            }

            Spacer(Modifier.height(34.dp))

            SettingCard {
                SettingMenuRows(
                    menus = listOfNotNull(
                        SettingMenu.CHANGE_PASSWORD.takeIf { uiState.isPasswordChangeAvailable },
                        SettingMenu.WITHDRAW
                    ),
                    onMenuClick = onMenuClick,
                    isSubdued = true
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    LoadingOverlay(isLoading = isLoading)
}

@Composable
private fun ProfileRow(
    label: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    SettingsRow(
        modifier = Modifier.height(22.dp),
        onClick = onClick
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = SpiritTodoTheme.color.font02
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                fontSize = 14.sp,
                color = if (onClick != null) SpiritTodoTheme.color.font01 else SpiritTodoTheme.color.fontGrey
            )

            if (onClick != null) {
                Image(
                    painter = painterResource(R.drawable.widgetly_arrow_16),
                    contentDescription = null
                )
            }
        }
    }
}
