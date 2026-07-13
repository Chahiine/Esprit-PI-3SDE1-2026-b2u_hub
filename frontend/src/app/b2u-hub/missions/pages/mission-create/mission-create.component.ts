import { Component, inject, signal } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MissionApiService } from '../../mission-api.service';
import type { AiMissionDescriptionResponse } from '../../models';
import { formatHttpError } from '../../../evaluation/http-error.util';

@Component({
  selector: 'app-mission-create',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './mission-create.component.html',
})
export class MissionCreateComponent {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(MissionApiService);
  private readonly router = inject(Router);

  readonly aiLoading = signal(false);
  readonly aiError = signal<string | null>(null);
  readonly aiResult = signal<AiMissionDescriptionResponse | null>(null);
  readonly saving = signal(false);
  readonly saveError = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.minLength(3)]],
    description: ['', [Validators.required, Validators.minLength(20)]],
    companyName: ['', Validators.required],
    location: [''],
    skillsRequired: [''],
    duration: [''],
    status: ['OPEN' as const],
    companyEmail: [''],
    externalLink: [''],
  });

  genererDescriptionIa() {
    const v = this.form.getRawValue();
    if (!v.title.trim()) {
      this.aiError.set('Saisissez au minimum un titre avant d’utiliser l’IA.');
      return;
    }
    this.aiError.set(null);
    this.aiLoading.set(true);
    this.aiResult.set(null);

    this.api
      .suggestMissionDescription({
        title: v.title.trim(),
        companyName: v.companyName.trim() || undefined,
        location: v.location.trim() || undefined,
        skillsRequired: v.skillsRequired.trim() || undefined,
        duration: v.duration.trim() || undefined,
        externalLink: v.externalLink.trim() || undefined,
      })
      .subscribe({
        next: (res) => {
          this.aiResult.set(res);
          if (res.suggestedTitle) {
            this.form.controls.title.setValue(res.suggestedTitle);
          }
          this.form.controls.description.setValue(res.suggestedDescription);
          this.aiLoading.set(false);
        },
        error: (err) => {
          this.aiLoading.set(false);
          this.aiError.set(formatHttpError(err));
        },
      });
  }

  publier() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saveError.set(null);
    this.saving.set(true);
    const v = this.form.getRawValue();

    this.api
      .createMission({
        title: v.title.trim(),
        description: v.description.trim(),
        companyName: v.companyName.trim(),
        location: v.location.trim() || undefined,
        skillsRequired: v.skillsRequired.trim() || undefined,
        duration: v.duration.trim() || undefined,
        status: v.status,
        companyEmail: v.companyEmail.trim() || undefined,
        externalLink: v.externalLink.trim() || undefined,
      })
      .subscribe({
        next: () => {
          this.saving.set(false);
          void this.router.navigateByUrl('/missions/entreprise');
        },
        error: (err) => {
          this.saving.set(false);
          this.saveError.set(formatHttpError(err));
        },
      });
  }
}
