package com.example.domain.exception

class FieldValidationException(
    val fieldErrors: Map<String, String>,
    message: String
) : Exception(message)
