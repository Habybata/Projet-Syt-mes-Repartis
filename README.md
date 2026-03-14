# Système Distribué de Supervision Réseau - M1 SRIV (UN-CHK)

Ce projet implémente un système de supervision réseau basé sur une architecture client-serveur, réalisé dans le cadre du cours de Systèmes Répartis (P2025).

## 📋 Livrables du Projet
Conformément aux exigences du sujet, ce dépôt contient :
*   **Modules Java** : `agent-client`, `supervision-server`, `shared-model`.
*   **Script BD** : `schema.sql` (création de la structure SQLite).
*   **Scripts d'exécution** : `run_server.bat` (Serveur) et `run_load_test.bat` (Simulation de charge).
*   **Configuration Logs** : `logback.xml` dans les ressources du serveur.

---

## 🏗️ Architecture du Système

Le système repose sur une communication Socket TCP bidirectionnelle (JSON).

### 1. Agent de Supervision (Client)
*   **Collecte périodique (30s)** : ID, OS, CPU (Type/Charge), Mémoire, Disque, Uptime.
*   **Services & Ports** : Surveille 6 services (3 réseau, 3 applicatifs) et 4 ports réseau.
*   **Alertes Locales** : Log `WARN` si une charge (CPU, RAM, Disque) dépasse **90%**.
*   **Réception d'ordres** : Capable d'exécuter des commandes `UP` envoyées par le serveur.

### 2. Serveur de Supervision
*   **Multi-threading** : Utilise un **`FixedThreadPool`** pour gérer les connexions simultanées (choisi pour sa stabilité sous forte charge).
*   **Persistance** : Stockage des métriques dans une base **SQLite** via un pool de connexions **HikariCP**.
*   **Monitoring de Statut** : Détecte les pannes si un agent ne répond pas pendant **90 secondes**.
*   **Journalisation Centralisée** :
    *   Logs des connexions/déconnexions.
    *   **Alertes de seuil (>90%)** reçues des agents.
    *   Détection des nœuds hors ligne.
*   **Console CLI** : Interface d'administration pour lister les nœuds, voir les métriques et envoyer des commandes.

---

## 🚀 Installation et Lancement

### Prérequis
*   **Java 11+** et **Maven** installés.

### 1. Compilation
```bash
mvn clean package
```

### 2. Lancer le Serveur
Utilisez le script batch pour Windows (active l'UTF-8) :
```bat
.\run_server.bat
```
Ou manuellement :
```bash
java -jar supervision-server/target/supervision-server-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### 3. Lancer un Agent
```bash
java -jar agent-client/target/agent-client-1.0-SNAPSHOT-jar-with-dependencies.jar agent-01
```

---

## 📊 Tests de Charge
Le sujet demande des tests avec 10, 50 et 100 clients. Utilisez le script fourni :
```bat
.\run_load_test.bat
```
*(Le script lance par défaut 50 instances. Modifiez la boucle dans le .bat pour tester 10 ou 100).*

---

## 🛠️ Choix Techniques & Concurrence
*   **Threads** : Le serveur utilise `Executors.newFixedThreadPool(20)`. Contrairement au `CachedThreadPool`, il limite le nombre de threads actifs, évitant l'épuisement des ressources système lors de pics de connexion (essentiel pour la stabilité d'un serveur de supervision).
*   **Base de données** : HikariCP assure une gestion performante des accès concurrents à SQLite, évitant les verrous (`database is locked`) fréquents en accès direct.
*   **Format** : JSON (Gson) a été choisi pour sa flexibilité et sa facilité de lecture humaine par rapport à un format texte brut.

---
**Auteur** : [Votre Nom / Groupe]  
**Date** : Mars 2026  
**Lien Git** : [Insérez votre lien ici]
