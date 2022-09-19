package com.bouyahya.plugins

import com.bouyahya.routes.*
import com.bouyahya.service.TokenService
import com.bouyahya.service.UserService
import io.ktor.server.routing.*
import io.ktor.server.application.*

fun Application.configureRouting(
    userService: UserService,
    tokenService: TokenService
) {
    val jwtIssuer = environment.config.property("jwt.domain").getString()
    val jwtAudience = environment.config.property("jwt.audience").getString()
    val jwtSecret = environment.config.property("jwt.secret").getString()

    routing {
        authenticate()

        register(
            userService = userService,
            tokenService = tokenService
        )

        login(
            userService = userService,
            jwtIssuer = jwtIssuer,
            jwtAudience = jwtAudience,
            jwtSecret = jwtSecret
        )

        confirmation(
            tokenService = tokenService,
            userService = userService
        )

        resendCode(
            userService = userService,
            tokenService = tokenService
        )

        forgetPassword(
            userService = userService,
            tokenService = tokenService
        )

        verifyTokenPassword(
            tokenService = tokenService
        )

        resetPassword(
            userService = userService
        )
    }
}