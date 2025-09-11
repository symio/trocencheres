// src/app/features/login/login.component.ts
import { Component, OnInit } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { OAuth2Service } from '@core/services/oauth2.service';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [
        CommonModule,        // ✅ pour *ngIf, *ngFor, etc.
        ReactiveFormsModule, // ✅ pour formGroup, formControlName
        RouterModule         // ✅ pour routerLink
    ],
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss'],
})
export class LoginComponent implements OnInit {
    loginForm!: FormGroup;
    loading = false;
    error = '';
    returnUrl = '';

    constructor(
        private formBuilder: FormBuilder,
        private oauth2Service: OAuth2Service,
        private router: Router,
        private route: ActivatedRoute
    ) { }

    ngOnInit(): void {
        this.loginForm = this.formBuilder.group({
            clientId: ['', [Validators.required]],
            clientSecret: ['', [Validators.required]]
        });

        // Obtenir l'URL de retour
        this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/dashboard';

        // Rediriger si déjà connecté
        if (this.oauth2Service.isAuthenticated()) {
            this.router.navigate([this.returnUrl]);
        }
    }

    onSubmit(): void {
        if (this.loginForm.invalid) {
            return;
        }

        this.loading = true;
        this.error = '';

        const credentials = {
            clientId: this.loginForm.value.clientId,
            clientSecret: this.loginForm.value.clientSecret
        };

        this.oauth2Service.login(credentials).subscribe({
            next: (response) => {
                console.log('Connexion réussie:', response);
                this.router.navigate([this.returnUrl]);
            },
            error: (error) => {
                console.error('Erreur de connexion:', error);
                this.error = 'Identifiants invalides. Veuillez réessayer.';
                this.loading = false;
            },
            complete: () => {
                this.loading = false;
            }
        });
    }

    // Getters pour faciliter l'accès aux contrôles du formulaire
    get clientId() { return this.loginForm.get('clientId'); }
    get clientSecret() { return this.loginForm.get('clientSecret'); }
}
