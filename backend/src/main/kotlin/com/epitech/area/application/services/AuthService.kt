package com.epitech.area.application.services

import com.epitech.area.domain.entities.User
import com.epitech.area.domain.repositories.UserRepository
import com.epitech.area.infrastructure.security.JwtService
import com.epitech.area.infrastructure.security.PasswordEncoder
import com.epitech.area.infrastructure.security.TokenPair
import org.bson.types.ObjectId

class AuthService(
    private val userRepository: UserRepository,
    private val jwtService: JwtService
) {
    suspend fun register(email: String, password: String, username: String): Result<TokenPair> {
        if (userRepository.existsByEmail(email)) {
            return Result.failure(Exception("Email already exists"))
        }

        val user = User(
            email = email,
            passwordHash = PasswordEncoder.encode(password),
            username = username
        )

        val createdUser = userRepository.create(user)
        val tokens = jwtService.generateTokenPair(createdUser.id, createdUser.email)

        return Result.success(tokens)
    }

    suspend fun login(email: String, password: String): Result<TokenPair> {
        val user = userRepository.findByEmail(email)
            ?: return Result.failure(Exception("Invalid credentials"))

        if (user.passwordHash == null || !PasswordEncoder.verify(password, user.passwordHash)) {
            return Result.failure(Exception("Invalid credentials"))
        }

        val tokens = jwtService.generateTokenPair(user.id, user.email)
        return Result.success(tokens)
    }

    suspend fun verifyToken(token: String): Result<User> {
        val payload = jwtService.decodeToken(token)
            ?: return Result.failure(Exception("Invalid token"))

        val user = userRepository.findById(ObjectId(payload.userId))
            ?: return Result.failure(Exception("User not found"))

        return Result.success(user)
    }

    suspend fun refreshToken(refreshToken: String): Result<TokenPair> {
        val payload = jwtService.decodeToken(refreshToken)
            ?: return Result.failure(Exception("Invalid refresh token"))

        val user = userRepository.findById(ObjectId(payload.userId))
            ?: return Result.failure(Exception("User not found"))

        val tokens = jwtService.generateTokenPair(user.id, user.email)
        return Result.success(tokens)
    }
}
