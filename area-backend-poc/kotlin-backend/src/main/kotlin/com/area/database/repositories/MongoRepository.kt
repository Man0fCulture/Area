package com.area.database.repositories

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class MongoDocument(
    val _id: String = ObjectId().toHexString(),
    val data: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

class MongoRepository(private val database: MongoDatabase) {

    suspend fun saveDocument(collectionName: String, document: MongoDocument): String {
        val collection = database.getCollection<MongoDocument>(collectionName)
        collection.insertOne(document)
        return document._id
    }

    suspend fun findDocument(collectionName: String, id: String): MongoDocument? {
        val collection = database.getCollection<MongoDocument>(collectionName)
        return collection.find(Filters.eq("_id", id)).firstOrNull()
    }

    suspend fun findAllDocuments(collectionName: String): List<MongoDocument> {
        val collection = database.getCollection<MongoDocument>(collectionName)
        return collection.find().toList()
    }

    suspend fun updateDocument(collectionName: String, id: String, document: MongoDocument): Boolean {
        val collection = database.getCollection<MongoDocument>(collectionName)
        val result = collection.replaceOne(Filters.eq("_id", id), document)
        return result.matchedCount > 0
    }

    suspend fun deleteDocument(collectionName: String, id: String): Boolean {
        val collection = database.getCollection<MongoDocument>(collectionName)
        val result = collection.deleteOne(Filters.eq("_id", id))
        return result.deletedCount > 0
    }

    suspend fun createIndex(collectionName: String, field: String) {
        val collection = database.getCollection<MongoDocument>(collectionName)
        collection.createIndex(org.bson.Document(field, 1))
    }
}