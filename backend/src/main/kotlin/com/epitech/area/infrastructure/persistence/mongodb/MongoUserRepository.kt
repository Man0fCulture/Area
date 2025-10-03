package com.epitech.area.infrastructure.persistence.mongodb

import com.epitech.area.domain.entities.User
import com.epitech.area.domain.repositories.UserRepository
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Indexes
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import org.bson.types.ObjectId

class MongoUserRepository(database: MongoDatabase) : UserRepository {
    private val collection = database.getCollection<User>("users")

    init {
        kotlinx.coroutines.runBlocking {
            collection.createIndex(Indexes.ascending("email"))
            collection.createIndex(Indexes.ascending("apiKeys"))
        }
    }

    override suspend fun create(user: User): User {
        collection.insertOne(user)
        return user
    }

    override suspend fun findById(id: ObjectId): User? {
        return collection.find(Filters.eq("_id", id)).firstOrNull()
    }

    override suspend fun findByEmail(email: String): User? {
        return collection.find(Filters.eq("email", email)).firstOrNull()
    }

    override suspend fun findByApiKey(apiKey: String): User? {
        return collection.find(Filters.eq("apiKeys", apiKey)).firstOrNull()
    }

    override suspend fun update(user: User): User {
        collection.replaceOne(Filters.eq("_id", user.id), user)
        return user
    }

    override suspend fun delete(id: ObjectId): Boolean {
        val result = collection.deleteOne(Filters.eq("_id", id))
        return result.deletedCount > 0
    }

    override suspend fun existsByEmail(email: String): Boolean {
        return collection.countDocuments(Filters.eq("email", email)) > 0
    }
}
