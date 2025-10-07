package com.epitech.area.domain.entities

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class User(
    @BsonId
    val id: ObjectId = ObjectId(),
    val email: String,
    val passwordHash: String? = null,
    val username: String,
    val oauthProviders: Map<String, OAuthProvider> = emptyMap(),
    val apiKeys: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class OAuthProvider(
    val provider: String,
    val providerId: String,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val expiresAt: Long? = null
)
