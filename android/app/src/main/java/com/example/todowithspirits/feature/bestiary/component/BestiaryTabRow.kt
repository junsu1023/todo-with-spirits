package com.example.todowithspirits.feature.bestiary.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.feature.bestiary.model.BestiaryTab
import com.example.todowithspirits.theme.SpiritTodoTheme

private val TabShape = RoundedCornerShape(6.dp)

// 선택된 탭은 분홍 바탕, 나머지는 테두리만 있는 흰 바탕
@Composable
fun BestiaryTabRow(
    tabs: List<BestiaryTab>,
    selectedTab: BestiaryTab,
    onTabSelected: (BestiaryTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        tabs.forEach { tab ->
            val isSelected = tab == selectedTab

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .then(
                        if (isSelected) {
                            Modifier.background(SpiritTodoTheme.color.colorSub02, TabShape)
                        } else {
                            Modifier
                                .background(SpiritTodoTheme.color.white, TabShape)
                                .border(1.dp, SpiritTodoTheme.color.font02, TabShape)
                        }
                    )
                    .noRippleClickable { onTabSelected(tab) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(tab.labelRes),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) SpiritTodoTheme.color.white else SpiritTodoTheme.color.font02
                )
            }
        }
    }
}
