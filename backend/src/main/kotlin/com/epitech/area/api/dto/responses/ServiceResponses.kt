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
    val description: String
)

@Serializable
data class ReactionDefResponse(
    val id: String,
    val name: String,
    val description: String
)

fun Service.toResponse(): ServiceResponse = ServiceResponse(
    id = id.toHexString(),
    name = name,
    description = description,
    category = category,
    requiresAuth = requiresAuth,
    actions = actions.map { ActionDefResponse(it.id, it.name, it.description) },
    reactions = reactions.map { ReactionDefResponse(it.id, it.name, it.description) }
)
