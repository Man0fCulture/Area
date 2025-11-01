package com.epitech.area.infrastructure

import com.epitech.area.domain.entities.*
import com.epitech.area.domain.repositories.ServiceRepository
import com.epitech.area.sdk.ServiceRegistry
import org.slf4j.LoggerFactory

class ServiceInitializer(
    private val serviceRepository: ServiceRepository,
    private val serviceRegistry: ServiceRegistry
) {
    private val logger = LoggerFactory.getLogger(ServiceInitializer::class.java)

    suspend fun initializeServices() {
        logger.info("Initializing services in database from ServiceRegistry...")

        // Services Kotlin hardcodés
        val kotlinServices = listOf(
            createTimerService(),
            createWebhookService(),
            createGmailService(),
            createRandomService(),
            createTextService(),
            createMathService(),
            createLoggerService()
        )

        // Services YAML dynamiques
        val yamlServices = serviceRegistry.getAll()
            .filterIsInstance<com.epitech.area.sdk.yaml.YamlBasedService>()
            .map { yamlService ->
                com.epitech.area.sdk.yaml.YamlToEntityConverter.toServiceEntity(yamlService.getConfig())
            }

        val allServices = kotlinServices + yamlServices

        allServices.forEach { service ->
            val existing = serviceRepository.findByName(service.name)
            if (existing == null) {
                serviceRepository.create(service)
                logger.info("✓ Created service: ${service.name}")
            } else {
                serviceRepository.update(service.copy(id = existing.id))
                logger.info("✓ Updated service: ${service.name}")
            }
        }

        logger.info("✓ Services initialization completed (${allServices.size} services: ${kotlinServices.size} Kotlin + ${yamlServices.size} YAML)")
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

    private fun createRandomService(): Service {
        return Service(
            name = "Random",
            description = "Generate random values and make random choices",
            category = "Utility",
            requiresAuth = false,
            authType = null,
            actions = listOf(
                ActionDefinition(
                    id = "random_chance",
                    name = "Random chance",
                    description = "Trigger randomly based on percentage",
                    triggerType = TriggerType.EVENT,
                    configSchema = mapOf(
                        "percentage" to FieldSchema(
                            type = "number",
                            required = true,
                            default = "50",
                            description = "Chance of triggering (0-100%)"
                        )
                    )
                )
            ),
            reactions = listOf(
                ReactionDefinition(
                    id = "generate_number",
                    name = "Generate number",
                    description = "Generate a random number in range",
                    configSchema = mapOf(
                        "min" to FieldSchema(type = "number", required = false, default = "0"),
                        "max" to FieldSchema(type = "number", required = false, default = "100")
                    )
                ),
                ReactionDefinition(
                    id = "choose_from_list",
                    name = "Choose from list",
                    description = "Pick a random item from comma-separated list",
                    configSchema = mapOf(
                        "items" to FieldSchema(
                            type = "string",
                            required = true,
                            description = "Comma-separated items"
                        )
                    )
                ),
                ReactionDefinition(
                    id = "generate_uuid",
                    name = "Generate UUID",
                    description = "Generate a random UUID",
                    configSchema = emptyMap()
                )
            ),
            enabled = true
        )
    }

    private fun createTextService(): Service {
        return Service(
            name = "Text",
            description = "Text manipulation and formatting",
            category = "Utility",
            requiresAuth = false,
            authType = null,
            actions = emptyList(),
            reactions = listOf(
                ReactionDefinition(
                    id = "to_uppercase",
                    name = "To uppercase",
                    description = "Convert text to uppercase",
                    configSchema = mapOf(
                        "text" to FieldSchema(type = "string", required = true)
                    )
                ),
                ReactionDefinition(
                    id = "to_lowercase",
                    name = "To lowercase",
                    description = "Convert text to lowercase",
                    configSchema = mapOf(
                        "text" to FieldSchema(type = "string", required = true)
                    )
                ),
                ReactionDefinition(
                    id = "concat",
                    name = "Concatenate",
                    description = "Join two texts together",
                    configSchema = mapOf(
                        "text1" to FieldSchema(type = "string", required = false),
                        "text2" to FieldSchema(type = "string", required = false),
                        "separator" to FieldSchema(type = "string", required = false)
                    )
                ),
                ReactionDefinition(
                    id = "replace",
                    name = "Replace text",
                    description = "Replace text in a string",
                    configSchema = mapOf(
                        "text" to FieldSchema(type = "string", required = true),
                        "find" to FieldSchema(type = "string", required = true),
                        "replace" to FieldSchema(type = "string", required = false)
                    )
                )
            ),
            enabled = true
        )
    }

    private fun createMathService(): Service {
        return Service(
            name = "Math",
            description = "Mathematical operations and calculations",
            category = "Utility",
            requiresAuth = false,
            authType = null,
            actions = emptyList(),
            reactions = listOf(
                ReactionDefinition(
                    id = "add",
                    name = "Add",
                    description = "Add two numbers",
                    configSchema = mapOf(
                        "a" to FieldSchema(type = "number", required = true),
                        "b" to FieldSchema(type = "number", required = true)
                    )
                ),
                ReactionDefinition(
                    id = "subtract",
                    name = "Subtract",
                    description = "Subtract two numbers",
                    configSchema = mapOf(
                        "a" to FieldSchema(type = "number", required = true),
                        "b" to FieldSchema(type = "number", required = true)
                    )
                ),
                ReactionDefinition(
                    id = "multiply",
                    name = "Multiply",
                    description = "Multiply two numbers",
                    configSchema = mapOf(
                        "a" to FieldSchema(type = "number", required = true),
                        "b" to FieldSchema(type = "number", required = true)
                    )
                ),
                ReactionDefinition(
                    id = "divide",
                    name = "Divide",
                    description = "Divide two numbers",
                    configSchema = mapOf(
                        "a" to FieldSchema(type = "number", required = true),
                        "b" to FieldSchema(type = "number", required = true)
                    )
                ),
                ReactionDefinition(
                    id = "power",
                    name = "Power",
                    description = "Raise number to power",
                    configSchema = mapOf(
                        "base" to FieldSchema(type = "number", required = true),
                        "exponent" to FieldSchema(type = "number", required = true)
                    )
                )
            ),
            enabled = true
        )
    }

    private fun createLoggerService(): Service {
        return Service(
            name = "Logger",
            description = "Log messages for debugging and monitoring",
            category = "Utility",
            requiresAuth = false,
            authType = null,
            actions = emptyList(),
            reactions = listOf(
                ReactionDefinition(
                    id = "log_info",
                    name = "Log info",
                    description = "Log an info message",
                    configSchema = mapOf(
                        "message" to FieldSchema(type = "string", required = true),
                        "include_trigger_data" to FieldSchema(type = "boolean", required = false, default = "false")
                    )
                ),
                ReactionDefinition(
                    id = "log_warn",
                    name = "Log warning",
                    description = "Log a warning message",
                    configSchema = mapOf(
                        "message" to FieldSchema(type = "string", required = true)
                    )
                ),
                ReactionDefinition(
                    id = "log_error",
                    name = "Log error",
                    description = "Log an error message",
                    configSchema = mapOf(
                        "message" to FieldSchema(type = "string", required = true)
                    )
                )
            ),
            enabled = true
        )
    }
}
