# Configuration des Services YAML

## Vue d'ensemble

Les services YAML nécessitent parfois des clés API ou des configurations côté serveur. Voici le guide complet.

## Services sans configuration requise ✅

Ces services fonctionnent immédiatement, aucune configuration serveur nécessaire :

### 1. Discord
- **Type** : Webhooks
- **Configuration** : Par utilisateur (webhook URL dans l'AREA)
- **Setup** : L'utilisateur crée un webhook Discord et fournit l'URL

### 2. GitHub Public
- **Type** : API publique
- **Configuration** : Aucune
- **Limitation** : 60 requêtes/heure sans auth

### 3. JSONPlaceholder
- **Type** : API de test
- **Configuration** : Aucune
- **Note** : Pour tests et développement uniquement

## Services nécessitant une configuration serveur 🔑

### OpenWeatherMap

**Nécessite une clé API** configurée côté serveur.

#### Obtenir la clé API

1. Créer un compte sur https://openweathermap.org
2. Aller dans "API keys"
3. Copier votre clé API (gratuite)

#### Configuration

Ajouter dans `backend/.env` :

```bash
# OpenWeatherMap API
# Free tier: 1000 calls/day, 60 calls/minute
OPENWEATHER_API_KEY=votre_cle_api_ici
```

#### Vérification

```bash
# Tester la clé
curl "https://api.openweathermap.org/data/2.5/weather?q=Paris&appid=VOTRE_CLE"
```

Si la réponse contient des données météo, c'est bon !

#### Free Tier

- **1000 appels/jour**
- **60 appels/minute**
- Données actuelles + prévisions 5 jours
- Gratuit à vie

## Récapitulatif

| Service | Config serveur ? | Fichier | Variable |
|---------|------------------|---------|----------|
| Discord | ❌ Non | - | - |
| GitHub Public | ❌ Non | - | - |
| JSONPlaceholder | ❌ Non | - | - |
| OpenWeatherMap | ✅ Oui | `.env` | `OPENWEATHER_API_KEY` |

## Ajouter un nouveau service avec API key

Si vous créez un service YAML nécessitant une clé API :

### 1. Dans le YAML

```yaml
auth:
  type: API_KEY
  apiKey:
    header: Authorization
    prefix: Bearer
    envVar: VOTRE_SERVICE_API_KEY
```

### 2. Dans `.env.example`

```bash
# Votre Service API
# Description et lien
VOTRE_SERVICE_API_KEY=
```

### 3. Dans la documentation

Ajouter une section dans ce fichier expliquant :
- Comment obtenir la clé
- Les limites du free tier
- Comment tester la clé

## Variables d'environnement disponibles

Dans les YAML, vous pouvez utiliser :

```yaml
endpoint:
  url: "https://api.example.com/data"
  params:
    api_key: "{{env.VOTRE_CLE_API}}"
```

Les variables `{{env.*}}` sont remplacées automatiquement au runtime.

## Sécurité

⚠️ **Ne jamais committer les clés API !**

- Utiliser `.env` (ignoré par git)
- Documenter dans `.env.example`
- Fournir des instructions claires

## Troubleshooting

### OpenWeather retourne une erreur 401

```json
{"cod":401, "message": "Invalid API key"}
```

**Solution** : Vérifier que `OPENWEATHER_API_KEY` est bien défini dans `.env`

### Service YAML ne charge pas

Vérifier les logs au démarrage :
```
✅ Loaded YAML service: OpenWeatherMap (openweather)
```

Si absent, vérifier le fichier YAML dans `integrations/`.

### Variable d'environnement non trouvée

Le service utilisera une valeur vide. Vérifier :
1. La variable est dans `.env`
2. Le backend a été redémarré
3. Le nom de la variable est correct

## Exemple complet : OpenWeatherMap

### Configuration

```bash
# backend/.env
OPENWEATHER_API_KEY=abc123def456ghi789
```

### Utilisation dans une AREA

**Trigger** : Temperature Threshold
```json
{
  "city": "Paris",
  "threshold": "30",
  "condition": "above"
}
```

Le système :
1. Lit `OPENWEATHER_API_KEY` depuis `.env`
2. L'injecte dans la requête API
3. Appelle `https://api.openweathermap.org/data/2.5/weather?q=Paris&appid=abc123...`
4. Retourne les données météo

**Aucune clé API exposée aux utilisateurs** ! Tout est géré côté serveur.

## Best Practices

1. **Toujours** documenter les variables d'environnement
2. **Toujours** fournir `.env.example` à jour
3. **Toujours** expliquer comment obtenir les clés
4. **Tester** avec les vraies APIs avant de commit
5. **Documenter** les limites du free tier

## Support

Pour ajouter un nouveau service nécessitant une config serveur, voir `ADDING_A_SERVICE.md`.
