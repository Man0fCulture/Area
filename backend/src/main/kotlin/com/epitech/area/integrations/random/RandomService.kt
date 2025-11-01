package com.epitech.area.integrations.random

import com.epitech.area.domain.entities.UserService
import com.epitech.area.sdk.*
import org.bson.Document
import kotlin.random.Random

/**
 * Service Random - Génération de valeurs aléatoires
 *
 * Triggers:
 * - random_chance: Déclenché aléatoirement selon un pourcentage
 *
 * Actions:
 * - generate_number: Générer un nombre aléatoire
 * - choose_from_list: Choisir un élément aléatoire dans une liste
 * - generate_uuid: Générer un UUID
 */
class RandomService : ServiceDefinition() {
    override val id = "random"
    override val name = "Random"
    override val description = "Generate random values and make random choices"
    override val category = "Utility"
    override val requiresAuth = false

    override suspend fun executeTrigger(
        triggerId: String,
        config: Map<String, String>,
        userService: UserService?
    ): TriggerResult {
        return when (triggerId) {
            "random_chance" -> handleRandomChance(config)
            else -> TriggerResult.error("Unknown trigger: $triggerId")
        }
    }

    private fun handleRandomChance(config: Map<String, String>): TriggerResult {
        val percentage = config["percentage"]?.toIntOrNull() ?: 50

        if (percentage < 0 || percentage > 100) {
            return TriggerResult.error("Percentage must be between 0 and 100")
        }

        val random = Random.nextInt(100)
        val triggered = random < percentage

        return if (triggered) {
            TriggerResult.success(
                "triggered" to true,
                "percentage" to percentage,
                "random_value" to random,
                "message" to "Randomly triggered with $percentage% chance"
            )
        } else {
            TriggerResult.notTriggered()
        }
    }

    override suspend fun executeAction(
        actionId: String,
        config: Map<String, String>,
        triggerData: Document,
        userService: UserService?
    ): ActionResult {
        return when (actionId) {
            "generate_number" -> handleGenerateNumber(config)
            "choose_from_list" -> handleChooseFromList(config)
            "generate_uuid" -> handleGenerateUuid()
            else -> ActionResult.error("Unknown action: $actionId")
        }
    }

    private fun handleGenerateNumber(config: Map<String, String>): ActionResult {
        val min = config["min"]?.toIntOrNull() ?: 0
        val max = config["max"]?.toIntOrNull() ?: 100

        if (min >= max) {
            return ActionResult.error("'min' must be less than 'max'")
        }

        val number = Random.nextInt(min, max + 1)
        runtime.log("Generated random number: $number (between $min and $max)")

        return ActionResult.success("Generated number: $number")
    }

    private fun handleChooseFromList(config: Map<String, String>): ActionResult {
        val items = config["items"]?.split(",")?.map { it.trim() }
            ?: return ActionResult.error("Missing 'items' parameter")

        if (items.isEmpty()) {
            return ActionResult.error("Items list is empty")
        }

        val chosen = items.random()
        runtime.log("Randomly chose: $chosen from ${items.size} items")

        return ActionResult.success("Chose: $chosen")
    }

    private fun handleGenerateUuid(): ActionResult {
        val uuid = java.util.UUID.randomUUID().toString()
        runtime.log("Generated UUID: $uuid")
        return ActionResult.success("Generated UUID: $uuid")
    }

    override suspend fun validateTriggerConfig(
        triggerId: String,
        config: Map<String, String>
    ): ValidationResult {
        return when (triggerId) {
            "random_chance" -> {
                val percentage = config["percentage"]?.toIntOrNull()
                if (percentage == null || percentage < 0 || percentage > 100) {
                    ValidationResult(false, "'percentage' must be between 0 and 100")
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
            "generate_number" -> {
                val min = config["min"]?.toIntOrNull()
                val max = config["max"]?.toIntOrNull()
                if (min != null && max != null && min >= max) {
                    ValidationResult(false, "'min' must be less than 'max'")
                } else {
                    ValidationResult(true)
                }
            }
            "choose_from_list" -> {
                val items = config["items"]
                if (items.isNullOrBlank()) {
                    ValidationResult(false, "'items' is required (comma-separated)")
                } else {
                    ValidationResult(true)
                }
            }
            "generate_uuid" -> ValidationResult(true)
            else -> ValidationResult(false, "Unknown action: $actionId")
        }
    }
}
