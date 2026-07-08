package com.example.todowithspirits.feature.record.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun MissedAreaRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SpiritTodoTheme.colors.white, RoundedCornerShape(6.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.fi_rr_frown),
                contentDescription = null
            )

            Spacer(Modifier.width(10.dp))

            Text(
                text = "건강",
                fontSize = 14.sp,
                color = SpiritTodoTheme.colors.mainTextColor
            )
        }
        Text(
            text = "15회",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = SpiritTodoTheme.colors.mainTextColor
        )
    }
}