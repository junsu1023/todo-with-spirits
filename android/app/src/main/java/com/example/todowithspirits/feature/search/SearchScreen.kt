package com.example.todowithspirits.feature.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todowithspirits.R
import com.example.todowithspirits.component.BottomShadow
import com.example.todowithspirits.component.LoadingOverlay
import com.example.todowithspirits.component.TitleHeader
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.feature.search.component.SearchField
import com.example.todowithspirits.feature.calendar.model.PlanItemData
import com.example.todowithspirits.feature.search.state.SearchEffect
import com.example.todowithspirits.feature.search.state.SearchEvent
import com.example.todowithspirits.feature.search.state.SearchUiState
import com.example.todowithspirits.feature.search.state.SearchDateGroup
import com.example.todowithspirits.feature.search.viewmodel.SearchViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.util.ObserveAsEffect
import java.time.LocalDate

private val SearchHorizontalMargin = 18.dp
private val DateColumnWidth = 60.dp

@Composable
fun SearchRoute(
    navigateBack: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    ObserveAsEffect(viewModel.effect) { effect ->
        when (effect) {
            SearchEffect.NavigateBack -> navigateBack()
        }
    }

    SearchScreen(
        uiState = uiState,
        isLoading = isLoading,
        onEvent = viewModel::onEvent
    )
}

// 일정 검색 결과 화면. 날짜별로 묶고, 달이 바뀌면 구분선, 올해가 아닌 연도는 연도 제목을 붙인다.
@Composable
fun SearchScreen(
    uiState: SearchUiState,
    isLoading: Boolean,
    onEvent: (SearchEvent) -> Unit
) {
    val currentYear = LocalDate.now().year
    val groups = uiState.dateGroups

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SpiritTodoTheme.color.surfaceColor1)
        ) {
            TitleHeader(
                title = stringResource(R.string.search_title),
                leftComposable = {
                    Image(
                        painter = painterResource(R.drawable.todo_back1),
                        contentDescription = null,
                        modifier = Modifier
                            .size(26.dp)
                            .noRippleClickable { onEvent(SearchEvent.BackClicked) }
                    )
                }
            )

            // 헤더와 검색창은 같은 흰 배경으로 이어지고, 그 아래로만 그림자가 진다.
            SearchField(
                value = uiState.input,
                onValueChange = { onEvent(SearchEvent.InputChanged(it)) },
                onSubmit = { onEvent(SearchEvent.Submitted) },
                modifier = Modifier.padding(start = SearchHorizontalMargin, top = 10.dp, end = SearchHorizontalMargin, bottom = 12.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (uiState.hasSearched && groups.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 56.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(R.drawable.todo_empty),
                            contentDescription = null
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = stringResource(R.string.search_no_result),
                            fontSize = 14.sp,
                            color = SpiritTodoTheme.color.systemGrey
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = SearchHorizontalMargin)
                    ) {
                        itemsIndexed(groups, key = { _, group -> group.date.toString() }) { index, group ->
                            val previous = groups.getOrNull(index - 1)?.date
                            val isNewMonth = previous != null &&
                                (previous.year != group.date.year || previous.month != group.date.month)
                            val showYear = group.date.year != currentYear &&
                                (previous == null || previous.year != group.date.year)

                            Column {
                                when {
                                    previous == null -> Spacer(Modifier.height(24.dp))
                                    isNewMonth -> {
                                        Spacer(Modifier.height(20.dp))
                                        HorizontalDivider(thickness = 1.dp, color = SpiritTodoTheme.color.surfaceColor4)
                                        Spacer(Modifier.height(20.dp))
                                    }
                                    else -> Spacer(Modifier.height(24.dp))
                                }

                                if (showYear) {
                                    Text(
                                        text = stringResource(R.string.year_format, group.date.year),
                                        fontSize = 16.sp,
                                        color = SpiritTodoTheme.color.todoTextMain
                                    )

                                    Spacer(Modifier.height(12.dp))
                                }

                                SearchDateGroupRow(group = group)
                            }
                        }

                        item { Spacer(Modifier.height(24.dp)) }
                    }
                }

                BottomShadow()
            }
        }

        LoadingOverlay(isLoading = isLoading)
    }
}

@Composable
private fun SearchDateGroupRow(group: SearchDateGroup) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.width(DateColumnWidth),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.month, group.date.monthValue),
                fontSize = 12.sp,
                color = SpiritTodoTheme.color.onSurfaceColor5
            )

            Text(
                text = "%02d".format(group.date.dayOfMonth),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.color.todoTextMain
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            group.items.forEach { item -> SearchResultCard(item = item) }
        }
    }
}

@Composable
private fun SearchResultCard(item: PlanItemData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(SpiritTodoTheme.color.systemBackground, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 완료 상태 디자인이 없어 캘린더 일정 카드와 같은 표시(회색 + 취소선)를 쓴다.
        Text(
            text = item.title,
            fontSize = 14.sp,
            color = if (item.isDone) SpiritTodoTheme.color.systemGrey else SpiritTodoTheme.color.todoTextMain,
            textDecoration = if (item.isDone) TextDecoration.LineThrough else null,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )

        if (item.isImportant) {
            Image(
                painter = painterResource(R.drawable.fi_rr_color_star),
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
