package com.example.todowithspirits.feature.plan.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CategoryOption
import com.example.todowithspirits.R
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.feature.plan.model.PlanItemData
import com.example.todowithspirits.feature.plan.model.PlanType
import com.example.todowithspirits.theme.SpiritTodoTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun PlanListItem(
    item: PlanItemData,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onPostpone: () -> Unit,
    onToggleComplete: () -> Unit,
    navigateToDetail: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val rightPanelWidthDp = 60.dp
    val leftPanelWidthDp = 61.dp
    val maxReveal = remember(density) { with(density) { -rightPanelWidthDp.toPx() } }
    val maxPostpone = remember(density) { with(density) { leftPanelWidthDp.toPx() } }
    val offsetX = remember { Animatable(0f) }

    /* 드래그가 시작된 시점의 앵커(닫힘/왼쪽 열림/오른쪽 열림)에 인접한 한 단계 범위로만
       이동을 제한한다. 예를 들어 왼쪽으로 열린 상태라면 [maxReveal, 0] 범위 안에서만
       움직여서, 아무리 크게 반대로 드래그해도 반대쪽 끝까지 한 번에 넘어가지 않는다. */
    var dragMinBound by remember { mutableFloatStateOf(maxReveal) }
    var dragMaxBound by remember { mutableFloatStateOf(maxPostpone) }

    fun closeThen(action: () -> Unit): () -> Unit = {
        coroutineScope.launch { offsetX.animateTo(0f, spring()) }
        action()
    }
    val typeColor = when (item.type) {
        PlanType.TODO -> SpiritTodoTheme.color.surfaceColor8
        PlanType.ROUTINE -> SpiritTodoTheme.color.surfaceColor9
    }
    val dDay = item.dueDate?.let { ChronoUnit.DAYS.between(LocalDate.now(), it).toInt() }
    val dDayText = when {
        dDay == null -> null
        dDay == 0 -> "D-Day"
        dDay > 0 -> "D-$dDay"
        else -> "D+${-dDay}"
    }
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("yy. MM. dd (E) a hh:mm", Locale.KOREAN)
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.matchParentSize(),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .width(leftPanelWidthDp)
                    .fillMaxHeight()
                    .padding(end = 8.dp)
                    .background(SpiritTodoTheme.color.surfaceColor10, RoundedCornerShape(6.dp))
                    .noRippleClickable(onClick = closeThen(onPostpone)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.fi_rr_arrow_right),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(SpiritTodoTheme.color.onSurfaceColor8)
                )
            }
        }

        Row(
            modifier = Modifier.matchParentSize(),
            horizontalArrangement = Arrangement.End
        ) {
            Column(
                modifier = Modifier
                    .width(rightPanelWidthDp)
                    .fillMaxHeight()
                    .padding(start = 8.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(SpiritTodoTheme.color.surfaceColor1, RoundedCornerShape(6.dp))
                        .border(1.dp, SpiritTodoTheme.color.onSurfaceColor7, RoundedCornerShape(6.dp))
                        .noRippleClickable(onClick = closeThen(onDelete)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.fi_rr_trash),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(SpiritTodoTheme.color.onSurfaceColor7)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(SpiritTodoTheme.color.surfaceColor1, RoundedCornerShape(6.dp))
                        .border(1.dp, SpiritTodoTheme.color.onSurfaceColor8, RoundedCornerShape(6.dp))
                        .noRippleClickable(onClick = closeThen(onEdit)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.fi_rr_pencil),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(SpiritTodoTheme.color.onSurfaceColor8)
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        coroutineScope.launch {
                            offsetX.snapTo(
                                (offsetX.value + delta).coerceIn(dragMinBound, dragMaxBound)
                            )
                        }
                    },
                    onDragStarted = {
                        val anchor = listOf(0f, maxReveal, maxPostpone)
                            .minBy { abs(it - offsetX.value) }
                        when (anchor) {
                            maxReveal -> { dragMinBound = maxReveal; dragMaxBound = 0f }
                            maxPostpone -> { dragMinBound = 0f; dragMaxBound = maxPostpone }
                            else -> { dragMinBound = maxReveal; dragMaxBound = maxPostpone }
                        }
                    },
                    onDragStopped = {
                        coroutineScope.launch {
                            val midpoint = (dragMinBound + dragMaxBound) / 2f
                            val target = if (offsetX.value <= midpoint) dragMinBound else dragMaxBound
                            offsetX.animateTo(target, spring())
                        }
                    }
                )
                .noRippleClickable {
                    if (offsetX.value == 0f) navigateToDetail()
                },
            color = SpiritTodoTheme.color.surfaceColor12,
            shape = RoundedCornerShape(6.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(130.dp)
                        .background(typeColor)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row {
                            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                                Checkbox(
                                    checked = item.isDone,
                                    onCheckedChange = { onToggleComplete() },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = typeColor,
                                        uncheckedColor = SpiritTodoTheme.color.onSurfaceColor8
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = item.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SpiritTodoTheme.color.onSurfaceColor5,
                                textDecoration = if(item.isDone) TextDecoration.LineThrough else TextDecoration.None
                            )

                            if (item.isImportant) {
                                Spacer(modifier = Modifier.width(4.dp))

                                Image(
                                    painter = painterResource(R.drawable.fi_rr_color_star),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        if (dDayText != null && item.type != PlanType.ROUTINE) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Text(
                                    text = dDayText,
                                    fontSize = 18.sp,
                                    color = if(dDayText == "D-Day") SpiritTodoTheme.color.mainTextAndStroke else SpiritTodoTheme.color.todoTextMain,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.padding(start = 34.dp)) {
                        val dateTimeText = buildString {
                            item.dueDate?.let { date ->
                                val time = item.dueTime ?: LocalTime.of(0, 0)
                                append(LocalDateTime.of(date, time).format(dateFormatter))
                            }
                        }
                        if (dateTimeText.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = dateTimeText,
                                fontSize = 12.sp,
                                color = SpiritTodoTheme.color.todoTextMain,
                                fontWeight = FontWeight.Light
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "(${item.memo.ifEmpty { "메모 없음" }})",
                            fontSize = 14.sp,
                            color = SpiritTodoTheme.color.onSurfaceColor8
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            TagChip(item.category ?: CategoryOption.NONE.displayName)

                            item.repeatInfo?.let { TagChip(it) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TagChip(text: String) {
    Box(
        modifier = Modifier
            .border(0.8.dp, SpiritTodoTheme.color.onSurfaceColor8, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = SpiritTodoTheme.color.onSurfaceColor8
        )
    }
}