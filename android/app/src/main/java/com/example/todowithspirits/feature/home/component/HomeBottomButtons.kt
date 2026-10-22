package com.example.todowithspirits.feature.home.component

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.theme.SpiritTodoTheme

private val BottomButtonSize = 71.dp

// Note: home1 디자인의 임시 버튼. 아이콘이 나오면 교체한다.
@Composable
fun HomeBottomButtons(
    onForestClick: () -> Unit,
    onCalendarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 30.dp, end = 30.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 이미 홈이라 누르면 아무 동작도 하지 않는다.
        HomeBottomButton(label = R.string.home, onClick = null)
        HomeBottomButton(label = R.string.forest, onClick = onForestClick)
        HomeBottomButton(label = R.string.calendar, onClick = onCalendarClick)
    }
}

@Composable
private fun HomeBottomButton(
    @StringRes label: Int,
    onClick: (() -> Unit)?
) {
    Box(
        modifier = Modifier
            .size(BottomButtonSize)
            .background(SpiritTodoTheme.color.surfaceColor15)
            .then(if (onClick != null) Modifier.noRippleClickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(label),
            fontSize = 16.sp,
            color = SpiritTodoTheme.color.surfaceColor5
        )
    }
}
