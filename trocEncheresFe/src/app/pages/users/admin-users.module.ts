// src/app/pages/users/admin-users.module.ts
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { AdminUsersRoutingModule } from './admin-users-routing.module';
@NgModule({
    declarations: [],
    imports: [
        CommonModule,
        ReactiveFormsModule,
        FormsModule,
        RouterModule,
        AdminUsersRoutingModule
    ],
    providers: [
    ]
})
export class AdminUsersModule { }