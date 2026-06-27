package com.example.todowithspirits.feature.add.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.theme.SplitsTodoTheme
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun TimeWheelPicker(
    initialHour: Int = 0,
    initialMinute: Int = 0,
    onTimeSelected: (Int, Int) -> Unit
) {
    var selectedHour by remember { mutableIntStateOf(initialHour) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(35.dp)
                .padding(horizontal = 14.dp)
                .clip(RoundedCornerShape(81.dp))
                .background(SplitsTodoTheme.colors.selectedTimeBoxColor)
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            WheelColumn(
                items = (0..23).toList(),
                initialIndex = initialHour,
                onItemSelected = {
                    selectedHour = it
                    onTimeSelected(selectedHour, selectedMinute)
                }
            )

            Text(
                text = ":",
                fontSize = 28.sp,
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                color = SplitsTodoTheme.colors.selectedDateTextColor
            )

            WheelColumn(
                items = (0..59).toList(),
                initialIndex = initialMinute,
                onItemSelected = {
                    selectedMinute = it
                    onTimeSelected(selectedHour, selectedMinute)
                }
            )
        }
    }
}

@Composable
private fun WheelColumn(
    items: List<Int>,
    initialIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    val itemHeight = 45.dp
    val visibleItemsCount = 5
    val repeatCount = 1000
    val totalItems = items.size * repeatCount
    val middleOffset = (repeatCount / 2) * items.size
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = middleOffset + initialIndex)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val scope = rememberCoroutineScope()
    val currentSelectedActualIndex by remember {
        derivedStateOf { listState.firstVisibleItemIndex % items.size }
    }

    LaunchedEffect(currentSelectedActualIndex) {
        onItemSelected(items[currentSelectedActualIndex])
    }

    Box(
        modifier = Modifier
            .width(70.dp)
            .height(itemHeight * visibleItemsCount),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = snapFlingBehavior,
            contentPadding = PaddingValues(vertical = itemHeight * 2),
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                count = totalItems,
                key = { it }
            ) { index ->
                val actualIndex = index % items.size

                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .graphicsLayer {
                            val layoutInfo = listState.layoutInfo
                            val visibleItem = layoutInfo.visibleItemsInfo.find { it.index == index }

                            if (visibleItem != null) {
                                val viewportCenter = (layoutInfo.viewportEndOffset + layoutInfo.viewportStartOffset) / 2f
                                val itemCenter = visibleItem.offset + visibleItem.size / 2f
                                val distanceFromCenter = itemCenter - viewportCenter
                                val fraction = (distanceFromCenter / (itemHeight.toPx() * 2.5f)).coerceIn(-1f, 1f)

                                rotationX = fraction * 70f
                                scaleX = 1f - (abs(fraction) * 0.4f)
                                scaleY = scaleX
                                translationY = -distanceFromCenter * 0.1f
                                cameraDistance = 16f * density
                            }
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            scope.launch { listState.animateScrollToItem(index) }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val isSelected = currentSelectedActualIndex == actualIndex && (listState.firstVisibleItemIndex == index)

                    Text(
                        text = actualIndex.toString().padStart(2, '0'),
                        fontSize = 28.sp,
                        color = if (isSelected) SplitsTodoTheme.colors.selectedDateTextColor else SplitsTodoTheme.colors.textColor1
                    )
                }
            }
        }
    }
}
