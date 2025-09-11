import { Routes } from '@angular/router';
import { LoginComponent } from '@app/features/login/login.component';
import { LogoutComponent } from '@app/features/logout/logout.component';
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
    path: 'logout',
    component: LogoutComponent,
    data: { title: 'Logout' }
  }
];
