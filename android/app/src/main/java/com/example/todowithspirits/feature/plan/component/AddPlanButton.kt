package com.example.todowithspirits.feature.plan.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun AddPlanButton(navigateToAdd: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(42.dp)
            .background(SpiritTodoTheme.color.mainArea, RoundedCornerShape(6.dp))
            .noRippleClickable { navigateToAdd() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "+", color = SpiritTodoTheme.color.onSurfaceColor3, fontSize = 16.sp)

            Spacer(modifier = Modifier.width(8.dp))

            Text(text = stringResource(R.string.add_plan), color = SpiritTodoTheme.color.onSurfaceColor3, fontSize = 16.sp)
        }
    }
}