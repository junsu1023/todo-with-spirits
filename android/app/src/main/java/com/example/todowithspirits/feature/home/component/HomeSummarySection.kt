package com.example.todowithspirits.feature.home.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.component.SpiritsTodoCheckbox
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.feature.home.state.HomeTodayTask
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.util.baselineAtBottom

/**
 * @param onCollapsedHeightChanged 접힌 부분(제목 + 숫자 줄)의 높이. 펼친 목록이 아래 콘텐츠 위로 겹치도록
 * HomeScreen이 이 높이만큼 자리를 비워둔다.
 */
@Composable
fun HomeTodaySummaryCard(
    completedCount: Int,
    totalCount: Int,
    tasks: List<HomeTodayTask>,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onTaskCompletionChange: (id: Long, isCompleted: Boolean) -> Unit,
    onTaskClick: (id: Long) -> Unit,
    onCollapsedHeightChanged: (Dp) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val hasTasks = tasks.isNotEmpty()
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "todayTasksArrowRotation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SpiritTodoTheme.color.white, RoundedCornerShape(8.dp))
            .animateContentSize()
            .border(1.dp, SpiritTodoTheme.color.stroke01, RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp)
    ) {
        Column(
            modifier = Modifier
                .onSizeChanged { onCollapsedHeightChanged(with(density) { it.height.toDp() }) }
                .padding(vertical = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.today_task),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.color.font01
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "$completedCount",
                        fontSize = 50.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SpiritTodoTheme.color.font01,
                        modifier = Modifier.baselineAtBottom(39.dp)
                    )

                    Spacer(Modifier.width(2.dp))

                    Text(
                        text = "/ $totalCount",
                        fontSize = 20.sp,
                        color = SpiritTodoTheme.color.font02,
                        modifier = Modifier.baselineAtBottom(24.dp)
                    )
                }

                if (hasTasks) {
                    Image(
                        painter = painterResource(R.drawable.widgetly_down_24),
                        contentDescription = null,
                        modifier = Modifier
                            .noRippleClickable(onClick = { onToggleExpanded() })
                            .rotate(arrowRotation)
                    )
                }
            }
        }

        if (isExpanded && hasTasks) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 150.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = tasks, key = { it.id }) { task ->
                    HomeTodayTaskItem(
                        task = task,
                        onCompletionChange = { onTaskCompletionChange(task.id, it) },
                        onClick = { onTaskClick(task.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeTodayTaskItem(
    task: HomeTodayTask,
    onCompletionChange: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SpiritsTodoCheckbox(
            checked = task.isCompleted,
            onCheckedChange = onCompletionChange,
            checkedIcon = painterResource(R.drawable.widgetly_circle_check_on_18),
            uncheckedIcon = painterResource(R.drawable.widgetly_circle_check_off_18)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            modifier = Modifier
                .weight(1f)
                .height(20.dp)
                .noRippleClickable(onClick = onClick),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = task.title,
                fontSize = 14.sp,
                color = when(task.isCompleted) {
                    true -> SpiritTodoTheme.color.fontGrey
                    else -> SpiritTodoTheme.color.font01
                },
                textDecoration = when(task.isCompleted) {
                    true -> TextDecoration.LineThrough
                    else -> null
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )

            if (task.isImportant) {
                Image(
                    painter = painterResource(R.drawable.widgetly_start_14),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(SpiritTodoTheme.color.key02)
                )
            }
        }
    }
}

@Composable
fun HomeStatusChipRow(
    importantCount: Int,
    completedCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (importantCount > 0) {
            HomeStatusChip(
                iconRes = R.drawable.widgetly_start_14,
                text = stringResource(R.string.has_important, importantCount),
                backgroundColor = SpiritTodoTheme.color.key02
            )
        }

        HomeStatusChip(
            iconRes = R.drawable.widgetly_check_14,
            text = "완료 ($completedCount)",
            backgroundColor = SpiritTodoTheme.color.key01
        )
    }
}

@Composable
private fun HomeStatusChip(
    @DrawableRes iconRes: Int,
    text: String,
    backgroundColor: Color
) {
    Row(
        modifier = Modifier
            .height(26.dp)
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(14.dp)
        )

        Text(
            text = text,
            fontSize = 12.sp,
            color = SpiritTodoTheme.color.white
        )
    }
}
