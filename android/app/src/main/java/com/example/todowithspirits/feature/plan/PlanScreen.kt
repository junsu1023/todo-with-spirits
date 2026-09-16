package com.example.todowithspirits.feature.plan

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.example.domain.model.PlanSortOption
import com.example.todowithspirits.R
import com.example.todowithspirits.component.CalendarDayEvent
import com.example.todowithspirits.component.CalendarView
import com.example.todowithspirits.component.LoadingOverlay
import com.example.todowithspirits.component.SpiritsTodoDropdown
import com.example.todowithspirits.component.TitleHeader
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.feature.plan.component.AddPlanButton
import com.example.todowithspirits.feature.plan.component.PlanListItem
import com.example.todowithspirits.feature.plan.component.PlanSearchArea
import com.example.todowithspirits.feature.plan.component.UnderlinePlanTabs
import com.example.todowithspirits.feature.plan.model.PlanItemData
import com.example.todowithspirits.feature.plan.model.PlanType
import com.example.todowithspirits.feature.plan.viewmodel.PlanViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun PlanScreen(
    planViewModel: PlanViewModel = hiltViewModel(),
    navigateToAdd: () -> Unit,
    navigateToDetail: (Int) -> Unit,
    navigateToEditTask: (Long) -> Unit,
    navigateToAlarm: () -> Unit
) {
    val uiState by planViewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by planViewModel.isLoading.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val todoColor = SpiritTodoTheme.color.surfaceColor8
    val routineColor = SpiritTodoTheme.color.surfaceColor9
    val calendarEventData = remember(uiState.calendarEvents, todoColor, routineColor) {
        uiState.calendarEvents.mapValues { (_, events) ->
            CalendarDayEvent(
                dotColors = buildList {
                    if (events.types.contains(PlanType.TODO)) add(todoColor)
                    if (events.types.contains(PlanType.ROUTINE)) add(routineColor)
                },
                label = events.importantCount
                    .takeIf { it > 0 }
                    ?.let { context.getString(R.string.important_count, it) }
            )
        }
    }
    val todoLabel = stringResource(R.string.todo)
    val routineLabel = stringResource(R.string.routine)
    val sortOptions = remember(uiState.isHidden) {
        PlanSortOption.entries
            .filter { !(uiState.isHidden && it == PlanSortOption.COMPLETE) }
            .map { it.displayName }
    }
    val filteredPlans = remember(uiState.plans, uiState.isHidden, uiState.selectedTab, todoLabel, routineLabel) {
        uiState.plans.filter { item ->
            val doneFilter = !uiState.isHidden || !item.isDone
            val tabFilter = when (uiState.selectedTab) {
                todoLabel -> item.type == PlanType.TODO
                routineLabel -> item.type == PlanType.ROUTINE
                else -> true
            }
            doneFilter && tabFilter
        }
    }
    val sortedPlans = remember(filteredPlans, uiState.sortOption) {
        when (uiState.sortOption) {
            PlanSortOption.DEADLINE -> filteredPlans.sortedWith(
                compareBy<PlanItemData> { it.endDate == null && it.endTime == null && it.isAllDay }
                    .thenBy { it.endDate ?: LocalDate.MAX }
                    .thenBy { it.endTime ?: LocalTime.MAX }
            )
            PlanSortOption.IMPORTANT -> filteredPlans.sortedWith(
                compareByDescending<PlanItemData> { it.isImportant }
                    .thenBy { it.title }
            )
            PlanSortOption.COMPLETE -> filteredPlans.sortedWith(
                compareByDescending<PlanItemData> { it.isDone }
                    .thenBy { it.title }
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SpiritTodoTheme.color.surfaceColor1)
    ) {
        item {
            TitleHeader(
                title = stringResource(R.string.all_plan),
                rightIconRes = R.drawable.todo_alarm,
                onRightIconClick = navigateToAlarm,
                isAlarm = true
            )
        }

        item {
            Column {
                Spacer(modifier = Modifier.height(10.dp))

                PlanSearchArea()

                Spacer(modifier = Modifier.height(14.dp))

                CalendarView(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .background(
                            SpiritTodoTheme.color.systemBackground,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 16.dp),
                    selectedDate = uiState.selectedDate,
                    onDateSelected = { planViewModel.setSelectedDate(it) },
                    showSelectedDateInHeader = true,
                    eventData = calendarEventData,
                    onMonthChanged = { planViewModel.setCalendarMonth(it) }
                )

                Spacer(modifier = Modifier.height(14.dp))

                AddPlanButton(navigateToAdd = navigateToAdd)

                Spacer(modifier = Modifier.height(26.dp))

                UnderlinePlanTabs(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = { planViewModel.setSelectedTab(it) }
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        if(uiState.plans.isNotEmpty()) {
            item {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.hide_completion),
                            color = if (uiState.isHidden) SpiritTodoTheme.color.mainTextAndStroke else SpiritTodoTheme.color.onSurfaceColor8,
                            fontSize = 14.sp,
                            modifier = Modifier.noRippleClickable { planViewModel.setHiddenState(!uiState.isHidden) }
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        SpiritsTodoDropdown(
                            value = uiState.sortOption.displayName,
                            options = sortOptions,
                            onOptionSelected = {
                                planViewModel.setSortOption(
                                    PlanSortOption.fromDisplayName(
                                        it
                                    )
                                )
                            },
                            dropdownWidth = 96.dp,
                            dropdownGap = 3.dp,
                            itemVerticalPadding = 14.dp
                        ) { expand ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.noRippleClickable { expand() }
                            ) {
                                Text(
                                    text = uiState.sortOption.displayName,
                                    color = SpiritTodoTheme.color.todoTextMain,
                                    fontSize = 14.sp
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Image(
                                    painter = painterResource(R.drawable.fi_rr_angle_small_down),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }
            }
        }

        if (sortedPlans.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.todo_empty),
                        contentDescription = null
                    )

                    Text(
                        text = stringResource(R.string.empty_plans),
                        fontSize = 14.sp,
                        color = SpiritTodoTheme.color.systemGrey
                    )
                }
            }
        } else {
            items(sortedPlans, key = { it.id }) { item ->
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    PlanListItem(
                        item = item,
                        onDelete = { planViewModel.deleteTask(item.id.toLong()) },
                        onEdit = { navigateToEditTask(item.id.toLong()) },
                        onPostpone = {},
                        onToggleComplete = {
                            if(item.isDone) {
                                planViewModel.cancelTaskCompletion(item.id.toLong(), uiState.selectedDate)
                            } else {
                                planViewModel.completeTask(item.id.toLong(), uiState.selectedDate)
                            }
                        },
                        navigateToDetail = { navigateToDetail(item.id) },
                        resetKey = uiState.selectedTab to uiState.sortOption
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    LoadingOverlay(isLoading = isLoading)
}