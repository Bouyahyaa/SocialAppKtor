package com.bouyahya.data.responses

import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(
    val success: Boolean,
    val message: String,
)