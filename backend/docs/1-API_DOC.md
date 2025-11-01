# AREA Backend API Documentation

## 📚 Table des matières

1. [Configuration](#base-url)
2. [Public Endpoints](#-public-endpoints-sans-authentification)
3. [Authentication](#-authentication-endpoints)
4. [OAuth2](#-oauth2-endpoints)
5. [Services](#-services-endpoints)
6. [Areas (Automations)](#-areas-endpoints-toutes-nécessitent-authentification-)
7. [Webhooks](#-webhook-endpoints)
8. [Token Structure](#-token-structure)
9. [Flow d'utilisation](#-flow-dutilisation-typique)
10. [Sécurité](#-sécurité)
11. [Debug](#-debug-tips)

## Base URL
```
http://localhost:8080
```

## Authentication
La plupart des endpoints nécessitent un JWT token dans le header :
```
Authorization: Bearer <your-jwt-token>
```

## Response Format
Toutes les réponses sont en JSON. Les erreurs suivent ce format :
```json
{
  "error": "Message d'erreur"
}
```

---

## 🌐 Public Endpoints (Sans authentification)

### Health Check
**GET** `/health`
- **Description** : Vérifie que le serveur fonctionne
- **Response** : `{ "status": "OK", "timestamp": 1234567890 }`
- **Utilisation** : Pour monitoring et vérifier que le backend est accessible

### Server Information
**GET** `/about.json`
- **Description** : Informations sur le serveur et les services disponibles
- **Response** : Liste complète des services avec leurs actions et réactions
- **Utilisation** : Pour découvrir dynamiquement les capacités du serveur

---

## 🔐 Authentication Endpoints

### Register
**POST** `/api/auth/register`
- **Body** : `{ "email": "string", "password": "string", "username": "string" }`
- **Response** : `{ "accessToken": "jwt", "refreshToken": "jwt", "expiresIn": 900000, "tokenType": "Bearer" }`
- **Règles** :
  - Email unique obligatoire
  - Password minimum 8 caractères recommandé
  - Username sera affiché dans l'interface

### Login
**POST** `/api/auth/login`
- **Body** : `{ "email": "string", "password": "string" }`
- **Response** : `{ "accessToken": "jwt", "refreshToken": "jwt", "expiresIn": 900000, "tokenType": "Bearer" }`
- **Note** : Access token expire après 15 minutes, refresh token après 7 jours

### Refresh Token
**POST** `/api/auth/refresh`
- **Body** : `{ "refreshToken": "string" }`
- **Response** : `{ "accessToken": "jwt", "refreshToken": "jwt", "expiresIn": 900000, "tokenType": "Bearer" }`
- **Utilisation** : Renouveler l'access token sans redemander le mot de passe

---

## 🔗 OAuth2 Endpoints

### Initialize OAuth2 Flow
**POST** `/api/auth/oauth/init`
- **Body** : `{ "provider": "google", "mode": "popup|redirect", "state": "optional-csrf-token" }`
- **Response** : `{ "authUrl": "https://accounts.google.com/...", "state": "csrf-token" }`
- **Modes** :
  - `popup` : Pour ouvrir dans une fenêtre popup (recommandé pour SPA)
  - `redirect` : Pour redirection complète de page

### OAuth2 Callback (Web Flow)
**GET** `/api/auth/oauth/{provider}/callback`
- **URL Params** : `code`, `state`, `mode`
- **Comportement** :
  - Mode redirect : Redirige vers frontend avec tokens en query params
  - Mode popup : Retourne HTML qui fait postMessage vers la fenêtre parent
- **Note** : Endpoint appelé automatiquement par le provider OAuth

### Exchange Code for Tokens (Mobile Flow)
**POST** `/api/auth/oauth/{provider}/token`
- **Body** : `{ "code": "authorization-code", "redirectUri": "your-redirect-uri", "state": "optional" }`
- **Response** : `{ "accessToken": "jwt", "refreshToken": "jwt", "user": {...} }`
- **Utilisation** : Pour mobile/Flutter qui capture le code directement

### List OAuth2 Providers
**GET** `/api/auth/oauth/providers`
- **Response** : `{ "providers": [{ "name": "google", "displayName": "Google", "enabled": true, "iconUrl": "..." }] }`
- **Utilisation** : Afficher dynamiquement les boutons de login social disponibles

### Link OAuth Account 🔒
**POST** `/api/auth/oauth/link`
- **Headers** : `Authorization: Bearer <token>`
- **Body** : `{ "provider": "google", "accessToken": "oauth-access-token" }`
- **Response** : `{ "message": "Account linked successfully" }`
- **Utilisation** : Ajouter un login social à un compte existant

### Unlink OAuth Account 🔒
**DELETE** `/api/auth/oauth/unlink`
- **Headers** : `Authorization: Bearer <token>`
- **Body** : `{ "provider": "google" }`
- **Response** : `{ "message": "Account unlinked successfully" }`
- **Règle** : Impossible de délier si c'est le seul moyen d'authentification

### Get Linked Accounts 🔒
**GET** `/api/auth/oauth/linked-accounts`
- **Headers** : `Authorization: Bearer <token>`
- **Response** : `{ "accounts": [{ "provider": "google", "email": "...", "linkedAt": "...", "displayName": "Google" }] }`
- **Utilisation** : Afficher les comptes sociaux liés dans les paramètres

### Refresh OAuth Token 🔒
**POST** `/api/auth/oauth/{provider}/refresh`
- **Headers** : `Authorization: Bearer <token>`
- **Params** : `provider` (google, github, facebook)
- **Response** : `{ "accessToken": "new-oauth-token", "expiresIn": 3600 }`
- **Comportement** :
  - Utilise le refresh token OAuth stocké pour obtenir un nouveau access token du provider
  - Met à jour automatiquement le token dans la base de données
  - Retourne le nouveau access token pour utilisation immédiate
- **Erreurs** :
  - `400` : Pas de refresh token disponible pour ce provider
  - `404` : Utilisateur ou compte OAuth non trouvé
  - `500` : Échec du rafraîchissement auprès du provider
- **Utilisation** : Renouveler l'accès OAuth pour les services qui requirent une authentification (Gmail, Calendar, etc.)

---

## 📦 Services Endpoints

### List All Services
**GET** `/api/services`
- **Response** : Array de services avec leurs actions et réactions disponibles
- **Structure Service** :
  ```json
  {
    "id": "ObjectId",
    "name": "timer",
    "displayName": "Timer",
    "description": "Service de temporisation",
    "category": "productivity",
    "actions": [{ "id": "interval", "name": "Interval Trigger", "description": "...", "configSchema": {} }],
    "reactions": [{ "id": "delay", "name": "Delay", "description": "...", "configSchema": {} }]
  }
  ```

### Get Service by ID
**GET** `/api/services/{id}`
- **Params** : `id` (MongoDB ObjectId)
- **Response** : Service détaillé avec schémas de configuration
- **Utilisation** : Obtenir les détails pour créer une AREA

---

## 🔄 Areas Endpoints (Toutes nécessitent authentification 🔒)

**Résumé des endpoints disponibles:**
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/areas` | Liste toutes les AREAs de l'utilisateur |
| POST | `/api/areas` | Créer une nouvelle AREA |
| GET | `/api/areas/{id}` | Obtenir les détails d'une AREA |
| PATCH | `/api/areas/{id}` | Mettre à jour une AREA |
| DELETE | `/api/areas/{id}` | Supprimer une AREA |
| POST | `/api/areas/{id}/activate` | Activer une AREA |
| POST | `/api/areas/{id}/deactivate` | Désactiver une AREA |
| POST | `/api/areas/{id}/test` | Tester manuellement une AREA |
| GET | `/api/areas/{id}/executions` | Historique des exécutions |
| GET | `/api/areas/{areaId}/executions/{executionId}` | Détails d'une exécution |

---

### List User's Areas
**GET** `/api/areas`
- **Headers** : `Authorization: Bearer <token>`
- **Response** : Array des AREAs de l'utilisateur
- **Structure AREA** :
  ```json
  {
    "id": "ObjectId",
    "name": "Mon automation",
    "description": "Description",
    "active": true,
    "action": {
      "serviceId": "ObjectId",
      "actionId": "interval",
      "config": { "interval": "60" }
    },
    "reactions": [{
      "serviceId": "ObjectId",
      "reactionId": "send_email",
      "config": { "to": "user@example.com" }
    }],
    "executionCount": 42,
    "lastTriggeredAt": 1234567890
  }
  ```

### Create Area
**POST** `/api/areas`
- **Headers** : `Authorization: Bearer <token>`
- **Body** :
  ```json
  {
    "name": "string",
    "description": "string",
    "action": {
      "serviceId": "ObjectId",
      "actionId": "string",
      "config": {}
    },
    "reactions": [{
      "serviceId": "ObjectId",
      "reactionId": "string",
      "config": {}
    }]
  }
  ```
- **Response** : AREA créée avec ID
- **Validation** : ServiceId et actionId/reactionId doivent exister

### Get Area by ID
**GET** `/api/areas/{id}`
- **Headers** : `Authorization: Bearer <token>`
- **Response** : AREA détaillée
- **Sécurité** : Seul le propriétaire peut accéder

### Update Area
**PATCH** `/api/areas/{id}`
- **Headers** : `Authorization: Bearer <token>`
- **Body** :
  ```json
  {
    "name": "string (optional)",
    "description": "string (optional)",
    "active": "boolean (optional)"
  }
  ```
- **Response** : AREA mise à jour
- **Note** : Permet de modifier nom, description ou état actif/inactif

### Delete Area
**DELETE** `/api/areas/{id}`
- **Headers** : `Authorization: Bearer <token>`
- **Response** : `204 No Content` (corps vide)
- **Effet** : Supprime l'AREA et son historique d'exécution

### Activate Area
**POST** `/api/areas/{id}/activate`
- **Headers** : `Authorization: Bearer <token>`
- **Response** : AREA avec `active: true`
- **Utilisation** : Activer une AREA désactivée

### Deactivate Area
**POST** `/api/areas/{id}/deactivate`
- **Headers** : `Authorization: Bearer <token>`
- **Response** : AREA avec `active: false`
- **Utilisation** : Désactiver une AREA sans la supprimer

### Test Area
**POST** `/api/areas/{id}/test`
- **Headers** : `Authorization: Bearer <token>`
- **Response** : `{ "message": "Area test triggered successfully" }`
- **Utilisation** : Déclencher manuellement une AREA pour tester

### Get Area Executions
**GET** `/api/areas/{id}/executions`
- **Headers** : `Authorization: Bearer <token>`
- **Query Parameters** :
  - `limit` : number (default: 50) - Nombre maximum d'exécutions à retourner
- **Response** : Liste des exécutions de l'AREA
- **Structure Execution** :
  ```json
  {
    "id": "ObjectId",
    "areaId": "ObjectId",
    "status": "PENDING|IN_PROGRESS|PROCESSING|SUCCESS|FAILED",
    "startedAt": 1234567890,
    "completedAt": 1234567890,
    "error": "null ou message",
    "progress": 100,
    "totalSteps": 3,
    "currentStep": null,
    "steps": [
      {
        "stepType": "ACTION",
        "stepName": "Action: new_email",
        "stepIndex": 0,
        "status": "SUCCESS",
        "startedAt": 1234567890,
        "completedAt": 1234567891,
        "duration": 1000,
        "error": null
      }
    ]
  }
  ```
- **Note** : `progress` va de 0 à 100%, `currentStep` est présent uniquement pendant l'exécution

### Get Execution Details
**GET** `/api/areas/{areaId}/executions/{executionId}`
- **Headers** : `Authorization: Bearer <token>`
- **Params** :
  - `areaId` : ObjectId de l'AREA
  - `executionId` : ObjectId de l'exécution
- **Response** : Détails complets d'une exécution avec tracking en temps réel
- **Structure** :
  ```json
  {
    "id": "507f1f77bcf86cd799439013",
    "areaId": "507f1f77bcf86cd799439010",
    "status": "IN_PROGRESS",
    "startedAt": 1635789012345,
    "completedAt": null,
    "error": null,
    "currentStep": {
      "type": "REACTION",
      "name": "Reaction: send_message",
      "index": 2,
      "total": 3
    },
    "steps": [
      {
        "stepType": "ACTION",
        "stepName": "Action: new_email",
        "stepIndex": 0,
        "status": "SUCCESS",
        "startedAt": 1635789012345,
        "completedAt": 1635789013000,
        "duration": 655,
        "error": null
      },
      {
        "stepType": "REACTION",
        "stepName": "Reaction 1: send_message",
        "stepIndex": 1,
        "status": "SUCCESS",
        "startedAt": 1635789013000,
        "completedAt": 1635789015678,
        "duration": 2678,
        "error": null
      },
      {
        "stepType": "REACTION",
        "stepName": "Reaction 2: log_info",
        "stepIndex": 2,
        "status": "IN_PROGRESS",
        "startedAt": 1635789015678,
        "completedAt": null,
        "duration": null,
        "error": null
      }
    ],
    "progress": 66,
    "totalSteps": 3
  }
  ```
- **Utilisation** :
  - Suivi en temps réel de l'exécution d'une AREA
  - Afficher une barre de progression
  - Identifier rapidement quelle étape a échoué
  - Mesurer les performances de chaque réaction
- **Note** :
  - `currentStep` est null quand l'exécution est terminée
  - `progress` représente le pourcentage d'avancement (0-100)
  - Chaque `step` contient sa `duration` en millisecondes

---

## 🪝 Webhook Endpoints

### Trigger Webhook
**POST** `/api/webhooks/{serviceId}/{hookId}`
- **Params** :
  - `serviceId` : ObjectId du service (MongoDB ObjectId)
  - `hookId` : ID unique du webhook généré lors de la création de l'AREA
- **Body** : Données arbitraires (texte brut ou JSON) - optionnel
- **Response** : `{ "message": "Webhook received and processing started" }`
- **Comportement** :
  - Vérifie que le webhook est enregistré
  - Vérifie que l'AREA associée existe et est active
  - Déclenche l'exécution de l'AREA de manière asynchrone
  - Retourne immédiatement (pas d'attente de l'exécution)
- **Codes de retour** :
  - `200 OK` : Webhook reçu et traitement démarré
  - `400 Bad Request` : serviceId invalide
  - `404 Not Found` : Webhook non enregistré ou AREA non trouvée
- **Exemple** :
  ```bash
  curl -X POST http://localhost:8080/api/webhooks/507f1f77bcf86cd799439011/my-webhook-id \
    -H "Content-Type: application/json" \
    -d '{"event": "test", "data": "hello"}'
  ```
- **Note** : L'URL complète du webhook est fournie lors de la création d'une AREA avec action webhook

---

## 🔑 Token Structure

### JWT Payload
```json
{
  "userId": "ObjectId",
  "email": "user@example.com",
  "iat": 1234567890,
  "exp": 1234567890
}
```

### Token Lifecycle
- **Access Token** : 15 minutes (900 secondes)
- **Refresh Token** : 7 jours
- **Stratégie** : Rafraîchir automatiquement avant expiration

---

## 📊 Status Codes

- **200** : Succès
- **201** : Ressource créée
- **400** : Requête invalide (vérifier les paramètres)
- **401** : Non authentifié (token manquant ou expiré)
- **403** : Non autorisé (pas les permissions)
- **404** : Ressource non trouvée
- **500** : Erreur serveur

---

## 🚀 Flow d'utilisation typique

### 1. Inscription/Connexion
1. `POST /api/auth/register` ou `/login` → Obtenir tokens
2. Stocker tokens de manière sécurisée
3. Ajouter token dans headers pour requêtes authentifiées

### 2. OAuth2 (Alternative)
1. `POST /api/auth/oauth/init` → Obtenir URL
2. Ouvrir URL dans popup/redirect
3. Récupérer tokens via postMessage ou callback
4. Utiliser tokens comme authentification classique

### 3. Créer une AREA
1. `GET /api/services` → Lister services disponibles
2. Choisir action et reaction(s)
3. `POST /api/areas` → Créer l'AREA
4. AREA s'exécute automatiquement selon trigger

### 4. Monitoring
1. `GET /api/areas` → Lister ses AREAs
2. `GET /api/areas/{id}/executions` → Voir historique
3. `PATCH /api/areas/{id}/toggle` → Activer/désactiver

---

## 🔄 Polling & Real-time

### Hook System
- Le backend vérifie les triggers toutes les 30 secondes
- Les webhooks sont instantanés
- Timer minimum : 60 secondes

### WebSockets (Future)
- Endpoint prévu : `ws://localhost:8080/ws`
- Pour notifications real-time des exécutions

---

## 🛡️ Sécurité

### CORS
Headers autorisés par défaut :
- `http://localhost:3000` (React)
- `http://localhost:8081` (Flutter Web)
- `http://localhost:5173` (Vite)

### Rate Limiting
- 60 requêtes par minute
- 1000 requêtes par heure

### Best Practices
1. **HTTPS en production** obligatoire
2. **Tokens en mémoire** ou secure storage
3. **Refresh avant expiration** pour UX fluide
4. **State parameter** pour OAuth2 CSRF protection

---

## 🐛 Debug Tips

### Token expiré ?
→ Utiliser refresh token : `POST /api/auth/refresh`

### CORS bloqué ?
→ Vérifier que votre origin est dans `.env` : `CORS_ALLOWED_HOSTS`

### OAuth2 ne fonctionne pas ?
→ Vérifier variables d'environnement `GOOGLE_CLIENT_ID` et `GOOGLE_CLIENT_SECRET`

### AREA ne se déclenche pas ?
→ Vérifier que `active: true` et que le service est configuré

---

## 📝 Notes importantes

1. **ObjectId MongoDB** : Format 24 caractères hex (ex: `507f1f77bcf86cd799439011`)
2. **Timestamps** : Millisecondes Unix epoch
3. **Config schemas** : Varient selon service, voir `/api/services` pour détails
4. **Reactions multiples** : Exécutées séquentiellement, pas en parallèle
5. **Pas de pagination** : Pour l'instant, tous les résultats retournés d'un coup