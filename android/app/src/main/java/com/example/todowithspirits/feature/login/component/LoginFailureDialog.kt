package com.example.todowithspirits.feature.login.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import com.example.todowithspirits.component.DimPopup
import com.example.todowithspirits.component.SpiritsTodoPrimaryButton
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun LoginFailureDialog(
    message: String,
    onConfirm: () -> Unit
) {
    DimPopup(onDismiss = onConfirm) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .background(color = SpiritTodoTheme.color.white, shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.login_failed_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.color.font01,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = message,
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                color = SpiritTodoTheme.color.font01,
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
