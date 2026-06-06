# PI — B2U-HUB Évaluation & Feedback

Plateforme web de mise en relation **entreprises / freelances (étudiants)** avec module **Évaluation & Feedback**, assistant **IA** pour rédiger les commentaires, et **notification e-mail** à l'étudiant.

---

## Fonctionnalités

| Module | Description |
|--------|-------------|
| **Vitrine Eduleb** | Pages Angular (accueil, cours, blog, contact…) |
| **Espace entreprise** | Évaluer un étudiant, historique API, IA feedback |
| **Espace étudiant** | Voir les feedbacks reçus, noter une entreprise, répondre |
| **API REST** | Spring Boot + PostgreSQL |
| **IA** | `POST /api/ai/suggest-feedback` — moteur Java interne (règles + texte) |
| **E-mail** | Notification après publication (mode console ou Gmail SMTP) |

---

## Stack technique

| Couche | Technologie | Port |
|--------|-------------|------|
| Frontend | Angular 18, TypeScript | 4200 |
| Backend | Spring Boot 4, Java 17 | 8081 |
| Base | PostgreSQL (`b2u_hub`) | 5432 |

---

## Démarrage rapide

### 1. Base de données

```sql
CREATE DATABASE b2u_hub;
```

Script : `scripts/create-database-b2u_hub.sql`

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

### Pages module B2U-HUB

| URL | Rôle |
|-----|------|
| http://localhost:4200/evaluation/entreprise | Dashboard entreprise |
| http://localhost:4200/evaluation/entreprise/nouvelle | Nouvelle évaluation + IA |
| http://localhost:4200/evaluation/etudiant | Espace étudiant |

---

## API principale

**Base :** `http://localhost:8081`

| Endpoint | Méthode | Rôle |
|----------|---------|------|
| `/api/ai/suggest-feedback` | POST | Générer feedback IA |
| `/api/evaluations` | GET/POST | Liste / créer évaluation |
| `/api/users/register` | POST | Inscrire un utilisateur |
| `/api/mail/test` | POST | Tester l'e-mail |
| `/api/mail/status` | GET | État config e-mail |

Collection Postman : `postman/evaluations-crud.postman_collection.json`

---

## IA — quelle API ?

**Aucune API externe** (pas OpenAI / ChatGPT dans la version actuelle).

Le feedback est généré par **`AiFeedbackService.java`** (règles métier + assemblage de texte), exposé via **`POST /api/ai/suggest-feedback`**.

---

## Configuration

| Fichier | Rôle |
|---------|------|
| `src/main/resources/application.properties` | PostgreSQL, port, e-mail |
| `src/main/resources/application-local.properties` | Gmail (non versionné) |
| `frontend/src/environments/environment.ts` | URL API Angular |

**Prérequis :** JDK 17, Node 18+, PostgreSQL, Maven (`mvnw`).

---

## Auteurs / démo

Étudiants de démo : Chahine Sassi, Samira Benali, Lucas Martin, Youssef Trabelsi, Ines Gharbi.

---

*Projet académique PI — module B2U-HUB.*
