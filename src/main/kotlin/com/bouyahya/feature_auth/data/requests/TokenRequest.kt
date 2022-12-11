package com.bouyahya.feature_auth.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class TokenRequest(
    val code: String
)