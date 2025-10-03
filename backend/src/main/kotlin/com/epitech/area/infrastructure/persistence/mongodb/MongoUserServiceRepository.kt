package com.epitech.area.infrastructure.persistence.mongodb

import com.epitech.area.domain.entities.UserService
import com.epitech.area.domain.repositories.UserServiceRepository
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Indexes
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

class MongoUserServiceRepository(database: MongoDatabase) : UserServiceRepository {
    private val collection = database.getCollection<UserService>("user_services")

    init {
        kotlinx.coroutines.runBlocking {
            collection.createIndex(Indexes.ascending("userId"))
            collection.createIndex(Indexes.ascending("serviceId"))
        }
    }

    override suspend fun create(userService: UserService): UserService {
        collection.insertOne(userService)
        return userService
    }

    override suspend fun findById(id: ObjectId): UserService? {
        return collection.find(Filters.eq("_id", id)).firstOrNull()
    }

    override suspend fun findByUserIdAndServiceId(userId: ObjectId, serviceId: ObjectId): UserService? {
        return collection.find(
            Filters.and(
                Filters.eq("userId", userId),
                Filters.eq("serviceId", serviceId)
            )
        ).firstOrNull()
    }

    override suspend fun findByUserId(userId: ObjectId): List<UserService> {
        return collection.find(Filters.eq("userId", userId)).toList()
    }

    override suspend fun update(userService: UserService): UserService {
        collection.replaceOne(Filters.eq("_id", userService.id), userService)
        return userService
    }

    override suspend fun delete(id: ObjectId): Boolean {
        val result = collection.deleteOne(Filters.eq("_id", id))
        return result.deletedCount > 0
    }
}
