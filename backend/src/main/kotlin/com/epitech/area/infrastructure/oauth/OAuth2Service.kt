package com.epitech.area.infrastructure.oauth

import com.epitech.area.api.dto.responses.OAuth2UserInfoResponse
import com.epitech.area.infrastructure.oauth.providers.GoogleOAuth2Provider
import com.epitech.area.infrastructure.oauth.providers.OAuth2Exception
import io.ktor.client.*
import io.ktor.server.config.*
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class OAuth2Service(
    private val config: ApplicationConfig,
    private val httpClient: HttpClient
) {
    private val logger = LoggerFactory.getLogger(OAuth2Service::class.java)
    private val providers = ConcurrentHashMap<String, OAuth2Provider>()

    init {
        registerProviders()
    }

    private fun registerProviders() {
        // Register Google OAuth2 Provider
        val googleConfig = OAuth2Config(
            clientId = config.property("oauth.google.clientId").getString(),
            clientSecret = config.property("oauth.google.clientSecret").getString(),
            redirectUri = config.property("oauth.google.redirectUri").getString(),
            scope = "openid email profile",
            authorizationUrl = "https://accounts.google.com/o/oauth2/v2/auth",
            tokenUrl = "https://oauth2.googleapis.com/token",
            userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo",
            revokeUrl = "https://oauth2.googleapis.com/revoke"
        )

        if (googleConfig.clientId.isNotEmpty() && googleConfig.clientSecret.isNotEmpty()) {
            providers["google"] = GoogleOAuth2Provider(googleConfig, httpClient)
            logger.info("Registered Google OAuth2 provider")
        }

        // Add more providers here in the future (GitHub, Facebook, etc.)
    }

    fun getProvider(name: String): OAuth2Provider? {
        return providers[name.lowercase()]
    }

    fun getAvailableProviders(): List<String> {
        return providers.keys.toList()
    }

    fun isProviderEnabled(name: String): Boolean {
        return providers.containsKey(name.lowercase())
    }

    suspend fun getAuthorizationUrl(provider: String, redirectUri: String?, state: String?): String {
        val oauthProvider = getProvider(provider)
            ?: throw OAuth2Exception("Provider $provider not found or not configured")

        val finalRedirectUri = redirectUri
            ?: config.property("oauth.$provider.redirectUri").getString()

        return oauthProvider.getAuthorizationUrl(finalRedirectUri, state)
    }

    suspend fun exchangeCodeForTokens(
        provider: String,
        code: String,
        redirectUri: String?
    ): OAuth2TokenResult {
        val oauthProvider = getProvider(provider)
            ?: throw OAuth2Exception("Provider $provider not found or not configured")

        val finalRedirectUri = redirectUri
            ?: config.property("oauth.$provider.redirectUri").getString()

        return oauthProvider.exchangeCodeForTokens(code, finalRedirectUri)
    }

    suspend fun getUserInfo(provider: String, accessToken: String): OAuth2UserInfoResponse {
        val oauthProvider = getProvider(provider)
            ?: throw OAuth2Exception("Provider $provider not found or not configured")

        return oauthProvider.getUserInfo(accessToken)
    }

    suspend fun refreshAccessToken(provider: String, refreshToken: String): OAuth2TokenResult {
        val oauthProvider = getProvider(provider)
            ?: throw OAuth2Exception("Provider $provider not found or not configured")

        return oauthProvider.refreshAccessToken(refreshToken)
    }

    suspend fun revokeToken(provider: String, token: String) {
        val oauthProvider = getProvider(provider)
            ?: throw OAuth2Exception("Provider $provider not found or not configured")

        oauthProvider.revokeToken(token)
    }
}