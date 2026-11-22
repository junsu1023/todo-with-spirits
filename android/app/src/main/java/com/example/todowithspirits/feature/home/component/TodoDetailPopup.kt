package com.example.todowithspirits.feature.home.component

import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.todowithspirits.R
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.feature.home.state.HomeTodayTask
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.util.KoreanHourMinuteFormatter
import com.example.todowithspirits.util.KoreanMonthDayWithDayFormatter
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun TodoDetailPopup(
    task: HomeTodayTask,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .width(274.dp)
                .background(SpiritTodoTheme.color.white, RoundedCornerShape(8.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Title(
                    title = task.title,
                    isImportant = task.isImportant,
                    modifier = Modifier.padding(horizontal = 28.dp)
                )

                Spacer(Modifier.height(4.dp))

                Detail(task.date, task.time)

                Spacer(Modifier.height(18.dp))

                ActionButtonArea(onDelete, onEdit)
            }

            CloseButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}
@Composable
private fun CloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(R.drawable.widgetly_cross_24),
        contentDescription = null,
        colorFilter = ColorFilter.tint(SpiritTodoTheme.color.stroke01),
        modifier = modifier
            .padding(top = 2.dp, end = 4.dp)
            .noRippleClickable(onClick = onClick)
            .padding(8.dp)
    )
}

@Composable
private fun Title(
    title: String,
    isImportant: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = SpiritTodoTheme.color.font01,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f, fill = false)
        )

        if (isImportant) {
            Image(
                painter = painterResource(R.drawable.widgetly_start_14),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                colorFilter = ColorFilter.tint(SpiritTodoTheme.color.key02)
            )
        }
    }
}

@Composable
private fun Detail(date: LocalDate, time: LocalTime?) {
    Text(
        text = buildString {
            append(date.format(KoreanMonthDayWithDayFormatter))
            time?.let { append(" ${it.format(KoreanHourMinuteFormatter)}") }
        },
        fontSize = 14.sp,
        color = SpiritTodoTheme.color.fontGrey,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun ActionButtonArea(
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TodoDetailActionButton(
            iconRes = R.drawable.widgetly_trash_20,
            label = stringResource(R.string.delete),
            color = SpiritTodoTheme.color.colorSub02,
            onClick = onDelete,
            modifier = Modifier.weight(1f)
        )

        TodoDetailActionButton(
            iconRes = R.drawable.widgetly_pencil_20,
            label = stringResource(R.string.modify),
            color = SpiritTodoTheme.color.font01,
            onClick = onEdit,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TodoDetailActionButton(
    @DrawableRes iconRes: Int,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(56.dp)
            .background(SpiritTodoTheme.color.white, RoundedCornerShape(6.dp))
            .border(0.5.dp, color, RoundedCornerShape(6.dp))
            .noRippleClickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null
        )

        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}
