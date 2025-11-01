package com.epitech.area.api.dto.responses

import com.epitech.area.domain.entities.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import org.bson.Document

@Serializable
data class AreaExecutionResponse(
    val id: String,
    val areaId: String,
    val status: String,
    val startedAt: Long,
    val completedAt: Long?,
    val error: String?,
    val currentStep: ExecutionStepResponse?,
    val steps: List<ExecutionStepRecordResponse>,
    val progress: Int,
    val totalSteps: Int
)

@Serializable
data class ExecutionStepResponse(
    val type: String,
    val name: String,
    val index: Int,
    val total: Int
)

@Serializable
data class ExecutionStepRecordResponse(
    val stepType: String,
    val stepName: String,
    val stepIndex: Int,
    val status: String,
    val startedAt: Long,
    val completedAt: Long?,
    val error: String?,
    val duration: Long?
)

fun AreaExecution.toResponse(): AreaExecutionResponse = AreaExecutionResponse(
    id = id.toHexString(),
    areaId = areaId.toHexString(),
    status = status.name,
    startedAt = startedAt,
    completedAt = completedAt,
    error = error,
    currentStep = currentStep?.toResponse(),
    steps = steps.map { it.toResponse() },
    progress = progress,
    totalSteps = totalSteps
)

fun ExecutionStep.toResponse(): ExecutionStepResponse = ExecutionStepResponse(
    type = type.name,
    name = name,
    index = index,
    total = total
)

fun ExecutionStepRecord.toResponse(): ExecutionStepRecordResponse = ExecutionStepRecordResponse(
    stepType = stepType.name,
    stepName = stepName,
    stepIndex = stepIndex,
    status = status.name,
    startedAt = startedAt,
    completedAt = completedAt,
    error = error,
    duration = duration
)
