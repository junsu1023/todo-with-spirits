package com.example.todowithspirits.feature.today.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.todowithspirits.R
import com.example.todowithspirits.component.BottomBarHeight
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.theme.SpiritTodoTheme
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun TodoDetailBottomSheet(
    title: String,
    isImportant: Boolean,
    dueDate: LocalDate?,
    dueTime: LocalTime?,
    memo: String,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onPostpone: () -> Unit,
    onEdit: () -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yy. MM. dd (E)", Locale.KOREAN) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("a HH:mm", Locale.KOREAN) }

    val dDay = dueDate?.let { ChronoUnit.DAYS.between(LocalDate.now(), it).toInt() }
    val dDayText = when {
        dDay == null -> null
        dDay == 0 -> "D-Day"
        dDay > 0 -> "D-$dDay"
        else -> "D+${-dDay}"
    }

    Popup(
        onDismissRequest = onDismiss,
        properties = PopupProperties(
            focusable = true,
            dismissOnClickOutside = true,
            dismissOnBackPress = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(bottom = BottomBarHeight)
                .imePadding(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SpiritTodoTheme.color.dimColor)
                    .noRippleClickable { onDismiss() }
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SpiritTodoTheme.color.surfaceColor1,
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SpiritTodoTheme.color.onSurfaceColor5,
                                    textAlign = TextAlign.Center
                                )

                                if (isImportant) {
                                    Spacer(Modifier.width(4.dp))

                                    Image(
                                        painter = painterResource(R.drawable.todo_important),
                                        contentDescription = null
                                    )
                                }
                            }

                            if (dueDate != null) {
                                Spacer(Modifier.height(2.dp))

                                val dateText = buildString {
                                    append("마감일 : ")
                                    append(dueDate.format(dateFormatter))
                                    if (dueTime != null) append(" ${dueTime.format(timeFormatter)}")
                                }

                                Text(
                                    text = dateText,
                                    fontSize = 14.sp,
                                    color = SpiritTodoTheme.color.systemGrey,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Image(
                            painter = painterResource(R.drawable.todo_cross),
                            contentDescription = null,
                            modifier = Modifier
                                .size(12.dp)
                                .align(Alignment.TopEnd)
                                .noRippleClickable { onDismiss() }
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    if (dDayText != null) {
                        Text(
                            text = dDayText,
                            fontSize = 22.sp,
                            color = SpiritTodoTheme.color.mainTextAndStroke,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(12.dp))
                    }

                    Text(
                        text = memo.ifEmpty { "메모 없음" },
                        fontSize = 14.sp,
                        color = SpiritTodoTheme.color.surfaceColor10,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DetailActionButton(
                            modifier = Modifier.weight(1f),
                            iconRes = R.drawable.todo_trash,
                            label = stringResource(R.string.delete),
                            color = SpiritTodoTheme.color.surfaceColor11,
                            onClick = onDelete
                        )

                        DetailActionButton(
                            modifier = Modifier.weight(1f),
                            iconRes = R.drawable.todo_arrow,
                            label = stringResource(R.string.delay),
                            color = SpiritTodoTheme.color.todoTextMain,
                            onClick = onPostpone
                        )

                        DetailActionButton(
                            modifier = Modifier.weight(1f),
                            iconRes = R.drawable.todo_pencil,
                            label = stringResource(R.string.modify),
                            color = SpiritTodoTheme.color.todoTextMain,
                            onClick = onEdit
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailActionButton(
    modifier: Modifier = Modifier,
    iconRes: Int,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .border(1.dp, color, RoundedCornerShape(6.dp))
            .noRippleClickable { onClick() }
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = label,
            fontSize = 12.sp,
            color = color
        )
    }
}
