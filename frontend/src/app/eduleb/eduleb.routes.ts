import { Routes } from '@angular/router';
import { evaluationChildRoutes } from '../b2u-hub/evaluation/evaluation.routes';
import { missionChildRoutes } from '../b2u-hub/missions/mission.routes';

export const edulebRoutes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'home' },
  {
    path: 'home',
    loadComponent: () =>
      import('./pages/home/home.component').then((m) => m.HomePageComponent),
  },
  {
    path: 'home-2',
    loadComponent: () =>
      import('./pages/home-2/home-2.component').then((m) => m.Home2PageComponent),
  },
  {
    path: 'about',
    loadComponent: () =>
      import('./pages/about/about.component').then((m) => m.AboutPageComponent),
  },
  {
    path: 'contact',
    loadComponent: () =>
      import('./pages/contact/contact.component').then((m) => m.ContactPageComponent),
  },
  {
    path: 'course',
    loadComponent: () =>
      import('./pages/course/course.component').then((m) => m.CoursePageComponent),
  },
  {
    path: 'course-details',
    loadComponent: () =>
      import('./pages/course-details/course-details.component').then(
        (m) => m.CourseDetailsPageComponent,
      ),
  },
  {
    path: 'instructors',
    loadComponent: () =>
      import('./pages/instructors/instructors.component').then(
        (m) => m.InstructorsPageComponent,
      ),
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
    loadComponent: () =>
      import('./pages/faq/faq.component').then((m) => m.FaqPageComponent),
  },
  {
    path: 'blog',
    loadComponent: () =>
      import('./pages/blog/blog.component').then((m) => m.BlogPageComponent),
  },
  {
    path: 'blog-single',
    loadComponent: () =>
      import('./pages/blog-single/blog-single.component').then(
        (m) => m.BlogSinglePageComponent,
      ),
  },
  {
    path: 'thank-you',
    loadComponent: () =>
      import('./pages/thank-you/thank-you.component').then(
        (m) => m.ThankYouPageComponent,
      ),
  },
  {
    path: 'evaluation',
    loadComponent: () =>
      import('../b2u-hub/evaluation/evaluation-shell.component').then(
        (m) => m.EvaluationShellComponent,
      ),
    children: evaluationChildRoutes,
  },
  {
    path: 'missions',
    loadComponent: () =>
      import('../b2u-hub/missions/mission-shell.component').then(
        (m) => m.MissionShellComponent,
      ),
    children: missionChildRoutes,
  },
  {
    path: 'not-found',
    loadComponent: () =>
      import('./pages/not-found/not-found.component').then(
        (m) => m.NotFoundPageComponent,
      ),
  },
  {
    path: '**',
    loadComponent: () =>
      import('./pages/not-found/not-found.component').then(
        (m) => m.NotFoundPageComponent,
      ),
  },
];
