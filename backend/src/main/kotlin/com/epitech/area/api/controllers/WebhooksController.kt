package com.epitech.area.api.controllers

import com.epitech.area.domain.repositories.AreaRepository
import com.epitech.area.infrastructure.hooks.HookProcessor
import com.epitech.area.infrastructure.hooks.HookRegistry
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.launch
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory

fun Route.webhooksRoutes(
    hookRegistry: HookRegistry,
    hookProcessor: HookProcessor,
    areaRepository: AreaRepository
) {
    val logger = LoggerFactory.getLogger("WebhooksController")

    route("/webhooks") {
        post("/{serviceId}/{hookId}") {
            val serviceId = try {
                ObjectId(call.parameters["serviceId"])
            } catch (e: Exception) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Invalid service ID")
                )
            }

            val hookId = call.parameters["hookId"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing hook ID")
            )

            val webhookUrl = "/api/webhooks/${serviceId}/${hookId}"
            val registration = hookRegistry.findByWebhookUrl(webhookUrl)

            if (registration == null) {
                logger.warn("Webhook not registered: $webhookUrl")
                return@post call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Webhook not found")
                )
            }

            val area = areaRepository.findById(registration.areaId)
            if (area == null) {
                logger.warn("Area not found for webhook: ${registration.areaId}")
                return@post call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Associated area not found")
                )
            }

            if (!area.active) {
                logger.info("Area ${area.id} is inactive, ignoring webhook")
                return@post call.respond(
                    HttpStatusCode.OK,
                    mapOf("message" to "Webhook received but area is inactive")
                )
            }

            val payload = try {
                call.receiveText()
            } catch (e: Exception) {
                ""
            }

            logger.info("Webhook triggered for area ${area.id}: $webhookUrl")

            launch {
                try {
                    val result = hookProcessor.processArea(area)
                    if (result.success) {
                        logger.info("Webhook area ${area.id} executed successfully")
                    } else {
                        logger.error("Webhook area ${area.id} execution failed: ${result.error}")
                    }
                } catch (e: Exception) {
                    logger.error("Error processing webhook area ${area.id}", e)
                }
            }

            call.respond(
                HttpStatusCode.OK,
                mapOf("message" to "Webhook received and processing started")
            )
        }
    }
}
