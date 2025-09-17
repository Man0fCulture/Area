package com.area.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class AboutResponse(
    val client: Client,
    val server: Server
)

@Serializable
data class Client(
    val host: String
)

@Serializable
data class Server(
    val current_time: Long,
    val services: List<Service>
)

@Serializable
data class Service(
    val name: String,
    val description: String,
    val actions: List<Action>,
    val reactions: List<Reaction>
)

@Serializable
data class Action(
    val name: String,
    val description: String
)

@Serializable
data class Reaction(
    val name: String,
    val description: String
)

fun Application.configureMain() {
    routing {
        testDatabaseRoutes()

        get("/about.json") {
            val response = AboutResponse(
                client = Client(
                    host = call.request.local.remoteHost
                ),
                server = Server(
                    current_time = System.currentTimeMillis() / 1000,
                    services = listOf(
                        Service(
                            name = "ktor",
                            description = "Kotlin web framework",
                            actions = listOf(
                                Action(
                                    name = "user_registered",
                                    description = "A new user registers with Ktor"
                                )
                            ),
                            reactions = listOf(
                                Reaction(
                                    name = "save_user",
                                    description = "Save user data with Exposed"
                                )
                            )
                        )
                    )
                )
            )
            call.respond(response)
        }
    }
}