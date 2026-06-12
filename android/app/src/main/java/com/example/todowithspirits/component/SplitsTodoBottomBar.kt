package com.example.todowithspirits.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import com.example.todowithspirits.theme.SplitsTodoTheme

data class BottomNavItem(
    val route: String,
    @StringRes val label: Int,
    @DrawableRes val iconRes: Int
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Today.route, R.string.today, R.drawable.today),
    BottomNavItem(Screen.Plan.route, R.string.plan, R.drawable.plan),
    BottomNavItem(Screen.Forest.route, R.string.forest, R.drawable.forest),
    BottomNavItem(Screen.Record.route, R.string.record, R.drawable.record),
    BottomNavItem(Screen.MyPage.route, R.string.myPage, R.drawable.my_page)
)

@Composable
fun SplitsTodoBottomBar(
    currentRoute: String?,
    onItemSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = SplitsTodoTheme.colors.bgColor2,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(74.dp)
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemSelected(item.route) },
                icon = {
                    Icon(
                        painter = painterResource(item.iconRes),
                        contentDescription = stringResource(item.label)
                    )
                },
                label = {
                    Text(
                        text = stringResource(item.label),
                        style = TextStyle(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 8.sp,
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SplitsTodoTheme.colors.selectedColor,
                    selectedTextColor = SplitsTodoTheme.colors.selectedColor,
                    unselectedIconColor = SplitsTodoTheme.colors.unselectedColor,
                    unselectedTextColor = SplitsTodoTheme.colors.unselectedColor,
                    indicatorColor = SplitsTodoTheme.colors.transparent
                )
            )
        }
    }
}