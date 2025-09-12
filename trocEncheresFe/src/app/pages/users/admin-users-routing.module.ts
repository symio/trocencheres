import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AdminUsersListComponent } from './admin-users-list/admin-users-list.component';
import { AdminUserDetailsComponent } from './admin-user-details/admin-user-details.component';
import { AdminAddComponent } from './admin-add-user/admin-add-user.component';

const routes: Routes = [
    {
        path: '',
        component: AdminUsersListComponent,
        data: {
            title: 'Gestion des utilisateurs',
            breadcrumb: 'Utilisateurs'
        }
    },
    {
        path: 'create',
        component: AdminAddComponent,
        data: {
            title: 'Créer un utilisateur',
            breadcrumb: 'Créer'
        }
    },
    {
        path: 'details/:pseudo',
        component: AdminUserDetailsComponent,
        data: {
            title: 'Détails utilisateur',
            breadcrumb: 'Détails'
        }
    },
    {
        path: 'edit/:pseudo',
        component: AdminAddComponent,
        data: {
            title: 'Modifier utilisateur',
            breadcrumb: 'Modifier'
        }
    },
    {
        path: '**',
        redirectTo: ''
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class AdminUsersRoutingModule { }
