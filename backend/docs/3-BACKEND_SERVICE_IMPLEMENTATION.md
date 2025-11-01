# Backend - Guide d'implémentation des Services

## Architecture Backend pour ajout rapide de services

### Structure de projet recommandée

```
src/
├── main/kotlin/
│   ├── services/
│   │   ├── core/
│   │   │   ├── Service.kt                 // Interface de base
│   │   │   ├── Action.kt                  // Interface pour actions
│   │   │   ├── Reaction.kt                // Interface pour reactions
│   │   │   ├── ServiceRegistry.kt         // Registry de tous les services
│   │   │   └── OAuth2Provider.kt          // Provider OAuth2 générique
│   │   ├── implementations/
│   │   │   ├── github/
│   │   │   │   ├── GitHubService.kt
│   │   │   │   ├── GitHubOAuthProvider.kt
│   │   │   │   ├── actions/
│   │   │   │   │   ├── NewPushAction.kt
│   │   │   │   │   ├── NewIssueAction.kt
│   │   │   │   │   └── NewPRAction.kt
│   │   │   │   └── reactions/
│   │   │   │       ├── CreateIssueReaction.kt
│   │   │   │       └── AddCommentReaction.kt
│   │   │   ├── discord/
│   │   │   │   ├── DiscordService.kt
│   │   │   │   ├── DiscordOAuthProvider.kt
│   │   │   │   ├── actions/
│   │   │   │   │   └── NewMessageAction.kt
│   │   │   │   └── reactions/
│   │   │   │       └── SendMessageReaction.kt
│   │   │   ├── gmail/
│   │   │   ├── spotify/
│   │   │   ├── twitter/
│   │   │   └── weather/
│   │   └── executor/
│   │       ├── AppletExecutor.kt          // Exécuteur d'applets
│   │       └── TriggerListener.kt         // Listener pour les triggers
```

---

## 1. Interfaces de base

### Service.kt
```kotlin
package services.core

interface Service {
    val id: String
    val name: String
    val slug: String
    val description: String
    val icon: String
    val color: String
    val authType: AuthType
    val categories: List<String>

    fun getActions(): List<Action<*>>
    fun getReactions(): List<Reaction<*>>
    fun getOAuthProvider(): OAuth2Provider?
}

enum class AuthType {
    OAUTH2,
    API_KEY,
    NONE
}
```

### Action.kt
```kotlin
package services.core

interface Action<T : ActionData> {
    val id: String
    val name: String
    val slug: String
    val description: String
    val inputs: List<InputField>
    val outputs: List<OutputField>

    // Pour les triggers polling (vérifie toutes les X minutes)
    suspend fun poll(config: Map<String, Any>, userId: String, accessToken: String): List<T>?

    // Pour les triggers webhook (appelé quand webhook reçu)
    suspend fun handleWebhook(payload: Map<String, Any>, config: Map<String, Any>): T?

    // Validation de la config
    fun validateConfig(config: Map<String, Any>): ValidationResult
}

data class ActionData(
    val data: Map<String, Any>
)
```

### Reaction.kt
```kotlin
package services.core

interface Reaction<T : ReactionResult> {
    val id: String
    val name: String
    val slug: String
    val description: String
    val inputs: List<InputField>
    val outputs: List<OutputField>

    // Exécute la reaction avec les données de l'action
    suspend fun execute(
        config: Map<String, Any>,
        actionData: Map<String, Any>,
        userId: String,
        accessToken: String
    ): T

    // Validation de la config
    fun validateConfig(config: Map<String, Any>): ValidationResult
}

data class ReactionResult(
    val success: Boolean,
    val data: Map<String, Any> = emptyMap(),
    val error: String? = null
)
```

### InputField.kt
```kotlin
package services.core

data class InputField(
    val name: String,
    val label: String,
    val type: InputType,
    val required: Boolean,
    val placeholder: String? = null,
    val defaultValue: Any? = null,
    val options: List<String>? = null,  // Pour les dropdowns
    val validation: ValidationRule? = null
)

enum class InputType {
    STRING,
    TEXT,
    NUMBER,
    BOOLEAN,
    ARRAY,
    SELECT,
    EMAIL,
    URL
}

data class OutputField(
    val name: String,
    val type: String,
    val description: String
)

data class ValidationRule(
    val pattern: String? = null,
    val min: Int? = null,
    val max: Int? = null,
    val minLength: Int? = null,
    val maxLength: Int? = null
)

data class ValidationResult(
    val isValid: Boolean,
    val errors: Map<String, String> = emptyMap()
)
```

