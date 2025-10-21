package com.epitech.area.sdk

import com.epitech.area.domain.entities.UserService
import org.bson.Document

/**
 * Base interface pour tous les services AREA
 * Chaque service doit implémenter cette interface de manière simple
 */
abstract class ServiceDefinition {

    /**
     * Métadonnées du service (chargées depuis YAML ou définies en code)
     */
    abstract val id: String
    abstract val name: String
    abstract val description: String
    abstract val category: String
    abstract val requiresAuth: Boolean

    /**
     * Runtime fourni par AREA - gère HTTP, OAuth, logging, etc.
     * Automatiquement injecté au chargement
     */
    lateinit var runtime: AreaRuntime

    /**
     * Exécuter un trigger (Action dans AREA)
     *
     * @param triggerId ID du trigger à exécuter (ex: "new_email")
     * @param config Configuration fournie par l'utilisateur
     * @param userService Credentials OAuth2 si requiresAuth = true
     * @return TriggerResult avec success + data ou error
     */
    abstract suspend fun executeTrigger(
        triggerId: String,
        config: Map<String, String>,
        userService: UserService?
    ): TriggerResult

    /**
     * Exécuter une action (Reaction dans AREA)
     *
     * @param actionId ID de l'action à exécuter (ex: "send_email")
     * @param config Configuration fournie par l'utilisateur
     * @param triggerData Données retournées par le trigger
     * @param userService Credentials OAuth2 si requiresAuth = true
     * @return ActionResult avec success + message ou error
     */
    abstract suspend fun executeAction(
        actionId: String,
        config: Map<String, String>,
        triggerData: Document,
        userService: UserService?
    ): ActionResult

    /**
     * Valider la configuration d'un trigger (optionnel - par défaut ok)
     */
    open suspend fun validateTriggerConfig(
        triggerId: String,
        config: Map<String, String>
    ): ValidationResult = ValidationResult(true)

    /**
     * Valider la configuration d'une action (optionnel - par défaut ok)
     */
    open suspend fun validateActionConfig(
        actionId: String,
        config: Map<String, String>
    ): ValidationResult = ValidationResult(true)
}

/**
 * Résultat d'un trigger
 */
data class TriggerResult(
    val success: Boolean,
    val data: Document = Document(),
    val error: String? = null
) {
    companion object {
        fun success(data: Document) = TriggerResult(true, data)
        fun success(vararg pairs: Pair<String, Any>) = TriggerResult(
            true,
            Document(pairs.toMap())
        )
        fun error(message: String) = TriggerResult(false, error = message)
        fun notTriggered() = TriggerResult(false, error = "Condition not met")
    }
}

/**
 * Résultat d'une action
 */
data class ActionResult(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
) {
    companion object {
        fun success(message: String = "Action completed") = ActionResult(true, message)
        fun error(message: String) = ActionResult(false, error = message)
    }
}

/**
 * Résultat de validation
 */
data class ValidationResult(
    val valid: Boolean,
    val errors: List<String> = emptyList()
) {
    constructor(valid: Boolean, error: String) : this(valid, listOf(error))
}
