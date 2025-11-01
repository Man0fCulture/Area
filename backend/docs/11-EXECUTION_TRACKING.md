# Système de Suivi d'Exécution en Temps Réel

**Feature**: Tracking détaillé des exécutions d'AREA
**Status**: ✅ Implémenté
**Version**: 1.0
**Date**: 2024-11-01

---

## 🎯 Vue d'ensemble

Le système de suivi d'exécution permet de **monitorer en temps réel** l'exécution de chaque AREA, étape par étape, avec :
- ✅ Barre de progression (0-100%)
- ✅ Étape actuelle en cours
- ✅ Historique complet de toutes les étapes
- ✅ Durée de chaque étape en millisecondes
- ✅ Identification rapide des erreurs

**Cas d'usage** :
- Afficher une barre de progression sur le frontend
- Débugger pourquoi une AREA échoue
- Mesurer les performances des réactions
- Identifier les goulots d'étranglement

---

## 📊 Architecture

### Entités Principales

#### 1. AreaExecution

**Fichier** : `domain/entities/AreaExecution.kt`

```kotlin
data class AreaExecution(
    val id: ObjectId,
    val areaId: ObjectId,
    val status: ExecutionStatus,           // PENDING, IN_PROGRESS, PROCESSING, SUCCESS, FAILED
    val startedAt: Long,
    val completedAt: Long?,
    val error: String?,
    val currentStep: ExecutionStep?,        // Étape en cours (null si terminé)
    val steps: List<ExecutionStepRecord>,   // Historique complet
    val progress: Int,                      // 0-100%
    val totalSteps: Int                     // 1 action + N réactions
)
```

#### 2. ExecutionStep

Représente l'**étape actuellement en exécution** :

```kotlin
data class ExecutionStep(
    val type: StepType,        // ACTION ou REACTION
    val name: String,          // "Executing action" / "Reaction: send_email"
    val index: Int,            // 0 pour action, 1+ pour réactions
    val total: Int             // Nombre total de steps
)

enum class StepType {
    ACTION,      // L'étape de trigger
    REACTION     // Une des réactions
}
```

**Exemple** :
```json
{
  "type": "REACTION",
  "name": "Reaction: send_message",
  "index": 2,
  "total": 4
}
```

#### 3. ExecutionStepRecord

Représente une **étape terminée** dans l'historique :

```kotlin
data class ExecutionStepRecord(
    val stepType: StepType,
    val stepName: String,          // "Action: new_email" ou "Reaction 1: send_message"
    val stepIndex: Int,
    val status: ExecutionStatus,    // SUCCESS ou FAILED
    val startedAt: Long,
    val completedAt: Long?,
    val error: String?,
    val duration: Long?             // completedAt - startedAt (en ms)
)
```

**Exemple** :
```json
{
  "stepType": "REACTION",
  "stepName": "Reaction 1: send_message",
  "stepIndex": 1,
  "status": "SUCCESS",
  "startedAt": 1635789013000,
  "completedAt": 1635789015678,
  "duration": 2678,
  "error": null
}
```

#### 4. ExecutionStatus

```kotlin
enum class ExecutionStatus {
    PENDING,       // Créé mais pas encore démarré
    IN_PROGRESS,   // En cours d'exécution (step actif)
    PROCESSING,    // Entre deux steps
    SUCCESS,       // Terminé avec succès
    FAILED         // Terminé avec erreur
}
```

---

## 🔄 Flux d'Exécution

### 1. Création de l'exécution

```kotlin
val execution = AreaExecution(
    areaId = area.id,
    status = ExecutionStatus.PENDING,
    startedAt = System.currentTimeMillis(),
    totalSteps = 1 + area.reactions.size,  // 1 action + N réactions
    progress = 0
)
areaExecutionRepository.create(execution)
```

### 2. Exécution de l'action

