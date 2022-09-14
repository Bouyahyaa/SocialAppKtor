package com.bouyahya.data.repository.token

import com.bouyahya.data.models.Token

interface TokenRepository {
    suspend fun createToken(token: Token)
    suspend fun getToken(userId: String, type: String): Token?
    suspend fun deleteToken(token: Token): Boolean
}