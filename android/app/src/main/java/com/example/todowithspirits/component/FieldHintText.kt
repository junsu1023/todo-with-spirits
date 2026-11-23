package com.example.todowithspirits.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun FieldHintText(text: String, isError: Boolean) {
    Spacer(Modifier.height(5.dp))

    Text(
        text = text,
        fontSize = 12.sp,
        color = if(isError) SpiritTodoTheme.color.colorSub02 else SpiritTodoTheme.color.fontGrey
    )
}
