package com.area

import com.area.database.DatabaseManager
import com.area.routes.configureAuth
import com.area.routes.configureMain
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.http.*
import com.typesafe.config.ConfigFactory
import io.ktor.util.*

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080

    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    // Load configuration
    val appConfig = ConfigFactory.load()

    // Initialize all databases
    val databaseManager = DatabaseManager(appConfig)
    databaseManager.init()

    // Store database manager in application attributes for use in routes
    attributes.put(AttributeKey<DatabaseManager>("DatabaseManager"), databaseManager)

    // Register shutdown hook
    environment.monitor.subscribe(ApplicationStopped) {
        databaseManager.close()
    }

    // Configure CORS
    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
    }

    // Configure JSON
    install(ContentNegotiation) {
        json()
    }

    // Configure routes
    configureMain()
    configureAuth()
}