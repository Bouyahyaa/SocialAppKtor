package com.bouyahya.feature_auth.service

import com.bouyahya.feature_auth.data.models.User
import com.bouyahya.feature_auth.data.repository.user.UserRepository
import com.bouyahya.feature_auth.data.requests.LoginRequest
import com.bouyahya.feature_auth.data.requests.RegisterRequest
import com.bouyahya.feature_auth.data.requests.ResetPasswordRequest
import com.bouyahya.feature_auth.events.AuthValidationEvent
import com.bouyahya.util.Constants
import org.mindrot.jbcrypt.BCrypt

class UserService(
    private val userRepository: UserRepository
) {

    suspend fun validateRegistration(request: RegisterRequest): AuthValidationEvent {

        val areFieldsBlank = request.username.isBlank() || request.email.isBlank() ||
                request.password.isBlank() || request.confirmPassword.isBlank()
        if (areFieldsBlank) {
            return AuthValidationEvent.ErrorFieldEmpty
        }

        val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
        if (!emailRegex.toRegex().matches(request.email)) {
            return AuthValidationEvent.InvalidEmail
        }

        if (getUserByEmail(request.email) != null) {
            return AuthValidationEvent.IsUserExist
        }

        val isUsernameTooShort = request.username.length < 3
        if (isUsernameTooShort) {
            return AuthValidationEvent.UsernameTooShort
        }

        val isPwTooShort = request.password.length < 8
        if (isPwTooShort) {
            return AuthValidationEvent.PasswordTooShort
        }

        val containsLettersAndDigits = request.password.any { it.isDigit() } && request.password.any { it.isLetter() }
        if (!containsLettersAndDigits) {
            return AuthValidationEvent.InvalidPassword
        }

        val doesPasswordsMatch = request.password == request.confirmPassword
        if (!doesPasswordsMatch) {
            return AuthValidationEvent.PasswordsNotMatch
        }

        return AuthValidationEvent.Success
    }

    suspend fun validateLogin(request: LoginRequest): AuthValidationEvent {
        val areFieldsBlank = request.email.isBlank() || request.password.isBlank()
        if (areFieldsBlank) {
            return AuthValidationEvent.ErrorFieldEmpty
        }

        if (getUserByEmail(request.email) == null) {
            return AuthValidationEvent.IsUserExist
        }

        if (!isEmailVerified(request.email)) {
            return AuthValidationEvent.EmailNotVerified
        }

        if (!isValidPassword(request.email, request.password)) {
            return AuthValidationEvent.InvalidPassword
        }

        return AuthValidationEvent.Success
    }


    fun validateResetPassword(request: ResetPasswordRequest): AuthValidationEvent {

        val areFieldsBlank = request.password.isBlank() || request.confirmPassword.isBlank()
        if (areFieldsBlank) {
            return AuthValidationEvent.ErrorFieldEmpty
        }

        val isPwTooShort = request.password.length < 8
        if (isPwTooShort) {
            return AuthValidationEvent.PasswordTooShort
        }

        val containsLettersAndDigits = request.password.any { it.isDigit() } && request.password.any { it.isLetter() }
        if (!containsLettersAndDigits) {
            return AuthValidationEvent.InvalidPassword
        }

        val doesPasswordsMatch = request.password == request.confirmPassword
        if (!doesPasswordsMatch) {
            return AuthValidationEvent.PasswordsNotMatch
        }

        return AuthValidationEvent.Success
    }

    suspend fun resetPassword(request: ResetPasswordRequest, email: String): Boolean {
        val hashedPassword = BCrypt.hashpw(request.password, BCrypt.gensalt())
        val user = getUserByEmail(email)
        val updatedUser = User(
            id = user?.id!!,
            email = user.email,
            username = user.username,
            password = hashedPassword,
            profileImageUrl = user.profileImageUrl,
            bio = user.bio,
            isVerified = user.isVerified,
        )
        return userRepository.updateUser(updatedUser)
    }

    suspend fun createUser(request: RegisterRequest) {
        val hashedPassword = BCrypt.hashpw(request.password, BCrypt.gensalt())
        userRepository.createUser(
            User(
                email = request.email,
                username = request.username,
                password = hashedPassword,
                profileImageUrl = Constants.DEFAULT_PROFILE_PICTURE_PATH,
                bio = "",
            )
        )
    }

    suspend fun getUserByEmail(email: String): User? {
        return userRepository.getUserByEmail(email)
    }

    suspend fun confirmEmailUser(email: String): Boolean {
        val user = getUserByEmail(email)
        val updatedUser = User(
            id = user?.id!!,
            email = user.email,
            username = user.username,
            password = user.password,
            profileImageUrl = user.profileImageUrl,
            bio = user.bio,
            isVerified = true,
        )
        return userRepository.updateUser(updatedUser)
    }

    private suspend fun isValidPassword(email: String, enteredPassword: String): Boolean {
        val user = userRepository.getUserByEmail(email)
        return BCrypt.checkpw(enteredPassword, user?.password)
    }

    private suspend fun isEmailVerified(email: String): Boolean {
        val user = userRepository.getUserByEmail(email)
        return user?.isVerified!!
    }
}