# 📝 POC Mobile – Kotlin (Login / Register)

## 🎯 Objectif  
Ce projet est un **POC (Proof of Concept)** visant à tester le développement mobile avec **Kotlin (Android)**.  
L’objectif est de valider :  
- la **faisabilité** d’un système simple de **connexion / inscription**,  
- l’**ergonomie** des écrans avec Kotlin,  
- la **gestion des entrées utilisateur** et la navigation entre plusieurs écrans.  

👉 Ce prototype se concentre sur la validation technique et ergonomique.  
Il n’inclut pas :  
- de sécurité avancée (ex. chiffrement des mots de passe),  
- de persistance via une base de données,  
- ni de design graphique abouti (interfaces volontairement simples).  

---

## 🚀 Pourquoi Kotlin ?  
- **Langage officiel Android** : maintenu et promu par Google.  
- **Interopérable avec Java** : compatible avec l’écosystème existant.  
- **Modernité** : syntaxe concise, null-safety, lambdas.  
- **Productivité** : très bien intégré à Android Studio, génération rapide d’apps.  
- **Pérennité** : adopté par la majorité des projets Android récents.  

---

## ⚖️ Benchmark – Comparaison avec d’autres solutions  

| Critère                    | **Kotlin (Natif Android)** | **Flutter**                     | **React Native**                |
|----------------------------|----------------------------|---------------------------------|---------------------------------|
| **Performance**            | Optimale (100% natif)      | Quasi native (60fps)            | Correcte, parfois bridée        |
| **Temps de dev.**          | Plus long (Android only)   | Rapide (Hot Reload)             | Rapide (Fast Refresh)           |
| **Courbe d’apprentissage** | Moyenne (Android SDK)      | Modérée (apprendre Dart)        | Faible (JS connu)               |
| **Taille APK/IPA**         | Plus léger                 | 8–15 Mo                         | 6–12 Mo                         |
| **Consommation mémoire**   | Optimale                   | Optimisée mais plus lourde      | Moyenne                         |
| **Communauté**             | Stable et mature           | Large, très active              | Très large                      |
| **Plugins & intégration**  | 100% supporté par l’OS     | Riche (pub.dev)                 | Nombreux mais parfois instables |

👉 **Conclusion rapide** : Kotlin reste le choix le plus performant et le plus fiable pour Android, mais au prix d’un temps de développement plus élevé.  

---

## ✅ Points positifs
- **Code concis et moderne** (comparé à Java).  
- **Intégration parfaite avec Android Studio**.  
- **Performances optimales** (aucune couche intermédiaire).  
- **Navigation fluide** entre les écrans.  

---

## 🚫 Points négatifs
- **Temps de développement plus long** que Flutter ou React Native.  
- **Uniquement Android** : pas de compatibilité iOS.  
- **Courbe d’apprentissage** : nécessite de maîtriser Android SDK et ses nombreux concepts.  

---

## Test 

 - kotlinc test.kt -include-runtime -d test.jar
 - java -jar test.jar

---
## 🚀 Demo

### Login page:
<img src="readme/Log.png" alt="Connexion" width="300">

### Login page connexion réussie:
<img src="readme/Co.png" alt="Connexion" width="300">

### Register page:

<img src="readme/register.png" alt="Connexion" width="300">

### Register:

<img src="readme/regSuccess.png" alt="Connexion" width="300">

---

## 🛠️ Installation & Lancement 

1. Installer [Android Studio](https://developer.android.com/studio).  
2. Cloner le projet.  
3. Ouvrir le projet dans Android Studio.  
4. Lancer un émulateur Android ou connecter un smartphone.  
5. Compiler et exécuter :  
   ```bash
   ./gradlew installDebug

---

Alexandre De-Angelis
Benjamin Buisson
Enzo Petit
Hugo Dufour
Suleman Maqsood
