import type { AiFeedbackRequest, AiFeedbackResponse } from './evaluation-api.service';

/** Repli local si le backend n’est pas démarré ou pas recompilé (même logique que AiFeedbackService Java). */
export function suggestFeedbackFallback(
  request: AiFeedbackRequest,
): AiFeedbackResponse {
  const rating = clamp(request.rating);
  const quality = clamp(request.quality ?? rating);
  const communication = clamp(request.communication ?? rating);
  const professionalism = clamp(request.professionalism ?? rating);
  const average = (quality + communication + professionalism) / 3;

  const strengths: string[] = [];
  const improvements: string[] = [];

  if (quality >= 4) {
    strengths.push('Livrables de bonne qualité, conformes aux attentes du projet.');
  } else if (quality <= 2) {
    improvements.push('Renforcer la qualité et la finition des livrables.');
  }
  if (communication >= 4) {
    strengths.push('Communication claire et réactive avec l’équipe.');
  } else if (communication <= 2) {
    improvements.push('Améliorer la réactivité et la clarté des échanges.');
  }
  if (professionalism >= 4) {
    strengths.push('Attitude professionnelle et respect des délais.');
  } else if (professionalism <= 2) {
    improvements.push('Travailler la ponctualité et le professionnalisme au quotidien.');
  }
  if (!strengths.length && average >= 3.5) {
    strengths.push(`Participation correcte au projet ${request.projectTitle}.`);
  }
  if (!improvements.length && average < 3.5) {
    improvements.push(
      'Consolider les compétences techniques et relationnelles sur de futurs projets.',
    );
  }

  const tone =
    average >= 4 ? 'excellent' : average >= 3 ? 'satisfaisant' : 'à renforcer';
  let comment = `${request.studentName} a mené le projet « ${request.projectTitle} » avec un niveau ${tone} (note globale : ${rating}/5). `;
  if (strengths.length) {
    comment += `Points forts : ${strengths.join(' ')} `;
  }
  if (improvements.length) {
    comment += `Axes d'amélioration : ${improvements.join(' ')} `;
  }
  comment +=
    " Ce retour a été assisté par l'IA B2U-HUB (mode local — redémarrez le backend pour l’API Spring).";

  return {
    suggestedComment: comment.trim(),
    strengths,
    improvements,
    insightSummary: `Profil analysé : note globale ${rating}/5, moyenne critères ${average.toFixed(1)}/5.`,
    confidenceScore: Math.round((0.75 + (average / 5) * 0.2) * 100) / 100,
    modelLabel: 'B2U-HUB AI Assistant (mode local)',
  };
}

function clamp(n: number | undefined): number {
  if (n == null) return 1;
  return Math.max(1, Math.min(5, n));
}
