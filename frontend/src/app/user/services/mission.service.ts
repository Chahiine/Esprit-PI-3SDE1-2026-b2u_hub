import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Mission, MissionDto } from '../models/mission.model';

@Injectable({ providedIn: 'root' })
export class MissionService {

  private readonly api = `${environment.apiUrl}/api/missions`;

  constructor(private http: HttpClient) {}

  // Public — open missions list
  getOpen(): Observable<Mission[]> {
    return this.http.get<Mission[]>(`${this.api}/open`);
  }

  // Public — mission detail
  getById(id: number): Observable<Mission> {
    return this.http.get<Mission>(`${this.api}/${id}`);
  }

  // Public — search
  search(keyword: string): Observable<Mission[]> {
    return this.http.get<Mission[]>(`${this.api}/search`, { params: { keyword } });
  }

  // Authenticated
  getAll(): Observable<Mission[]> {
    return this.http.get<Mission[]>(`${this.api}`);
  }

  getMyMissions(userId: number): Observable<Mission[]> {
    return this.http.get<Mission[]>(`${this.api}/my/${userId}`);
  }

  create(dto: MissionDto): Observable<Mission> {
    return this.http.post<Mission>(`${this.api}`, dto);
  }

  update(id: number, dto: MissionDto): Observable<Mission> {
    return this.http.put<Mission>(`${this.api}/${id}`, dto);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/${id}`);
  }

  getStats(): Observable<any> {
    return this.http.get<any>(`${this.api}/stats`);
  }

  // Helper: skills string → array
  skillsList(mission: Mission): string[] {
    return (mission.skillsRequired ?? '')
      .split(',')
      .map(s => s.trim())
      .filter(Boolean);
  }

  // Helper: budget display
  budgetLabel(mission: Mission): string {
    if (!mission.budget) return 'Budget à négocier';
    return `${mission.budget} TND`;
  }

  // Helper: category → course image number (1-6)
  categoryImage(category: string): string {
    const map: Record<string, string> = {
      'Frontend':  '1',
      'Backend':   '2',
      'Mobile':    '3',
      'Design':    '4',
      'DevOps':    '5',
      'Data':      '6',
    };
    return map[category] ?? '1';
  }
}
