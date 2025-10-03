package com.epitech.area.domain.entities

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Area(
    @BsonId
    val id: ObjectId = ObjectId(),
    val userId: ObjectId,
    val name: String,
    val description: String? = null,
    val action: ActionConfig,
    val reactions: List<ReactionConfig>,
    val active: Boolean = true,
    val lastTriggeredAt: Long? = null,
    val executionCount: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class ActionConfig(
    val serviceId: ObjectId,
    val actionId: String,
    val config: Map<String, String> = emptyMap()
)

data class ReactionConfig(
    val serviceId: ObjectId,
    val reactionId: String,
    val config: Map<String, String> = emptyMap()
)
