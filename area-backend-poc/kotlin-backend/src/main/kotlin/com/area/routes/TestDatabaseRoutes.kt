package com.area.routes

import com.area.services.getDatabaseService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.temporal.ChronoUnit

@Serializable
data class TestData(
    val key: String,
    val value: String
)

@Serializable
data class MetricData(
    val name: String,
    val value: Double,
    val tags: Map<String, String> = emptyMap()
)

fun Route.testDatabaseRoutes() {
    route("/api/test-db") {

        // Test MongoDB
        post("/mongo") {
            val data = call.receive<TestData>()
            val service = application.getDatabaseService()

            val id = service.saveToMongo("test-collection", mapOf(data.key to data.value))
            call.respond(HttpStatusCode.Created, mapOf("id" to id, "database" to "MongoDB"))
        }

        get("/mongo/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val service = application.getDatabaseService()

            val document = service.getFromMongo("test-collection", id)
            if (document != null) {
                call.respond(mapOf("data" to document, "database" to "MongoDB"))
            } else {
                call.respond(HttpStatusCode.NotFound, "Document not found")
            }
        }

        get("/mongo") {
            val service = application.getDatabaseService()
            val documents = service.getAllFromMongo("test-collection")
            call.respond(mapOf("documents" to documents, "count" to documents.size, "database" to "MongoDB"))
        }

        // Test InfluxDB
        post("/influx") {
            val data = call.receive<MetricData>()
            val service = application.getDatabaseService()

            service.saveMetrics(
                measurement = "test-metrics",
                tags = data.tags + mapOf("source" to "api"),
                fields = mapOf(
                    data.name to data.value,
                    "timestamp" to System.currentTimeMillis()
                )
            )
            call.respond(HttpStatusCode.Created, mapOf("message" to "Metric saved", "database" to "InfluxDB"))
        }

        get("/influx/recent") {
            val service = application.getDatabaseService()
            val metrics = service.getRecentMetrics("test-metrics")
            call.respond(mapOf("metrics" to metrics, "count" to metrics.size, "database" to "InfluxDB"))
        }

        get("/influx/range") {
            val hours = call.request.queryParameters["hours"]?.toIntOrNull() ?: 24
            val service = application.getDatabaseService()

            val end = Instant.now()
            val start = end.minus(hours.toLong(), ChronoUnit.HOURS)

            val metrics = service.getMetricsByTimeRange("test-metrics", start, end)
            call.respond(mapOf(
                "metrics" to metrics,
                "count" to metrics.size,
                "timeRange" to mapOf("start" to start.toString(), "end" to end.toString()),
                "database" to "InfluxDB"
            ))
        }

        // Test all databases
        get("/status") {
            call.respond(mapOf(
                "status" to "All databases configured",
                "databases" to listOf(
                    "PostgreSQL (via Exposed ORM)",
                    "MongoDB (Document Store)",
                    "InfluxDB (Time Series)"
                )
            ))
        }
    }
}