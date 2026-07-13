import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, map, of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { suggestMissionDescriptionFallback } from './ai-mission-fallback';
import { matchMissionsFallback } from './ai-mission-match-fallback';
import type {
  AiMissionDescriptionRequest,
  AiMissionDescriptionResponse,
  AiMissionMatchRequest,
  AiMissionMatchResponse,
  MissionDto,
  MissionSearchFilters,
} from './models';

@Injectable({ providedIn: 'root' })
export class MissionApiService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl
    ? `${environment.apiUrl}/api`
    : '/api';

  listMissions(filters: MissionSearchFilters = {}): Observable<MissionDto[]> {
    let params = new HttpParams();
    if (filters.keyword?.trim()) {
      params = params.set('keyword', filters.keyword.trim());
    }
    if (filters.location?.trim()) {
      params = params.set('location', filters.location.trim());
    }
    if (filters.status) {
      params = params.set('status', filters.status);
    }
    if (filters.skills?.trim()) {
      params = params.set('skills', filters.skills.trim());
    }
    if (filters.companyName?.trim()) {
      params = params.set('companyName', filters.companyName.trim());
    }
    return this.http.get<MissionDto[]>(`${this.base}/missions`, { params });
  }

  getMission(id: number): Observable<MissionDto> {
    return this.http.get<MissionDto>(`${this.base}/missions/${id}`);
  }

  createMission(body: MissionDto): Observable<MissionDto> {
    return this.http.post<MissionDto>(`${this.base}/missions`, body);
  }

  updateMission(id: number, body: MissionDto): Observable<MissionDto> {
    return this.http.put<MissionDto>(`${this.base}/missions/${id}`, body);
  }

  deleteMission(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/missions/${id}`);
  }

  suggestMissionDescription(
    body: AiMissionDescriptionRequest,
  ): Observable<AiMissionDescriptionResponse> {
    return this.http
      .post<AiMissionDescriptionResponse>(
        `${this.base}/ai/suggest-mission-description`,
        body,
      )
      .pipe(catchError(() => of(suggestMissionDescriptionFallback(body))));
  }

  matchMissions(body: AiMissionMatchRequest): Observable<AiMissionMatchResponse> {
    return this.http
      .post<AiMissionMatchResponse>(`${this.base}/ai/match-missions`, body)
      .pipe(
        catchError(() =>
          this.listMissions({ status: 'OPEN' }).pipe(
            map((missions) => matchMissionsFallback(body, missions)),
          ),
        ),
      );
  }
}
