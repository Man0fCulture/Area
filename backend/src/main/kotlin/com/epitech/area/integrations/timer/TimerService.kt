package com.epitech.area.integrations.timer

import com.epitech.area.domain.entities.UserService
import com.epitech.area.sdk.*
import kotlinx.coroutines.delay
import org.bson.Document

/**
 * Service Timer - Actions basées sur le temps
 *
 * Triggers:
 * - every_x_seconds: Déclenche toutes les X secondes
 * - at_time: Déclenche à une heure précise chaque jour
 *
 * Actions:
 * - wait: Attend X secondes
 */
class TimerService : ServiceDefinition() {
    override val id = "timer"
    override val name = "Timer"
    override val description = "Schedule actions based on time"
    override val category = "Productivity"
    override val requiresAuth = false

    override suspend fun executeTrigger(
        triggerId: String,
        config: Map<String, String>,
        userService: UserService?
    ): TriggerResult {
        return when (triggerId) {
            "every_x_seconds" -> handleEveryXSeconds(config)
            "at_time" -> handleAtTime(config)
            else -> TriggerResult.error("Unknown trigger: $triggerId")
        }
    }

    private fun handleEveryXSeconds(config: Map<String, String>): TriggerResult {
        val interval = config["interval"]?.toIntOrNull()
            ?: return TriggerResult.error("Missing or invalid 'interval' parameter")

        // Le trigger est toujours déclenché (le scheduler gère l'intervalle)
        return TriggerResult.success(
            "triggered_at" to System.currentTimeMillis(),
            "interval" to interval,
            "message" to "Timer triggered after $interval seconds"
        )
    }

    private fun handleAtTime(config: Map<String, String>): TriggerResult {
        val time = config["time"]
            ?: return TriggerResult.error("Missing 'time' parameter (format: HH:mm)")

        return TriggerResult.success(
            "triggered_at" to System.currentTimeMillis(),
            "scheduled_time" to time,
            "message" to "Timer triggered at $time"
        )
    }

    override suspend fun executeAction(
        actionId: String,
        config: Map<String, String>,
        triggerData: Document,
        userService: UserService?
    ): ActionResult {
        return when (actionId) {
            "wait" -> handleWait(config)
            else -> ActionResult.error("Unknown action: $actionId")
        }
    }

    private suspend fun handleWait(config: Map<String, String>): ActionResult {
        val seconds = config["seconds"]?.toIntOrNull()
            ?: return ActionResult.error("Missing or invalid 'seconds' parameter")

        if (seconds < 0 || seconds > 300) {
            return ActionResult.error("Seconds must be between 0 and 300")
        }

        runtime.log("Waiting for $seconds seconds...")
        delay(seconds * 1000L)

        return ActionResult.success("Waited for $seconds seconds")
    }

    override suspend fun validateTriggerConfig(
        triggerId: String,
        config: Map<String, String>
    ): ValidationResult {
        return when (triggerId) {
            "every_x_seconds" -> {
                val interval = config["interval"]?.toIntOrNull()
                if (interval == null || interval < 1) {
                    ValidationResult(false, "'interval' must be a positive integer")
                } else {
                    ValidationResult(true)
                }
            }
            "at_time" -> {
                val time = config["time"]
                if (time.isNullOrBlank() || !time.matches(Regex("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$"))) {
                    ValidationResult(false, "'time' must be in HH:mm format")
                } else {
                    ValidationResult(true)
                }
            }
            else -> ValidationResult(false, "Unknown trigger: $triggerId")
        }
    }

    override suspend fun validateActionConfig(
        actionId: String,
        config: Map<String, String>
    ): ValidationResult {
        return when (actionId) {
            "wait" -> {
                val seconds = config["seconds"]?.toIntOrNull()
                if (seconds == null || seconds < 0 || seconds > 300) {
                    ValidationResult(false, "'seconds' must be between 0 and 300")
                } else {
                    ValidationResult(true)
                }
            }
            else -> ValidationResult(false, "Unknown action: $actionId")
        }
    }
}
