package com.example.todowithspirits.feature.record.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

// 자주 놓친 분야 하단에 표시되는 코멘트 배너. 서버 응답에 별도 데이터가 없어 문구는 고정 텍스트를 사용한다.
@Composable
fun WeeklyHighlightBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SpiritTodoTheme.color.transparent, RoundedCornerShape(6.dp))
            .border(1.dp, SpiritTodoTheme.color.mainTextAndStroke, RoundedCornerShape(6.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.fi_rr_leaf),
            contentDescription = null,
            colorFilter = ColorFilter.tint(SpiritTodoTheme.color.mainTextAndStroke)
        )

        Spacer(Modifier.width(10.dp))

        Column {
            Text(
                text = stringResource(R.string.weekly_missed_area_highlight_title),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = SpiritTodoTheme.color.mainTextAndStroke
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = stringResource(R.string.weekly_missed_area_highlight_subtitle),
                fontSize = 10.sp,
                color = SpiritTodoTheme.color.mainTextAndStroke
            )
        }
    }
}
