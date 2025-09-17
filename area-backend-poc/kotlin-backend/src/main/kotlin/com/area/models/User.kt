package com.area.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

// Table definition for PostgreSQL
object Users : Table() {
    val id = integer("id").autoIncrement()
    val email = varchar("email", 255).uniqueIndex()
    val password = varchar("password", 255)
    val firstName = varchar("first_name", 100)
    val lastName = varchar("last_name", 100)
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}

// Data classes
@Serializable
data class User(
    val id: Int = 0,
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String
)

@Serializable
data class UserRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserResponse
)