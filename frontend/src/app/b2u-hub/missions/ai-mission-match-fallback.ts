import type {
  AiMissionMatchRequest,
  AiMissionMatchResponse,
  MissionDto,
  MissionSearchFilters,
} from './models';

export function matchMissionsFallback(
  request: AiMissionMatchRequest,
  missions: MissionDto[],
): AiMissionMatchResponse {
  const studentTokens = tokenize(request.skills ?? '');
  const location = request.preferredLocation?.trim().toLowerCase();

  const ranked = missions
    .filter((m) => m.status === 'OPEN')
    .map((mission) => {
      const missionTokens = new Set([
        ...tokenize(mission.skillsRequired ?? ''),
        ...tokenize(mission.title),
      ]);
      const strengths: string[] = [];
      const gaps: string[] = [];

      missionTokens.forEach((token) => {
        if (studentTokens.has(token)) {
          strengths.push(`Compétence alignée : ${token}`);
        } else if (token.length > 2) {
          gaps.push(`À renforcer : ${token}`);
        }
      });

      let score = Math.min(
        100,
        Math.max(20, strengths.length * 18 + missionTokens.size * 2),
      );
      if (
        location &&
        mission.location?.toLowerCase().includes(location)
      ) {
        score = Math.min(100, score + 10);
        strengths.push(`Localisation : ${mission.location}`);
      }

      return {
        missionId: mission.id ?? 0,
        title: mission.title,
        companyName: mission.companyName,
        location: mission.location,
        skillsRequired: mission.skillsRequired,
        matchScore: score,
        matchLevel: score >= 75 ? 'EXCELLENT' : score >= 50 ? 'BON' : 'PARTIEL',
        strengths: strengths.slice(0, 4),
        gaps: gaps.slice(0, 3),
        recommendation:
          score >= 75
            ? 'Profil très aligné — postulez.'
            : 'Bonne opportunité avec montée en compétences.',
      };
    })
    .sort((a, b) => b.matchScore - a.matchScore)
    .slice(0, request.maxResults ?? 5);

  return {
    studentSummary: `${request.studentName} — compétences : ${request.skills ?? '—'}`,
    matches: ranked,
    globalInsight: 'Matching local (backend indisponible ou Gemini non configuré).',
    modelLabel: 'B2U-HUB AI Matcher (mode local)',
    externalApiUrl:
      'https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent',
    confidenceScore: 0.7,
  };
}

function tokenize(text: string): Set<string> {
  return new Set(
    text
      .toLowerCase()
      .split(/[,;\s/|+]+/)
      .map((s) => s.trim())
      .filter((s) => s.length > 2),
  );
}
