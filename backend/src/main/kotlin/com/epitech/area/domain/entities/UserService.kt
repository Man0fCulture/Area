package com.epitech.area.domain.entities

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class UserService(
    @BsonId
    val id: ObjectId = ObjectId(),
    val userId: ObjectId,
    val serviceId: ObjectId,
    val serviceName: String,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val expiresAt: Long? = null,
    val credentials: Map<String, String> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
