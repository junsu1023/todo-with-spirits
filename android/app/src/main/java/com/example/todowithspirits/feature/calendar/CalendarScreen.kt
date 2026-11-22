package com.example.todowithspirits.feature.calendar

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todowithspirits.R
import com.example.todowithspirits.component.BottomShadow
import com.example.todowithspirits.component.calendar.CalendarView
import com.example.todowithspirits.component.LoadingOverlay
import com.example.todowithspirits.component.TaskFilterBar
import com.example.todowithspirits.component.TopNaviBar
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.feature.calendar.component.AddPlanButton
import com.example.todowithspirits.feature.search.component.SearchPanel
import com.example.todowithspirits.feature.calendar.component.TaskCard
import com.example.todowithspirits.feature.calendar.model.labelRes
import com.example.todowithspirits.feature.calendar.state.CalendarEffect
import com.example.todowithspirits.feature.calendar.state.CalendarEvent
import com.example.todowithspirits.feature.calendar.state.CalendarUiState
import com.example.todowithspirits.feature.calendar.viewmodel.CalendarViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.util.ObserveAsEffect
import java.time.LocalDate
import java.time.YearMonth

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
    BackHandler(enabled = uiState.isSearchOpen) { onEvent(CalendarEvent.SearchClosed) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .background(SpiritTodoTheme.color.back01)
    ) {
        CalendarHeader(
            isSearchOpen = uiState.isSearchOpen,
            onBackClick = { onEvent(CalendarEvent.BackClicked) },
            onSearchClosed = { onEvent(CalendarEvent.SearchClosed) },
            onSearchClicked = { onEvent(CalendarEvent.SearchClicked) }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))

                    CalendarSection(
                        selectedDate = uiState.selectedDate,
                        calendarEvents = uiState.calendarEvents,
                        onDateSelected = { date -> onEvent(CalendarEvent.DateSelected(date)) },
                        onMonthChanged = { month -> onEvent(CalendarEvent.MonthChanged(month)) },
                        onAddClicked = { onEvent(CalendarEvent.AddClicked)}
                    )
                }

                if(uiState.plans.isNotEmpty()) {
                    item {
                        TaskFilterBar(
                            checkLabel = stringResource(R.string.hide_completion),
                            isChecked = uiState.isHidden,
                            onCheckToggled = { onEvent(CalendarEvent.HideCompletedToggled) },
                            sortLabel = stringResource(uiState.sortOption.labelRes),
                            sortLabels = uiState.sortOptions.map { stringResource(it.labelRes) },
                            onSortSelected = { idx -> onEvent(CalendarEvent.SortOptionSelected(uiState.sortOptions[idx])) },
                            modifier = Modifier
                                .padding(horizontal = 18.dp)
                                .padding(top = 28.dp, bottom = 16.dp)
                        )
                    }
                }

                if(uiState.visiblePlans.isEmpty()) {
                    item {
                        EmptyTaskSection()
                    }
                } else {
                    items(uiState.visiblePlans, key = { it.id }) { item ->
                        TaskCard(
                            item = item,
                            isExpanded = uiState.expandedTaskId == item.id,
                            onToggleExpanded = { onEvent(CalendarEvent.TaskClicked(item.id)) },
                            onToggleComplete = { onEvent(CalendarEvent.TaskCompletionToggled(item.id, item.isDone, item.endDate)) },
                            onDelete = { onEvent(CalendarEvent.TaskDeleteClicked(item.id)) },
                            onEdit = { onEvent(CalendarEvent.TaskEditClicked(item.id)) },
                            modifier = Modifier
                                .padding(horizontal = 18.dp)
                                .padding(bottom = 10.dp)
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

@Composable
private fun CalendarHeader(
    isSearchOpen: Boolean,
    onBackClick: () -> Unit,
    onSearchClosed: () -> Unit,
    onSearchClicked: () -> Unit
) {
    TopNaviBar(
        modifier = Modifier.padding(horizontal = 17.dp),
        title = stringResource(R.string.calendar),
        leftComposable = {
            Image(
                painter = painterResource(R.drawable.widgetly_back_26),
                contentDescription = null,
                modifier = Modifier.noRippleClickable { onBackClick() }
            )
        },
        rightComposable = {
            if (isSearchOpen) {
                Image(
                    painter = painterResource(R.drawable.widgetly_cross_24),
                    contentDescription = null,
                    modifier = Modifier.noRippleClickable { onSearchClosed() }
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.widgetly_search_24),
                    contentDescription = null,
                    modifier = Modifier.noRippleClickable { onSearchClicked() }
                )
            }
        }
    )
}

@Composable
private fun CalendarSection(
    selectedDate: LocalDate,
    calendarEvents: Map<LocalDate, Int>,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (YearMonth) -> Unit,
    onAddClicked: () -> Unit
) {
    val context = LocalContext.current
    val calendarEventLabels = remember(calendarEvents) {
        calendarEvents.mapValues { (_, events) ->
            events.takeIf { it > 0 }?.let { context.getString(R.string.important_count, it) }
        }
    }

    Column(
        modifier = Modifier.padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CalendarView(
            selectedDate = selectedDate,
            onDateSelected = { date -> onDateSelected(date) },
            eventLabels = calendarEventLabels,
            onMonthChanged = { month -> onMonthChanged(month) },
        )

        AddPlanButton(onClick = onAddClicked)
    }
}

@Composable
private fun EmptyTaskSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.widgetly_empty),
            contentDescription = null
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.empty_plans),
            fontSize = 14.sp,
            color = SpiritTodoTheme.color.font02
        )
    }
}