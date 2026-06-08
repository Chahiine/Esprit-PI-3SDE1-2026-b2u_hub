# Pipeline DevOps — B2U-HUB (2 pipelines)

Architecture : **Backend** puis **Frontend**, avec trigger Jenkins automatique.

```
b2u-pipeline-backend  ──succes──>  b2u-pipeline-frontend
         (1er)                           (2eme)
```

Le backend declenche le frontend de **2 facons** :
1. **build job** dans le `post { success }` du pipeline backend
2. **Trigger upstream** configure dans le pipeline frontend

---

## 1. Demarrer Jenkins + SonarQube

```powershell
cd devops
.\start-devops.ps1
.\rebuild-jenkins.ps1
```

---

## 2. Creer les 2 jobs Jenkins

### Job 1 : `b2u-pipeline-backend` (lancer en premier)

1. **New Item** → nom : `b2u-pipeline-backend` → **Pipeline**
2. **Configure → Pipeline → Pipeline script**
3. Coller le contenu de `devops/jenkins/Jenkinsfile.pipeline-backend`
4. **Save**

### Job 2 : `b2u-pipeline-frontend` (declenche automatiquement)

1. **New Item** → nom : `b2u-pipeline-frontend` → **Pipeline**
2. **Configure → Pipeline → Pipeline script**
3. Coller le contenu de `devops/jenkins/Jenkinsfile.pipeline-frontend`
4. **Save**

> Les noms doivent etre **exactement** `b2u-pipeline-backend` et `b2u-pipeline-frontend`.

---

## 3. Contenu des pipelines

### Pipeline Backend (`b2u-pipeline-backend`)

| Etape | Action |
|-------|--------|
| Checkout | Clone GitHub |
| Build & Test | `mvnw clean verify` |
| SonarQube | Analyse qualite |
| Docker Build | Image `b2u-hub-backend` |
| Deploy | `docker run` port **8082** (8081 = dev local) |
| **Trigger** | Lance `b2u-pipeline-frontend` |

### Pipeline Frontend (`b2u-pipeline-frontend`)

| Etape | Action |
|-------|--------|
| Trigger | Attend succes de `b2u-pipeline-backend` |
| Checkout | Clone GitHub |
| Install | `npm ci` |
| Tests | `npm run test:ci` |
| Build | `npm run build` production |
| Docker Build | Image `b2u-hub-frontend` |
| Deploy | `docker run` port **4201** (4200 = dev local) |

---

## 4. Lancer

**Build Now** sur `b2u-pipeline-backend` uniquement.

Le frontend se lance automatiquement apres le succes du backend.

---

## 5. Configuration SonarQube (backend)

- Credential Jenkins ID : `sonar-token`
- Serveur SonarQube : Name `SonarQube`, URL `http://sonarqube:9000`

---

## 6. URLs

| Service | URL |
|---------|-----|
| Jenkins | http://localhost:8080 |
| SonarQube | http://localhost:9000 |
| Backend (Docker) | http://localhost:8082 |
| Frontend (Docker) | http://localhost:4201 |
| Backend (dev local) | http://localhost:8081 |
| Frontend (dev local) | http://localhost:4200 |

---

## 7. Fichiers

| Fichier | Job Jenkins |
|---------|-------------|
| `devops/jenkins/Jenkinsfile.pipeline-backend` | `b2u-pipeline-backend` |
| `devops/jenkins/Jenkinsfile.pipeline-frontend` | `b2u-pipeline-frontend` |
