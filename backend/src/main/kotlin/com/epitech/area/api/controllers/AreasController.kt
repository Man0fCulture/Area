package com.epitech.area.api.controllers

import com.epitech.area.api.dto.requests.CreateAreaRequest
import com.epitech.area.api.dto.requests.UpdateAreaRequest
import com.epitech.area.api.dto.responses.toResponse
import com.epitech.area.api.middleware.getCurrentUserId
import com.epitech.area.application.services.AreaService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.types.ObjectId

fun Route.areasRoutes(areaService: AreaService) {
    authenticate("auth-jwt") {
        route("/areas") {
            post {
                val request = call.receive<CreateAreaRequest>()
                val userId = call.getCurrentUserId()

                val actionServiceId = try {
                    ObjectId(request.action.serviceId)
                } catch (e: Exception) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid action service ID")
                    )
                }

                val reactions = request.reactions.map { reaction ->
                    try {
                        Triple(
                            ObjectId(reaction.serviceId),
                            reaction.reactionId,
                            reaction.config
                        )
                    } catch (e: Exception) {
                        return@post call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid reaction service ID")
                        )
                    }
                }

                val result = areaService.createArea(
                    userId = userId,
                    name = request.name,
                    description = request.description,
                    actionServiceId = actionServiceId,
                    actionId = request.action.actionId,
                    actionConfig = request.action.config,
                    reactions = reactions
                )

                result.fold(
                    onSuccess = { area ->
                        call.respond(HttpStatusCode.Created, area.toResponse())
                    },
                    onFailure = { error ->
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to (error.message ?: "Failed to create area"))
                        )
                    }
                )
            }

            get {
                val userId = call.getCurrentUserId()
                val areas = areaService.getUserAreas(userId).map { it.toResponse() }
                call.respond(HttpStatusCode.OK, areas)
            }

            get("/{id}") {
                val areaId = try {
                    ObjectId(call.parameters["id"])
                } catch (e: Exception) {
                    return@get call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid area ID")
                    )
                }

                val userId = call.getCurrentUserId()
                val result = areaService.getAreaById(areaId, userId)

                result.fold(
                    onSuccess = { area ->
                        call.respond(HttpStatusCode.OK, area.toResponse())
                    },
                    onFailure = { error ->
                        when (error.message) {
                            "Area not found" -> call.respond(
                                HttpStatusCode.NotFound,
                                mapOf("error" to error.message)
                            )
                            "Unauthorized" -> call.respond(
                                HttpStatusCode.Forbidden,
                                mapOf("error" to error.message)
                            )
                            else -> call.respond(
                                HttpStatusCode.InternalServerError,
                                mapOf("error" to (error.message ?: "Unknown error"))
                            )
                        }
                    }
                )
            }

            patch("/{id}") {
                val areaId = try {
                    ObjectId(call.parameters["id"])
                } catch (e: Exception) {
                    return@patch call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid area ID")
                    )
                }

                val request = call.receive<UpdateAreaRequest>()
                val userId = call.getCurrentUserId()

                val result = areaService.updateArea(
                    areaId = areaId,
                    userId = userId,
                    name = request.name,
                    description = request.description,
                    active = request.active
                )

                result.fold(
                    onSuccess = { area ->
                        call.respond(HttpStatusCode.OK, area.toResponse())
                    },
                    onFailure = { error ->
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to (error.message ?: "Failed to update area"))
                        )
                    }
                )
            }

            delete("/{id}") {
                val areaId = try {
                    ObjectId(call.parameters["id"])
                } catch (e: Exception) {
                    return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid area ID")
                    )
                }

                val userId = call.getCurrentUserId()
                val result = areaService.deleteArea(areaId, userId)

                result.fold(
                    onSuccess = {
                        call.respond(HttpStatusCode.NoContent)
                    },
                    onFailure = { error ->
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to (error.message ?: "Failed to delete area"))
                        )
                    }
                )
            }

            post("/{id}/activate") {
                val areaId = try {
                    ObjectId(call.parameters["id"])
                } catch (e: Exception) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid area ID")
                    )
                }

                val userId = call.getCurrentUserId()
                val result = areaService.activateArea(areaId, userId)

                result.fold(
                    onSuccess = { area ->
                        call.respond(HttpStatusCode.OK, area.toResponse())
                    },
                    onFailure = { error ->
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to (error.message ?: "Failed to activate area"))
                        )
                    }
                )
            }

            post("/{id}/deactivate") {
                val areaId = try {
                    ObjectId(call.parameters["id"])
                } catch (e: Exception) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid area ID")
                    )
                }

                val userId = call.getCurrentUserId()
                val result = areaService.deactivateArea(areaId, userId)

                result.fold(
                    onSuccess = { area ->
                        call.respond(HttpStatusCode.OK, area.toResponse())
                    },
                    onFailure = { error ->
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to (error.message ?: "Failed to deactivate area"))
                        )
                    }
                )
            }

            post("/{id}/test") {
                val areaId = try {
                    ObjectId(call.parameters["id"])
                } catch (e: Exception) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid area ID")
                    )
                }

                val userId = call.getCurrentUserId()
                val result = areaService.testArea(areaId, userId)

                result.fold(
                    onSuccess = { message ->
                        call.respond(HttpStatusCode.OK, mapOf("message" to message))
                    },
                    onFailure = { error ->
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to (error.message ?: "Failed to test area"))
                        )
                    }
                )
            }

            get("/{id}/executions") {
                val areaId = try {
                    ObjectId(call.parameters["id"])
                } catch (e: Exception) {
                    return@get call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid area ID")
                    )
                }

                val userId = call.getCurrentUserId()
                val limit = call.parameters["limit"]?.toIntOrNull() ?: 50

                val result = areaService.getAreaExecutions(areaId, userId, limit)

                result.fold(
                    onSuccess = { executions ->
                        call.respond(HttpStatusCode.OK, executions)
                    },
                    onFailure = { error ->
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to (error.message ?: "Failed to get executions"))
                        )
                    }
                )
            }

            get("/{areaId}/executions/{executionId}") {
                val areaId = try {
                    ObjectId(call.parameters["areaId"])
                } catch (e: Exception) {
                    return@get call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid area ID")
                    )
                }

                val executionId = try {
                    ObjectId(call.parameters["executionId"])
                } catch (e: Exception) {
                    return@get call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid execution ID")
                    )
                }

                val userId = call.getCurrentUserId()
                val result = areaService.getExecutionDetails(areaId, executionId, userId)

                result.fold(
                    onSuccess = { execution ->
                        call.respond(HttpStatusCode.OK, execution)
                    },
                    onFailure = { error ->
                        call.respond(
                            HttpStatusCode.NotFound,
                            mapOf("error" to (error.message ?: "Execution not found"))
                        )
                    }
                )
            }
        }
    }
}
