package com.example.todowithspirits

import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import com.example.todowithspirits.theme.SpiritTodoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var rootFocusManager: FocusManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            SpiritTodoTheme {
                rootFocusManager = LocalFocusManager.current
                SpiritsTodoApp()
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if(ev?.action == MotionEvent.ACTION_DOWN || ev?.action == MotionEvent.ACTION_MOVE) {
            val focusView = currentFocus

            if(focusView != null) {
                val outRect = Rect()
                focusView.getGlobalVisibleRect(outRect)

                val x = ev.rawX.toInt()
                val y = ev.rawY.toInt()

                if(outRect.contains(x, y)) {
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(focusView.windowToken, 0)
                    focusView.clearFocus()
                    rootFocusManager?.clearFocus(force = true)
                }
            }
        }

        return super.dispatchTouchEvent(ev)
    }
}