package com.epitech.area.api.dto.responses

import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val tokenType: String = "Bearer"
)

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val username: String,
    val createdAt: Long
)

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String
)
