import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MissionService } from '../../../user/services/mission.service';
import { Mission } from '../../../user/models/mission.model';
import { AuthService } from '../../../user/services/auth.service';
import { AiMatchingService, MatchingResult } from '../../../user/services/ai-matching.service';

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

  matchingResult: MatchingResult | null = null;
  matchingLoading = false;
  matchingError = '';

  constructor(
    private route: ActivatedRoute,
    public missionService: MissionService,
    public auth: AuthService,
    private aiMatching: AiMatchingService,
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

  // ── Fix: lire skillsRequired directement depuis la mission ────
  get skillsList(): string[] {
    if (!this.mission) return [];
    const raw = this.mission.skillsRequired ?? '';
    return raw
      .split(',')
      .map((s) => s.trim())
      .filter(Boolean);
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

  analyzeCompatibility(): void {
    const user = this.auth.user();
    const m = this.mission;
    if (!user || !m) return;

    this.matchingLoading = true;
    this.matchingError = '';
    this.matchingResult = null;

    this.aiMatching
      .analyzeMatch(
        user.skills ?? '',
        user.bio ?? '',
        m.title,
        m.description,
        m.skillsRequired ?? '',
      )
      .subscribe({
        next: (result) => {
          this.matchingResult = result;
          this.matchingLoading = false;
        },
        error: () => {
          this.matchingError = 'Analyse échouée. Réessayez.';
          this.matchingLoading = false;
        },
      });
  }

  get scoreColor(): string {
    const s = this.matchingResult?.score ?? 0;
    if (s >= 75) return '#22c55e';
    if (s >= 50) return '#f59e0b';
    return '#ef4444';
  }
}
