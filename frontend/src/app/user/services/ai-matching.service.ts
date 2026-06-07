import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface MatchingResult {
  score: number;
  level: string;
  summary: string;
  matchedSkills: string[];
  missingSkills: string[];
  recommendation: string;
}

@Injectable({ providedIn: 'root' })
export class AiMatchingService {
  analyzeMatch(
    studentSkills: string,
    studentBio: string,
    missionTitle: string,
    missionDescription: string,
    missionSkillsRequired: string,
  ): Observable<MatchingResult> {
    return new Observable((observer) => {
      const studentList = studentSkills
        .toLowerCase()
        .split(',')
        .map((s) => s.trim())
        .filter(Boolean);

      const requiredList = missionSkillsRequired
        .toLowerCase()
        .split(',')
        .map((s) => s.trim())
        .filter(Boolean);

      // Match skills
      const matched = requiredList.filter((req) =>
        studentList.some((s) => s.includes(req) || req.includes(s)),
      );
      const missing = requiredList.filter(
        (req) => !studentList.some((s) => s.includes(req) || req.includes(s)),
      );

      // Score calculation
      let score =
        requiredList.length > 0 ? Math.round((matched.length / requiredList.length) * 100) : 50;

      // Bonus if bio mentions mission keywords
      if (studentBio) {
        const bioLower = studentBio.toLowerCase();
        const missionWords = (missionTitle + ' ' + missionDescription)
          .toLowerCase()
          .split(/\s+/)
          .filter((w) => w.length > 4);
        const bioBonus = missionWords.filter((w) => bioLower.includes(w)).length;
        score = Math.min(100, score + Math.min(bioBonus * 2, 10));
      }

      const level =
        score >= 75 ? 'Excellent' : score >= 50 ? 'Bon' : score >= 25 ? 'Moyen' : 'Faible';

      const result: MatchingResult = {
        score,
        level,
        summary: `Vous maîtrisez ${matched.length} sur ${requiredList.length} compétences requises pour "${missionTitle}". ${
          missing.length > 0
            ? `Il vous manque ${missing.length} compétence(s) clé(s).`
            : 'Votre profil correspond bien à cette mission.'
        }`,
        matchedSkills: matched,
        missingSkills: missing,
        recommendation:
          score >= 75
            ? 'Excellent profil pour cette mission. Postulez sans hésiter !'
            : score >= 50
              ? 'Votre profil est compatible. Nous vous encourageons à postuler !'
              : score >= 25
                ? 'Profil partiellement compatible. Renforcez vos compétences manquantes.'
                : 'Nous vous recommandons de compléter votre profil avant de postuler.',
      };

      observer.next(result);
      observer.complete();
    });
  }
}
