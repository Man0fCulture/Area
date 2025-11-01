package com.epitech.area.sdk

import com.epitech.area.domain.entities.*
import com.epitech.area.sdk.yaml.YamlServiceLoader
import io.ktor.client.*
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Registry qui charge automatiquement tous les services
 * depuis le package integrations (Kotlin) et depuis les fichiers YAML
 */
class ServiceRegistry(private val httpClient: HttpClient) {
    private val logger = LoggerFactory.getLogger(ServiceRegistry::class.java)
    private val services = mutableMapOf<String, ServiceDefinition>()
    private val runtime = AreaRuntime(httpClient)
    private val yamlLoader = YamlServiceLoader(runtime)

    /**
     * Enregistrer manuellement un service
     */
    fun register(service: ServiceDefinition) {
        service.runtime = runtime
        services[service.id] = service
        logger.info("✓ Registered service: ${service.name} (${service.id})")
    }

    /**
     * Charger les services YAML depuis un dossier
     */
    fun loadYamlServices(directory: File = File("integrations")) {
        logger.info("📂 Loading YAML services from: ${directory.absolutePath}")
        val yamlServices = yamlLoader.loadFromDirectory(directory)
        yamlServices.forEach { service ->
            register(service)
        }
        logger.info("✅ Loaded ${yamlServices.size} YAML services")
    }

    /**
     * Charger un service YAML depuis un fichier spécifique
     */
    fun loadYamlService(yamlFile: File): Boolean {
        val service = yamlLoader.loadFromFile(yamlFile)
        return if (service != null) {
            register(service)
            true
        } else {
            false
        }
    }

    /**
     * Recharger un service YAML (hot-reload)
     */
    fun reloadYamlService(serviceId: String, yamlFile: File): Boolean {
        val service = yamlLoader.reload(yamlFile)
        return if (service != null) {
            services[serviceId] = service
            service.runtime = runtime
            logger.info("🔄 Reloaded service: ${service.name} (${service.id})")
            true
        } else {
            false
        }
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
        return services.values.mapNotNull { serviceDef ->
            // Si c'est un YamlBasedService, utiliser le converter
            if (serviceDef is com.epitech.area.sdk.yaml.YamlBasedService) {
                // Les métadonnées sont déjà dans le YAML
                // Le converter est appelé au niveau du loader
                null // Géré séparément
            } else {
                // Services Kotlin hardcodés
                Service(
                    name = serviceDef.name,
                    description = serviceDef.description,
                    category = serviceDef.category,
                    requiresAuth = serviceDef.requiresAuth,
                    authType = if (serviceDef.requiresAuth) "OAuth2" else null,
                    actions = emptyList(), // À définir manuellement pour les services Kotlin
                    reactions = emptyList(),
                    enabled = true
                )
            }
        }
    }

    /**
     * Récupérer la configuration YAML d'un service (si disponible)
     */
    fun getYamlConfig(serviceId: String): com.epitech.area.sdk.yaml.YamlServiceConfig? {
        val service = services[serviceId]
        return if (service is com.epitech.area.sdk.yaml.YamlBasedService) {
            service.getConfig()
        } else {
            null
        }
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
