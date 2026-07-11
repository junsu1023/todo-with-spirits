package com.example.core.auth

object TokenHolder {
    @Volatile
    var accessToken: String? = null
}
