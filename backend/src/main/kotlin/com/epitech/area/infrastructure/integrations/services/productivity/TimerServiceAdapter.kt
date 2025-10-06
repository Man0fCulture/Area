package com.epitech.area.infrastructure.integrations.services.productivity

import com.epitech.area.domain.entities.UserService
import com.epitech.area.infrastructure.integrations.*
import kotlinx.coroutines.delay
import org.bson.Document

class TimerServiceAdapter : ServiceAdapter {
    override val serviceId = "timer"
    override val serviceName = "Timer"

    override suspend fun executeAction(
        actionId: String,
        config: Map<String, Any>,
        userService: UserService?
    ): ActionResult {
        return when (actionId) {
            "every_x_seconds" -> executeEveryXSeconds(config)
            "at_time" -> executeAtTime(config)
            else -> ActionResult(false, error = "Unknown action: $actionId")
        }
    }

    private suspend fun executeEveryXSeconds(config: Map<String, Any>): ActionResult {
        val interval = (config["interval"] as? String)?.toIntOrNull() ?: return ActionResult(
            false,
            error = "Missing or invalid 'interval' parameter"
        )

        return ActionResult(
            success = true,
            data = Document().apply {
                append("triggered_at", System.currentTimeMillis())
                append("interval", interval)
                append("message", "Timer triggered after $interval seconds")
            }
        )
    }

    private suspend fun executeAtTime(config: Map<String, Any>): ActionResult {
        val time = config["time"] as? String ?: return ActionResult(
            false,
            error = "Missing 'time' parameter (format: HH:mm)"
        )

        return ActionResult(
            success = true,
            data = Document().apply {
                append("triggered_at", System.currentTimeMillis())
                append("scheduled_time", time)
                append("message", "Timer triggered at $time")
            }
        )
    }

    override suspend fun executeReaction(
        reactionId: String,
        config: Map<String, Any>,
        actionData: Document,
        userService: UserService?
    ): ReactionResult {
        return when (reactionId) {
            "wait" -> executeWait(config)
            else -> ReactionResult(false, error = "Unknown reaction: $reactionId")
        }
    }

    private suspend fun executeWait(config: Map<String, Any>): ReactionResult {
        val seconds = (config["seconds"] as? String)?.toIntOrNull() ?: return ReactionResult(
            false,
            error = "Missing or invalid 'seconds' parameter"
        )

        if (seconds < 0 || seconds > 300) {
            return ReactionResult(false, error = "Seconds must be between 0 and 300")
        }

        delay(seconds * 1000L)

        return ReactionResult(
            success = true,
            message = "Waited for $seconds seconds"
        )
    }

    override suspend fun validateActionConfig(actionId: String, config: Map<String, Any>): ValidationResult {
        return when (actionId) {
            "every_x_seconds" -> {
                val interval = (config["interval"] as? String)?.toIntOrNull()
                if (interval == null || interval < 1) {
                    ValidationResult(false, listOf("'interval' must be a positive integer"))
                } else {
                    ValidationResult(true)
                }
            }
            "at_time" -> {
                val time = config["time"] as? String
                if (time.isNullOrBlank() || !time.matches(Regex("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$"))) {
                    ValidationResult(false, listOf("'time' must be in HH:mm format"))
                } else {
                    ValidationResult(true)
                }
            }
            else -> ValidationResult(false, listOf("Unknown action: $actionId"))
        }
    }

    override suspend fun validateReactionConfig(reactionId: String, config: Map<String, Any>): ValidationResult {
        return when (reactionId) {
            "wait" -> {
                val seconds = (config["seconds"] as? String)?.toIntOrNull()
                if (seconds == null || seconds < 0 || seconds > 300) {
                    ValidationResult(false, listOf("'seconds' must be between 0 and 300"))
                } else {
                    ValidationResult(true)
                }
            }
            else -> ValidationResult(false, listOf("Unknown reaction: $reactionId"))
        }
    }
}
