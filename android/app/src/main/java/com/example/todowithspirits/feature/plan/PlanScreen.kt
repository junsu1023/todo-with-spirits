package com.example.todowithspirits.feature.plan

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.domain.model.PlanSortOption
import com.example.todowithspirits.R
import com.example.todowithspirits.component.CalendarDayEvent
import com.example.todowithspirits.component.CalendarView
import com.example.todowithspirits.component.SpiritsTodoDropdown
import com.example.todowithspirits.component.TitleHeader
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.feature.plan.component.AddPlanButton
import com.example.todowithspirits.feature.plan.component.PlanListItem
import com.example.todowithspirits.feature.plan.component.PlanSearchArea
import com.example.todowithspirits.feature.plan.component.UnderlinePlanTabs
import com.example.todowithspirits.feature.plan.viewmodel.PlanViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme
import java.time.LocalDate
import java.time.LocalTime

enum class PlanType { TODO, ROUTINE }

data class PlanItemData(
    val id: Int,
    val title: String,
    val type: PlanType,
    val isImportant: Boolean,
    val isDone: Boolean,
    val dueDate: LocalDate?,
    val dueTime: LocalTime?,
    val memo: String = "",
    val category: String? = null,
    val repeatInfo: String? = null
)

@Composable
private fun PlanType.color(): Color = when (this) {
    PlanType.TODO -> SpiritTodoTheme.color.surfaceColor8
    PlanType.ROUTINE -> SpiritTodoTheme.color.surfaceColor9
}

@Composable
fun PlanScreen(
    planViewModel: PlanViewModel = hiltViewModel(),
    navigateToAdd: () -> Unit,
    navigateToDetail: (Int) -> Unit,
    navigateToAlarm: () -> Unit
) {
    val uiState by planViewModel.uiState.collectAsState()

    val calendarEventData: Map<LocalDate, CalendarDayEvent> = uiState.calendarEvents
        .mapValues { (_, events) ->
            CalendarDayEvent(
                dotColors = events.types.map { it.color() },
                label = events.importantCount
                    .takeIf { it > 0 }
                    ?.let { stringResource(R.string.important_count, it) }
            )
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpiritTodoTheme.color.surfaceColor1)
            .verticalScroll(rememberScrollState())
    ) {
        TitleHeader(
            title = stringResource(R.string.all_plan),
            rightIconRes = R.drawable.todo_alarm,
            onRightIconClick = navigateToAlarm,
            isAlarm = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        PlanSearchArea()

        Spacer(modifier = Modifier.height(14.dp))

        CalendarView(
            modifier = Modifier.padding(horizontal = 16.dp),
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

        val filteredPlans = uiState.plans.filter { item ->
            val doneFilter = !uiState.isHidden || !item.isDone
            val tabFilter = when (uiState.selectedTab) {
                stringResource(R.string.todo) -> item.type == PlanType.TODO
                stringResource(R.string.routine) -> item.type == PlanType.ROUTINE
                else -> true
            }
            doneFilter && tabFilter
        }

        if (filteredPlans.isEmpty()) {
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
        } else {
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
                    options = PlanSortOption.getAllDisplayNames(),
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

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                filteredPlans.forEach { item ->
                    PlanListItem(
                        item = item,
                        onDelete = {},
                        onEdit = {},
                        onPostpone = {},
                        navigateToDetail = { navigateToDetail(item.id) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}