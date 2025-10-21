# 🚀 Guide : Ajouter un nouveau service AREA

Ce guide explique comment ajouter un nouveau service en **2 étapes simples**.

## 📋 Prérequis

- Kotlin installé
- Accès au projet AREA

---

## ✨ Étape 1 : Créer le service

### 1.1 Créer le dossier du service

```bash
mkdir -p src/main/kotlin/com/epitech/area/integrations/slack
```

### 1.2 Créer la classe du service

**Fichier** : `src/main/kotlin/com/epitech/area/integrations/slack/SlackService.kt`

```kotlin
package com.epitech.area.integrations.slack

import com.epitech.area.domain.entities.UserService
import com.epitech.area.sdk.*
import org.bson.Document

/**
 * Service Slack - Envoi et réception de messages
 */
class SlackService : ServiceDefinition() {
    // Métadonnées du service
    override val id = "slack"
    override val name = "Slack"
    override val description = "Send and receive Slack messages"
    override val category = "Communication"
    override val requiresAuth = true  // OAuth2 requis

    /**
     * Triggers (Actions dans AREA)
     * Vérifie si une condition est remplie
     */
    override suspend fun executeTrigger(
        triggerId: String,
        config: Map<String, String>,
        userService: UserService?
    ): TriggerResult {
        // Vérifier l'authentification
        if (userService == null) {
            return TriggerResult.error("Slack requires authentication")
        }

        return when (triggerId) {
            "new_message" -> checkNewMessage(config, userService)
            else -> TriggerResult.error("Unknown trigger: $triggerId")
        }
    }

    private suspend fun checkNewMessage(
        config: Map<String, String>,
        userService: UserService
    ): TriggerResult {
        val channel = config["channel"]
            ?: return TriggerResult.error("Missing 'channel' parameter")

        return try {
            // Utiliser le runtime pour faire des requêtes HTTP
            val response = runtime.get(
                url = "https://slack.com/api/conversations.history",
                headers = mapOf(
                    "channel" to channel,
                    "limit" to "1"
                ),
                userService = userService  // Auto OAuth2
            )

            // Parser la réponse et retourner le résultat
            // Si pas de nouveau message : TriggerResult.notTriggered()
            // Si nouveau message : TriggerResult.success(data)

            TriggerResult.success(
                "message" to "Hello from Slack",
                "user" to "U12345",
                "channel" to channel,
                "timestamp" to System.currentTimeMillis()
            )
        } catch (e: Exception) {
            runtime.log("Failed to check messages: ${e.message}", LogLevel.ERROR)
            TriggerResult.error("Failed to check messages: ${e.message}")
        }
    }

    /**
     * Actions (Reactions dans AREA)
     * Exécute une action en réponse à un trigger
     */
    override suspend fun executeAction(
        actionId: String,
        config: Map<String, String>,
        triggerData: Document,
        userService: UserService?
    ): ActionResult {
        if (userService == null) {
            return ActionResult.error("Slack requires authentication")
        }

        return when (actionId) {
            "send_message" -> sendMessage(config, userService)
            else -> ActionResult.error("Unknown action: $actionId")
        }
    }

    private suspend fun sendMessage(
        config: Map<String, String>,
        userService: UserService
    ): ActionResult {
        val channel = config["channel"]
            ?: return ActionResult.error("Missing 'channel' parameter")
        val text = config["text"]
            ?: return ActionResult.error("Missing 'text' parameter")

        return try {
            runtime.post(
                url = "https://slack.com/api/chat.postMessage",
                body = mapOf(
                    "channel" to channel,
                    "text" to text
                ),
                userService = userService
            )

            runtime.log("Message sent to $channel")
            ActionResult.success("Message sent successfully")
        } catch (e: Exception) {
            runtime.log("Failed to send message: ${e.message}", LogLevel.ERROR)
            ActionResult.error("Failed to send message: ${e.message}")
        }
    }

    /**
     * Validation de la configuration (optionnel)
     */
    override suspend fun validateTriggerConfig(
        triggerId: String,
        config: Map<String, String>
    ): ValidationResult {
        return when (triggerId) {
            "new_message" -> {
                val channel = config["channel"]
                if (channel.isNullOrBlank()) {
                    ValidationResult(false, "'channel' is required")
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
            "send_message" -> {
                val errors = mutableListOf<String>()
                if (config["channel"].isNullOrBlank()) errors.add("'channel' is required")
                if (config["text"].isNullOrBlank()) errors.add("'text' is required")

                if (errors.isEmpty()) ValidationResult(true)
                else ValidationResult(false, errors)
            }
            else -> ValidationResult(false, "Unknown action: $actionId")
        }
    }
}
```

---

## 🔌 Étape 2 : Enregistrer le service

### 2.1 Ajouter dans `DependencyInjection.kt`

**Fichier** : `infrastructure/DependencyInjection.kt`