```kotlin
// Marquer l'étape comme en cours
updateExecutionWithStep(
    execution.id,
    ExecutionStatus.IN_PROGRESS,
    ExecutionStep(StepType.ACTION, "Executing action", 0, totalSteps),
    progress = 0
)

// Exécuter l'action
val actionStartTime = System.currentTimeMillis()
val actionResult = executeAction(area)
val actionEndTime = System.currentTimeMillis()

// Enregistrer le résultat
val actionStepRecord = ExecutionStepRecord(
    stepType = StepType.ACTION,
    stepName = "Action: ${area.action.actionId}",
    stepIndex = 0,
    status = if (actionResult.success) SUCCESS else FAILED,
    startedAt = actionStartTime,
    completedAt = actionEndTime,
    duration = actionEndTime - actionStartTime,
    error = actionResult.error
)

updateExecutionWithStepComplete(execution.id, PROCESSING, actionStepRecord)
```

### 3. Exécution des réactions

Pour chaque réaction :

```kotlin
val stepIndex = reactionIndex + 1
val progress = ((stepIndex.toDouble() / totalSteps) * 100).toInt()

// Marquer comme en cours
updateExecutionWithStep(
    execution.id,
    IN_PROGRESS,
    ExecutionStep(REACTION, "Reaction: ${reaction.reactionId}", stepIndex, totalSteps),
    progress
)

// Exécuter
val reactionStartTime = System.currentTimeMillis()
val reactionResult = executeReaction(reaction, actionData, userId)
val reactionEndTime = System.currentTimeMillis()

// Enregistrer
val reactionStepRecord = ExecutionStepRecord(
    stepType = REACTION,
    stepName = "Reaction ${stepIndex}: ${reaction.reactionId}",
    stepIndex = stepIndex,
    status = if (reactionResult.success) SUCCESS else FAILED,
    startedAt = reactionStartTime,
    completedAt = reactionEndTime,
    duration = reactionEndTime - reactionStartTime,
    error = reactionResult.error
)

updateExecutionWithStepComplete(execution.id, PROCESSING, reactionStepRecord)
```

### 4. Finalisation

```kotlin
if (allReactionsSucceeded) {
    updateExecutionStatus(execution.id, SUCCESS, progress = 100)
} else {
    updateExecutionStatus(execution.id, FAILED, progress = 100, error = errors.joinToString())
}
```

---

## 🌐 API Endpoints

### 1. Lister les exécutions

**GET** `/api/areas/{id}/executions?limit=50`

**Headers** :
```
Authorization: Bearer <jwt-token>
```

**Response** :
```json
[
  {
    "id": "507f1f77bcf86cd799439013",
    "areaId": "507f1f77bcf86cd799439010",
    "status": "SUCCESS",
    "startedAt": 1635789012345,
    "completedAt": 1635789025678,
    "error": null,
    "progress": 100,
    "totalSteps": 3,
    "currentStep": null,
    "steps": [...]
  }
]
```

### 2. Obtenir les détails d'une exécution

**GET** `/api/areas/{areaId}/executions/{executionId}`

**Headers** :
```
Authorization: Bearer <jwt-token>
```

**Response** :
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

---

## 💡 Exemples d'Utilisation Frontend

### 1. Barre de Progression

```typescript
function ProgressBar({ execution }: { execution: AreaExecution }) {
  return (
    <div className="w-full bg-gray-200 rounded-full h-2.5">
      <div
        className="bg-blue-600 h-2.5 rounded-full transition-all"
        style={{ width: `${execution.progress}%` }}
      />
      <p className="text-sm mt-1">
        {execution.currentStep
          ? `${execution.currentStep.name} (${execution.progress}%)`
          : execution.status
        }
      </p>
    </div>
  );
}
```

### 2. Timeline des Étapes

