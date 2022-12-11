package com.bouyahya.feature_auth.data.repository.token

import com.bouyahya.feature_auth.data.models.Token
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class TokenRepositoryImpl(db: CoroutineDatabase) : TokenRepository {
    private val tokens = db.getCollection<Token>()

    override suspend fun createToken(token: Token) {
        tokens.insertOne(token)
    }

    override suspend fun getToken(userId: String, type: String): Token? {
        return tokens.findOne(Token::userId eq userId, Token::type eq type)
    }

    override suspend fun deleteToken(token: Token): Boolean {
        return tokens.deleteOne(Token::id eq token.id).wasAcknowledged()
    }
}