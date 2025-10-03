package com.epitech.area.infrastructure

import com.epitech.area.domain.entities.*
import com.epitech.area.domain.repositories.ServiceRepository
import org.slf4j.LoggerFactory

class ServiceInitializer(private val serviceRepository: ServiceRepository) {
    private val logger = LoggerFactory.getLogger(ServiceInitializer::class.java)

    suspend fun initializeServices() {
        logger.info("Initializing services in database...")

        val services = listOf(
            createTimerService(),
            createWebhookService(),
            createGmailService()
        )

        services.forEach { service ->
            val existing = serviceRepository.findByName(service.name)
            if (existing == null) {
                serviceRepository.create(service)
                logger.info("Created service: ${service.name}")
            } else {
                serviceRepository.update(service.copy(id = existing.id))
                logger.info("Updated service: ${service.name}")
            }
        }

        logger.info("Services initialization completed")
    }

    private fun createTimerService(): Service {
        return Service(
            name = "Timer",
            description = "Schedule actions based on time",
            category = "Productivity",
            iconUrl = null,
            requiresAuth = false,
            authType = null,
            actions = listOf(
                ActionDefinition(
                    id = "every_x_seconds",
                    name = "Every X seconds",
                    description = "Trigger every X seconds",
                    triggerType = TriggerType.SCHEDULE,
                    configSchema = mapOf(
                        "interval" to FieldSchema(
                            type = "number",
                            required = true,
                            description = "Interval in seconds"
                        )
                    )
                ),
                ActionDefinition(
                    id = "at_time",
                    name = "At specific time",
                    description = "Trigger at specific time each day",
                    triggerType = TriggerType.SCHEDULE,
                    configSchema = mapOf(
                        "time" to FieldSchema(
                            type = "string",
                            required = true,
                            description = "Time in HH:mm format"
                        )
                    )
                )
            ),
            reactions = listOf(
                ReactionDefinition(
                    id = "wait",
                    name = "Wait X seconds",
                    description = "Wait for X seconds before continuing",
                    configSchema = mapOf(
                        "seconds" to FieldSchema(
                            type = "number",
                            required = true,
                            description = "Number of seconds to wait (max 300)"
                        )
                    )
                )
            ),
            enabled = true
        )
    }

    private fun createWebhookService(): Service {
        return Service(
            name = "Webhook",
            description = "Trigger actions via webhooks or call external webhooks",
            category = "Integration",
            iconUrl = null,
            requiresAuth = false,
            authType = null,
            actions = listOf(
                ActionDefinition(
                    id = "webhook_triggered",
                    name = "Webhook received",
                    description = "Trigger when webhook URL is called",
                    triggerType = TriggerType.WEBHOOK,
                    configSchema = emptyMap()
                )
            ),
            reactions = listOf(
                ReactionDefinition(
                    id = "call_webhook",
                    name = "Call webhook",
                    description = "Call an external webhook URL",
                    configSchema = mapOf(
                        "url" to FieldSchema(
                            type = "string",
                            required = true,
                            description = "Webhook URL to call"
                        ),
                        "method" to FieldSchema(
                            type = "string",
                            required = false,
                            default = "POST",
                            options = listOf("GET", "POST", "PUT"),
                            description = "HTTP method"
                        ),
                        "body" to FieldSchema(
                            type = "string",
                            required = false,
                            description = "Request body (JSON)"
                        )
                    )
                )
            ),
            enabled = true
        )
    }

    private fun createGmailService(): Service {
        return Service(
            name = "Gmail",
            description = "Trigger on new emails and send emails",
            category = "Email",
            iconUrl = null,
            requiresAuth = true,
            authType = "OAuth2",
            actions = listOf(
                ActionDefinition(
                    id = "new_email",
                    name = "New email received",
                    description = "Trigger when a new email is received",
                    triggerType = TriggerType.POLLING,
                    configSchema = emptyMap()
                ),
                ActionDefinition(
                    id = "email_with_subject",
                    name = "Email with subject",
                    description = "Trigger when email with specific subject is received",
                    triggerType = TriggerType.POLLING,
                    configSchema = mapOf(
                        "subject" to FieldSchema(
                            type = "string",
                            required = true,
                            description = "Subject to match (case-insensitive)"
                        )
                    )
                )
            ),
            reactions = listOf(
                ReactionDefinition(
                    id = "send_email",
                    name = "Send email",
                    description = "Send an email",
                    configSchema = mapOf(
                        "to" to FieldSchema(
                            type = "string",
                            required = true,
                            description = "Recipient email address"
                        ),
                        "subject" to FieldSchema(
                            type = "string",
                            required = true,
                            description = "Email subject"
                        ),
                        "body" to FieldSchema(
                            type = "string",
                            required = true,
                            description = "Email body"
                        )
                    )
                ),
                ReactionDefinition(
                    id = "reply_email",
                    name = "Reply to email",
                    description = "Reply to the triggering email",
                    configSchema = mapOf(
                        "body" to FieldSchema(
                            type = "string",
                            required = true,
                            description = "Reply body"
                        )
                    )
                )
            ),
            enabled = true
        )
    }
}
