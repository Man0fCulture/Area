package com.epitech.area.infrastructure.persistence.redis

import org.slf4j.LoggerFactory
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

class RedisConnection(
    private val host: String,
    private val port: Int,
    private val password: String?
) {
    private val logger = LoggerFactory.getLogger(RedisConnection::class.java)
    private var _pool: JedisPool? = null

    val pool: JedisPool
        get() = _pool ?: throw IllegalStateException("Redis pool not initialized")

    fun connect() {
        try {
            logger.info("Connecting to Redis at $host:$port...")

            val poolConfig = JedisPoolConfig().apply {
                maxTotal = 128
                maxIdle = 128
                minIdle = 16
                testOnBorrow = true
                testOnReturn = true
                testWhileIdle = true
                blockWhenExhausted = true
                maxWaitMillis = 5000
            }

            _pool = if (password.isNullOrEmpty()) {
                JedisPool(poolConfig, host, port, 5000)
            } else {
                JedisPool(poolConfig, host, port, 5000, password)
            }

            _pool!!.resource.use { jedis ->
                val response = jedis.ping()
                if (response != "PONG") {
                    throw RuntimeException("Invalid Redis ping response: $response")
                }
            }

            logger.info("Successfully connected to Redis at $host:$port")
        } catch (e: Exception) {
            logger.error("Failed to connect to Redis: ${e.message}")
            throw RuntimeException("Redis connection failed: ${e.message}", e)
        }
    }

    fun close() {
        _pool?.close()
        logger.info("Redis connection pool closed")
    }

    companion object {
        private var instance: RedisConnection? = null

        fun getInstance(host: String, port: Int, password: String?): RedisConnection {
            if (instance == null) {
                instance = RedisConnection(host, port, password)
            }
            return instance!!
        }
    }
}
