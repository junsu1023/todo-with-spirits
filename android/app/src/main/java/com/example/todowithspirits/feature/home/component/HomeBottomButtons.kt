package com.example.todowithspirits.feature.home.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun HomeBottomButtons(
    onBestiaryClick: () -> Unit,
    onForestClick: () -> Unit,
    onCalendarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        HomeBottomButton(
            icon = R.drawable.widgetly_spirit_bestiary,
            label = R.string.spirit_bestiary,
            onClick = onBestiaryClick
        )

        HomeBottomButton(
            icon = R.drawable.widgetly_spirit_forest_disabled, // R.drawable.widgetly_spirit_forest,
            label = R.string.forest,
            isComingSoon = true, // TODO 정령의 숲 나오면 제거
            onClick = { /* TODO 정령의 숲 나오면 주석 기 */ } // onForestClick
        )

        HomeBottomButton(
            icon = R.drawable.widgetly_calendar,
            label = R.string.calendar,
            onClick = onCalendarClick
        )
    }
}

@Composable
private fun HomeBottomButton(
    @DrawableRes icon: Int,
    @StringRes label: Int,
    onClick: () -> Unit,
    isComingSoon: Boolean = false
) {
    Column(
        modifier = Modifier
            .width(80.dp)
            .height(94.dp)
            .noRippleClickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(icon),
                contentDescription = stringResource(label),
                tint = Color.Unspecified
            )

            if (isComingSoon) {
                Icon(
                    painter = painterResource(R.drawable.widgetly_lock),
                    contentDescription = "출시 예정",
                    tint = Color.Unspecified
                )

                ComingSoonBadge(modifier = Modifier.matchParentSize())
            }
        }

        Box {
            Text(
                text = stringResource(label),
                fontSize = 12.sp,
                color = SpiritTodoTheme.color.white,
                style = TextStyle(
                    drawStyle = Stroke(
                        width = with(LocalDensity.current) { 4.dp.toPx() },
                        join = StrokeJoin.Round
                    )
                )
            )

            Text(
                text = stringResource(label),
                fontSize = 12.sp,
                color = if(isComingSoon) SpiritTodoTheme.color.fontGrey else SpiritTodoTheme.color.font01
            )
        }
    }
}
