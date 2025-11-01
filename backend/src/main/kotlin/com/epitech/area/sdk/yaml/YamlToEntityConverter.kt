package com.epitech.area.sdk.yaml

import com.epitech.area.domain.entities.*

/**
 * Convertit les définitions YAML en entités domain
 * pour les sauvegarder en base de données
 */
object YamlToEntityConverter {

    /**
     * Convertir un YamlServiceConfig en Service entity
     */
    fun toServiceEntity(config: YamlServiceConfig): Service {
        return Service(
            name = config.service.name,
            description = config.service.description,
            category = config.service.category,
            iconUrl = config.service.icon,
            requiresAuth = config.auth?.type != AuthType.NONE,
            authType = when (config.auth?.type) {
                AuthType.OAUTH2 -> "OAuth2"
                AuthType.API_KEY -> "API_KEY"
                else -> null
            },
            actions = config.actions.map { toActionDefinition(it) },
            reactions = config.reactions.map { toReactionDefinition(it) },
            enabled = true
        )
    }

    /**
     * Convertir un ActionConfig en ActionDefinition
     */
    private fun toActionDefinition(action: ActionConfig): ActionDefinition {
        return ActionDefinition(
            id = action.id,
            name = action.name,
            description = action.description,
            triggerType = when (action.type) {
                TriggerType.POLLING -> com.epitech.area.domain.entities.TriggerType.POLLING
                TriggerType.WEBHOOK -> com.epitech.area.domain.entities.TriggerType.WEBHOOK
                TriggerType.SCHEDULE -> com.epitech.area.domain.entities.TriggerType.SCHEDULE
            },
            configSchema = action.config.associate { field ->
                field.name to toFieldSchema(field)
            }
        )
    }

    /**
     * Convertir un ReactionConfig en ReactionDefinition
     */
    private fun toReactionDefinition(reaction: ReactionConfig): ReactionDefinition {
        return ReactionDefinition(
            id = reaction.id,
            name = reaction.name,
            description = reaction.description,
            configSchema = reaction.config.associate { field ->
                field.name to toFieldSchema(field)
            }
        )
    }

    /**
     * Convertir un ConfigField en FieldSchema
     */
    private fun toFieldSchema(field: ConfigField): FieldSchema {
        return FieldSchema(
            type = field.type.name.lowercase(),
            required = field.required,
            description = field.description ?: field.placeholder,
            default = field.default,
            options = field.options
        )
    }
}
