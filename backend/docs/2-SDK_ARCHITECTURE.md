# 🏗️ Architecture SDK Modulaire AREA

## 🎯 Vue d'ensemble

Le backend AREA utilise une **architecture SDK modulaire** inspirée de **Zapier, n8n et IFTTT**, permettant d'ajouter des centaines de services sans modifier le code core.

```
┌─────────────────────────────────────────────────────────┐
│                   AREA Core Engine                      │
│  ┌───────────────────────────────────────────────────┐  │
│  │          ServiceRegistry (Auto-discovery)         │  │
│  └───────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────┐  │
│  │         AreaRuntime (HTTP, OAuth, Logging)        │  │
│  └───────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────┐  │
│  │        HookProcessor (Workflow Execution)         │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                           ▲
                           │
        ┌──────────────────┴──────────────────┐
        │                                     │
┌───────────────┐  ┌───────────────┐  ┌──────────────┐
│ TimerService  │  │ WebhookSvc    │  │ GmailService │
│   (Timer)     │  │  (Webhook)    │  │   (Gmail)    │
└───────────────┘  └───────────────┘  └──────────────┘
     Plugin           Plugin              Plugin
        │                                     │
┌───────────────┐  ┌───────────────┐  ┌──────────────┐
│ RandomService │  │ MathService   │  │ TextService  │
│   (Random)    │  │    (Math)     │  │    (Text)    │
└───────────────┘  └───────────────┘  └──────────────┘
     Plugin           Plugin              Plugin
        │
┌───────────────┐  ┌───────────────────────────────────┐
│ LoggerService │  │  YAML Services (4 actifs)         │
│   (Logger)    │  │  - Discord, OpenWeather           │
└───────────────┘  │  - GitHub, JSONPlaceholder        │
     Plugin        └───────────────────────────────────┘
```

---

## 📦 Structure du projet

```
backend/
├── src/main/kotlin/com/epitech/area/
│   │
│   ├── sdk/                              ← 🔧 SDK CORE
│   │   ├── ServiceDefinition.kt          ← Interface pour tous les services
│   │   ├── AreaRuntime.kt                ← HTTP client + OAuth + Logging
│   │   ├── ServiceRegistry.kt            ← Registry auto-discovery
│   │   └── ServiceAdapterBridge.kt       ← Bridge ancien système
│   │
│   ├── integrations/                     ← 🔌 SERVICES KOTLIN (7 services)
│   │   ├── timer/
│   │   │   └── TimerService.kt           ← Service Timer
│   │   ├── webhook/
│   │   │   └── WebhookService.kt         ← Service Webhook
│   │   ├── gmail/
│   │   │   └── GmailService.kt           ← Service Gmail
│   │   ├── random/
│   │   │   └── RandomService.kt          ← Service Random
│   │   ├── math/
│   │   │   └── MathService.kt            ← Service Math
│   │   ├── text/
│   │   │   └── TextService.kt            ← Service Text
│   │   └── logger/
│   │       └── LoggerService.kt          ← Service Logger
│   │
│   ├── domain/                           ← 📊 DOMAIN
│   │   ├── entities/
│   │   │   ├── Service.kt                ← Modèle service DB
│   │   │   ├── Area.kt                   ← Modèle AREA
│   │   │   └── User.kt
│   │   └── repositories/
│   │
│   ├── infrastructure/                   ← 🏗️ INFRASTRUCTURE
│   │   ├── DependencyInjection.kt        ← Injection de dépendances
│   │   ├── ServiceInitializer.kt         ← Init services en DB
│   │   ├── hooks/
│   │   │   ├── HookProcessor.kt          ← Exécution des AREA
│   │   │   └── HookScheduler.kt          ← Scheduler polling
│   │   └── oauth/
│   │
│   └── api/                              ← 🌐 API REST
│       └── controllers/
│
└── ADDING_A_SERVICE.md                   ← 📚 Documentation
```

---

## 🚀 Comment ça marche ?

### 1️⃣ **ServiceDefinition** - Interface unifiée

Tous les services héritent de `ServiceDefinition` :