---

## 2. Exemple complet: GitHub Service

### GitHubService.kt
```kotlin
package services.implementations.github

import services.core.*
import services.implementations.github.actions.*
import services.implementations.github.reactions.*

class GitHubService : Service {
    override val id = "github"
    override val name = "GitHub"
    override val slug = "github"
    override val description = "Connect to GitHub repositories"
    override val icon = "https://cdn.example.com/github.png"
    override val color = "#181717"
    override val authType = AuthType.OAUTH2
    override val categories = listOf("developer", "productivity")

    override fun getActions() = listOf(
        NewPushAction(),
        NewIssueAction(),
        NewPRAction(),
        NewReleaseAction()
    )

    override fun getReactions() = listOf(
        CreateIssueReaction(),
        AddCommentReaction(),
        CreatePRReaction()
    )

    override fun getOAuthProvider() = GitHubOAuthProvider()
}
```

### GitHubOAuthProvider.kt
```kotlin
package services.implementations.github

import services.core.OAuth2Provider

class GitHubOAuthProvider : OAuth2Provider {
    override val clientId = System.getenv("GITHUB_CLIENT_ID")
    override val clientSecret = System.getenv("GITHUB_CLIENT_SECRET")
    override val authUrl = "https://github.com/login/oauth/authorize"
    override val tokenUrl = "https://github.com/login/oauth/access_token"
    override val scopes = listOf("repo", "read:user", "user:email")

    override suspend fun getAccessToken(code: String): String {
        // HTTP call pour échanger le code contre un token
        val response = httpClient.post(tokenUrl) {
            parameter("client_id", clientId)
            parameter("client_secret", clientSecret)
            parameter("code", code)
        }
        return response.access_token
    }

    override suspend fun refreshToken(refreshToken: String): String {
        // GitHub ne supporte pas refresh token, retourner le même
        return refreshToken
    }

    override suspend fun getUserInfo(accessToken: String): Map<String, Any> {
        val response = httpClient.get("https://api.github.com/user") {
            header("Authorization", "Bearer $accessToken")
        }
        return mapOf(
            "username" to response.login,
            "email" to response.email,
            "avatar" to response.avatar_url
        )
    }
}
```

### actions/NewPushAction.kt
```kotlin
package services.implementations.github.actions

import services.core.*

class NewPushAction : Action<PushData> {
    override val id = "github_new_push"
    override val name = "New Push"
    override val slug = "new_push"
    override val description = "Triggers when a new push is made to a repository"

    override val inputs = listOf(
        InputField(
            name = "repository",
            label = "Repository",
            type = InputType.STRING,
            required = true,
            placeholder = "owner/repo",
            validation = ValidationRule(pattern = "^[a-zA-Z0-9_-]+/[a-zA-Z0-9_-]+$")
        ),
        InputField(
            name = "branch",
            label = "Branch",
            type = InputType.STRING,
            required = false,
            placeholder = "main"
        )
    )

    override val outputs = listOf(
        OutputField("commit_message", "string", "The commit message"),
        OutputField("author", "string", "The commit author"),
        OutputField("sha", "string", "The commit SHA"),
        OutputField("repository", "string", "The repository name"),
        OutputField("branch", "string", "The branch name"),
        OutputField("url", "string", "The commit URL")
    )

    // Polling: vérifie les nouveaux commits toutes les X minutes
    override suspend fun poll(
        config: Map<String, Any>,
        userId: String,
        accessToken: String
    ): List<PushData>? {
        val repo = config["repository"] as String
        val branch = config["branch"] as? String ?: "main"

        // Récupérer le dernier commit checkpointed pour cet applet
        val lastCheckedSha = getLastCheckedCommit(userId, repo, branch)

        // Appel API GitHub
        val commits = httpClient.get("https://api.github.com/repos/$repo/commits") {
            header("Authorization", "Bearer $accessToken")
            parameter("sha", branch)
            parameter("since", lastCheckedSha)
        }

        // Si nouveaux commits, retourner les données
        return commits.map { commit ->
            PushData(
                commitMessage = commit.commit.message,
                author = commit.commit.author.name,
                sha = commit.sha,
                repository = repo,
                branch = branch,
                url = commit.html_url
            )
        }
    }

    // Webhook: GitHub peut appeler ce endpoint directement
    override suspend fun handleWebhook(
        payload: Map<String, Any>,
        config: Map<String, Any>
    ): PushData? {
        val ref = payload["ref"] as String
        val branchName = ref.removePrefix("refs/heads/")
        val configBranch = config["branch"] as? String ?: "main"

        // Vérifier si c'est la bonne branche
        if (branchName != configBranch) return null

        val commits = payload["commits"] as List<Map<String, Any>>
        val lastCommit = commits.lastOrNull() ?: return null

        return PushData(
            commitMessage = lastCommit["message"] as String,
            author = (lastCommit["author"] as Map<*, *>)["name"] as String,
            sha = lastCommit["id"] as String,
            repository = config["repository"] as String,
            branch = branchName,
            url = lastCommit["url"] as String
        )
    }

    override fun validateConfig(config: Map<String, Any>): ValidationResult {
        val errors = mutableMapOf<String, String>()

        val repo = config["repository"] as? String
        if (repo.isNullOrBlank()) {
            errors["repository"] = "Repository is required"
        } else if (!repo.matches(Regex("^[a-zA-Z0-9_-]+/[a-zA-Z0-9_-]+$"))) {
            errors["repository"] = "Repository must be in format owner/repo"
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
}

data class PushData(
    val commitMessage: String,
    val author: String,
    val sha: String,
    val repository: String,
    val branch: String,
    val url: String
) : ActionData(
    data = mapOf(
        "commit_message" to commitMessage,
        "author" to author,
        "sha" to sha,
        "repository" to repository,
        "branch" to branch,
        "url" to url
    )
)
```

