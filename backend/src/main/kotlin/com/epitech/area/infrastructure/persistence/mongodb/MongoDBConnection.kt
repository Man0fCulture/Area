package com.epitech.area.infrastructure.persistence.mongodb

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.runBlocking
import org.bson.Document
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class MongoDBConnection(
    private val uri: String,
    private val databaseName: String
) {
    private val logger = LoggerFactory.getLogger(MongoDBConnection::class.java)
    private var _client: MongoClient? = null
    private var _database: MongoDatabase? = null

    val client: MongoClient
        get() = _client ?: throw IllegalStateException("MongoDB client not initialized")

    val database: MongoDatabase
        get() = _database ?: throw IllegalStateException("MongoDB database not initialized")

    fun connect() {
        try {
            logger.info("Connecting to MongoDB at $uri...")

            val settings = MongoClientSettings.builder()
                .applyConnectionString(ConnectionString(uri))
                .applyToSocketSettings { builder ->
                    builder.connectTimeout(5, TimeUnit.SECONDS)
                    builder.readTimeout(5, TimeUnit.SECONDS)
                }
                .applyToClusterSettings { builder ->
                    builder.serverSelectionTimeout(5, TimeUnit.SECONDS)
                }
                .build()

            _client = MongoClient.create(settings)
            _database = _client!!.getDatabase(databaseName)

            runBlocking {
                _database!!.runCommand(Document("ping", 1))
            }

            logger.info("Successfully connected to MongoDB database: $databaseName")
        } catch (e: Exception) {
            logger.error("Failed to connect to MongoDB: ${e.message}")
            throw RuntimeException("MongoDB connection failed: ${e.message}", e)
        }
    }

    fun close() {
        _client?.close()
        logger.info("MongoDB connection closed")
    }

    companion object {
        private var instance: MongoDBConnection? = null

        fun getInstance(uri: String, databaseName: String): MongoDBConnection {
            if (instance == null) {
                instance = MongoDBConnection(uri, databaseName)
            }
            return instance!!
        }
    }
}
