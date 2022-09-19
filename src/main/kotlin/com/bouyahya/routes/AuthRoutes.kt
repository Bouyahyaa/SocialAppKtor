package com.bouyahya.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.bouyahya.data.models.Token
import com.bouyahya.data.requests.LoginRequest
import com.bouyahya.data.requests.RegisterRequest
import com.bouyahya.data.requests.ResetPasswordRequest
import com.bouyahya.data.requests.TokenRequest
import com.bouyahya.data.responses.LoginResponse
import com.bouyahya.events.AuthValidationEvent
import com.bouyahya.events.TokenValidationEvent
import com.bouyahya.service.TokenService
import com.bouyahya.service.UserService
import com.bouyahya.util.Constants
import com.bouyahya.util.GmailOperations
import com.bouyahya.util.Response
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
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
                HttpStatusCode.BadRequest, Response(
                    success = false,
                    message = "Bad Request"
                )
            )
            return@post
        }

        when (userService.validateRegistration(request)) {

            is AuthValidationEvent.ErrorFieldEmpty -> {
                call.respond(
                    HttpStatusCode.Conflict, Response(
                        success = false,
                        message = "Required Field is missing"
                    )
                )
                return@post
            }

            is AuthValidationEvent.InvalidEmail -> {
                call.respond(
                    HttpStatusCode.Conflict, Response(
                        success = false,
                        message = "Invalid Email"
                    )
                )
                return@post
            }

            is AuthValidationEvent.IsUserExist -> {
                call.respond(
                    HttpStatusCode.Conflict, Response(
                        success = false,
                        message = "User already register with this email"
                    )
                )
                return@post
            }

            is AuthValidationEvent.UsernameTooShort -> {
                call.respond(
                    HttpStatusCode.Conflict, Response(
                        success = false,
                        message = "Username is too short.."
                    )
                )
                return@post
            }

            is AuthValidationEvent.PasswordTooShort -> {
                call.respond(
                    HttpStatusCode.Conflict, Response(
                        success = false,
                        message = "Password is too short.."
                    )
                )
                return@post
            }

            is AuthValidationEvent.InvalidPassword -> {
                call.respond(
                    HttpStatusCode.Conflict, Response(
                        success = false,
                        message = "The password needs to contain at least one letter and digit"
                    )
                )
                return@post
            }

            is AuthValidationEvent.PasswordsNotMatch -> {
                call.respond(
                    HttpStatusCode.Conflict, Response(
                        success = false,
                        message = "Passwords doesn't match"
                    )
                )
                return@post
            }

            is AuthValidationEvent.Success -> {
                userService.createUser(request)
                val number = tokenService.generateToken()
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
                    HttpStatusCode.OK, Response(
                        success = true,
                        message = "You have successfully registered"
                    )
                )
            }

            else -> {
                call.respond(
                    HttpStatusCode.BadRequest, Response(
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
                    HttpStatusCode.Conflict, LoginResponse(
                        success = false,
                        message = "Required Field is missing"
                    )
                )
                return@post
            }

            is AuthValidationEvent.IsUserExist -> {
                call.respond(
                    HttpStatusCode.Conflict, LoginResponse(
                        success = false,
                        message = "No user is registered with this email"
                    )
                )
                return@post
            }

            is AuthValidationEvent.InvalidPassword -> {
                call.respond(
                    HttpStatusCode.Conflict, LoginResponse(
                        success = false,
                        message = "Incorrect Password"
                    )
                )
                return@post
            }

            is AuthValidationEvent.EmailNotVerified -> {
                call.respond(
                    HttpStatusCode.Conflict, LoginResponse(
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
                    LoginResponse(
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

        when (tokenService.validateConfirmationToken(request, email!!)) {

            is TokenValidationEvent.AlreadyVerified -> {
                call.respond(
                    HttpStatusCode.Conflict, Response(
                        success = false,
                        message = "User Already have confirmed email . You can log in now ."
                    )
                )
                return@post
            }

            is TokenValidationEvent.ErrorFieldEmpty -> {
                call.respond(
                    HttpStatusCode.Conflict, Response(
                        success = false,
                        message = "Token is required to confirm your email"
                    )
                )
                return@post
            }

            is TokenValidationEvent.InvalidToken -> {
                call.respond(
                    HttpStatusCode.Conflict, Response(
                        success = false,
                        message = "Token should have only digits"
                    )
                )
                return@post
            }

            is TokenValidationEvent.TokenTooShort -> {
                call.respond(
                    HttpStatusCode.Conflict, Response(
                        success = false,
                        message = "Token must be exactly 5 digits"
                    )
                )
                return@post
            }

            is TokenValidationEvent.TokensNotMatch -> {
                call.respond(
                    HttpStatusCode.Conflict, Response(
                        success = false,
                        message = "That's not the right token"
                    )
                )
                return@post
            }

            is TokenValidationEvent.Success -> {
                userService.confirmEmailUser(email)
                call.respond(
                    HttpStatusCode.OK,
                    Response(
                        success = true,
                        message = "You have successfully verified your email"
                    )
                )
            }
        }
    }
}

fun Route.resendCode(
    userService: UserService,
    tokenService: TokenService
) {
    post("/api/users/resendCode/{email}") {
        val email = call.parameters["email"]
        val user = userService.getUserByEmail(email!!)

        if (user == null) {
            call.respond(
                HttpStatusCode.Conflict,
                Response(
                    success = false,
                    message = "No user found with this email"
                )
            )
        }

        val number = tokenService.generateToken()
        val subjectEmail = " Resend Code Verification "
        val bodyTextEmail = "Hello " +
                user?.username +
                ",\n\n" +
                "Your confirmation code to access Social T Network App is : \n" +
                "$number" +
                "\n\nThank you ! \n"
        GmailOperations().sendEmail(user?.email!!, subjectEmail, bodyTextEmail)
        val token = tokenService.getToken(user.id, Constants.EMAIL_CODE)
        if (token != null) {
            tokenService.deleteToken(token)
        }
        tokenService.createToken(
            Token(
                userId = user.id,
                type = Constants.EMAIL_CODE,
                code = number.toString()
            )
        )

        call.respond(
            HttpStatusCode.OK,
            Response(
                success = true,
                message = "New Token is generated . Please check your email"
            )
        )
    }
}

fun Route.forgetPassword(
    userService: UserService,
    tokenService: TokenService
) {
    post("/api/users/forgetPassword/{email}") {
        val email = call.parameters["email"]
        val user = userService.getUserByEmail(email!!)

        if (user == null) {
            call.respond(
                HttpStatusCode.Conflict,
                Response(
                    success = false,
                    message = "No user found with this email"
                )
            )
        }

        val number = tokenService.generateToken()
        val subjectEmail = " Reset Password Code "
        val bodyTextEmail = "Hello " +
                user?.username +
                ",\n\n" +
                "Your code to reset password and jump into Social T Network App is : \n" +
                "$number" +
                "\n\nThank you ! \n"
        GmailOperations().sendEmail(user?.email!!, subjectEmail, bodyTextEmail)

        val token = tokenService.getToken(user.id, Constants.PASSWORD_CODE)
        if (token != null) {
            tokenService.deleteToken(token)
        }
        tokenService.createToken(
            Token(
                userId = user.id,
                type = Constants.PASSWORD_CODE,
                code = number.toString()
            )
        )

        call.respond(
            HttpStatusCode.OK,
            Response(
                success = true,
                message = "Token is generated to reset password . Please check your email"
            )
        )
    }
}

fun Route.verifyTokenPassword(
    tokenService: TokenService
) {
    post("/api/users/verifyTokenPassword/{email}") {
        val request = call.receiveOrNull<TokenRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val email = call.parameters["email"]

        when (tokenService.validateResetPasswordToken(request, email!!)) {

            is TokenValidationEvent.ErrorFieldEmpty -> {
                call.respond(
                    HttpStatusCode.Conflict, Response(
                        success = false,
                        message = "Token is required to reset your password"
                    )
                )
                return@post
            }

            is TokenValidationEvent.InvalidToken -> {
                call.respond(
                    HttpStatusCode.Conflict, Response(
                        success = false,
                        message = "Token should have only digits"
                    )
                )
                return@post
            }

            is TokenValidationEvent.TokenTooShort -> {
                call.respond(
                    HttpStatusCode.Conflict, Response(
                        success = false,
                        message = "Token must be exactly 5 digits"
                    )
                )
                return@post
            }

            is TokenValidationEvent.TokensNotMatch -> {
                call.respond(
                    HttpStatusCode.Conflict, Response(
                        success = false,
                        message = "That's not the right token"
                    )
                )
                return@post
            }

            is TokenValidationEvent.Success -> {
                call.respond(
                    HttpStatusCode.OK,
                    Response(
                        success = true,
                        message = "You have the permission now to reset your password"
                    )
                )
            }

            else -> {
                call.respond(HttpStatusCode.BadRequest, "Something Wrong")
            }
        }
    }
}

fun Route.resetPassword(
    userService: UserService
) {
    post("/api/users/resetPassword/{email}") {
        val request = call.receiveOrNull<ResetPasswordRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val email = call.parameters["email"]

        when (userService.validateResetPassword(request)) {
            is AuthValidationEvent.ErrorFieldEmpty -> {
                call.respond(
                    HttpStatusCode.Conflict, Response(
                        success = false,
                        message = "Required Field is missing"
                    )
                )
                return@post
            }

            is AuthValidationEvent.PasswordTooShort -> {
                call.respond(
                    HttpStatusCode.Conflict, Response(
                        success = false,
                        message = "Password is too short.."
                    )
                )
                return@post
            }

            is AuthValidationEvent.InvalidPassword -> {
                call.respond(
                    HttpStatusCode.Conflict, Response(
                        success = false,
                        message = "The password needs to contain at least one letter and digit"
                    )
                )
                return@post
            }

            is AuthValidationEvent.PasswordsNotMatch -> {
                call.respond(
                    HttpStatusCode.Conflict, Response(
                        success = false,
                        message = "Passwords doesn't match"
                    )
                )
                return@post
            }

            is AuthValidationEvent.Success -> {
                userService.resetPassword(request, email!!)
                call.respond(
                    HttpStatusCode.OK,
                    Response(
                        success = true,
                        message = "You have successfully reset your password"
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