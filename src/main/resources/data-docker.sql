-- Donnees de demo missions (Docker) — inseres une seule fois si table vide
INSERT INTO missions (title, description, company_name, location, skills_required, duration, status, company_email, external_link, created_at, updated_at)
SELECT 'Stage développeur Angular', 'Développement du portail B2U-HUB avec Angular et intégration API REST.', 'TechNova SAS', 'Tunis', 'Angular, TypeScript, Spring Boot', '3 mois', 'OPEN', 'hr@technova.tn', 'https://technova.tn/careers/angular', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM missions LIMIT 1);

INSERT INTO missions (title, description, company_name, location, skills_required, duration, status, company_email, external_link, created_at, updated_at)
SELECT 'Stage Spring Boot', 'API REST, PostgreSQL, tests unitaires.', 'InnoSoft', 'Sfax', 'Java, Spring Boot, PostgreSQL', '4 mois', 'OPEN', 'contact@innosoft.tn', NULL, NOW(), NOW()
WHERE (SELECT COUNT(*) FROM missions) < 2;

INSERT INTO missions (title, description, company_name, location, skills_required, duration, status, company_email, external_link, created_at, updated_at)
SELECT 'Stage DevOps — Jenkins & Docker', 'Pipelines CI/CD, SonarQube, déploiement Docker.', 'CloudBridge Tunisia', 'Tunis', 'Jenkins, Docker, SonarQube, Git', '4 mois', 'OPEN', 'recrutement@cloudbridge.tn', 'https://cloudbridge.tn/carrieres/devops', NOW(), NOW()
WHERE (SELECT COUNT(*) FROM missions) < 3;

INSERT INTO missions (title, description, company_name, location, skills_required, duration, status, company_email, external_link, created_at, updated_at)
SELECT 'Stage IA — matching étudiant / mission', 'Intégration API Gemini et scoring de compatibilité.', 'AI Solutions Tunisie', 'Tunis', 'Python, Spring Boot, PostgreSQL, Gemini', '6 mois', 'OPEN', 'ia@aisolutions.tn', 'https://aisolutions.tn/careers/ia', NOW(), NOW()
WHERE (SELECT COUNT(*) FROM missions) < 4;
