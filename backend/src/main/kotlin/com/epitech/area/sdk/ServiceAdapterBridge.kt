package com.epitech.area.sdk

import com.epitech.area.domain.entities.UserService
import com.epitech.area.infrastructure.integrations.ServiceAdapter
import org.bson.Document
import com.epitech.area.infrastructure.integrations.ActionResult as InfraActionResult
import com.epitech.area.infrastructure.integrations.ReactionResult as InfraReactionResult
import com.epitech.area.infrastructure.integrations.ValidationResult as InfraValidationResult

/**
 * Bridge entre le nouveau SDK et l'ancien système ServiceAdapter
 * Permet la migration progressive sans casser le code existant
 */
class ServiceAdapterBridge(
    private val serviceDefinition: ServiceDefinition
) : ServiceAdapter {

    override val serviceId: String
        get() = serviceDefinition.id

    override val serviceName: String
        get() = serviceDefinition.name

    override suspend fun executeAction(
        actionId: String,
        config: Map<String, Any>,
        userService: UserService?
    ): InfraActionResult {
        // Convertir Map<String, Any> en Map<String, String>
        val stringConfig = config.mapValues { it.value.toString() }

        val result = serviceDefinition.executeTrigger(actionId, stringConfig, userService)

        return InfraActionResult(
            success = result.success,
            data = result.data,
            error = result.error
        )
    }

    override suspend fun executeReaction(
        reactionId: String,
        config: Map<String, Any>,
        actionData: Document,
        userService: UserService?
    ): InfraReactionResult {
        // Convertir Map<String, Any> en Map<String, String>
        val stringConfig = config.mapValues { it.value.toString() }

        val result = serviceDefinition.executeAction(reactionId, stringConfig, actionData, userService)

        return InfraReactionResult(
            success = result.success,
            message = result.message,
            error = result.error
        )
    }

    override suspend fun validateActionConfig(
        actionId: String,
        config: Map<String, Any>
    ): InfraValidationResult {
        val stringConfig = config.mapValues { it.value.toString() }
        val result = serviceDefinition.validateTriggerConfig(actionId, stringConfig)

        return InfraValidationResult(
            valid = result.valid,
            errors = result.errors
        )
    }

    override suspend fun validateReactionConfig(
        reactionId: String,
        config: Map<String, Any>
    ): InfraValidationResult {
        val stringConfig = config.mapValues { it.value.toString() }
        val result = serviceDefinition.validateActionConfig(reactionId, stringConfig)

        return InfraValidationResult(
            valid = result.valid,
            errors = result.errors
        )
    }
}
