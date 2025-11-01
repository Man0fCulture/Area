package com.epitech.area.integrations.webhook

import com.epitech.area.domain.entities.UserService
import com.epitech.area.sdk.*
import io.ktor.http.*
import org.bson.Document

/**
 * Service Webhook - Intégration HTTP générique
 *
 * Triggers:
 * - webhook_triggered: Déclenché quand l'URL webhook est appelée
 *
 * Actions:
 * - call_webhook: Appelle une URL externe
 */
class WebhookService : ServiceDefinition() {
    override val id = "webhook"
    override val name = "Webhook"
    override val description = "Trigger actions via webhooks or call external webhooks"
    override val category = "Integration"
    override val requiresAuth = false

    override suspend fun executeTrigger(
        triggerId: String,
        config: Map<String, String>,
        userService: UserService?
    ): TriggerResult {
        return when (triggerId) {
            "webhook_triggered" -> {
                // Le trigger webhook est géré par le controller WebhooksController
                // Ici on retourne juste success (les données sont dans triggerData)
                TriggerResult.success(
                    "triggered_at" to System.currentTimeMillis(),
                    "message" to "Webhook received"
                )
            }
            else -> TriggerResult.error("Unknown trigger: $triggerId")
        }
    }

    override suspend fun executeAction(
        actionId: String,
        config: Map<String, String>,
        triggerData: Document,
        userService: UserService?
    ): ActionResult {
        return when (actionId) {
            "call_webhook" -> handleCallWebhook(config, triggerData)
            else -> ActionResult.error("Unknown action: $actionId")
        }
    }

    private suspend fun handleCallWebhook(
        config: Map<String, String>,
        triggerData: Document
    ): ActionResult {
        val url = config["url"]
            ?: return ActionResult.error("Missing 'url' parameter")

        val method = config["method"] ?: "POST"
        val body = config["body"]

        return try {
            val httpMethod = when (method.uppercase()) {
                "GET" -> HttpMethod.Get
                "POST" -> HttpMethod.Post
                "PUT" -> HttpMethod.Put
                else -> return ActionResult.error("Invalid method: $method")
            }

            val response = runtime.request(
                url = url,
                method = httpMethod,
                body = body
            )

            runtime.log("Webhook called: $method $url -> ${response.status}")
            ActionResult.success("Webhook called successfully: ${response.status}")

        } catch (e: Exception) {
            runtime.log("Failed to call webhook: ${e.message}", LogLevel.ERROR)
            ActionResult.error("Failed to call webhook: ${e.message}")
        }
    }

    override suspend fun validateActionConfig(
        actionId: String,
        config: Map<String, String>
    ): ValidationResult {
        return when (actionId) {
            "call_webhook" -> {
                val url = config["url"]
                val method = config["method"] ?: "POST"

                val errors = mutableListOf<String>()
                if (url.isNullOrBlank()) {
                    errors.add("'url' is required")
                }
                if (method.uppercase() !in listOf("GET", "POST", "PUT")) {
                    errors.add("'method' must be GET, POST, or PUT")
                }

                if (errors.isEmpty()) ValidationResult(true)
                else ValidationResult(false, errors)
            }
            else -> ValidationResult(false, "Unknown action: $actionId")
        }
    }
}
