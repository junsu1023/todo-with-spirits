package com.example.todowithspirits.feature.mypage.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

@Composable
fun ProfileSection(navigateToAccountSetting: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(SpiritTodoTheme.color.surfaceColor17, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.todo_bottom_nv),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(SpiritTodoTheme.color.systemGrey)
                )
            }

            Spacer(Modifier.width(10.dp))

            Column {
                Text(
                    text = "일하기 싫어요",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SpiritTodoTheme.color.todoTextMain
                )

                Text(
                    text = "wish0221@gmail.com",
                    fontSize = 12.sp,
                    color = SpiritTodoTheme.color.systemGrey
                )
            }
        }

        Box(
            modifier = Modifier
                .border(1.dp, SpiritTodoTheme.color.systemGrey, RoundedCornerShape(6.dp))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = navigateToAccountSetting
                )
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.account_management),
                fontSize = 12.sp,
                color = SpiritTodoTheme.color.systemGrey
            )
        }
    }
}