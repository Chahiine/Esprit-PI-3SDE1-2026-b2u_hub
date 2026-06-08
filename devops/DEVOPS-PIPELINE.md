# Pipeline DevOps — B2U-HUB

Jenkins + SonarQube + Docker pour le projet **Esprit-PI-3SDE1-2026-b2u_hub**.

---

## 1. Prérequis

- **Docker Desktop** (Windows) démarré
- **Git** installé
- Ports libres : `8080` (Jenkins), `9000` (SonarQube)

---

## 2. Démarrer l'infrastructure

```powershell
cd devops
.\start-devops.ps1
```

| Service   | URL                      | Identifiants initiaux      |
|-----------|--------------------------|----------------------------|
| Jenkins   | http://localhost:8080    | Mot de passe affiché par le script |
| SonarQube | http://localhost:9000    | `admin` / `admin` (changer au 1er login) |

Vérifier Docker dans Jenkins :

```powershell
docker exec b2u-jenkins docker --version
```

Si erreur de permission sur le socket Docker, reconstruire Jenkins :

```powershell
docker compose -f devops/docker-compose.devops.yml up -d --build jenkins
```

---

## 3. Configuration Jenkins (une fois)

### 3.1 Plugins

**Manage Jenkins → Plugins → Available** — installer :

- Pipeline
- Git
- SonarQube Scanner

Redémarrer Jenkins si demandé.

### 3.2 Credential SonarQube

1. SonarQube → **My Account → Security → Generate Tokens**
2. Type : **Global Analysis Token** → copier le token `squ_...`
3. Jenkins → **Manage Jenkins → Credentials → System → Global**
4. **Add Credentials** :
   - Kind : **Secret text**
   - Secret : coller le token `squ_...`
   - ID : **`sonar-token`** (obligatoire, exactement ce nom)
   - Description : SonarQube analysis token

### 3.3 Serveur SonarQube dans Jenkins

**Manage Jenkins → System → SonarQube servers**

| Champ              | Valeur                    |
|--------------------|---------------------------|
| Name               | `SonarQube`               |
| Server URL         | `http://sonarqube:9000`   |
| Server authentication token | `sonar-token`   |

> Utiliser `sonarqube` (nom du service Docker), pas `localhost`, car Jenkins tourne dans un conteneur.

### 3.4 Job Pipeline

1. **New Item** → nom : `b2u-hub-pipeline` → type **Pipeline**
2. **Configure → Pipeline** :
   - Definition : **Pipeline script**
   - Script : copier le contenu de [`jenkins-pipeline.groovy`](jenkins-pipeline.groovy)
3. **Save** → **Build Now**

Alternative SCM : Definition **Pipeline script from SCM**, repo GitHub, Script Path `Jenkinsfile`.

---

## 4. Étapes du pipeline

| Stage                  | Action                                      |
|------------------------|---------------------------------------------|
| Checkout               | Clone `main` depuis GitHub                  |
| Build & Test Backend   | `./mvnw clean verify`                       |
| SonarQube Analysis     | `sonar:sonar` avec token `sonar-token`      |
| Quality Gate           | Attente résultat SonarQube (non bloquant)   |
| Docker Build Backend   | Image `b2u-hub-backend`                     |
| Docker Build Frontend  | Image `b2u-hub-frontend`                    |

---

## 5. Fichiers du dépôt

| Fichier | Rôle |
|---------|------|
| `Jenkinsfile` | Pipeline Linux (Jenkins Docker) |
| `Jenkinsfile.windows` | Pipeline Windows natif (`bat`) |
| `devops/jenkins-pipeline.groovy` | Script à coller dans l'UI Jenkins |
| `devops/docker-compose.devops.yml` | Jenkins + SonarQube + PostgreSQL |
| `devops/jenkins/Dockerfile` | Jenkins + Docker CLI + Maven |
| `Dockerfile.backend` | Image Spring Boot |
| `frontend/Dockerfile` | Build Angular + nginx |
| `sonar-project.properties` | Config analyse SonarQube |

---

## 6. Dépannage

### `bat` introuvable

Jenkins tourne en **Linux** (conteneur). Utiliser `sh`, pas `bat`. Le fichier `Jenkinsfile` (racine) est le bon.

### `-Dsonar.token=` vide

- Vérifier que le credential ID est exactement **`sonar-token`**
- Le pipeline utilise `withCredentials` — ne pas utiliser `SONAR_AUTH_TOKEN` seul

### `withSonarQubeEnv` introuvable

Installer le plugin **SonarQube Scanner**.

### SonarQube : 401 Unauthorized

Régénérer un token dans SonarQube et mettre à jour le credential Jenkins (**Change Password** sur `sonar-token`).

### Docker build échoue dans Jenkins

```powershell
docker compose -f devops/docker-compose.devops.yml up -d --build jenkins
docker exec b2u-jenkins docker ps
```

### Quality Gate en échec

Normal au premier build. Le pipeline continue (`abortPipeline: false`). Consulter http://localhost:9000 → projet **b2u-hub**.

---

## 7. Build Docker manuel (hors Jenkins)

```powershell
# Backend
docker build -f Dockerfile.backend -t b2u-hub-backend:latest .

# Frontend
docker build -f Dockerfile -t b2u-hub-frontend:latest ./frontend
```

---

## 8. Analyse SonarQube locale

```powershell
$env:SONAR_TOKEN = "squ_VOTRE_TOKEN"
.\mvnw sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.token=$env:SONAR_TOKEN
```
