package com.example.todowithspirits.feature.plan.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun PlanHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .padding(start = 10.dp, end = 8.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.fi_rr_back),
            contentDescription = null,
            modifier = Modifier.align(Alignment.CenterStart)
        )

        Text(
            text = "전체 플랜",
            modifier = Modifier.align(Alignment.Center),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = SpiritTodoTheme.colors.mainTextColor
        )
    }
}