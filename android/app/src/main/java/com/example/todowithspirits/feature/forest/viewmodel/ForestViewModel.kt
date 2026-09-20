package com.example.todowithspirits.feature.forest.viewmodel

import androidx.lifecycle.ViewModel
import com.example.todowithspirits.util.ForestExitBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

@HiltViewModel
class ForestViewModel @Inject constructor(
    forestExitBus: ForestExitBus
) : ViewModel() {
    // ForestUnityActivity는 별도 태스크에서 moveTaskToBack만 하고 finish()하지 않으므로
    // ActivityResult로는 종료 시점을 알 수 없다. ForestExitBus로만 전달받는다.
    val exitEvents: SharedFlow<Unit> = forestExitBus.events
}
