package com.epitech.area.api.dto.responses

import com.epitech.area.domain.entities.Area
import com.epitech.area.domain.entities.ActionConfig
import com.epitech.area.domain.entities.ReactionConfig
import kotlinx.serialization.Serializable

@Serializable
data class AreaResponse(
    val id: String,
    val name: String,
    val description: String?,
    val action: ActionConfigResponse,
    val reactions: List<ReactionConfigResponse>,
    val active: Boolean,
    val lastTriggeredAt: Long?,
    val executionCount: Long,
    val createdAt: Long
)

@Serializable
data class ActionConfigResponse(
    val serviceId: String,
    val actionId: String,
    val config: Map<String, String>
)

@Serializable
data class ReactionConfigResponse(
    val serviceId: String,
    val reactionId: String,
    val config: Map<String, String>
)

fun Area.toResponse(): AreaResponse = AreaResponse(
    id = id.toHexString(),
    name = name,
    description = description,
    action = action.toResponse(),
    reactions = reactions.map { it.toResponse() },
    active = active,
    lastTriggeredAt = lastTriggeredAt,
    executionCount = executionCount,
    createdAt = createdAt
)

fun ActionConfig.toResponse(): ActionConfigResponse = ActionConfigResponse(
    serviceId = serviceId.toHexString(),
    actionId = actionId,
    config = config
)

fun ReactionConfig.toResponse(): ReactionConfigResponse = ReactionConfigResponse(
    serviceId = serviceId.toHexString(),
    reactionId = reactionId,
    config = config
)
