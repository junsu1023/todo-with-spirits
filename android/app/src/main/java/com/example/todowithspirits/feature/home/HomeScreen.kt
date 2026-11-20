package com.example.todowithspirits.feature.home

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todowithspirits.R
import com.example.todowithspirits.component.AlarmIconSection
import com.example.todowithspirits.component.LoadingOverlay
import com.example.todowithspirits.component.TopNaviBar
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.feature.home.component.HomeBottomButtons
import com.example.todowithspirits.feature.home.component.HomeStatusChipRow
import com.example.todowithspirits.feature.home.component.HomeTodaySummaryCard
import com.example.todowithspirits.feature.home.component.SpiritStage
import com.example.todowithspirits.feature.home.component.TodoDetailPopup
import com.example.todowithspirits.feature.home.state.HomeEffect
import com.example.todowithspirits.feature.home.state.HomeEvent
import com.example.todowithspirits.feature.home.state.HomeUiState
import com.example.todowithspirits.feature.home.viewmodel.HomeViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.util.KoreanMonthDayWithDayFormatter
import com.example.todowithspirits.util.ObserveAsEffect

private val HomeHorizontalMargin = 18.dp
private val HomeSectionGap = 10.dp

@Composable
fun HomeRoute(
    navigateToAlarm: () -> Unit,
    navigateToMyPage: () -> Unit,
    navigateToBestiary: () -> Unit,
    navigateToForest: () -> Unit,
    navigateToCalendar: () -> Unit,
    navigateToEditTask: (Long) -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by homeViewModel.isLoading.collectAsStateWithLifecycle()

    ObserveAsEffect(homeViewModel.effect) { effect ->
        when (effect) {
            HomeEffect.NavigateToMyPage -> navigateToMyPage()
            HomeEffect.NavigateToAlarm -> navigateToAlarm()
            HomeEffect.NavigateToBestiary -> navigateToBestiary()
            HomeEffect.NavigateToForest -> navigateToForest()
            HomeEffect.NavigateToCalendar -> navigateToCalendar()
            is HomeEffect.NavigateToEditTask -> navigateToEditTask(effect.taskId)
        }
    }

    HomeScreen(
        uiState = uiState,
        isLoading = isLoading,
        onEvent = homeViewModel::onEvent
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    isLoading: Boolean,
    onEvent: (HomeEvent) -> Unit
) {
    var todayCardCollapsedHeight by remember { mutableStateOf(98.dp) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SpiritTodoTheme.color.back01)
        ) {
            TopNaviBar(
                modifier = Modifier.padding(horizontal = 17.dp),
                leftComposable = {
                    Text(
                        text = uiState.selectedDate.format(KoreanMonthDayWithDayFormatter),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = SpiritTodoTheme.color.font01
                    )
                },
                rightComposable = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(R.drawable.todo_setting),
                            contentDescription = null,
                            modifier = Modifier
                                .size(26.dp)
                                .noRippleClickable { onEvent(HomeEvent.SettingClicked) }
                        )

                        AlarmIconSection(
                            alarmIconRes = R.drawable.todo_alarm,
                            onAlarmClick = { onEvent(HomeEvent.AlarmClicked) },
                            msgCnt = 100,
                            iconSize = 26.dp,
                            badgeColor = SpiritTodoTheme.color.key02
                        )
                    }
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // 중요 일정 / 완료 뱃지 Y축 띄우기
                    Spacer(Modifier.height(24.dp + todayCardCollapsedHeight))

                    HomeStatusChipRow(
                        importantCount = uiState.todayImportantCount,
                        completedCount = uiState.todayCompletedCount,
                        modifier = Modifier.padding(horizontal = HomeHorizontalMargin)
                    )

                    SpiritStage(
                        spiritName = uiState.spiritInfo.name,
                        message = "얼마 안남았어요!",
                        isMenuVisible = uiState.isSpiritMenuVisible,
                        onSpiritClick = { onEvent(HomeEvent.SpiritClicked) },
                        onMenuAddClick = { onEvent(HomeEvent.SpiritMenuAddClicked) },
                        modifier = Modifier.weight(1f)
                    )
                }

                HomeTodaySummaryCard(
                    completedCount = uiState.todayCompletedCount,
                    totalCount = uiState.todayTotalCount,
                    tasks = uiState.todayTasks,
                    isExpanded = uiState.isTodayTasksExpanded,
                    onToggleExpanded = { onEvent(HomeEvent.TodayTasksToggled) },
                    onTaskCompletionChange = { id, isCompleted ->
                        onEvent(HomeEvent.TodayTaskCompletionChanged(id, isCompleted))
                    },
                    onTaskClick = { id -> onEvent(HomeEvent.TodayTaskClicked(id)) },
                    onCollapsedHeightChanged = { todayCardCollapsedHeight = it },
                    modifier = Modifier.padding(
                        start = HomeHorizontalMargin,
                        top = HomeSectionGap,
                        end = HomeHorizontalMargin
                    )
                )
            }
        }

        HomeBottomButtons(
            onBestiaryClick = { onEvent(HomeEvent.BestiaryButtonClicked) },
            onForestClick = { onEvent(HomeEvent.ForestButtonClicked) },
            onCalendarClick = { onEvent(HomeEvent.CalendarButtonClicked) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        uiState.selectedTask?.let { task ->
            TodoDetailPopup(
                task = task,
                onDismiss = { onEvent(HomeEvent.TodayTaskDetailDismissed) },
                onDelete = { onEvent(HomeEvent.TodayTaskDeleteClicked(task.id)) },
                onEdit = { onEvent(HomeEvent.TodayTaskEditClicked(task.id)) }
            )
        }

        LoadingOverlay(isLoading = isLoading)
    }
}
