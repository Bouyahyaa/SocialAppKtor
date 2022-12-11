package com.bouyahya.feature_auth.data.repository.token

import com.bouyahya.feature_auth.data.models.Token

interface TokenRepository {
    suspend fun createToken(token: Token)
    suspend fun getToken(userId: String, type: String): Token?
    suspend fun deleteToken(token: Token): Boolean
}