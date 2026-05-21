import { Component, inject, OnInit, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { filter } from 'rxjs/operators';
import {
  EvaluationApiService,
  EvaluationDto,
} from '../../evaluation-api.service';
import { EvaluationMockService } from '../../evaluation-mock.service';

@Component({
  selector: 'app-company-dashboard',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './company-dashboard.component.html',
})
export class CompanyDashboardComponent implements OnInit {
  private readonly api = inject(EvaluationApiService);
  private readonly mock = inject(EvaluationMockService);
  private readonly router = inject(Router);

  readonly evaluations = signal<EvaluationDto[]>([]);
  readonly sourceApi = signal(false);
  readonly loading = signal(true);
  readonly emailStatus = signal<string | null>(null);

  ngOnInit() {
    this.readEmailBanner();
    this.loadEvaluations();

    this.router.events
      .pipe(filter((e) => e instanceof NavigationEnd))
      .subscribe(() => {
        if (this.router.url === '/evaluation/entreprise') {
          this.readEmailBanner();
          this.loadEvaluations();
        }
      });
  }

  private readEmailBanner() {
    const status = sessionStorage.getItem('b2u_last_email_status');
    sessionStorage.removeItem('b2u_last_email_status');
    if (status && status !== 'undefined' && status !== 'null') {
      this.emailStatus.set(status);
    }
  }

  loadEvaluations() {
    this.loading.set(true);
    this.api.listEvaluations().subscribe({
      next: (list) => {
        this.evaluations.set(list);
        this.sourceApi.set(true);
        this.loading.set(false);
      },
      error: () => {
        const local = this.mock.evaluationsEntreprise().map((ev) => ({
          id: Number(ev.id.replace(/\D/g, '')) || undefined,
          studentName: ev.etudiantNom,
          studentEmail: '—',
          enterpriseName: '—',
          projectTitle: ev.projetTitre,
          rating: ev.noteGlobale,
          comment: ev.commentaire,
          projectDate: ev.date,
        }));
        this.evaluations.set(local);
        this.sourceApi.set(false);
        this.loading.set(false);
      },
    });
  }

  moyenneNotes(): string {
    const list = this.evaluations();
    if (!list.length) return '';
    const sum = list.reduce((a, e) => a + (e.rating ?? 0), 0);
    return (sum / list.length).toFixed(1);
  }
}
