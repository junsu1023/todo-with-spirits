package com.example.todowithspirits.feature.home.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.example.todowithspirits.R
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.feature.home.state.HomeTodayTask
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.util.KoreanHourMinuteFormatter
import com.example.todowithspirits.util.KoreanMonthDayWithDayFormatter

/*
오늘 일정 항목을 눌렀을 때 뜨는 상세 팝업 (피그마 home3).
화면 전체(상태바·하단 버튼 포함)를 딤 처리해야 해서 Dialog 창의 dim을 dimColor 투명도로 맞춘다.
 */
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
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        val dimAmount = SpiritTodoTheme.color.dimColor.alpha
        SideEffect { window?.setDimAmount(dimAmount) }

        Column(
            modifier = Modifier
                .width(274.dp)
                .background(SpiritTodoTheme.color.surfaceColor1, RoundedCornerShape(8.dp))
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SpiritTodoTheme.color.onSurfaceColor5,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (task.isImportant) {
                    Image(
                        painter = painterResource(R.drawable.fi_rr_color_star),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = buildString {
                    append(task.date.format(KoreanMonthDayWithDayFormatter))
                    task.time?.let { append(" ${it.format(KoreanHourMinuteFormatter)}") }
                },
                fontSize = 14.sp,
                color = SpiritTodoTheme.color.systemGrey,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TodoDetailActionButton(
                    iconRes = R.drawable.todo_trash,
                    label = stringResource(R.string.delete),
                    color = SpiritTodoTheme.color.systemRed,
                    onClick = onDelete,
                    modifier = Modifier.weight(1f)
                )

                TodoDetailActionButton(
                    iconRes = R.drawable.todo_pencil,
                    label = stringResource(R.string.modify),
                    color = SpiritTodoTheme.color.todoTextMain,
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                )
            }
        }
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
            .background(SpiritTodoTheme.color.surfaceColor1, RoundedCornerShape(6.dp))
            .border(0.5.dp, color, RoundedCornerShape(6.dp))
            .noRippleClickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )

        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}
