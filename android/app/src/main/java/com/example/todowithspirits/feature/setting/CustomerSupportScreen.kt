package com.example.todowithspirits.feature.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.todowithspirits.R
import com.example.todowithspirits.component.SettingActionRow
import com.example.todowithspirits.component.TitleHeader
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun CustomerSupportScreen(
    onBack: () -> Unit = {},
    onNoticeClick: () -> Unit = {},
    onUsageGuideClick: () -> Unit = {},
    onFaqClick: () -> Unit = {},
    onInquiryClick: () -> Unit = {},
    onTermsAndPolicyClick: () -> Unit = {},
    onOpenSourceLicenseClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpiritTodoTheme.color.surfaceColor1)
            .verticalScroll(rememberScrollState())
    ) {
        TitleHeader(
            leftIconRes = R.drawable.todo_back1,
            title = stringResource(R.string.customer_support),
            onLeftIconClick = onBack
        )

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            SettingActionRow(label = stringResource(R.string.notice), onClick = onNoticeClick)

            SettingActionRow(label = stringResource(R.string.usage_guide), onClick = onUsageGuideClick)

            SettingActionRow(label = stringResource(R.string.faq), onClick = onFaqClick)

            SettingActionRow(label = stringResource(R.string.inquiry), onClick = onInquiryClick)

            SettingActionRow(label = stringResource(R.string.terms_and_policy), onClick = onTermsAndPolicyClick)

            SettingActionRow(label = stringResource(R.string.open_source_license), onClick = onOpenSourceLicenseClick)
        }

        Spacer(Modifier.height(24.dp))
    }
}
