package com.example.todowithspirits.feature.today.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getString
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.domain.model.RepeatOption
import com.example.todowithspirits.R
import com.example.todowithspirits.component.QuickAddPopupBottomInset
import com.example.todowithspirits.component.QuickAddPopupIconBadgeSize
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.component.throttleClickable
import com.example.todowithspirits.component.QuickAddSharedKeys
import com.example.todowithspirits.component.SelectionTabs
import com.example.todowithspirits.component.SpiritsTodoDropdown
import com.example.todowithspirits.component.SpiritsTodoSwitch
import com.example.todowithspirits.component.TabItems
import com.example.todowithspirits.component.CalendarView
import com.example.todowithspirits.feature.add.component.DayOfWeekSelector
import com.example.todowithspirits.component.MonthlyCalendarView
import com.example.todowithspirits.component.TimeWheelPicker
import com.example.todowithspirits.feature.today.viewmodel.QuickAddViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.util.KoreanDateWithDayFormatter
import com.example.todowithspirits.util.ToastUtil
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

private val routineRepeatOptions = listOf(
    RepeatOption.DAILY.displayName,
    RepeatOption.WEEKLY.displayName,
    RepeatOption.MONTHLY.displayName
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun QuickAddBottomPopup(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    quickAddViewModel: QuickAddViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val routineText = stringResource(R.string.routine)
    var isTitleFocused by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(getString(context, R.string.todo)) }
    var title by remember { mutableStateOf("") }
    var isImportant by remember { mutableStateOf(false) }
    var isScheduleSectionVisible by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var isDateExpanded by remember { mutableStateOf(false) }
    var isTimeEnabled by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(0, 0)) }
    var repeatOption by remember { mutableStateOf(RepeatOption.DAILY) }
    var selectedWeekDays by remember { mutableStateOf(setOf<DayOfWeek>()) }
    var selectedMonthDays by remember { mutableStateOf(setOf<Int>()) }
    val dateFormatter = KoreanDateWithDayFormatter

    LaunchedEffect(quickAddViewModel) {
        quickAddViewModel.errorMsg.collect { message ->
            ToastUtil.show(context, message)
        }
    }

    BackHandler(onBack = onDismiss)

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val restingBottomInsets = WindowInsets.navigationBars.add(WindowInsets(bottom = QuickAddPopupBottomInset))
    val bottomInsets = WindowInsets.ime.union(restingBottomInsets)

    with(sharedTransitionScope) { with(animatedVisibilityScope) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(bottomInsets)
            .noRippleClickable {
                if(isTitleFocused) {
                    keyboardController?.hide()
                    focusManager.clearFocus(force = true)
                } else {
                    onDismiss()
                }
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .sharedBounds(
                    rememberSharedContentState(key = QuickAddSharedKeys.CONTAINER),
                    animatedVisibilityScope = animatedVisibilityScope,
                    // RemeasureToBounds는 애니메이션 중 매 프레임 실제 크기 제약을 걸어 자식을 다시 레이아웃한다.
                    // 이 팝업의 자식(Column)은 탭/텍스트필드 등 텍스트 콘텐츠가 커서, 전환 초반 아직 작은
                    // bounds로 강제로 눌리면서 세로로 다 안 들어가 위쪽 테두리가 잘려 보였다.
                    // scaleToBounds(Fit)는 자식을 원래(안정된) 크기로 먼저 측정한 뒤 화면에 그릴 때만
                    // 비율 유지로 축소/확대하므로, 눌려서 잘리는 대신 전체가 작게 보였다가 커지는 식으로
                    // 부드럽게 이어진다.
                    resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(ContentScale.Fit),
                    clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(12.dp))
                )
                .noRippleClickable {
                    if(isTitleFocused) {
                        keyboardController?.hide()
                        focusManager.clearFocus(force = true)
                    }
                },
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, SpiritTodoTheme.color.mainTextAndStroke),
            color = SpiritTodoTheme.color.surfaceColor1
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .padding(top = 14.dp, bottom = 12.dp)
                    .animateEnterExit(
                        enter = fadeIn(),
                        exit = fadeOut()
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SelectionTabs(
                        tabItems = TabItems(listOf(stringResource(R.string.todo), stringResource(R.string.routine))),
                        selectedItem = selectedTab,
                        onItemSelected = { tab ->
                            selectedTab = tab
                            isDateExpanded = false
                            isTimeEnabled = false
                            isScheduleSectionVisible = false
                        }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    if(selectedTab != routineText) {
                        Image(
                            modifier = Modifier
                                .size(26.dp)
                                .noRippleClickable { isImportant = !isImportant },
                            painter = if(isImportant) painterResource(R.drawable.todo_important) else painterResource(R.drawable.todo_important_26),
                            contentDescription = null,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isTitleFocused = it.isFocused },
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        color = SpiritTodoTheme.color.todoTextMain
                    ),
                    decorationBox = { innerTextField ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                if (title.isEmpty()) {
                                    Text(
                                        text = "제목 없음",
                                        style = TextStyle(
                                            fontSize = 18.sp,
                                            color = SpiritTodoTheme.color.onSurfaceColor2
                                        )
                                    )
                                }

                                innerTextField()
                            }

                            if(selectedTab == stringResource(R.string.todo)) {
                                Image(
                                    painter = painterResource(R.drawable.todo_clock),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .noRippleClickable {
                                            isScheduleSectionVisible = !isScheduleSectionVisible

                                            if(!isScheduleSectionVisible) {
                                                isDateExpanded = false
                                                isTimeEnabled = false
                                            }
                                        },
                                    colorFilter = ColorFilter.tint(
                                        if(isScheduleSectionVisible) SpiritTodoTheme.color.mainArea
                                        else SpiritTodoTheme.color.systemGrey
                                    )
                                )
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                HorizontalDivider(color = SpiritTodoTheme.color.onSurfaceColor2, thickness = 1.dp)

                Spacer(modifier = Modifier.height(8.dp))

                when(selectedTab) {
                    stringResource(R.string.todo) -> {
                        AnimatedVisibility(
                            visible = isScheduleSectionVisible,
                            enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
                            exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut()
                        ) {
                            Column {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SpiritTodoTheme.color.surfaceColor4, RoundedCornerShape(8.dp))
                                        .noRippleClickable {
                                            isDateExpanded = !isDateExpanded
                                            if (isDateExpanded) isTimeEnabled = false
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(R.string.date),
                                            modifier = Modifier.weight(1f),
                                            fontSize = 14.sp,
                                            color = SpiritTodoTheme.color.todoTextMain
                                        )

                                        selectedDate?.let { date ->
                                            Text(
                                                text = date.format(dateFormatter),
                                                fontSize = 14.sp,
                                                color = if(isDateExpanded) SpiritTodoTheme.color.mainTextAndStroke else SpiritTodoTheme.color.todoTextMain
                                            )
                                        }
                                    }

                                    AnimatedVisibility(
                                        visible = isDateExpanded,
                                        enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
                                        exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut()
                                    ) {
                                        Column {
                                            CalendarView(
                                                selectedDate = selectedDate ?: LocalDate.now(),
                                                onDateSelected = {
                                                    selectedDate = it
                                                    isDateExpanded = false
                                                },
                                                showMonthNavigation = false
                                            )

                                            Spacer(Modifier.height(24.dp))
                                        }
                                    }
                                }

                                Spacer(Modifier.height(6.dp))

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SpiritTodoTheme.color.surfaceColor4, RoundedCornerShape(8.dp))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(R.string.time),
                                            modifier = Modifier.weight(1f),
                                            fontSize = 14.sp,
                                            color = SpiritTodoTheme.color.todoTextMain
                                        )

                                        SpiritsTodoSwitch(
                                            checked = isTimeEnabled,
                                            onCheckedChange = { enabled ->
                                                isTimeEnabled = enabled
                                                if (enabled) isDateExpanded = false
                                            },
                                            modifier = Modifier.width(48.dp).height(24.dp),
                                            thumbSize = 20.dp
                                        )
                                    }

                                    AnimatedVisibility(
                                        visible = isTimeEnabled,
                                        enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
                                        exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut()
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            TimeWheelPicker(
                                                initialHour = selectedTime.hour,
                                                initialMinute = selectedTime.minute,
                                                textSize = 18,
                                                onTimeSelected = { h, m ->
                                                    selectedTime = LocalTime.of(h, m)
                                                }
                                            )

                                            Spacer(Modifier.height(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    stringResource(R.string.routine) -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SpiritTodoTheme.color.surfaceColor4, RoundedCornerShape(8.dp))
                                .padding(horizontal = 14.dp)
                        ) {
                            QuickRepeatRow(
                                value = repeatOption.displayName,
                                onOptionSelected = { repeatOption = RepeatOption.fromDisplayName(it) }
                            )

                            AnimatedVisibility(
                                visible = repeatOption == RepeatOption.WEEKLY,
                                enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
                                exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut()
                            ) {
                                DayOfWeekSelector(
                                    selectedDays = selectedWeekDays,
                                    onDayToggled = { day ->
                                        selectedWeekDays = selectedWeekDays.toMutableSet().apply {
                                            if (day in this) remove(day) else add(day)
                                        }
                                    }
                                )
                            }

                            AnimatedVisibility(
                                visible = repeatOption == RepeatOption.MONTHLY,
                                enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
                                exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut()
                            ) {
                                MonthlyCalendarView(
                                    selectedDays = selectedMonthDays,
                                    onDayToggled = { day ->
                                        selectedMonthDays = selectedMonthDays.toMutableSet().apply {
                                            if (day in this) remove(day) else add(day)
                                        }
                                    },
                                    compact = true
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.todo_cross),
                        contentDescription = null,
                        tint = SpiritTodoTheme.color.systemGrey,
                        modifier = Modifier.noRippleClickable { onDismiss() }
                    )

                    Box(
                        modifier = Modifier
                            .size(QuickAddPopupIconBadgeSize)
                            .background(color = SpiritTodoTheme.color.mainBackground, CircleShape)
                            .throttleClickable(showRipple = false) {
                                if(title.isNotBlank()) {
                                    if(selectedTab == routineText) {
                                        quickAddViewModel.createRoutine(
                                            title = title,
                                            repeatOption = repeatOption,
                                            selectedWeekDays = selectedWeekDays,
                                            selectedMonthDays = selectedMonthDays,
                                            onSuccess = {
                                                ToastUtil.show(context, "루틴 추가 성공!")
                                                onDismiss()
                                            }
                                        )
                                    } else {
                                        quickAddViewModel.createTodo(
                                            title = title,
                                            isImportant = isImportant,
                                            date = selectedDate ?: LocalDate.now(),
                                            isTimeEnabled = isTimeEnabled,
                                            dueTime = selectedTime,
                                            onSuccess = {
                                                ToastUtil.show(context, "Todo 추가 성공!")
                                                onDismiss()
                                            }
                                        )
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.todo_plus),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(SpiritTodoTheme.color.mainTextAndStroke),
                            modifier = Modifier.sharedElement(
                                rememberSharedContentState(key = QuickAddSharedKeys.ICON),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        )
                    }
                }
            }
        }
    }
    } }
}

@Composable
private fun QuickRepeatRow(
    value: String,
    onOptionSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.repeat),
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            color = SpiritTodoTheme.color.todoTextMain
        )

        SpiritsTodoDropdown(
            value = value,
            options = routineRepeatOptions,
            onOptionSelected = onOptionSelected
        ) { expand ->
            Row(
                modifier = Modifier.clickable { expand() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    fontSize = 14.sp,
                    color = SpiritTodoTheme.color.todoTextMain
                )

                Spacer(Modifier.width(6.dp))

                Image(
                    painter = painterResource(R.drawable.fi_rr_up_down),
                    contentDescription = null,
                    modifier = Modifier.width(8.dp).height(11.dp)
                )
            }
        }
    }
}
