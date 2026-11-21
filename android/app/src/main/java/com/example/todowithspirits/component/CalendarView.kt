package com.example.todowithspirits.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.absoluteValue
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

private data class CalendarDay(
    val date: LocalDate,
    val isCurrentMonth: Boolean
)

@Composable
fun CalendarView(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    eventLabels: Map<LocalDate, String?> = emptyMap(),
    onMonthChanged: (YearMonth) -> Unit = {},
    firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY
) {
    val scope = rememberCoroutineScope()
    val anchorMonth = remember { YearMonth.from(selectedDate) }
    fun monthForPage(page: Int): YearMonth = anchorMonth.plusMonths((page - 50000).toLong())
    fun pageForMonth(month: YearMonth): Int = 50000 + ChronoUnit.MONTHS.between(anchorMonth, month).toInt()
    val pagerState = rememberPagerState(
        initialPage = pageForMonth(YearMonth.from(selectedDate)),
        pageCount = { 100000 }
    )
    val currentMonth = monthForPage(pagerState.currentPage)
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SpiritTodoTheme.color.white, RoundedCornerShape(8.dp))
            .border(1.dp, SpiritTodoTheme.color.stroke01, RoundedCornerShape(8.dp))
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        CalendarHeader(
            year = currentMonth.year,
            month = currentMonth.monthValue,
            onNext = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
            onPrevious = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } }
        )

        Spacer(modifier = Modifier.height(4.dp))

        CalendarWeekHeader(firstDayOfWeek)

        Spacer(modifier = Modifier.height(6.dp))

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
                eventLabels = eventLabels,
                onDateSelected = onDateSelected,
                firstDayOfWeek = firstDayOfWeek,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .graphicsLayer {
                        val pageOffset = pagerState.offsetForPage(page)

                        // 회전축을 책이 접히는 가장자리(스파인)에 둔다.
                        transformOrigin = when(pageOffset > 0f) {
                            true -> TransformOrigin(0f, 0.5f) // 오른쪽으로 넘길 때: 왼쪽 가장자리 고정
                            false -> TransformOrigin(1f, 0.5f) // 왼쪽으로 넘길 때: 오른쪽 가장자리 고정
                        }

                        // Y축 회전 (넘어가는 방향으로 최대 ±90도)
                        rotationY = lerp(
                            start = 0f,
                            stop = if (pageOffset > 0f) 90f else -90f,
                            fraction = pageOffset.absoluteValue.coerceIn(0f, 1f)
                        )

                        // 3D 입체감을 위한 카메라 거리 (작을수록 원근 왜곡이 커짐)
                        cameraDistance = 16f * density

                        // 넘어갈수록 서서히 페이드 아웃해 뒷장이 비치지 않게 한다. (|offset| 0.5 에서 완전 투명)
                        alpha = (1f - pageOffset.absoluteValue * 2f).coerceIn(0f, 1f)
                    }
            )
        }
    }
}

private fun PagerState.offsetForPage(page: Int): Float = (currentPage - page) + currentPageOffsetFraction

@Composable
private fun CalendarHeader(
    year: Int,
    month: Int,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.year_month, year, month),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = SpiritTodoTheme.color.font01
        )

        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Image(
                painter = painterResource(R.drawable.widgetly_left_20),
                contentDescription = null,
                modifier = Modifier.clickable(onClick = onPrevious)
            )

            Image(
                painter = painterResource(R.drawable.widgetly_right_20),
                contentDescription = null,
                modifier = Modifier.clickable(onClick = onNext)
            )
        }
    }
}

@Composable
private fun CalendarWeekHeader(firstDayOfWeek: DayOfWeek) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(25.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val weekDays = remember(firstDayOfWeek) {
            (0L until 7L).map {
                firstDayOfWeek
                    .plus(it)
                    .getDisplayName(TextStyle.SHORT, Locale.US).uppercase()
            }
        }

        weekDays.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = SpiritTodoTheme.color.font02
            )
        }
    }
}

@Composable
private fun CalendarMonthGrid(
    month: YearMonth,
    selectedDate: LocalDate,
    eventLabels: Map<LocalDate, String?>,
    onDateSelected: (LocalDate) -> Unit,
    firstDayOfWeek: DayOfWeek,
    modifier: Modifier = Modifier
) {
    val calendarDays = remember(month, firstDayOfWeek) {
        val firstDayOfMonth = month.atDay(1)
        // 주 시작 요일부터 1일 전까지 채울 이전 달 날짜 수
        val leadingCount = (firstDayOfMonth.dayOfWeek.value - firstDayOfWeek.value + 7) % 7
        val days = mutableListOf<CalendarDay>()

        for (offset in leadingCount downTo 1) {
            days.add(
                CalendarDay(
                    date = firstDayOfMonth.minusDays(offset.toLong()),
                    isCurrentMonth = false
                )
            )
        }

        for (day in 1..month.lengthOfMonth()) {
            days.add(
                CalendarDay(
                    date = month.atDay(day),
                    isCurrentMonth = true
                )
            )
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
                    .height(54.dp)
            ) {
                week.forEach { calendarDay ->
                    val date = calendarDay.date
                    val isSelected = date == selectedDate
                    val hasEvent = date in eventLabels
                    val label = eventLabels[date]

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .noRippleClickable { onDateSelected(date) },
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        DayArea(
                            isSelected = isSelected,
                            isCurrentMonth = calendarDay.isCurrentMonth,
                            day = date.dayOfMonth.toString()
                        )

                        if (hasEvent) {
                            DotArea()
                        }

                        if (label != null) {
                            LabelArea(label)
                        }
                    }
                }
            }

            if (weekIdx != chunks.lastIndex) {
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun DayArea(
    isSelected: Boolean,
    isCurrentMonth: Boolean,
    day: String
) {
    Box(
        modifier = Modifier
            .padding(top = 2.dp)
            .size(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .requiredSize(14.dp)
                    .background(SpiritTodoTheme.color.key01, CircleShape)
            )
        }

        Text(
            text = day,
            color = when {
                isSelected -> SpiritTodoTheme.color.onSurfaceColor3
                !isCurrentMonth -> SpiritTodoTheme.color.surfaceColor15
                else -> SpiritTodoTheme.color.todoTextMain
            },
            fontSize = when {
                isSelected -> 10.sp
                else -> 12.sp
            },
            fontWeight = FontWeight.Medium,
            softWrap = false
        )
    }
}

@Composable
private fun DotArea() {
    Row(
        modifier = Modifier.padding(top = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .background(SpiritTodoTheme.color.key02, CircleShape)
        )
    }
}

@Composable
private fun LabelArea(label: String) {
    Box(
        modifier = Modifier
            .padding(top = 6.dp)
            .background(SpiritTodoTheme.color.key02Op, RoundedCornerShape(2.dp))
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = SpiritTodoTheme.color.font01,
            maxLines = 1
        )
    }
}