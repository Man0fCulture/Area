package com.epitech.area.sdk.yaml

import com.epitech.area.domain.entities.UserService
import com.epitech.area.sdk.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import org.bson.Document

/**
 * Service générique qui exécute les définitions YAML
 * Pas besoin d'écrire du code Kotlin pour chaque service !
 */
class YamlBasedService(
    private val config: YamlServiceConfig
) : ServiceDefinition() {

    override val id: String = config.service.id
    override val name: String = config.service.name
    override val description: String = config.service.description
    override val category: String = config.service.category
    override val requiresAuth: Boolean = config.auth?.type != AuthType.NONE

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Récupérer la configuration YAML complète
     */
    fun getConfig(): YamlServiceConfig = config

    /**
     * Exécuter un trigger depuis la définition YAML
     */
    override suspend fun executeTrigger(
        triggerId: String,
        config: Map<String, String>,
        userService: UserService?
    ): TriggerResult {
        val action = this.config.actions.find { it.id == triggerId }
            ?: return TriggerResult.error("Unknown trigger: $triggerId")

        // Vérifier l'auth si nécessaire
        if (requiresAuth && userService == null) {
            return TriggerResult.error("${this.name} requires authentication")
        }

        return try {
            // Construire l'URL avec templating
            val url = TemplateEngine.render(action.endpoint.url, config)

            // Construire les params
            val params = action.endpoint.params?.mapValues { (_, value) ->
                TemplateEngine.render(value, config)
            } ?: emptyMap()

            // Construire les headers
            val headers = buildHeaders(action.endpoint.headers, config, userService)

            // Faire la requête HTTP
            val response = when (action.endpoint.method) {
                HttpMethod.GET -> runtime.get(url, headers, userService)
                HttpMethod.POST -> runtime.post(url, params, headers, userService)
                HttpMethod.PUT -> runtime.put(url, params, headers, userService)
                HttpMethod.DELETE -> runtime.delete(url, headers, userService)
                else -> return TriggerResult.error("Unsupported HTTP method: ${action.endpoint.method}")
            }

            val responseBody = response.bodyAsText()

            // Vérifier la condition si définie
            if (action.condition != null) {
                val triggered = TemplateEngine.evaluateCondition(responseBody, action.condition)
                if (!triggered) {
                    return TriggerResult.notTriggered()
                }
            }

            // Extraire les données de sortie
            val data = if (action.output.isNotEmpty()) {
                TemplateEngine.extractFields(responseBody, action.output)
            } else {
                Document.parse(responseBody)
            }

            runtime.log("Trigger ${action.name} executed successfully")
            TriggerResult.success(data)

        } catch (e: Exception) {
            runtime.log("Trigger ${action.name} failed: ${e.message}", LogLevel.ERROR)
            TriggerResult.error("Failed to execute trigger: ${e.message}")
        }
    }

    /**
     * Exécuter une action depuis la définition YAML
     */
    override suspend fun executeAction(
        actionId: String,
        config: Map<String, String>,
        triggerData: Document,
        userService: UserService?
    ): ActionResult {
        val reaction = this.config.reactions.find { it.id == actionId }
            ?: return ActionResult.error("Unknown action: $actionId")

        // Vérifier l'auth si nécessaire
        if (requiresAuth && userService == null) {
            return ActionResult.error("${this.name} requires authentication")
        }

        return try {
            // Construire l'URL avec templating (config + triggerData)
            val url = TemplateEngine.render(reaction.endpoint.url, config, triggerData)

            // Construire les headers
            val headers = buildHeaders(reaction.endpoint.headers, config, userService, triggerData)

            // Construire le body
            val body: Any = when {
                reaction.endpoint.bodyRaw != null -> {
                    TemplateEngine.render(reaction.endpoint.bodyRaw, config, triggerData)
                }
                reaction.endpoint.body != null -> {
                    reaction.endpoint.body.mapValues { (_, value) ->
                        TemplateEngine.render(value, config, triggerData)
                    }
                }
                else -> emptyMap<String, String>()
            }

            // Faire la requête HTTP
            val response = when (reaction.endpoint.method) {
                HttpMethod.GET -> runtime.get(url, headers, userService)
                HttpMethod.POST -> {
                    if (reaction.endpoint.bodyRaw != null) {
                        runtime.post(url, body as String, headers, userService)
                    } else {
                        runtime.post(url, body as Map<*, *>, headers, userService)
                    }
                }
                HttpMethod.PUT -> runtime.put(url, body, headers, userService)
                HttpMethod.DELETE -> runtime.delete(url, headers, userService)
                else -> return ActionResult.error("Unsupported HTTP method: ${reaction.endpoint.method}")
            }

            if (response.status.isSuccess()) {
                runtime.log("Action ${reaction.name} executed successfully")
                ActionResult.success("${reaction.name} completed successfully")
            } else {
                runtime.log("Action ${reaction.name} failed with status ${response.status}", LogLevel.ERROR)
                ActionResult.error("Failed with status: ${response.status}")
            }

        } catch (e: Exception) {
            runtime.log("Action ${reaction.name} failed: ${e.message}", LogLevel.ERROR)
            ActionResult.error("Failed to execute action: ${e.message}")
        }
    }

    /**
     * Valider la configuration d'un trigger
     */
    override suspend fun validateTriggerConfig(
        triggerId: String,
        config: Map<String, String>
    ): ValidationResult {
        val action = this.config.actions.find { it.id == triggerId }
            ?: return ValidationResult(false, "Unknown trigger: $triggerId")

        val errors = validateFields(action.config, config)
        return if (errors.isEmpty()) {
            ValidationResult(true)
        } else {
            ValidationResult(false, errors)
        }
    }

    /**
     * Valider la configuration d'une action
     */
    override suspend fun validateActionConfig(
        actionId: String,
        config: Map<String, String>
    ): ValidationResult {
        val reaction = this.config.reactions.find { it.id == actionId }
            ?: return ValidationResult(false, "Unknown action: $actionId")

        val errors = validateFields(reaction.config, config)
        return if (errors.isEmpty()) {
            ValidationResult(true)
        } else {
            ValidationResult(false, errors)
        }
    }

    /**
     * Valider les champs de configuration
     */
    private fun validateFields(fields: List<ConfigField>, config: Map<String, String>): List<String> {
        val errors = mutableListOf<String>()

        fields.forEach { field ->
            val value = config[field.name]

            if (field.required && value.isNullOrBlank()) {
                errors.add("Field '${field.label}' is required")
            }

            if (value != null && value.isNotBlank()) {
                when (field.type) {
                    FieldType.EMAIL -> {
                        if (!value.contains("@")) {
                            errors.add("Field '${field.label}' must be a valid email")
                        }
                    }
                    FieldType.URL -> {
                        if (!value.startsWith("http://") && !value.startsWith("https://")) {
                            errors.add("Field '${field.label}' must be a valid URL")
                        }
                    }
                    FieldType.NUMBER -> {
                        if (value.toIntOrNull() == null) {
                            errors.add("Field '${field.label}' must be a number")
                        }
                    }
                    FieldType.SELECT -> {
                        if (field.options != null && value !in field.options) {
                            errors.add("Field '${field.label}' must be one of: ${field.options.joinToString(", ")}")
                        }
                    }
                    else -> { /* Pas de validation spéciale */ }
                }
            }
        }

        return errors
    }

    /**
     * Construire les headers avec auth et templating
     */
    private fun buildHeaders(
        templateHeaders: Map<String, String>?,
        config: Map<String, String>,
        userService: UserService?,
        triggerData: Document = Document()
    ): Map<String, String> {
        val headers = mutableMapOf<String, String>()

        // Headers depuis la config YAML
        templateHeaders?.forEach { (key, value) ->
            headers[key] = TemplateEngine.render(value, config, triggerData)
        }

        // Auth headers
        if (this.config.auth?.type == AuthType.API_KEY && this.config.auth.apiKey != null) {
            val apiKeyConfig = this.config.auth.apiKey
            val apiKey = System.getenv(apiKeyConfig.envVar)
            if (apiKey != null) {
                headers[apiKeyConfig.header] = "${apiKeyConfig.prefix} $apiKey".trim()
            }
        }

        return headers
    }
}
