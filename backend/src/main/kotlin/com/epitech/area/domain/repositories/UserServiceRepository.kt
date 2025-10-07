package com.epitech.area.domain.repositories

import com.epitech.area.domain.entities.UserService
import org.bson.types.ObjectId

interface UserServiceRepository {
    suspend fun create(userService: UserService): UserService
    suspend fun findById(id: ObjectId): UserService?
    suspend fun findByUserIdAndServiceId(userId: ObjectId, serviceId: ObjectId): UserService?
    suspend fun findByUserId(userId: ObjectId): List<UserService>
    suspend fun update(userService: UserService): UserService
    suspend fun delete(id: ObjectId): Boolean
}
