package com.example.todowithspirits.util

import android.content.Context
import android.widget.Toast

object ToastUtil {
    fun show(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }
}
