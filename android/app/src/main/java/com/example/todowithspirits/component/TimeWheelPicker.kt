package com.example.todowithspirits.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
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
import com.example.todowithspirits.theme.SpiritTodoTheme
import kotlinx.coroutines.launch
import kotlin.math.abs

private val HOURS = (0..23).toList()
private val MINUTES = (0..59).toList()
private val ITEM_HEIGHT = 45.dp
private const val VISIBLE_ITEMS_COUNT = 5
private const val REPEAT_COUNT = 300

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
            .height(ITEM_HEIGHT * VISIBLE_ITEMS_COUNT),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(35.dp)
                .padding(horizontal = 14.dp)
                .clip(RoundedCornerShape(81.dp))
                .background(SpiritTodoTheme.color.surfaceColor13)
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            WheelColumn(
                items = HOURS,
                initialIndex = initialHour,
                onItemSelected = {
                    selectedHour = it
                    onTimeSelected(selectedHour, selectedMinute)
                }
            )

            Text(
                text = ":",
                fontSize = 28.sp,
                modifier = Modifier.padding(horizontal = 12.dp),
                color = SpiritTodoTheme.color.mainTextAndStroke
            )

            WheelColumn(
                items = MINUTES,
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
    val itemCount = items.size
    val totalItems = itemCount * REPEAT_COUNT
    val middleOffset = (REPEAT_COUNT / 2) * itemCount
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = middleOffset + initialIndex)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val scope = rememberCoroutineScope()
    val selectedIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    val visibleItemsMap by remember { derivedStateOf { listState.layoutInfo.visibleItemsInfo.associateBy { it.index } } }
    val viewportCenter by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            (info.viewportEndOffset + info.viewportStartOffset) / 2f
        }
    }
    var isInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(selectedIndex) {
        if (isInitialized) {
            onItemSelected(items[selectedIndex % itemCount])
        } else {
            isInitialized = true
        }
    }

    Box(
        modifier = Modifier
            .width(35.dp)
            .height(ITEM_HEIGHT * VISIBLE_ITEMS_COUNT),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = snapFlingBehavior,
            contentPadding = PaddingValues(vertical = ITEM_HEIGHT * 2),
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                count = totalItems,
                key = { it }
            ) { index ->
                val actualIndex = index % itemCount

                Box(
                    modifier = Modifier
                        .height(ITEM_HEIGHT)
                        .fillMaxWidth()
                        .graphicsLayer {
                            val visibleItem = visibleItemsMap[index]
                            if (visibleItem != null) {
                                val distanceFromCenter = (visibleItem.offset + visibleItem.size / 2f) - viewportCenter
                                val fraction = (distanceFromCenter / (ITEM_HEIGHT.toPx() * 2.5f)).coerceIn(-1f, 1f)
                                val scale = 1f - (abs(fraction) * 0.4f)

                                rotationX = fraction * 70f
                                scaleX = scale
                                scaleY = scale
                                translationY = -distanceFromCenter * 0.1f
                                cameraDistance = 16f * density
                            }
                        }
                        .noRippleClickable {
                            scope.launch { listState.animateScrollToItem(index) }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = actualIndex.toString().padStart(2, '0'),
                        fontSize = 28.sp,
                        color = if(selectedIndex == index) SpiritTodoTheme.color.mainTextAndStroke
                                else SpiritTodoTheme.color.onSurfaceColor8
                    )
                }
            }
        }
    }
}