```typescript
function ExecutionTimeline({ steps }: { steps: ExecutionStepRecord[] }) {
  return (
    <div className="space-y-4">
      {steps.map((step, index) => (
        <div key={index} className="flex items-start">
          {/* Icon based on status */}
          <div className={`w-8 h-8 rounded-full flex items-center justify-center ${
            step.status === 'SUCCESS' ? 'bg-green-500' :
            step.status === 'FAILED' ? 'bg-red-500' :
            'bg-yellow-500'
          }`}>
            {step.status === 'SUCCESS' && '✓'}
            {step.status === 'FAILED' && '✗'}
            {step.status === 'IN_PROGRESS' && '⟳'}
          </div>

          {/* Step info */}
          <div className="ml-4">
            <h4 className="font-semibold">{step.stepName}</h4>
            {step.duration && (
              <p className="text-sm text-gray-600">{step.duration}ms</p>
            )}
            {step.error && (
              <p className="text-sm text-red-600">{step.error}</p>
            )}
          </div>
        </div>
      ))}
    </div>
  );
}
```

### 3. Polling en Temps Réel

```typescript
function useExecutionTracking(areaId: string, executionId: string) {
  const [execution, setExecution] = useState<AreaExecution | null>(null);

  useEffect(() => {
    const interval = setInterval(async () => {
      const response = await fetch(
        `/api/areas/${areaId}/executions/${executionId}`,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      const data = await response.json();
      setExecution(data);

      // Stop polling si terminé
      if (data.status === 'SUCCESS' || data.status === 'FAILED') {
        clearInterval(interval);
      }
    }, 1000); // Poll toutes les secondes

    return () => clearInterval(interval);
  }, [areaId, executionId]);

  return execution;
}
```

---

## 🔍 Cas d'Usage Avancés

### 1. Identifier les Réactions Lentes

```typescript
const slowReactions = execution.steps
  .filter(step => step.duration && step.duration > 3000)
  .map(step => ({
    name: step.stepName,
    duration: step.duration,
    message: `⚠️ Réaction lente : ${step.duration}ms`
  }));
```

### 2. Taux de Succès par Étape

```sql
-- Analyse MongoDB
db.area_executions.aggregate([
  { $unwind: "$steps" },
  { $group: {
    _id: "$steps.stepName",
    total: { $sum: 1 },
    successes: {
      $sum: { $cond: [{ $eq: ["$steps.status", "SUCCESS"] }, 1, 0] }
    }
  }},
  { $project: {
    stepName: "$_id",
    successRate: { $multiply: [{ $divide: ["$successes", "$total"] }, 100] }
  }}
])
```

### 3. Dashboard Analytics

```typescript
interface ExecutionStats {
  totalExecutions: number;
  successRate: number;
  averageDuration: number;
  failedSteps: Array<{
    stepName: string;
    failureCount: number;
    mostCommonError: string;
  }>;
}

function calculateStats(executions: AreaExecution[]): ExecutionStats {
  const total = executions.length;
  const successes = executions.filter(e => e.status === 'SUCCESS').length;

  const durations = executions
    .filter(e => e.completedAt)
    .map(e => e.completedAt! - e.startedAt);

  const avgDuration = durations.reduce((a, b) => a + b, 0) / durations.length;

  return {
    totalExecutions: total,
    successRate: (successes / total) * 100,
    averageDuration: avgDuration,
    failedSteps: analyzeFailedSteps(executions)
  };
}
```

---

## 🚀 Performance

### Optimisations Implémentées

1. **Index MongoDB** :
   ```javascript
   db.area_executions.createIndex({ areaId: 1, startedAt: -1 })
   db.area_executions.createIndex({ status: 1 })
   ```

2. **Limit par défaut** : 50 exécutions max par requête

3. **Projection partielle** : Possibilité d'exclure `steps` pour la liste

### Recommandations

- **Archivage** : Supprimer les exécutions > 30 jours
- **Pagination** : Implémenter pagination côté frontend
- **Cache** : Mettre en cache les exécutions terminées

---

## 🔧 Implémentation Backend

### Fichiers Principaux

