package com.example.todowithspirits.feature.signup.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.core.tag.TAG
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.exception.FieldValidationException
import com.example.domain.usecase.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
) : BaseViewModel() {
    private val _fieldErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val fieldErrors: StateFlow<Map<String, String>> get() = _fieldErrors.asStateFlow()

    fun signUp(email: String, password: String, nickname: String?, onSuccess: () -> Unit = {}) {
        _fieldErrors.update { emptyMap() }

        viewModelScope.launchWithLoading {
            signUpUseCase(email, password, nickname)
                .onSuccess {
                    Log.d(TAG, "signUp success = $it")
                    onSuccess()
                }
                .onFailure { error ->
                    Log.e(TAG, "signUp failed!", error)

                    if (error is FieldValidationException) {
                        _fieldErrors.update { error.fieldErrors }
                    } else {
                        emitErrorMsg(error.localizedMessage ?: "회원가입에 실패했습니다")
                    }
                }
        }
    }
}
