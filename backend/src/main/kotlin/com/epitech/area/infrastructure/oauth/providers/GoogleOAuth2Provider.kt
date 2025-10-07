package com.epitech.area.infrastructure.oauth.providers

import com.epitech.area.api.dto.responses.OAuth2UserInfoResponse
import com.epitech.area.infrastructure.oauth.OAuth2Config
import com.epitech.area.infrastructure.oauth.OAuth2Provider
import com.epitech.area.infrastructure.oauth.OAuth2TokenResult
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.net.URLEncoder

class GoogleOAuth2Provider(
    private val config: OAuth2Config,
    private val httpClient: HttpClient
) : OAuth2Provider {

    private val logger = LoggerFactory.getLogger(GoogleOAuth2Provider::class.java)
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override val name = "google"
    override val displayName = "Google"
    override val authorizationUrl = "https://accounts.google.com/o/oauth2/v2/auth"
    override val tokenUrl = "https://oauth2.googleapis.com/token"
    override val userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo"
    override val scope = "openid email profile"

    override fun getAuthorizationUrl(redirectUri: String, state: String?): String {
        val params = mutableMapOf(
            "client_id" to config.clientId,
            "redirect_uri" to redirectUri,
            "response_type" to "code",
            "scope" to scope,
            "access_type" to "offline",
            "prompt" to "consent"
        )

        state?.let { params["state"] = it }

        val queryString = params.entries.joinToString("&") { (key, value) ->
            "$key=${URLEncoder.encode(value, "UTF-8")}"
        }

        return "$authorizationUrl?$queryString"
    }

    override suspend fun exchangeCodeForTokens(code: String, redirectUri: String): OAuth2TokenResult {
        try {
            logger.info("Exchanging code for tokens with Google OAuth2")

            val response: HttpResponse = httpClient.submitForm(
                url = tokenUrl,
                formParameters = parameters {
                    append("code", code)
                    append("client_id", config.clientId)
                    append("client_secret", config.clientSecret)
                    append("redirect_uri", redirectUri)
                    append("grant_type", "authorization_code")
                }
            )

            val responseBody = response.bodyAsText()
            logger.debug("Token exchange response: $responseBody")

            val tokenResponse = json.decodeFromString<GoogleTokenResponse>(responseBody)

            return OAuth2TokenResult(
                accessToken = tokenResponse.accessToken,
                refreshToken = tokenResponse.refreshToken,
                expiresIn = tokenResponse.expiresIn,
                tokenType = tokenResponse.tokenType,
                idToken = tokenResponse.idToken
            )
        } catch (e: Exception) {
            logger.error("Failed to exchange code for tokens", e)
            throw OAuth2Exception("Failed to exchange authorization code: ${e.message}")
        }
    }

    override suspend fun getUserInfo(accessToken: String): OAuth2UserInfoResponse {
        try {
            logger.info("Fetching user info from Google")

            val response: HttpResponse = httpClient.get(userInfoUrl) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }

            val responseBody = response.bodyAsText()
            logger.debug("User info response: $responseBody")

            val userInfo = json.decodeFromString<GoogleUserInfo>(responseBody)

            return OAuth2UserInfoResponse(
                id = userInfo.sub,
                email = userInfo.email,
                name = userInfo.name,
                picture = userInfo.picture,
                provider = name
            )
        } catch (e: Exception) {
            logger.error("Failed to fetch user info", e)
            throw OAuth2Exception("Failed to fetch user info: ${e.message}")
        }
    }

    override suspend fun refreshAccessToken(refreshToken: String): OAuth2TokenResult {
        try {
            logger.info("Refreshing access token with Google OAuth2")

            val response: HttpResponse = httpClient.submitForm(
                url = tokenUrl,
                formParameters = parameters {
                    append("refresh_token", refreshToken)
                    append("client_id", config.clientId)
                    append("client_secret", config.clientSecret)
                    append("grant_type", "refresh_token")
                }
            )

            val responseBody = response.bodyAsText()
            val tokenResponse = json.decodeFromString<GoogleTokenResponse>(responseBody)

            return OAuth2TokenResult(
                accessToken = tokenResponse.accessToken,
                refreshToken = refreshToken, // Google doesn't return a new refresh token
                expiresIn = tokenResponse.expiresIn,
                tokenType = tokenResponse.tokenType
            )
        } catch (e: Exception) {
            logger.error("Failed to refresh access token", e)
            throw OAuth2Exception("Failed to refresh access token: ${e.message}")
        }
    }

    override suspend fun revokeToken(token: String) {
        try {
            logger.info("Revoking token with Google OAuth2")

            httpClient.post("https://oauth2.googleapis.com/revoke") {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody("token=$token")
            }

            logger.info("Token revoked successfully")
        } catch (e: Exception) {
            logger.error("Failed to revoke token", e)
            // Revocation errors are not critical
        }
    }

    @Serializable
    private data class GoogleTokenResponse(
        @SerialName("access_token") val accessToken: String,
        @SerialName("refresh_token") val refreshToken: String? = null,
        @SerialName("expires_in") val expiresIn: Long? = null,
        @SerialName("token_type") val tokenType: String = "Bearer",
        @SerialName("id_token") val idToken: String? = null
    )

    @Serializable
    private data class GoogleUserInfo(
        val sub: String,
        val email: String,
        val name: String? = null,
        val picture: String? = null,
        @SerialName("email_verified") val emailVerified: Boolean = false
    )
}

class OAuth2Exception(message: String, cause: Throwable? = null) : Exception(message, cause)