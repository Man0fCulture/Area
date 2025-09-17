package com.area.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import java.time.LocalDateTime

@Serializable
data class MongoUser(
    @SerialName("_id") val id: String = ObjectId().toString(),
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val isActive: Boolean = true,
    val createdAt: String = LocalDateTime.now().toString(),
    val updatedAt: String = LocalDateTime.now().toString()
)