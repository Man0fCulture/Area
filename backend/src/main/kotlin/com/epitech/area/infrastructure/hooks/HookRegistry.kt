package com.epitech.area.infrastructure.hooks

import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class HookRegistry {
    private val logger = LoggerFactory.getLogger(HookRegistry::class.java)
    private val webhookRegistry = ConcurrentHashMap<String, WebhookRegistration>()

    fun registerWebhook(serviceId: ObjectId, areaId: ObjectId, hookId: String): String {
        val webhookUrl = generateWebhookUrl(serviceId, hookId)
        val registration = WebhookRegistration(
            serviceId = serviceId,
            areaId = areaId,
            hookId = hookId,
            url = webhookUrl,
            createdAt = System.currentTimeMillis()
        )
        
        webhookRegistry[webhookUrl] = registration
        logger.info("Registered webhook: $webhookUrl for area $areaId")
        
        return webhookUrl
    }

    fun unregisterWebhook(webhookUrl: String) {
        webhookRegistry.remove(webhookUrl)
        logger.info("Unregistered webhook: $webhookUrl")
    }

    fun findByWebhookUrl(webhookUrl: String): WebhookRegistration? {
        return webhookRegistry[webhookUrl]
    }

    fun findByAreaId(areaId: ObjectId): List<WebhookRegistration> {
        return webhookRegistry.values.filter { it.areaId == areaId }
    }

    fun unregisterByAreaId(areaId: ObjectId) {
        val toRemove = webhookRegistry.entries.filter { it.value.areaId == areaId }
        toRemove.forEach { 
            webhookRegistry.remove(it.key)
            logger.info("Unregistered webhook: ${it.key} for area $areaId")
        }
    }

    private fun generateWebhookUrl(serviceId: ObjectId, hookId: String): String {
        return "/api/webhooks/${serviceId}/${hookId}"
    }

    fun getAllWebhooks(): List<WebhookRegistration> {
        return webhookRegistry.values.toList()
    }
}

data class WebhookRegistration(
    val serviceId: ObjectId,
    val areaId: ObjectId,
    val hookId: String,
    val url: String,
    val createdAt: Long
)
