package com.example.todowithspirits.feature.mypage.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun StatusRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SpiritTodoTheme.colors.surfaceColor4, RoundedCornerShape(6.dp))
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.consecutive_achievement),
                fontSize = 12.sp,
                color = SpiritTodoTheme.colors.mainTextColor
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = "12일", // dummy
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = SpiritTodoTheme.colors.mainTextColor
            )
        }

        VerticalDivider(
            modifier = Modifier.height(40.dp),
            thickness = 1.dp,
            color = SpiritTodoTheme.colors.onSurfaceColor3
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.total_completion),
                fontSize = 12.sp,
                color = SpiritTodoTheme.colors.mainTextColor
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "12일", // dummy
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = SpiritTodoTheme.colors.mainTextColor
            )
        }
    }
}