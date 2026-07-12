package com.example.todowithspirits.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.navigation.Screen
import com.example.todowithspirits.theme.SpiritTodoTheme

val BottomBarHeight = 74.dp

data class BottomNavItem(
    val route: String,
    @param:StringRes val label: Int,
    @param:DrawableRes val selectedIconRes: Int,
    @param:DrawableRes val unselectedIconRes: Int
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Today.route, R.string.today, R.drawable.home_selected, R.drawable.home_status),
    BottomNavItem(Screen.Plan.route, R.string.plan, R.drawable.plan_selected, R.drawable.plan_status),
    BottomNavItem(Screen.Forest.route, R.string.forest, R.drawable.forest_selected, R.drawable.forest_status),
    BottomNavItem(Screen.Record.route, R.string.record, R.drawable.record_selected, R.drawable.record_status),
    BottomNavItem(Screen.MyPage.route, R.string.my_page, R.drawable.my_page_selected, R.drawable.my_page_status)
)

@Composable
fun SpiritsTodoBottomBar(
    currentRoute: String?,
    onItemSelected: (String) -> Unit
) {
    Surface(
        color = SpiritTodoTheme.color.surfaceColor1,
        tonalElevation = 0.dp,
        modifier = Modifier.navigationBarsPadding()
    ) {
        NavigationBar(
            containerColor = SpiritTodoTheme.color.surfaceColor1,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets(0, 0, 0, 0),
            modifier = Modifier
                .fillMaxWidth()
                .height(BottomBarHeight)
                .padding(horizontal = 10.dp)
        ) {
            bottomNavItems.forEach { item ->
                val isSelected = currentRoute == item.route

                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onItemSelected(item.route) },
                    icon = {
                        Image(
                            painter = if(isSelected) painterResource(item.selectedIconRes) else painterResource(item.unselectedIconRes),
                            contentDescription = stringResource(item.label)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(item.label),
                            style = TextStyle(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 8.sp
                            )
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedTextColor = SpiritTodoTheme.color.todoTextMain,
                        unselectedTextColor = SpiritTodoTheme.color.onSurfaceColor2,
                        indicatorColor = SpiritTodoTheme.color.transparent
                    )
                )
            }
        }
    }
}
