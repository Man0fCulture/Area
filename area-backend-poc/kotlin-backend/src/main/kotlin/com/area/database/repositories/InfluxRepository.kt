package com.area.database.repositories

import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.kotlin.InfluxDBClientKotlin
import com.influxdb.client.write.Point
import com.influxdb.query.FluxRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.temporal.ChronoUnit

data class TimeSeriesData(
    val measurement: String,
    val tags: Map<String, String>,
    val fields: Map<String, Any>,
    val timestamp: Long = System.currentTimeMillis()
)

class InfluxRepository(
    private val client: InfluxDBClientKotlin,
    private val bucket: String,
    private val org: String
) {

    suspend fun writePoint(data: TimeSeriesData) {
        val point = Point.measurement(data.measurement).apply {
            data.tags.forEach { (key, value) ->
                addTag(key, value)
            }
            data.fields.forEach { (key, value) ->
                when (value) {
                    is String -> addField(key, value)
                    is Long -> addField(key, value)
                    is Double -> addField(key, value)
                    is Boolean -> addField(key, value)
                    is Int -> addField(key, value.toLong())
                    else -> addField(key, value.toString())
                }
            }
            time(data.timestamp, WritePrecision.MS)
        }

        client.getWriteKotlinApi().writePoint(point, bucket, org)
    }

    suspend fun writeBatch(dataList: List<TimeSeriesData>) {
        val points = dataList.map { data ->
            Point.measurement(data.measurement).apply {
                data.tags.forEach { (key, value) ->
                    addTag(key, value)
                }
                data.fields.forEach { (key, value) ->
                    when (value) {
                        is String -> addField(key, value)
                        is Long -> addField(key, value)
                        is Double -> addField(key, value)
                        is Boolean -> addField(key, value)
                        is Int -> addField(key, value.toLong())
                        else -> addField(key, value.toString())
                    }
                }
                time(data.timestamp, WritePrecision.MS)
            }
        }

        client.getWriteKotlinApi().writePoints(points, bucket, org)
    }

    fun queryLast24Hours(measurement: String): Flow<Map<String, Any>> {
        val fluxQuery = """
            from(bucket: "$bucket")
              |> range(start: -24h)
              |> filter(fn: (r) => r._measurement == "$measurement")
        """.trimIndent()

        return client.getQueryKotlinApi()
            .query(fluxQuery, org)
            .map { record: FluxRecord ->
                mapOf(
                    "time" to record.time,
                    "measurement" to record.measurement,
                    "field" to record.field,
                    "value" to record.value
                )
            }
    }

    fun queryByTimeRange(
        measurement: String,
        start: Instant,
        stop: Instant,
        tags: Map<String, String> = emptyMap()
    ): Flow<Map<String, Any>> {
        val tagFilters = tags.entries.joinToString(" and ") { (key, value) ->
            "r.$key == \"$value\""
        }

        val fluxQuery = if (tagFilters.isNotEmpty()) {
            """
                from(bucket: "$bucket")
                  |> range(start: $start, stop: $stop)
                  |> filter(fn: (r) => r._measurement == "$measurement" and $tagFilters)
            """.trimIndent()
        } else {
            """
                from(bucket: "$bucket")
                  |> range(start: $start, stop: $stop)
                  |> filter(fn: (r) => r._measurement == "$measurement")
            """.trimIndent()
        }

        return client.getQueryKotlinApi()
            .query(fluxQuery, org)
            .map { record: FluxRecord ->
                mapOf(
                    "time" to record.time,
                    "measurement" to record.measurement,
                    "field" to record.field,
                    "value" to record.value,
                    "tags" to record.values
                )
            }
    }

    fun queryRaw(fluxQuery: String): Flow<Map<String, Any>> {
        return client.getQueryKotlinApi()
            .query(fluxQuery, org)
            .map { record: FluxRecord ->
                record.values
            }
    }

    suspend fun deleteByTimeRange(
        measurement: String,
        start: Instant,
        stop: Instant
    ) {
        val predicate = "_measurement=\"$measurement\""
        // Note: Delete API needs to be accessed from synchronous client
        // For now, we'll skip this functionality as it requires different client setup
        // You can implement this using the Java client if needed
    }
}