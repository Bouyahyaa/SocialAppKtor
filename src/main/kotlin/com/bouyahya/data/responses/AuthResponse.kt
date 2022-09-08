package com.bouyahya.data.responses

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val userId: String? = null,
    val token: String? = null,
    val success: Boolean,
    val message: String
)