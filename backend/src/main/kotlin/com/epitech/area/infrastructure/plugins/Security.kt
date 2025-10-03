package com.epitech.area.infrastructure.plugins

import com.epitech.area.api.middleware.JwtAuthMiddleware
import com.epitech.area.api.middleware.UserPrincipal
import com.epitech.area.infrastructure.DependencyContainer
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.compression.*

fun Application.configureSecurity() {
    val container = DependencyContainer(this)

    install(Authentication) {
        bearer("auth-jwt") {
            authenticate { tokenCredential ->
                val token = tokenCredential.token
                val result = container.authService.verifyToken(token)
                result.fold(
                    onSuccess = { user -> UserPrincipal(user) },
                    onFailure = { null }
                )
            }
        }
    }
    install(CORS) {
        val allowedHosts = this@configureSecurity.environment.config.property("cors.allowedHosts")
            .getString()
            .split(",")

        allowedHosts.forEach { host ->
            allowHost(host.trim().removePrefix("http://").removePrefix("https://"), schemes = listOf("http", "https"))
        }

        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Options)
        allowCredentials = true
    }

    install(DefaultHeaders) {
        header("X-Engine", "Ktor")
        header("X-Content-Type-Options", "nosniff")
        header("X-Frame-Options", "DENY")
        header("X-XSS-Protection", "1; mode=block")
    }

    install(Compression) {
        gzip {
            priority = 1.0
        }
        deflate {
            priority = 10.0
            minimumSize(1024)
        }
    }
}
