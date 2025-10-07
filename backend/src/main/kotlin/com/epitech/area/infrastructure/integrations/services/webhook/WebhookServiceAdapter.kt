package com.epitech.area.infrastructure.integrations.services.webhook

import com.epitech.area.domain.entities.UserService
import com.epitech.area.infrastructure.integrations.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.bson.Document

class WebhookServiceAdapter : ServiceAdapter {
    override val serviceId = "webhook"
    override val serviceName = "Webhook"

    private val httpClient = HttpClient(CIO) {
        engine {
            requestTimeout = 30000
        }
    }

    override suspend fun executeAction(
        actionId: String,
        config: Map<String, Any>,
        userService: UserService?
    ): ActionResult {
        return when (actionId) {
            "webhook_triggered" -> executeWebhookTriggered(config)
            else -> ActionResult(false, error = "Unknown action: $actionId")
        }
    }

    private suspend fun executeWebhookTriggered(config: Map<String, Any>): ActionResult {
        return ActionResult(
            success = true,
            data = Document(config).apply {
                append("triggered_at", System.currentTimeMillis())
                append("webhook_id", config["webhook_id"] ?: "unknown")
            }
        )
    }

    override suspend fun executeReaction(
        reactionId: String,
        config: Map<String, Any>,
        actionData: Document,
        userService: UserService?
    ): ReactionResult {
        return when (reactionId) {
            "call_webhook" -> executeCallWebhook(config, actionData)
            else -> ReactionResult(false, error = "Unknown reaction: $reactionId")
        }
    }

    private suspend fun executeCallWebhook(
        config: Map<String, Any>,
        actionData: Document
    ): ReactionResult {
        val url = config["url"] as? String ?: return ReactionResult(
            false,
            error = "Missing 'url' parameter"
        )

        val method = (config["method"] as? String ?: "POST").uppercase()
        val headers = config["headers"] as? Map<String, String> ?: emptyMap()
        val body = config["body"] as? String

        return try {
            val response = when (method) {
                "GET" -> httpClient.get(url) {
                    headers.forEach { (key, value) -> header(key, value) }
                }
                "POST" -> httpClient.post(url) {
                    headers.forEach { (key, value) -> header(key, value) }
                    contentType(ContentType.Application.Json)
                    setBody(body ?: actionData)
                }
                "PUT" -> httpClient.put(url) {
                    headers.forEach { (key, value) -> header(key, value) }
                    contentType(ContentType.Application.Json)
                    setBody(body ?: actionData)
                }
                else -> return ReactionResult(false, error = "Unsupported HTTP method: $method")
            }

            if (response.status.isSuccess()) {
                ReactionResult(
                    success = true,
                    message = "Webhook called successfully: ${response.status}"
                )
            } else {
                ReactionResult(
                    false,
                    error = "Webhook call failed with status: ${response.status}"
                )
            }
        } catch (e: Exception) {
            ReactionResult(false, error = "Webhook call failed: ${e.message}")
        }
    }

    override suspend fun validateActionConfig(actionId: String, config: Map<String, Any>): ValidationResult {
        return when (actionId) {
            "webhook_triggered" -> ValidationResult(true)
            else -> ValidationResult(false, listOf("Unknown action: $actionId"))
        }
    }

    override suspend fun validateReactionConfig(reactionId: String, config: Map<String, Any>): ValidationResult {
        return when (reactionId) {
            "call_webhook" -> {
                val url = config["url"] as? String
                if (url.isNullOrBlank()) {
                    ValidationResult(false, listOf("'url' is required"))
                } else if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    ValidationResult(false, listOf("'url' must start with http:// or https://"))
                } else {
                    ValidationResult(true)
                }
            }
            else -> ValidationResult(false, listOf("Unknown reaction: $reactionId"))
        }
    }
}
