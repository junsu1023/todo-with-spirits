package com.example.todowithspirits.feature.plan

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.component.TitleHeader
import com.example.todowithspirits.theme.SpiritTodoTheme
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PlanDetailScreen(
    itemId: Int,
    onBack: () -> Unit,
    navigateToAdd: () -> Unit
) {
    val item = dummyPlans.find { it.id == itemId } ?: return
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy. MM. dd (E)", Locale.KOREAN) }
    val dateText = buildString {
        item.dueDate?.let { date ->
            append(date.format(dateFormatter))
            if (item.dueTime != null) {
                append(" " + item.dueTime.format(DateTimeFormatter.ofPattern("HH:mm")))
            } else {
                append(" 하루 종일")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpiritTodoTheme.color.surfaceColor1)
    ) {
        TitleHeader(
            leftIconRes = R.drawable.todo_back1,
            onLeftIconClick = onBack,
            rightComposable =  {
                Row(horizontalArrangement = Arrangement.spacedBy(26.dp)) {
                    Image(
                        painter = painterResource(R.drawable.todo_pencil),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = navigateToAdd
                            )
                    )

                    Image(
                        painter = painterResource(R.drawable.todo_trash),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(SpiritTodoTheme.color.onSurfaceColor1),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 28.dp)
        ) {
            Box(
                modifier = Modifier
                    .border(1.dp, SpiritTodoTheme.color.surfaceColor2, RoundedCornerShape(30.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (item.type == PlanType.TODO) stringResource(R.string.todo) else stringResource(R.string.routine),
                    color = SpiritTodoTheme.color.onSurfaceColor4,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(18.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = SpiritTodoTheme.color.onSurfaceColor1,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (item.isImportant) {
                    Spacer(Modifier.width(4.dp))

                    Image(
                        painter = painterResource(R.drawable.todo_important),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (dateText.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))

                Text(
                    text = dateText,
                    fontSize = 14.sp,
                    color = SpiritTodoTheme.color.onSurfaceColor1
                )
            }

            Spacer(Modifier.height(28.dp))

            HorizontalDivider(
                thickness = 1.dp,
                color = SpiritTodoTheme.color.onSurfaceColor2
            )

            Spacer(Modifier.height(30.dp))

            DetailInfoRow(
                iconRes = R.drawable.todo_alarm,
                text = "10분 전"
            )

            Spacer(Modifier.height(26.dp))

            DetailInfoRow(
                iconRes = R.drawable.todo_category,
                text = item.category ?: "-"
            )

            Spacer(Modifier.height(26.dp))

            DetailInfoRow(
                iconRes = R.drawable.todo_private,
                text = "비공개"
            )

            Spacer(Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp)
                    .border(1.dp, SpiritTodoTheme.color.onSurfaceColor2, RoundedCornerShape(6.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = item.memo.ifEmpty { "메모" },
                    fontSize = 15.sp,
                    color = if(item.memo.isEmpty()) SpiritTodoTheme.color.onSurfaceColor2 else SpiritTodoTheme.color.onSurfaceColor1
                )
            }

            Spacer(Modifier.height(24.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .background(SpiritTodoTheme.color.surfaceColor3, RoundedCornerShape(6.dp))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onBack() }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.check),
                color = SpiritTodoTheme.color.onSurfaceColor3,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DetailInfoRow(iconRes: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = text,
            fontSize = 14.sp,
            color = SpiritTodoTheme.color.onSurfaceColor1
        )
    }
}
