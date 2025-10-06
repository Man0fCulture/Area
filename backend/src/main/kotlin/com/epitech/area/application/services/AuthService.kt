package com.epitech.area.application.services

import com.epitech.area.domain.entities.OAuthProvider
import com.epitech.area.domain.entities.User
import com.epitech.area.domain.repositories.UserRepository
import com.epitech.area.infrastructure.security.JwtService
import com.epitech.area.infrastructure.security.PasswordEncoder
import com.epitech.area.infrastructure.security.TokenPair
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory

class AuthService(
    private val userRepository: UserRepository,
    private val jwtService: JwtService
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    data class OAuthLoginResult(
        val user: User,
        val accessToken: String,
        val refreshToken: String
    )

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

    suspend fun loginOrCreateWithOAuth(
        provider: String,
        providerId: String,
        email: String,
        username: String,
        accessToken: String,
        refreshToken: String? = null
    ): OAuthLoginResult {
        logger.info("OAuth login/create for $email with provider $provider")

        // Check if user exists with this email
        var user = userRepository.findByEmail(email)

        if (user != null) {
            // User exists - update or add OAuth provider
            logger.info("Existing user found for email $email")

            val updatedProviders = user.oauthProviders.toMutableMap()
            updatedProviders[provider] = OAuthProvider(
                provider = provider,
                providerId = providerId,
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresAt = System.currentTimeMillis() + 3600000 // 1 hour
            )

            user = user.copy(
                oauthProviders = updatedProviders,
                updatedAt = System.currentTimeMillis()
            )

            userRepository.update(user)
        } else {
            // Create new user with OAuth
            logger.info("Creating new user for email $email with OAuth provider $provider")

            user = User(
                email = email,
                passwordHash = null, // No password for OAuth-only accounts
                username = username,
                oauthProviders = mapOf(
                    provider to OAuthProvider(
                        provider = provider,
                        providerId = providerId,
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        expiresAt = System.currentTimeMillis() + 3600000
                    )
                )
            )

            user = userRepository.create(user)
        }

        // Generate JWT tokens
        val tokens = jwtService.generateTokenPair(user.id, user.email)

        return OAuthLoginResult(
            user = user,
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken
        )
    }

    suspend fun linkOAuthAccount(
        userId: ObjectId,
        provider: String,
        providerId: String,
        accessToken: String,
        refreshToken: String? = null
    ): Boolean {
        val user = userRepository.findById(userId) ?: return false

        // Check if provider is already linked
        if (user.oauthProviders.containsKey(provider)) {
            logger.warn("Provider $provider is already linked to user ${user.email}")
            return false
        }

        val updatedProviders = user.oauthProviders.toMutableMap()
        updatedProviders[provider] = OAuthProvider(
            provider = provider,
            providerId = providerId,
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAt = System.currentTimeMillis() + 3600000
        )

        val updatedUser = user.copy(
            oauthProviders = updatedProviders,
            updatedAt = System.currentTimeMillis()
        )

        userRepository.update(updatedUser)
        return true
    }

    suspend fun unlinkOAuthAccount(userId: ObjectId, provider: String): Boolean {
        val user = userRepository.findById(userId) ?: return false

        // Check if provider is linked
        if (!user.oauthProviders.containsKey(provider)) {
            logger.warn("Provider $provider is not linked to user ${user.email}")
            return false
        }

        val updatedProviders = user.oauthProviders.toMutableMap()
        updatedProviders.remove(provider)

        val updatedUser = user.copy(
            oauthProviders = updatedProviders,
            updatedAt = System.currentTimeMillis()
        )

        userRepository.update(updatedUser)
        return true
    }

    suspend fun canUnlinkOAuthAccount(userId: ObjectId, provider: String): Boolean {
        val user = userRepository.findById(userId) ?: return false

        // Can unlink if:
        // 1. User has a password set, OR
        // 2. User has other OAuth providers linked
        val hasPassword = user.passwordHash != null
        val hasOtherProviders = user.oauthProviders.size > 1

        return hasPassword || hasOtherProviders
    }

    suspend fun updateOAuthTokens(
        userId: ObjectId,
        provider: String,
        accessToken: String,
        refreshToken: String? = null
    ): Boolean {
        val user = userRepository.findById(userId) ?: return false

        val oauthData = user.oauthProviders[provider] ?: return false

        val updatedProviders = user.oauthProviders.toMutableMap()
        updatedProviders[provider] = oauthData.copy(
            accessToken = accessToken,
            refreshToken = refreshToken ?: oauthData.refreshToken,
            expiresAt = System.currentTimeMillis() + 3600000
        )

        val updatedUser = user.copy(
            oauthProviders = updatedProviders,
            updatedAt = System.currentTimeMillis()
        )

        userRepository.update(updatedUser)
        return true
    }

    suspend fun getUserById(id: ObjectId): User? {
        return userRepository.findById(id)
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
