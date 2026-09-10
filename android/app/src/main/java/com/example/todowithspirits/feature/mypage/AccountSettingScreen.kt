package com.example.todowithspirits.feature.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todowithspirits.R
import com.example.todowithspirits.component.LoadingOverlay
import com.example.todowithspirits.component.SettingActionRow
import com.example.todowithspirits.component.SettingsRow
import com.example.todowithspirits.component.TitleHeader
import com.example.todowithspirits.feature.mypage.viewmodel.AccountSettingViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun AccountSettingScreen(
    onBack: () -> Unit = {},
    onNicknameClick: () -> Unit = {},
    onModifyPasswordClick: () -> Unit = {},
    onWithdrawSuccess: () -> Unit = {},
    accountSettingViewModel: AccountSettingViewModel = hiltViewModel()
) {
    var showWithdrawDialog by remember { mutableStateOf(false) }
    val isLoading by accountSettingViewModel.isLoading.collectAsStateWithLifecycle()
    val isPasswordChangeAvailable by accountSettingViewModel.isPasswordChangeAvailable.collectAsStateWithLifecycle()

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

        if (isPasswordChangeAvailable) {
            SettingActionRow(
                label = stringResource(R.string.modify_password),
                modifier = Modifier.padding(horizontal = 18.dp),
                labelColor = SpiritTodoTheme.color.systemGrey,
                trailingIconRes = R.drawable.todo_arrow2_20,
                trailingIconTint = null,
                onClick = onModifyPasswordClick
            )

            Spacer(modifier = Modifier.height(30.dp))
        }

        SettingActionRow(
            label = stringResource(R.string.withdraw),
            modifier = Modifier.padding(horizontal = 18.dp),
            labelColor = SpiritTodoTheme.color.systemGrey,
            trailingIconRes = R.drawable.todo_arrow2_20,
            trailingIconTint = null,
            onClick = { showWithdrawDialog = true }
        )

        Spacer(Modifier.height(21.dp))
    }

    if (showWithdrawDialog) {
        AlertDialog(
            onDismissRequest = { showWithdrawDialog = false },
            title = { Text(text = stringResource(R.string.withdraw)) },
            text = { Text(text = stringResource(R.string.withdraw_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showWithdrawDialog = false
                        accountSettingViewModel.withdraw(onSuccess = onWithdrawSuccess)
                    }
                ) {
                    Text(text = stringResource(R.string.withdraw))
                }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawDialog = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }

    LoadingOverlay(isLoading = isLoading)
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    showChevron: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    SettingsRow(
        modifier = Modifier.padding(horizontal = 18.dp),
        onClick = onClick
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = SpiritTodoTheme.color.todoTextMain
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = 14.sp,
                color = SpiritTodoTheme.color.systemGrey
            )

            if (showChevron) {
                Spacer(modifier = Modifier.width(4.dp))

                Image(
                    painter = painterResource(R.drawable.todo_arrow2_20),
                    contentDescription = null
                )
            }
        }
    }
}
