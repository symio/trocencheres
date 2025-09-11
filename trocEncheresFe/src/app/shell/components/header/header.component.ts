// src/app/shell/components/header/header.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router'; // ⭐ AJOUT : Import nécessaire pour routerLink
import { LanguageSelectorComponent } from '@app/i18n';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { OAuth2Service } from '@app/@core/services/oauth2.service';
import { Observable } from 'rxjs';

@UntilDestroy()
@Component({
    selector: 'app-header',
    standalone: true,
    imports: [
        LanguageSelectorComponent, 
        CommonModule,
        RouterModule
    ],
    templateUrl: './header.component.html',
    styleUrls: ['./header.component.scss']
})
export class HeaderComponent {
    menuHidden = true;
    isAuthenticated$ : Observable<boolean>; // ⭐ CORRECTION : Typage correct

    constructor(private oauth2Service: OAuth2Service) {
        this.isAuthenticated$ = this.oauth2Service.isAuthenticated$;
    }
    onLogout(): void {
        this.menuHidden = true;
    }
}