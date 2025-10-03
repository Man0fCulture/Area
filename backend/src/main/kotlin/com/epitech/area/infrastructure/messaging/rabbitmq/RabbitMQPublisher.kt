package com.epitech.area.infrastructure.messaging.rabbitmq

import com.rabbitmq.client.Channel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

class RabbitMQPublisher(private val connection: RabbitMQConnection) {
    private val logger = LoggerFactory.getLogger(RabbitMQPublisher::class.java)
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        const val ACTION_QUEUE = "action_triggers"
        const val REACTION_QUEUE = "reaction_executions"
        const val NOTIFICATION_QUEUE = "notifications"
    }

    init {
        ensureQueuesExist()
    }

    private fun ensureQueuesExist() {
        val channel = connection.connection.createChannel()
        try {
            channel.queueDeclare(ACTION_QUEUE, true, false, false, null)
            channel.queueDeclare(REACTION_QUEUE, true, false, false, null)
            channel.queueDeclare(NOTIFICATION_QUEUE, true, false, false, null)
            logger.info("RabbitMQ queues declared: $ACTION_QUEUE, $REACTION_QUEUE, $NOTIFICATION_QUEUE")
        } finally {
            channel.close()
        }
    }

    fun publishActionTrigger(message: ActionTriggerMessage) {
        val channel = connection.connection.createChannel()
        try {
            val body = json.encodeToString(message).toByteArray()
            channel.basicPublish("", ACTION_QUEUE, null, body)
            logger.debug("Published action trigger: ${message.areaId}")
        } catch (e: Exception) {
            logger.error("Failed to publish action trigger", e)
            throw e
        } finally {
            channel.close()
        }
    }

    fun publishReactionExecution(message: ReactionExecutionMessage) {
        val channel = connection.connection.createChannel()
        try {
            val body = json.encodeToString(message).toByteArray()
            channel.basicPublish("", REACTION_QUEUE, null, body)
            logger.debug("Published reaction execution: ${message.areaId}")
        } catch (e: Exception) {
            logger.error("Failed to publish reaction execution", e)
            throw e
        } finally {
            channel.close()
        }
    }

    fun publishNotification(message: NotificationMessage) {
        val channel = connection.connection.createChannel()
        try {
            val body = json.encodeToString(message).toByteArray()
            channel.basicPublish("", NOTIFICATION_QUEUE, null, body)
            logger.debug("Published notification for user: ${message.userId}")
        } catch (e: Exception) {
            logger.error("Failed to publish notification", e)
            throw e
        } finally {
            channel.close()
        }
    }
}

@kotlinx.serialization.Serializable
data class ActionTriggerMessage(
    val areaId: String,
    val userId: String,
    val actionId: String,
    val triggerData: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

@kotlinx.serialization.Serializable
data class ReactionExecutionMessage(
    val areaId: String,
    val reactionId: String,
    val inputData: Map<String, String> = emptyMap(),
    val priority: String = "normal"
)

@kotlinx.serialization.Serializable
data class NotificationMessage(
    val userId: String,
    val type: String,
    val subject: String,
    val content: String,
    val metadata: Map<String, String> = emptyMap()
)