### reactions/CreateIssueReaction.kt
```kotlin
package services.implementations.github.reactions

import services.core.*

class CreateIssueReaction : Reaction<CreateIssueResult> {
    override val id = "github_create_issue"
    override val name = "Create Issue"
    override val slug = "create_issue"
    override val description = "Creates a new issue in a repository"

    override val inputs = listOf(
        InputField(
            name = "repository",
            label = "Repository",
            type = InputType.STRING,
            required = true,
            placeholder = "owner/repo"
        ),
        InputField(
            name = "title",
            label = "Issue Title",
            type = InputType.STRING,
            required = true,
            placeholder = "Bug: Something is broken"
        ),
        InputField(
            name = "body",
            label = "Issue Body",
            type = InputType.TEXT,
            required = false,
            placeholder = "Description of the issue"
        ),
        InputField(
            name = "labels",
            label = "Labels",
            type = InputType.ARRAY,
            required = false,
            placeholder = "bug, enhancement"
        )
    )

    override val outputs = listOf(
        OutputField("issue_number", "number", "The created issue number"),
        OutputField("issue_url", "string", "The URL of the created issue")
    )

    override suspend fun execute(
        config: Map<String, Any>,
        actionData: Map<String, Any>,
        userId: String,
        accessToken: String
    ): CreateIssueResult {
        val repo = config["repository"] as String

        // Interpolation des variables {{variable}}
        val title = interpolateVariables(config["title"] as String, actionData)
        val body = config["body"]?.let {
            interpolateVariables(it as String, actionData)
        }
        val labels = config["labels"] as? List<String>

        try {
            val response = httpClient.post("https://api.github.com/repos/$repo/issues") {
                header("Authorization", "Bearer $accessToken")
                setBody(mapOf(
                    "title" to title,
                    "body" to body,
                    "labels" to labels
                ))
            }

            return CreateIssueResult(
                success = true,
                data = mapOf(
                    "issue_number" to response.number,
                    "issue_url" to response.html_url
                )
            )
        } catch (e: Exception) {
            return CreateIssueResult(
                success = false,
                error = "Failed to create issue: ${e.message}"
            )
        }
    }

    override fun validateConfig(config: Map<String, Any>): ValidationResult {
        val errors = mutableMapOf<String, String>()

        if (config["repository"] == null) {
            errors["repository"] = "Repository is required"
        }
        if (config["title"] == null) {
            errors["title"] = "Title is required"
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }

    // Helper pour remplacer {{variable}} par les valeurs
    private fun interpolateVariables(template: String, data: Map<String, Any>): String {
        var result = template
        val regex = Regex("\\{\\{([^}]+)\\}\\}")

        regex.findAll(template).forEach { match ->
            val variableName = match.groupValues[1].trim()
            val value = data[variableName]?.toString() ?: ""
            result = result.replace("{{$variableName}}", value)
        }

        return result
    }
}

data class CreateIssueResult(
    override val success: Boolean,
    override val data: Map<String, Any> = emptyMap(),
    override val error: String? = null
) : ReactionResult(success, data, error)
```

