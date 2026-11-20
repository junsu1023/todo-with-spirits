package com.example.todowithspirits.feature.bestiary

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todowithspirits.R
import com.example.todowithspirits.component.ListOptionRow
import com.example.todowithspirits.component.LoadingOverlay
import com.example.todowithspirits.component.TopNaviBar
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.feature.bestiary.component.BestiarySpiritCard
import com.example.todowithspirits.feature.bestiary.state.BestiaryEffect
import com.example.todowithspirits.feature.bestiary.state.BestiaryEvent
import com.example.todowithspirits.feature.bestiary.state.BestiaryUiState
import com.example.todowithspirits.feature.bestiary.viewmodel.BestiaryViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.util.ObserveAsEffect
import com.example.todowithspirits.util.ToastUtil

private val BestiaryHorizontalMargin = 18.dp

@Composable
fun BestiaryRoute(
    navigateBack: () -> Unit,
    viewModel: BestiaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.errorMsg.collect { message -> ToastUtil.show(context, message) }
    }

    ObserveAsEffect(viewModel.effect) { effect ->
        when (effect) {
            BestiaryEffect.NavigateBack -> navigateBack()
        }
    }

    BestiaryScreen(
        uiState = uiState,
        isLoading = isLoading,
        onEvent = viewModel::onEvent
    )
}

// 정령도감. 전체 정령 종류를 3열 격자로 보여주고, 아직 못 만난 정령은 발견 전으로 표시한다.
@Composable
fun BestiaryScreen(
    uiState: BestiaryUiState,
    isLoading: Boolean,
    onEvent: (BestiaryEvent) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SpiritTodoTheme.color.surfaceColor1)
        ) {
            TopNaviBar(
                title = stringResource(R.string.spirit_bestiary_title),
                leftComposable = {
                    Image(
                        painter = painterResource(R.drawable.todo_back1),
                        contentDescription = null,
                        modifier = Modifier
                            .size(26.dp)
                            .noRippleClickable { onEvent(BestiaryEvent.BackClicked) }
                    )
                }
            )

            ListOptionRow(
                checkLabel = stringResource(R.string.show_unowned_only),
                isChecked = uiState.isUnownedOnly,
                onCheckToggled = { onEvent(BestiaryEvent.UnownedOnlyToggled) },
                sortLabel = stringResource(uiState.sortOption.labelRes),
                sortLabels = uiState.sortOptions.map { stringResource(it.labelRes) },
                onSortSelected = { index ->
                    onEvent(BestiaryEvent.SortOptionSelected(uiState.sortOptions[index]))
                },
                modifier = Modifier.padding(
                    start = BestiaryHorizontalMargin,
                    top = 8.dp,
                    end = BestiaryHorizontalMargin,
                    bottom = 16.dp
                )
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = BestiaryHorizontalMargin,
                    end = BestiaryHorizontalMargin,
                    bottom = 24.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.visibleSpirits, key = { it.speciesId }) { spirit ->
                    BestiarySpiritCard(spirit = spirit)
                }
            }
        }

        LoadingOverlay(isLoading = isLoading)
    }
}
