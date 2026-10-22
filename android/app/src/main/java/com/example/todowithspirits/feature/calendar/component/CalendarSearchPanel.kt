package com.example.todowithspirits.feature.calendar.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.theme.SpiritTodoTheme

// 캘린더 검색 패널 (피그마 Calendar3): 검색창 + 최근 검색어. 열리면 검색창에 바로 포커스한다.
@Composable
fun CalendarSearchPanel(
    input: String,
    recentSearches: List<String>,
    onInputChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onRecentClick: (String) -> Unit,
    onRecentRemove: (String) -> Unit,
    onRecentClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(start = 18.dp, top = 10.dp, end = 18.dp, bottom = 24.dp)) {
        CalendarSearchField(
            value = input,
            onValueChange = onInputChange,
            onSubmit = onSubmit
        )

        if (recentSearches.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.recent_search),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = SpiritTodoTheme.color.systemGrey,
                    modifier = Modifier.weight(1f)
                )

                // 피그마 호출 한도로 색을 확인하지 못해 스크린샷 기준으로 맞췄다.
                Text(
                    text = stringResource(R.string.clear_all),
                    fontSize = 12.sp,
                    color = SpiritTodoTheme.color.todoTextMain,
                    modifier = Modifier.noRippleClickable(onClick = onRecentClear)
                )
            }

            Spacer(Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                recentSearches.forEach { query ->
                    RecentSearchChip(
                        text = query,
                        onClick = { onRecentClick(query) },
                        onRemove = { onRecentRemove(query) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .border(1.dp, SpiritTodoTheme.color.onSurfaceColor2, RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(fontSize = 14.sp, color = SpiritTodoTheme.color.todoTextMain),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = stringResource(R.string.search_placeholder),
                            fontSize = 14.sp,
                            color = SpiritTodoTheme.color.onSurfaceColor8
                        )
                    }

                    innerTextField()
                }
            }
        )

        Image(
            painter = painterResource(R.drawable.fi_rr_search),
            contentDescription = null,
            modifier = Modifier
                .size(20.dp)
                .noRippleClickable(onClick = onSubmit)
        )
    }
}

@Composable
private fun RecentSearchChip(
    text: String,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .background(SpiritTodoTheme.color.systemArea, RoundedCornerShape(17.dp))
            .noRippleClickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = SpiritTodoTheme.color.todoTextMain
        )

        Image(
            painter = painterResource(R.drawable.todo_cross),
            contentDescription = null,
            colorFilter = ColorFilter.tint(SpiritTodoTheme.color.systemGrey),
            modifier = Modifier
                .size(12.dp)
                .noRippleClickable(onClick = onRemove)
        )
    }
}
