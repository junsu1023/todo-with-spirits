package com.example.todowithspirits.feature.plan

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import com.example.todowithspirits.feature.plan.component.AddPlanButton
import com.example.todowithspirits.feature.plan.component.PlanListItem
import com.example.todowithspirits.feature.plan.component.PlanSearchArea
import com.example.todowithspirits.feature.plan.component.UnderlinePlanTabs
import com.example.todowithspirits.feature.plan.viewmodel.PlanViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

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

internal val dummyPlans = listOf(
    PlanItemData(1, "성과 보고서 제출 마감", PlanType.TODO, true, true, LocalDate.of(2026, 6, 1), LocalTime.of(18, 0), "", "업무/커리어"),
    PlanItemData(2, "민지랑 저녁", PlanType.TODO, false, false, LocalDate.of(2026, 6, 1), LocalTime.of(19, 0), "", "인간관계/약속"),
    PlanItemData(3, "책 20페이지 읽기", PlanType.ROUTINE, false, false, LocalDate.of(2026, 6, 1), LocalTime.of(19, 0), "", "자기계발", "매주 월, 화"),
    PlanItemData(4, "비행기 티켓 끊기", PlanType.TODO, false, false, LocalDate.of(2026, 6, 12), LocalTime.of(23, 59), "", "자기계발", "매주 월, 화")
)

private data class DummyDayPlan(val types: List<PlanType>, val label: String? = null)

private val dummyDayOfMonthPlans = mapOf(
    1 to DummyDayPlan(listOf(PlanType.TODO), "중요 1"),
    2 to DummyDayPlan(listOf(PlanType.TODO)),
    3 to DummyDayPlan(listOf(PlanType.ROUTINE)),
    4 to DummyDayPlan(listOf(PlanType.TODO)),
    5 to DummyDayPlan(listOf(PlanType.TODO, PlanType.ROUTINE)),
    7 to DummyDayPlan(listOf(PlanType.TODO)),
    8 to DummyDayPlan(listOf(PlanType.TODO)),
    9 to DummyDayPlan(listOf(PlanType.TODO, PlanType.TODO)),
    10 to DummyDayPlan(listOf(PlanType.ROUTINE)),
    12 to DummyDayPlan(listOf(PlanType.TODO)),
    15 to DummyDayPlan(listOf(PlanType.TODO)),
    17 to DummyDayPlan(listOf(PlanType.TODO, PlanType.ROUTINE)),
    18 to DummyDayPlan(listOf(PlanType.TODO), "중요 2"),
    19 to DummyDayPlan(listOf(PlanType.TODO)),
    22 to DummyDayPlan(listOf(PlanType.TODO)),
    24 to DummyDayPlan(listOf(PlanType.TODO, PlanType.TODO)),
    26 to DummyDayPlan(listOf(PlanType.ROUTINE)),
    29 to DummyDayPlan(listOf(PlanType.TODO)),
    30 to DummyDayPlan(listOf(PlanType.TODO, PlanType.ROUTINE))
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

    // 더미 데이터라 선택된 날짜가 속한 달에만 이벤트가 채워진다. 실제 API 연동 시 교체 예정.
    val calendarYearMonth = YearMonth.from(uiState.selectedDate)
    val calendarEventData: Map<LocalDate, CalendarDayEvent> = dummyDayOfMonthPlans
        .filterKeys { it <= calendarYearMonth.lengthOfMonth() }
        .mapValues { (_, plan) -> CalendarDayEvent(plan.types.map { it.color() }, plan.label) }
        .mapKeys { (day, _) -> calendarYearMonth.atDay(day) }

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
            eventData = calendarEventData
        )

        Spacer(modifier = Modifier.height(14.dp))

        AddPlanButton(navigateToAdd = navigateToAdd)

        Spacer(modifier = Modifier.height(26.dp))

        UnderlinePlanTabs(
            selectedTab = uiState.selectedTab,
            onTabSelected = { planViewModel.setSelectedTab(it) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.hide_completion),
                color = if(uiState.isHidden) SpiritTodoTheme.color.onSurfaceColor4 else SpiritTodoTheme.color.onSurfaceColor8,
                fontSize = 14.sp,
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { planViewModel.setHiddenState(!uiState.isHidden) }
            )

            Spacer(modifier = Modifier.weight(1f))

            SpiritsTodoDropdown(
                value = uiState.sortOption.displayName,
                options = PlanSortOption.getAllDisplayNames(),
                onOptionSelected = { planViewModel.setSortOption(PlanSortOption.fromDisplayName(it)) },
                dropdownWidth = 96.dp,
                dropdownGap = 3.dp,
                itemVerticalPadding = 14.dp
            ) { expand ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { expand() }
                    )
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

        val filteredPlans = dummyPlans.filter { item ->
            val doneFilter = !uiState.isHidden || !item.isDone
            val tabFilter = when (uiState.selectedTab) {
                stringResource(R.string.todo) -> item.type == PlanType.TODO
                stringResource(R.string.routine) -> item.type == PlanType.ROUTINE
                else -> true
            }
            doneFilter && tabFilter
        }

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