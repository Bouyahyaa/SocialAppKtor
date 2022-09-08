package com.bouyahya.data.repository.user

import com.bouyahya.data.models.User
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class UserRepositoryImpl(db: CoroutineDatabase) : UserRepository {

    private val users = db.getCollection<User>()

    override suspend fun createUser(user: User) {
        users.insertOne(user)
    }


    override suspend fun getUserByEmail(email: String): User? {
        return users.findOne(User::email eq email)
    }
}