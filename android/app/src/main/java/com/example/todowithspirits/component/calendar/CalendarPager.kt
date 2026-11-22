package com.example.todowithspirits.component.calendar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
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

internal data class CalendarDay(
    val date: LocalDate,
    val isCurrentMonth: Boolean
)

/**
 * 달력의 공통 동작(무한 월 페이징, 선택 날짜 따라가기, 월 변경 콜백, 책장 넘김 애니메이션)만 담당한다.
 * 모양은 [header]와 [monthContent] 슬롯으로 각 달력이 정한다.
 */
@Composable
internal fun CalendarPager(
    selectedDate: LocalDate,
    modifier: Modifier = Modifier,
    onMonthChanged: (YearMonth) -> Unit = {},
    header: @Composable ColumnScope.(month: YearMonth, onPrevious: () -> Unit, onNext: () -> Unit) -> Unit,
    monthContent: @Composable (month: YearMonth) -> Unit
) {
    val scope = rememberCoroutineScope()
    val anchorMonth = remember { YearMonth.from(selectedDate) }
    fun monthForPage(page: Int): YearMonth = anchorMonth.plusMonths((page - 5000).toLong())
    fun pageForMonth(month: YearMonth): Int = 5000 + ChronoUnit.MONTHS.between(anchorMonth, month).toInt()
    val pagerState = rememberPagerState(
        initialPage = pageForMonth(YearMonth.from(selectedDate)),
        pageCount = { 100000 }
    )
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
        header(
            monthForPage(pagerState.currentPage),
            { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
            { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            // 인접한 달을 미리 구성/측정해 두어, 넘기기 시작하는 순간의 최초 컴포지션 비용을 없앤다.
            beyondViewportPageCount = 1,
            verticalAlignment = Alignment.Top
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
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
            ) {
                monthContent(monthForPage(page))
            }
        }
    }
}

private fun PagerState.offsetForPage(page: Int): Float = (currentPage - page) + currentPageOffsetFraction

internal fun weekDayLabels(firstDayOfWeek: DayOfWeek): List<String> =
    (0L until 7L).map {
        firstDayOfWeek
            .plus(it)
            .getDisplayName(TextStyle.SHORT, Locale.US).uppercase()
    }

/**
 * [month]를 주 시작 요일 기준 6주(42칸)로 채운 날짜 목록.
 * 달마다 주 수가 달라지면 페이저 높이가 바뀌어 상위 레이아웃 전체가 relayout 되고, 그게 넘기기 시작/끝에서 버벅임으로 나타난다.
 */
internal fun calendarDaysOf(month: YearMonth, firstDayOfWeek: DayOfWeek): List<CalendarDay> {
    val firstDayOfMonth = month.atDay(1)
    // 주 시작 요일부터 1일 전까지 채울 이전 달 날짜 수
    val leadingCount = (firstDayOfMonth.dayOfWeek.value - firstDayOfWeek.value + 7) % 7
    val gridStart = firstDayOfMonth.minusDays(leadingCount.toLong())

    return (0L until 42L).map { offset ->
        val date = gridStart.plusDays(offset)
        CalendarDay(date = date, isCurrentMonth = YearMonth.from(date) == month)
    }
}
