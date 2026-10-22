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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todowithspirits.R
import com.example.todowithspirits.component.AlarmIconSection
import com.example.todowithspirits.component.LoadingOverlay
import com.example.todowithspirits.component.TitleHeader
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

// 첫 측정 전 기본값 (피그마 접힌 카드 높이)
private val DefaultTodayCardHeight = 94.dp

@Composable
fun HomeRoute(
    navigateToAlarm: () -> Unit,
    navigateToMyPage: () -> Unit,
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
    var todayCardCollapsedHeight by remember { mutableStateOf(DefaultTodayCardHeight) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SpiritTodoTheme.color.surfaceColor1)
        ) {
            TitleHeader(
                leftComposable = {
                    Text(
                        text = uiState.selectedDate.format(KoreanMonthDayWithDayFormatter),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = SpiritTodoTheme.color.pointPink
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
                            badgeColor = SpiritTodoTheme.color.pointPink
                        )
                    }
                }
            )

            // 오늘 일정 카드는 펼치면 아래 콘텐츠 위로 겹쳐야 해서, 같은 Box 안에서 카드를 마지막에(위에) 그린다.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(Modifier.height(HomeSectionGap + todayCardCollapsedHeight + HomeSectionGap))

                    HomeStatusChipRow(
                        importantCount = uiState.todayImportantCount,
                        completedCount = uiState.todayCompletedCount,
                        modifier = Modifier.padding(horizontal = HomeHorizontalMargin)
                    )

                    SpiritStage(
                        spiritName = uiState.spiritInfo.name,
                        message = "오늘 하루도 힘차게!",
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

        // 언덕 배경 위에 겹쳐 보이도록 화면 맨 아래에 띄운다.
        HomeBottomButtons(
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
