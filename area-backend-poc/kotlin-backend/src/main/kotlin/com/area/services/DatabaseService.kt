package com.area.services

import com.area.database.DatabaseManager
import com.area.database.repositories.InfluxRepository
import com.area.database.repositories.MongoRepository
import com.area.database.repositories.TimeSeriesData
import com.area.database.repositories.MongoDocument
import io.ktor.server.application.*
import io.ktor.util.*
import kotlinx.coroutines.flow.toList
import java.time.Instant

class DatabaseService(private val databaseManager: DatabaseManager) {

    private val mongoDb = databaseManager.getMongoDatabase("area")
    private val mongoRepository = MongoRepository(mongoDb)

    private val influxRepository = InfluxRepository(
        databaseManager.influxClient,
        databaseManager.config.getString("database.influxdb.bucket"),
        databaseManager.config.getString("database.influxdb.org")
    )

    suspend fun saveToMongo(collection: String, data: Map<String, String>): String {
        val document = MongoDocument(data = data)
        return mongoRepository.saveDocument(collection, document)
    }

    suspend fun getFromMongo(collection: String, id: String): MongoDocument? {
        return mongoRepository.findDocument(collection, id)
    }

    suspend fun getAllFromMongo(collection: String): List<MongoDocument> {
        return mongoRepository.findAllDocuments(collection)
    }

    suspend fun saveMetrics(measurement: String, tags: Map<String, String>, fields: Map<String, Any>) {
        val data = TimeSeriesData(measurement, tags, fields)
        influxRepository.writePoint(data)
    }

    suspend fun getRecentMetrics(measurement: String): List<Map<String, Any>> {
        return influxRepository.queryLast24Hours(measurement).toList()
    }

    suspend fun getMetricsByTimeRange(
        measurement: String,
        start: Instant,
        stop: Instant,
        tags: Map<String, String> = emptyMap()
    ): List<Map<String, Any>> {
        return influxRepository.queryByTimeRange(measurement, start, stop, tags).toList()
    }
}

fun Application.getDatabaseService(): DatabaseService {
    val databaseManager = attributes[AttributeKey<DatabaseManager>("DatabaseManager")]
    return DatabaseService(databaseManager)
}
