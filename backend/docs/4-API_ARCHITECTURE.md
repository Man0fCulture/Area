# API Architecture - IFTTT/Zapier Clone

## Base URL
```
http://localhost:8080/api/v1
```

## Authentication

### Register
```http
POST /auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "username": "johndoe"
}

Response 201:
{
  "id": "uuid",
  "email": "user@example.com",
  "username": "johndoe",
  "createdAt": "2025-10-03T10:00:00Z"
}
```

### Login
```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}

Response 200:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "refresh_token_here",
  "expiresIn": 3600,
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "username": "johndoe"
  }
}
```

### Refresh Token
```http
POST /auth/refresh
Content-Type: application/json

{
  "refreshToken": "refresh_token_here"
}

Response 200:
{
  "token": "new_jwt_token",
  "expiresIn": 3600
}
```

### Logout
```http
POST /auth/logout
Authorization: Bearer {token}

Response 204: No Content
```

---

## Services (Actions & Reactions)

### Get All Services
```http
GET /services
Authorization: Bearer {token}

Response 200:
{
  "services": [
    {
      "id": "uuid",
      "name": "GitHub",
      "slug": "github",
      "description": "Connect to GitHub repositories",
      "icon": "https://cdn.example.com/github.png",
      "color": "#181717",
      "authType": "oauth2",
      "isActive": true,
      "categories": ["developer", "productivity"]
    },
    {
      "id": "uuid",
      "name": "Gmail",
      "slug": "gmail",
      "description": "Send and receive emails",
      "icon": "https://cdn.example.com/gmail.png",
      "color": "#EA4335",
      "authType": "oauth2",
      "isActive": true,
      "categories": ["communication", "email"]
    }
  ]
}
```

### Get Service Details
```http
GET /services/{serviceId}
Authorization: Bearer {token}

Response 200:
{
  "id": "uuid",
  "name": "GitHub",
  "slug": "github",
  "description": "Connect to GitHub repositories",
  "icon": "https://cdn.example.com/github.png",
  "color": "#181717",
  "authType": "oauth2",
  "isActive": true,
  "categories": ["developer", "productivity"],
  "actions": [
    {
      "id": "uuid",
      "name": "New Push",
      "slug": "new_push",
      "description": "Triggers when a new push is made to a repository",
      "type": "trigger",
      "inputs": [
        {
          "name": "repository",
          "label": "Repository",
          "type": "string",
          "required": true,
          "placeholder": "owner/repo"
        },
        {
          "name": "branch",
          "label": "Branch",
          "type": "string",
          "required": false,
          "placeholder": "main"
        }
      ],
      "outputs": [
        {
          "name": "commit_message",
          "type": "string",
          "description": "The commit message"
        },
        {
          "name": "author",
          "type": "string",
          "description": "The commit author"
        },
        {
          "name": "sha",
          "type": "string",
          "description": "The commit SHA"
        }
      ]
    }
  ],
  "reactions": [
    {
      "id": "uuid",
      "name": "Create Issue",
      "slug": "create_issue",
      "description": "Creates a new issue in a repository",
      "type": "action",
      "inputs": [
        {
          "name": "repository",
          "label": "Repository",
          "type": "string",
          "required": true,
          "placeholder": "owner/repo"
        },
        {
          "name": "title",
          "label": "Issue Title",
          "type": "string",
          "required": true,
          "placeholder": "Bug: Something is broken"
        },
        {
          "name": "body",
          "label": "Issue Body",
          "type": "text",
          "required": false,
          "placeholder": "Description of the issue"
        },
        {
          "name": "labels",
          "label": "Labels",
          "type": "array",
          "required": false,
          "placeholder": "bug, enhancement"
        }
      ],
      "outputs": [
        {
          "name": "issue_number",
          "type": "number",
          "description": "The created issue number"
        },
        {
          "name": "issue_url",
          "type": "string",
          "description": "The URL of the created issue"
        }
      ]
    }
  ]
}
```

---

## Service Authentication

### Get OAuth URL
```http
GET /services/{serviceId}/auth/url
Authorization: Bearer {token}

Response 200:
{
  "authUrl": "https://github.com/login/oauth/authorize?client_id=xxx&redirect_uri=xxx&scope=repo",
  "state": "random_state_string"
}
```

