package com.epitech.area.domain.entities

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class AreaExecution(
    @BsonId
    val id: ObjectId = ObjectId(),
    val areaId: ObjectId,
    val status: ExecutionStatus = ExecutionStatus.PENDING,
    val actionData: Map<String, Any>? = null,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val error: String? = null
)

data class ExecutionResult(
    val serviceId: ObjectId,
    val actionOrReactionId: String,
    val status: ExecutionStatus,
    val data: Map<String, Any>? = null,
    val error: String? = null,
    val executedAt: Long = System.currentTimeMillis()
)

enum class ExecutionStatus {
    PENDING,
    PROCESSING,
    SUCCESS,
    FAILED
}
