package com.example.todowithspirits.feature.mypage.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme


@Composable
fun MyPageSettingHeader(
    onBack: () -> Unit,
    title: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 17.dp)
            .padding(vertical = 18.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.fi_rr_back),
            contentDescription = null,
            colorFilter = ColorFilter.tint(SpiritTodoTheme.colors.mainTextColor),
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterStart)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onBack() }
        )

        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = SpiritTodoTheme.colors.mainTextColor,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}