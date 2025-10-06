package com.epitech.area.api.controllers

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Route.oAuth2Routes(oAuth2Controller: OAuth2Controller) {
    route("/auth/oauth") {
        // Public OAuth routes

        // Initialize OAuth flow - returns authorization URL
        post("/init") {
            oAuth2Controller.initializeOAuth(call)
        }

        // Get available OAuth providers
        get("/providers") {
            oAuth2Controller.getProviders(call)
        }

        // OAuth callback route for web flow (handled by provider)
        get("/{provider}/callback") {
            oAuth2Controller.handleOAuthCallback(call)
        }

        // Exchange authorization code for tokens (mobile flow)
        post("/{provider}/token") {
            oAuth2Controller.exchangeCodeForTokens(call)
        }

        // Protected OAuth routes (require authentication)
        authenticate("auth-jwt") {
            // Link OAuth account to existing user
            post("/link") {
                oAuth2Controller.linkAccount(call)
            }

            // Unlink OAuth account from user
            delete("/unlink") {
                oAuth2Controller.unlinkAccount(call)
            }

            // Get user's linked OAuth accounts
            get("/linked-accounts") {
                oAuth2Controller.getLinkedAccounts(call)
            }

            // Refresh OAuth access token for a provider
            post("/{provider}/refresh") {
                oAuth2Controller.refreshOAuthToken(call)
            }
        }
    }
}