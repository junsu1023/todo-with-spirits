package com.example.todowithspirits.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun TaskFilterBar(
    checkLabel: String,
    isChecked: Boolean,
    onCheckToggled: () -> Unit,
    sortLabel: String,
    sortLabels: List<String>,
    onSortSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    labelFontSize: TextUnit = 16.sp,
    checkLabelColor: Color = SpiritTodoTheme.color.font01,
    sortLabelFontWeight: FontWeight = FontWeight.Normal
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.noRippleClickable(onClick = onCheckToggled),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SpiritsTodoCheckbox(
                checked = isChecked,
                onCheckedChange = { onCheckToggled() },
                checkedIcon = painterResource(R.drawable.widgetly_checkbox_on_16),
                uncheckedIcon = painterResource(R.drawable.widgetly_checkbox_off_16)
            )

            Text(
                text = checkLabel,
                color = checkLabelColor,
                fontSize = labelFontSize
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        SpiritsTodoDropdown(
            value = sortLabel,
            options = sortLabels,
            onOptionSelected = { label ->
                sortLabels.indexOf(label).takeIf { it >= 0 }?.let(onSortSelected)
            },
            dropdownWidth = 96.dp,
            dropdownGap = 6.dp,
            itemVerticalPadding = 10.dp,
            selectedOptionColor = SpiritTodoTheme.color.key02
        ) { expand ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.noRippleClickable { expand() }
            ) {
                Text(
                    text = sortLabel,
                    color = SpiritTodoTheme.color.font01,
                    fontSize = labelFontSize,
                    fontWeight = sortLabelFontWeight,
                    modifier = Modifier.offset(y = (-1).dp)
                )

                Image(
                    painter = painterResource(R.drawable.widgetly_down_16),
                    contentDescription = null
                )
            }
        }
    }
}
