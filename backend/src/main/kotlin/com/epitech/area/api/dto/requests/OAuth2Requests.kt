package com.epitech.area.api.dto.requests

import kotlinx.serialization.Serializable

@Serializable
data class OAuth2InitRequest(
    val provider: String,
    val redirectUri: String? = null,
    val state: String? = null,
    val mode: String? = "redirect" // "popup" or "redirect"
)

@Serializable
data class OAuth2CallbackRequest(
    val code: String,
    val state: String? = null
)

@Serializable
data class OAuth2TokenExchangeRequest(
    val code: String,
    val redirectUri: String,
    val state: String? = null
)

@Serializable
data class OAuth2RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class OAuth2LinkAccountRequest(
    val provider: String,
    val accessToken: String
)

@Serializable
data class OAuth2UnlinkAccountRequest(
    val provider: String
)