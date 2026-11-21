package com.example.todowithspirits.feature.calendar.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import java.time.LocalDate
import java.time.LocalTime

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
    val animatedHeight by animateDpAsState(
        targetValue = if(isExpanded) 103.dp else 61.dp,
        animationSpec = tween(durationMillis = 300),
        label = "heightAnimation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SpiritTodoTheme.color.white, RoundedCornerShape(8.dp))
            .border(
                1.dp,
                if(isExpanded) SpiritTodoTheme.color.font02 else SpiritTodoTheme.color.stroke01,
                RoundedCornerShape(8.dp))
            .height(animatedHeight)
            .noRippleClickable(onClick = onToggleExpanded)
            .padding(12.dp)
    ) {
        TaskCardHeader(
            title = item.title,
            isDone = item.isDone,
            isImportant = item.isImportant,
            isExpanded = isExpanded,
            onToggleComplete = onToggleComplete
        )

        TaskDateTime(
            endDate = item.endDate,
            endTime = item.endTime,
            isAllDay = item.isAllDay
        )

        if (isExpanded) {
            Spacer(Modifier.height(10.dp))

            TaskButtons(
                onDelete = onDelete,
                onEdit = onEdit
            )
        }
    }
}

@Composable
private fun TaskCardHeader(
    title: String,
    isDone: Boolean,
    isImportant: Boolean,
    isExpanded: Boolean,
    onToggleComplete: () -> Unit
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "calendarTaskArrowRotation"
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        SpiritsTodoCheckbox(
            checked = isDone,
            onCheckedChange = { onToggleComplete() },
            checkedIcon = painterResource(R.drawable.widgetly_circle_check_on_18),
            uncheckedIcon = painterResource(R.drawable.widgetly_circle_check_off_18)
        )

        TaskTitle(
            modifier = Modifier.weight(1f),
            title = title,
            isDone = isDone,
            isImportant = isImportant
        )

        Image(
            painter = painterResource(R.drawable.widgetly_down_18),
            contentDescription = null,
            modifier = Modifier.rotate(arrowRotation)
        )
    }
}

@Composable
private fun TaskTitle(
    modifier: Modifier,
    title: String,
    isDone: Boolean,
    isImportant: Boolean
) {
    Row(
        modifier = modifier.padding(start = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = if(isDone) SpiritTodoTheme.color.fontGrey else SpiritTodoTheme.color.font01,
            textDecoration = if(isDone) TextDecoration.LineThrough else null,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )

        if (isImportant) {
            Image(
                painter = painterResource(R.drawable.widgetly_start_14),
                contentDescription = null,
                colorFilter = ColorFilter.tint(SpiritTodoTheme.color.key02)
            )
        }
    }
}

@Composable
private fun TaskDateTime(
    endDate: LocalDate?,
    endTime: LocalTime?,
    isAllDay: Boolean
) {
    endDate?.let { date ->
        Text(
            text = buildString {
                append(date.format(KoreanMonthDayWithDayFormatter))
                if (!isAllDay) endTime?.let { append(" ${it.format(KoreanHourMinuteFormatter)}") }
            },
            fontSize = 12.sp,
            color = SpiritTodoTheme.color.font02,
            modifier = Modifier.padding(start = 26.dp, top = 4.dp)
        )
    }
}

@Composable
private fun TaskButtons(
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CalendarTaskActionButton(
            text = stringResource(R.string.delete),
            fontColor = SpiritTodoTheme.color.colorSub02,
            borderColor = SpiritTodoTheme.color.colorSub02,
            onClick = onDelete,
            modifier = Modifier.weight(1f)
        )

        CalendarTaskActionButton(
            text = stringResource(R.string.modify),
            fontColor = SpiritTodoTheme.color.font01,
            borderColor = SpiritTodoTheme.color.stroke01,
            onClick = onEdit,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CalendarTaskActionButton(
    text: String,
    fontColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(32.dp)
            .background(SpiritTodoTheme.color.white, RoundedCornerShape(6.dp))
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .noRippleClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = fontColor
        )
    }
}
