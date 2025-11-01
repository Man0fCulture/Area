package com.epitech.area.infrastructure.hooks

import com.epitech.area.domain.entities.*
import com.epitech.area.domain.repositories.*
import com.epitech.area.infrastructure.integrations.ServiceAdapter
import org.bson.Document
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

        val totalSteps = 1 + area.reactions.size
        val execution = AreaExecution(
            areaId = area.id,
            status = ExecutionStatus.PENDING,
            startedAt = System.currentTimeMillis(),
            totalSteps = totalSteps,
            progress = 0
        )
        val savedExecution = areaExecutionRepository.create(execution)

        try {
            updateExecutionWithStep(
                savedExecution.id,
                ExecutionStatus.IN_PROGRESS,
                ExecutionStep(StepType.ACTION, "Executing action", 0, totalSteps),
                0
            )

            val actionStartTime = System.currentTimeMillis()
            val actionResult = executeAction(area)
            val actionEndTime = System.currentTimeMillis()

            val actionStepRecord = ExecutionStepRecord(
                stepType = StepType.ACTION,
                stepName = "Action: ${area.action.actionId}",
                stepIndex = 0,
                status = if (actionResult.success) ExecutionStatus.SUCCESS else ExecutionStatus.FAILED,
                startedAt = actionStartTime,
                completedAt = actionEndTime,
                data = actionResult.data,
                error = actionResult.error,
                duration = actionEndTime - actionStartTime
            )

            if (!actionResult.success) {
                logger.warn("Action failed for area ${area.id}: ${actionResult.error}")
                updateExecutionWithStepComplete(
                    savedExecution.id,
                    ExecutionStatus.FAILED,
                    actionStepRecord,
                    error = actionResult.error
                )
                return@withContext ProcessResult(success = false, error = actionResult.error)
            }

            updateExecutionWithStepComplete(
                savedExecution.id,
                ExecutionStatus.PROCESSING,
                actionStepRecord,
                actionData = actionResult.data
            )

            var allReactionsSucceeded = true
            val reactionErrors = mutableListOf<String>()

            for ((index, reaction) in area.reactions.withIndex()) {
                val stepIndex = index + 1
                val progress = ((stepIndex.toDouble() / totalSteps) * 100).toInt()

                logger.info("Executing reaction ${stepIndex}/${area.reactions.size} for area ${area.id}")

                updateExecutionWithStep(
                    savedExecution.id,
                    ExecutionStatus.IN_PROGRESS,
                    ExecutionStep(StepType.REACTION, "Reaction: ${reaction.reactionId}", stepIndex, totalSteps),
                    progress
                )

                val reactionStartTime = System.currentTimeMillis()
                val reactionResult = executeReaction(reaction, actionResult.data, area.userId)
                val reactionEndTime = System.currentTimeMillis()

                val reactionStepRecord = ExecutionStepRecord(
                    stepType = StepType.REACTION,
                    stepName = "Reaction ${stepIndex}: ${reaction.reactionId}",
                    stepIndex = stepIndex,
                    status = if (reactionResult.success) ExecutionStatus.SUCCESS else ExecutionStatus.FAILED,
                    startedAt = reactionStartTime,
                    completedAt = reactionEndTime,
                    data = if (reactionResult.message != null) Document("message", reactionResult.message) else null,
                    error = reactionResult.error,
                    duration = reactionEndTime - reactionStartTime
                )

                if (!reactionResult.success) {
                    logger.error("Reaction ${stepIndex} failed: ${reactionResult.error}")
                    allReactionsSucceeded = false
                    reactionErrors.add("Reaction ${stepIndex}: ${reactionResult.error}")
                } else {
                    logger.info("Reaction ${stepIndex} succeeded")
                }

                updateExecutionWithStepComplete(
                    savedExecution.id,
                    if (allReactionsSucceeded) ExecutionStatus.PROCESSING else ExecutionStatus.IN_PROGRESS,
                    reactionStepRecord
                )
            }

            if (allReactionsSucceeded) {
                updateExecutionStatus(savedExecution.id, ExecutionStatus.SUCCESS, progress = 100)
                updateAreaStats(area.id)
                logger.info("Area ${area.id} executed successfully")
                ProcessResult(success = true, message = "Area executed successfully")
            } else {
                updateExecutionStatus(
                    savedExecution.id,
                    ExecutionStatus.FAILED,
                    error = reactionErrors.joinToString("; "),
                    progress = 100
                )
                ProcessResult(success = false, error = reactionErrors.joinToString("; "))
            }

        } catch (e: Exception) {
            logger.error("Unexpected error processing area ${area.id}", e)
            updateExecutionStatus(savedExecution.id, ExecutionStatus.FAILED, error = e.message, progress = 100)
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
        actionData: Document,
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
        actionData: Document? = null,
        error: String? = null,
        progress: Int? = null
    ) {
        val execution = areaExecutionRepository.findById(executionId) ?: return

        val updated = execution.copy(
            status = status,
            actionData = actionData ?: execution.actionData,
            error = error,
            progress = progress ?: execution.progress,
            completedAt = if (status == ExecutionStatus.SUCCESS || status == ExecutionStatus.FAILED) {
                System.currentTimeMillis()
            } else {
                null
            }
        )

        areaExecutionRepository.update(updated)
    }

    private suspend fun updateExecutionWithStep(
        executionId: org.bson.types.ObjectId,
        status: ExecutionStatus,
        currentStep: ExecutionStep,
        progress: Int
    ) {
        val execution = areaExecutionRepository.findById(executionId) ?: return

        val updated = execution.copy(
            status = status,
            currentStep = currentStep,
            progress = progress
        )

        areaExecutionRepository.update(updated)
    }

    private suspend fun updateExecutionWithStepComplete(
        executionId: org.bson.types.ObjectId,
        status: ExecutionStatus,
        stepRecord: ExecutionStepRecord,
        actionData: Document? = null,
        error: String? = null
    ): AreaExecution {
        val execution = areaExecutionRepository.findById(executionId) ?: throw IllegalStateException("Execution not found")

        val updatedSteps = execution.steps + stepRecord
        val progress = ((updatedSteps.size.toDouble() / execution.totalSteps) * 100).toInt()

        val updated = execution.copy(
            status = status,
            steps = updatedSteps,
            progress = progress,
            actionData = actionData ?: execution.actionData,
            error = error,
            currentStep = null
        )

        areaExecutionRepository.update(updated)
        return updated
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
    val data: Document = Document(),
    val error: String? = null
)

data class ReactionExecutionResult(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)
