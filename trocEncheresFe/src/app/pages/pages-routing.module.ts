// src/app/pages/pages-routing.module.ts"
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AdminGuard } from '@app/@core/guards/admin.guard';
import { Shell } from '@app/shell/services/shell.service';
import { DashboardComponent } from '@pages/dashboard/dashboard.component';

const routes: Routes = [
  Shell.childRoutes([
    {
      path: 'dashboard',
      component: DashboardComponent,
    },
    {
        path: 'users',
        loadChildren: () => import('../pages/users/admin-users.module').then(m => m.AdminUsersModule),
        canActivate: [AdminGuard], 
      },

    // Fallback when no prior route is matched
    { path: '**', redirectTo: '', pathMatch: 'full' },
  ]),
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class PagesRoutingModule {}