### Connect Service (OAuth Callback)
```http
POST /services/{serviceId}/auth/callback
Authorization: Bearer {token}
Content-Type: application/json

{
  "code": "oauth_code_from_provider",
  "state": "random_state_string"
}

Response 200:
{
  "id": "uuid",
  "serviceId": "uuid",
  "userId": "uuid",
  "isConnected": true,
  "connectedAt": "2025-10-03T10:00:00Z",
  "accountInfo": {
    "username": "johndoe",
    "email": "john@example.com"
  }
}
```

### Get User Connected Services
```http
GET /services/connected
Authorization: Bearer {token}

Response 200:
{
  "connections": [
    {
      "id": "uuid",
      "serviceId": "uuid",
      "serviceName": "GitHub",
      "serviceSlug": "github",
      "serviceIcon": "https://cdn.example.com/github.png",
      "isConnected": true,
      "connectedAt": "2025-10-03T10:00:00Z",
      "accountInfo": {
        "username": "johndoe"
      }
    }
  ]
}
```

### Disconnect Service
```http
DELETE /services/{serviceId}/auth
Authorization: Bearer {token}

Response 204: No Content
```

---

## Applets (Areas/Workflows)

### Create Applet
```http
POST /applets
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "GitHub to Discord",
  "description": "Send Discord notification on new GitHub push",
  "isActive": true,
  "action": {
    "serviceId": "github-service-uuid",
    "actionId": "new-push-action-uuid",
    "config": {
      "repository": "owner/repo",
      "branch": "main"
    }
  },
  "reactions": [
    {
      "serviceId": "discord-service-uuid",
      "reactionId": "send-message-reaction-uuid",
      "config": {
        "channel": "general",
        "message": "New push by {{author}}: {{commit_message}}"
      }
    }
  ]
}

Response 201:
{
  "id": "uuid",
  "userId": "uuid",
  "name": "GitHub to Discord",
  "description": "Send Discord notification on new GitHub push",
  "isActive": true,
  "action": {
    "id": "uuid",
    "serviceId": "github-service-uuid",
    "serviceName": "GitHub",
    "actionId": "new-push-action-uuid",
    "actionName": "New Push",
    "config": {
      "repository": "owner/repo",
      "branch": "main"
    }
  },
  "reactions": [
    {
      "id": "uuid",
      "serviceId": "discord-service-uuid",
      "serviceName": "Discord",
      "reactionId": "send-message-reaction-uuid",
      "reactionName": "Send Message",
      "config": {
        "channel": "general",
        "message": "New push by {{author}}: {{commit_message}}"
      },
      "order": 0
    }
  ],
  "createdAt": "2025-10-03T10:00:00Z",
  "updatedAt": "2025-10-03T10:00:00Z"
}
```

### Get All User Applets
```http
GET /applets
Authorization: Bearer {token}
Query Parameters:
  - page: number (default: 1)
  - limit: number (default: 20)
  - isActive: boolean (optional)
  - serviceId: string (optional)

Response 200:
{
  "applets": [
    {
      "id": "uuid",
      "name": "GitHub to Discord",
      "description": "Send Discord notification on new GitHub push",
      "isActive": true,
      "action": {
        "serviceName": "GitHub",
        "serviceIcon": "https://cdn.example.com/github.png",
        "actionName": "New Push"
      },
      "reactionsCount": 1,
      "executionsCount": 42,
      "lastExecutedAt": "2025-10-03T09:30:00Z",
      "createdAt": "2025-10-01T10:00:00Z"
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 5,
    "totalPages": 1
  }
}
```