---

## 3. Service Registry

### ServiceRegistry.kt
```kotlin
package services.core

import services.implementations.github.GitHubService
import services.implementations.discord.DiscordService
import services.implementations.gmail.GmailService
import services.implementations.spotify.SpotifyService

object ServiceRegistry {
    private val services = mutableMapOf<String, Service>()

    init {
        // Enregistrer tous les services ici
        register(GitHubService())
        register(DiscordService())
        register(GmailService())
        register(SpotifyService())
        // ... ajouter nouveaux services ici
    }

    fun register(service: Service) {
        services[service.slug] = service
    }

    fun getService(slug: String): Service? = services[slug]

    fun getAllServices(): List<Service> = services.values.toList()

    fun getAction(serviceSlug: String, actionSlug: String): Action<*>? {
        return getService(serviceSlug)
            ?.getActions()
            ?.find { it.slug == actionSlug }
    }

    fun getReaction(serviceSlug: String, reactionSlug: String): Reaction<*>? {
        return getService(serviceSlug)
            ?.getReactions()
            ?.find { it.slug == reactionSlug }
    }
}
```

---

## 4. Executor d'Applets

### AppletExecutor.kt
```kotlin
package services.executor

import services.core.*
import kotlinx.coroutines.*

class AppletExecutor {

    suspend fun executeApplet(applet: Applet, actionData: Map<String, Any>) {
        val execution = createExecution(applet.id)

        try {
            // Exécuter toutes les reactions en séquence
            applet.reactions.sortedBy { it.order }.forEach { reaction ->
                val reactionImpl = ServiceRegistry.getReaction(
                    reaction.serviceSlug,
                    reaction.reactionSlug
                ) ?: throw Exception("Reaction not found")

                val userToken = getUserAccessToken(applet.userId, reaction.serviceSlug)

                val result = reactionImpl.execute(
                    config = reaction.config,
                    actionData = actionData,
                    userId = applet.userId,
                    accessToken = userToken
                )

                // Logger le résultat
                logReactionResult(execution.id, reaction.id, result)

                if (!result.success) {
                    throw Exception(result.error ?: "Reaction failed")
                }
            }

            // Marquer l'exécution comme réussie
            markExecutionSuccess(execution.id)

        } catch (e: Exception) {
            // Marquer l'exécution comme failed
            markExecutionFailed(execution.id, e.message)
        }
    }
}
```

### TriggerListener.kt
```kotlin
package services.executor

import services.core.*
import kotlinx.coroutines.*

class TriggerListener {
    private val pollingJobs = mutableMapOf<String, Job>()

    // Démarre le polling pour tous les applets actifs
    fun startPolling(applet: Applet) {
        val job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    checkTrigger(applet)
                } catch (e: Exception) {
                    logError("Polling error for applet ${applet.id}", e)
                }

                // Attendre 5 minutes entre chaque poll
                delay(5 * 60 * 1000)
            }
        }

        pollingJobs[applet.id] = job
    }

    fun stopPolling(appletId: String) {
        pollingJobs[appletId]?.cancel()
        pollingJobs.remove(appletId)
    }

    private suspend fun checkTrigger(applet: Applet) {
        val action = ServiceRegistry.getAction(
            applet.action.serviceSlug,
            applet.action.actionSlug
        ) ?: return

        val userToken = getUserAccessToken(applet.userId, applet.action.serviceSlug)

        val results = action.poll(
            config = applet.action.config,
            userId = applet.userId,
            accessToken = userToken
        )

        // Si nouveaux résultats, exécuter l'applet pour chaque résultat
        results?.forEach { data ->
            AppletExecutor().executeApplet(applet, data.data)
        }
    }

    // Pour les webhooks
    suspend fun handleWebhook(
        serviceSlug: String,
        actionSlug: String,
        payload: Map<String, Any>
    ) {
        // Trouver tous les applets qui utilisent ce trigger
        val applets = findAppletsByAction(serviceSlug, actionSlug)

        applets.forEach { applet ->
            val action = ServiceRegistry.getAction(serviceSlug, actionSlug) ?: return@forEach

            val data = action.handleWebhook(payload, applet.action.config)

            if (data != null) {
                AppletExecutor().executeApplet(applet, data.data)
            }
        }
    }
}
```

