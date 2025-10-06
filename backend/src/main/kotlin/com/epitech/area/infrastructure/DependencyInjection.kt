package com.epitech.area.infrastructure

import com.epitech.area.api.controllers.OAuth2Controller
import com.epitech.area.application.services.AuthService
import com.epitech.area.domain.repositories.*
import com.epitech.area.infrastructure.messaging.rabbitmq.*
import com.epitech.area.infrastructure.oauth.OAuth2Service
import com.epitech.area.infrastructure.persistence.mongodb.MongoDBConnection
import com.epitech.area.infrastructure.persistence.mongodb.*
import com.epitech.area.infrastructure.persistence.redis.CacheService
import com.epitech.area.infrastructure.persistence.redis.RedisConnection
import com.epitech.area.infrastructure.security.JwtService
import com.epitech.area.infrastructure.hooks.*
import com.epitech.area.infrastructure.integrations.ServiceAdapter
import com.epitech.area.infrastructure.integrations.services.productivity.TimerServiceAdapter
import com.epitech.area.infrastructure.integrations.services.webhook.WebhookServiceAdapter
import com.epitech.area.infrastructure.integrations.services.email.GmailServiceAdapter
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import kotlinx.serialization.json.Json

class DependencyContainer(private val application: Application) {
    val config = application.environment.config

    val mongoConnection: MongoDBConnection by lazy {
        val uri = config.property("database.mongodb.uri").getString()
        val database = config.property("database.mongodb.database").getString()
        MongoDBConnection.getInstance(uri, database)
    }

    val redisConnection: RedisConnection by lazy {
        val host = config.property("database.redis.host").getString()
        val port = config.property("database.redis.port").getString().toInt()
        val password = config.propertyOrNull("database.redis.password")?.getString()
        RedisConnection.getInstance(host, port, password)
    }

    val rabbitMQConnection: RabbitMQConnection by lazy {
        val host = config.property("messaging.rabbitmq.host").getString()
        val port = config.property("messaging.rabbitmq.port").getString().toInt()
        val username = config.property("messaging.rabbitmq.username").getString()
        val password = config.property("messaging.rabbitmq.password").getString()
        val virtualHost = config.property("messaging.rabbitmq.virtualHost").getString()
        RabbitMQConnection.getInstance(host, port, username, password, virtualHost)
    }

    val cacheService: CacheService by lazy {
        CacheService(redisConnection.pool)
    }

    val jwtService: JwtService by lazy {
        JwtService(
            secret = config.property("jwt.secret").getString(),
            issuer = config.property("jwt.issuer").getString(),
            audience = config.property("jwt.audience").getString(),
            accessTokenExpiration = config.property("jwt.accessTokenExpiration").getString().toLong(),
            refreshTokenExpiration = config.property("jwt.refreshTokenExpiration").getString().toLong()
        )
    }

    val userRepository: UserRepository by lazy {
        MongoUserRepository(mongoConnection.database)
    }

    val serviceRepository: ServiceRepository by lazy {
        MongoServiceRepository(mongoConnection.database)
    }

    val areaRepository: AreaRepository by lazy {
        MongoAreaRepository(mongoConnection.database)
    }

    val userServiceRepository: UserServiceRepository by lazy {
        MongoUserServiceRepository(mongoConnection.database)
    }

    val areaExecutionRepository: AreaExecutionRepository by lazy {
        MongoAreaExecutionRepository(mongoConnection.database)
    }

    val httpClient: HttpClient by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    val oAuth2Service: OAuth2Service by lazy {
        OAuth2Service(config, httpClient)
    }

    val authService: AuthService by lazy {
        AuthService(userRepository, jwtService)
    }

    val oAuth2Controller: OAuth2Controller by lazy {
        OAuth2Controller(oAuth2Service, authService, config)
    }

    val serviceAdapters: Map<String, ServiceAdapter> by lazy {
        mapOf(
            "timer" to TimerServiceAdapter(),
            "webhook" to WebhookServiceAdapter(),
            "gmail" to GmailServiceAdapter()
        )
    }

    val hookRegistry: HookRegistry by lazy {
        HookRegistry()
    }

    val hookProcessor: HookProcessor by lazy {
        HookProcessor(
            areaRepository = areaRepository,
            serviceRepository = serviceRepository,
            userServiceRepository = userServiceRepository,
            areaExecutionRepository = areaExecutionRepository,
            serviceAdapters = serviceAdapters
        )
    }

    val hookScheduler: HookScheduler by lazy {
        HookScheduler(
            areaRepository = areaRepository,
            serviceRepository = serviceRepository,
            hookProcessor = hookProcessor
        )
    }

    val rabbitMQPublisher: RabbitMQPublisher by lazy {
        RabbitMQPublisher(rabbitMQConnection)
    }

    val rabbitMQConsumer: RabbitMQConsumer by lazy {
        RabbitMQConsumer(
            connection = rabbitMQConnection,
            areaRepository = areaRepository,
            hookProcessor = hookProcessor
        )
    }

    val areaService: com.epitech.area.application.services.AreaService by lazy {
        com.epitech.area.application.services.AreaService(
            areaRepository = areaRepository,
            serviceRepository = serviceRepository,
            areaExecutionRepository = areaExecutionRepository,
            hookScheduler = hookScheduler
        )
    }
}
