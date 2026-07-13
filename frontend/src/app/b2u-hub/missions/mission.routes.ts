import { Routes } from '@angular/router';

export const missionChildRoutes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'consulter' },
  {
    path: 'consulter',
    loadComponent: () =>
      import('./pages/mission-list/mission-list.component').then(
        (m) => m.MissionListComponent,
      ),
  },
  {
    path: 'etudiant/matching',
    loadComponent: () =>
      import('./pages/mission-ai-match/mission-ai-match.component').then(
        (m) => m.MissionAiMatchComponent,
      ),
  },
  {
    path: 'entreprise',
    loadComponent: () =>
      import('./pages/company-mission-dashboard/company-mission-dashboard.component').then(
        (m) => m.CompanyMissionDashboardComponent,
      ),
  },
  {
    path: 'entreprise/nouvelle',
    loadComponent: () =>
      import('./pages/mission-create/mission-create.component').then(
        (m) => m.MissionCreateComponent,
      ),
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./pages/mission-detail/mission-detail.component').then(
        (m) => m.MissionDetailComponent,
      ),
  },
];
