package com.epitech.area.application.services

import com.epitech.area.domain.entities.Area
import com.epitech.area.domain.entities.ActionConfig
import com.epitech.area.domain.entities.ReactionConfig
import com.epitech.area.domain.entities.AreaExecution
import com.epitech.area.domain.repositories.AreaRepository
import com.epitech.area.domain.repositories.ServiceRepository
import com.epitech.area.domain.repositories.AreaExecutionRepository
import com.epitech.area.infrastructure.hooks.HookScheduler
import org.bson.types.ObjectId

class AreaService(
    private val areaRepository: AreaRepository,
    private val serviceRepository: ServiceRepository,
    private val areaExecutionRepository: AreaExecutionRepository,
    private val hookScheduler: HookScheduler
) {
    suspend fun createArea(
        userId: ObjectId,
        name: String,
        description: String?,
        actionServiceId: ObjectId,
        actionId: String,
        actionConfig: Map<String, String>,
        reactions: List<Triple<ObjectId, String, Map<String, String>>>
    ): Result<Area> {
        val actionService = serviceRepository.findById(actionServiceId)
            ?: return Result.failure(Exception("Action service not found"))

        val actionDef = actionService.actions.find { it.id == actionId }
            ?: return Result.failure(Exception("Action not found in service"))

        val reactionConfigs = reactions.map { (serviceId, reactionId, config) ->
            val reactionService = serviceRepository.findById(serviceId)
                ?: return Result.failure(Exception("Reaction service not found: $serviceId"))

            val reactionDef = reactionService.reactions.find { it.id == reactionId }
                ?: return Result.failure(Exception("Reaction not found: $reactionId"))

            ReactionConfig(
                serviceId = serviceId,
                reactionId = reactionId,
                config = config
            )
        }

        val area = Area(
            userId = userId,
            name = name,
            description = description,
            action = ActionConfig(
                serviceId = actionServiceId,
                actionId = actionId,
                config = actionConfig
            ),
            reactions = reactionConfigs,
            active = true
        )

        val created = areaRepository.create(area)
        return Result.success(created)
    }

    suspend fun getUserAreas(userId: ObjectId): List<Area> {
        return areaRepository.findByUserId(userId)
    }

    suspend fun getActiveUserAreas(userId: ObjectId): List<Area> {
        return areaRepository.findActiveAreasByUserId(userId)
    }

    suspend fun getAreaById(areaId: ObjectId, userId: ObjectId): Result<Area> {
        val area = areaRepository.findById(areaId)
            ?: return Result.failure(Exception("Area not found"))

        if (area.userId != userId) {
            return Result.failure(Exception("Unauthorized"))
        }

        return Result.success(area)
    }

    suspend fun updateArea(
        areaId: ObjectId,
        userId: ObjectId,
        name: String?,
        description: String?,
        active: Boolean?
    ): Result<Area> {
        val area = areaRepository.findById(areaId)
            ?: return Result.failure(Exception("Area not found"))

        if (area.userId != userId) {
            return Result.failure(Exception("Unauthorized"))
        }

        val updated = area.copy(
            name = name ?: area.name,
            description = description ?: area.description,
            active = active ?: area.active,
            updatedAt = System.currentTimeMillis()
        )

        areaRepository.update(updated)
        return Result.success(updated)
    }

    suspend fun deleteArea(areaId: ObjectId, userId: ObjectId): Result<Boolean> {
        val area = areaRepository.findById(areaId)
            ?: return Result.failure(Exception("Area not found"))

        if (area.userId != userId) {
            return Result.failure(Exception("Unauthorized"))
        }

        val deleted = areaRepository.delete(areaId)
        return Result.success(deleted)
    }

    suspend fun getAllActiveAreas(): List<Area> {
        return areaRepository.findActiveAreas()
    }

    suspend fun activateArea(areaId: ObjectId, userId: ObjectId): Result<Area> {
        val area = areaRepository.findById(areaId)
            ?: return Result.failure(Exception("Area not found"))

        if (area.userId != userId) {
            return Result.failure(Exception("Unauthorized"))
        }

        val updated = area.copy(
            active = true,
            updatedAt = System.currentTimeMillis()
        )

        areaRepository.update(updated)
        return Result.success(updated)
    }

    suspend fun deactivateArea(areaId: ObjectId, userId: ObjectId): Result<Area> {
        val area = areaRepository.findById(areaId)
            ?: return Result.failure(Exception("Area not found"))

        if (area.userId != userId) {
            return Result.failure(Exception("Unauthorized"))
        }

        val updated = area.copy(
            active = false,
            updatedAt = System.currentTimeMillis()
        )

        areaRepository.update(updated)
        hookScheduler.cancelScheduledArea(areaId.toHexString())
        
        return Result.success(updated)
    }

    suspend fun testArea(areaId: ObjectId, userId: ObjectId): Result<String> {
        val area = areaRepository.findById(areaId)
            ?: return Result.failure(Exception("Area not found"))

        if (area.userId != userId) {
            return Result.failure(Exception("Unauthorized"))
        }

        hookScheduler.triggerAreaManually(areaId)
        
        return Result.success("Area test triggered successfully")
    }

    suspend fun getAreaExecutions(
        areaId: ObjectId,
        userId: ObjectId,
        limit: Int
    ): Result<List<AreaExecution>> {
        val area = areaRepository.findById(areaId)
            ?: return Result.failure(Exception("Area not found"))

        if (area.userId != userId) {
            return Result.failure(Exception("Unauthorized"))
        }

        val executions = areaExecutionRepository.findByAreaId(areaId, limit)
        return Result.success(executions)
    }
}
