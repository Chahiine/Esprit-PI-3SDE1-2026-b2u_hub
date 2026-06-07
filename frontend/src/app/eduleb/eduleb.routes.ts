import { Routes } from '@angular/router';
import { evaluationChildRoutes } from '../b2u-hub/evaluation/evaluation.routes';
import { authGuard, adminGuard, guestGuard } from '../user/guards/auth.guard';

export const edulebRoutes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'home' },

  // ── Pages publiques eduleb ───────────────────────────────────
  {
    path: 'home',
    loadComponent: () => import('./pages/home/home.component').then((m) => m.HomePageComponent),
  },
  {
    path: 'home-2',
    loadComponent: () =>
      import('./pages/home-2/home-2.component').then((m) => m.Home2PageComponent),
  },
  {
    path: 'about',
    loadComponent: () => import('./pages/about/about.component').then((m) => m.AboutPageComponent),
  },
  {
    path: 'contact',
    loadComponent: () =>
      import('./pages/contact/contact.component').then((m) => m.ContactPageComponent),
  },
  {
    path: 'instructors',
    loadComponent: () =>
      import('./pages/instructors/instructors.component').then((m) => m.InstructorsPageComponent),
  },
  {
    path: 'instructor-details',
    loadComponent: () =>
      import('./pages/instructor-details/instructor-details.component').then(
        (m) => m.InstructorDetailsPageComponent,
      ),
  },
  {
    path: 'pricing',
    loadComponent: () =>
      import('./pages/pricing/pricing.component').then((m) => m.PricingPageComponent),
  },
  {
    path: 'faq',
    loadComponent: () => import('./pages/faq/faq.component').then((m) => m.FaqPageComponent),
  },
  {
    path: 'blog',
    loadComponent: () => import('./pages/blog/blog.component').then((m) => m.BlogPageComponent),
  },
  {
    path: 'blog-single',
    loadComponent: () =>
      import('./pages/blog-single/blog-single.component').then((m) => m.BlogSinglePageComponent),
  },
  {
    path: 'thank-you',
    loadComponent: () =>
      import('./pages/thank-you/thank-you.component').then((m) => m.ThankYouPageComponent),
  },

  // ── MISSIONS — remplace course ────────────────────────────────
  {
    path: 'course',
    loadComponent: () =>
      import('./pages/course/course.component').then((m) => m.CoursePageComponent),
  },
  {
    path: 'course-details/:id',
    loadComponent: () =>
      import('././pages/course-details/course-details.component').then(
        (m) => m.CourseDetailsPageComponent,
      ),
  },
  {
    path: 'course-details', // fallback sans id
    loadComponent: () =>
      import('./pages/course-details/course-details.component').then(
        (m) => m.CourseDetailsPageComponent,
      ),
  },
  {
    path: 'mission-form',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/mission-form/mission-form.component').then((m) => m.MissionFormComponent),
  },
  {
    path: 'mission-form/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/mission-form/mission-form.component').then((m) => m.MissionFormComponent),
  },

  // ── Auth ──────────────────────────────────────────────────────
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('../user/pages/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('../user/pages/register/register.component').then((m) => m.RegisterComponent),
  },
  {
    path: 'oauth-success',
    loadComponent: () =>
      import('../user/pages/oauth-success/oauth-success.component').then(
        (m) => m.OauthSuccessComponent,
      ),
  },

  // ── Profil & Admin ────────────────────────────────────────────
  {
    path: 'profile',
    canActivate: [authGuard],
    loadComponent: () =>
      import('../user/pages/profile/profile.component').then((m) => m.ProfileComponent),
  },
  {
    path: 'admin/users',
    canActivate: [authGuard, adminGuard],
    loadComponent: () =>
      import('../user/pages/admin-users/admin-users.component').then((m) => m.AdminUsersComponent),
  },

  // ── Évaluations (collègue) ────────────────────────────────────
  {
    path: 'evaluation',
    loadComponent: () =>
      import('../b2u-hub/evaluation/evaluation-shell.component').then(
        (m) => m.EvaluationShellComponent,
      ),
    children: evaluationChildRoutes,
  },

  // ── 404 ───────────────────────────────────────────────────────
  {
    path: 'not-found',
    loadComponent: () =>
      import('./pages/not-found/not-found.component').then((m) => m.NotFoundPageComponent),
  },
  {
    path: '**',
    loadComponent: () =>
      import('./pages/not-found/not-found.component').then((m) => m.NotFoundPageComponent),
  },
];
