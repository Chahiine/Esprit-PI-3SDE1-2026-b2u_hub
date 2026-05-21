import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  EvaluationApiService,
  EvaluationDto,
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
  readonly loading = signal(true);
  readonly sourceApi = signal(false);

  readonly mesEvaluationsEntreprises = this.mock.evaluationsEtudiant;

  readonly reponseForm = this.fb.nonNullable.group({
    evaluationId: ['', Validators.required],
    texte: ['', [Validators.required, Validators.minLength(5)]],
  });

  readonly ack = signal(false);

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
      },
    });
  }

  feedbacksPourReponse() {
    return this.feedbacksApi();
  }

  envoyerReponse() {
    if (this.reponseForm.invalid) {
      this.reponseForm.markAllAsTouched();
      return;
    }
    const v = this.reponseForm.getRawValue();
    this.mock.ajouterReponseFeedback({
      evaluationId: v.evaluationId,
      texte: v.texte.trim(),
    });
    this.reponseForm.reset({ evaluationId: '', texte: '' });
    this.ack.set(true);
    setTimeout(() => this.ack.set(false), 3000);
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
