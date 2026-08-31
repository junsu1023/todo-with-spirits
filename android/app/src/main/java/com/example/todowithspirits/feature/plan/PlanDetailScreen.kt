package com.example.todowithspirits.feature.plan

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.domain.model.CategoryOption
import com.example.domain.model.PublicStateOption
import com.example.todowithspirits.R
import com.example.todowithspirits.component.LoadingOverlay
import com.example.todowithspirits.component.TitleHeader
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.feature.add.viewmodel.toAlarmOption
import com.example.todowithspirits.feature.plan.model.PlanType
import com.example.todowithspirits.feature.plan.viewmodel.PlanDetailViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.util.KoreanDateWithDayFormatter
import java.time.format.DateTimeFormatter

@Composable
fun PlanDetailScreen(
    itemId: Int,
    onBack: () -> Unit,
    navigateToAdd: () -> Unit,
    planDetailViewModel: PlanDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(itemId) {
        planDetailViewModel.loadTask(itemId.toLong())
    }

    val planState by planDetailViewModel.plan.collectAsStateWithLifecycle()
    val isLoading by planDetailViewModel.isLoading.collectAsStateWithLifecycle()
    val item = planState

    if (isLoading) {
        LoadingOverlay(isLoading = true, background = SpiritTodoTheme.color.surfaceColor1)
        return
    }

    if(item == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SpiritTodoTheme.color.surfaceColor1),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "플랜 정보를 불러오지 못했습니다",
                fontSize = 15.sp,
                color = SpiritTodoTheme.color.todoTextMain
            )
        }
        return
    }

    val dateFormatter = KoreanDateWithDayFormatter
    val dateText = buildString {
        item.endDate?.let { date ->
            append(date.format(dateFormatter))
            if(item.endTime != null) {
                append(" " + item.endTime.format(DateTimeFormatter.ofPattern("HH:mm")))
            } else {
                append(" 하루 종일")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpiritTodoTheme.color.surfaceColor1)
    ) {
        TitleHeader(
            leftIconRes = R.drawable.todo_back1,
            onLeftIconClick = onBack,
            rightComposable =  {
                Row(horizontalArrangement = Arrangement.spacedBy(26.dp)) {
                    Image(
                        painter = painterResource(R.drawable.todo_pencil),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .noRippleClickable(onClick = navigateToAdd)
                    )

                    Image(
                        painter = painterResource(R.drawable.todo_trash),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(SpiritTodoTheme.color.todoTextMain),
                        modifier = Modifier
                            .size(24.dp)
                            .noRippleClickable {
                                planDetailViewModel.deleteTask(item.id.toLong(), onSuccess = onBack)
                            }
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 28.dp)
        ) {
            Box(
                modifier = Modifier
                    .border(1.dp, SpiritTodoTheme.color.mainTextAndStroke, RoundedCornerShape(30.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if(item.type == PlanType.TODO) stringResource(R.string.todo) else stringResource(R.string.routine),
                    color = SpiritTodoTheme.color.mainTextAndStroke,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(18.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = SpiritTodoTheme.color.todoTextMain,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if(item.isImportant) {
                    Spacer(Modifier.width(4.dp))

                    Image(
                        painter = painterResource(R.drawable.todo_important),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if(dateText.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))

                Text(
                    text = dateText,
                    fontSize = 14.sp,
                    color = SpiritTodoTheme.color.todoTextMain
                )
            }

            Spacer(Modifier.height(28.dp))

            HorizontalDivider(
                thickness = 1.dp,
                color = SpiritTodoTheme.color.onSurfaceColor2
            )

            Spacer(Modifier.height(30.dp))

            DetailInfoRow(
                iconRes = R.drawable.todo_alarm,
                text = item.notificationMinutes.toAlarmOption().displayName
            )

            Spacer(Modifier.height(26.dp))

            DetailInfoRow(
                iconRes = R.drawable.todo_category,
                text = item.category ?: CategoryOption.NONE.displayName
            )

            Spacer(Modifier.height(26.dp))

            DetailInfoRow(
                iconRes = R.drawable.todo_private,
                text = if(item.isPublic) PublicStateOption.PUBLIC.displayName else PublicStateOption.PRIVATE.displayName
            )

            Spacer(Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp)
                    .border(1.dp, SpiritTodoTheme.color.onSurfaceColor2, RoundedCornerShape(6.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = item.memo.ifEmpty { "메모" },
                    fontSize = 15.sp,
                    color = if(item.memo.isEmpty()) SpiritTodoTheme.color.onSurfaceColor2 else SpiritTodoTheme.color.todoTextMain
                )
            }

            Spacer(Modifier.height(24.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .background(SpiritTodoTheme.color.mainArea, RoundedCornerShape(6.dp))
                .noRippleClickable { onBack() }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.check),
                color = SpiritTodoTheme.color.onSurfaceColor3,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DetailInfoRow(iconRes: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = text,
            fontSize = 14.sp,
            color = SpiritTodoTheme.color.todoTextMain
        )
    }
}
