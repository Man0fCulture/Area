package com.epitech.area.api.dto.responses

import kotlinx.serialization.Serializable

@Serializable
data class OAuth2AuthUrlResponse(
    val authUrl: String,
    val state: String? = null
)

@Serializable
data class OAuth2TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val tokenType: String = "Bearer",
    val user: UserResponse
)

@Serializable
data class OAuth2UserInfoResponse(
    val id: String,
    val email: String,
    val name: String? = null,
    val picture: String? = null,
    val provider: String
)

@Serializable
data class OAuth2ProvidersResponse(
    val providers: List<OAuth2ProviderInfo>
)

@Serializable
data class OAuth2ProviderInfo(
    val name: String,
    val displayName: String,
    val enabled: Boolean,
    val iconUrl: String? = null
)

@Serializable
data class OAuth2LinkedAccountsResponse(
    val accounts: List<OAuth2LinkedAccount>
)

@Serializable
data class OAuth2LinkedAccount(
    val provider: String,
    val email: String,
    val linkedAt: String,
    val displayName: String? = null
)