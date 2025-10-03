package com.epitech.area.infrastructure.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import org.bson.types.ObjectId
import java.util.*

data class TokenPair(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)

data class JwtPayload(
    val userId: String,
    val email: String
)

class JwtService(
    private val secret: String,
    private val issuer: String,
    private val audience: String,
    private val accessTokenExpiration: Long,
    private val refreshTokenExpiration: Long
) {
    private val algorithm = Algorithm.HMAC256(secret)

    fun generateTokenPair(userId: ObjectId, email: String): TokenPair {
        val now = Date()

        val accessToken = JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withSubject(userId.toHexString())
            .withClaim("email", email)
            .withClaim("type", "access")
            .withIssuedAt(now)
            .withExpiresAt(Date(now.time + accessTokenExpiration))
            .sign(algorithm)

        val refreshToken = JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withSubject(userId.toHexString())
            .withClaim("email", email)
            .withClaim("type", "refresh")
            .withIssuedAt(now)
            .withExpiresAt(Date(now.time + refreshTokenExpiration))
            .sign(algorithm)

        return TokenPair(accessToken, refreshToken, accessTokenExpiration / 1000)
    }

    fun verifyToken(token: String): DecodedJWT {
        return JWT.require(algorithm)
            .withAudience(audience)
            .withIssuer(issuer)
            .build()
            .verify(token)
    }

    fun decodeToken(token: String): JwtPayload? {
        return try {
            val decoded = verifyToken(token)
            JwtPayload(
                userId = decoded.subject,
                email = decoded.getClaim("email").asString()
            )
        } catch (e: Exception) {
            null
        }
    }
}
