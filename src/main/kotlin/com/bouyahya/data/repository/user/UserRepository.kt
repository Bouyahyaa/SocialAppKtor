package com.bouyahya.data.repository.user

import com.bouyahya.data.models.User

interface UserRepository {
    suspend fun createUser(user: User)
    suspend fun getUserByEmail(email: String): User?
}