### Get Applet Details
```http
GET /applets/{appletId}
Authorization: Bearer {token}

Response 200:
{
  "id": "uuid",
  "userId": "uuid",
  "name": "GitHub to Discord",
  "description": "Send Discord notification on new GitHub push",
  "isActive": true,
  "action": {
    "id": "uuid",
    "serviceId": "github-service-uuid",
    "serviceName": "GitHub",
    "serviceIcon": "https://cdn.example.com/github.png",
    "serviceColor": "#181717",
    "actionId": "new-push-action-uuid",
    "actionName": "New Push",
    "actionDescription": "Triggers when a new push is made",
    "config": {
      "repository": "owner/repo",
      "branch": "main"
    }
  },
  "reactions": [
    {
      "id": "uuid",
      "serviceId": "discord-service-uuid",
      "serviceName": "Discord",
      "serviceIcon": "https://cdn.example.com/discord.png",
      "serviceColor": "#5865F2",
      "reactionId": "send-message-reaction-uuid",
      "reactionName": "Send Message",
      "reactionDescription": "Sends a message to a channel",
      "config": {
        "channel": "general",
        "message": "New push by {{author}}: {{commit_message}}"
      },
      "order": 0
    }
  ],
  "createdAt": "2025-10-03T10:00:00Z",
  "updatedAt": "2025-10-03T10:00:00Z",
  "stats": {
    "totalExecutions": 42,
    "successfulExecutions": 40,
    "failedExecutions": 2,
    "lastExecutedAt": "2025-10-03T09:30:00Z"
  }
}
```

### Update Applet
```http
PUT /applets/{appletId}
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Updated Name",
  "description": "Updated description",
  "isActive": false,
  "action": {
    "serviceId": "github-service-uuid",
    "actionId": "new-push-action-uuid",
    "config": {
      "repository": "owner/new-repo",
      "branch": "develop"
    }
  },
  "reactions": [
    {
      "serviceId": "discord-service-uuid",
      "reactionId": "send-message-reaction-uuid",
      "config": {
        "channel": "dev",
        "message": "New push!"
      }
    }
  ]
}

Response 200: (same structure as Create Applet)
```

### Toggle Applet Status
```http
PATCH /applets/{appletId}/toggle
Authorization: Bearer {token}

Response 200:
{
  "id": "uuid",
  "isActive": true
}
```

### Delete Applet
```http
DELETE /applets/{appletId}
Authorization: Bearer {token}

Response 204: No Content
```

---

## Applet Executions (Logs)

### Get Applet Execution History
```http
GET /applets/{appletId}/executions
Authorization: Bearer {token}
Query Parameters:
  - page: number (default: 1)
  - limit: number (default: 50)
  - status: string (optional: "success", "failed", "pending")
  - from: ISO date (optional)
  - to: ISO date (optional)

Response 200:
{
  "executions": [
    {
      "id": "uuid",
      "appletId": "uuid",
      "status": "success",
      "triggeredAt": "2025-10-03T09:30:00Z",
      "completedAt": "2025-10-03T09:30:02Z",
      "duration": 2000,
      "actionData": {
        "commit_message": "Fix bug",
        "author": "johndoe",
        "sha": "abc123"
      },
      "reactionsResults": [
        {
          "reactionId": "uuid",
          "reactionName": "Send Message",
          "status": "success",
          "executedAt": "2025-10-03T09:30:01Z",
          "duration": 500
        }
      ]
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 50,
    "total": 42,
    "totalPages": 1
  }
}
```

### Get Execution Details
```http
GET /applets/{appletId}/executions/{executionId}
Authorization: Bearer {token}

Response 200:
{
  "id": "uuid",
  "appletId": "uuid",
  "appletName": "GitHub to Discord",
  "status": "success",
  "triggeredAt": "2025-10-03T09:30:00Z",
  "completedAt": "2025-10-03T09:30:02Z",
  "duration": 2000,
  "action": {
    "serviceName": "GitHub",
    "actionName": "New Push",
    "data": {
      "commit_message": "Fix bug",
      "author": "johndoe",
      "sha": "abc123",
      "repository": "owner/repo",
      "branch": "main"
    }
  },
  "reactions": [
    {
      "id": "uuid",
      "serviceName": "Discord",
      "reactionName": "Send Message",
      "status": "success",
      "executedAt": "2025-10-03T09:30:01Z",
      "completedAt": "2025-10-03T09:30:01.5Z",
      "duration": 500,
      "input": {
        "channel": "general",
        "message": "New push by johndoe: Fix bug"
      },
      "output": {
        "message_id": "123456789",
        "sent_at": "2025-10-03T09:30:01.5Z"
      }
    }
  ],
  "error": null
}
```

