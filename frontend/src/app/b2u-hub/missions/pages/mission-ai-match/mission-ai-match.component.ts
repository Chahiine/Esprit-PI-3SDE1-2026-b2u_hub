import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MissionApiService } from '../../mission-api.service';
import type { AiMissionMatchResponse } from '../../models';
import { MISSION_STUDENT_PROFILE } from '../../student-profile';
import { formatHttpError } from '../../../evaluation/http-error.util';

@Component({
  selector: 'app-mission-ai-match',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './mission-ai-match.component.html',
})
export class MissionAiMatchComponent {
  private readonly api = inject(MissionApiService);
  private readonly fb = inject(FormBuilder);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly result = signal<AiMissionMatchResponse | null>(null);

  readonly form = this.fb.nonNullable.group({
    studentName: [MISSION_STUDENT_PROFILE.fullName, Validators.required],
    studentEmail: [MISSION_STUDENT_PROFILE.email],
    skills: [MISSION_STUDENT_PROFILE.skills, Validators.required],
    preferredLocation: [MISSION_STUDENT_PROFILE.preferredLocation],
    maxResults: [5],
  });

  lancerMatching() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    this.loading.set(true);
    this.error.set(null);
    this.result.set(null);

    this.api
      .matchMissions({
        studentName: v.studentName.trim(),
        studentEmail: v.studentEmail.trim() || undefined,
        skills: v.skills.trim(),
        preferredLocation: v.preferredLocation.trim() || undefined,
        maxResults: v.maxResults,
      })
      .subscribe({
        next: (res) => {
          this.result.set(res);
          this.loading.set(false);
        },
        error: (err) => {
          this.error.set(formatHttpError(err));
          this.loading.set(false);
        },
      });
  }

  scoreClass(score: number): string {
    if (score >= 75) return 'bg-success';
    if (score >= 50) return 'bg-warning text-dark';
    return 'bg-secondary';
  }
}
