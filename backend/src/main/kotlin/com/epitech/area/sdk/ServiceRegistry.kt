package com.epitech.area.sdk

import com.epitech.area.domain.entities.*
import io.ktor.client.*
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Registry qui charge automatiquement tous les services
 * depuis le package integrations
 */
class ServiceRegistry(private val httpClient: HttpClient) {
    private val logger = LoggerFactory.getLogger(ServiceRegistry::class.java)
    private val services = mutableMapOf<String, ServiceDefinition>()
    private val runtime = AreaRuntime(httpClient)

    /**
     * Enregistrer manuellement un service
     */
    fun register(service: ServiceDefinition) {
        service.runtime = runtime
        services[service.id] = service
        logger.info("✓ Registered service: ${service.name} (${service.id})")
    }

    /**
     * Récupérer un service par son ID
     */
    fun get(serviceId: String): ServiceDefinition? = services[serviceId]

    /**
     * Récupérer tous les services
     */
    fun getAll(): List<ServiceDefinition> = services.values.toList()

    /**
     * Convertir les services enregistrés en entités domain
     * pour les sauvegarder en base de données
     */
    fun toServiceEntities(): List<Service> {
        return services.values.map { serviceDef ->
            Service(
                name = serviceDef.name,
                description = serviceDef.description,
                category = serviceDef.category,
                requiresAuth = serviceDef.requiresAuth,
                authType = if (serviceDef.requiresAuth) "OAuth2" else null,
                actions = loadActionsForService(serviceDef),
                reactions = loadReactionsForService(serviceDef),
                enabled = true
            )
        }
    }

    /**
     * Charger les actions depuis un fichier YAML (si existe)
     * Sinon retourner des définitions par défaut
     */
    private fun loadActionsForService(service: ServiceDefinition): List<ActionDefinition> {
        // Pour l'instant, on retourne une liste vide
        // Les actions seront chargées depuis YAML dans la prochaine version
        return emptyList()
    }

    /**
     * Charger les réactions depuis un fichier YAML (si existe)
     */
    private fun loadReactionsForService(service: ServiceDefinition): List<ReactionDefinition> {
        // Pour l'instant, on retourne une liste vide
        // Les réactions seront chargées depuis YAML dans la prochaine version
        return emptyList()
    }
}

/**
 * Modèle pour charger un service depuis YAML (futur)
 */
@Serializable
data class ServiceManifest(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val requiresAuth: Boolean = false,
    val triggers: List<TriggerManifest> = emptyList(),
    val actions: List<ActionManifest> = emptyList()
)

@Serializable
data class TriggerManifest(
    val id: String,
    val name: String,
    val description: String,
    val type: String, // polling, webhook, schedule
    val interval: Int? = null,
    val configSchema: Map<String, FieldManifest> = emptyMap()
)

@Serializable
data class ActionManifest(
    val id: String,
    val name: String,
    val description: String,
    val configSchema: Map<String, FieldManifest> = emptyMap()
)

@Serializable
data class FieldManifest(
    val type: String,
    val required: Boolean = false,
    val default: String? = null,
    val description: String? = null
)
