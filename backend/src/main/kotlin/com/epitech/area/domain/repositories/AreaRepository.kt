package com.epitech.area.domain.repositories

import com.epitech.area.domain.entities.Area
import org.bson.types.ObjectId

interface AreaRepository {
    suspend fun create(area: Area): Area
    suspend fun findById(id: ObjectId): Area?
    suspend fun findByUserId(userId: ObjectId): List<Area>
    suspend fun findActiveAreas(): List<Area>
    suspend fun findActiveAreasByUserId(userId: ObjectId): List<Area>
    suspend fun update(area: Area): Area
    suspend fun delete(id: ObjectId): Boolean
    suspend fun updateLastTriggered(id: ObjectId, timestamp: Long): Boolean
    suspend fun incrementExecutionCount(id: ObjectId): Boolean
}
