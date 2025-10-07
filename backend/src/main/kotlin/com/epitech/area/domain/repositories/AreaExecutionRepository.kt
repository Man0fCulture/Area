package com.epitech.area.domain.repositories

import com.epitech.area.domain.entities.AreaExecution
import com.epitech.area.domain.entities.ExecutionStatus
import org.bson.types.ObjectId

interface AreaExecutionRepository {
    suspend fun create(execution: AreaExecution): AreaExecution
    suspend fun findById(id: ObjectId): AreaExecution?
    suspend fun findByAreaId(areaId: ObjectId, limit: Int = 50): List<AreaExecution>
    suspend fun findByStatus(status: ExecutionStatus): List<AreaExecution>
    suspend fun update(execution: AreaExecution): Boolean
    suspend fun delete(id: ObjectId): Boolean
    suspend fun deleteByAreaId(areaId: ObjectId): Boolean
}
