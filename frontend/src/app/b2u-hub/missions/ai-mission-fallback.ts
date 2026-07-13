import type {
  AiMissionDescriptionRequest,
  AiMissionDescriptionResponse,
} from './models';

export function suggestMissionDescriptionFallback(
  request: AiMissionDescriptionRequest,
): AiMissionDescriptionResponse {
  const title = request.title.trim();
  const company = request.companyName?.trim() || 'notre entreprise';
  const location = request.location?.trim() || 'Tunis';
  const skills = request.skillsRequired?.trim() || 'compétences techniques adaptées';
  const duration = request.duration?.trim() || '3 mois';

  const description =
    `${company} recrute pour la mission « ${title} » basée à ${location} pour une durée de ${duration}. ` +
    `Le profil recherché maîtrise : ${skills}. ` +
    'Vous participerez à un projet concret au sein de l’équipe, avec un encadrement régulier et des livrables définis. ' +
    'Cette offre est publiée sur B2U-HUB pour connecter étudiants et entreprises.' +
    (request.externalLink?.trim()
      ? ` Lien externe : ${request.externalLink.trim()}`
      : '');

  return {
    suggestedTitle: title,
    suggestedDescription: description,
    insightSummary: `Mission orientée ${location} — prioriser les candidats avec ${skills}.`,
    externalApiUrl:
      'https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent',
    modelLabel: 'B2U-HUB AI Assistant (mode local)',
    confidenceScore: 0.72,
  };
}
