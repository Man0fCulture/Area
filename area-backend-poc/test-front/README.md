# AREA Test Frontend

Frontend de test pour les 3 backends AREA (Express, Python, Kotlin).

## Installation

```bash
npm install
```

## Lancement

```bash
npm run dev
```

Le frontend sera accessible sur http://localhost:3000

## Fonctionnalités

- **Login/Register** : Test d'authentification sur les 3 backends
- **Dashboard** : Affichage des informations utilisateur
- **Test API** : Console de test pour tous les endpoints

## Backends

Assurez-vous que les backends sont lancés :

- **Express** (port 8080): `cd express-backend && npm start`
- **Python** (port 8081): `cd python-backend && python main.py`
- **Kotlin** (port 8082): `cd kotlin-backend && ./gradlew bootRun`

## Routes disponibles

- `/about.json` : Informations sur les services
- `/api/auth/register` : Inscription
- `/api/auth/login` : Connexion