```kotlin
import com.epitech.area.integrations.slack.SlackService  // ← Importer

// ...

val serviceRegistry: ServiceRegistry by lazy {
    ServiceRegistry(httpClient).apply {
        register(TimerService())
        register(WebhookService())
        register(GmailService())
        register(SlackService())  // ← Ajouter ici
    }
}
```

### 2.2 Ajouter les définitions dans `ServiceInitializer.kt`

**Fichier** : `infrastructure/ServiceInitializer.kt`

```kotlin
suspend fun initializeServices() {
    logger.info("Initializing services in database from ServiceRegistry...")

    val services = listOf(
        createTimerService(),
        createWebhookService(),
        createGmailService(),
        createSlackService()  // ← Ajouter ici
    )
    // ...
}

// Ajouter la fonction de création
private fun createSlackService(): Service {
    return Service(
        name = "Slack",
        description = "Send and receive Slack messages",
        category = "Communication",
        requiresAuth = true,
        authType = "OAuth2",
        actions = listOf(
            ActionDefinition(
                id = "new_message",
                name = "New message in channel",
                description = "Trigger when new message posted in channel",
                triggerType = TriggerType.POLLING,
                configSchema = mapOf(
                    "channel" to FieldSchema(
                        type = "string",
                        required = true,
                        description = "Channel ID or name"
                    )
                )
            )
        ),
        reactions = listOf(
            ReactionDefinition(
                id = "send_message",
                name = "Send message",
                description = "Post a message to a channel",
                configSchema = mapOf(
                    "channel" to FieldSchema(
                        type = "string",
                        required = true,
                        description = "Channel ID or name"
                    ),
                    "text" to FieldSchema(
                        type = "string",
                        required = true,
                        description = "Message text"
                    )
                )
            )
        ),
        enabled = true
    )
}
```

---

## ✅ C'est tout !

Votre service est maintenant :
- ✅ Enregistré dans le système
- ✅ Disponible via l'API REST
- ✅ Utilisable dans les AREA
- ✅ Intégré au système de hooks

---

## 🎯 API Runtime disponible

Le `runtime` fourni par AREA offre :

### Requêtes HTTP
```kotlin
// GET request
val response = runtime.get(url, headers, userService)

// POST request
val response = runtime.post(url, body, headers, userService)

// PUT request
val response = runtime.put(url, body, headers, userService)

// DELETE request
val response = runtime.delete(url, headers, userService)

// Generic request
val response = runtime.request(url, method, headers, body, userService)
```

### Logging
```kotlin
runtime.log("Info message")
runtime.log("Debug message", LogLevel.DEBUG)
runtime.log("Warning message", LogLevel.WARN)
runtime.log("Error message", LogLevel.ERROR)
```

### Gestion automatique
- ✅ **OAuth2** : Token automatiquement injecté si `userService` fourni
- ✅ **Retry** : Retry automatique avec backoff exponentiel
- ✅ **Rate limiting** : Gestion automatique des limites d'API
- ✅ **Logging** : Logs automatiques de toutes les requêtes

---

## 📝 Résultats disponibles

### TriggerResult
```kotlin
// Succès
TriggerResult.success(data)
TriggerResult.success("key" to value, "key2" to value2)

// Pas déclenché
TriggerResult.notTriggered()

// Erreur
TriggerResult.error("Error message")
```

### ActionResult
```kotlin
// Succès
ActionResult.success("Message sent")

// Erreur
ActionResult.error("Failed to send")
```

### ValidationResult
```kotlin
// Valide
ValidationResult(true)

// Invalide
ValidationResult(false, "Error message")
ValidationResult(false, listOf("Error 1", "Error 2"))
```

---

## 🔐 OAuth2 (services authentifiés)

Si `requiresAuth = true`, définir le provider OAuth2 :

**Fichier** : `infrastructure/oauth/providers/SlackOAuth2Provider.kt`

```kotlin
package com.epitech.area.infrastructure.oauth.providers

import com.epitech.area.infrastructure.oauth.OAuth2Provider
import io.ktor.server.config.*

class SlackOAuth2Provider(config: ApplicationConfig) : OAuth2Provider {
    override val name = "slack"

    private val clientId = config.property("oauth.slack.clientId").getString()
    private val clientSecret = config.property("oauth.slack.clientSecret").getString()

    override fun getAuthorizationUrl(state: String): String {
        return "https://slack.com/oauth/authorize" +
                "?client_id=$clientId" +
                "&scope=chat:write,channels:read" +
                "&state=$state"
    }

    override suspend fun exchangeCodeForToken(code: String): Map<String, Any> {
        // Implémenter l'échange code → token
    }
}
```

Puis l'enregistrer dans `OAuth2Service.kt`.

---

## 🎉 Félicitations !

Vous avez ajouté un nouveau service en **2 étapes** :
1. Créer la classe `ServiceDefinition`
2. L'enregistrer dans `DependencyInjection`

Le système est **ultra-modulaire** et **extensible à l'infini** ! 🚀
