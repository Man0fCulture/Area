package com.epitech.area.integrations.text

import com.epitech.area.domain.entities.UserService
import com.epitech.area.sdk.*
import org.bson.Document

/**
 * Service Text - Manipulation de texte
 *
 * Actions:
 * - to_uppercase: Convertir en majuscules
 * - to_lowercase: Convertir en minuscules
 * - concat: Concaténer des textes
 * - replace: Remplacer du texte
 * - length: Obtenir la longueur
 * - contains: Vérifier si contient un texte
 */
class TextService : ServiceDefinition() {
    override val id = "text"
    override val name = "Text"
    override val description = "Text manipulation and formatting"
    override val category = "Utility"
    override val requiresAuth = false

    override suspend fun executeTrigger(
        triggerId: String,
        config: Map<String, String>,
        userService: UserService?
    ): TriggerResult {
        // Ce service n'a pas de triggers, uniquement des actions
        return TriggerResult.error("Text service has no triggers")
    }

    override suspend fun executeAction(
        actionId: String,
        config: Map<String, String>,
        triggerData: Document,
        userService: UserService?
    ): ActionResult {
        return when (actionId) {
            "to_uppercase" -> handleToUppercase(config)
            "to_lowercase" -> handleToLowercase(config)
            "concat" -> handleConcat(config)
            "replace" -> handleReplace(config)
            "length" -> handleLength(config)
            "contains" -> handleContains(config)
            else -> ActionResult.error("Unknown action: $actionId")
        }
    }

    private fun handleToUppercase(config: Map<String, String>): ActionResult {
        val text = config["text"] ?: return ActionResult.error("Missing 'text' parameter")
        val result = text.uppercase()
        runtime.log("Converted to uppercase: $result")
        return ActionResult.success("Result: $result")
    }

    private fun handleToLowercase(config: Map<String, String>): ActionResult {
        val text = config["text"] ?: return ActionResult.error("Missing 'text' parameter")
        val result = text.lowercase()
        runtime.log("Converted to lowercase: $result")
        return ActionResult.success("Result: $result")
    }

    private fun handleConcat(config: Map<String, String>): ActionResult {
        val text1 = config["text1"] ?: ""
        val text2 = config["text2"] ?: ""
        val separator = config["separator"] ?: ""

        val result = if (separator.isEmpty()) {
            text1 + text2
        } else {
            "$text1$separator$text2"
        }

        runtime.log("Concatenated: $result")
        return ActionResult.success("Result: $result")
    }

    private fun handleReplace(config: Map<String, String>): ActionResult {
        val text = config["text"] ?: return ActionResult.error("Missing 'text' parameter")
        val find = config["find"] ?: return ActionResult.error("Missing 'find' parameter")
        val replace = config["replace"] ?: ""

        val result = text.replace(find, replace)
        runtime.log("Replaced '$find' with '$replace': $result")
        return ActionResult.success("Result: $result")
    }

    private fun handleLength(config: Map<String, String>): ActionResult {
        val text = config["text"] ?: return ActionResult.error("Missing 'text' parameter")
        val length = text.length
        runtime.log("Text length: $length")
        return ActionResult.success("Length: $length")
    }

    private fun handleContains(config: Map<String, String>): ActionResult {
        val text = config["text"] ?: return ActionResult.error("Missing 'text' parameter")
        val search = config["search"] ?: return ActionResult.error("Missing 'search' parameter")
        val caseSensitive = config["case_sensitive"]?.toBoolean() ?: true

        val contains = if (caseSensitive) {
            text.contains(search)
        } else {
            text.lowercase().contains(search.lowercase())
        }

        runtime.log("Text contains '$search': $contains")
        return ActionResult.success("Contains: $contains")
    }

    override suspend fun validateActionConfig(
        actionId: String,
        config: Map<String, String>
    ): ValidationResult {
        return when (actionId) {
            "to_uppercase", "to_lowercase", "length" -> {
                if (config["text"].isNullOrBlank()) {
                    ValidationResult(false, "'text' is required")
                } else {
                    ValidationResult(true)
                }
            }
            "concat" -> ValidationResult(true) // Optional parameters
            "replace" -> {
                val errors = mutableListOf<String>()
                if (config["text"].isNullOrBlank()) errors.add("'text' is required")
                if (config["find"].isNullOrBlank()) errors.add("'find' is required")
                if (errors.isEmpty()) ValidationResult(true) else ValidationResult(false, errors)
            }
            "contains" -> {
                val errors = mutableListOf<String>()
                if (config["text"].isNullOrBlank()) errors.add("'text' is required")
                if (config["search"].isNullOrBlank()) errors.add("'search' is required")
                if (errors.isEmpty()) ValidationResult(true) else ValidationResult(false, errors)
            }
            else -> ValidationResult(false, "Unknown action: $actionId")
        }
    }
}
