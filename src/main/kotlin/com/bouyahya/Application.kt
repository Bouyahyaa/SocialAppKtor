package com.bouyahya

import com.bouyahya.feature_auth.data.repository.token.TokenRepositoryImpl
import com.bouyahya.feature_auth.data.repository.user.UserRepositoryImpl
import io.ktor.server.application.*
import com.bouyahya.plugins.*
import com.bouyahya.feature_auth.service.TokenService
import com.bouyahya.feature_auth.service.UserService
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {

    val mongoPassword = System.getenv("MONGO_PW")
    val databaseName = "SocialAppDB"
    val db = KMongo.createClient(
        connectionString = "mongodb+srv://bilelJs:$mongoPassword@cluster0.0dt5b.mongodb.net/$databaseName?retryWrites=true&w=majority"
    ).coroutine.getDatabase(databaseName)

    val userDataSource = UserRepositoryImpl(db)
    val userService = UserService(userDataSource)

    val tokenDataSource = TokenRepositoryImpl(db)
    val tokenService = TokenService(tokenDataSource, userDataSource)

    configureSerialization()
    configureMonitoring()
    configureSecurity()
    configureRouting(userService, tokenService)
}