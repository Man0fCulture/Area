package com.epitech.area.api.controllers

import com.epitech.area.api.dto.requests.*
import com.epitech.area.api.dto.responses.*
import com.epitech.area.application.services.AuthService
import com.epitech.area.domain.entities.User
import com.epitech.area.infrastructure.oauth.OAuth2Service
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import java.net.URLEncoder
import java.util.*

class OAuth2Controller(
    private val oAuth2Service: OAuth2Service,
    private val authService: AuthService,
    private val config: ApplicationConfig
) {
    private val logger = LoggerFactory.getLogger(OAuth2Controller::class.java)

    // Initialize OAuth flow - returns authorization URL
    suspend fun initializeOAuth(call: ApplicationCall) {
        try {
            val request = call.receive<OAuth2InitRequest>()
            val provider = request.provider.lowercase()

            if (!oAuth2Service.isProviderEnabled(provider)) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Provider $provider is not configured"))
                return
            }

            val state = request.state ?: UUID.randomUUID().toString()
            val mode = request.mode ?: "redirect"

            // Modify redirect URI to include mode parameter for popup flow
            val redirectUri = if (mode == "popup" && request.redirectUri == null) {
                val baseUri = config.property("oauth.$provider.redirectUri").getString()
                "$baseUri${if (baseUri.contains("?")) "&" else "?"}mode=popup"
            } else {
                request.redirectUri
            }

            val authUrl = oAuth2Service.getAuthorizationUrl(provider, redirectUri, state)

            call.respond(HttpStatusCode.OK, OAuth2AuthUrlResponse(authUrl, state))
        } catch (e: Exception) {
            logger.error("Failed to initialize OAuth", e)
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to initialize OAuth"))
        }
    }

    // Handle OAuth callback (for web flow)
    suspend fun handleOAuthCallback(call: ApplicationCall) {
        try {
            val provider = call.parameters["provider"]?.lowercase()
            val code = call.parameters["code"]
            val state = call.parameters["state"]
            val error = call.parameters["error"]
            val mode = call.parameters["mode"] ?: "redirect" // "popup" or "redirect"

            if (provider == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Provider is required"))
                return
            }

            if (error != null) {
                logger.error("OAuth callback error: $error")
                val errorDescription = call.parameters["error_description"] ?: "Authentication failed"

                if (mode == "popup") {
                    // Return HTML page that will close the popup
                    val html = getCallbackHtml(error = errorDescription, state = state)
                    call.respondText(html, ContentType.Text.Html)
                } else {
                    // Redirect to frontend with error
                    call.respondRedirect("${getFrontendUrl(call)}/auth/error?error=${URLEncoder.encode(errorDescription, "UTF-8")}")
                }
                return
            }

            if (code == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Authorization code is required"))
                return
            }

            // Exchange code for tokens
            val tokenResult = oAuth2Service.exchangeCodeForTokens(provider, code, null)

            // Get user info from provider
            val userInfo = oAuth2Service.getUserInfo(provider, tokenResult.accessToken)

            // Create or login user
            val authResult = authService.loginOrCreateWithOAuth(
                provider = provider,
                providerId = userInfo.id,
                email = userInfo.email,
                username = userInfo.name ?: userInfo.email.substringBefore("@"),
                accessToken = tokenResult.accessToken,
                refreshToken = tokenResult.refreshToken
            )

            if (mode == "popup") {
                // Return HTML page that will close the popup and send tokens via postMessage
                val html = getCallbackHtml(
                    accessToken = authResult.accessToken,
                    refreshToken = authResult.refreshToken,
                    state = state
                )
                call.respondText(html, ContentType.Text.Html)
            } else {
                // Redirect to frontend with tokens
                val frontendUrl = getFrontendUrl(call)
                val redirectUrl = "$frontendUrl/auth/success" +
                        "?access_token=${authResult.accessToken}" +
                        "&refresh_token=${authResult.refreshToken}" +
                        state?.let { "&state=${URLEncoder.encode(it, "UTF-8")}" }.orEmpty()

                call.respondRedirect(redirectUrl)
            }
        } catch (e: Exception) {
            logger.error("OAuth callback failed", e)

            val mode = call.parameters["mode"] ?: "redirect"
            if (mode == "popup") {
                val html = getCallbackHtml(error = "authentication_failed", state = call.parameters["state"])
                call.respondText(html, ContentType.Text.Html)
            } else {
                call.respondRedirect("${getFrontendUrl(call)}/auth/error?error=authentication_failed")
            }
        }
    }

    // Exchange authorization code for tokens (for mobile flow)
    suspend fun exchangeCodeForTokens(call: ApplicationCall) {
        try {
            val provider = call.parameters["provider"]?.lowercase()
            val request = call.receive<OAuth2TokenExchangeRequest>()

            if (provider == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Provider is required"))
                return
            }

            // Exchange code for tokens
            val tokenResult = oAuth2Service.exchangeCodeForTokens(provider, request.code, request.redirectUri)

            // Get user info from provider
            val userInfo = oAuth2Service.getUserInfo(provider, tokenResult.accessToken)

            // Create or login user
            val authResult = authService.loginOrCreateWithOAuth(
                provider = provider,
                providerId = userInfo.id,
                email = userInfo.email,
                username = userInfo.name ?: userInfo.email.substringBefore("@"),
                accessToken = tokenResult.accessToken,
                refreshToken = tokenResult.refreshToken
            )

            val userResponse = UserResponse(
                id = authResult.user.id.toString(),
                email = authResult.user.email,
                username = authResult.user.username,
                createdAt = authResult.user.createdAt
            )

            call.respond(
                HttpStatusCode.OK,
                OAuth2TokenResponse(
                    accessToken = authResult.accessToken,
                    refreshToken = authResult.refreshToken,
                    expiresIn = 900000, // 15 minutes
                    tokenType = "Bearer",
                    user = userResponse
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to exchange code for tokens", e)
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to exchange code for tokens"))
        }
    }

    // Get available OAuth providers
    suspend fun getProviders(call: ApplicationCall) {
        try {
            val providers = oAuth2Service.getAvailableProviders().map { provider ->
                OAuth2ProviderInfo(
                    name = provider,
                    displayName = provider.capitalize(),
                    enabled = true,
                    iconUrl = getProviderIconUrl(provider)
                )
            }

            call.respond(HttpStatusCode.OK, OAuth2ProvidersResponse(providers))
        } catch (e: Exception) {
            logger.error("Failed to get providers", e)
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to get providers"))
        }
    }

    // Link OAuth account to existing user
    suspend fun linkAccount(call: ApplicationCall) {
        try {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asString()

            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                return
            }

            val request = call.receive<OAuth2LinkAccountRequest>()
            val provider = request.provider.lowercase()

            // Get user info from provider
            val userInfo = oAuth2Service.getUserInfo(provider, request.accessToken)

            // Link account to existing user
            val success = authService.linkOAuthAccount(
                userId = ObjectId(userId),
                provider = provider,
                providerId = userInfo.id,
                accessToken = request.accessToken
            )

            if (success) {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Account linked successfully"))
            } else {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Failed to link account"))
            }
        } catch (e: Exception) {
            logger.error("Failed to link account", e)
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to link account"))
        }
    }

    // Unlink OAuth account from user
    suspend fun unlinkAccount(call: ApplicationCall) {
        try {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asString()

            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                return
            }

            val request = call.receive<OAuth2UnlinkAccountRequest>()
            val provider = request.provider.lowercase()

            // Check if user has password or other OAuth providers
            val canUnlink = authService.canUnlinkOAuthAccount(ObjectId(userId), provider)

            if (!canUnlink) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Cannot unlink the only authentication method. Please set a password first or link another provider.")
                )
                return
            }

            // Unlink account
            val success = authService.unlinkOAuthAccount(ObjectId(userId), provider)

            if (success) {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Account unlinked successfully"))
            } else {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Failed to unlink account"))
            }
        } catch (e: Exception) {
            logger.error("Failed to unlink account", e)
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to unlink account"))
        }
    }

    // Get user's linked OAuth accounts
    suspend fun getLinkedAccounts(call: ApplicationCall) {
        try {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asString()

            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                return
            }

            val user = authService.getUserById(ObjectId(userId))

            if (user == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                return
            }

            val accounts = user.oauthProviders.map { (provider, data) ->
                OAuth2LinkedAccount(
                    provider = provider,
                    email = user.email,
                    linkedAt = user.createdAt.toString(),
                    displayName = provider.capitalize()
                )
            }

            call.respond(HttpStatusCode.OK, OAuth2LinkedAccountsResponse(accounts))
        } catch (e: Exception) {
            logger.error("Failed to get linked accounts", e)
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to get linked accounts"))
        }
    }

    // Refresh OAuth access token
    suspend fun refreshOAuthToken(call: ApplicationCall) {
        try {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asString()

            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                return
            }

            val provider = call.parameters["provider"]?.lowercase()

            if (provider == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Provider is required"))
                return
            }

            val user = authService.getUserById(ObjectId(userId))

            if (user == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                return
            }

            val oauthData = user.oauthProviders[provider]

            if (oauthData?.refreshToken == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "No refresh token available for this provider"))
                return
            }

            // Refresh the token
            val tokenResult = oAuth2Service.refreshAccessToken(provider, oauthData.refreshToken)

            // Update user's OAuth data
            authService.updateOAuthTokens(
                userId = ObjectId(userId),
                provider = provider,
                accessToken = tokenResult.accessToken,
                refreshToken = tokenResult.refreshToken ?: oauthData.refreshToken
            )

            call.respond(
                HttpStatusCode.OK,
                mapOf(
                    "accessToken" to tokenResult.accessToken,
                    "expiresIn" to (tokenResult.expiresIn ?: 3600)
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to refresh OAuth token", e)
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to refresh OAuth token"))
        }
    }

    private fun getFrontendUrl(call: ApplicationCall): String {
        // Get frontend URL from config first, then headers, then default
        val configUrl = try {
            config.property("frontend.url").getString()
        } catch (e: Exception) {
            null
        }

        if (configUrl != null && configUrl.isNotBlank()) {
            return configUrl
        }

        val origin = call.request.headers["Origin"]
        val referer = call.request.headers["Referer"]?.substringBefore("/auth")

        return when {
            origin != null && !origin.contains("google.com") && !origin.contains("github.com") && !origin.contains("facebook.com") -> origin
            referer != null && !referer.contains("google.com") && !referer.contains("github.com") && !referer.contains("facebook.com") -> referer
            else -> "http://localhost:3000" // Default frontend URL
        }
    }

    private fun getProviderIconUrl(provider: String): String {
        return when (provider.lowercase()) {
            "google" -> "https://www.google.com/favicon.ico"
            "github" -> "https://github.com/favicon.ico"
            "facebook" -> "https://www.facebook.com/favicon.ico"
            else -> ""
        }
    }

    private fun getCallbackHtml(
        accessToken: String? = null,
        refreshToken: String? = null,
        error: String? = null,
        state: String? = null
    ): String {
        // Read the HTML template from resources
        val htmlTemplate = this::class.java.classLoader.getResource("oauth-callback.html")?.readText()
            ?: getDefaultCallbackHtml()

        // If we have the template, use it. Otherwise, generate a simple one
        return if (accessToken != null && refreshToken != null) {
            htmlTemplate.replace("window.location.search",
                "?access_token=$accessToken&refresh_token=$refreshToken${state?.let { "&state=$it" } ?: ""}")
        } else if (error != null) {
            htmlTemplate.replace("window.location.search",
                "?error=${URLEncoder.encode(error, "UTF-8")}${state?.let { "&state=$it" } ?: ""}")
        } else {
            htmlTemplate
        }
    }

    private fun getDefaultCallbackHtml(): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>OAuth2 Callback</title>
                <script>
                    const params = new URLSearchParams(window.location.search);
                    const data = {
                        type: params.get('error') ? 'oauth2_error' : 'oauth2_success',
                        accessToken: params.get('access_token'),
                        refreshToken: params.get('refresh_token'),
                        error: params.get('error'),
                        state: params.get('state')
                    };
                    if (window.opener) {
                        window.opener.postMessage(data, '*');
                        setTimeout(() => window.close(), 1000);
                    } else {
                        localStorage.setItem('oauth2_result', JSON.stringify(data));
                        window.location.href = '/';
                    }
                </script>
            </head>
            <body>
                <div style="text-align: center; padding: 50px; font-family: Arial;">
                    <h2>Authentication Complete</h2>
                    <p>You can close this window...</p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}