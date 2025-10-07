package com.epitech.area.infrastructure.oauth

import com.epitech.area.api.dto.responses.OAuth2UserInfoResponse

interface OAuth2Provider {
    val name: String
    val displayName: String
    val authorizationUrl: String
    val tokenUrl: String
    val userInfoUrl: String
    val scope: String

    fun getAuthorizationUrl(redirectUri: String, state: String?): String
    suspend fun exchangeCodeForTokens(code: String, redirectUri: String): OAuth2TokenResult
    suspend fun getUserInfo(accessToken: String): OAuth2UserInfoResponse
    suspend fun refreshAccessToken(refreshToken: String): OAuth2TokenResult
    suspend fun revokeToken(token: String)
}

data class OAuth2TokenResult(
    val accessToken: String,
    val refreshToken: String? = null,
    val expiresIn: Long? = null,
    val tokenType: String = "Bearer",
    val idToken: String? = null
)

data class OAuth2Config(
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
    val scope: String = "",
    val authorizationUrl: String,
    val tokenUrl: String,
    val userInfoUrl: String,
    val revokeUrl: String? = null
)