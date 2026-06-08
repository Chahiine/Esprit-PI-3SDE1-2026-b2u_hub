import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  EvaluationApiService,
  EvaluationDto,
  FeedbackResponseDto,
} from '../../evaluation-api.service';
import { EvaluationMockService } from '../../evaluation-mock.service';
import { STUDENT_PROFILE } from '../../student-profile';
import { StarRatingDisplayComponent } from '../../shared/star-rating-display.component';

@Component({
  selector: 'app-student-dashboard',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, StarRatingDisplayComponent],
  templateUrl: './student-dashboard.component.html',
  styleUrl: './student-dashboard.component.css',
})
export class StudentDashboardComponent implements OnInit {
  private readonly apiHttp = inject(EvaluationApiService);
  readonly mock = inject(EvaluationMockService);
  private readonly fb = inject(FormBuilder);

  readonly profile = STUDENT_PROFILE;
  readonly feedbacksApi = signal<EvaluationDto[]>([]);
  readonly reponsesFeedback = signal<FeedbackResponseDto[]>([]);
  readonly loading = signal(true);
  readonly savingResponse = signal(false);
  readonly sourceApi = signal(false);

  readonly mesEvaluationsEntreprises = this.mock.evaluationsEtudiant;

  readonly reponseForm = this.fb.nonNullable.group({
    evaluationId: ['', Validators.required],
    texte: ['', [Validators.required, Validators.minLength(5)]],
  });

  readonly ack = signal(false);
  readonly errorMsg = signal('');

  ngOnInit() {
    this.loadFeedbacks();
  }

  loadFeedbacks() {
    this.loading.set(true);
    this.apiHttp.listEvaluations().subscribe({
      next: (list) => {
        const mine = list.filter(
          (e) =>
            e.studentEmail === this.profile.email ||
            e.studentName === this.profile.fullName,
        );
        this.feedbacksApi.set(mine);
        this.sourceApi.set(true);
        this.loading.set(false);
        this.loadReponses();
      },
      error: () => {
        const local = this.mock.evaluationsEntreprise().map((ev) => ({
          id: Number(ev.id.replace(/\D/g, '')) || 0,
          studentName: ev.etudiantNom,
          studentEmail: this.profile.email,
          enterpriseName: 'TechNova SAS',
          projectTitle: ev.projetTitre,
          rating: ev.noteGlobale,
          comment: ev.commentaire,
          projectDate: ev.date,
        }));
        this.feedbacksApi.set(local);
        this.sourceApi.set(false);
        this.loading.set(false);
        this.reponsesFeedback.set(
          this.mock.reponsesFeedback().map((r) => ({
            id: Number(r.id.replace(/\D/g, '')) || undefined,
            evaluationId: Number(r.evaluationId) || 0,
            studentName: this.profile.fullName,
            studentEmail: this.profile.email,
            message: r.texte,
            createdAt: r.date,
          })),
        );
      },
    });
  }

  loadReponses() {
    this.apiHttp.listFeedbackResponses(this.profile.email).subscribe({
      next: (list) => this.reponsesFeedback.set(list),
      error: () => this.reponsesFeedback.set([]),
    });
  }

  feedbacksPourReponse() {
    return this.feedbacksApi().filter((e) => e.id != null);
  }

  reponsesPourEvaluation(evaluationId?: number) {
    if (evaluationId == null) return [];
    return this.reponsesFeedback().filter((r) => r.evaluationId === evaluationId);
  }

  envoyerReponse() {
    if (this.reponseForm.invalid) {
      this.reponseForm.markAllAsTouched();
      return;
    }
    const v = this.reponseForm.getRawValue();
    const evaluationId = Number(v.evaluationId);
    this.errorMsg.set('');
    this.savingResponse.set(true);

    if (!this.sourceApi()) {
      this.mock.ajouterReponseFeedback({
        evaluationId: v.evaluationId,
        texte: v.texte.trim(),
      });
      this.reponsesFeedback.update((list) => [
        {
          evaluationId,
          studentName: this.profile.fullName,
          studentEmail: this.profile.email,
          message: v.texte.trim(),
          createdAt: new Date().toISOString().slice(0, 10),
        },
        ...list,
      ]);
      this.finaliserEnvoi();
      return;
    }

    this.apiHttp
      .createFeedbackResponse({
        evaluationId,
        studentName: this.profile.fullName,
        studentEmail: this.profile.email,
        message: v.texte.trim(),
      })
      .subscribe({
        next: () => {
          this.loadReponses();
          this.finaliserEnvoi();
        },
        error: () => {
          this.errorMsg.set("Impossible d'enregistrer la réponse. Réessayez.");
          this.savingResponse.set(false);
        },
      });
  }

  private finaliserEnvoi() {
    this.reponseForm.reset({ evaluationId: '', texte: '' });
    this.savingResponse.set(false);
    this.ack.set(true);
    setTimeout(() => this.ack.set(false), 3000);
  }

  formatDate(value?: string): string {
    if (!value) return '—';
    return value.length >= 10 ? value.slice(0, 10) : value;
  }

  moyenneRecue(): string {
    const list = this.feedbacksApi();
    if (!list.length) return '';
    const s = list.reduce((a, e) => a + e.rating, 0);
    return (s / list.length).toFixed(1);
  }

  scoreLabel(rating: number): string {
    if (rating >= 5) return 'Excellent';
    if (rating >= 4) return 'Très bien';
    if (rating >= 3) return 'Satisfaisant';
    return 'À améliorer';
  }
}
