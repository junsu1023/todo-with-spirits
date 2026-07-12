package com.example.todowithspirits.feature.alarm.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun AlarmBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SpiritTodoTheme.color.surfaceColor3, RoundedCornerShape(8.dp))
            .padding(vertical = 12.dp)
            .padding(start = 14.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.fi_rr_info),
            contentDescription = null
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = stringResource(R.string.alarm_desc),
            fontSize = 14.sp,
            color = SpiritTodoTheme.color.onSurfaceColor3,
            modifier = Modifier.weight(1f)
        )

        Image(
            painter = painterResource(R.drawable.fi_rr_angle_small_right),
            contentDescription = null
        )
    }
}