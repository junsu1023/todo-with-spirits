package com.example.todowithspirits

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.todowithspirits.navigation.SplitsTodoNavigation
import com.example.todowithspirits.theme.SplitsTodoTheme

@Composable
fun SplitsTodoApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    Scaffold(
        containerColor = SplitsTodoTheme.colors.bgColor1
    ) { innerPadding ->
        SplitsTodoNavigation(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}