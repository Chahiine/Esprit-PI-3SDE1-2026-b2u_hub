import { Component, inject, signal } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { EvaluationMockService } from '../../evaluation-mock.service';
import { StarRatingInputComponent } from '../../shared/star-rating-input.component';

@Component({
  selector: 'app-student-rate-company',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, StarRatingInputComponent],
  templateUrl: './student-rate-company.component.html',
  styleUrl: './student-rate-company.component.css',
})
export class StudentRateCompanyComponent {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(EvaluationMockService);
  private readonly router = inject(Router);

  readonly projets = this.api.projetsTermines;

  readonly noteExperience = signal(0);
  readonly clariteBrief = signal(0);
  readonly accompagnement = signal(0);
  readonly recommanderait = signal<boolean | null>(null);

  readonly form = this.fb.nonNullable.group({
    projetId: ['', Validators.required],
    commentaire: ['', [Validators.required, Validators.minLength(10)]],
  });

  projetCourant() {
    const id = this.form.controls.projetId.value;
    return this.projets().find((x) => x.id === id);
  }

  entrepriseCourante(): string {
    return this.projetCourant()?.entrepriseNom ?? '—';
  }

  enregistrer() {
    if (this.form.invalid || this.recommanderait() === null) {
      this.form.markAllAsTouched();
      return;
    }
    const pid = this.form.controls.projetId.value;
    const p = this.projets().find((x) => x.id === pid);
    if (!p || !this.noteExperience()) return;

    this.api.ajouterEvaluationEtudiant({
      projetId: p.id,
      projetTitre: p.titre,
      entrepriseNom: p.entrepriseNom,
      noteExperience: this.noteExperience(),
      clariteBrief: this.clariteBrief() || this.noteExperience(),
      accompagnement: this.accompagnement() || this.noteExperience(),
      recommanderait: this.recommanderait()!,
      commentaire: this.form.controls.commentaire.value.trim(),
    });
    void this.router.navigateByUrl('/evaluation/etudiant');
  }
}
