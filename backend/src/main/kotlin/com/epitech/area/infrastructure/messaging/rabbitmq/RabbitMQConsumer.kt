package com.epitech.area.infrastructure.messaging.rabbitmq

import com.epitech.area.infrastructure.hooks.HookProcessor
import com.epitech.area.domain.repositories.AreaRepository
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DeliverCallback
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory

class RabbitMQConsumer(
    private val connection: RabbitMQConnection,
    private val areaRepository: AreaRepository,
    private val hookProcessor: HookProcessor
) {
    private val logger = LoggerFactory.getLogger(RabbitMQConsumer::class.java)
    private val json = Json { ignoreUnknownKeys = true }
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private var actionChannel: Channel? = null
    private var reactionChannel: Channel? = null
    private var notificationChannel: Channel? = null

    fun startConsuming() {
        logger.info("Starting RabbitMQ consumers...")

        // Ensure queues exist before trying to consume
        ensureQueuesExist()

        startActionConsumer()
        startReactionConsumer()
        startNotificationConsumer()

        logger.info("All RabbitMQ consumers started")
    }

    private fun ensureQueuesExist() {
        val channel = connection.connection.createChannel()
        try {
            channel.queueDeclare(RabbitMQPublisher.ACTION_QUEUE, true, false, false, null)
            channel.queueDeclare(RabbitMQPublisher.REACTION_QUEUE, true, false, false, null)
            channel.queueDeclare(RabbitMQPublisher.NOTIFICATION_QUEUE, true, false, false, null)
            logger.info("RabbitMQ queues ensured: ${RabbitMQPublisher.ACTION_QUEUE}, ${RabbitMQPublisher.REACTION_QUEUE}, ${RabbitMQPublisher.NOTIFICATION_QUEUE}")
        } catch (e: Exception) {
            logger.error("Failed to declare queues", e)
            throw e
        } finally {
            channel.close()
        }
    }

    private fun startActionConsumer() {
        actionChannel = connection.connection.createChannel()
        actionChannel?.basicQos(1)
        
        val deliverCallback = DeliverCallback { _, delivery ->
            scope.launch {
                try {
                    val message = json.decodeFromString<ActionTriggerMessage>(String(delivery.body))
                    logger.info("Received action trigger for area: ${message.areaId}")
                    
                    val area = areaRepository.findById(ObjectId(message.areaId))
                    if (area != null) {
                        hookProcessor.processArea(area)
                        actionChannel?.basicAck(delivery.envelope.deliveryTag, false)
                    } else {
                        logger.warn("Area not found: ${message.areaId}")
                        actionChannel?.basicNack(delivery.envelope.deliveryTag, false, false)
                    }
                } catch (e: Exception) {
                    logger.error("Error processing action trigger", e)
                    actionChannel?.basicNack(delivery.envelope.deliveryTag, false, true)
                }
            }
        }
        
        actionChannel?.basicConsume(RabbitMQPublisher.ACTION_QUEUE, false, deliverCallback) { _ -> }
        logger.info("Action consumer started on queue: ${RabbitMQPublisher.ACTION_QUEUE}")
    }

    private fun startReactionConsumer() {
        reactionChannel = connection.connection.createChannel()
        reactionChannel?.basicQos(1)
        
        val deliverCallback = DeliverCallback { _, delivery ->
            scope.launch {
                try {
                    val message = json.decodeFromString<ReactionExecutionMessage>(String(delivery.body))
                    logger.info("Received reaction execution for area: ${message.areaId}")
                    
                    reactionChannel?.basicAck(delivery.envelope.deliveryTag, false)
                } catch (e: Exception) {
                    logger.error("Error processing reaction execution", e)
                    reactionChannel?.basicNack(delivery.envelope.deliveryTag, false, true)
                }
            }
        }
        
        reactionChannel?.basicConsume(RabbitMQPublisher.REACTION_QUEUE, false, deliverCallback) { _ -> }
        logger.info("Reaction consumer started on queue: ${RabbitMQPublisher.REACTION_QUEUE}")
    }

    private fun startNotificationConsumer() {
        notificationChannel = connection.connection.createChannel()
        notificationChannel?.basicQos(1)
        
        val deliverCallback = DeliverCallback { _, delivery ->
            scope.launch {
                try {
                    val message = json.decodeFromString<NotificationMessage>(String(delivery.body))
                    logger.info("Received notification for user: ${message.userId}")
                    
                    notificationChannel?.basicAck(delivery.envelope.deliveryTag, false)
                } catch (e: Exception) {
                    logger.error("Error processing notification", e)
                    notificationChannel?.basicNack(delivery.envelope.deliveryTag, false, true)
                }
            }
        }
        
        notificationChannel?.basicConsume(RabbitMQPublisher.NOTIFICATION_QUEUE, false, deliverCallback) { _ -> }
        logger.info("Notification consumer started on queue: ${RabbitMQPublisher.NOTIFICATION_QUEUE}")
    }

    fun stop() {
        logger.info("Stopping RabbitMQ consumers...")
        actionChannel?.close()
        reactionChannel?.close()
        notificationChannel?.close()
        scope.cancel()
        logger.info("All RabbitMQ consumers stopped")
    }
}
