// src/app/shell/components/header/header.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LanguageSelectorComponent } from '@app/i18n';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { OAuth2Service } from '@app/@core/services/oauth2.service';

@UntilDestroy()
@Component({
    selector: 'app-header',
    standalone: true,
    imports: [LanguageSelectorComponent, CommonModule],
    templateUrl: './header.component.html',
    styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {
    menuHidden = true;
    isAuthenticated = false;

    constructor(private oauth2Service: OAuth2Service) {
        this.isAuthenticated$ = this.oauth2Service.isAuthenticated$;
    }
}
