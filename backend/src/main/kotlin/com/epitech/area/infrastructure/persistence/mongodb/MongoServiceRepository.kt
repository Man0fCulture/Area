package com.epitech.area.infrastructure.persistence.mongodb

import com.epitech.area.domain.entities.Service
import com.epitech.area.domain.repositories.ServiceRepository
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Indexes
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

class MongoServiceRepository(database: MongoDatabase) : ServiceRepository {
    private val collection = database.getCollection<Service>("services")

    init {
        kotlinx.coroutines.runBlocking {
            collection.createIndex(Indexes.ascending("name"))
        }
    }

    override suspend fun create(service: Service): Service {
        collection.insertOne(service)
        return service
    }

    override suspend fun findById(id: ObjectId): Service? {
        return collection.find(Filters.eq("_id", id)).firstOrNull()
    }

    override suspend fun findByName(name: String): Service? {
        return collection.find(Filters.eq("name", name)).firstOrNull()
    }

    override suspend fun findAll(): List<Service> {
        return collection.find().toList()
    }

    override suspend fun findAllEnabled(): List<Service> {
        return collection.find(Filters.eq("enabled", true)).toList()
    }

    override suspend fun update(service: Service): Service {
        collection.replaceOne(Filters.eq("_id", service.id), service)
        return service
    }

    override suspend fun delete(id: ObjectId): Boolean {
        val result = collection.deleteOne(Filters.eq("_id", id))
        return result.deletedCount > 0
    }
}
