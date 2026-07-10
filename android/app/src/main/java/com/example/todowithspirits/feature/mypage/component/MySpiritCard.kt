package com.example.todowithspirits.feature.mypage.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.HexagonShape
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun MySpiritCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SpiritTodoTheme.colors.surfaceColor4, RoundedCornerShape(6.dp))
            .padding(top = 16.dp, bottom = 14.dp)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.my_spirit),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = SpiritTodoTheme.colors.mainTextColor
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.temp_spirit),
                contentDescription = null,
                modifier = Modifier.size(85.dp)
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .background(SpiritTodoTheme.colors.surfaceColor1, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.representative_spirit),
                        fontSize = 10.sp,
                        color = SpiritTodoTheme.colors.onSurfaceColor1,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "루미", // dummy
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SpiritTodoTheme.colors.mainTextColor
                    )

                    Spacer(Modifier.width(4.dp))

                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(SpiritTodoTheme.colors.onSurfaceColor1, HexagonShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "3", // dummy
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SpiritTodoTheme.colors.white
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.exp_format, 999, 1000),
                        fontSize = 10.sp,
                        color = SpiritTodoTheme.colors.mainTextColor
                    )
                    Text(
                        text = "100%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SpiritTodoTheme.colors.onSurfaceColor1
                    )
                }

                Spacer(Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(SpiritTodoTheme.colors.trackColor)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.999f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(9999.dp))
                            .background(SpiritTodoTheme.colors.onSurfaceColor1)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(SpiritTodoTheme.colors.white, RoundedCornerShape(6.dp))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.view_spirit),
                    fontSize = 14.sp,
                    color = SpiritTodoTheme.colors.mainTextColor
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(SpiritTodoTheme.colors.white, RoundedCornerShape(6.dp))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.change_representative),
                    fontSize = 14.sp,
                    color = SpiritTodoTheme.colors.mainTextColor
                )
            }
        }
    }
}