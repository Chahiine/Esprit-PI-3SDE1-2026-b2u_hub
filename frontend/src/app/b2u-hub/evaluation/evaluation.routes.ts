import { Routes } from '@angular/router';

export const evaluationChildRoutes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'entreprise' },
  {
    path: 'entreprise',
    loadComponent: () =>
      import('./pages/company-dashboard/company-dashboard.component').then(
        (m) => m.CompanyDashboardComponent,
      ),
  },
  {
    path: 'entreprise/nouvelle',
    loadComponent: () =>
      import('./pages/company-new-evaluation/company-new-evaluation.component').then(
        (m) => m.CompanyNewEvaluationComponent,
      ),
  },
  {
    path: 'etudiant',
    loadComponent: () =>
      import('./pages/student-dashboard/student-dashboard.component').then(
        (m) => m.StudentDashboardComponent,
      ),
  },
  {
    path: 'etudiant/noter-entreprise',
    loadComponent: () =>
      import('./pages/student-rate-company/student-rate-company.component').then(
        (m) => m.StudentRateCompanyComponent,
      ),
  },
];
