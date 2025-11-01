package com.epitech.area.integrations.math

import com.epitech.area.domain.entities.UserService
import com.epitech.area.sdk.*
import org.bson.Document
import kotlin.math.*

/**
 * Service Math - Opérations mathématiques
 *
 * Actions:
 * - add: Addition
 * - subtract: Soustraction
 * - multiply: Multiplication
 * - divide: Division
 * - modulo: Modulo
 * - power: Puissance
 * - sqrt: Racine carrée
 * - abs: Valeur absolue
 * - round: Arrondir
 */
class MathService : ServiceDefinition() {
    override val id = "math"
    override val name = "Math"
    override val description = "Mathematical operations and calculations"
    override val category = "Utility"
    override val requiresAuth = false

    override suspend fun executeTrigger(
        triggerId: String,
        config: Map<String, String>,
        userService: UserService?
    ): TriggerResult {
        // Ce service n'a pas de triggers
        return TriggerResult.error("Math service has no triggers")
    }

    override suspend fun executeAction(
        actionId: String,
        config: Map<String, String>,
        triggerData: Document,
        userService: UserService?
    ): ActionResult {
        return when (actionId) {
            "add" -> handleAdd(config)
            "subtract" -> handleSubtract(config)
            "multiply" -> handleMultiply(config)
            "divide" -> handleDivide(config)
            "modulo" -> handleModulo(config)
            "power" -> handlePower(config)
            "sqrt" -> handleSqrt(config)
            "abs" -> handleAbs(config)
            "round" -> handleRound(config)
            else -> ActionResult.error("Unknown action: $actionId")
        }
    }

    private fun handleAdd(config: Map<String, String>): ActionResult {
        val a = config["a"]?.toDoubleOrNull() ?: return ActionResult.error("Missing 'a' parameter")
        val b = config["b"]?.toDoubleOrNull() ?: return ActionResult.error("Missing 'b' parameter")
        val result = a + b
        runtime.log("$a + $b = $result")
        return ActionResult.success("Result: $result")
    }

    private fun handleSubtract(config: Map<String, String>): ActionResult {
        val a = config["a"]?.toDoubleOrNull() ?: return ActionResult.error("Missing 'a' parameter")
        val b = config["b"]?.toDoubleOrNull() ?: return ActionResult.error("Missing 'b' parameter")
        val result = a - b
        runtime.log("$a - $b = $result")
        return ActionResult.success("Result: $result")
    }

    private fun handleMultiply(config: Map<String, String>): ActionResult {
        val a = config["a"]?.toDoubleOrNull() ?: return ActionResult.error("Missing 'a' parameter")
        val b = config["b"]?.toDoubleOrNull() ?: return ActionResult.error("Missing 'b' parameter")
        val result = a * b
        runtime.log("$a * $b = $result")
        return ActionResult.success("Result: $result")
    }

    private fun handleDivide(config: Map<String, String>): ActionResult {
        val a = config["a"]?.toDoubleOrNull() ?: return ActionResult.error("Missing 'a' parameter")
        val b = config["b"]?.toDoubleOrNull() ?: return ActionResult.error("Missing 'b' parameter")

        if (b == 0.0) {
            return ActionResult.error("Division by zero")
        }

        val result = a / b
        runtime.log("$a / $b = $result")
        return ActionResult.success("Result: $result")
    }

    private fun handleModulo(config: Map<String, String>): ActionResult {
        val a = config["a"]?.toDoubleOrNull() ?: return ActionResult.error("Missing 'a' parameter")
        val b = config["b"]?.toDoubleOrNull() ?: return ActionResult.error("Missing 'b' parameter")

        if (b == 0.0) {
            return ActionResult.error("Modulo by zero")
        }

        val result = a % b
        runtime.log("$a % $b = $result")
        return ActionResult.success("Result: $result")
    }

    private fun handlePower(config: Map<String, String>): ActionResult {
        val base = config["base"]?.toDoubleOrNull() ?: return ActionResult.error("Missing 'base' parameter")
        val exponent = config["exponent"]?.toDoubleOrNull() ?: return ActionResult.error("Missing 'exponent' parameter")

        val result = base.pow(exponent)
        runtime.log("$base ^ $exponent = $result")
        return ActionResult.success("Result: $result")
    }

    private fun handleSqrt(config: Map<String, String>): ActionResult {
        val number = config["number"]?.toDoubleOrNull() ?: return ActionResult.error("Missing 'number' parameter")

        if (number < 0) {
            return ActionResult.error("Cannot calculate square root of negative number")
        }

        val result = sqrt(number)
        runtime.log("sqrt($number) = $result")
        return ActionResult.success("Result: $result")
    }

    private fun handleAbs(config: Map<String, String>): ActionResult {
        val number = config["number"]?.toDoubleOrNull() ?: return ActionResult.error("Missing 'number' parameter")
        val result = abs(number)
        runtime.log("abs($number) = $result")
        return ActionResult.success("Result: $result")
    }

    private fun handleRound(config: Map<String, String>): ActionResult {
        val number = config["number"]?.toDoubleOrNull() ?: return ActionResult.error("Missing 'number' parameter")
        val decimals = config["decimals"]?.toIntOrNull() ?: 0

        val multiplier = 10.0.pow(decimals)
        val result = round(number * multiplier) / multiplier

        runtime.log("round($number, $decimals decimals) = $result")
        return ActionResult.success("Result: $result")
    }

    override suspend fun validateActionConfig(
        actionId: String,
        config: Map<String, String>
    ): ValidationResult {
        return when (actionId) {
            "add", "subtract", "multiply", "divide", "modulo" -> {
                val errors = mutableListOf<String>()
                if (config["a"]?.toDoubleOrNull() == null) errors.add("'a' must be a number")
                if (config["b"]?.toDoubleOrNull() == null) errors.add("'b' must be a number")
                if (errors.isEmpty()) ValidationResult(true) else ValidationResult(false, errors)
            }
            "power" -> {
                val errors = mutableListOf<String>()
                if (config["base"]?.toDoubleOrNull() == null) errors.add("'base' must be a number")
                if (config["exponent"]?.toDoubleOrNull() == null) errors.add("'exponent' must be a number")
                if (errors.isEmpty()) ValidationResult(true) else ValidationResult(false, errors)
            }
            "sqrt", "abs" -> {
                if (config["number"]?.toDoubleOrNull() == null) {
                    ValidationResult(false, "'number' must be a number")
                } else {
                    ValidationResult(true)
                }
            }
            "round" -> {
                val errors = mutableListOf<String>()
                if (config["number"]?.toDoubleOrNull() == null) errors.add("'number' must be a number")
                val decimals = config["decimals"]?.toIntOrNull()
                if (decimals != null && decimals < 0) errors.add("'decimals' must be >= 0")
                if (errors.isEmpty()) ValidationResult(true) else ValidationResult(false, errors)
            }
            else -> ValidationResult(false, "Unknown action: $actionId")
        }
    }
}
