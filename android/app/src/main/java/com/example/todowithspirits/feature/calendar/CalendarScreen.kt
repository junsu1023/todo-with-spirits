package com.example.todowithspirits.feature.calendar

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.PlanSortOption
import com.example.todowithspirits.R
import com.example.todowithspirits.component.BottomShadow
import com.example.todowithspirits.component.CalendarDayEvent
import com.example.todowithspirits.component.CalendarView
import com.example.todowithspirits.component.CalendarViewDefaults
import com.example.todowithspirits.component.LoadingOverlay
import com.example.todowithspirits.component.SpiritsTodoCheckbox
import com.example.todowithspirits.component.SpiritsTodoDropdown
import com.example.todowithspirits.component.TitleHeader
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.feature.calendar.component.AddPlanButton
import com.example.todowithspirits.feature.search.component.SearchPanel
import com.example.todowithspirits.feature.calendar.component.CalendarTaskItem
import com.example.todowithspirits.feature.calendar.model.labelRes
import com.example.todowithspirits.feature.calendar.state.CalendarEffect
import com.example.todowithspirits.feature.calendar.state.CalendarEvent
import com.example.todowithspirits.feature.calendar.state.CalendarUiState
import com.example.todowithspirits.feature.calendar.viewmodel.CalendarViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.util.ObserveAsEffect

private val CalendarHorizontalMargin = 18.dp

@Composable
fun CalendarRoute(
    navigateToAdd: () -> Unit,
    navigateToEditTask: (Long) -> Unit,
    navigateBack: () -> Unit,
    navigateToSearch: (String) -> Unit,
    calendarViewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by calendarViewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by calendarViewModel.isLoading.collectAsStateWithLifecycle()

    ObserveAsEffect(calendarViewModel.effect) { effect ->
        when (effect) {
            CalendarEffect.NavigateBack -> navigateBack()
            CalendarEffect.NavigateToAdd -> navigateToAdd()
            is CalendarEffect.NavigateToEditTask -> navigateToEditTask(effect.taskId)
            is CalendarEffect.NavigateToSearch -> navigateToSearch(effect.query)
        }
    }

    CalendarScreen(
        uiState = uiState,
        isLoading = isLoading,
        onEvent = calendarViewModel::onEvent
    )
}

