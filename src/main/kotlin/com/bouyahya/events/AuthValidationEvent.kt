package com.bouyahya.events

sealed class AuthValidationEvent {
    object ErrorFieldEmpty : AuthValidationEvent()
    object InvalidEmail : AuthValidationEvent()
    object InvalidPassword : AuthValidationEvent()
    object IsUserExist : AuthValidationEvent()
    object PasswordTooShort : AuthValidationEvent()
    object UsernameTooShort : AuthValidationEvent()
    object PasswordsNotMatch : AuthValidationEvent()
    object Success : AuthValidationEvent()
}