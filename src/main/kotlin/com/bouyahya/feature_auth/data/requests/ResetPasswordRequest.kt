package com.bouyahya.feature_auth.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordRequest(
    val password: String,
    val confirmPassword: String
)