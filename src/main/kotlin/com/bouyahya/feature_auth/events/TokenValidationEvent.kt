package com.bouyahya.feature_auth.events

sealed class TokenValidationEvent {
    object ErrorFieldEmpty : TokenValidationEvent()
    object TokenTooShort : TokenValidationEvent()
    object InvalidToken : TokenValidationEvent()
    object TokensNotMatch : TokenValidationEvent()
    object AlreadyVerified : TokenValidationEvent()
    object Success : TokenValidationEvent()
}