---

## 5. Template pour ajouter un nouveau service RAPIDEMENT

### Créer un nouveau service en 5 minutes

#### 1. Créer le service
```kotlin
// services/implementations/spotify/SpotifyService.kt
class SpotifyService : Service {
    override val id = "spotify"
    override val name = "Spotify"
    override val slug = "spotify"
    override val description = "Control your Spotify playback"
    override val icon = "https://cdn.example.com/spotify.png"
    override val color = "#1DB954"
    override val authType = AuthType.OAUTH2
    override val categories = listOf("music", "entertainment")

    override fun getActions() = listOf(NewLikedSongAction())
    override fun getReactions() = listOf(AddToPlaylistReaction())
    override fun getOAuthProvider() = SpotifyOAuthProvider()
}
```

#### 2. Créer une action
```kotlin
// services/implementations/spotify/actions/NewLikedSongAction.kt
class NewLikedSongAction : Action<LikedSongData> {
    override val id = "spotify_new_liked_song"
    override val name = "New Liked Song"
    override val slug = "new_liked_song"
    override val description = "Triggers when you like a new song"

    override val inputs = emptyList<InputField>()
    override val outputs = listOf(
        OutputField("track_name", "string", "Song name"),
        OutputField("artist", "string", "Artist name"),
        OutputField("album", "string", "Album name"),
        OutputField("spotify_url", "string", "Spotify URL")
    )

    override suspend fun poll(
        config: Map<String, Any>,
        userId: String,
        accessToken: String
    ): List<LikedSongData>? {
        val response = httpClient.get("https://api.spotify.com/v1/me/tracks") {
            header("Authorization", "Bearer $accessToken")
            parameter("limit", 50)
        }

        // Récupérer uniquement les nouvelles depuis le dernier check
        val lastChecked = getLastCheckedTimestamp(userId)

        return response.items
            .filter { it.added_at > lastChecked }
            .map { item ->
                LikedSongData(
                    trackName = item.track.name,
                    artist = item.track.artists[0].name,
                    album = item.track.album.name,
                    spotifyUrl = item.track.external_urls.spotify
                )
            }
    }

    override suspend fun handleWebhook(payload: Map<String, Any>, config: Map<String, Any>) = null
    override fun validateConfig(config: Map<String, Any>) = ValidationResult(true)
}

data class LikedSongData(
    val trackName: String,
    val artist: String,
    val album: String,
    val spotifyUrl: String
) : ActionData(
    data = mapOf(
        "track_name" to trackName,
        "artist" to artist,
        "album" to album,
        "spotify_url" to spotifyUrl
    )
)
```

#### 3. Créer une reaction
```kotlin
// services/implementations/spotify/reactions/AddToPlaylistReaction.kt
class AddToPlaylistReaction : Reaction<AddToPlaylistResult> {
    override val id = "spotify_add_to_playlist"
    override val name = "Add to Playlist"
    override val slug = "add_to_playlist"
    override val description = "Adds a song to a playlist"

    override val inputs = listOf(
        InputField("playlist_id", "Playlist ID", InputType.STRING, true, "37i9dQZF1DXcBWIGoYBM5M")
    )

    override val outputs = listOf(
        OutputField("success", "boolean", "Whether the song was added")
    )

    override suspend fun execute(
        config: Map<String, Any>,
        actionData: Map<String, Any>,
        userId: String,
        accessToken: String
    ): AddToPlaylistResult {
        val playlistId = config["playlist_id"] as String
        val trackUrl = actionData["spotify_url"] as String

        try {
            httpClient.post("https://api.spotify.com/v1/playlists/$playlistId/tracks") {
                header("Authorization", "Bearer $accessToken")
                setBody(mapOf("uris" to listOf(trackUrl)))
            }

            return AddToPlaylistResult(success = true)
        } catch (e: Exception) {
            return AddToPlaylistResult(success = false, error = e.message)
        }
    }

    override fun validateConfig(config: Map<String, Any>): ValidationResult {
        if (config["playlist_id"] == null) {
            return ValidationResult(false, mapOf("playlist_id" to "Required"))
        }
        return ValidationResult(true)
    }
}

data class AddToPlaylistResult(
    override val success: Boolean,
    override val error: String? = null
) : ReactionResult(success, emptyMap(), error)
```