@Composable
fun CalendarScreen(
    uiState: CalendarUiState,
    isLoading: Boolean,
    onEvent: (CalendarEvent) -> Unit
) {
    val context = LocalContext.current
    val dotColor = SpiritTodoTheme.color.pointPink
    val calendarEventData = remember(uiState.calendarEvents, dotColor) {
        uiState.calendarEvents.mapValues { (_, events) ->
            CalendarDayEvent(
                dotColors = listOf(dotColor),
                label = events.importantCount
                    .takeIf { it > 0 }
                    ?.let { context.getString(R.string.important_count, it) }
            )
        }
    }
    val visiblePlans = uiState.visiblePlans

    BackHandler(enabled = uiState.isSearchOpen) { onEvent(CalendarEvent.SearchClosed) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpiritTodoTheme.color.surfaceColor1)
    ) {
        TitleHeader(
            title = stringResource(R.string.calendar),
            leftComposable = {
                Image(
                    painter = painterResource(R.drawable.todo_back1),
                    contentDescription = null,
                    modifier = Modifier
                        .size(26.dp)
                        .noRippleClickable { onEvent(CalendarEvent.BackClicked) }
                )
            },
            rightComposable = {
                if (uiState.isSearchOpen) {
                    Image(
                        painter = painterResource(R.drawable.todo_cross),
                        contentDescription = null,
                        modifier = Modifier
                            .size(28.dp)
                            .noRippleClickable { onEvent(CalendarEvent.SearchClosed) }
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.fi_rr_search),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(SpiritTodoTheme.color.todoTextMain),
                        modifier = Modifier
                            .size(24.dp)
                            .noRippleClickable { onEvent(CalendarEvent.SearchClicked) }
                    )
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = CalendarHorizontalMargin)) {
                        Spacer(modifier = Modifier.height(4.dp))

                        CalendarView(
                            modifier = Modifier
                                .background(SpiritTodoTheme.color.systemBackground, RoundedCornerShape(8.dp))
                                .padding(top = 10.dp),
                            selectedDate = uiState.selectedDate,
                            onDateSelected = { onEvent(CalendarEvent.DateSelected(it)) },
                            eventData = calendarEventData,
                            onMonthChanged = { onEvent(CalendarEvent.MonthChanged(it)) },
                            style = CalendarViewDefaults.calendarScreenStyle()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        AddPlanButton(onClick = { onEvent(CalendarEvent.AddClicked) })
                    }
                }

                if (uiState.plans.isNotEmpty()) {
                    item {
                        CalendarListOptionRow(
                            isHidden = uiState.isHidden,
                            sortOption = uiState.sortOption,
                            sortOptions = uiState.sortOptions,
                            onEvent = onEvent,
                            modifier = Modifier.padding(
                                start = CalendarHorizontalMargin,
                                top = 28.dp,
                                end = CalendarHorizontalMargin,
                                bottom = 16.dp
                            )
                        )
                    }
                }

                if (visiblePlans.isEmpty()) {
                    item {
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
                                text = stringResource(R.string.empty_plans),
                                fontSize = 14.sp,
                                color = SpiritTodoTheme.color.systemGrey
                            )
                        }
                    }
                } else {
                    items(visiblePlans, key = { it.id }) { item ->
                        CalendarTaskItem(
                            item = item,
                            isExpanded = uiState.expandedTaskId == item.id,
                            onToggleExpanded = { onEvent(CalendarEvent.TaskClicked(item.id)) },
                            onToggleComplete = { onEvent(CalendarEvent.TaskCompletionToggled(item.id, item.isDone, item.endDate)) },
                            onDelete = { onEvent(CalendarEvent.TaskDeleteClicked(item.id)) },
                            onEdit = { onEvent(CalendarEvent.TaskEditClicked(item.id)) },
                            modifier = Modifier.padding(
                                start = CalendarHorizontalMargin,
                                end = CalendarHorizontalMargin,
                                bottom = 10.dp
                            )
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            if (uiState.isSearchOpen) {
                Column(modifier = Modifier.fillMaxSize()) {
                    SearchPanel(
                        input = uiState.searchInput,
                        recentSearches = uiState.recentSearches,
                        onInputChange = { onEvent(CalendarEvent.SearchInputChanged(it)) },
                        onSubmit = { onEvent(CalendarEvent.SearchSubmitted) },
                        onRecentClick = { onEvent(CalendarEvent.RecentSearchClicked(it)) },
                        onRecentRemove = { onEvent(CalendarEvent.RecentSearchRemoved(it)) },
                        onRecentClear = { onEvent(CalendarEvent.RecentSearchesCleared) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SpiritTodoTheme.color.surfaceColor1)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SpiritTodoTheme.color.dimColor)
                            .noRippleClickable { onEvent(CalendarEvent.SearchClosed) }
                    ) {
                        BottomShadow()
                    }
                }
            }
        }
    }

    LoadingOverlay(isLoading = isLoading)
}

// 완료 제외 체크 + 정렬 드롭다운
@Composable
private fun CalendarListOptionRow(
    isHidden: Boolean,
    sortOption: PlanSortOption,
    sortOptions: List<PlanSortOption>,
    onEvent: (CalendarEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val sortLabels = sortOptions.map { stringResource(it.labelRes) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.noRippleClickable { onEvent(CalendarEvent.HideCompletedToggled) },
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SpiritsTodoCheckbox(
                modifier = Modifier.size(14.dp),
                checked = isHidden,
                onCheckedChange = { onEvent(CalendarEvent.HideCompletedToggled) },
                checkedIcon = painterResource(R.drawable.ic_checkbox_square_on),
                uncheckedIcon = painterResource(R.drawable.ic_checkbox_square_off)
            )

            Text(
                text = stringResource(R.string.hide_completion),
                color = SpiritTodoTheme.color.systemGrey,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        SpiritsTodoDropdown(
            value = stringResource(sortOption.labelRes),
            options = sortLabels,
            onOptionSelected = { label ->
                sortOptions.getOrNull(sortLabels.indexOf(label))?.let {
                    onEvent(CalendarEvent.SortOptionSelected(it))
                }
            },
            dropdownWidth = 96.dp,
            dropdownGap = 6.dp,
            itemVerticalPadding = 10.dp,
            selectedOptionColor = SpiritTodoTheme.color.pointPink
        ) { expand ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.noRippleClickable { expand() }
            ) {
                Text(
                    text = stringResource(sortOption.labelRes),
                    color = SpiritTodoTheme.color.todoTextMain,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Image(
                    painter = painterResource(R.drawable.fi_rr_angle_small_down),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
