package com.epitech.area.infrastructure.persistence.mongodb

import com.epitech.area.domain.entities.Area
import com.epitech.area.domain.repositories.AreaRepository
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Indexes
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

class MongoAreaRepository(database: MongoDatabase) : AreaRepository {
    private val collection = database.getCollection<Area>("areas")

    init {
        kotlinx.coroutines.runBlocking {
            collection.createIndex(Indexes.ascending("userId"))
            collection.createIndex(Indexes.ascending("active"))
        }
    }

    override suspend fun create(area: Area): Area {
        collection.insertOne(area)
        return area
    }

    override suspend fun findById(id: ObjectId): Area? {
        return collection.find(Filters.eq("_id", id)).firstOrNull()
    }

    override suspend fun findByUserId(userId: ObjectId): List<Area> {
        return collection.find(Filters.eq("userId", userId)).toList()
    }

    override suspend fun findActiveAreas(): List<Area> {
        return collection.find(Filters.eq("active", true)).toList()
    }

    override suspend fun findActiveAreasByUserId(userId: ObjectId): List<Area> {
        return collection.find(
            Filters.and(
                Filters.eq("userId", userId),
                Filters.eq("active", true)
            )
        ).toList()
    }

    override suspend fun update(area: Area): Area {
        collection.replaceOne(Filters.eq("_id", area.id), area)
        return area
    }

    override suspend fun delete(id: ObjectId): Boolean {
        val result = collection.deleteOne(Filters.eq("_id", id))
        return result.deletedCount > 0
    }

    override suspend fun updateLastTriggered(id: ObjectId, timestamp: Long): Boolean {
        val result = collection.updateOne(
            Filters.eq("_id", id),
            Updates.set("lastTriggeredAt", timestamp)
        )
        return result.modifiedCount > 0
    }

    override suspend fun incrementExecutionCount(id: ObjectId): Boolean {
        val result = collection.updateOne(
            Filters.eq("_id", id),
            Updates.inc("executionCount", 1)
        )
        return result.modifiedCount > 0
    }
}
