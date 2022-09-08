package com.bouyahya.plugins

import com.bouyahya.routes.authenticate
import com.bouyahya.routes.getSecretInfo
import com.bouyahya.routes.login
import com.bouyahya.routes.register
import com.bouyahya.service.UserService
import io.ktor.server.routing.*
import io.ktor.server.application.*

fun Application.configureRouting(
    userService: UserService
) {
    val jwtIssuer = environment.config.property("jwt.domain").getString()
    val jwtAudience = environment.config.property("jwt.audience").getString()
    val jwtSecret = environment.config.property("jwt.secret").getString()

    routing {
        authenticate()
        register(userService = userService)
        login(
            userService = userService,
            jwtIssuer = jwtIssuer,
            jwtAudience = jwtAudience,
            jwtSecret = jwtSecret
        )
        getSecretInfo()
    }
}
