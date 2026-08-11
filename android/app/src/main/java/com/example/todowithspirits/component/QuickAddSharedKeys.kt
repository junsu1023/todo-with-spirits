package com.example.todowithspirits.component

import androidx.compose.ui.unit.dp

// FloatingButton <-> QuickAddBottomPopup 컨테이너/아이콘 shared element 전환에 쓰는 공용 키.
object QuickAddSharedKeys {
    const val CONTAINER = "quickAddContainer"
    const val ICON = "quickAddIcon"
}

// FloatingButton의 + 와 QuickAddBottomPopup의 + 가 항상 같은 위치에 오도록 하는 공용 레이아웃 값.
// 두 크기 모두 각자의 컨테이너(FAB 원 / 팝업의 원형 배지) 안에서 아이콘이 정중앙에 오므로,
// "컨테이너 중심 = 아이콘 중심"이 되어 아이콘 자체의 크기는 계산에 영향을 주지 않는다.
// FloatingButton.kt, QuickAddBottomPopup.kt는 이 값들을 직접 쓰거나(FAB 크기, 배지 크기) 이 값들로
// 계산된 마진(QuickAddFabEndMargin/BottomMargin, QuickAddPopupBottomInset)을 써야 한다 - 하드코딩하면
// 둘 중 하나만 바뀌었을 때 위치가 어긋난다.
val QuickAddFabSize = 42.dp
val QuickAddPopupIconBadgeSize = 26.dp

private val FAB_TO_BOTTOM_BAR_GAP = 20.dp
private val QUICK_ADD_POPUP_SURFACE_HORIZONTAL_MARGIN = 14.dp
private val QUICK_ADD_POPUP_COLUMN_HORIZONTAL_PADDING = 14.dp
private val QUICK_ADD_POPUP_COLUMN_BOTTOM_PADDING = 12.dp

// FAB(원)의 바닥 여백. SpiritsTodoBottomBar 상단에서 정확히 20dp 떨어지도록 한다.
val QuickAddFabBottomMargin = BottomBarHeight + FAB_TO_BOTTOM_BAR_GAP

// FAB의 + 중심 x가 팝업의 + 배지 중심 x와 같아지도록 역산한 FAB의 오른쪽 여백.
// 팝업 쪽 배지 중심까지의 거리 = Surface 바깥 마진 + Column 안쪽 패딩 + 배지 반지름
// FAB 쪽 + 중심까지의 거리 = FAB 오른쪽 여백 + FAB 반지름
// 두 식을 같게 풀어서 FAB 오른쪽 여백을 구한다.
val QuickAddFabEndMargin = QUICK_ADD_POPUP_SURFACE_HORIZONTAL_MARGIN +
    QUICK_ADD_POPUP_COLUMN_HORIZONTAL_PADDING +
    QuickAddPopupIconBadgeSize / 2 -
    QuickAddFabSize / 2

// QuickAddBottomPopup의 + 배지 중심이 FAB의 + 중심과 같은 높이에 오도록 역산한 하단 inset.
// FAB 아이콘 중심 = QuickAddFabBottomMargin + FAB 반지름
// 팝업 배지 중심 = X + Column 아래쪽 패딩 + 배지 반지름
// 두 식을 같게 풀어서 X를 구한다.
val QuickAddPopupBottomInset = QuickAddFabBottomMargin +
    QuickAddFabSize / 2 -
    QUICK_ADD_POPUP_COLUMN_BOTTOM_PADDING -
    QuickAddPopupIconBadgeSize / 2
