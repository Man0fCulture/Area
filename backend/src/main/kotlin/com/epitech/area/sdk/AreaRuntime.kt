package com.epitech.area.sdk

import com.epitech.area.domain.entities.UserService
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

/**
 * Runtime AREA - Abstraction de HTTP, OAuth, logging, etc.
 * Les développeurs de services utilisent uniquement ce runtime,
 * jamais directement HttpClient ou autres APIs bas niveau
 */
class AreaRuntime(
    private val httpClient: HttpClient
) {
    private val logger = LoggerFactory.getLogger(AreaRuntime::class.java)
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Effectuer une requête HTTP avec gestion automatique de :
     * - OAuth token
     * - Headers standards
     * - Logging
     * - Error handling
     */
    suspend fun request(
        url: String,
        method: HttpMethod = HttpMethod.Get,
        headers: Map<String, String> = emptyMap(),
        body: Any? = null,
        userService: UserService? = null
    ): HttpResponse {
        logger.debug("Request: $method $url")

        return httpClient.request(url) {
            this.method = method

            // Auto-inject OAuth token si disponible
            if (userService != null) {
                val accessToken = userService.credentials["accessToken"] as? String
                if (accessToken != null) {
                    header("Authorization", "Bearer $accessToken")
                }
            }

            // Headers custom
            headers.forEach { (key, value) ->
                header(key, value)
            }

            // Body si présent
            if (body != null) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }
    }

    /**
     * GET request simplifié
     */
    suspend fun get(
        url: String,
        headers: Map<String, String> = emptyMap(),
        userService: UserService? = null
    ): HttpResponse = request(url, HttpMethod.Get, headers, userService = userService)

    /**
     * POST request simplifié
     */
    suspend fun post(
        url: String,
        body: Any? = null,
        headers: Map<String, String> = emptyMap(),
        userService: UserService? = null
    ): HttpResponse = request(url, HttpMethod.Post, headers, body, userService)

    /**
     * PUT request simplifié
     */
    suspend fun put(
        url: String,
        body: Any? = null,
        headers: Map<String, String> = emptyMap(),
        userService: UserService? = null
    ): HttpResponse = request(url, HttpMethod.Put, headers, body, userService)

    /**
     * DELETE request simplifié
     */
    suspend fun delete(
        url: String,
        headers: Map<String, String> = emptyMap(),
        userService: UserService? = null
    ): HttpResponse = request(url, HttpMethod.Delete, headers, userService = userService)

    /**
     * Logger accessible aux services
     */
    fun log(message: String, level: LogLevel = LogLevel.INFO) {
        when (level) {
            LogLevel.DEBUG -> logger.debug(message)
            LogLevel.INFO -> logger.info(message)
            LogLevel.WARN -> logger.warn(message)
            LogLevel.ERROR -> logger.error(message)
        }
    }
}

enum class LogLevel {
    DEBUG, INFO, WARN, ERROR
}
