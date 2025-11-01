package com.epitech.area.api.controllers

import com.epitech.area.api.dto.responses.toResponse
import com.epitech.area.domain.repositories.ServiceRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.servicesRoutes(serviceRepository: ServiceRepository) {
    route("/services") {
        get {
            val services = serviceRepository.findAllEnabled().map { it.toResponse() }
            call.respond(HttpStatusCode.OK, services)
        }

        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing service ID")
            )

            val service = try {
                serviceRepository.findById(org.bson.types.ObjectId(id))
            } catch (e: Exception) {
                return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Invalid service ID")
                )
            }

            if (service == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Service not found"))
            } else {
                call.respond(HttpStatusCode.OK, service.toResponse())
            }
        }
    }
}
