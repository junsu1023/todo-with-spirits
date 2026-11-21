package com.example.todowithspirits.widget

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.todowithspirits.MainActivity
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.spiritsLightColor
import com.example.todowithspirits.util.KoreanHourMinuteFormatter

private val WidgetHorizontalPadding = 16.dp
private val WidgetVerticalPadding = 12.dp
private val TaskRowHeight = 36.dp
private val TaskRowGap = 6.dp
private val CheckSize = 18.dp
private val SpiritPeekSize = 32.dp

// Glance는 SpiritTodoTheme을 쓸 수 없어 같은 색 토큰을 ColorProvider로 감싸 쓴다.
internal object WidgetColors {
    val background get() = spiritsLightColor.surfaceColor1
    val taskRow get() = spiritsLightColor.surfaceColor16
    val date get() = spiritsLightColor.pointPink
    val text get() = spiritsLightColor.todoTextMain
    val subText get() = spiritsLightColor.systemGrey
    val accent get() = spiritsLightColor.mainTextAndStroke
    val track get() = spiritsLightColor.mainBackground
    val divider get() = spiritsLightColor.systemArea
    val sunday get() = spiritsLightColor.systemRed
    val onAccent get() = spiritsLightColor.onSurfaceColor3
}

internal fun Color.provider(): ColorProvider = ColorProvider(this)

// cornerRadius는 Android 12 미만에서 무시되므로, 모양 drawable을 테마 색으로 틴트해 배경으로 깐다.
internal fun GlanceModifier.shapeBackground(@DrawableRes shape: Int, color: Color): GlanceModifier =
    background(ImageProvider(shape), colorFilter = ColorFilter.tint(color.provider()))

// 위젯 바깥 카드. 빈 곳을 누르면 앱을 연다.
@Composable
internal fun TodoWidgetCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .shapeBackground(R.drawable.widget_shape_rounded_20, WidgetColors.background)
            .padding(horizontal = WidgetHorizontalPadding, vertical = WidgetVerticalPadding)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        content()
    }
}

@Composable
internal fun TodoWidgetHeader(
    title: String,
    completedCount: Int,
    totalCount: Int
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = TextStyle(
                color = WidgetColors.date.provider(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )

        Spacer(GlanceModifier.defaultWeight())

        Text(
            text = "$completedCount / $totalCount",
            style = TextStyle(
                color = WidgetColors.text.provider(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        )

        Spacer(GlanceModifier.width(8.dp))

        AddTaskButton()
    }
}

// 앱을 띄우지 않고 일정 추가 화면만 연다. 저장하면 화면이 닫히고 위젯만 갱신된다.
@Composable
private fun AddTaskButton() {
    Box(
        modifier = GlanceModifier
            .size(28.dp)
            .shapeBackground(R.drawable.widget_shape_circle, WidgetColors.accent)
            .clickable(actionStartActivity<WidgetAddTaskActivity>()),
        contentAlignment = Alignment.Center
    ) {
        Image(
            provider = ImageProvider(R.drawable.widgetly_plus_14),
            contentDescription = "일정 추가",
            modifier = GlanceModifier.size(12.dp)
        )
    }
}

@Composable
internal fun TodoWidgetProgressBar(progress: Float) {
    LinearProgressIndicator(
        progress = progress,
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(4.dp),
        color = WidgetColors.accent.provider(),
        backgroundColor = WidgetColors.track.provider()
    )
}

// 일정 목록 + 오른쪽 아래에 고개를 내민 정령
@Composable
internal fun TodoWidgetTaskList(
    tasks: List<WidgetTask>,
    modifier: GlanceModifier = GlanceModifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomEnd
    ) {
        LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
            items(tasks, itemId = { it.id }) { task ->
                Column(modifier = GlanceModifier.padding(bottom = TaskRowGap)) {
                    TodoWidgetTaskRow(task)
                }
            }

            // 마지막 일정이 정령에 가려지지 않도록 아래 여백을 둔다.
            item { Spacer(GlanceModifier.height(SpiritPeekSize - TaskRowGap)) }
        }

        Image(
            provider = ImageProvider(R.drawable.wind_spirit),
            contentDescription = null,
            modifier = GlanceModifier.size(SpiritPeekSize)
        )
    }
}

