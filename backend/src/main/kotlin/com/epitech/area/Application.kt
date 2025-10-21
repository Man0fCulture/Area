package com.epitech.area

import com.epitech.area.infrastructure.DependencyContainer
import com.epitech.area.infrastructure.ServiceInitializer
import com.epitech.area.infrastructure.messaging.rabbitmq.RabbitMQConnection
import com.epitech.area.infrastructure.persistence.mongodb.MongoDBConnection
import com.epitech.area.infrastructure.persistence.redis.RedisConnection
import com.epitech.area.infrastructure.plugins.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

private val logger = LoggerFactory.getLogger("Application")

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    initializeConnections()

    configureSerialization()
    configureMonitoring()
    configureStatusPages()
    configureSecurity()
    configureRouting()

    val container = DependencyContainer(this@module)

    runBlocking {
        try {
            val serviceInitializer = ServiceInitializer(
                container.serviceRepository,
                container.serviceRegistry
            )
            serviceInitializer.initializeServices()
            logger.info("✓ Services initialized successfully")
        } catch (e: Exception) {
            logger.error("Failed to initialize services", e)
        }

        try {
            container.hookScheduler.start()
            logger.info("Hook scheduler started")
        } catch (e: Exception) {
            logger.error("Failed to start hook scheduler", e)
        }

        try {
            container.rabbitMQConsumer.startConsuming()
            logger.info("RabbitMQ consumers started")
        } catch (e: Exception) {
            logger.error("Failed to start RabbitMQ consumers", e)
        }
    }

    environment.monitor.subscribe(ApplicationStopped) {
        shutdownHooks(container)
        shutdownConnections()
    }
}

private fun shutdownHooks(container: DependencyContainer) {
    logger.info("Shutting down hooks and consumers...")
    try {
        container.hookScheduler.stop()
    } catch (_: Exception) {}
    try {
        container.rabbitMQConsumer.stop()
    } catch (_: Exception) {}
    logger.info("Hooks and consumers stopped")
}

private fun Application.initializeConnections() {
    logger.info("Initializing external service connections...")

    try {
        val mongoUri = environment.config.property("database.mongodb.uri").getString()
        val mongoDatabase = environment.config.property("database.mongodb.database").getString()
        val mongoConnection = MongoDBConnection.getInstance(mongoUri, mongoDatabase)
        mongoConnection.connect()
    } catch (e: Exception) {
        logger.error("Failed to initialize MongoDB connection", e)
        exitProcess(1)
    }

    try {
        val redisHost = environment.config.property("database.redis.host").getString()
        val redisPort = environment.config.property("database.redis.port").getString().toInt()
        val redisPassword = environment.config.propertyOrNull("database.redis.password")?.getString()?.takeIf { it.isNotBlank() }
        val redisConnection = RedisConnection.getInstance(redisHost, redisPort, redisPassword)
        redisConnection.connect()
    } catch (e: Exception) {
        logger.error("Failed to initialize Redis connection", e)
        exitProcess(1)
    }

    try {
        val rabbitHost = environment.config.property("messaging.rabbitmq.host").getString()
        val rabbitPort = environment.config.property("messaging.rabbitmq.port").getString().toInt()
        val rabbitUsername = environment.config.property("messaging.rabbitmq.username").getString()
        val rabbitPassword = environment.config.property("messaging.rabbitmq.password").getString()
        val rabbitVirtualHost = environment.config.property("messaging.rabbitmq.virtualHost").getString()
        val rabbitConnection = RabbitMQConnection.getInstance(
            rabbitHost, rabbitPort, rabbitUsername, rabbitPassword, rabbitVirtualHost
        )
        rabbitConnection.connect()
    } catch (e: Exception) {
        logger.error("Failed to initialize RabbitMQ connection", e)
        exitProcess(1)
    }

    logger.info("All external service connections initialized successfully")
}

private fun shutdownConnections() {
    logger.info("Shutting down connections...")

    try {
        MongoDBConnection.getInstance("", "").close()
    } catch (_: Exception) {}

    try {
        RedisConnection.getInstance("", 0, null).close()
    } catch (_: Exception) {}

    try {
        RabbitMQConnection.getInstance("", 0, "", "", "").close()
    } catch (_: Exception) {}

    logger.info("Connections closed")
}