```kotlin
abstract class ServiceDefinition {
    abstract val id: String                    // "slack", "gmail"
    abstract val name: String                  // "Slack", "Gmail"
    abstract val requiresAuth: Boolean         // OAuth2 requis ?

    lateinit var runtime: AreaRuntime          // Injecté automatiquement

    // Triggers (Actions AREA)
    abstract suspend fun executeTrigger(...)

    // Actions (Reactions AREA)
    abstract suspend fun executeAction(...)
}
```

### 2️⃣ **AreaRuntime** - Abstraction totale

Les services utilisent `runtime` pour **tout** :

```kotlin
// HTTP avec auto OAuth2
runtime.get(url, userService = userService)
runtime.post(url, body, userService = userService)

// Logging
runtime.log("Message sent")
runtime.log("Error occurred", LogLevel.ERROR)
```

**Gestion automatique** :
- ✅ OAuth2 token injection
- ✅ Retry avec backoff
- ✅ Rate limiting
- ✅ Request logging

### 3️⃣ **ServiceRegistry** - Auto-discovery

Les services sont enregistrés au démarrage :

```kotlin
val serviceRegistry = ServiceRegistry(httpClient).apply {
    // Services Kotlin hardcodés
    register(TimerService())
    register(WebhookService())
    register(GmailService())
    register(RandomService())
    register(MathService())
    register(TextService())
    register(LoggerService())

    // Services YAML chargés automatiquement
    loadYamlServices()  // Discord, OpenWeather, GitHub, JSONPlaceholder
}
```

### 4️⃣ **HookProcessor** - Exécution des AREA

```
AREA créée par user
    ↓
HookScheduler détecte qu'il faut vérifier
    ↓
HookProcessor.processArea()
    ↓
1. Récupère le service (via ServiceRegistry)
2. Exécute le trigger (executeTrigger)
3. Si success → exécute les reactions (executeAction)
4. Sauvegarde l'exécution en DB
```

---

## 🎨 Patterns utilisés

### Pattern 1 : **Plugin Architecture**
Chaque service = plugin indépendant qui s'enregistre au démarrage.

### Pattern 2 : **Dependency Injection**
Le `runtime` est injecté automatiquement dans chaque service.

### Pattern 3 : **Bridge Pattern**
`ServiceAdapterBridge` convertit nouveaux services → ancien système.

### Pattern 4 : **Registry Pattern**
`ServiceRegistry` maintient la liste de tous les services actifs.

### Pattern 5 : **Template Method**
`ServiceDefinition` définit le squelette, services implémentent les détails.

---

## 🔧 Composants du SDK

### `ServiceDefinition.kt`
Interface de base pour tous les services. Définit :
- Métadonnées (id, name, description, category)
- `executeTrigger()` - Vérifie si action déclenchée
- `executeAction()` - Exécute une réaction
- `validateConfig()` - Valide la configuration

### `AreaRuntime.kt`
Abstraction qui fournit :
- HTTP client intelligent (GET, POST, PUT, DELETE)
- Auto-injection OAuth2
- Logging unifié
- Retry automatique

### `ServiceRegistry.kt`
Registry qui :
- Enregistre tous les services disponibles
- Fournit accès aux services par ID
- Convertit services → entités DB

### `ServiceAdapterBridge.kt`
Bridge pour compatibilité :
- Ancien système : `ServiceAdapter` (interface existante)
- Nouveau système : `ServiceDefinition` (SDK)
- Permet migration progressive

---

## 📊 Workflow complet

### Démarrage de l'application

```
Application.kt
    ↓
DependencyContainer initialise ServiceRegistry
    ↓
ServiceRegistry enregistre les services Kotlin:
  - register(TimerService())
  - register(WebhookService())
  - register(GmailService())
  - register(RandomService())
  - register(MathService())
  - register(TextService())
  - register(LoggerService())
    ↓
ServiceRegistry charge les services YAML:
  - loadYamlServices() → Discord, OpenWeather, GitHub, JSONPlaceholder
    ↓
ServiceInitializer synchronise tout en MongoDB (11 services total)
    ↓
HookScheduler démarre et vérifie les AREA actives
```

