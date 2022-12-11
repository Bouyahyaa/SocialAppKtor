package com.bouyahya.feature_auth.data.responses

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val userId: String? = null,
    val token: String? = null,
    val success: Boolean,
    val message: String
)