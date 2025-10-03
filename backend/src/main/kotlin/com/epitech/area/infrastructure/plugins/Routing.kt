package com.epitech.area.infrastructure.plugins

import com.epitech.area.api.controllers.areasRoutes
import com.epitech.area.api.controllers.authRoutes
import com.epitech.area.api.controllers.servicesRoutes
import com.epitech.area.api.controllers.webhooksRoutes
import com.epitech.area.infrastructure.DependencyContainer
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(val status: String, val timestamp: Long)

@Serializable
data class AboutResponse(
    val client: ClientInfo,
    val server: ServerInfo
)

@Serializable
data class ClientInfo(val host: String)

@Serializable
data class ServerInfo(
    val current_time: Long,
    val services: List<ServiceInfo>
)

@Serializable
data class ServiceInfo(
    val name: String,
    val actions: List<ActionInfo>,
    val reactions: List<ReactionInfo>
)

@Serializable
data class ActionInfo(
    val name: String,
    val description: String
)

@Serializable
data class ReactionInfo(
    val name: String,
    val description: String
)

fun Application.configureRouting() {
    val container = DependencyContainer(this)

    routing {
        get("/health") {
            call.respond(
                HttpStatusCode.OK,
                HealthResponse(status = "OK", timestamp = System.currentTimeMillis())
            )
        }

        get("/about.json") {
            val services = try {
                container.serviceRepository.findAllEnabled().map { service ->
                    ServiceInfo(
                        name = service.name,
                        actions = service.actions.map { action ->
                            ActionInfo(name = action.name, description = action.description)
                        },
                        reactions = service.reactions.map { reaction ->
                            ReactionInfo(name = reaction.name, description = reaction.description)
                        }
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }

            call.respond(
                HttpStatusCode.OK,
                AboutResponse(
                    client = ClientInfo(host = call.request.local.remoteHost),
                    server = ServerInfo(
                        current_time = System.currentTimeMillis() / 1000,
                        services = services
                    )
                )
            )
        }

        route("/api") {
            authRoutes(container.authService)
            servicesRoutes(container.serviceRepository)
            areasRoutes(container.areaService)
            webhooksRoutes(
                hookRegistry = container.hookRegistry,
                hookProcessor = container.hookProcessor,
                areaRepository = container.areaRepository
            )
        }
    }
}
