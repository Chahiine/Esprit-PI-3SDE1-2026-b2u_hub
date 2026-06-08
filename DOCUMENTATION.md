# Documentation complète — B2U-HUB PI

Plateforme web de mise en relation **entreprises / freelances (étudiants)** avec module **Évaluation & Feedback**, assistant **IA** (Gemini), notifications **e-mail**, et chaîne **DevOps** complète (Jenkins, SonarQube, Prometheus, Grafana, Docker).

---

## Table des matières

1. [Vue d'ensemble du projet](#1-vue-densemble-du-projet)
2. [Architecture technique](#2-architecture-technique)
3. [Structure du dépôt](#3-structure-du-dépôt)
4. [Démarrage en développement](#4-démarrage-en-développement)
5. [Docker Desktop — rôle et utilisation](#5-docker-desktop--rôle-et-utilisation)
6. [Jenkins — CI/CD](#6-jenkins--cicd)
7. [SonarQube — qualité de code](#7-sonarqube--qualité-de-code)
8. [Prometheus — collecte de métriques](#8-prometheus--collecte-de-métriques)
9. [Grafana — visualisation](#9-grafana--visualisation)
10. [Schéma d'intégration DevOps](#10-schéma-dintégration-devops)
11. [Référence des URLs](#11-référence-des-urls)
12. [Tests — guide complet](#12-tests--guide-complet)

---

## 1. Vue d'ensemble du projet

### Objectif

B2U-HUB est un projet académique (PI) qui permet :

- aux **entreprises** d'évaluer des étudiants freelances ;
- aux **étudiants** de consulter leurs feedbacks, noter une entreprise et y répondre ;
- la génération de **feedback assisté par IA** (Google Gemini ou moteur local) ;
- l'envoi de **notifications e-mail** après publication d'une évaluation.

### Fonctionnalités principales

| Module | Description |
|--------|-------------|
| **Vitrine Eduleb** | Pages marketing Angular (accueil, cours, blog, contact…) |
| **Espace entreprise** | Créer une évaluation, historique, suggestion IA |
| **Espace étudiant** | Voir les feedbacks, noter une entreprise, répondre |
| **API REST** | Spring Boot + PostgreSQL |
| **IA** | `POST /api/ai/suggest-feedback` |
| **E-mail** | Console ou Gmail SMTP |
| **Monitoring** | Métriques JVM/HTTP via Prometheus + Grafana |

### Pages frontend (module B2U-HUB)

| URL | Rôle |
|-----|------|
| `/evaluation/entreprise` | Dashboard entreprise |
| `/evaluation/entreprise/nouvelle` | Nouvelle évaluation + IA |
| `/evaluation/etudiant` | Espace étudiant |
| `/evaluation/etudiant/noter-entreprise` | Noter une entreprise |

---

## 2. Architecture technique

| Couche | Technologie | Port (dev) | Port (Docker CI) |
|--------|-------------|------------|------------------|
| Frontend | Angular 18, TypeScript, nginx | 4200 | 4201 |
| Backend | Spring Boot 4, Java 17 | 8081 | 8082 |
| Base de données | PostgreSQL (`b2u_hub`) | 5432 | — |
| IA | Google Gemini (optionnel) | — | — |
| CI/CD | Jenkins | 8080 | — |
| Qualité | SonarQube | 9000 | — |
| Métriques | Prometheus | 9090 | — |
| Dashboards | Grafana | 3000 | — |

### API REST principale

**Base URL (dev) :** `http://localhost:8081`

| Endpoint | Méthode | Rôle |
|----------|---------|------|
| `/api/ai/suggest-feedback` | POST | Générer un feedback IA |
| `/api/evaluations` | GET / POST | Lister / créer une évaluation |
| `/api/users/register` | POST | Inscrire un utilisateur |
| `/api/mail/status` | GET | État de la configuration e-mail |
| `/api/mail/test` | POST | Tester l'envoi d'e-mail |
| `/actuator/health` | GET | Santé de l'application |
| `/actuator/prometheus` | GET | Métriques pour Prometheus |

Collection Postman : `postman/evaluations-crud.postman_collection.json`

---

## 3. Structure du dépôt

```
PI/
├── README.md                          # Guide rapide
├── DOCUMENTATION.md                   # Ce document
├── pom.xml                            # Backend Maven (JaCoCo, Sonar, Actuator)
├── sonar-project.properties           # Configuration SonarQube
├── Dockerfile.backend                 # Image Docker backend
├── Jenkinsfile                        # Déprécié (pointe vers devops/)
├── Jenkinsfile.windows                # Pipeline Jenkins natif Windows
│
├── src/main/java/com/example/pi/      # Code source backend
│   ├── PiApplication.java             # Point d'entrée Spring Boot
│   ├── config/                        # Security, Mail, Gemini, Web
│   ├── controller/                    # Contrôleurs REST
│   ├── service/                       # Logique métier
│   ├── entity/                        # Entités JPA
│   ├── repository/                    # Repositories Spring Data
│   └── dto/                           # Objets de transfert
├── src/main/resources/
│   ├── application.properties         # Config principale + monitoring
│   └── application-local.properties.example
├── src/test/java/                     # Tests JUnit (8 classes)
│
├── frontend/                          # Application Angular 18
│   ├── Dockerfile                     # Image nginx
│   ├── nginx.conf
│   ├── karma.conf.js                  # Tests headless pour CI
│   └── src/app/
│       ├── eduleb/                    # Vitrine marketing
│       └── b2u-hub/evaluation/        # Module évaluation
│
├── devops/                            # Stack DevOps
│   ├── docker-compose.devops.yml      # Jenkins, SonarQube, Prometheus, Grafana
│   ├── start-devops.ps1               # Script de démarrage
│   ├── rebuild-jenkins.ps1            # Reconstruire Jenkins si Docker manque
│   ├── DEVOPS-PIPELINE.md             # Guide pipelines Jenkins
│   ├── jenkins/
│   │   ├── Dockerfile                 # Image Jenkins personnalisée
│   │   ├── Jenkinsfile.pipeline-backend
│   │   ├── Jenkinsfile.pipeline-frontend
│   │   ├── Jenkinsfile.ci-backend     # Ancien mode 4 jobs
│   │   ├── Jenkinsfile.ci-frontend
│   │   ├── Jenkinsfile.cd-backend
│   │   └── Jenkinsfile.cd-frontend
│   ├── prometheus/prometheus.yml      # Cibles de scraping
│   └── grafana/provisioning/          # Datasource + dashboard auto
│
├── scripts/                           # Scripts SQL, build
└── postman/                           # Collection API
```

### Fichiers de configuration importants

| Fichier | Rôle |
|---------|------|
| `src/main/resources/application.properties` | PostgreSQL, port 8081, actuator, e-mail |
| `src/main/resources/application-local.properties` | Clé Gemini + Gmail (non versionné) |
| `frontend/src/environments/environment.ts` | URL API Angular (`http://localhost:8081`) |
| `src/test/resources/application-test.properties` | Base H2 en mémoire pour les tests |

---

## 4. Démarrage en développement

### Prérequis

- **JDK 17**
- **Node.js 18+** et npm
- **PostgreSQL** (port 5432)
- **Docker Desktop** (pour la stack DevOps et les déploiements CI)

### 1. Base de données

```sql
CREATE DATABASE b2u_hub;
```

Script fourni : `scripts/create-database-b2u_hub.sql`

### 2. Backend

```powershell
cd PI
.\mvnw.cmd spring-boot:run
```

→ http://localhost:8081

### 3. Frontend

```powershell
cd frontend
npm install
npm start
```

→ http://localhost:4200

### 4. Configuration optionnelle (IA + e-mail)

Copier `application-local.properties.example` vers `application-local.properties` :

```properties
app.ai.gemini.enabled=true
app.ai.gemini.api-key=VOTRE_CLE
app.ai.gemini.model=gemini-2.5-flash
```

---

## 5. Docker Desktop — rôle et utilisation

### Pourquoi Docker Desktop ?

**Docker Desktop** est le moteur d'exécution des conteneurs sur Windows. Dans ce projet, il sert à **trois choses** :

1. **Héberger la stack DevOps** — Jenkins, SonarQube, Prometheus et Grafana tournent dans des conteneurs définis par `devops/docker-compose.devops.yml`.
2. **Construire et déployer l'application** — Jenkins exécute `docker build` et `docker run` pour produire les images backend et frontend.
3. **Permettre le pattern Docker-in-Docker** — le conteneur Jenkins monte le socket Docker de l'hôte (`/var/run/docker.sock`) pour piloter Docker Desktop depuis l'intérieur de Jenkins.

> Sans Docker Desktop démarré, ni la stack DevOps ni les pipelines de déploiement ne fonctionneront.

### Où trouver les implémentations Docker ?

| Fichier | Rôle |
|---------|------|
| `devops/docker-compose.devops.yml` | Stack DevOps (5 services) |
| `Dockerfile.backend` | Image Spring Boot (multi-stage JDK → JRE) |
| `frontend/Dockerfile` | Image Angular (build Node → nginx) |
| `devops/jenkins/Dockerfile` | Image Jenkins avec Maven, Node, Chromium, Docker CLI |
| `.dockerignore` | Fichiers exclus du build |

### Services dans docker-compose.devops.yml

| Service | Image | Port hôte | Conteneur |
|---------|-------|-----------|-----------|
| `sonar-db` | `postgres:15-alpine` | interne | `b2u-sonar-db` |
| `sonarqube` | `sonarqube:10.6-community` | 9000 | `b2u-sonarqube` |
| `jenkins` | `b2u-jenkins:latest` (build local) | 8080, 50000 | `b2u-jenkins` |
| `prometheus` | `prom/prometheus:v2.55.1` | 9090 | `b2u-prometheus` |
| `grafana` | `grafana/grafana:11.4.0` | 3000 | `b2u-grafana` |

### Images produites par la CI

| Image | Conteneur déployé | Port hôte |
|-------|-------------------|-----------|
| `b2u-hub-backend:latest` | `b2u-backend` | 8082 |
| `b2u-hub-frontend:latest` | `b2u-frontend` | 4201 |

### Démarrer la stack DevOps

```powershell
cd devops
.\start-devops.ps1
```

Ou manuellement :

```powershell
cd devops
docker compose -f docker-compose.devops.yml up -d --build
docker compose -f docker-compose.devops.yml ps
```

### Tester Docker manuellement (sans Jenkins)

```powershell
# Backend
cd PI
docker build -f Dockerfile.backend -t b2u-hub-backend:latest .
docker run -d --name b2u-backend -p 8082:8081 b2u-hub-backend:latest

# Frontend
cd frontend
docker build -f Dockerfile -t b2u-hub-frontend:latest .
docker run -d --name b2u-frontend -p 4201:80 b2u-hub-frontend:latest
```

Vérification :

- Backend : http://localhost:8082/actuator/health
- Frontend : http://localhost:4201

---

## 6. Jenkins — CI/CD

### Pourquoi Jenkins ?

**Jenkins** automatise l'intégration et le déploiement continus (CI/CD). Dans ce projet, il :

- clone le code depuis GitHub ;
- compile et teste le backend (Maven) et le frontend (npm + Karma) ;
- envoie l'analyse de qualité à SonarQube ;
- construit les images Docker ;
- déploie les conteneurs backend et frontend ;
- enchaîne les pipelines : le succès du backend déclenche automatiquement le frontend.

### Où trouver les implémentations ?

| Fichier | Rôle |
|---------|------|
| `devops/docker-compose.devops.yml` | Service Jenkins (port 8080) |
| `devops/jenkins/Dockerfile` | Image Jenkins personnalisée |
| `devops/jenkins/Jenkinsfile.pipeline-backend` | Pipeline backend (recommandé) |
| `devops/jenkins/Jenkinsfile.pipeline-frontend` | Pipeline frontend (recommandé) |
| `devops/DEVOPS-PIPELINE.md` | Guide de configuration des jobs |
| `devops/start-devops.ps1` | Démarrage de la stack |
| `devops/rebuild-jenkins.ps1` | Rebuild si Docker CLI manque dans Jenkins |
| `Jenkinsfile.windows` | Alternative : Jenkins installé nativement sur Windows |

### Architecture des pipelines (recommandée)

```
b2u-pipeline-backend  ──succès──>  b2u-pipeline-frontend
```

#### Pipeline Backend (`b2u-pipeline-backend`)

| Étape | Action |
|-------|--------|
| Checkout | Clone `main` depuis GitHub |
| Build & Test | `./mvnw clean verify` |
| SonarQube | `./mvnw sonar:sonar` |
| Docker Build | Image `b2u-hub-backend` |
| Deploy | `docker run` sur le port **8082** |
| Trigger | Lance `b2u-pipeline-frontend` |

#### Pipeline Frontend (`b2u-pipeline-frontend`)

| Étape | Action |
|-------|--------|
| Trigger | Attend le succès de `b2u-pipeline-backend` |
| Checkout | Clone GitHub |
| Install | `npm ci` |
| Tests | `npm run test:ci` (Karma headless) |
| Build | `npm run build --configuration=production` |
| Docker Build | Image `b2u-hub-frontend` |
| Deploy | `docker run` sur le port **4201** |

### Configuration initiale Jenkins

1. Démarrer la stack : `.\devops\start-devops.ps1`
2. Ouvrir http://localhost:8080
3. Récupérer le mot de passe initial :
   ```powershell
   docker exec b2u-jenkins cat /var/jenkins_home/secrets/initialAdminPassword
   ```
4. Installer les plugins suggérés + **Pipeline** + **SonarQube Scanner**
5. Créer le credential `sonar-token` (Secret text)
6. Configurer le serveur SonarQube : nom `SonarQube`, URL `http://sonarqube:9000`
7. Créer 2 jobs Pipeline en collant le contenu des Jenkinsfiles (voir `DEVOPS-PIPELINE.md`)

### Comment tester Jenkins

```powershell
# 1. Vérifier que Docker Desktop tourne
docker ps

# 2. Démarrer la stack DevOps
cd devops
.\start-devops.ps1

# 3. Vérifier Docker dans Jenkins
docker exec b2u-jenkins docker --version
# Si "not found" → .\rebuild-jenkins.ps1

# 4. Dans l'interface Jenkins : Build Now sur b2u-pipeline-backend

# 5. Vérifier le déploiement
curl http://localhost:8082/actuator/health
# Frontend : http://localhost:4201
```

---

## 7. SonarQube — qualité de code

### Pourquoi SonarQube ?

**SonarQube** analyse statiquement le code source pour détecter :

- **bugs** potentiels ;
- **code smells** (mauvaises pratiques) ;
- **vulnérabilités** et points de sécurité ;
- la **couverture de tests** (via les rapports JaCoCo générés par Maven).

Cela garantit un code backend maintenable et conforme aux standards avant chaque déploiement.

### Où trouver les implémentations ?

| Fichier | Rôle |
|---------|------|
| `sonar-project.properties` | Clé projet, sources, tests, exclusions, chemin JaCoCo |
| `pom.xml` | Plugins `sonar-maven-plugin` et `jacoco-maven-plugin` |
| `devops/docker-compose.devops.yml` | Services `sonarqube` (port 9000) + `sonar-db` (PostgreSQL) |
| `devops/jenkins/Jenkinsfile.pipeline-backend` | Étape SonarQube dans la CI |

### Configuration clé

**`sonar-project.properties` :**

```properties
sonar.projectKey=b2u-hub
sonar.sources=src/main/java
sonar.tests=src/test/java
sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
sonar.exclusions=**/PiApplication.java
sonar.coverage.exclusions=**/dto/**,**/PiApplication.java
```

**Intégration Jenkins :**

- Credential ID : `sonar-token`
- Serveur SonarQube : `SonarQube` → `http://sonarqube:9000`
- Commande : `./mvnw sonar:sonar -Dsonar.projectKey=b2u-hub -Dsonar.token=$SONAR_TOKEN`

### Comment tester SonarQube

#### Méthode 1 — Interface web

```powershell
cd devops
.\start-devops.ps1
```

Ouvrir http://localhost:9000 → login `admin` (définir le mot de passe au premier accès).

#### Méthode 2 — Analyse manuelle (sans Jenkins)

```powershell
cd PI

# 1. Générer les tests + rapport JaCoCo
.\mvnw.cmd clean verify

# 2. Créer un token dans SonarQube : My Account → Security → Generate Token

# 3. Lancer l'analyse
.\mvnw.cmd sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.token=VOTRE_TOKEN
```

#### Méthode 3 — Via Jenkins

Lancer `b2u-pipeline-backend` → consulter le projet `b2u-hub` dans SonarQube.

#### Résultat attendu

Dans SonarQube, le projet **B2U-HUB PI** (`b2u-hub`) affiche :

- note de fiabilité, sécurité, maintenabilité ;
- couverture de code (JaCoCo) ;
- liste des issues détectées.

---

## 8. Prometheus — collecte de métriques

### Pourquoi Prometheus ?

**Prometheus** collecte et stocke les **métriques de runtime** du backend Spring Boot :

- utilisation CPU et mémoire JVM ;
- nombre et latence des requêtes HTTP ;
- état du pool de connexions PostgreSQL (HikariCP) ;
- disponibilité du service (`up`).

Ces données alimentent Grafana pour le monitoring en temps réel.

### Où trouver les implémentations ?

| Fichier | Rôle |
|---------|------|
| `devops/prometheus/prometheus.yml` | Configuration des cibles de scraping |
| `devops/docker-compose.devops.yml` | Service Prometheus (port 9090) |
| `pom.xml` | Dépendances `spring-boot-starter-actuator` + `micrometer-registry-prometheus` |
| `src/main/resources/application.properties` | Exposition des endpoints actuator |

### Configuration clé

**`application.properties` :**

```properties
management.endpoints.web.exposure.include=health,info,prometheus,metrics
management.endpoint.health.show-details=always
management.metrics.tags.application=${spring.application.name}
```

**`prometheus.yml` — deux jobs de scraping :**

| Job | Cible | Contexte |
|-----|-------|----------|
| `b2u-backend-local` | `host.docker.internal:8081` | Backend lancé en dev (IntelliJ / `mvnw`) |
| `b2u-backend-docker` | `host.docker.internal:8082` | Backend déployé par Jenkins |

Scraping toutes les **15 secondes** sur `/actuator/prometheus`.

> `host.docker.internal` permet au conteneur Prometheus d'atteindre les ports de la machine hôte Windows via Docker Desktop.

### Comment tester Prometheus

```powershell
# 1. Démarrer la stack DevOps
cd devops
.\start-devops.ps1

# 2. Démarrer le backend (au moins un des deux)
.\mvnw.cmd spring-boot:run          # port 8081
# OU après déploiement Jenkins     # port 8082

# 3. Vérifier les métriques brutes
# Ouvrir : http://localhost:8081/actuator/prometheus

# 4. Interface Prometheus
# Ouvrir : http://localhost:9090

# 5. Vérifier les cibles : Status → Targets
# Les jobs b2u-backend-local et/ou b2u-backend-docker doivent être UP

# 6. Tester une requête : onglet Graph
# Requête : up{job=~"b2u-backend.*"}
```

#### Résultat attendu

- `/actuator/prometheus` retourne du texte au format Prometheus (métriques `jvm_*`, `http_server_*`, `hikaricp_*`).
- Dans Prometheus UI, les targets sont **UP** et les requêtes retournent des valeurs.

---

## 9. Grafana — visualisation

### Pourquoi Grafana ?

**Grafana** transforme les métriques Prometheus en **tableaux de bord visuels** pour surveiller la santé du backend en temps réel, sans lire des fichiers de métriques bruts.

### Où trouver les implémentations ?

| Fichier | Rôle |
|---------|------|
| `devops/docker-compose.devops.yml` | Service Grafana (port 3000, admin/admin) |
| `devops/grafana/provisioning/datasources/prometheus.yml` | Datasource Prometheus auto-configurée |
| `devops/grafana/provisioning/dashboards/dashboard.yml` | Chargement automatique des dashboards |
| `devops/grafana/provisioning/dashboards/json/b2u-hub-backend.json` | Dashboard pré-construit |

### Dashboard : « B2U-HUB Backend Monitoring »

| Panneau | Métrique Prometheus |
|---------|----------------------|
| Statut backend | `up{job=~"b2u-backend.*"}` |
| CPU | `process_cpu_usage{application="PI"}` |
| Mémoire JVM (heap) | `jvm_memory_used_bytes{application="PI", area="heap"}` |
| Débit HTTP | `rate(http_server_requests_seconds_count{application="PI"}[1m])` |
| Latence HTTP p95 | `histogram_quantile(0.95, ...)` |
| Pool PostgreSQL | `hikaricp_connections_active/idle{application="PI"}` |

### Comment tester Grafana

```powershell
# 1. Démarrer la stack DevOps
cd devops
.\start-devops.ps1

# 2. Démarrer le backend (8081 ou 8082)
.\mvnw.cmd spring-boot:run

# 3. Ouvrir Grafana
# URL : http://localhost:3000
# Login : admin / admin

# 4. Naviguer vers le dashboard
# Dashboards → B2U-HUB → B2U-HUB Backend Monitoring

# 5. Générer du trafic pour voir les graphiques bouger
# Appeler quelques endpoints API ou recharger la page frontend
```

#### Résultat attendu

Les panneaux affichent des courbes actives (CPU, mémoire, requêtes HTTP). Si le backend n'est pas démarré, le panneau « Statut » affiche **DOWN**.

---

## 10. Schéma d'intégration DevOps

```
┌─────────────────────────────────────────────────────────────────┐
│                    Machine Windows (Docker Desktop)            │
│                                                                  │
│  ┌──────────────── docker-compose.devops.yml ────────────────┐  │
│  │  Jenkins :8080    SonarQube :9000    Prometheus :9090      │  │
│  │                   (+ sonar-db PostgreSQL)  Grafana :3000   │  │
│  └────────────────────────────────────────────────────────────┘  │
│         │                    │                    │                │
│         │ mvn verify         │ scrape             │ datasource    │
│         │ sonar:sonar        │ /actuator/         │               │
│         ▼                    │ prometheus         ▼                │
│  ┌─────────────┐      ┌──────────────┐      ┌──────────┐        │
│  │  SonarQube  │      │  Backend     │      │ Grafana  │        │
│  │  (qualité)  │      │  :8081 dev   │      │(dashboard)│       │
│  └─────────────┘      │  :8082 docker│      └──────────┘        │
│         ▲              └──────────────┘                           │
│         │                    ▲                                    │
│  ┌──────┴──────┐    docker build/run                             │
│  │   Jenkins   │─────────────────────────> b2u-backend :8082      │
│  │  (CI/CD)    │─────────────────────────> b2u-frontend :4201    │
│  └──────┬──────┘                                                  │
│         │ clone                                                   │
│         ▼                                                         │
│  ┌─────────────┐                                                  │
│  │   GitHub    │                                                  │
│  └─────────────┘                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Flux complet d'un déploiement

1. Développeur pousse du code sur GitHub (`main`).
2. Jenkins clone le dépôt.
3. Maven compile, teste et génère le rapport JaCoCo.
4. SonarQube analyse le code et affiche la qualité + couverture.
5. Jenkins construit l'image Docker backend et la déploie sur le port 8082.
6. Jenkins déclenche automatiquement le pipeline frontend.
7. npm teste, build et déploie le frontend sur le port 4201.
8. Prometheus scrape les métriques du backend toutes les 15 s.
9. Grafana affiche les tableaux de bord en temps réel.

---

## 11. Référence des URLs

| Service | URL | Identifiants |
|---------|-----|--------------|
| Jenkins | http://localhost:8080 | Mot de passe initial (voir `docker exec`) |
| SonarQube | http://localhost:9000 | admin / (défini au 1er login) |
| Prometheus | http://localhost:9090 | — |
| Grafana | http://localhost:3000 | admin / admin |
| Backend (dev) | http://localhost:8081 | — |
| Backend (Docker CI) | http://localhost:8082 | — |
| Frontend (dev) | http://localhost:4200 | — |
| Frontend (Docker CI) | http://localhost:4201 | — |
| Métriques | http://localhost:8081/actuator/prometheus | — |
| Santé backend | http://localhost:8081/actuator/health | — |

---

## 12. Tests — guide complet

### Tests unitaires backend

```powershell
cd PI
.\mvnw.cmd test
# ou avec couverture JaCoCo :
.\mvnw.cmd clean verify
# Rapport : target/site/jacoco/index.html
```

Classes de test : `src/test/java/com/example/pi/` (8 classes JUnit).

### Tests unitaires frontend

```powershell
cd frontend
npm run test:ci    # Mode headless (utilisé par Jenkins)
npm test           # Mode interactif
```

Config Karma : `frontend/karma.conf.js`

### Tests API (Postman)

Importer `postman/evaluations-crud.postman_collection.json` dans Postman.
Base URL : `http://localhost:8081`

### Tests e-mail

```powershell
# Vérifier la config
curl http://localhost:8081/api/mail/status

# Envoyer un e-mail de test
curl -X POST http://localhost:8081/api/mail/test
```

### Tests DevOps — checklist

| Outil | Commande / Action | Résultat attendu |
|-------|-------------------|------------------|
| **Docker Desktop** | `docker ps` | Liste les conteneurs actifs |
| **Stack DevOps** | `.\devops\start-devops.ps1` | 5 conteneurs UP |
| **Jenkins** | Build Now sur `b2u-pipeline-backend` | Pipeline vert, backend sur :8082 |
| **SonarQube** | Ouvrir http://localhost:9000, projet `b2u-hub` | Rapport qualité + couverture |
| **Prometheus** | http://localhost:9090 → Status → Targets | Jobs `b2u-backend-*` = UP |
| **Grafana** | http://localhost:3000 → dashboard B2U-HUB | Graphiques avec données |
| **Backend Docker** | http://localhost:8082/actuator/health | `{"status":"UP"}` |
| **Frontend Docker** | http://localhost:4201 | Page Angular chargée |

### Ordre recommandé pour une démo complète

```powershell
# 1. Démarrer Docker Desktop

# 2. Lancer la stack DevOps
cd devops
.\start-devops.ps1

# 3. Configurer Jenkins (première fois uniquement)
#    → créer jobs, credential sonar-token, serveur SonarQube

# 4. Lancer le pipeline
#    → Jenkins : Build Now sur b2u-pipeline-backend

# 5. Vérifier le déploiement
#    → http://localhost:8082/actuator/health
#    → http://localhost:4201

# 6. Vérifier le monitoring
#    → http://localhost:9090 (Prometheus targets UP)
#    → http://localhost:3000 (dashboard Grafana)

# 7. Vérifier la qualité
#    → http://localhost:9000 (projet b2u-hub)
```

---

## Documents complémentaires

| Fichier | Contenu |
|---------|---------|
| `README.md` | Guide de démarrage rapide |
| `devops/DEVOPS-PIPELINE.md` | Configuration détaillée des jobs Jenkins |
| `application-local.properties.example` | Template Gemini + Gmail |
| `postman/evaluations-crud.postman_collection.json` | Tests API |

---

*Projet académique PI — module B2U-HUB. Auteurs : Chahine Sassi, Samira Benali, Lucas Martin, Youssef Trabelsi, Ines Gharbi.*
