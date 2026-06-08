# Pipeline DevOps — B2U-HUB (4 pipelines CI/CD)

Architecture conforme au cours PI : **2 CI + 2 CD**, avec declenchement automatique CD apres succes CI.

```
b2u-ci-backend  ──success──>  b2u-cd-backend
b2u-ci-frontend ──success──>  b2u-cd-frontend
```

---

## 1. Prerequis

- Docker Desktop demarre
- Ports libres : `8080` (Jenkins), `9000` (SonarQube), `8081` (backend), `4200` (frontend)

```powershell
cd devops
.\start-devops.ps1
```

Si Docker manque dans Jenkins :

```powershell
.\rebuild-jenkins.ps1
```

---

## 2. Configuration Jenkins (une fois)

### Plugins

- Pipeline
- Git
- SonarQube Scanner

### Credential SonarQube

| Champ | Valeur |
|-------|--------|
| ID | `sonar-token` |
| Type | Secret text |
| Secret | token `squ_...` de SonarQube |

### Serveur SonarQube

**Manage Jenkins → System → SonarQube servers**

| Champ | Valeur |
|-------|--------|
| Name | `SonarQube` |
| URL | `http://sonarqube:9000` |
| Token | `sonar-token` |

---

## 3. Creer les 4 jobs Pipeline

Pour chaque job : **New Item** → type **Pipeline** → **Pipeline script** → coller le fichier indique.

| Job Jenkins | Fichier script | Role |
|-------------|----------------|------|
| `b2u-ci-backend` | `devops/jenkins/Jenkinsfile.ci-backend` | Build Maven, tests, SonarQube |
| `b2u-ci-frontend` | `devops/jenkins/Jenkinsfile.ci-frontend` | npm ci, tests, build Angular |
| `b2u-cd-backend` | `devops/jenkins/Jenkinsfile.cd-backend` | Docker build + deploy backend |
| `b2u-cd-frontend` | `devops/jenkins/Jenkinsfile.cd-frontend` | Docker build + deploy frontend |

**Important :** les noms des jobs doivent etre **exactement** ceux du tableau (le CI declenche le CD par nom).

---

## 4. Detail des pipelines

### CI Backend (`b2u-ci-backend`)

1. Checkout GitHub
2. `./mvnw clean verify` (tests + JaCoCo)
3. SonarQube analysis
4. **Post-success** → declenche `b2u-cd-backend`

### CI Frontend (`b2u-ci-frontend`)

1. Checkout GitHub
2. `npm ci`
3. `npm run test:ci` (Karma headless)
4. `npm run build --configuration=production`
5. **Post-success** → declenche `b2u-cd-frontend`

### CD Backend (`b2u-cd-backend`)

1. Checkout
2. `docker build` image `b2u-hub-backend`
3. `docker run` sur port **8081**

### CD Frontend (`b2u-cd-frontend`)

1. Checkout
2. `docker build` image `b2u-hub-frontend`
3. `docker run` sur port **4200**

---

## 5. Lancer les pipelines

```text
Build Now sur b2u-ci-backend   → declenche automatiquement b2u-cd-backend
Build Now sur b2u-ci-frontend  → declenche automatiquement b2u-cd-frontend
```

Les jobs CD ne doivent **pas** etre lances manuellement en usage normal (sauf test).

---

## 6. URLs apres deploiement

| Service | URL |
|---------|-----|
| Jenkins | http://localhost:8080 |
| SonarQube | http://localhost:9000 |
| Backend (CD) | http://localhost:8081 |
| Frontend (CD) | http://localhost:4200 |

---

## 7. Depannage

### `docker: not found`

```powershell
.\rebuild-jenkins.ps1
```

### `fatal: not in a git directory`

Workspace corrompu → **Wipe Out Current Workspace** sur le job, puis relancer.

### Tests frontend echouent (Chrome)

Reconstruire Jenkins (Node 20 + Chromium inclus dans l'image).

### CD backend ne demarre pas l'app

Le backend a besoin de PostgreSQL en local. Pour la demo CD Docker seule, l'image est buildée et le conteneur est cree ; configurer PostgreSQL separement si necessaire.

---

## 8. Fichiers

| Fichier | Description |
|---------|-------------|
| `devops/jenkins/Jenkinsfile.ci-backend` | Pipeline CI Backend |
| `devops/jenkins/Jenkinsfile.ci-frontend` | Pipeline CI Frontend |
| `devops/jenkins/Jenkinsfile.cd-backend` | Pipeline CD Backend |
| `devops/jenkins/Jenkinsfile.cd-frontend` | Pipeline CD Frontend |
| `devops/docker-compose.devops.yml` | Jenkins + SonarQube |
| `Dockerfile.backend` | Image Spring Boot |
| `frontend/Dockerfile` | Image Angular + nginx |
