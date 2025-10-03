package com.epitech.area.infrastructure.messaging.rabbitmq

import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import org.slf4j.LoggerFactory

class RabbitMQConnection(
    private val host: String,
    private val port: Int,
    private val username: String,
    private val password: String,
    private val virtualHost: String
) {
    private val logger = LoggerFactory.getLogger(RabbitMQConnection::class.java)
    private var _connection: Connection? = null

    val connection: Connection
        get() = _connection ?: throw IllegalStateException("RabbitMQ connection not initialized")

    fun connect() {
        try {
            logger.info("Connecting to RabbitMQ at $host:$port...")

            val factory = ConnectionFactory().apply {
                this.host = this@RabbitMQConnection.host
                this.port = this@RabbitMQConnection.port
                this.username = this@RabbitMQConnection.username
                this.password = this@RabbitMQConnection.password
                this.virtualHost = this@RabbitMQConnection.virtualHost
                connectionTimeout = 5000
                requestedHeartbeat = 60
                isAutomaticRecoveryEnabled = true
                networkRecoveryInterval = 10000
            }

            _connection = factory.newConnection()

            if (!_connection!!.isOpen) {
                throw RuntimeException("RabbitMQ connection is not open")
            }

            _connection!!.createChannel().use { channel ->
                if (!channel.isOpen) {
                    throw RuntimeException("Failed to create RabbitMQ channel")
                }
            }

            logger.info("Successfully connected to RabbitMQ at $host:$port (vhost: $virtualHost)")
        } catch (e: Exception) {
            logger.error("Failed to connect to RabbitMQ: ${e.message}")
            throw RuntimeException("RabbitMQ connection failed: ${e.message}", e)
        }
    }

    fun close() {
        _connection?.close()
        logger.info("RabbitMQ connection closed")
    }

    companion object {
        private var instance: RabbitMQConnection? = null

        fun getInstance(
            host: String,
            port: Int,
            username: String,
            password: String,
            virtualHost: String
        ): RabbitMQConnection {
            if (instance == null) {
                instance = RabbitMQConnection(host, port, username, password, virtualHost)
            }
            return instance!!
        }
    }
}
