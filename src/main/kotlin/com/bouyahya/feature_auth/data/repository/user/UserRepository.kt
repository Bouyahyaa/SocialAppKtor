package com.bouyahya.feature_auth.data.repository.user

import com.bouyahya.feature_auth.data.models.User

interface UserRepository {
    suspend fun createUser(user: User)
    suspend fun getUserByEmail(email: String): User?
    suspend fun updateUser(user: User): Boolean
}