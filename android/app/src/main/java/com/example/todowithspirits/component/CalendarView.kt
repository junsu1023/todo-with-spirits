package com.example.todowithspirits.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.util.KoreanDateWithDayFormatter

private data class CalendarDay(
    val date: LocalDate,
    val isCurrentMonth: Boolean
)

data class CalendarDayEvent(
    val dotColors: List<Color> = emptyList(),
    val label: String? = null
)

/**
 * CalendarView의 모양. 기본값([CalendarViewDefaults.style])은 기존 모양이고,
 * 화면마다 디자인이 다르면 [CalendarViewDefaults]에 스타일을 추가해 넘긴다.
 */
@Immutable
data class CalendarViewStyle(
    val monthTextSize: TextUnit,
    val monthTextColor: Color,
    @param:DrawableRes val prevIconRes: Int,
    @param:DrawableRes val nextIconRes: Int,
    val navIconSize: Dp,
    val navIconTint: Color?,
    val navIconGap: Dp,
    val headerStartPadding: Dp,
    val headerEndPadding: Dp,
    val gridHorizontalPadding: Dp,
    val weekdayTopPadding: Dp,
    val weekdayBottomPadding: Dp,
    val weekdayRowHeight: Dp,
    val weekdayColor: Color,
    val weekdayFontWeight: FontWeight,
    val weekRowHeight: Dp,
    val weekRowGap: Dp,
    val dayBoxSize: Dp,
    val dayBoxTopPadding: Dp,
    val dayFontWeight: FontWeight,
    val otherMonthFontSize: TextUnit,
    val selectedCircleSize: Dp,
    val selectedCircleColor: Color,
    val selectedFontSize: TextUnit,
    val selectedFontWeight: FontWeight,
    val dotTopPadding: Dp,
    val labelTopPadding: Dp
)

object CalendarViewDefaults {
    @Composable
    fun style() = CalendarViewStyle(
        monthTextSize = 16.sp,
        monthTextColor = SpiritTodoTheme.color.todoTextMain,
        prevIconRes = R.drawable.left,
        nextIconRes = R.drawable.right,
        navIconSize = Dp.Unspecified,
        navIconTint = null,
        navIconGap = 16.dp,
        headerStartPadding = 15.dp,
        headerEndPadding = 14.dp,
        gridHorizontalPadding = 0.dp,
        weekdayTopPadding = 16.dp,
        weekdayBottomPadding = 18.dp,
        weekdayRowHeight = Dp.Unspecified,
        weekdayColor = SpiritTodoTheme.color.onSurfaceColor9,
        weekdayFontWeight = FontWeight.Normal,
        weekRowHeight = 58.dp,
        weekRowGap = 12.dp,
        dayBoxSize = 32.dp,
        dayBoxTopPadding = 0.dp,
        dayFontWeight = FontWeight.Normal,
        otherMonthFontSize = 12.sp,
        selectedCircleSize = 18.dp,
        selectedCircleColor = SpiritTodoTheme.color.mainArea,
        selectedFontSize = 12.sp,
        selectedFontWeight = FontWeight.SemiBold,
        dotTopPadding = 0.dp,
        labelTopPadding = 4.dp
    )

    // 캘린더 화면 (피그마 Calendar1)
    @Composable
    fun calendarScreenStyle() = style().copy(
        monthTextSize = 20.sp,
        monthTextColor = SpiritTodoTheme.color.pointPink,
        prevIconRes = R.drawable.fi_rr_angle_small_left,
        nextIconRes = R.drawable.fi_rr_angle_small_right,
        navIconSize = 20.dp,
        navIconTint = SpiritTodoTheme.color.onSurfaceColor5,
        navIconGap = 14.dp,
        headerStartPadding = 14.dp,
        headerEndPadding = 8.dp,
        gridHorizontalPadding = 8.dp,
        weekdayTopPadding = 6.dp,
        weekdayBottomPadding = 4.dp,
        weekdayRowHeight = 25.dp,
        weekdayColor = SpiritTodoTheme.color.onSurfaceColor8,
        weekdayFontWeight = FontWeight.Medium,
        weekRowHeight = 54.dp,
        weekRowGap = 6.dp,
        dayBoxSize = 16.dp,
        dayBoxTopPadding = 2.dp,
        dayFontWeight = FontWeight.Medium,
        otherMonthFontSize = 10.sp,
        selectedCircleSize = 14.dp,
        selectedCircleColor = SpiritTodoTheme.color.pointPink,
        selectedFontSize = 10.sp,
        selectedFontWeight = FontWeight.Medium,
        dotTopPadding = 4.dp,
        labelTopPadding = 6.dp
    )
}

