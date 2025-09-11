// app.routes.ts 
import { Routes } from '@angular/router';
import { authRoutes } from './auth/auth.routing';
import { ShellComponent } from './shell/shell.component';
import { UnauthorizedComponent } from './features/unauthorized/unauthorized.component';
import { AuthGuard } from '@core/guards/auth.guard';

export const routes: Routes = [
  // Routes d'authentification (publiques)
  { 
    path: 'auth', 
    children: authRoutes 
  },
  
  // Page non autorisée
  { 
    path: 'unauthorized', 
    component: UnauthorizedComponent 
  },
  
  {
    path: '',
    component: ShellComponent, // Utiliser ShellComponent ici
    canActivate: [AuthGuard], // Protection par authentification
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./pages/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'users',
        loadChildren: () => import('./pages/users/users.module').then(m => m.UsersModule)
      },
      // Ajouter d'autres routes protégées ici...
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      }
    ]
  },
  
  // Redirection par défaut
  { 
    path: '', 
    redirectTo: '/auth/login', 
    pathMatch: 'full' 
  },
  { 
    path: '**', 
    redirectTo: '/auth/login', 
    pathMatch: 'full' 
  }
];
