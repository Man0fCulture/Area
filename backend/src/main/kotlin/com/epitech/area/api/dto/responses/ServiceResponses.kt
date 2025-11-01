package com.epitech.area.api.dto.responses

import com.epitech.area.domain.entities.Service
import kotlinx.serialization.Serializable

@Serializable
data class ServiceResponse(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val requiresAuth: Boolean,
    val actions: List<ActionDefResponse>,
    val reactions: List<ReactionDefResponse>
)

@Serializable
data class ActionDefResponse(
    val id: String,
    val name: String,
    val description: String,
    val configSchema: Map<String, FieldSchemaResponse>
)

@Serializable
data class ReactionDefResponse(
    val id: String,
    val name: String,
    val description: String,
    val configSchema: Map<String, FieldSchemaResponse>
)

@Serializable
data class FieldSchemaResponse(
    val type: String,
    val required: Boolean,
    val description: String? = null,
    val default: String? = null,
    val options: List<String>? = null
)

fun Service.toResponse(): ServiceResponse = ServiceResponse(
    id = id.toHexString(),
    name = name,
    description = description,
    category = category,
    requiresAuth = requiresAuth,
    actions = actions.map { action ->
        ActionDefResponse(
            id = action.id,
            name = action.name,
            description = action.description,
            configSchema = action.configSchema.mapValues { (_, schema) ->
                FieldSchemaResponse(
                    type = schema.type,
                    required = schema.required,
                    description = schema.description,
                    default = schema.default,
                    options = schema.options
                )
            }
        )
    },
    reactions = reactions.map { reaction ->
        ReactionDefResponse(
            id = reaction.id,
            name = reaction.name,
            description = reaction.description,
            configSchema = reaction.configSchema.mapValues { (_, schema) ->
                FieldSchemaResponse(
                    type = schema.type,
                    required = schema.required,
                    description = schema.description,
                    default = schema.default,
                    options = schema.options
                )
            }
        )
    }
)
