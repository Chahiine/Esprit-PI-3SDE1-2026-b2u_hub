export type MissionStatus = 'OPEN' | 'IN_PROGRESS' | 'CLOSED';

export interface MissionDto {
  id?: number;
  title: string;
  description?: string;
  companyName: string;
  location?: string;
  skillsRequired?: string;
  duration?: string;
  status: MissionStatus;
  companyEmail?: string;
  externalLink?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface MissionSearchFilters {
  keyword?: string;
  location?: string;
  status?: MissionStatus | '';
  skills?: string;
  companyName?: string;
}

export interface AiMissionDescriptionRequest {
  title: string;
  companyName?: string;
  location?: string;
  skillsRequired?: string;
  duration?: string;
  externalLink?: string;
}

export interface AiMissionDescriptionResponse {
  suggestedDescription: string;
  suggestedTitle: string;
  insightSummary: string;
  externalApiUrl: string;
  modelLabel: string;
  confidenceScore: number;
}

export interface AiMissionMatchRequest {
  studentName: string;
  studentEmail?: string;
  skills?: string;
  preferredLocation?: string;
  maxResults?: number;
}

export interface MissionMatchItem {
  missionId: number;
  title: string;
  companyName: string;
  location?: string;
  skillsRequired?: string;
  matchScore: number;
  matchLevel: string;
  strengths: string[];
  gaps: string[];
  recommendation: string;
}

export interface AiMissionMatchResponse {
  studentSummary: string;
  matches: MissionMatchItem[];
  globalInsight: string;
  modelLabel: string;
  externalApiUrl: string;
  confidenceScore: number;
}

export const MISSION_STATUS_LABELS: Record<MissionStatus, string> = {
  OPEN: 'Ouverte',
  IN_PROGRESS: 'En cours',
  CLOSED: 'Clôturée',
};
