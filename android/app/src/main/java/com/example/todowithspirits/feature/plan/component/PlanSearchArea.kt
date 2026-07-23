package com.example.todowithspirits.feature.plan.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun PlanSearchArea() {
    val searchQuery = remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 16.dp, vertical = 0.dp)
            .background(SpiritTodoTheme.color.surfaceColor1, RoundedCornerShape(6.dp))
            .border(width = 1.dp, color = SpiritTodoTheme.color.onSurfaceColor2, RoundedCornerShape(6.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BasicTextField(
                value = searchQuery.value,
                onValueChange = { searchQuery.value = it },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    color = SpiritTodoTheme.color.todoTextMain
                ),
                decorationBox = { innerTextField ->
                    if (searchQuery.value.isEmpty()) {
                        Text(
                            text = stringResource(R.string.search_plan),
                            fontSize = 14.sp,
                            color = SpiritTodoTheme.color.onSurfaceColor8
                        )
                    }

                    innerTextField()
                },
                singleLine = true
            )

            Image(
                painter = painterResource(R.drawable.fi_rr_search),
                contentDescription = null
            )
        }
    }
}