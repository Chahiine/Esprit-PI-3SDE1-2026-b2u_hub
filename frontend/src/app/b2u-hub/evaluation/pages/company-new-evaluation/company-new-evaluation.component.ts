import { Component, inject, signal } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { EvaluationMockService } from '../../evaluation-mock.service';
import {
  AiFeedbackResponse,
  EvaluationApiService,
} from '../../evaluation-api.service';
import { StarRatingInputComponent } from '../../shared/star-rating-input.component';
import { formatHttpError } from '../../http-error.util';

@Component({
  selector: 'app-company-new-evaluation',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, StarRatingInputComponent],
  templateUrl: './company-new-evaluation.component.html',
})
export class CompanyNewEvaluationComponent {
  private readonly fb = inject(FormBuilder);
  private readonly mock = inject(EvaluationMockService);
  private readonly api = inject(EvaluationApiService);
  private readonly router = inject(Router);

  readonly projets = this.mock.projetsTermines;

  readonly noteGlobale = signal(0);
  readonly qualiteLivraisons = signal(0);
  readonly communication = signal(0);
  readonly professionnalisme = signal(0);

  readonly aiLoading = signal(false);
  readonly aiError = signal<string | null>(null);
  readonly aiResult = signal<AiFeedbackResponse | null>(null);

  readonly form = this.fb.nonNullable.group({
    projetId: ['', Validators.required],
    commentaire: ['', [Validators.required, Validators.minLength(10)]],
  });

  projetSelectionne() {
    const id = this.form.controls.projetId.value;
    return this.projets().find((x) => x.id === id);
  }

  etudiantCourant(): string {
    return this.projetSelectionne()?.etudiantNom ?? '—';
  }

  emailEtudiant(): string {
    return this.projetSelectionne()?.etudiantEmail ?? '—';
  }

  genererFeedbackIa() {
    const p = this.projetSelectionne();
    if (!p || !this.noteGlobale()) {
      this.aiError.set('Choisissez un projet et une note globale avant d’utiliser l’IA.');
      return;
    }
    this.aiError.set(null);
    this.aiLoading.set(true);
    this.aiResult.set(null);

    this.api
      .suggestFeedback({
        studentName: p.etudiantNom,
        projectTitle: p.titre,
        rating: this.noteGlobale(),
        quality: this.qualiteLivraisons() || this.noteGlobale(),
        communication: this.communication() || this.noteGlobale(),
        professionalism: this.professionnalisme() || this.noteGlobale(),
      })
      .subscribe({
        next: (res) => {
          this.aiResult.set(res);
          this.form.controls.commentaire.setValue(res.suggestedComment);
          this.aiLoading.set(false);
        },
        error: (err) => {
          this.aiLoading.set(false);
          this.aiError.set(formatHttpError(err));
        },
      });
  }

  enregistrer() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const p = this.projetSelectionne();
    if (!p || !this.noteGlobale()) return;

    const body = {
      studentName: p.etudiantNom,
      studentEmail: p.etudiantEmail,
      enterpriseName: p.entrepriseNom,
      projectTitle: p.titre,
      rating: this.noteGlobale(),
      comment: this.form.controls.commentaire.value.trim(),
      projectDate: new Date().toISOString().slice(0, 10),
    };

    this.api.createEvaluation(body).subscribe({
      next: (res) => {
        const msg =
          res?.emailMessage ??
          (res?.emailSent
            ? 'Notification e-mail envoyée à l’étudiant.'
            : 'Évaluation enregistrée avec succès.');
        sessionStorage.setItem('b2u_last_email_status', msg);
        void this.router.navigateByUrl('/evaluation/entreprise');
      },
      error: () => {
        this.mock.ajouterEvaluationEntreprise({
          projetId: p.id,
          projetTitre: p.titre,
          etudiantNom: p.etudiantNom,
          noteGlobale: this.noteGlobale(),
          qualiteLivraisons: this.qualiteLivraisons() || this.noteGlobale(),
          communication: this.communication() || this.noteGlobale(),
          professionnalisme: this.professionnalisme() || this.noteGlobale(),
          commentaire: body.comment!,
        });
        void this.router.navigateByUrl('/evaluation/entreprise');
      },
    });
  }
}
