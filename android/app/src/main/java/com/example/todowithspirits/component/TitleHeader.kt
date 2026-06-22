package com.example.todowithspirits.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.theme.SplitsTodoTheme

@Composable
fun TitleHeader(
    @DrawableRes leftIconRes: Int? = null,
    @StringRes titleRes: Int? = null,
    @DrawableRes rightIconRes: Int? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if(leftIconRes != null) {
            Icon(
                painter = painterResource(leftIconRes),
                contentDescription = null
            )
        }

        if(titleRes != null) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(titleRes),
                style = TextStyle(
                    fontSize = 18.sp,
                    color = SplitsTodoTheme.colors.titleTextColor,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            )
        }

        if(rightIconRes != null) {
            Icon(
                painter = painterResource(rightIconRes),
                contentDescription = null
            )
        }
    }
}