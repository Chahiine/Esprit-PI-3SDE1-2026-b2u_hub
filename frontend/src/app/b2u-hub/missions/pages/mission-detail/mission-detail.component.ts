import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MissionApiService } from '../../mission-api.service';
import type { MissionDto, MissionStatus } from '../../models';
import { MISSION_STATUS_LABELS } from '../../models';
import { formatHttpError } from '../../../evaluation/http-error.util';

@Component({
  selector: 'app-mission-detail',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './mission-detail.component.html',
})
export class MissionDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(MissionApiService);

  readonly mission = signal<MissionDto | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly statusLabels = MISSION_STATUS_LABELS;

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.error.set('Identifiant de mission invalide.');
      this.loading.set(false);
      return;
    }
    this.api.getMission(id).subscribe({
      next: (m) => {
        this.mission.set(m);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(formatHttpError(err));
        this.loading.set(false);
      },
    });
  }

  statutLabel(status: MissionStatus): string {
    return this.statusLabels[status] ?? status;
  }

  formatDate(value?: string): string {
    if (!value) return '—';
    return value.replace('T', ' ').slice(0, 16);
  }
}
