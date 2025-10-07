package com.epitech.area.infrastructure.persistence.redis

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import redis.clients.jedis.JedisPool

class CacheService(private val jedisPool: JedisPool) {
    @PublishedApi
    internal val logger = LoggerFactory.getLogger(CacheService::class.java)
    @PublishedApi
    internal val json = Json { ignoreUnknownKeys = true }

    fun <T> get(key: String, transform: (String) -> T): T? {
        return try {
            jedisPool.resource.use { jedis ->
                val value = jedis.get(key)
                value?.let { transform(it) }
            }
        } catch (e: Exception) {
            logger.error("Redis GET error for key: $key", e)
            null
        }
    }

    fun set(key: String, value: String, ttlSeconds: Int? = null): Boolean {
        return try {
            jedisPool.resource.use { jedis ->
                if (ttlSeconds != null) {
                    jedis.setex(key, ttlSeconds.toLong(), value)
                } else {
                    jedis.set(key, value)
                }
                true
            }
        } catch (e: Exception) {
            logger.error("Redis SET error for key: $key", e)
            false
        }
    }

    inline fun <reified T> setJson(key: String, value: T, ttlSeconds: Int? = null): Boolean {
        val jsonValue = json.encodeToString(value)
        return set(key, jsonValue, ttlSeconds)
    }

    inline fun <reified T> getJson(key: String): T? {
        val value = get(key) { it } ?: return null
        return try {
            json.decodeFromString<T>(value)
        } catch (e: Exception) {
            logger.error("Redis JSON decode error for key: $key", e)
            null
        }
    }

    fun delete(key: String): Boolean {
        return try {
            jedisPool.resource.use { jedis ->
                jedis.del(key) > 0
            }
        } catch (e: Exception) {
            logger.error("Redis DELETE error for key: $key", e)
            false
        }
    }

    fun exists(key: String): Boolean {
        return try {
            jedisPool.resource.use { jedis ->
                jedis.exists(key)
            }
        } catch (e: Exception) {
            logger.error("Redis EXISTS error for key: $key", e)
            false
        }
    }

    fun deleteByPattern(pattern: String): Long {
        return try {
            jedisPool.resource.use { jedis ->
                val keys = jedis.keys(pattern)
                if (keys.isNotEmpty()) {
                    jedis.del(*keys.toTypedArray())
                } else {
                    0
                }
            }
        } catch (e: Exception) {
            logger.error("Redis DELETE pattern error for: $pattern", e)
            0
        }
    }
}
