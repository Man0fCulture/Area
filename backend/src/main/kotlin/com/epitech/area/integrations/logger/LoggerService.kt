package com.epitech.area.integrations.logger

import com.epitech.area.domain.entities.UserService
import com.epitech.area.sdk.*
import org.bson.Document

/**
 * Service Logger - Logging et debugging
 *
 * Actions:
 * - log_info: Logger un message info
 * - log_warn: Logger un warning
 * - log_error: Logger une erreur
 * - log_debug: Logger en mode debug
 */
class LoggerService : ServiceDefinition() {
    override val id = "logger"
    override val name = "Logger"
    override val description = "Log messages for debugging and monitoring"
    override val category = "Utility"
    override val requiresAuth = false

    override suspend fun executeTrigger(
        triggerId: String,
        config: Map<String, String>,
        userService: UserService?
    ): TriggerResult {
        return TriggerResult.error("Logger service has no triggers")
    }

    override suspend fun executeAction(
        actionId: String,
        config: Map<String, String>,
        triggerData: Document,
        userService: UserService?
    ): ActionResult {
        return when (actionId) {
            "log_info" -> handleLogInfo(config, triggerData)
            "log_warn" -> handleLogWarn(config, triggerData)
            "log_error" -> handleLogError(config, triggerData)
            "log_debug" -> handleLogDebug(config, triggerData)
            else -> ActionResult.error("Unknown action: $actionId")
        }
    }

    private fun handleLogInfo(config: Map<String, String>, triggerData: Document): ActionResult {
        val message = config["message"] ?: return ActionResult.error("Missing 'message' parameter")
        val includeData = config["include_trigger_data"]?.toBoolean() ?: false

        val logMessage = if (includeData) {
            "$message | Trigger data: $triggerData"
        } else {
            message
        }

        runtime.log(logMessage, LogLevel.INFO)
        return ActionResult.success("Logged: $message")
    }

    private fun handleLogWarn(config: Map<String, String>, triggerData: Document): ActionResult {
        val message = config["message"] ?: return ActionResult.error("Missing 'message' parameter")
        val includeData = config["include_trigger_data"]?.toBoolean() ?: false

        val logMessage = if (includeData) {
            "$message | Trigger data: $triggerData"
        } else {
            message
        }

        runtime.log(logMessage, LogLevel.WARN)
        return ActionResult.success("Logged warning: $message")
    }

    private fun handleLogError(config: Map<String, String>, triggerData: Document): ActionResult {
        val message = config["message"] ?: return ActionResult.error("Missing 'message' parameter")
        val includeData = config["include_trigger_data"]?.toBoolean() ?: false

        val logMessage = if (includeData) {
            "$message | Trigger data: $triggerData"
        } else {
            message
        }

        runtime.log(logMessage, LogLevel.ERROR)
        return ActionResult.success("Logged error: $message")
    }

    private fun handleLogDebug(config: Map<String, String>, triggerData: Document): ActionResult {
        val message = config["message"] ?: return ActionResult.error("Missing 'message' parameter")
        val includeData = config["include_trigger_data"]?.toBoolean() ?: true // Par défaut true pour debug

        val logMessage = if (includeData) {
            "$message | Trigger data: $triggerData"
        } else {
            message
        }

        runtime.log(logMessage, LogLevel.DEBUG)
        return ActionResult.success("Logged debug: $message")
    }

    override suspend fun validateActionConfig(
        actionId: String,
        config: Map<String, String>
    ): ValidationResult {
        return when (actionId) {
            "log_info", "log_warn", "log_error", "log_debug" -> {
                if (config["message"].isNullOrBlank()) {
                    ValidationResult(false, "'message' is required")
                } else {
                    ValidationResult(true)
                }
            }
            else -> ValidationResult(false, "Unknown action: $actionId")
        }
    }
}