```
backend/src/main/kotlin/com/epitech/area/
├── domain/entities/
│   └── AreaExecution.kt              ← Entités de tracking
├── infrastructure/hooks/
│   └── HookProcessor.kt              ← Logique d'exécution avec tracking
├── domain/repositories/
│   └── AreaExecutionRepository.kt    ← Interface repository
├── infrastructure/persistence/mongodb/
│   └── MongoAreaExecutionRepository.kt  ← Implémentation MongoDB
├── api/controllers/
│   └── AreasController.kt            ← Endpoints API
├── api/dto/responses/
│   └── ExecutionResponses.kt         ← DTOs sérialisables
└── application/services/
    └── AreaService.kt                ← Service métier
```

### Méthodes Clés HookProcessor

```kotlin
// Marquer une étape comme en cours
private suspend fun updateExecutionWithStep(
    executionId: ObjectId,
    status: ExecutionStatus,
    currentStep: ExecutionStep,
    progress: Int
)

// Enregistrer une étape terminée
private suspend fun updateExecutionWithStepComplete(
    executionId: ObjectId,
    status: ExecutionStatus,
    stepRecord: ExecutionStepRecord,
    actionData: Document? = null,
    error: String? = null
): AreaExecution
```

---

## 📈 Métriques Disponibles

Pour chaque exécution :
- ✅ **Temps total** : `completedAt - startedAt`
- ✅ **Temps par étape** : Chaque `ExecutionStepRecord.duration`
- ✅ **Taux de réussite** : `steps.filter(SUCCESS).length / totalSteps`
- ✅ **Progression** : `progress` (0-100%)

Pour une AREA :
- ✅ **Nombre d'exécutions** : `Area.executionCount`
- ✅ **Dernière exécution** : `Area.lastTriggeredAt`
- ✅ **Historique complet** : `AreaExecution[]`

---

## 🎓 Best Practices

### Backend

1. **Toujours mesurer la durée** :
   ```kotlin
   val start = System.currentTimeMillis()
   // ... operation ...
   val duration = System.currentTimeMillis() - start
   ```

2. **Gérer les erreurs proprement** :
   ```kotlin
   try {
     executeReaction(...)
   } catch (e: Exception) {
     stepRecord.error = e.message
     stepRecord.status = FAILED
   }
   ```

3. **Mettre à jour atomiquement** :
   ```kotlin
   // Ne jamais perdre d'étapes
   execution.copy(steps = execution.steps + newStep)
   ```

### Frontend

1. **Polling intelligent** :
   - Poll toutes les 1s si `IN_PROGRESS`
   - Stop quand `SUCCESS` ou `FAILED`
   - Utiliser WebSocket si disponible (futur)

2. **Gérer les états de chargement** :
   ```typescript
   if (!execution) return <Spinner />;
   if (execution.status === 'FAILED') return <ErrorView />;
   return <SuccessView />;
   ```

3. **Afficher les durées de façon lisible** :
   ```typescript
   function formatDuration(ms: number): string {
     if (ms < 1000) return `${ms}ms`;
     return `${(ms / 1000).toFixed(1)}s`;
   }
   ```

---

## 🔮 Évolutions Futures

### Phase 1 : ✅ Tracking Basique (IMPLÉMENTÉ)
- Steps avec durées
- Progress bar
- Status en temps réel

### Phase 2 : WebSocket (À VENIR)
```typescript
const ws = new WebSocket('ws://localhost:8080/ws/executions');
ws.on('execution_update', (data) => {
  // Mise à jour temps réel sans polling
});
```

### Phase 3 : Analytics Avancés (À VENIR)
- Graphes de performance
- Alertes sur réactions lentes
- Prédiction de durée

### Phase 4 : Replay & Debug (À VENIR)
- Rejouer une exécution ratée
- Mode step-by-step pour debug
- Logs détaillés par étape

---

## 📚 Ressources

- **API Doc** : `1-API_DOC.md` - Endpoints complets
- **Architecture** : `2-SDK_ARCHITECTURE.md` - Vue d'ensemble
- **Code** :
  - Entités : `domain/entities/AreaExecution.kt`
  - Logique : `infrastructure/hooks/HookProcessor.kt`
  - API : `api/controllers/AreasController.kt`

---

**Dernière mise à jour** : 2024-11-01
**Auteur** : AREA Backend Team
**Version** : 1.0
