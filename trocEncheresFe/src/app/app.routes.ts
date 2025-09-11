import { Routes } from '@angular/router';
import { authRoutes } from './auth/auth.routing';
import { ShellComponent } from './shell/shell.component';
import { UnauthorizedComponent } from './features/unauthorized/unauthorized.component';

export const routes: Routes = [
  { path: 'auth', children: authRoutes },
  { path: 'unauthorized', component: UnauthorizedComponent },
  { path: '', redirectTo: '/auth/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/auth/login', pathMatch: 'full' }
];
