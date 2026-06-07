export type MissionStatus = 'OPEN' | 'IN_PROGRESS' | 'CLOSED';

export interface Mission {
  id?: number;
  title: string;
  description: string;
  enterpriseName: string;
  skillsRequired: string;   // "Java, Angular, Spring"
  status: MissionStatus;
  level: string;            // BEGINNER | INTERMEDIATE | EXPERT
  budget?: number;
  deadline?: string;        // ISO date string
  category: string;         // badge label on card
  createdByUserId?: number;
  createdAt?: string;
}

export interface MissionDto {
  title: string;
  description: string;
  enterpriseName: string;
  skillsRequired: string;
  status?: MissionStatus;
  level: string;
  budget?: number;
  deadline?: string;
  category: string;
  createdByUserId?: number;
}