### Retry Failed Execution
```http
POST /applets/{appletId}/executions/{executionId}/retry
Authorization: Bearer {token}

Response 200:
{
  "id": "new-execution-uuid",
  "status": "pending",
  "triggeredAt": "2025-10-03T10:00:00Z"
}
```

---

## User Profile

### Get Current User
```http
GET /users/me
Authorization: Bearer {token}

Response 200:
{
  "id": "uuid",
  "email": "user@example.com",
  "username": "johndoe",
  "firstName": "John",
  "lastName": "Doe",
  "avatar": "https://cdn.example.com/avatars/user.jpg",
  "createdAt": "2025-01-01T10:00:00Z",
  "stats": {
    "appletsCount": 5,
    "activeAppletsCount": 3,
    "totalExecutions": 150,
    "connectedServicesCount": 4
  }
}
```

### Update User Profile
```http
PATCH /users/me
Authorization: Bearer {token}
Content-Type: application/json

{
  "username": "newusername",
  "firstName": "John",
  "lastName": "Doe",
  "avatar": "https://cdn.example.com/avatars/new.jpg"
}

Response 200: (same structure as Get Current User)
```

### Change Password
```http
POST /users/me/password
Authorization: Bearer {token}
Content-Type: application/json

{
  "currentPassword": "oldpassword",
  "newPassword": "newpassword123"
}

Response 204: No Content
```

### Delete Account
```http
DELETE /users/me
Authorization: Bearer {token}
Content-Type: application/json

{
  "password": "current_password"
}

Response 204: No Content
```

---

## Webhooks (for custom triggers)

### Create Webhook
```http
POST /webhooks
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Custom Webhook",
  "description": "Trigger for custom events"
}

Response 201:
{
  "id": "uuid",
  "userId": "uuid",
  "name": "Custom Webhook",
  "description": "Trigger for custom events",
  "url": "https://api.example.com/webhooks/abc123def456",
  "secret": "webhook_secret_key",
  "createdAt": "2025-10-03T10:00:00Z"
}
```

### List User Webhooks
```http
GET /webhooks
Authorization: Bearer {token}

Response 200:
{
  "webhooks": [
    {
      "id": "uuid",
      "name": "Custom Webhook",
      "description": "Trigger for custom events",
      "url": "https://api.example.com/webhooks/abc123def456",
      "createdAt": "2025-10-03T10:00:00Z",
      "lastTriggeredAt": "2025-10-03T09:45:00Z",
      "triggersCount": 12
    }
  ]
}
```

### Delete Webhook
```http
DELETE /webhooks/{webhookId}
Authorization: Bearer {token}

Response 204: No Content
```

### Trigger Webhook (Public endpoint, no auth)
```http
POST /webhooks/{webhookToken}
Content-Type: application/json
X-Webhook-Secret: webhook_secret_key (optional)

{
  "event": "custom_event",
  "data": {
    "key": "value",
    "another_key": "another_value"
  }
}

Response 200:
{
  "received": true,
  "triggeredApplets": 2
}
```

---

## Statistics & Analytics

### Get User Dashboard Stats
```http
GET /stats/dashboard
Authorization: Bearer {token}
Query Parameters:
  - period: string (optional: "7d", "30d", "90d", default: "7d")

Response 200:
{
  "period": "7d",
  "totalApplets": 5,
  "activeApplets": 3,
  "totalExecutions": 142,
  "successfulExecutions": 138,
  "failedExecutions": 4,
  "successRate": 97.18,
  "executionsByDay": [
    {
      "date": "2025-10-01",
      "total": 20,
      "success": 19,
      "failed": 1
    },
    {
      "date": "2025-10-02",
      "total": 18,
      "success": 18,
      "failed": 0
    }
  ],
  "topServices": [
    {
      "serviceId": "uuid",
      "serviceName": "GitHub",
      "serviceIcon": "https://cdn.example.com/github.png",
      "usageCount": 45
    }
  ]
}
```

### Get Applet Statistics
```http
GET /applets/{appletId}/stats
Authorization: Bearer {token}
Query Parameters:
  - period: string (optional: "7d", "30d", "90d", default: "30d")

Response 200:
{
  "period": "30d",
  "totalExecutions": 89,
  "successfulExecutions": 87,
  "failedExecutions": 2,
  "averageDuration": 1500,
  "executionsByDay": [
    {
      "date": "2025-10-01",
      "total": 3,
      "success": 3,
      "failed": 0,
      "avgDuration": 1200
    }
  ]
}
```

