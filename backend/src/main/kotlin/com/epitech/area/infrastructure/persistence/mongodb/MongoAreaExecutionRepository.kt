package com.epitech.area.infrastructure.persistence.mongodb

import com.epitech.area.domain.entities.AreaExecution
import com.epitech.area.domain.entities.ExecutionStatus
import com.epitech.area.domain.repositories.AreaExecutionRepository
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Indexes
import com.mongodb.client.model.Sorts
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory

class MongoAreaExecutionRepository(database: MongoDatabase) : AreaExecutionRepository {
    private val logger = LoggerFactory.getLogger(MongoAreaExecutionRepository::class.java)
    private val collection = database.getCollection<AreaExecution>("area_executions")

    init {
        kotlinx.coroutines.runBlocking {
            collection.createIndex(Indexes.ascending("areaId"))
            collection.createIndex(Indexes.descending("startedAt"))
            collection.createIndex(Indexes.ascending("status"))
        }
    }

    override suspend fun create(execution: AreaExecution): AreaExecution {
        collection.insertOne(execution)
        logger.debug("Created area execution: ${execution.id}")
        return execution
    }

    override suspend fun findById(id: ObjectId): AreaExecution? {
        return collection.find(Filters.eq("_id", id)).firstOrNull()
    }

    override suspend fun findByAreaId(areaId: ObjectId, limit: Int): List<AreaExecution> {
        return collection.find(Filters.eq("areaId", areaId))
            .sort(Sorts.descending("startedAt"))
            .limit(limit)
            .toList()
    }

    override suspend fun findByStatus(status: ExecutionStatus): List<AreaExecution> {
        return collection.find(Filters.eq("status", status.name)).toList()
    }

    override suspend fun update(execution: AreaExecution): Boolean {
        val result = collection.replaceOne(Filters.eq("_id", execution.id), execution)
        return result.modifiedCount > 0
    }

    override suspend fun delete(id: ObjectId): Boolean {
        val result = collection.deleteOne(Filters.eq("_id", id))
        return result.deletedCount > 0
    }

    override suspend fun deleteByAreaId(areaId: ObjectId): Boolean {
        val result = collection.deleteMany(Filters.eq("areaId", areaId))
        return result.deletedCount > 0
    }
}
