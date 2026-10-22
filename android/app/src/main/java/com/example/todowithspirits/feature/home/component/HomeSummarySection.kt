package com.example.todowithspirits.feature.home.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.component.SpiritsTodoCheckbox
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.feature.home.state.HomeTodayTask
import com.example.todowithspirits.theme.SpiritTodoTheme

// home3: 펼친 카드 244 = 접힌 94 + 목록 150. 넘치면 목록이 스크롤된다.
private val TodayTaskListMaxHeight = 150.dp

// 피그마의 숫자 박스 높이 (폰트 위아래 여백을 뺀 글자 높이)
private val CompletedCountBoxHeight = 39.dp
private val TotalCountBoxHeight = 24.dp

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
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "todayTasksArrowRotation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SpiritTodoTheme.color.systemBackground, RoundedCornerShape(6.dp))
            .animateContentSize()
            // 카드는 아래 콘텐츠 위에 겹쳐 그려지므로, 카드 어디를 눌러도 아래(정령 등)로 터치가 넘어가지 않게 카드가 받는다.
            .noRippleClickable(onClick = onToggleExpanded)
            .padding(horizontal = 14.dp)
    ) {
        Column(
            modifier = Modifier
                .onSizeChanged { onCollapsedHeightChanged(with(density) { it.height.toDp() }) }
                .padding(vertical = 12.dp)
        ) {
            Text(
                text = "오늘 일정",
                fontSize = 16.sp,
                color = SpiritTodoTheme.color.todoTextMain
            )

            Spacer(Modifier.height(12.dp))

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
                        fontSize = 53.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SpiritTodoTheme.color.todoTextMain,
                        modifier = Modifier.baselineAtBottom(CompletedCountBoxHeight)
                    )

                    Spacer(Modifier.width(4.dp))

                    Text(
                        text = "/ $totalCount",
                        fontSize = 20.sp,
                        color = SpiritTodoTheme.color.systemGrey,
                        modifier = Modifier.baselineAtBottom(TotalCountBoxHeight)
                    )
                }

                Image(
                    painter = painterResource(R.drawable.fi_rr_angle_small_down),
                    contentDescription = null,
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(arrowRotation)
                )
            }
        }

        if (isExpanded) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = TodayTaskListMaxHeight),
                contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp),
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

/*
큰 숫자는 폰트 자체의 위아래 여백(ascent/descent) 때문에 피그마보다 훨씬 크게 잡힌다.
텍스트는 원래 크기로 그리되(잘리지 않게), 레이아웃에는 기준선을 하단에 맞춘 height만큼만 차지하도록 보고한다.
숫자와 '/'는 기준선 아래로 내려가는 부분이 거의 없어 하단 정렬만으로 피그마와 같은 모양이 된다.
 */
private fun Modifier.baselineAtBottom(height: Dp) = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity))
    val boxHeight = height.roundToPx()
    val baseline = placeable[FirstBaseline]

    layout(placeable.width, boxHeight, mapOf(FirstBaseline to boxHeight)) {
        placeable.place(0, boxHeight - baseline)
    }
}

// 체크 영역은 완료 전환, 나머지(제목) 영역은 상세 팝업을 연다.
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
        // 완료 상태 디자인이 없어 이전 오늘 일정과 같은 체크 아이콘을 쓴다.
        SpiritsTodoCheckbox(
            modifier = Modifier.size(20.dp),
            checked = task.isCompleted,
            onCheckedChange = onCompletionChange,
            checkedIcon = painterResource(R.drawable.todo_icon_check_pp),
            uncheckedIcon = painterResource(R.drawable.ic_check_circle_empty)
        )

        Row(
            modifier = Modifier
                .weight(1f)
                .height(20.dp)
                .noRippleClickable(onClick = onClick)
                .padding(start = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = task.title,
                fontSize = 14.sp,
                color = if (task.isCompleted) SpiritTodoTheme.color.systemGrey else SpiritTodoTheme.color.todoTextMain,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
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
                iconRes = R.drawable.fi_rr_color_star,
                text = "중요 일정이 ${importantCount}건 있어요!",
                backgroundColor = SpiritTodoTheme.color.mainBackground
            )
        }

        HomeStatusChip(
            iconRes = R.drawable.ic_check_routine,
            text = "완료 ($completedCount)",
            backgroundColor = SpiritTodoTheme.color.routineBackground
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
        horizontalArrangement = Arrangement.spacedBy(4.dp),
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
            color = SpiritTodoTheme.color.todoTextMain
        )
    }
}