---

## About.json Endpoint (pour mobile)

### Get About.json
```http
GET /about.json
No authentication required

Response 200:
{
  "client": {
    "host": "10.0.2.2:8080"
  },
  "server": {
    "current_time": 1727950800,
    "services": [
      {
        "name": "GitHub",
        "slug": "github",
        "actions": [
          {
            "name": "New Push",
            "description": "Triggers when a new push is made to a repository"
          },
          {
            "name": "New Issue",
            "description": "Triggers when a new issue is created"
          }
        ],
        "reactions": [
          {
            "name": "Create Issue",
            "description": "Creates a new issue in a repository"
          },
          {
            "name": "Add Comment",
            "description": "Adds a comment to an issue or pull request"
          }
        ]
      },
      {
        "name": "Discord",
        "slug": "discord",
        "actions": [
          {
            "name": "New Message",
            "description": "Triggers when a new message is posted in a channel"
          }
        ],
        "reactions": [
          {
            "name": "Send Message",
            "description": "Sends a message to a channel"
          }
        ]
      },
      {
        "name": "Gmail",
        "slug": "gmail",
        "actions": [
          {
            "name": "New Email",
            "description": "Triggers when a new email is received"
          }
        ],
        "reactions": [
          {
            "name": "Send Email",
            "description": "Sends an email"
          }
        ]
      },
      {
        "name": "Spotify",
        "slug": "spotify",
        "actions": [
          {
            "name": "New Liked Song",
            "description": "Triggers when you like a new song"
          }
        ],
        "reactions": [
          {
            "name": "Add to Playlist",
            "description": "Adds a song to a playlist"
          }
        ]
      },
      {
        "name": "Weather",
        "slug": "weather",
        "actions": [
          {
            "name": "Weather Change",
            "description": "Triggers when weather conditions change"
          }
        ],
        "reactions": []
      },
      {
        "name": "Twitter/X",
        "slug": "twitter",
        "actions": [
          {
            "name": "New Tweet from User",
            "description": "Triggers when a user posts a new tweet"
          }
        ],
        "reactions": [
          {
            "name": "Post Tweet",
            "description": "Posts a new tweet"
          }
        ]
      }
    ]
  }
}
```

---

## Error Responses

Toutes les erreurs suivent ce format:

```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human readable error message",
    "details": {
      "field": "Specific error details if applicable"
    }
  }
}
```

### Common Error Codes

- `400 Bad Request`: Invalid request data
  ```json
  {
    "error": {
      "code": "INVALID_REQUEST",
      "message": "Invalid request data",
      "details": {
        "email": "Invalid email format"
      }
    }
  }
  ```

- `401 Unauthorized`: Missing or invalid token
  ```json
  {
    "error": {
      "code": "UNAUTHORIZED",
      "message": "Authentication required"
    }
  }
  ```

- `403 Forbidden`: Insufficient permissions
  ```json
  {
    "error": {
      "code": "FORBIDDEN",
      "message": "You don't have permission to access this resource"
    }
  }
  ```

- `404 Not Found`: Resource not found
  ```json
  {
    "error": {
      "code": "NOT_FOUND",
      "message": "Applet not found"
    }
  }
  ```

- `409 Conflict`: Resource conflict
  ```json
  {
    "error": {
      "code": "CONFLICT",
      "message": "Email already exists"
    }
  }
  ```

- `422 Unprocessable Entity`: Validation error
  ```json
  {
    "error": {
      "code": "VALIDATION_ERROR",
      "message": "Validation failed",
      "details": {
        "repository": "Repository is required",
        "channel": "Channel must be a valid channel name"
      }
    }
  }
  ```

- `429 Too Many Requests`: Rate limit exceeded
  ```json
  {
    "error": {
      "code": "RATE_LIMIT_EXCEEDED",
      "message": "Too many requests. Please try again later.",
      "retryAfter": 60
    }
  }
  ```

