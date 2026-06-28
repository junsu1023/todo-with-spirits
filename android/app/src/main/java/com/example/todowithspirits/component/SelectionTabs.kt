package com.example.todowithspirits.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.theme.SpiritTodoTheme

@Immutable
data class TabItems(
    val items: List<String>
)

@Composable
fun SelectionTabs(
    tabItems: TabItems,
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        tabItems.items.forEach { item ->
            val isSelected = item == selectedItem

            SelectionTabItem(
                text = item,
                isSelected = isSelected,
                onClick = { onItemSelected(item) }
            )
        }
    }
}

@Composable
private fun SelectionTabItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val selectedMainColor = SpiritTodoTheme.colors.selectedTabColor
    val selectedBgColor = SpiritTodoTheme.colors.white
    val unselectedTextColor = SpiritTodoTheme.colors.mainTextColor
    val unselectedBgColor = SpiritTodoTheme.colors.unselectedTabBgColor

    Box(
        modifier = Modifier
            .width(66.dp)
            .height(34.dp)
            .background(
                color = if(isSelected) selectedBgColor else unselectedBgColor,
                shape = RoundedCornerShape(54.dp)
            )
            .then(
                if (isSelected) {
                    Modifier.border(1.dp, selectedMainColor, RoundedCornerShape(54.dp))
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) selectedMainColor else unselectedTextColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
