import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, map, of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { suggestFeedbackFallback } from './ai-feedback-fallback';

export interface AiFeedbackRequest {
  studentName: string;
  projectTitle: string;
  rating: number;
  quality?: number;
  communication?: number;
  professionalism?: number;
}

export interface AiFeedbackResponse {
  suggestedComment: string;
  strengths: string[];
  improvements: string[];
  insightSummary: string;
  confidenceScore: number;
  modelLabel: string;
}

export interface EvaluationDto {
  id?: number;
  studentName: string;
  studentEmail: string;
  enterpriseName: string;
  projectTitle: string;
  rating: number;
  comment?: string;
  projectDate: string | { year: number; month: number; day?: number; dayOfMonth?: number };
}

export interface EvaluationCreateResponse {
  evaluation: EvaluationDto;
  emailSent: boolean;
  emailMessage: string;
}

export interface FeedbackResponseDto {
  id?: number;
  evaluationId: number;
  studentName: string;
  studentEmail: string;
  message: string;
  createdAt?: string;
}

export interface FeedbackResponseCreateRequest {
  evaluationId: number;
  studentName: string;
  studentEmail: string;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class EvaluationApiService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl
    ? `${environment.apiUrl}/api`
    : '/api';

  suggestFeedback(body: AiFeedbackRequest): Observable<AiFeedbackResponse> {
    return this.http
      .post<AiFeedbackResponse>(`${this.base}/ai/suggest-feedback`, body)
      .pipe(
        catchError(() => of(suggestFeedbackFallback(body))),
      );
  }

  createEvaluation(body: EvaluationDto): Observable<EvaluationCreateResponse> {
    return this.http.post<EvaluationCreateResponse | EvaluationDto>(
      `${this.base}/evaluations`,
      body,
    ).pipe(
      map((res) => this.normalizeCreateResponse(res)),
    );
  }

  private normalizeCreateResponse(
    res: EvaluationCreateResponse | EvaluationDto,
  ): EvaluationCreateResponse {
    if (res && 'emailMessage' in res && res.emailMessage) {
      return res as EvaluationCreateResponse;
    }
    const evaluation =
      'evaluation' in res && res.evaluation
        ? (res as EvaluationCreateResponse).evaluation
        : (res as EvaluationDto);
    return {
      evaluation,
      emailSent: false,
      emailMessage: 'Évaluation enregistrée avec succès.',
    };
  }

  createFeedbackResponse(
    body: FeedbackResponseCreateRequest,
  ): Observable<FeedbackResponseDto> {
    return this.http.post<FeedbackResponseDto>(`${this.base}/feedback-responses`, body);
  }

  listFeedbackResponses(studentEmail: string): Observable<FeedbackResponseDto[]> {
    return this.http.get<FeedbackResponseDto[]>(
      `${this.base}/feedback-responses`,
      { params: { studentEmail } },
    );
  }

  listEvaluations(): Observable<EvaluationDto[]> {
    return this.http.get<EvaluationDto[]>(`${this.base}/evaluations`).pipe(
      map((list) => {
        const rows = Array.isArray(list) ? list : [];
        return rows.map((e) => ({
          ...e,
          studentEmail: e.studentEmail ?? '—',
          projectDate: this.formatProjectDate(e.projectDate),
        }));
      }),
    );
  }

  private formatProjectDate(value: EvaluationDto['projectDate']): string {
    if (!value) return '—';
    if (typeof value === 'string') return value;
    if (typeof value === 'object' && value !== null && 'year' in value) {
      const d = value as { year: number; month: number; day?: number; dayOfMonth?: number };
      const day = d.day ?? d.dayOfMonth ?? 1;
      const month = String(d.month).padStart(2, '0');
      const dayStr = String(day).padStart(2, '0');
      return `${d.year}-${month}-${dayStr}`;
    }
    return String(value);
  }
}
