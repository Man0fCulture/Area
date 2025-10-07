# AREA - Application Mobile Flutter

Application mobile Flutter pour le projet AREA (Action-REAction).

## Description

Application mobile permettant de créer et gérer des automatisations (AREA) connectant différents services via des actions et réactions.

## Technologies

- **Flutter** 3.9.2+
- **Dart**
- **Packages principaux:**
  - `http` - Requêtes HTTP
  - `shared_preferences` - Stockage local
  - `google_sign_in` - Authentification Google OAuth

## Architecture

```
lib/
├── main.dart                  # Point d'entrée de l'application
├── constants/
│   └── api_config.dart       # Configuration API et endpoints
├── models/
│   ├── area.dart             # Modèle AREA
│   ├── user.dart             # Modèle utilisateur
│   ├── service.dart          # Modèle service
│   ├── token_response.dart   # Modèle réponse token
│   └── api_error.dart        # Modèle erreur API
├── screens/
│   ├── login_page.dart       # Page connexion/inscription
│   ├── register_page.dart    # Page inscription
│   ├── home_page.dart        # Page d'accueil avec dashboard
│   └── create_area_page.dart # Page création AREA
└── services/
    ├── api_service.dart      # Service HTTP
    ├── auth_service.dart     # Service authentification
    └── area_service.dart     # Service gestion AREA
```

## Fonctionnalités implémentées

### Authentification
- **Connexion classique** (email/mot de passe) - `lib/services/auth_service.dart:45`
- **Inscription** avec email, username et mot de passe - `lib/services/auth_service.dart:68`
- **OAuth Google** via Google Sign-In - `lib/services/auth_service.dart:127`
- **Gestion tokens** (access token + refresh token) stockés localement
- **Refresh automatique** des tokens expirés
- **Déconnexion** avec nettoyage tokens

### Dashboard
- **Liste des AREA** créées par l'utilisateur
- **Compteur d'exécutions** pour chaque AREA
- **Activation/désactivation** des AREA via switch
- **Suppression d'AREA** avec confirmation
- **Pull-to-refresh** pour actualiser la liste
- **État vide** avec message si aucune AREA

### Navigation
- **Bottom navigation** avec 2 onglets:
  - Tableau de bord (liste AREA)
  - Profil utilisateur
- **Bouton déconnexion** dans l'AppBar

### Interface
- **Thème Material Design** avec couleurs bleues
- **Cartes d'AREA** affichant:
  - Nom et description
  - Action configurée
  - Nombre de réactions
  - Compteur d'exécutions
  - Dernière exécution formatée
- **Animations** et indicateurs de chargement

## Configuration

### API Backend

Modifier l'adresse du serveur dans `lib/constants/api_config.dart`:

```dart
static const String _serverHost = '192.168.1.28';
static const String _serverPort = '8080';
```

### Endpoints disponibles

- `POST /api/auth/login` - Connexion
- `POST /api/auth/register` - Inscription
- `POST /api/auth/refresh` - Refresh token
- `POST /api/auth/login/google` - OAuth Google
- `GET /api/areas` - Liste des AREA
- `POST /api/areas` - Créer AREA
- `PATCH /api/areas/:id` - Modifier AREA
- `DELETE /api/areas/:id` - Supprimer AREA
- `GET /api/services` - Liste des services

## Installation

```bash
# Installer les dépendances
flutter pub get

# Lancer l'application
flutter run
```

## État actuel

### Implémenté ✅
- Authentification complète (classique + OAuth Google)
- Gestion des tokens avec refresh automatique
- Dashboard avec liste AREA
- Toggle activation/désactivation AREA
- Suppression AREA
- Navigation bottom bar
- Page profil

### En cours / À implémenter ⏳
- Création d'AREA fonctionnelle
- Configuration actions/réactions
- Gestion profil utilisateur dynamique
- Connexion autres services OAuth
- Notifications push
- Mode sombre
