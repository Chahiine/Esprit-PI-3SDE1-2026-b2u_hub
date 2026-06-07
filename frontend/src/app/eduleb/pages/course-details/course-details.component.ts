import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MissionService } from '../../../user/services/mission.service';
import { Mission } from '../../../user/models/mission.model';
import { AuthService } from '../../../user/services/auth.service';

@Component({
  selector: 'app-course-details',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './course-details.component.html',
})
export class CourseDetailsPageComponent implements OnInit {
  mission: Mission | null = null;
  loading = true;
  activeTab = 'overview';

  constructor(
    private route: ActivatedRoute,
    public missionService: MissionService,
    public auth: AuthService,
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.missionService.getById(id).subscribe({
        next: (m) => {
          this.mission = m;
          this.loading = false;
        },
        error: () => {
          this.loading = false;
        },
      });
    }
  }

  setTab(tab: string): void {
    this.activeTab = tab;
  }

  get skillsList(): string[] {
    return this.mission ? this.missionService.skillsList(this.mission) : [];
  }

  levelLabel(level: string): string {
    const map: Record<string, string> = {
      BEGINNER: 'Débutant',
      INTERMEDIATE: 'Intermédiaire',
      EXPERT: 'Expert',
    };
    return map[level] ?? level;
  }

  statusLabel(status: string): string {
    return { OPEN: 'Ouverte', IN_PROGRESS: 'En cours', CLOSED: 'Fermée' }[status] ?? status;
  }

  imageFor(category: string): string {
    return '/eduleb/assets/img/course/' + this.missionService.categoryImage(category) + '.png';
  }

  get isLoggedIn(): boolean {
    return this.auth.isLoggedIn();
  }
  get isCompanyOrAdmin(): boolean {
    return this.auth.isAdmin() || this.auth.isCompany();
  }
}