### Exécution d'une AREA

```
User crée AREA:
  - Trigger: "Slack.new_message" (channel = "general")
  - Reaction: "Gmail.send_email" (to = "user@example.com")
    ↓
HookScheduler vérifie toutes les 30s
    ↓
HookProcessor.processArea(area)
    ↓
1. Récupère SlackService depuis ServiceRegistry
2. Appelle slackService.executeTrigger("new_message", config, userService)
    ↓
3. SlackService utilise runtime.get() pour appeler Slack API
    ↓ (runtime injecte OAuth token automatiquement)
4. Si nouveau message → retourne TriggerResult.success(data)
    ↓
5. HookProcessor récupère GmailService
6. Appelle gmailService.executeAction("send_email", config, triggerData, userService)
    ↓
7. GmailService utilise runtime.post() pour envoyer email
    ↓
8. ActionResult.success() → AREA exécutée avec succès
    ↓
9. Sauvegarde dans AreaExecution (MongoDB)
```

---

## 🎯 Avantages de cette architecture

### ✅ **Ultra-modulaire**
- Ajouter un service = créer 1 fichier + 2 lignes d'enregistrement
- Zéro modification du core
- Hot-reload possible (futur)

### ✅ **Scalable à l'infini**
- Centaines de services possibles
- Chaque service complètement isolé
- Tests indépendants

### ✅ **Maintenable**
- Code simple et lisible
- Pattern uniforme pour tous les services
- Documentation auto-générée possible

### ✅ **Type-safe**
- Kotlin compile-time checks
- Erreurs détectées avant runtime
- IDE autocomplete

### ✅ **Extensible**
- ✅ Services YAML déjà implémentés (4 actifs)
- Futur : marketplace de services community
- Futur : plugins externes (JAR)

---

## 🔮 Évolutions et État Actuel

### Phase 1 : ✅ **SDK Core** (IMPLÉMENTÉ)
- ✅ Interface ServiceDefinition
- ✅ AreaRuntime
- ✅ ServiceRegistry
- ✅ Bridge vers ancien système

### Phase 2 : ✅ **YAML Definitions** (IMPLÉMENTÉ)
Services ajoutables sans code Kotlin, comme Zapier/n8n :

**Fichiers** : `backend/integrations/*/service.yml`

**Services YAML actifs** :
- **Discord** (Communication) : send_message, send_embed
- **OpenWeatherMap** (Weather) : current_weather_change, temperature_threshold
- **GitHub Public** (Developer Tools) : new_release, new_commit, repo_star_count, new_issue
- **JSONPlaceholder** (Testing) : API de test pour prototypage

**Fonctionnalités** :
- Templating avec `{{config.field}}`, `{{trigger.data}}`, `{{env.VAR}}`
- Extraction JSONPath pour parser les réponses API
- Support POLLING, WEBHOOK, SCHEDULE
- Auth : NONE, API_KEY, OAuth2

**Documentation** : Voir `6-YAML_SERVICES.md` pour ajouter vos propres services

### Phase 3 : **Hot Reload** (À VENIR)
Ajouter/modifier un service sans redémarrer le serveur

### Phase 4 : **Plugin Marketplace** (À VENIR)
```bash
area install @community/notion-integration
area install @company/internal-crm
```

### Phase 5 : **Visual Service Builder** (À VENIR)
Interface web pour créer des services sans code

---

## 📚 Documentation

- **[ADDING_A_SERVICE.md](./ADDING_A_SERVICE.md)** - Guide pour ajouter un service
- **[API_ARCHITECTURE.md](../API_ARCHITECTURE.md)** - Architecture globale de l'API
- **Code examples** - Voir `integrations/*/` pour exemples

---

## 🎉 Résumé

**En 2025, gérer des centaines de services est simple avec l'architecture SDK modulaire.**

Inspiré de :
- 🔷 **Zapier** - SDK JavaScript + Lambda isolation
- 🟢 **n8n** - Node-based architecture
- 🔴 **IFTTT** - Service-based triggers/actions

Résultat : **Architecture simple, puissante et infiniment extensible** 🚀
