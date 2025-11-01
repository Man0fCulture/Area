package com.epitech.area.sdk.yaml

import com.charleskorn.kaml.Yaml
import com.epitech.area.sdk.AreaRuntime
import com.epitech.area.sdk.ServiceDefinition
import java.io.File

/**
 * Loader pour charger des services depuis des fichiers YAML
 * Permet d'ajouter des services sans recompiler le backend !
 */
class YamlServiceLoader(private val runtime: AreaRuntime) {

    private val yaml = Yaml.default

    /**
     * Charger un service depuis un fichier YAML
     */
    fun loadFromFile(yamlFile: File): ServiceDefinition? {
        return try {
            val content = yamlFile.readText()
            val config = yaml.decodeFromString(YamlServiceConfig.serializer(), content)

            val service = YamlBasedService(config)
            service.runtime = runtime

            println("✅ Loaded YAML service: ${config.service.name} (${config.service.id})")
            service
        } catch (e: Exception) {
            println("❌ Failed to load YAML service from ${yamlFile.name}: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * Charger un service depuis une string YAML
     */
    fun loadFromString(yamlContent: String): ServiceDefinition? {
        return try {
            val config = yaml.decodeFromString(YamlServiceConfig.serializer(), yamlContent)

            val service = YamlBasedService(config)
            service.runtime = runtime

            println("✅ Loaded YAML service: ${config.service.name} (${config.service.id})")
            service
        } catch (e: Exception) {
            println("❌ Failed to load YAML service: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * Charger tous les services depuis un dossier
     * Cherche les fichiers *.yml ou *.yaml
     */
    fun loadFromDirectory(directory: File): List<ServiceDefinition> {
        if (!directory.exists() || !directory.isDirectory) {
            println("⚠️  Directory not found: ${directory.absolutePath}")
            return emptyList()
        }

        println("📂 Loading YAML services from: ${directory.absolutePath}")

        val services = mutableListOf<ServiceDefinition>()

        directory.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                // Chercher un fichier YAML avec le nom du dossier
                val yamlFile = File(file, "${file.name}.yml")
                    .takeIf { it.exists() }
                    ?: File(file, "${file.name}.yaml")
                        .takeIf { it.exists() }

                yamlFile?.let {
                    loadFromFile(it)?.let { service ->
                        services.add(service)
                    }
                }
            } else if (file.extension in listOf("yml", "yaml")) {
                loadFromFile(file)?.let { service ->
                    services.add(service)
                }
            }
        }

        println("✅ Loaded ${services.size} YAML services")
        return services
    }

    /**
     * Recharger un service (hot-reload)
     */
    fun reload(yamlFile: File): ServiceDefinition? {
        println("🔄 Reloading service from ${yamlFile.name}...")
        return loadFromFile(yamlFile)
    }
}
