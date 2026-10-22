package com.example.todowithspirits.feature.calendar.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.component.SpiritsTodoCheckbox
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.feature.calendar.model.PlanItemData
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.util.KoreanHourMinuteFormatter
import com.example.todowithspirits.util.KoreanMonthDayWithDayFormatter

private val CheckSize = 18.dp
private val CheckToTitleGap = 10.dp

// 캘린더 일정 카드 (피그마 Calendar1). 카드를 누르면 펼쳐져 삭제/수정 버튼이 나온다.
@Composable
fun CalendarTaskItem(
    item: PlanItemData,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "calendarTaskArrowRotation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SpiritTodoTheme.color.surfaceColor16, RoundedCornerShape(8.dp))
            .animateContentSize()
            .noRippleClickable(onClick = onToggleExpanded)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SpiritsTodoCheckbox(
                modifier = Modifier.size(CheckSize),
                checked = item.isDone,
                onCheckedChange = { onToggleComplete() },
                checkedIcon = painterResource(R.drawable.ic_check_circle_on_18),
                uncheckedIcon = painterResource(R.drawable.ic_check_circle_off_18)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = CheckToTitleGap),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (item.isDone) SpiritTodoTheme.color.systemGrey else SpiritTodoTheme.color.onSurfaceColor5,
                    textDecoration = if (item.isDone) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (item.isImportant) {
                    Image(
                        painter = painterResource(R.drawable.fi_rr_color_star),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Image(
                painter = painterResource(R.drawable.fi_rr_angle_small_down),
                contentDescription = null,
                colorFilter = ColorFilter.tint(SpiritTodoTheme.color.systemGrey),
                modifier = Modifier
                    .size(18.dp)
                    .rotate(arrowRotation)
            )
        }

        item.endDate?.let { date ->
            Text(
                text = buildString {
                    append(date.format(KoreanMonthDayWithDayFormatter))
                    if (!item.isAllDay) item.endTime?.let { append(" ${it.format(KoreanHourMinuteFormatter)}") }
                },
                fontSize = 12.sp,
                color = SpiritTodoTheme.color.systemGrey,
                modifier = Modifier.padding(start = CheckSize + CheckToTitleGap, top = 4.dp)
            )
        }

        if (isExpanded) {
            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalendarTaskActionButton(
                    text = stringResource(R.string.delete),
                    color = SpiritTodoTheme.color.systemRed,
                    onClick = onDelete,
                    modifier = Modifier.weight(1f)
                )

                CalendarTaskActionButton(
                    text = stringResource(R.string.modify),
                    color = SpiritTodoTheme.color.systemGrey,
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CalendarTaskActionButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(32.dp)
            .background(SpiritTodoTheme.color.surfaceColor1, RoundedCornerShape(6.dp))
            .noRippleClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = color
        )
    }
}
