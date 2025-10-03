package com.epitech.area.infrastructure.hooks

import com.epitech.area.domain.entities.*
import com.epitech.area.domain.repositories.*
import com.epitech.area.infrastructure.integrations.ServiceAdapter
import org.slf4j.LoggerFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HookProcessor(
    private val areaRepository: AreaRepository,
    private val serviceRepository: ServiceRepository,
    private val userServiceRepository: UserServiceRepository,
    private val areaExecutionRepository: AreaExecutionRepository,
    private val serviceAdapters: Map<String, ServiceAdapter>
) {
    private val logger = LoggerFactory.getLogger(HookProcessor::class.java)

    suspend fun processArea(area: Area): ProcessResult = withContext(Dispatchers.IO) {
        logger.info("Processing area ${area.id} - ${area.name}")
        
        if (!area.active) {
            logger.warn("Area ${area.id} is inactive, skipping")
            return@withContext ProcessResult(success = false, error = "Area is inactive")
        }

        val execution = AreaExecution(
            areaId = area.id,
            status = ExecutionStatus.PENDING,
            startedAt = System.currentTimeMillis()
        )
        areaExecutionRepository.create(execution)

        try {
            val actionResult = executeAction(area)
            
            if (!actionResult.success) {
                logger.warn("Action failed for area ${area.id}: ${actionResult.error}")
                updateExecutionStatus(
                    execution.id,
                    ExecutionStatus.FAILED,
                    error = actionResult.error
                )
                return@withContext ProcessResult(success = false, error = actionResult.error)
            }

            updateExecutionStatus(execution.id, ExecutionStatus.PROCESSING, actionData = actionResult.data)

            var allReactionsSucceeded = true
            val reactionErrors = mutableListOf<String>()

            for ((index, reaction) in area.reactions.withIndex()) {
                logger.info("Executing reaction ${index + 1}/${area.reactions.size} for area ${area.id}")
                
                val reactionResult = executeReaction(reaction, actionResult.data, area.userId)
                
                if (!reactionResult.success) {
                    logger.error("Reaction ${index + 1} failed: ${reactionResult.error}")
                    allReactionsSucceeded = false
                    reactionErrors.add("Reaction ${index + 1}: ${reactionResult.error}")
                } else {
                    logger.info("Reaction ${index + 1} succeeded")
                }
            }

            if (allReactionsSucceeded) {
                updateExecutionStatus(execution.id, ExecutionStatus.SUCCESS)
                updateAreaStats(area.id)
                logger.info("Area ${area.id} executed successfully")
                ProcessResult(success = true, message = "Area executed successfully")
            } else {
                updateExecutionStatus(
                    execution.id,
                    ExecutionStatus.FAILED,
                    error = reactionErrors.joinToString("; ")
                )
                ProcessResult(success = false, error = reactionErrors.joinToString("; "))
            }

        } catch (e: Exception) {
            logger.error("Unexpected error processing area ${area.id}", e)
            updateExecutionStatus(execution.id, ExecutionStatus.FAILED, error = e.message)
            ProcessResult(success = false, error = e.message ?: "Unknown error")
        }
    }

    private suspend fun executeAction(area: Area): ActionExecutionResult {
        val service = serviceRepository.findById(area.action.serviceId)
            ?: return ActionExecutionResult(false, error = "Service not found")

        val adapter = serviceAdapters[service.name.lowercase()]
            ?: return ActionExecutionResult(false, error = "Service adapter not found for ${service.name}")

        val userService = if (service.requiresAuth) {
            userServiceRepository.findByUserIdAndServiceId(area.userId, area.action.serviceId)
                ?: return ActionExecutionResult(false, error = "User not connected to ${service.name}")
        } else {
            null
        }

        return try {
            val result = adapter.executeAction(
                area.action.actionId,
                area.action.config,
                userService
            )
            
            ActionExecutionResult(
                success = result.success,
                data = result.data,
                error = result.error
            )
        } catch (e: Exception) {
            logger.error("Action execution failed", e)
            ActionExecutionResult(false, error = "Action execution failed: ${e.message}")
        }
    }

    private suspend fun executeReaction(
        reaction: ReactionConfig,
        actionData: Map<String, Any>,
        userId: org.bson.types.ObjectId
    ): ReactionExecutionResult {
        val service = serviceRepository.findById(reaction.serviceId)
            ?: return ReactionExecutionResult(false, error = "Service not found")

        val adapter = serviceAdapters[service.name.lowercase()]
            ?: return ReactionExecutionResult(false, error = "Service adapter not found for ${service.name}")

        val userService = if (service.requiresAuth) {
            userServiceRepository.findByUserIdAndServiceId(userId, reaction.serviceId)
                ?: return ReactionExecutionResult(false, error = "User not connected to ${service.name}")
        } else {
            null
        }

        return try {
            val result = adapter.executeReaction(
                reaction.reactionId,
                reaction.config,
                actionData,
                userService
            )
            
            ReactionExecutionResult(
                success = result.success,
                message = result.message,
                error = result.error
            )
        } catch (e: Exception) {
            logger.error("Reaction execution failed", e)
            ReactionExecutionResult(false, error = "Reaction execution failed: ${e.message}")
        }
    }

    private suspend fun updateExecutionStatus(
        executionId: org.bson.types.ObjectId,
        status: ExecutionStatus,
        actionData: Map<String, Any>? = null,
        error: String? = null
    ) {
        val execution = areaExecutionRepository.findById(executionId) ?: return
        
        val updated = execution.copy(
            status = status,
            actionData = actionData ?: execution.actionData,
            error = error,
            completedAt = if (status == ExecutionStatus.SUCCESS || status == ExecutionStatus.FAILED) {
                System.currentTimeMillis()
            } else {
                null
            }
        )
        
        areaExecutionRepository.update(updated)
    }

    private suspend fun updateAreaStats(areaId: org.bson.types.ObjectId) {
        val area = areaRepository.findById(areaId) ?: return
        
        val updated = area.copy(
            lastTriggeredAt = System.currentTimeMillis(),
            executionCount = area.executionCount + 1,
            updatedAt = System.currentTimeMillis()
        )
        
        areaRepository.update(updated)
    }

    suspend fun checkTriggerCondition(area: Area): Boolean {
        val service = serviceRepository.findById(area.action.serviceId) ?: return false
        val adapter = serviceAdapters[service.name.lowercase()] ?: return false

        val userService = if (service.requiresAuth) {
            userServiceRepository.findByUserIdAndServiceId(area.userId, area.action.serviceId)
        } else {
            null
        }

        return try {
            val result = adapter.executeAction(area.action.actionId, area.action.config, userService)
            result.success
        } catch (e: Exception) {
            logger.error("Failed to check trigger condition for area ${area.id}", e)
            false
        }
    }
}

data class ProcessResult(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)

data class ActionExecutionResult(
    val success: Boolean,
    val data: Map<String, Any> = emptyMap(),
    val error: String? = null
)

data class ReactionExecutionResult(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)
