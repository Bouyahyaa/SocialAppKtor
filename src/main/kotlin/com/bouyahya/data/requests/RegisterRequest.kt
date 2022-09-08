package com.bouyahya.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
)