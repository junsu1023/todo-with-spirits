package com.example.todowithspirits.feature.plan.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.theme.SpiritTodoTheme

private val filterTabs = listOf("전체", "To do", "루틴")

@Composable
fun UnderlinePlanTabs(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    val selectedIndex = remember(selectedTab) { filterTabs.indexOf(selectedTab) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        val tabWidth = maxWidth / filterTabs.size

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(SpiritTodoTheme.colors.onSurfaceColor8)
                .align(Alignment.BottomStart)
        )

        val indicatorOffset by animateDpAsState(
            targetValue = tabWidth * selectedIndex,
            animationSpec = spring(stiffness = 500f),
            label = "TabIndicator"
        )

        Box(
            modifier = Modifier
                .width(tabWidth)
                .height(2.dp)
                .offset(x = indicatorOffset)
                .background(SpiritTodoTheme.colors.onSurfaceColor1)
                .align(Alignment.BottomStart)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            filterTabs.forEach { tab ->
                val isSelected = tab == selectedTab

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onTabSelected(tab) }
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        color = if (isSelected) SpiritTodoTheme.colors.onSurfaceColor1 else SpiritTodoTheme.colors.onSurfaceColor7,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}