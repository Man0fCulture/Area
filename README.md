# Proof of Concept – Backend

## 🎯 Objectif

Ce projet est un POC (Proof of Concept) réalisé dans le cadre d’une étude comparative de plusieurs langages backend et bases de données.
L’objectif est de tester Kotlin comme langage backend principal, d’évaluer son intégration avec MongoDB et d’expérimenter des patterns modernes de communication et de cache (RabbitMQ, Redis).

## 🚀 Pourquoi Kotlin ?

* 🟦 **Oriented Object (POO)** : Kotlin offre une approche très orientée objet, parfaitement adaptée à notre besoin de structurer un projet complexe avec beaucoup de services.
* 🛠 **Interopérabilité JVM** : accès à tout l’écosystème Java (librairies, frameworks, outils).
* ⚡ **Performance** : compilé en bytecode JVM → exécution rapide et stable.
* 🌱 **Modernité** : syntaxe claire, conciseness, null-safety intégrée.
* 🔄 **Asynchrone** : support natif des coroutines pour la gestion des IO non bloquants.

👉 Conclusion rapide : Kotlin est un langage backend moderne, robuste et parfaitement adapté à un projet scalable.

## ⚖️ Benchmark – Langages Backend

| Critère                | Kotlin (Ktor/Spring)              | Python (FastAPI/Django)        | Express (Node.js)               |
| ---------------------- | --------------------------------- | ------------------------------ | ------------------------------- |
| Performance            | ⚡ Excellente (JVM optimisée)      | Moyenne (interprété, GIL)      | Bonne (JS V8, event-loop)       |
| Temps de dev.          | Rapide (POO + DSL clairs)         | Rapide (syntaxe simple)        | Rapide (JS familier)            |
| Courbe d’apprentissage | Moyenne (apprendre Kotlin)        | Faible (Python connu)          | Faible (JS connu)               |
| Scalabilité            | Très bonne (multi-thread natif)   | Limitée (async + workers)      | Bonne (cluster + workers)       |
| Écosystème             | Large (JVM, Gradle, Spring, etc.) | Très large (librairies Python) | Très large (npm)                |
| Stabilité              | Haute (Google/JetBrains)          | Bonne mais dépend des libs     | Bonne mais npm parfois instable |

👉 Conclusion rapide : **Kotlin est le meilleur compromis entre performance et structuration POO**. Python et Express restent valides pour des projets plus légers ou prototypage rapide, mais Kotlin prend l’avantage pour des projets à forte complexité.

## ⚖️ Benchmark – Bases de Données

| Critère                | MongoDB (NoSQL)                  | PostgreSQL (SQL)                | InfluxDB (Time Series)       |
| ---------------------- | -------------------------------- | ------------------------------- | ---------------------------- |
| Modèle                 | Documents (flexible, JSON-like)  | Relationnel (tables, schémas)   | Time-series optimisé         |
| Performance            | ⚡ Excellente en lecture/écriture | Très bonne, mais plus rigide    | Excellente pour métriques    |
| Scalabilité            | Très bonne (sharding natif)      | Bonne mais complexe à gérer     | Bonne (orientée séries)      |
| Utilisation idéale     | Données dynamiques, flexibles    | Données relationnelles strictes | Données temporelles, IoT     |
| Courbe d’apprentissage | Moyenne (NoSQL)                  | Moyenne/élevée (SQL avancé)     | Spécialisée (plus restreint) |

👉 Conclusion rapide : **MongoDB est le choix parfait pour AREA** → flexibilité des schémas, rapidité en écriture/lecture, et idéal pour manipuler des objets complexes via POO.

## 🧩 Stack retenue

Après analyse, la stack choisie pour ce projet est :

* **Langage Backend** : Kotlin (Ktor ou Spring Boot)
* **Base de Données** : MongoDB (NoSQL, flexible et scalable)
* **Queue** : RabbitMQ (gestion asynchrone des actions et events)
* **Cache** : Redis (accélération des requêtes MongoDB, sessions, pub/sub)

👉 Combo final :

* **Kotlin & RabbitMQ** → pour gérer et distribuer les actions.
* **MongoDB & Redis** → pour stocker et mettre en cache efficacement les données.

## 🛠️ Installation & Lancement

1. Installer Kotlin + Gradle : [Documentation officielle](https://kotlinlang.org/docs/command-line.html)
2. Installer MongoDB : [Documentation officielle](https://www.mongodb.com/docs/manual/installation/)
3. Installer Redis : [Documentation officielle](https://redis.io/docs/getting-started/)
4. Installer RabbitMQ : [Documentation officielle](https://www.rabbitmq.com/download.html)
5. Vérifier l’installation :

   ```bash
   java -version
   kotlin -version
   mongod --version
   redis-server --version
   rabbitmqctl status
   ```
6. Lancer le projet :

   ```bash
   ./gradlew run
   ```

## 📂 Structure du projet

Le projet contient :

* `Application.kt` → point d’entrée backend (Ktor/Spring)
* `services/` → gestion des services métier (POO)
* `repository/` → gestion des accès MongoDB
* `queue/` → gestion des consumers/producers RabbitMQ
* `cache/` → intégration Redis

## ✅ Conclusion

Kotlin associé à MongoDB constitue une solution backend :

* **robuste** (POO, JVM, coroutines),
* **performante** (scalabilité et rapidité d’exécution),
* **adaptée** à un projet complexe et orienté objets comme AREA.

En ajoutant **RabbitMQ pour la gestion des actions** et **Redis pour l’optimisation des requêtes**, nous obtenons une architecture moderne, scalable et prête pour la production.

C’est donc la stack que nous utiliserons pour **AREA**.
