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
**PUT** `/api/areas/{id}`
- **Headers** : `Authorization: Bearer <token>`
- **Body** : Même structure que Create
- **Response** : AREA mise à jour
- **Note** : Permet de modifier configuration ou activer/désactiver

### Delete Area
**DELETE** `/api/areas/{id}`
- **Headers** : `Authorization: Bearer <token>`
- **Response** : `{ "message": "Area deleted successfully" }`
- **Effet** : Supprime aussi l'historique d'exécution

### Toggle Area Active State
**PATCH** `/api/areas/{id}/toggle`
- **Headers** : `Authorization: Bearer <token>`
- **Response** : AREA avec nouvel état `active`
- **Utilisation** : Activer/désactiver sans supprimer

### Get Area Executions
**GET** `/api/areas/{id}/executions`
- **Headers** : `Authorization: Bearer <token>`
- **Response** : Historique des exécutions
- **Structure Execution** :
  ```json
  {
    "id": "ObjectId",
    "status": "SUCCESS|FAILED",
    "startedAt": 1234567890,
    "completedAt": 1234567890,
    "actionData": {},
    "error": "null ou message"
  }
  ```

---

## 🪝 Webhook Endpoints

### Trigger Webhook
**POST** `/api/webhooks/trigger/{webhookId}`
- **Params** : `webhookId` (unique ID du webhook)
- **Body** : Données arbitraires (JSON)
- **Headers** : Optionnel `X-Webhook-Secret` pour sécurité
- **Response** : `{ "message": "Webhook triggered", "executionId": "..." }`
- **Note** : Déclenche toutes les AREAs liées à ce webhook

### Register Webhook (Internal)
**POST** `/api/webhooks/register`
- **Headers** : `Authorization: Bearer <token>`
- **Body** : `{ "areaId": "ObjectId" }`
- **Response** : `{ "webhookId": "unique-id", "url": "full-webhook-url" }`
- **Utilisation** : Appelé automatiquement lors de la création d'AREA avec action webhook

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