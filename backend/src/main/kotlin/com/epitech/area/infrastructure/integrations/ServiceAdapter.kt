package com.epitech.area.infrastructure.integrations

import com.epitech.area.domain.entities.UserService
import org.bson.Document

interface ServiceAdapter {
    val serviceId: String
    val serviceName: String

    suspend fun executeAction(
        actionId: String,
        config: Map<String, Any>,
        userService: UserService?
    ): ActionResult

    suspend fun executeReaction(
        reactionId: String,
        config: Map<String, Any>,
        actionData: Document,
        userService: UserService?
    ): ReactionResult

    suspend fun validateActionConfig(actionId: String, config: Map<String, Any>): ValidationResult
    suspend fun validateReactionConfig(reactionId: String, config: Map<String, Any>): ValidationResult
}

data class ActionResult(
    val success: Boolean,
    val data: Document = Document(),
    val error: String? = null
)

data class ReactionResult(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)

data class ValidationResult(
    val valid: Boolean,
    val errors: List<String> = emptyList()
)