// 좌우로 무한히 넘길 수 있도록 넉넉한 페이지 수를 잡고, 그 중앙을 기준 달(anchorMonth)로 삼는다.
private const val CALENDAR_PAGE_COUNT = 100_000
private const val CALENDAR_START_PAGE = CALENDAR_PAGE_COUNT / 2


@Composable
fun CalendarView(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    showMonthNavigation: Boolean = true,
    showSelectedDateInHeader: Boolean = false,
    eventData: Map<LocalDate, CalendarDayEvent> = emptyMap(),
    onMonthChanged: (YearMonth) -> Unit = {},
    style: CalendarViewStyle = CalendarViewDefaults.style()
) {
    val today = remember { LocalDate.now() }
    val selectedDateFormatter = KoreanDateWithDayFormatter
    val scope = rememberCoroutineScope()
    val anchorMonth = remember { YearMonth.from(selectedDate) }
    fun monthForPage(page: Int): YearMonth =
        anchorMonth.plusMonths((page - CALENDAR_START_PAGE).toLong())
    fun pageForMonth(month: YearMonth): Int =
        CALENDAR_START_PAGE + ChronoUnit.MONTHS.between(anchorMonth, month).toInt()

    val pagerState = rememberPagerState(
        initialPage = pageForMonth(YearMonth.from(selectedDate)),
        pageCount = { CALENDAR_PAGE_COUNT }
    )

    val currentMonth = monthForPage(pagerState.currentPage)
    val headerDate = remember(currentMonth, today) {
        if (currentMonth == YearMonth.from(today)) today else currentMonth.atDay(1)
    }

    val currentOnMonthChanged by rememberUpdatedState(onMonthChanged)

    LaunchedEffect(selectedDate) {
        val target = pageForMonth(YearMonth.from(selectedDate))
        if (target != pagerState.currentPage) {
            pagerState.animateScrollToPage(target)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .drop(1)
            .collect { page ->
                currentOnMonthChanged(monthForPage(page))
            }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (showMonthNavigation) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = style.headerStartPadding, end = style.headerEndPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (showSelectedDateInHeader) {
                        headerDate.format(selectedDateFormatter)
                    } else {
                        stringResource(R.string.month, currentMonth.monthValue)
                    },
                    fontSize = style.monthTextSize,
                    fontWeight = FontWeight.Medium,
                    color = style.monthTextColor
                )

                Row(horizontalArrangement = Arrangement.spacedBy(style.navIconGap)) {
                    val navIconModifier = if (style.navIconSize != Dp.Unspecified) Modifier.size(style.navIconSize) else Modifier
                    val navIconColorFilter = style.navIconTint?.let { ColorFilter.tint(it) }

                    Image(
                        painter = painterResource(style.prevIconRes),
                        contentDescription = null,
                        colorFilter = navIconColorFilter,
                        modifier = navIconModifier.clickable(onClick = {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                        })
                    )

                    Image(
                        painter = painterResource(style.nextIconRes),
                        contentDescription = null,
                        colorFilter = navIconColorFilter,
                        modifier = navIconModifier.clickable(onClick = {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        })
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = style.weekdayTopPadding, bottom = style.weekdayBottomPadding)
                .padding(horizontal = style.gridHorizontalPadding)
                .then(if (style.weekdayRowHeight != Dp.Unspecified) Modifier.height(style.weekdayRowHeight) else Modifier),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val weekDays = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
            weekDays.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontWeight = style.weekdayFontWeight,
                    color = style.weekdayColor
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            // 인접한 달을 미리 구성/측정해 두어, 넘기기 시작하는 순간의 최초 컴포지션 비용을 없앤다.
            beyondViewportPageCount = 1,
            verticalAlignment = Alignment.Top
        ) { page ->
            CalendarMonthGrid(
                month = monthForPage(page),
                selectedDate = selectedDate,
                eventData = eventData,
                onDateSelected = onDateSelected,
                style = style,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = style.gridHorizontalPadding)
                    .graphicsLayer {
                        // 현재 페이지 기준 이 페이지의 상대 위치 (-1 ~ 1)
                        val pageOffset = pagerState.offsetForPage(page)

                        // 1. 회전축을 책이 접히는 가장자리(스파인)에 둔다.
                        transformOrigin = if (pageOffset > 0f) {
                            TransformOrigin(0f, 0.5f) // 오른쪽으로 넘길 때: 왼쪽 가장자리 고정
                        } else {
                            TransformOrigin(1f, 0.5f) // 왼쪽으로 넘길 때: 오른쪽 가장자리 고정
                        }

                        // 2. Y축 회전 (넘어가는 방향으로 최대 ±90도)
                        rotationY = lerp(
                            start = 0f,
                            stop = if (pageOffset > 0f) 90f else -90f,
                            fraction = pageOffset.absoluteValue.coerceIn(0f, 1f)
                        )

                        // 3. 3D 입체감을 위한 카메라 거리 (작을수록 원근 왜곡이 커짐)
                        cameraDistance = 16f * density

                        // 4. 넘어갈수록 서서히 페이드 아웃해 뒷장이 비치지 않게 한다. (|offset| 0.5 에서 완전 투명)
                        alpha = (1f - pageOffset.absoluteValue * 2f).coerceIn(0f, 1f)
                    }
            )
        }
    }
}

