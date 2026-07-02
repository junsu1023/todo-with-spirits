package com.example.todowithspirits

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import com.example.todowithspirits.theme.SpiritTodoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val systemHealth = mainViewModel.systemHealth.collectAsState()
            println("test-kjs: systemHealth = ${systemHealth.value}")

            SpiritTodoTheme {
                SpiritsTodoApp()
            }
        }
    }
}