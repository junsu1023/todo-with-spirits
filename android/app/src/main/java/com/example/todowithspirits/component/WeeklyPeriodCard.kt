package com.example.todowithspirits.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun WeeklyPeriodCard(
    isWeekExpanded: Boolean,
    weekStart: LocalDate,
    weekEnd: LocalDate,
    onBeforeClick: (LocalDate) -> Unit,
    onAfterClick: (LocalDate) -> Unit
) {
    val weekDateFormatter = remember { DateTimeFormatter.ofPattern("yy년 M월 d일", Locale.KOREAN) }

    AnimatedVisibility(
        visible = isWeekExpanded,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SpiritTodoTheme.color.systemBackground, RoundedCornerShape(6.dp))
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.todo_arrow3_24),
                contentDescription = null,
                modifier = Modifier.noRippleClickable { onBeforeClick(weekStart.minusWeeks(1)) }
            )

            Text(
                text = "${weekStart.format(weekDateFormatter)} ~ ${weekEnd.format(weekDateFormatter)}",
                fontSize = 16.sp,
                color = SpiritTodoTheme.color.todoTextMain,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            Image(
                painter = painterResource(R.drawable.todo_arrow2_24),
                contentDescription = null,
                modifier = Modifier.noRippleClickable { onAfterClick(weekStart.plusWeeks(1)) }
            )
        }
    }
}