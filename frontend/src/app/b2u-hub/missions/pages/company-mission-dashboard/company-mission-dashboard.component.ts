import { Component, inject, OnInit, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { filter } from 'rxjs/operators';
import { MissionApiService } from '../../mission-api.service';
import type { MissionDto, MissionStatus } from '../../models';
import { MISSION_STATUS_LABELS } from '../../models';

@Component({
  selector: 'app-company-mission-dashboard',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './company-mission-dashboard.component.html',
  styleUrl: './company-mission-dashboard.component.css',
})
export class CompanyMissionDashboardComponent implements OnInit {
  private readonly api = inject(MissionApiService);
  private readonly router = inject(Router);

  readonly missions = signal<MissionDto[]>([]);
  readonly loading = signal(true);
  readonly sourceApi = signal(false);
  readonly statusLabels = MISSION_STATUS_LABELS;

  ngOnInit() {
    this.loadMissions();
    this.router.events
      .pipe(filter((e) => e instanceof NavigationEnd))
      .subscribe(() => {
        if (this.router.url === '/missions/entreprise') {
          this.loadMissions();
        }
      });
  }

  loadMissions() {
    this.loading.set(true);
    this.api.listMissions().subscribe({
      next: (list) => {
        this.missions.set(list);
        this.sourceApi.set(true);
        this.loading.set(false);
      },
      error: () => {
        this.missions.set([]);
        this.sourceApi.set(false);
        this.loading.set(false);
      },
    });
  }

  statutLabel(status: MissionStatus): string {
    return this.statusLabels[status] ?? status;
  }

  countOpen(): number {
    return this.missions().filter((m) => m.status === 'OPEN').length;
  }
}
