package com.example.todowithspirits.feature.plan.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.feature.plan.PlanItemData
import com.example.todowithspirits.feature.plan.PlanType
import com.example.todowithspirits.theme.SpiritTodoTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun PlanListItem(
    item: PlanItemData,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onPostpone: () -> Unit,
    navigateToDetail: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val rightPanelWidthDp = 60.dp
    val leftPanelWidthDp = 61.dp
    val maxReveal = remember(density) { with(density) { -rightPanelWidthDp.toPx() } }
    val maxPostpone = remember(density) { with(density) { leftPanelWidthDp.toPx() } }
    val offsetX = remember { Animatable(0f) }
    val typeColor = when (item.type) {
        PlanType.TODO -> SpiritTodoTheme.colors.surfaceColor5
        PlanType.ROUTINE -> SpiritTodoTheme.colors.onSurfaceColor5
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
                    .background(SpiritTodoTheme.colors.surfaceColor4, RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        coroutineScope.launch { offsetX.animateTo(0f, spring()) }
                        onPostpone()
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.fi_rr_arrow_right),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(SpiritTodoTheme.colors.onSurfaceColor7)
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
                        .background(SpiritTodoTheme.colors.white, RoundedCornerShape(6.dp))
                        .border(1.dp, SpiritTodoTheme.colors.onSurfaceColor6, RoundedCornerShape(6.dp))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            coroutineScope.launch { offsetX.animateTo(0f, spring()) }
                            onDelete()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.fi_rr_trash),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(SpiritTodoTheme.colors.onSurfaceColor6)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(SpiritTodoTheme.colors.white, RoundedCornerShape(6.dp))
                        .border(1.dp, SpiritTodoTheme.colors.onSurfaceColor2, RoundedCornerShape(6.dp))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            coroutineScope.launch { offsetX.animateTo(0f, spring()) }
                            onEdit()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.fi_rr_pencil),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(SpiritTodoTheme.colors.onSurfaceColor7)
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
                                (offsetX.value + delta).coerceIn(maxReveal, maxPostpone)
                            )
                        }
                    },
                    onDragStopped = {
                        coroutineScope.launch {
                            val target = when {
                                offsetX.value < maxReveal / 2 -> maxReveal
                                offsetX.value > maxPostpone / 2 -> maxPostpone
                                else -> 0f
                            }
                            offsetX.animateTo(target, spring())
                        }
                    }
                )
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    if (offsetX.value == 0f) navigateToDetail()
                },
            color = SpiritTodoTheme.colors.homeColor,
            shadowElevation = 1.dp,
            shape = RoundedCornerShape(6.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(124.dp)
                        .background(typeColor)
                )

                Box(modifier = Modifier.padding(top = 14.dp, start = 14.dp, end = 12.dp)) {
                    Checkbox(
                        checked = item.isDone,
                        onCheckedChange = null,
                        colors = CheckboxDefaults.colors(
                            checkedColor = typeColor,
                            uncheckedColor = SpiritTodoTheme.colors.onSurfaceColor2
                        )
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 14.dp, bottom = 8.dp, end = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SpiritTodoTheme.colors.mainTextColor
                        )

                        if (item.isImportant) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Image(
                                painter = painterResource(R.drawable.fi_rr_color_star),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        if (dDayText != null) {
                            Text(
                                text = dDayText,
                                fontSize = 16.sp,
                                color = SpiritTodoTheme.colors.onSurfaceColor1,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

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
                            color = SpiritTodoTheme.colors.mainTextColor,
                            fontWeight = FontWeight.Light
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "(${item.memo.ifEmpty { "메모 없음" }})",
                        fontSize = 14.sp,
                        color = SpiritTodoTheme.colors.onSurfaceColor2
                    )

                    if (item.category != null || item.repeatInfo != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            item.category?.let { TagChip(it) }
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
            .border(0.8.dp, SpiritTodoTheme.colors.onSurfaceColor2, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = SpiritTodoTheme.colors.onSurfaceColor2
        )
    }
}