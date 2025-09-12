// src/app/pages/users/admin-user-details/admin-user-details.component.ts
import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { UserService } from '@app/@core/services/user.service';
import { OAuth2Service } from '@app/@core/services/oauth2.service';

@Component({
    selector: 'app-admin-user-details',
    templateUrl: './admin-user-details.component.html',
    styleUrls: ['./admin-user-details.component.scss'],
    standalone: true,
    imports: [CommonModule]
})
export class AdminUserDetailsComponent implements OnInit, OnDestroy {
    private destroy$ = new Subject<void>();

    user: any = null;
    userPseudo: string | null = null;
    isLoading = false;
    errorMessage = '';
    successMessage = '';

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private userService: UserService,
        private oauth2Service: OAuth2Service
    ) { }

    ngOnInit(): void {
        // Vérifier que l'utilisateur est admin
        if (!this.oauth2Service.isAdmin()) {
            this.router.navigate(['/']);
            return;
        }

        this.userPseudo = this.route.snapshot.paramMap.get('pseudo');
        if (this.userPseudo) {
            this.loadUserDetails();
        } else {
            this.errorMessage = 'Pseudo utilisateur manquant';
        }
    }

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    private loadUserDetails(): void {
        if (!this.userPseudo) return;

        this.isLoading = true;
        this.errorMessage = '';

        this.userService.getUserWithDetails(this.userPseudo)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
                next: (user) => {
                    this.user = user;
                    this.isLoading = false;
                },
                error: (error) => {
                    this.errorMessage = 'Erreur lors du chargement des détails de l\'utilisateur';
                    this.isLoading = false;
                    console.error('Erreur:', error);
                }
            });
    }

    goBack(): void {
        this.router.navigate(['/users']);
    }

    editUser(): void {
        if (this.userPseudo) {
            this.router.navigate(['/users/edit', this.userPseudo]);
        }
    }

    deleteUser(): void {
        if (!this.userPseudo || !this.user) return;

        const confirmMessage = `Êtes-vous sûr de vouloir supprimer l'utilisateur "${this.user.pseudo}" ?`;

        if (confirm(confirmMessage)) {
            this.isLoading = true;

            this.userService.deleteUser(this.userPseudo)
                .pipe(takeUntil(this.destroy$))
                .subscribe({
                    next: () => {
                        this.successMessage = `Utilisateur "${this.user.pseudo}" supprimé avec succès`;
                        setTimeout(() => {
                            this.router.navigate(['/users']);
                        }, 2000);
                    },
                    error: (error) => {
                        this.errorMessage = error.error?.message || 'Erreur lors de la suppression';
                        this.isLoading = false;
                        console.error('Erreur suppression:', error);
                    }
                });
        }
    }

    // Méthodes utilitaires
    getRoleDisplayName(role: string): string {
        const roleMap: { [key: string]: string } = {
            'ROLE_USER': 'Utilisateur Standard',
            'ROLE_ADMIN': 'Administrateur'
        };
        return roleMap[role] || role;
    }

    getRoleIcon(role: string): string {
        const roleIcons: { [key: string]: string } = {
            'ROLE_USER': '👤',
            'ROLE_ADMIN': '',
        };
        return roleIcons[role] || '👤';
    }

    getRoleBadgeClass(role: string): string {
        switch (role) {
            case 'ROLE_ADMIN':
                return 'badge-admin';
            default:
                return 'badge-user';
        }
    }

    formatDate(dateString: string): string {
        if (!dateString) return 'Non défini';
        try {
            return new Date(dateString).toLocaleString('fr-FR');
        } catch {
            return 'Format invalide';
        }
    }

    getFullAddress(): string {
        if (!this.user?.adresse) return 'Non définie';

        const { rue, codePostal, ville } = this.user.adresse;
        const parts = [rue, codePostal, ville].filter(part => part && part.trim());

        return parts.length > 0 ? parts.join(', ') : 'Non définie';
    }

    trackByPseudo(index: number, item: any): any {
        return item.pseudo;
    }
}
