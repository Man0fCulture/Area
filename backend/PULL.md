# Backend: implémentation complète du serveur Ktor (Mongo, Redis, RabbitMQ, OAuth2)

## Résumé
- Implémentation du backend Ktor avec architecture hexagonale et services modulaires
- Mise en place des couches API, domaine, infrastructure et intégrations externes (Mongo, Redis, RabbitMQ)
- Ajout d’un flux OAuth2 complet, d’une documentation Swagger et d’un guide d’exploitation détaillé

## Changements détaillés
- **Infrastructure** Initialisation du projet Gradle KTS, wrapper complet et configuration (`build.gradle.kts`, `backend/gradle/wrapper/*`, `backend/gradlew`)
- **Architecture** Structuration par couches avec services applicatifs `AreaService` et `AuthService`, entités domaine et repository interfaces (`backend/src/main/kotlin/com/epitech/area/application/services/*`, `.../domain/*`)
- **Intégrations** Connexions MongoDB, Redis, RabbitMQ, système de hooks (processor, scheduler, registry) et adapters Gmail/Webhook/Timer (`backend/src/main/kotlin/com/epitech/area/infrastructure/**`)
- **API** Contrôleurs REST pour auth, services, areas, webhooks et middleware JWT (`backend/src/main/kotlin/com/epitech/area/api/controllers/*`, `.../middleware/AuthMiddleware.kt`)
- **Sécurité & OAuth2** Service JWT, encodeur Argon2, provider OAuth2 Google + flux popup et callback HTML (`backend/src/main/kotlin/com/epitech/area/infrastructure/security/JwtService.kt`, `.../oauth/*`, `backend/src/main/resources/oauth-callback.html`)
- **Async** Garantie de création des files RabbitMQ avant consommation et améliorations Gradle pour charger automatiquement `.env` (`backend/src/main/kotlin/com/epitech/area/infrastructure/messaging/rabbitmq/*`, `backend/build.gradle.kts`)

## Documentation & CI
- Cahier des charges exhaustif et guide d’utilisation (`backend/README.md`)
- Documentation OpenAPI complète (`backend/swagger.json`)
- Mises à jour CI (`.github/workflows/frontend-prod.yml`, `.github/workflows/node.js.yml`) et ajout du wrapper Docker/Compose (`backend/Dockerfile`, `backend/docker-compose.yml`)

## Configuration
- Fichier `backend/src/main/resources/application.conf` avec variables d’environnement pour Mongo, Redis, RabbitMQ, JWT et CORS
- Nouveau paramètre `FRONTEND_URL` pour synchroniser les redirections OAuth2 côté front
- Exemples fournis via `.env.example` et `run.sh` pour lancer le projet localement

## Tests
- [ ] `./gradlew test`
- [ ] `./gradlew build`
- [ ] `docker compose up`

## Commits
54a975a fix: fixed gradlew file permission  
d2291e3 feat: add configurable frontend URL with OAuth2 redirect fix  
72d8f15 refactor: use Document instead of Map for action data  
fe94dce feat: enhance auth responses with user info and username in JWT  
fbf696e chore: add gradle-wrapper.jar for complete Gradle wrapper  
e7fd8dd chore: add Gradle wrapper for consistent build environment  
95379ff docs: add OAuth2 endpoints to Swagger documentation  
b424301 fix: ensure RabbitMQ queues exist before consuming  
0eb18ce chore: configure automatic .env loading in Gradle  
c801675 feat: add OAuth2 popup flow support  
c46fb0e feat: integrate OAuth2 into authentication system  
d23686f feat: add OAuth2 controller with comprehensive endpoints  
036374d feat: implement OAuth2 service layer with Google provider  
5498a4a feat: add OAuth2 request and response DTOs  
b35582a rm: removed useless README file for POC  
aa9d5c0 Merge remote-tracking branch 'origin/main' into backend  
bf275fa feat: README.md  for project usage  
88194ce docs: add OpenAPI 3.0 Swagger documentation for all API endpoints  
780a2f3 chore(docker): add Docker Compose setup with MongoDB, Redis and RabbitMQ  
e3f9ba3 feat: add main application entry point with Ktor server  
c01973b feat(infrastructure): add dependency injection container and service initializer  
faf5d87 feat(plugins): add Routing plugin with health and about endpoints  
4fa2065 feat(plugins): add Monitoring plugin with call logging  
4085fd0 feat(plugins): add StatusPages plugin for error handling  
c2d8bab feat(plugins): add Serialization plugin with Kotlinx Serialization  
60bba6c feat(plugins): add Security plugin with JWT authentication  
7eea955 feat(api): add Webhooks controller for external triggers  
a46dad9 feat(api): add Areas controller with full CRUD and actions  
e1a1255 feat(api): add services controller for listing available services  
62cc0a1 feat(api): add authentication controller (register, login, refresh)  
6b6adb0 feat(api): add JWT authentication middleware  
12e7997 feat(api): add DTOs for requests and responses  
a5352ff feat(application): add Area service with CRUD and lifecycle management  
a66ff9f feat(application): add authentication service with JWT  
4a08f29 feat(infrastructure): add hook system (processor, scheduler, registry)  
e23fcc2 feat(services): implement Gmail service adapter with OAuth2 support  
cd40e16 feat(services): implement Webhook service adapter with HTTP triggers  
e1f874d feat(services): implement Timer service adapter with polling and scheduling  
b19130a feat(infrastructure): add ServiceAdapter interface for service integrations  
b44324a feat(infrastructure): add JWT service and Argon2 password encoder  
832d00b feat(infrastructure): add RabbitMQ connection, publisher and consumer  
2e0ee76 feat(infrastructure): add Redis connection and cache service  
0149b28 feat(infrastructure): implement MongoDB UserService repository  
747645a feat(infrastructure): implement MongoDB AreaExecution repository  
a10880f feat(infrastructure): implement MongoDB Area repository  
aa36d5e feat(infrastructure): implement MongoDB Service repository  
012d439 feat(infrastructure): implement MongoDB User repository  
1e6e885 feat(infrastructure): add MongoDB connection with fail-fast startup  
d8c65aa feat(infrastructure): add ObjectId serializer for MongoDB  
0aba60e feat(domain): add repository interfaces for data access layer  
5620a13 feat(domain): add core domain entities (User, Service, Area, AreaExecution, UserService)  
4bb77df feat(domain): add Email value object  
d508c86 config: add application configuration files and logback setup  
631d9cc chore: add gitignore and environment configuration template  
5bbff0a build: initial Gradle project setup with Kotlin and Ktor dependencies  
7b3a9c8 Update frontend-prod.yml  
6584062 Create node.js.yml  
e52d680 feat: benchmarks on readme for backend
