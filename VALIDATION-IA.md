# Validation B2U-HUB — Tâche avancée IA

## Pitch (30 secondes)

> « Nous avons intégré un **assistant IA** dans le module **Évaluation & Feedback** : l’entreprise saisit ses notes sur plusieurs critères, l’IA analyse le profil et **propose un feedback professionnel** structuré, qui alimente le **scoring de crédibilité** des freelances sur B2U-HUB. »

## Démo live (ordre)

1. Démarrer **PostgreSQL** + **Spring Boot** (`PiApplication`, port **8081**).
2. Démarrer **Angular** : `cd frontend` → `npm start` → http://localhost:4200
3. Menu **B2U-HUB** → **Évaluations (entreprise)** → **Nouvelle évaluation**
4. Choisir un projet, mettre les **étoiles**
5. Cliquer **Générer le feedback avec l’IA** → texte rempli automatiquement
6. **Publier** → visible via GET http://localhost:8081/api/evaluations

## API IA (Postman)

**POST** `http://localhost:8081/api/ai/suggest-feedback`

```json
{
  "studentName": "Ahmed",
  "projectTitle": "Refonte site web",
  "rating": 5,
  "quality": 5,
  "communication": 4,
  "professionalism": 5
}
```

## Architecture technique

| Couche | Rôle |
|--------|------|
| Angular | Formulaire + bouton IA |
| `AiFeedbackController` | REST `/api/ai/suggest-feedback` |
| `AiFeedbackService` | Analyse multi-critères + génération texte |
| PostgreSQL | Persistance des évaluations |

## Évolution (mention jury)

Le moteur actuel est **hybride** (règles + NLP léger, démo fiable sans clé API). En production : branchement **OpenAI / Gemini** sur la même interface `AiFeedbackService`.
