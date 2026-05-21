import { Routes } from '@angular/router';
import { EdulebLayoutComponent } from './eduleb/layout/eduleb-layout.component';
import { edulebRoutes } from './eduleb/eduleb.routes';

export const routes: Routes = [
  {
    path: '',
    component: EdulebLayoutComponent,
    children: edulebRoutes,
  },
];
