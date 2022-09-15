package com.bouyahya.service

import com.bouyahya.data.models.Token
import com.bouyahya.data.repository.token.TokenRepository
import com.bouyahya.data.repository.user.UserRepository
import com.bouyahya.data.requests.TokenRequest
import com.bouyahya.events.TokenValidationEvent
import com.bouyahya.util.Constants

class TokenService(
    private val tokenRepository: TokenRepository,
    private val userRepository: UserRepository
) {
    suspend fun createToken(token: Token) {
        tokenRepository.createToken(token)
    }

    suspend fun getToken(userId: String, type: String): Token? {
        return tokenRepository.getToken(userId, type)
    }

    suspend fun deleteToken(token: Token): Boolean {
        return tokenRepository.deleteToken(token)
    }

    suspend fun validateToken(request: TokenRequest, email: String): TokenValidationEvent {

        if (request.code.isBlank()) {
            return TokenValidationEvent.ErrorFieldEmpty
        }

        val regex = Regex("[0-9]*")
        if (!regex.matches(request.code)) {
            return TokenValidationEvent.InvalidToken
        }

        if (request.code.length < 5) {
            return TokenValidationEvent.TokenTooShort
        }

        val user = userRepository.getUserByEmail(email)
        if (user?.isVerified!!) {
            return TokenValidationEvent.AlreadyVerified
        }

        val token = tokenRepository.getToken(user.id, Constants.EMAIL_CODE)
        if (token?.code != request.code) {
            return TokenValidationEvent.TokensNotMatch
        }

        return TokenValidationEvent.Success
    }
}