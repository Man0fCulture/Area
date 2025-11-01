package com.epitech.area.sdk.yaml

import kotlinx.serialization.Serializable

/**
 * Configuration complète d'un service chargé depuis YAML
 * Permet de définir un service sans écrire de code Kotlin
 */
@Serializable
data class YamlServiceConfig(
    val service: ServiceMetadata,
    val auth: AuthConfig? = null,
    val actions: List<ActionConfig> = emptyList(),
    val reactions: List<ReactionConfig> = emptyList()
)

/**
 * Métadonnées du service
 */
@Serializable
data class ServiceMetadata(
    val id: String,
    val name: String,
    val displayName: String,
    val description: String,
    val category: String,
    val icon: String? = null,
    val color: String? = null,
    val baseUrl: String? = null
)

/**
 * Configuration d'authentification
 */
@Serializable
data class AuthConfig(
    val type: AuthType,
    val provider: String? = null,
    val apiKey: ApiKeyConfig? = null,
    val oauth2: OAuth2Config? = null
)

@Serializable
enum class AuthType {
    NONE,
    API_KEY,
    OAUTH2
}

@Serializable
data class ApiKeyConfig(
    val header: String = "Authorization",
    val prefix: String = "Bearer",
    val envVar: String
)

@Serializable
data class OAuth2Config(
    val provider: String,
    val scopes: List<String> = emptyList()
)

/**
 * Configuration d'une action (trigger)
 */
@Serializable
data class ActionConfig(
    val id: String,
    val name: String,
    val description: String,
    val type: TriggerType,
    val pollInterval: Int? = 60, // secondes
    val config: List<ConfigField> = emptyList(),
    val endpoint: EndpointConfig,
    val output: List<OutputField> = emptyList(),
    val condition: String? = null // Expression pour vérifier si déclenché
)

@Serializable
enum class TriggerType {
    POLLING,    // Vérification périodique
    WEBHOOK,    // Déclenchement instantané
    SCHEDULE    // Cron-like
}

/**
 * Configuration d'une réaction
 */
@Serializable
data class ReactionConfig(
    val id: String,
    val name: String,
    val description: String,
    val config: List<ConfigField> = emptyList(),
    val endpoint: EndpointConfig
)

/**
 * Champ de configuration utilisateur
 */
@Serializable
data class ConfigField(
    val name: String,
    val type: FieldType,
    val label: String,
    val description: String? = null,
    val required: Boolean = true,
    val default: String? = null,
    val placeholder: String? = null,
    val options: List<String>? = null // Pour select/dropdown
)

@Serializable
enum class FieldType {
    STRING,
    TEXT,
    NUMBER,
    BOOLEAN,
    SELECT,
    EMAIL,
    URL
}

/**
 * Configuration d'un endpoint HTTP
 */
@Serializable
data class EndpointConfig(
    val method: HttpMethod,
    val url: String,
    val headers: Map<String, String>? = null,
    val params: Map<String, String>? = null,
    val body: Map<String, String>? = null,
    val bodyRaw: String? = null
)

@Serializable
enum class HttpMethod {
    GET,
    POST,
    PUT,
    PATCH,
    DELETE
}

/**
 * Champ de sortie (extraction de données de la réponse)
 */
@Serializable
data class OutputField(
    val name: String,
    val type: String = "string",
    val jsonPath: String? = null,
    val regex: String? = null,
    val transform: String? = null
)