- `500 Internal Server Error`: Server error
  ```json
  {
    "error": {
      "code": "INTERNAL_ERROR",
      "message": "An unexpected error occurred"
    }
  }
  ```

- `503 Service Unavailable`: Service temporarily unavailable
  ```json
  {
    "error": {
      "code": "SERVICE_UNAVAILABLE",
      "message": "Service temporarily unavailable"
    }
  }
  ```

---

## Rate Limiting

Headers inclus dans toutes les réponses:
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1727951400
```

---

## Pagination

Tous les endpoints qui retournent des listes incluent la pagination:

```json
{
  "data": [...],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 100,
    "totalPages": 5,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

---

## Webhooks Events (for real-time updates)

Le backend peut envoyer des événements WebSocket aux clients connectés:

### WebSocket Connection
```
ws://localhost:8080/ws?token={jwt_token}
```

### Events Format
```json
{
  "type": "APPLET_EXECUTED",
  "timestamp": "2025-10-03T10:00:00Z",
  "data": {
    "appletId": "uuid",
    "executionId": "uuid",
    "status": "success"
  }
}
```

### Event Types
- `APPLET_EXECUTED`: Applet execution completed
- `APPLET_CREATED`: New applet created
- `APPLET_UPDATED`: Applet updated
- `APPLET_DELETED`: Applet deleted
- `SERVICE_CONNECTED`: Service connected
- `SERVICE_DISCONNECTED`: Service disconnected
- `EXECUTION_FAILED`: Execution failed

---

## Variable Interpolation

Dans les configurations des reactions, vous pouvez utiliser les variables des actions avec la syntaxe `{{variable_name}}`.

Exemple:
```json
{
  "message": "New push by {{author}} on {{repository}}: {{commit_message}}"
}
```

Les variables disponibles dépendent de l'action source et sont listées dans le champ `outputs` de chaque action.

---

## Notes d'implémentation pour le frontend

1. **Token Storage**: Stocker le JWT dans localStorage ou sessionStorage
2. **Token Refresh**: Implémenter un intercepteur pour rafraîchir automatiquement le token avant expiration
3. **Error Handling**: Créer un handler global pour les erreurs API
4. **Loading States**: Gérer les états de chargement pour toutes les requêtes
5. **Optimistic Updates**: Pour une meilleure UX, mettre à jour l'UI immédiatement puis rollback en cas d'erreur
6. **WebSocket Reconnection**: Implémenter la reconnexion automatique en cas de déconnexion
7. **Debouncing**: Pour les recherches et filtres, implémenter du debouncing (300-500ms)
8. **Cache**: Considérer l'utilisation de React Query ou SWR pour le caching automatique
9. **Pagination**: Implémenter l'infinite scroll ou la pagination classique selon l'UX souhaitée
10. **Variable Editor**: Créer un composant d'édition avec auto-complétion pour les variables `{{var}}`

---

## Services Suggérés (exemples)

### GitHub
- Actions: New Push, New Issue, New PR, New Release, New Star
- Reactions: Create Issue, Add Comment, Create PR, Merge PR, Add Label

### Discord
- Actions: New Message, New Member, Reaction Added
- Reactions: Send Message, Create Channel, Add Role, Send DM

### Gmail
- Actions: New Email, New Email from Sender, New Label
- Reactions: Send Email, Add Label, Mark as Read, Move to Folder

### Spotify
- Actions: New Liked Song, Now Playing, New Playlist
- Reactions: Add to Playlist, Create Playlist, Like Song

### Weather
- Actions: Weather Change, Temperature Threshold, Rain Alert
- Reactions: (none - service en lecture seule)

### Twitter/X
- Actions: New Tweet, New Mention, New Follower
- Reactions: Post Tweet, Like Tweet, Retweet, Follow User

### Slack
- Actions: New Message, New Channel, New Mention
- Reactions: Send Message, Create Channel, Add Reaction

### Google Calendar
- Actions: New Event, Event Starting Soon, Event Updated
- Reactions: Create Event, Update Event, Delete Event

### Trello
- Actions: New Card, Card Moved, Card Commented
- Reactions: Create Card, Move Card, Add Comment, Add Label

### Notion
- Actions: New Page, Page Updated, Database Item Added
- Reactions: Create Page, Update Page, Add Database Item
