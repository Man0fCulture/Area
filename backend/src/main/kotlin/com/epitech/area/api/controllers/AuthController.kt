package com.epitech.area.api.controllers

import com.epitech.area.api.dto.requests.LoginRequest
import com.epitech.area.api.dto.requests.RefreshTokenRequest
import com.epitech.area.api.dto.requests.RegisterRequest
import com.epitech.area.api.dto.responses.AuthResponse
import com.epitech.area.api.dto.responses.ErrorResponse
import com.epitech.area.api.dto.responses.TokenResponse
import com.epitech.area.api.dto.responses.UserResponse
import com.epitech.area.application.services.AuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(authService: AuthService) {
    route("/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()

            val result = authService.register(request.email, request.password, request.username)

            result.fold(
                onSuccess = { authResult ->
                    call.respond(
                        HttpStatusCode.Created,
                        AuthResponse(
                            user = UserResponse(
                                id = authResult.user.id.toHexString(),
                                email = authResult.user.email,
                                username = authResult.user.username,
                                createdAt = authResult.user.createdAt
                            ),
                            accessToken = authResult.tokens.accessToken,
                            refreshToken = authResult.tokens.refreshToken
                        )
                    )
                },
                onFailure = { error ->
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("registration_failed", error.message ?: "Unknown error")
                    )
                }
            )
        }

        post("/login") {
            val request = call.receive<LoginRequest>()

            val result = authService.login(request.email, request.password)

            result.fold(
                onSuccess = { authResult ->
                    call.respond(
                        AuthResponse(
                            user = UserResponse(
                                id = authResult.user.id.toHexString(),
                                email = authResult.user.email,
                                username = authResult.user.username,
                                createdAt = authResult.user.createdAt
                            ),
                            accessToken = authResult.tokens.accessToken,
                            refreshToken = authResult.tokens.refreshToken
                        )
                    )
                },
                onFailure = { error ->
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("authentication_failed", error.message ?: "Invalid credentials")
                    )
                }
            )
        }

        post("/refresh") {
            val request = call.receive<RefreshTokenRequest>()

            if (request.refreshToken == null) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("token_refresh_failed", "Refresh token is required")
                )
            }

            val result = authService.refreshToken(request.refreshToken)

            result.fold(
                onSuccess = { tokens ->
                    call.respond(
                        TokenResponse(
                            accessToken = tokens.accessToken,
                            refreshToken = tokens.refreshToken,
                            expiresIn = tokens.expiresIn
                        )
                    )
                },
                onFailure = { error ->
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("token_refresh_failed", error.message ?: "Invalid refresh token")
                    )
                }
            )
        }
    }
}
