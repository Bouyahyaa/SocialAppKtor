package com.bouyahya.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class TokenRequest(
    val code: String
)