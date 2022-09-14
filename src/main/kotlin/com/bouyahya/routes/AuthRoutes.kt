package com.bouyahya.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.bouyahya.data.models.Token
import com.bouyahya.data.requests.LoginRequest
import com.bouyahya.data.requests.RegisterRequest
import com.bouyahya.data.requests.TokenRequest
import com.bouyahya.data.responses.AuthResponse
import com.bouyahya.data.responses.RegisterResponse
import com.bouyahya.data.responses.TokenResponse
import com.bouyahya.events.AuthValidationEvent
import com.bouyahya.events.TokenValidationEvent
import com.bouyahya.service.TokenService
import com.bouyahya.service.UserService
import com.bouyahya.util.Constants
import com.bouyahya.util.GmailOperations
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.register(
    userService: UserService,
    tokenService: TokenService
) {
    post("/api/users/register") {
        val request = call.receiveOrNull<RegisterRequest>() ?: kotlin.run {
            call.respond(
                HttpStatusCode.BadRequest, RegisterResponse(
                    success = false,
                    message = "Bad Request"
                )
            )
            return@post
        }

        when (userService.validateRegistration(request)) {

            is AuthValidationEvent.ErrorFieldEmpty -> {
                call.respond(
                    HttpStatusCode.Conflict, RegisterResponse(
                        success = false,
                        message = "Required Field is missing"
                    )
                )
                return@post
            }

            is AuthValidationEvent.InvalidEmail -> {
                call.respond(
                    HttpStatusCode.Conflict, RegisterResponse(
                        success = false,
                        message = "Invalid Email"
                    )
                )
                return@post
            }

            is AuthValidationEvent.IsUserExist -> {
                call.respond(
                    HttpStatusCode.Conflict, RegisterResponse(
                        success = false,
                        message = "User already register with this email"
                    )
                )
                return@post
            }

            is AuthValidationEvent.UsernameTooShort -> {
                call.respond(
                    HttpStatusCode.Conflict, RegisterResponse(
                        success = false,
                        message = "Username is too short.."
                    )
                )
                return@post
            }

            is AuthValidationEvent.PasswordTooShort -> {
                call.respond(
                    HttpStatusCode.Conflict, RegisterResponse(
                        success = false,
                        message = "Password is too short.."
                    )
                )
                return@post
            }

            is AuthValidationEvent.PasswordsNotMatch -> {
                call.respond(
                    HttpStatusCode.Conflict, RegisterResponse(
                        success = false,
                        message = "Passwords doesn't match"
                    )
                )
                return@post
            }

            is AuthValidationEvent.Success -> {
                userService.createUser(request)
                val lowerLimit = 12345L
                val upperLimit = 23456L
                val r = Random()
                val number = lowerLimit + (r.nextDouble() * (upperLimit - lowerLimit)).toLong()
                val subjectEmail = " Account Verification "
                val bodyTextEmail = "Hello " +
                        request.username +
                        ",\n\n" +
                        "Your confirmation code to access Social T Network App is : \n" +
                        "$number" +
                        "\n\nThank you ! \n"
                GmailOperations().sendEmail(request.email, subjectEmail, bodyTextEmail)
                val userId = userService.getUserByEmail(request.email)?.id!!
                val token = tokenService.getToken(userId, Constants.EMAIL_CODE)
                if (token != null) {
                    tokenService.deleteToken(token)
                }
                tokenService.createToken(
                    Token(
                        userId = userId,
                        type = Constants.EMAIL_CODE,
                        code = number.toString()
                    )
                )


                call.respond(
                    HttpStatusCode.OK, RegisterResponse(
                        success = true,
                        message = "You have successfully registered"
                    )
                )
            }

            else -> {
                call.respond(
                    HttpStatusCode.BadRequest, RegisterResponse(
                        success = true,
                        message = "Something Wrong"
                    )
                )
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
                call.respond(
                    HttpStatusCode.Conflict, AuthResponse(
                        success = false,
                        message = "Required Field is missing"
                    )
                )
                return@post
            }

            is AuthValidationEvent.IsUserExist -> {
                call.respond(
                    HttpStatusCode.Conflict, AuthResponse(
                        success = false,
                        message = "No user is registered with this email"
                    )
                )
                return@post
            }

            is AuthValidationEvent.InvalidPassword -> {
                call.respond(
                    HttpStatusCode.Conflict, AuthResponse(
                        success = false,
                        message = "Incorrect Password"
                    )
                )
                return@post
            }

            is AuthValidationEvent.EmailNotVerified -> {
                call.respond(
                    HttpStatusCode.Conflict, AuthResponse(
                        success = false,
                        message = "Email Not Verified . Please check your email"
                    )
                )
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
                        token = token,
                        success = true,
                        message = "You have successfully logged in"
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

fun Route.confirmation(
    tokenService: TokenService,
    userService: UserService
) {
    post("/api/users/confirmation/{email}") {
        val request = call.receiveOrNull<TokenRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val email = call.parameters["email"]

        when (tokenService.validateToken(request, email!!)) {

            is TokenValidationEvent.AlreadyVerified -> {
                call.respond(
                    HttpStatusCode.Conflict, TokenResponse(
                        success = false,
                        message = "User Already have confirmed email . You can log in now ."
                    )
                )
                return@post
            }

            is TokenValidationEvent.ErrorFieldEmpty -> {
                call.respond(
                    HttpStatusCode.Conflict, TokenResponse(
                        success = false,
                        message = "Token is required to confirm your email"
                    )
                )
                return@post
            }

            is TokenValidationEvent.InvalidToken -> {
                call.respond(
                    HttpStatusCode.Conflict, TokenResponse(
                        success = false,
                        message = "Token should have only digits"
                    )
                )
                return@post
            }

            is TokenValidationEvent.TokenTooShort -> {
                call.respond(
                    HttpStatusCode.Conflict, TokenResponse(
                        success = false,
                        message = "Token must be exactly 5 digits"
                    )
                )
                return@post
            }

            is TokenValidationEvent.TokensNotMatch -> {
                call.respond(
                    HttpStatusCode.Conflict, TokenResponse(
                        success = false,
                        message = "That's not the right token"
                    )
                )
                return@post
            }

            is TokenValidationEvent.Success -> {
                userService.updateUser(email)
                call.respond(
                    HttpStatusCode.OK,
                    TokenResponse(
                        success = true,
                        message = "You have successfully verified your email"
                    )
                )
            }
        }
    }
}