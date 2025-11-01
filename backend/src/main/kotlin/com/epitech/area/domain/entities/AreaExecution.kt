package com.epitech.area.domain.entities

import org.bson.Document
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class AreaExecution(
    @BsonId
    val id: ObjectId = ObjectId(),
    val areaId: ObjectId,
    val status: ExecutionStatus = ExecutionStatus.PENDING,
    val actionData: Document? = null,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val error: String? = null,
    val currentStep: ExecutionStep? = null,
    val steps: List<ExecutionStepRecord> = emptyList(),
    val progress: Int = 0,
    val totalSteps: Int = 0
)

data class ExecutionStepRecord(
    val stepType: StepType,
    val stepName: String,
    val stepIndex: Int,
    val status: ExecutionStatus,
    val startedAt: Long,
    val completedAt: Long? = null,
    val data: Document? = null,
    val error: String? = null,
    val duration: Long? = null
)

data class ExecutionStep(
    val type: StepType,
    val name: String,
    val index: Int,
    val total: Int
)

enum class StepType {
    ACTION,
    REACTION
}

data class ExecutionResult(
    val serviceId: ObjectId,
    val actionOrReactionId: String,
    val status: ExecutionStatus,
    val data: Document? = null,
    val error: String? = null,
    val executedAt: Long = System.currentTimeMillis()
)

enum class ExecutionStatus {
    PENDING,
    IN_PROGRESS,
    PROCESSING,
    SUCCESS,
    FAILED
}
