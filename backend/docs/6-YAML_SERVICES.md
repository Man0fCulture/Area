# YAML Services - Documentation

## Introduction

Le système YAML permet d'ajouter des services à AREA sans écrire de code Kotlin, similaire à Zapier/Make/n8n.

## Structure d'un service YAML

```yaml
service:
  id: string                    # Identifiant unique
  name: string                  # Nom affiché
  displayName: string
  description: string
  category: string
  baseUrl: string               # URL de base de l'API (optionnel)

auth:
  type: NONE | API_KEY | OAUTH2
  apiKey:                       # Si API_KEY
    header: string
    prefix: string
    envVar: string              # Variable d'environnement

actions:                        # Triggers
  - id: string
    name: string
    description: string
    type: POLLING | WEBHOOK | SCHEDULE
    pollInterval: number        # En secondes
    config:                     # Configuration utilisateur
      - name: string
        type: STRING | TEXT | NUMBER | EMAIL | URL | SELECT
        label: string
        required: boolean
        default: string
        options: [string]       # Pour SELECT
    endpoint:
      method: GET | POST | PUT | DELETE
      url: string               # Supporte {{variables}}
      params:
        key: value              # Supporte {{variables}}
      headers:
        key: value
    output:                     # Extraction des données
      - name: string
        jsonPath: string
    condition: string           # Ex: "$.count > 0"

reactions:                      # Actions
  - id: string
    name: string
    description: string
    config: [...]
    endpoint:
      method: ...
      url: string
      body:                     # Pour POST/PUT
        key: value
```

## Templating

Variables disponibles dans les URLs, headers et bodies :

- `{{config.field}}` - Configuration utilisateur
- `{{trigger.field}}` - Données du trigger
- `{{env.VAR}}` - Variables d'environnement

## Services disponibles

**📖 Pour la configuration détaillée, voir [YAML_SERVICES_CONFIGURATION.md](./YAML_SERVICES_CONFIGURATION.md)**

### Discord (`integrations/discord/discord.yml`)
- **Auth** : ❌ Aucune (webhook URL fournie par l'utilisateur)
- **Reactions** :
  - `send_message` - Envoyer un message
  - `send_embed` - Envoyer un embed

### OpenWeatherMap (`integrations/openweather/openweather.yml`)
- **Auth** : ✅ API Key requise (gratuite, 1000 calls/jour)
- **Config** : `OPENWEATHER_API_KEY` dans `.env`
- **Actions** :
  - `current_weather_change` - Changement météo
  - `temperature_threshold` - Seuil de température
- **Obtenir une clé** : https://openweathermap.org/api

### GitHub Public (`integrations/github/github.yml`)
- **Auth** : ❌ Aucune (repos publics)
- **Limite** : 60 req/heure sans authentification
- **Actions** :
  - `new_release` - Nouvelle release
  - `new_commit` - Nouveau commit
  - `repo_star_count` - Milestone de stars
  - `new_issue` - Nouvelle issue

### JSONPlaceholder (`integrations/jsonplaceholder/jsonplaceholder.yml`)
- **Auth** : ❌ Aucune
- **Note** : API de test (données non persistées), pour prototypage

## Ajouter un service

1. Créer le dossier :
```bash
mkdir -p backend/integrations/votre-service
```

2. Créer le fichier YAML :
```bash
nano backend/integrations/votre-service/votre-service.yml
```

3. Suivre la structure ci-dessus

4. Redémarrer le backend :
```bash
./gradlew run
```

Le service sera automatiquement chargé et disponible via `/api/services`.

## Exemples d'AREA

### Alerte météo → Discord
```
Trigger: openweather.temperature_threshold (Paris, 30°C)
Reaction: discord.send_message
```

### GitHub Release → Notification
```
Trigger: github_public.new_release (torvalds/linux)
Reaction: discord.send_embed
```

## APIs simples à intégrer

**Sans auth** :
- Cat Facts API
- Dog CEO API
- Advice Slip API
- REST Countries API

**Avec clé gratuite** :
- NASA API
- Unsplash API
- ExchangeRate API

Voir la liste complète : https://github.com/public-apis/public-apis

## Validation

Tester les YAML :
```bash
./test_yaml_services.sh
```

## Architecture technique

Voir `SDK_ARCHITECTURE.md` pour les détails d'implémentation.

Les fichiers sources sont dans :
- `src/main/kotlin/com/epitech/area/sdk/yaml/`
