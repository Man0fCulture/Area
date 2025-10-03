package com.epitech.area.api.middleware

import com.epitech.area.application.services.AuthService
import com.epitech.area.domain.entities.User
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import org.bson.types.ObjectId

data class UserPrincipal(val user: User) : Principal

suspend fun ApplicationCall.getCurrentUser(): User {
    return principal<UserPrincipal>()?.user
        ?: throw Exception("User not authenticated")
}

suspend fun ApplicationCall.getCurrentUserId(): ObjectId {
    return getCurrentUser().id
}

class JwtAuthMiddleware(private val authService: AuthService) {
    suspend fun authenticate(call: ApplicationCall): UserPrincipal? {
        val authHeader = call.request.headers["Authorization"]
            ?: return null

        if (!authHeader.startsWith("Bearer ")) {
            return null
        }

        val token = authHeader.removePrefix("Bearer ")

        val result = authService.verifyToken(token)
        return result.fold(
            onSuccess = { user -> UserPrincipal(user) },
            onFailure = { null }
        )
    }
}
