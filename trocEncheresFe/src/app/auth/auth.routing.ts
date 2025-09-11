// src/app/auth/auth.routing.ts
import { Routes } from '@angular/router';
import { LoginComponent } from '@app/features/login/login.component';
import { LogoutComponent } from '@app/features/logout/logout.component';
import { RegisterComponent } from '@app/features/register/register.component'; // ⭐ AJOUT
import { AlreadyLoggedCheckGuard } from '@app/auth/guard/authentication.guard';

export const authRoutes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  {
    path: 'login',
    canActivate: [AlreadyLoggedCheckGuard],
    component: LoginComponent,
    data: { title: 'Login' }
  },
  {
    path: 'register', 
    canActivate: [AlreadyLoggedCheckGuard],
    component: RegisterComponent,
    data: { title: 'Register' }
  },
  {
    path: 'logout',
    component: LogoutComponent,
    data: { title: 'Logout' }
  }
];