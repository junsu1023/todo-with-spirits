package com.example.todowithspirits.feature.setting.editprofile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todowithspirits.R
import com.example.todowithspirits.component.LoadingOverlay
import com.example.todowithspirits.component.SpiritsTodoPrimaryButton
import com.example.todowithspirits.component.TopNaviBar
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.feature.setting.viewmodel.EditProfileViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.util.ToastUtil

@Composable
fun EditProfileScreen(
    editProfileViewModel: EditProfileViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by editProfileViewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by editProfileViewModel.isLoading.collectAsStateWithLifecycle()
    var value by remember { mutableStateOf("") }

    LaunchedEffect(editProfileViewModel) {
        editProfileViewModel.errorMsg.collect { message -> ToastUtil.show(context, message) }
    }

    LaunchedEffect(uiState.nickname) {
        value = uiState.nickname
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .background(SpiritTodoTheme.color.back01)
    ) {
        TopNaviBar(
            leftIconRes = R.drawable.todo_back1,
            onLeftIconClick = onBack,
            title = stringResource(R.string.edit_profile),
            modifier = Modifier.padding(horizontal = 17.dp)
        )

        Spacer(Modifier.height(14.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
        ) {
            Text(
                text = stringResource(R.string.nickname),
                fontSize = 14.sp,
                color = SpiritTodoTheme.color.font01
            )

            Spacer(Modifier.height(6.dp))

            NicknameField(
                value = value,
                onValueChange = { value = it }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        SpiritsTodoPrimaryButton(
            text = stringResource(R.string.check),
            onClick = { editProfileViewModel.updateNickname(value, onSuccess = onBack) },
            modifier = Modifier.padding(horizontal = 14.dp)
        )

        Spacer(Modifier.height(48.dp))
    }

    LoadingOverlay(isLoading = isLoading)
}

@Composable
private fun NicknameField(value: String, onValueChange: (String) -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .border(
                width = 1.dp,
                color = if(isFocused) SpiritTodoTheme.color.font01 else SpiritTodoTheme.color.stroke01,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 12.dp, vertical = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 14.sp,
                color = SpiritTodoTheme.color.font01
            ),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = stringResource(R.string.nickname),
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = SpiritTodoTheme.color.fontGrey
                            )
                        )
                    }

                    innerTextField()
                }
            },
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
                .onFocusChanged { isFocused = it.isFocused }
        )

        if (!isFocused || value.isNotEmpty()) {
            Image(
                painter = painterResource(if (isFocused) R.drawable.widgetly_cross_20 else R.drawable.widgetly_edit_18),
                contentDescription = null,
                modifier = Modifier.noRippleClickable {
                    if (isFocused) onValueChange("") else focusRequester.requestFocus()
                }
            )
        }
    }
}
