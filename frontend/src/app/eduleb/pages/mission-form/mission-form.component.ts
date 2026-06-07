import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MissionService } from '../../../user/services/mission.service';
import { AuthService } from '../../../user/services/auth.service';
import { MissionDto } from '../../../user/models/mission.model';
@Component({
  selector: 'app-mission-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './mission-form.component.html',
})
export class MissionFormComponent implements OnInit {

  isEdit   = false;
  missionId: number | null = null;
  loading  = false;
  saving   = false;
  error    = '';
  success  = '';

  readonly categories = ['Frontend', 'Backend', 'Mobile', 'Design', 'DevOps', 'Data'];
  readonly levels     = ['BEGINNER', 'INTERMEDIATE', 'EXPERT'];

  form: MissionDto = {
    title:           '',
    description:     '',
    enterpriseName:  '',
    skillsRequired:  '',
    level:           'INTERMEDIATE',
    budget:          undefined,
    deadline:        '',
    category:        'Backend',
    status:          'OPEN',
    createdByUserId: undefined,
  };

  constructor(
    private route:          ActivatedRoute,
    private router:         Router,
    private missionService: MissionService,
    private auth:           AuthService,
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    const user = this.auth.user();
    if (user) this.form.createdByUserId = user.userId;

    if (id) {
      this.isEdit    = true;
      this.missionId = Number(id);
      this.loading   = true;
      this.missionService.getById(this.missionId).subscribe({
        next: (m) => {
          this.form = {
            title:           m.title,
            description:     m.description,
            enterpriseName:  m.enterpriseName,
            skillsRequired:  m.skillsRequired,
            level:           m.level,
            budget:          m.budget,
            deadline:        m.deadline ?? '',
            category:        m.category,
            status:          m.status,
            createdByUserId: m.createdByUserId,
          };
          this.loading = false;
        },
        error: () => { this.loading = false; this.error = 'Mission introuvable.'; }
      });
    }
  }

  submit(): void {
    if (!this.form.title || !this.form.description || !this.form.enterpriseName) {
      this.error = 'Titre, description et entreprise sont obligatoires.';
      return;
    }

    this.saving = true;
    this.error  = '';

    const obs = this.isEdit && this.missionId
      ? this.missionService.update(this.missionId, this.form)
      : this.missionService.create(this.form);

    obs.subscribe({
      next: (m) => {
        this.saving  = false;
        this.success = this.isEdit ? 'Mission modifiée !' : 'Mission créée !';
        setTimeout(() => this.router.navigate(['/course-details', m.id]), 1000);
      },
      error: (err) => {
        this.saving = false;
        this.error  = typeof err.error === 'string' ? err.error : 'Erreur lors de l\'enregistrement.';
      }
    });
  }
}
