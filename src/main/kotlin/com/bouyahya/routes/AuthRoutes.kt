package com.bouyahya.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.bouyahya.data.requests.LoginRequest
import com.bouyahya.data.requests.RegisterRequest
import com.bouyahya.data.responses.AuthResponse
import com.bouyahya.events.AuthValidationEvent
import com.bouyahya.routes.authenticate
import com.bouyahya.service.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.register(
    userService: UserService
) {
    post("/api/users/register") {
        val request = call.receiveOrNull<RegisterRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        when (userService.validateRegistration(request)) {

            is AuthValidationEvent.ErrorFieldEmpty -> {
                call.respond(HttpStatusCode.Conflict, "Required Field is missing")
                return@post
            }

            is AuthValidationEvent.InvalidEmail -> {
                call.respond(HttpStatusCode.Conflict, "Invalid Email")
                return@post
            }

            is AuthValidationEvent.IsUserExist -> {
                call.respond(HttpStatusCode.Conflict, "User already register with this email")
                return@post
            }

            is AuthValidationEvent.UsernameTooShort -> {
                call.respond(HttpStatusCode.Conflict, "Username is too short..")
                return@post
            }

            is AuthValidationEvent.PasswordTooShort -> {
                call.respond(HttpStatusCode.Conflict, "Password is too short..")
                return@post
            }

            is AuthValidationEvent.PasswordsNotMatch -> {
                call.respond(HttpStatusCode.Conflict, "Passwords doesn't match")
                return@post
            }

            is AuthValidationEvent.Success -> {
                userService.createUser(request)
                call.respond(HttpStatusCode.OK, "You have successfully registered")
            }

            else -> {
                call.respond(HttpStatusCode.BadRequest, "Something Wrong")
            }
        }
    }

}

fun Route.login(
    userService: UserService,
    jwtIssuer: String,
    jwtAudience: String,
    jwtSecret: String
) {
    post("/api/users/login") {
        val request = call.receiveOrNull<LoginRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        when (userService.validateLogin(request)) {
            is AuthValidationEvent.ErrorFieldEmpty -> {
                call.respond(HttpStatusCode.Conflict, "Required Field is missing")
                return@post
            }

            is AuthValidationEvent.IsUserExist -> {
                call.respond(HttpStatusCode.Conflict, "No user is registered with this email")
                return@post
            }

            is AuthValidationEvent.InvalidPassword -> {
                call.respond(HttpStatusCode.Conflict, "Incorrect Password")
                return@post
            }

            is AuthValidationEvent.Success -> {
                val expiresIn = 1000L * 60L * 60L * 24L * 365L
                val user = userService.getUserByEmail(request.email)
                val token = JWT.create()
                    .withClaim("userId", user?.id)
                    .withIssuer(jwtIssuer)
                    .withExpiresAt(Date(System.currentTimeMillis() + expiresIn))
                    .withAudience(jwtAudience)
                    .sign(Algorithm.HMAC256(jwtSecret))
                call.respond(
                    HttpStatusCode.OK,
                    AuthResponse(
                        userId = user?.id!!,
                        token = token
                    )
                )
            }

            else -> {
                call.respond(HttpStatusCode.BadRequest, "Something Wrong")
            }
        }
    }
}

fun Route.authenticate() {
    authenticate {
        get("/api/users/authenticate") {
            call.respond(HttpStatusCode.OK)
        }
    }
}

fun Route.getSecretInfo() {
    authenticate {
        get("/api/users/secret") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)
            call.respond(HttpStatusCode.OK, "Your userId is $userId")
        }
    }
}