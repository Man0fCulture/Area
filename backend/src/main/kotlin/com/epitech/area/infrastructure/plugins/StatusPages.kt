package com.epitech.area.infrastructure.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.SerializationException
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("StatusPages")

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<SerializationException> { call, cause ->
            logger.error("Serialization error: ${cause.message}", cause)
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Invalid request format", "details" to cause.message)
            )
        }

        exception<IllegalArgumentException> { call, cause ->
            logger.error("Illegal argument: ${cause.message}", cause)
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to cause.message)
            )
        }

        exception<Throwable> { call, cause ->
            logger.error("Unhandled exception: ${cause.message}", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "Internal server error")
            )
        }
    }
}
