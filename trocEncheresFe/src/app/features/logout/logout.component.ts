// src/app/features/logout/logout.component.ts
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { OAuth2Service } from '@app/@core/services/oauth2.service';
import { CredentialsService } from '@auth';

@Component({
  selector: 'app-logout',
  templateUrl: './logout.component.html',
  styleUrls: ['./logout.component.scss'],
  standalone: true,
})
export class LogoutComponent implements OnInit {
  constructor(
    private readonly _authService: OAuth2Service,
    private readonly _router: Router,
    private readonly _credentialsService: CredentialsService,
  ) {}

   ngOnInit() {
    this._authService.logout().subscribe({
      next: () => {
        this._credentialsService.setCredentials();
        this._router.navigate(['/auth/login']).then(() => {
          window.location.reload();
        });
      },
      error: (err) => {
        console.error('Erreur lors de la déconnexion :', err);
      }
    });
  }
}