#### 4. Enregistrer dans le Registry
```kotlin
// Dans ServiceRegistry.kt
init {
    register(GitHubService())
    register(SpotifyService()) // ← Ajouter cette ligne
}
```

**C'est tout !** Le service est maintenant disponible via l'API.

---

## 6. Checklist pour ajouter un service

- [ ] Créer `{Service}Service.kt` avec métadonnées
- [ ] Créer `{Service}OAuthProvider.kt` (si OAuth)
- [ ] Pour chaque action:
  - [ ] Créer classe action avec inputs/outputs
  - [ ] Implémenter `poll()` ou `handleWebhook()`
  - [ ] Créer data class pour les résultats
- [ ] Pour chaque reaction:
  - [ ] Créer classe reaction avec inputs/outputs
  - [ ] Implémenter `execute()`
  - [ ] Créer data class pour les résultats
- [ ] Enregistrer dans `ServiceRegistry`
- [ ] Ajouter credentials OAuth dans `.env`
- [ ] Tester avec Postman/curl

---

## 7. Variables d'environnement (.env)

```env
# GitHub
GITHUB_CLIENT_ID=your_client_id
GITHUB_CLIENT_SECRET=your_client_secret

# Discord
DISCORD_CLIENT_ID=your_client_id
DISCORD_CLIENT_SECRET=your_client_secret

# Gmail (Google OAuth)
GOOGLE_CLIENT_ID=your_client_id
GOOGLE_CLIENT_SECRET=your_client_secret

# Spotify
SPOTIFY_CLIENT_ID=your_client_id
SPOTIFY_CLIENT_SECRET=your_client_secret

# Twitter/X
TWITTER_CLIENT_ID=your_client_id
TWITTER_CLIENT_SECRET=your_client_secret

# Database
DATABASE_URL=postgresql://localhost:5432/area
JWT_SECRET=your_jwt_secret

# Webhook base URL
WEBHOOK_BASE_URL=https://api.example.com
```

---

## 8. Tips pour aller vite

### Service sans OAuth (ex: Weather)
```kotlin
class WeatherService : Service {
    // ...
    override val authType = AuthType.API_KEY
    override fun getOAuthProvider() = null
}
```

### Service en lecture seule (pas de reactions)
```kotlin
class WeatherService : Service {
    // ...
    override fun getReactions() = emptyList()
}
```

### Action webhook uniquement (pas de polling)
```kotlin
override suspend fun poll(...) = null
override suspend fun handleWebhook(...): Data { /* logic */ }
```

### Action polling uniquement (pas de webhook)
```kotlin
override suspend fun poll(...): List<Data> { /* logic */ }
override suspend fun handleWebhook(...) = null
```

---

## 9. Services prioritaires à implémenter

**Faciles (< 2h)**
1. ✅ GitHub (OAuth + API REST simple)
2. ✅ Discord (OAuth + webhooks)
3. ✅ Weather (API key, read-only)
4. Webhook custom (déjà dans l'API)

**Moyens (2-4h)**
5. Gmail (Google OAuth + API)
6. Spotify (OAuth + API REST)
7. Twitter/X (OAuth 2.0 + API v2)

**Complexes (4-8h)**
8. Slack (OAuth + Real-time API)
9. Google Calendar (OAuth + Calendar API)
10. Notion (OAuth + API complexe)

---

## 10. Exemple de flow complet

```
1. User crée applet: "GitHub new push → Discord message"

2. Backend:
   - Valide que user a connecté GitHub et Discord
   - Démarre polling GitHub toutes les 5min
   - Stocke config en DB

3. Toutes les 5min:
   - TriggerListener.checkTrigger()
   - NewPushAction.poll() → appelle GitHub API
   - Si nouveau commit → retourne PushData

4. Si nouveau commit détecté:
   - AppletExecutor.executeApplet()
   - Interpole variables: "New push by {{author}}"
   - SendMessageReaction.execute() → appelle Discord API
   - Log execution en DB

5. Frontend:
   - Affiche execution dans /applets/{id}/executions
   - Montre "success" ou "failed" avec durée
```

---

Avec cette architecture, tu peux ajouter **1 service complet en 30min-1h** une fois que tu maîtrises le pattern. C'est plug-and-play comme Zapier !
