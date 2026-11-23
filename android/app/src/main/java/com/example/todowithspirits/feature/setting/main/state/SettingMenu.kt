package com.example.todowithspirits.feature.setting.main.state

import androidx.annotation.StringRes
import com.example.todowithspirits.R

enum class SettingMenu(@StringRes val labelRes: Int) {
    NICKNAME(R.string.nickname),
    ALARM(R.string.alarm),
    DISPLAY(R.string.display),
    DATA(R.string.data),
    NOTICE(R.string.notice),
    USAGE_GUIDE(R.string.usage_guide),
    QNA(R.string.qna),
    TERMS_AND_POLICY(R.string.terms_and_policy),
    CHANGE_PASSWORD(R.string.change_password),
    WITHDRAW(R.string.withdraw)
}
