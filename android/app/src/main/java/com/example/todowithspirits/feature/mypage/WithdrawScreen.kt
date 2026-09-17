package com.example.todowithspirits.feature.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todowithspirits.R
import com.example.todowithspirits.component.LoadingOverlay
import com.example.todowithspirits.component.TitleHeader
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.feature.mypage.viewmodel.WithdrawViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.util.ToastUtil

@Composable
fun WithdrawScreen(
    withdrawViewModel: WithdrawViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onWithdrawSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by withdrawViewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by withdrawViewModel.isLoading.collectAsStateWithLifecycle()
    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(withdrawViewModel) {
        withdrawViewModel.errorMsg.collect { message -> ToastUtil.show(context, message) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpiritTodoTheme.color.surfaceColor1)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleHeader(
                leftIconRes = R.drawable.todo_back1,
                onLeftIconClick = onBack,
                title = stringResource(R.string.withdraw)
            )

            Spacer(Modifier.height(40.dp))

            SadSpiritIllustration(modifier = Modifier.align(Alignment.CenterHorizontally))

            Spacer(Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.withdraw_stay_title, uiState.nickname),
                fontSize = 20.sp,
                color = SpiritTodoTheme.color.onSurfaceColor1,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.withdraw_stay_subtitle),
                fontSize = 14.sp,
                color = SpiritTodoTheme.color.systemGrey,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WithdrawActionButton(
                    text = stringResource(R.string.withdraw_stay_button),
                    backgroundColor = SpiritTodoTheme.color.systemBackground,
                    textColor = SpiritTodoTheme.color.systemGrey,
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                )

                WithdrawActionButton(
                    text = stringResource(R.string.withdraw_leave_button),
                    backgroundColor = SpiritTodoTheme.color.mainArea,
                    textColor = SpiritTodoTheme.color.surfaceColor1,
                    onClick = { showConfirmDialog = true },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(21.dp))
        }

        if (showConfirmDialog) {
            WithdrawConfirmDialog(
                onCancel = { showConfirmDialog = false },
                onConfirm = {
                    showConfirmDialog = false
                    withdrawViewModel.withdraw(onSuccess = onWithdrawSuccess)
                }
            )
        }

        LoadingOverlay(isLoading = isLoading)
    }
}

@Composable
private fun SadSpiritIllustration(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                SpiritTodoTheme.color.mainArea.copy(alpha = 0.35f),
                                SpiritTodoTheme.color.mainArea.copy(alpha = 0f)
                            )
                        ),
                        shape = CircleShape
                    )
            )

            Image(
                painter = painterResource(R.drawable.temp_spirit),
                contentDescription = null,
                modifier = Modifier.size(96.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .size(width = 64.dp, height = 10.dp)
                .background(SpiritTodoTheme.color.systemArea, RoundedCornerShape(50))
        )
    }
}

@Composable
private fun WithdrawActionButton(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .background(backgroundColor, RoundedCornerShape(6.dp))
            .noRippleClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun WithdrawConfirmDialog(
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpiritTodoTheme.color.dimColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(SpiritTodoTheme.color.surfaceColor1, RoundedCornerShape(12.dp))
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.withdraw_dialog_title),
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = SpiritTodoTheme.color.todoTextMain,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.withdraw_dialog_message),
                fontSize = 13.sp,
                color = SpiritTodoTheme.color.systemGrey,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                WithdrawActionButton(
                    text = stringResource(R.string.cancel),
                    backgroundColor = SpiritTodoTheme.color.systemArea,
                    textColor = SpiritTodoTheme.color.systemGrey,
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                )

                WithdrawActionButton(
                    text = stringResource(R.string.withdraw_dialog_confirm),
                    backgroundColor = SpiritTodoTheme.color.mainArea,
                    textColor = SpiritTodoTheme.color.surfaceColor1,
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
