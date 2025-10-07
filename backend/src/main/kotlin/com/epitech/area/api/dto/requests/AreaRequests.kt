package com.epitech.area.api.dto.requests

import kotlinx.serialization.Serializable

@Serializable
data class CreateAreaRequest(
    val name: String,
    val description: String? = null,
    val action: ActionConfigRequest,
    val reactions: List<ReactionConfigRequest> = emptyList()
)

@Serializable
data class ActionConfigRequest(
    val serviceId: String,
    val actionId: String,
    val config: Map<String, String> = emptyMap()
)

@Serializable
data class ReactionConfigRequest(
    val serviceId: String,
    val reactionId: String,
    val config: Map<String, String> = emptyMap()
)

@Serializable
data class UpdateAreaRequest(
    val name: String? = null,
    val description: String? = null,
    val active: Boolean? = null
)