// 현재 페이지 기준 상대 오프셋(-1 ~ 1). 양수면 왼쪽으로 넘어가는(빠져나가는) 페이지.
private fun PagerState.offsetForPage(page: Int): Float =
    (currentPage - page) + currentPageOffsetFraction

@Composable
private fun CalendarMonthGrid(
    month: YearMonth,
    selectedDate: LocalDate,
    eventData: Map<LocalDate, CalendarDayEvent>,
    onDateSelected: (LocalDate) -> Unit,
    style: CalendarViewStyle,
    modifier: Modifier = Modifier
) {
    val calendarDays = remember(month) {
        val firstDayOfMonth = month.atDay(1)
        val leadingCount = firstDayOfMonth.dayOfWeek.value % 7

        val days = mutableListOf<CalendarDay>()
        for (offset in leadingCount downTo 1) {
            days.add(CalendarDay(firstDayOfMonth.minusDays(offset.toLong()), isCurrentMonth = false))
        }

        for (day in 1..month.lengthOfMonth()) {
            days.add(CalendarDay(month.atDay(day), isCurrentMonth = true))
        }

        // 항상 6주(42칸)로 채운다. 달마다 주 수가 달라지면 페이저 높이가 바뀌어 LazyColumn 전체가 relayout 되고, 그게 넘기기 시작/끝에서 버벅임으로 나타난다.
        val lastDayOfMonth = month.atEndOfMonth()
        val trailingCount = 42 - days.size
        for (offset in 1..trailingCount) {
            days.add(CalendarDay(lastDayOfMonth.plusDays(offset.toLong()), isCurrentMonth = false))
        }

        days
    }

    Column(modifier = modifier.fillMaxWidth()) {
        val chunks = calendarDays.chunked(7)
        chunks.forEachIndexed { weekIdx, week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(style.weekRowHeight)
            ) {
                week.forEach { calendarDay ->
                    val date = calendarDay.date
                    val isSelected = date == selectedDate
                    val event = eventData[date]

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .noRippleClickable { onDateSelected(date) },
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = style.dayBoxTopPadding)
                                .size(style.dayBoxSize),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .requiredSize(style.selectedCircleSize)
                                        .background(style.selectedCircleColor, CircleShape)
                                )
                            }

                            Text(
                                text = date.dayOfMonth.toString(),
                                color = when {
                                    isSelected -> SpiritTodoTheme.color.onSurfaceColor3
                                    !calendarDay.isCurrentMonth -> SpiritTodoTheme.color.surfaceColor15
                                    else -> SpiritTodoTheme.color.todoTextMain
                                },
                                fontSize = when {
                                    isSelected -> style.selectedFontSize
                                    !calendarDay.isCurrentMonth -> style.otherMonthFontSize
                                    else -> 12.sp
                                },
                                fontWeight = if (isSelected) style.selectedFontWeight else style.dayFontWeight,
                                softWrap = false
                            )
                        }

                        if (event != null && event.dotColors.isNotEmpty()) {
                            Row(
                                modifier = Modifier.padding(top = style.dotTopPadding),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                event.dotColors.forEach { dotColor ->
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .background(dotColor, CircleShape)
                                    )
                                }
                            }
                        }

                        if (event?.label != null) {
                            Box(
                                modifier = Modifier
                                    .padding(top = style.labelTopPadding)
                                    .background(SpiritTodoTheme.color.mainBackground, RoundedCornerShape(2.dp))
                                    .padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = event.label,
                                    fontSize = 10.sp,
                                    color = SpiritTodoTheme.color.todoTextMain,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            if (weekIdx != chunks.lastIndex) {
                Spacer(modifier = Modifier.height(style.weekRowGap))
            }
        }
    }
}
