package com.area.database

import com.area.models.Users
import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.typesafe.config.Config
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

class DatabaseManager(val config: Config) {
    private val logger = LoggerFactory.getLogger(DatabaseManager::class.java)

    lateinit var postgresDatabase: Database
    lateinit var mongoClient: MongoClient
    lateinit var influxClient: com.influxdb.client.kotlin.InfluxDBClientKotlin

    private val postgresEnabled = config.getBoolean("database.postgresql.enabled")
    private val mongoEnabled = config.getBoolean("database.mongodb.enabled")
    private val influxEnabled = config.getBoolean("database.influxdb.enabled")

    fun init() {
        logger.info("Initializing database connections...")

        if (postgresEnabled) {
            initPostgreSQL()
        }

        if (mongoEnabled) {
            initMongoDB()
        }

        if (influxEnabled) {
            initInfluxDB()
        }

        logger.info("Database connections initialized successfully")
    }

    private fun initPostgreSQL() {
        logger.info("Connecting to PostgreSQL...")
        val dataSource = createHikariDataSource()
        postgresDatabase = Database.connect(dataSource)

        transaction {
            SchemaUtils.create(Users)
        }
        logger.info("PostgreSQL connected and tables created")
    }

    private fun createHikariDataSource(): HikariDataSource {
        val config = HikariConfig().apply {
            driverClassName = config.getString("database.postgresql.driver")
            jdbcUrl = config.getString("database.postgresql.url")
            username = config.getString("database.postgresql.user")
            password = config.getString("database.postgresql.password")
            maximumPoolSize = config.getInt("database.postgresql.maximumPoolSize")
            minimumIdle = config.getInt("database.postgresql.minimumIdle")
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        return HikariDataSource(config)
    }

    private fun initMongoDB() {
        logger.info("Connecting to MongoDB...")
        val uri = config.getString("database.mongodb.uri")
        mongoClient = MongoClient.create(uri)

        // Test connection
        runBlocking {
            val databaseName = config.getString("database.mongodb.database")
            val database = mongoClient.getDatabase(databaseName)
            database.listCollectionNames().collect { name ->
                logger.debug("Found collection: $name")
            }
        }
        logger.info("MongoDB connected successfully")
    }

    private fun initInfluxDB() {
        logger.info("Connecting to InfluxDB...")
        val url = config.getString("database.influxdb.url")
        val token = config.getString("database.influxdb.token").toCharArray()
        val org = config.getString("database.influxdb.org")
        val bucket = config.getString("database.influxdb.bucket")

        influxClient = InfluxDBClientKotlinFactory.create(url, token, org, bucket)

        // Test connection
        runBlocking {
            val health = influxClient.health()
            logger.info("InfluxDB health status: ${health.status}")
        }
        logger.info("InfluxDB connected successfully")
    }

    fun getMongoDatabase(name: String) = mongoClient.getDatabase(name)

    fun close() {
        logger.info("Closing database connections...")

        if (::mongoClient.isInitialized) {
            mongoClient.close()
        }

        if (::influxClient.isInitialized) {
            influxClient.close()
        }

        logger.info("Database connections closed")
    }
}