@Composable
private fun TodoWidgetTaskRow(task: WidgetTask) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(TaskRowHeight)
            .shapeBackground(R.drawable.widget_shape_rounded_8, WidgetColors.taskRow)
            .padding(end = 12.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 체크 아이콘보다 넓게 눌리도록 왼쪽 여백까지 터치 영역에 포함한다.
        Box(
            modifier = GlanceModifier
                .width(CheckSize + 18.dp)
                .height(TaskRowHeight)
                .padding(start = 12.dp)
                .clickable(
                    actionRunCallback<ChangeTaskCompletionAction>(
                        actionParametersOf(
                            ChangeTaskCompletionAction.TaskIdKey to task.id,
                            ChangeTaskCompletionAction.DateKey to task.date.toString(),
                            ChangeTaskCompletionAction.IsCompletedKey to !task.isCompleted
                        )
                    )
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            Image(
                provider = ImageProvider(
                    if (task.isCompleted) R.drawable.ic_check_circle_on_18 else R.drawable.ic_check_circle_off_18
                ),
                contentDescription = if (task.isCompleted) "완료 취소" else "완료",
                modifier = GlanceModifier.size(CheckSize)
            )
        }

        if (task.isImportant) {
            Image(
                provider = ImageProvider(R.drawable.fi_rr_color_star),
                contentDescription = null,
                modifier = GlanceModifier.size(14.dp)
            )
            Spacer(GlanceModifier.width(4.dp))
        }

        Text(
            text = task.title,
            style = TextStyle(
                color = (if (task.isCompleted) WidgetColors.subText else WidgetColors.text).provider(),
                fontSize = 14.sp,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
            ),
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight()
        )

        task.time?.let { time ->
            Spacer(GlanceModifier.width(8.dp))
            Text(
                text = time.format(KoreanHourMinuteFormatter),
                style = TextStyle(color = WidgetColors.subText.provider(), fontSize = 12.sp)
            )
        }
    }
}

// 일정 없음 / 모두 완료 / 로그인 필요 등 가운데 정령과 함께 보여주는 안내
@Composable
internal fun TodoWidgetMessage(
    title: String,
    modifier: GlanceModifier = GlanceModifier,
    description: String? = null,
    button: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = ImageProvider(R.drawable.wind_spirit),
            contentDescription = null,
            modifier = GlanceModifier.size(44.dp)
        )

        Spacer(GlanceModifier.height(8.dp))

        Text(
            text = title,
            style = TextStyle(
                color = WidgetColors.text.provider(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            ),
            maxLines = 2
        )

        description?.let {
            Spacer(GlanceModifier.height(2.dp))
            Text(
                text = it,
                style = TextStyle(color = WidgetColors.subText.provider(), fontSize = 12.sp),
                maxLines = 1
            )
        }

        button?.let {
            Spacer(GlanceModifier.height(8.dp))
            it()
        }
    }
}

@Composable
private fun TodoWidgetPillButton(
    text: String,
    modifier: GlanceModifier
) {
    Box(
        modifier = modifier
            .shapeBackground(R.drawable.widget_shape_pill, WidgetColors.accent)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                color = WidgetColors.onAccent.provider(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

// 일정을 불러오기 전 상태(불러오는 중 / 로그인 필요 / 실패). 두 위젯이 같은 모양을 쓴다.
@Composable
internal fun TodoWidgetStatusMessage(state: TodoWidgetState) {
    when (state) {
        TodoWidgetState.Loading -> TodoWidgetMessage(
            title = "일정을 불러오고 있어요",
            modifier = GlanceModifier.fillMaxSize()
        )

        TodoWidgetState.LoggedOut -> TodoWidgetMessage(
            title = "로그인하고 오늘 일정을 확인해 보세요",
            modifier = GlanceModifier.fillMaxSize(),
            button = {
                TodoWidgetPillButton(
                    text = "앱 열기",
                    modifier = GlanceModifier.clickable(actionStartActivity<MainActivity>())
                )
            }
        )

        TodoWidgetState.Error -> TodoWidgetMessage(
            title = "일정을 불러오지 못했어요",
            modifier = GlanceModifier.fillMaxSize(),
            button = {
                TodoWidgetPillButton(
                    text = "다시 시도",
                    modifier = GlanceModifier.clickable(actionRunCallback<RefreshTodoWidgetAction>())
                )
            }
        )

        is TodoWidgetState.Loaded -> Unit
    }
}

// 오늘 일정이 하나도 없을 때
@Composable
internal fun TodoWidgetEmptyMessage(modifier: GlanceModifier = GlanceModifier) {
    TodoWidgetMessage(
        title = "오늘 일정이 없어요",
        description = "+ 눌러서 추가해 보세요",
        modifier = modifier
    )
}
