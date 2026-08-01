package com.example.todowithspirits.feature.login.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.component.SpiritsTodoPrimaryButton
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun LoginFailureDialog(
    message: String,
    onConfirm: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpiritTodoTheme.color.dimColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .background(color = SpiritTodoTheme.color.surfaceColor1, shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.login_failed_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.color.todoTextMain,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = message,
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                color = SpiritTodoTheme.color.systemGrey,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            SpiritsTodoPrimaryButton(
                text = stringResource(R.string.check),
                onClick = onConfirm
            )
        }
    }
}