package com.epitech.area.domain.repositories

import com.epitech.area.domain.entities.User
import org.bson.types.ObjectId

interface UserRepository {
    suspend fun create(user: User): User
    suspend fun findById(id: ObjectId): User?
    suspend fun findByEmail(email: String): User?
    suspend fun findByApiKey(apiKey: String): User?
    suspend fun update(user: User): User
    suspend fun delete(id: ObjectId): Boolean
    suspend fun existsByEmail(email: String): Boolean
}
