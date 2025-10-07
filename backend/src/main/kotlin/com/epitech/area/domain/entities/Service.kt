package com.epitech.area.domain.entities

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Service(
    @BsonId
    val id: ObjectId = ObjectId(),
    val name: String,
    val description: String,
    val iconUrl: String? = null,
    val category: String,
    val requiresAuth: Boolean = false,
    val authType: String? = null,
    val actions: List<ActionDefinition> = emptyList(),
    val reactions: List<ReactionDefinition> = emptyList(),
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class ActionDefinition(
    val id: String,
    val name: String,
    val description: String,
    val triggerType: TriggerType,
    val configSchema: Map<String, FieldSchema> = emptyMap()
)

data class ReactionDefinition(
    val id: String,
    val name: String,
    val description: String,
    val configSchema: Map<String, FieldSchema> = emptyMap()
)

data class FieldSchema(
    val type: String,
    val required: Boolean = false,
    val default: String? = null,
    val options: List<String>? = null,
    val description: String? = null
)

enum class TriggerType {
    POLLING,
    WEBHOOK,
    EVENT,
    SCHEDULE
}
