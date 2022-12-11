package com.bouyahya.feature_auth.service

import com.bouyahya.feature_auth.data.models.Token
import com.bouyahya.feature_auth.data.repository.token.TokenRepository
import com.bouyahya.feature_auth.data.repository.user.UserRepository
import com.bouyahya.feature_auth.data.requests.TokenRequest
import com.bouyahya.feature_auth.events.TokenValidationEvent
import com.bouyahya.util.Constants
import java.util.*

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

    suspend fun validateConfirmationToken(request: TokenRequest, email: String): TokenValidationEvent {

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


    suspend fun validateResetPasswordToken(request: TokenRequest, email: String): TokenValidationEvent {

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
        val token = tokenRepository.getToken(user?.id!!, Constants.PASSWORD_CODE)
        if (token?.code != request.code) {
            return TokenValidationEvent.TokensNotMatch
        }

        return TokenValidationEvent.Success
    }


    fun generateToken(): Long {
        val lowerLimit = 12345L
        val upperLimit = 23456L
        val r = Random()
        return lowerLimit + (r.nextDouble() * (upperLimit - lowerLimit)).toLong()
    }
}