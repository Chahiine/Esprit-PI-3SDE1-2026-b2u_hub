export type MissionStatus = 'OPEN' | 'IN_PROGRESS' | 'CLOSED';

export interface Mission {
  id?: number;
  title: string;
  description: string;
  enterpriseName: string;
  skillsRequired: string; // ← camelCase — correspond au JSON backend
  status: MissionStatus;
  level: string;
  budget?: number;
  deadline?: string;
  category: string;
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
