# Projet de Système de Supervision Distribué - M1 SRIV

Ce projet est une implémentation d'un système de supervision réseau basé sur une architecture client-serveur, réalisé dans le cadre du cours de Systèmes Répartis.

## Table des Matières

1. [Architecture](#architecture)
2. [Définition du Protocole](#définition-du-protocole)
3. [Structure du Projet](#structure-du-projet)
4. [Prérequis](#prérequis)
5. [Outils et Bibliothèques](#outils-et-bibliothèques)
6. [Installation et Lancement](#installation-et-lancement)
7. [Tests de Charge](#tests-de-charge)

---

## Architecture

Le système est conçu autour d'une architecture client-serveur classique, avec un focus sur la concurrence et la robustesse.

* **Agent (Client)** : une application Java légère qui s'exécute sur chaque machine à superviser.
  * Toutes les 30 secondes, il collecte des métriques système (charge CPU, mémoire, etc.).
  * **Nouveauté** : Il surveille désormais **6 services** (3 réseau, 3 applications) et **4 ports** réseau.
  * **Alertes** : Il émet un avertissement local si la charge CPU, mémoire ou disque dépasse **90%**.
  * Il envoie ces métriques au serveur central via une connexion TCP socket, en utilisant un format JSON, et peut recevoir en retour des commandes d'activation de service.

* **Serveur (Central)** : une application Java multi-threadée qui centralise les informations.
  * Il utilise un **pool de threads (`ExecutorService`)** pour gérer efficacement un grand nombre de connexions clientes simultanées.
  * Chaque connexion est validée et gérée par un `Handler` dédié.
  * Les métriques (incluant services et ports) sont stockées dans une base de données **SQLite**.
  * Il expose une **console d'administration (CLI)** pour visualiser l'état des agents et leur envoyer des ordres d'activation (`UP`).
  * Un moniteur de statut vérifie périodiquement si les agents sont toujours actifs (timeout de 90s).

## Définition du Protocole

La communication entre l'agent et le serveur se fait via TCP et utilise le format JSON.

#### Message : Agent -> Serveur

L'agent envoie périodiquement un objet JSON avec la structure suivante :

```json
{
  "nodeId": "agent-01",
  "timestamp": 1678886400000,
  "os": "Windows 10",
  "cpuType": "x86_64",
  "cpuLoad": 35.5,
  "memoryLoad": 62.0,
  "diskUsage": 45.2,
  "uptime": 12453,
  "services": {
    "HTTP": "OK",
    "SSH": "KO",
    "Chrome": "OK",
    ...
  },
  "ports": {
    "80": 1,
    "443": 1,
    "22": 0,
    ...
  }
}
```

#### Message : Serveur -> Agent (Optionnel)

Si une commande est en attente, le serveur répond avec une chaîne au format :
`UP:<serviceName>` (ex: `UP:HTTP`)

---

## Structure du Projet

Le projet est organisé en un projet Maven multi-modules pour une séparation claire des responsabilités.

```
/projet-supervision/
|
|-- agent-client/             # Module Maven pour l'application Agent
|   |-- src/main/java/
|   `-- pom.xml
|
|-- supervision-server/       # Module Maven pour l'application Serveur
|   |-- src/main/java/
|   `-- pom.xml
|
|-- shared-model/             # Module contenant le modèle de données partagé
|   |-- src/main/java/
|   `-- pom.xml
|
|-- run_load_test.bat         # Script de test de charge pour Windows
|-- run_load_test.sh          # Script de test de charge pour Linux/macOS
|-- pom.xml                   # POM parent qui gère les modules
`-- README.md                 # Cette documentation
```

---

## Prérequis

* **Java Development Kit (JDK)** : Version 11 ou supérieure.
* **Apache Maven** : Pour compiler le projet et gérer les dépendances.

---

## Outils et Bibliothèques

* **`Gson`** : Pour la sérialisation/désérialisation des objets Java en JSON.
* **`HikariCP`** : Pour la gestion d'un pool de connexions à la base de données, garantissant performance et fiabilité.
* **`SQLite-JDBC`** : Driver JDBC pour la base de données SQLite.
* **`SLF4J` & `Logback`** : Pour une journalisation (logging) flexible et puissante.

---

## Installation et Lancement

Suivez ces étapes depuis la racine du projet (`/projet-supervision/`).

#### 1. Compiler le Projet

Cette commande compile tous les modules et crée les fichiers `.jar` exécutables dans les dossiers `target` de chaque module.

```bash
mvn clean package
```

#### 2. Lancer le Serveur

Ouvrez un terminal et exécutez la commande suivante :

```bash
java -jar supervision-server/target/supervision-server-1.0-SNAPSHOT-jar-with-dependencies.jar
```

> 💡 **Sous Windows**, la console peut utiliser un encodage qui casse les accents.
> Il est recommandé d'utiliser le script `run_server.bat` qui active UTF-8 et lance le serveur correctement :
>
> ```bat
> .\run_server.bat
> ```
>
> Si vous préférez lancer manuellement, procédez comme suit :
>
> ```powershell
> chcp 65001
> java "-Dfile.encoding=UTF-8" -jar supervision-server/target/supervision-server-1.0-SNAPSHOT-jar-with-dependencies.jar
> ```

Le serveur va démarrer, créer le fichier de base de données `supervision.db` s'il n'existe pas, et se mettre en écoute des connexions.

#### 3. Lancer un Agent

Ouvrez un **nouveau** terminal et exécutez la commande suivante. Vous pouvez répéter cette étape dans plusieurs terminaux pour simuler plusieurs agents.

```bash
java -jar agent-client/target/agent-client-1.0-SNAPSHOT-jar-with-dependencies.jar agent-01
```

Dans un autre terminal :

```bash
java -jar agent-client/target/agent-client-1.0-SNAPSHOT-jar-with-dependencies.jar agent-02
```

#### 4. Utiliser la Console d'Administration du Serveur

Dans le terminal où le serveur s'exécute, vous pouvez taper des commandes :

* `list-nodes` : Affiche les agents actuellement connectés et leur dernière heure de contact.
* `show-metrics <nodeId>` : Montre les 10 dernières métriques enregistrées pour l'agent spécifié.
* `exit` : Arrête proprement le serveur.

---

## Tests de Charge

Pour simuler la connexion de nombreux clients simultanément, utilisez le script fourni correspondant à votre système d'exploitation. Il lancera 50 agents.

#### Sur Windows

```bat
.\run_load_test.bat
```

#### Sur Linux ou macOS

Rendez d'abord le script exécutable :

```bash
chmod +x run_load_test.sh
```

Puis lancez-le :

```bash
./run_load_test.sh
```
