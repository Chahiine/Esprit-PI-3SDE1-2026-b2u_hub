import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MissionService } from '../../../user/services/mission.service';
import { Mission } from '../../../user/models/mission.model';
import { AuthService } from '../../../user/services/auth.service';

@Component({
  selector: 'app-course',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './course.component.html',
})
export class CoursePageComponent implements OnInit {
  missions: Mission[] = [];
  filtered: Mission[] = [];
  loading = true;
  search = '';
  activeFilter = 'ALL';

  readonly categories = ['ALL', 'Frontend', 'Backend', 'Mobile', 'Design', 'DevOps', 'Data'];
  readonly levels = ['BEGINNER', 'INTERMEDIATE', 'EXPERT'];

  constructor(
    public missionService: MissionService,
    public auth: AuthService,
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.missionService.getOpen().subscribe({
      next: (data) => {
        this.missions = data;
        this.applyFilter();
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  applyFilter(): void {
    let result = this.missions;
    if (this.activeFilter !== 'ALL') {
      result = result.filter((m) => m.category === this.activeFilter);
    }
    if (this.search.trim()) {
      const q = this.search.toLowerCase();
      result = result.filter(
        (m) =>
          m.title.toLowerCase().includes(q) ||
          m.enterpriseName.toLowerCase().includes(q) ||
          m.skillsRequired?.toLowerCase().includes(q),
      );
    }
    this.filtered = result;
  }

  setFilter(cat: string): void {
    this.activeFilter = cat;
    this.applyFilter();
  }

  onSearch(): void {
    this.applyFilter();
  }

  get isCompanyOrAdmin(): boolean {
    return this.auth.isAdmin() || this.auth.isCompany();
  }

  levelLabel(level: string): string {
    const map: Record<string, string> = {
      BEGINNER: 'Débutant',
      INTERMEDIATE: 'Intermédiaire',
      EXPERT: 'Expert',
    };
    return map[level] ?? level;
  }

  statusClass(status: string): string {
    return (
      { OPEN: 'badge-open', IN_PROGRESS: 'badge-progress', CLOSED: 'badge-closed' }[status] ?? ''
    );
  }

  statusLabel(status: string): string {
    return { OPEN: 'Ouverte', IN_PROGRESS: 'En cours', CLOSED: 'Fermée' }[status] ?? status;
  }

  imageFor(category: string): string {
    return '/eduleb/assets/img/course/' + this.missionService.categoryImage(category) + '.png';
  }

  deleteMission(id: number): void {
    if (!confirm('Supprimer cette mission ?')) return;
    this.missionService.delete(id).subscribe({
      next: () => {
        this.missions = this.missions.filter((m) => m.id !== id);
        this.applyFilter();
      },
    });
  }
}
