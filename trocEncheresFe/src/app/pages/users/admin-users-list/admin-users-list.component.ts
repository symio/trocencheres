// src/app/pages/users/admin-users-list/admin-users-list.component.ts
import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil, debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '@app/@core/services/user.service';
import { OAuth2Service } from '@app/@core/services/oauth2.service';

@Component({
    selector: 'app-admin-users-list',
    templateUrl: './admin-users-list.component.html',
    styleUrls: ['./admin-users-list.component.scss'],
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        FormsModule
    ]
})
export class AdminUsersListComponent implements OnInit, OnDestroy {
    private destroy$ = new Subject<void>();

    users: any[] = [];
    filteredUsers: any[] = [];
    isLoading = false;
    errorMessage = '';
    successMessage = '';

    // Pagination
    currentPage = 0;
    pageSize = 10;
    totalElements = 0;
    totalPages = 0;
    pageSizes = [5, 10, 20, 50];

    // Recherche et filtres
    searchControl = new FormControl('');
    searchType = 'all'; // all, pseudo, email, nom
    selectedRole = '';
    availableRoles: any[] = [];

    // Tri
    sortField = 'pseudo';
    sortDirection: 'asc' | 'desc' = 'asc';

    // Exposer Math pour le template
    Math = Math;

    constructor(
        private userService: UserService,
        private oauth2Service: OAuth2Service,
        private router: Router
    ) {
        console.log('✅ AdminUsersListComponent instancié');
    }

    ngOnInit(): void {
        // Vérifier que l'utilisateur est admin
        if (!this.oauth2Service.isAdmin()) {
            this.router.navigate(['/']);
            return;
        }

        this.initSearchListener();
        this.loadRoles();
        this.loadUsers();
    }

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    private initSearchListener(): void {
        this.searchControl.valueChanges
            .pipe(
                takeUntil(this.destroy$),
                debounceTime(300),
                distinctUntilChanged()
            )
            .subscribe(() => {
                this.applyFilters();
            });
    }

    private loadRoles(): void {
        this.userService.getAllRoles()
            .pipe(takeUntil(this.destroy$))
            .subscribe({
                next: (roles) => {
                    this.availableRoles = roles;
                },
                error: (error) => {
                    console.error('Erreur lors du chargement des rôles:', error);
                }
            });
    }

    loadUsers(): void {
        this.isLoading = true;
        this.errorMessage = '';

        this.userService.getAllUsers(this.currentPage, this.pageSize)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
                next: (response) => {
                    this.users = response._embedded?.['utilisateurs'] || [];
                    this.totalElements = response.page?.totalElements || 0;
                    this.totalPages = response.page?.totalPages || 0;
                    this.applyFilters();
                    this.isLoading = false;
                },
                error: (error) => {
                    this.errorMessage = 'Erreur lors du chargement des utilisateurs';
                    this.isLoading = false;
                    console.error('Erreur:', error);
                }
            });
    }

    applyFilters(): void {
        let filtered = [...this.users];

        // Recherche textuelle
        const searchTerm = this.searchControl.value?.toLowerCase() || '';
        if (searchTerm) {
            filtered = filtered.filter(user => {
                switch (this.searchType) {
                    case 'pseudo':
                        return user.pseudo?.toLowerCase().includes(searchTerm);
                    case 'email':
                        return user.email?.toLowerCase().includes(searchTerm);
                    case 'nom':
                        return user.nom?.toLowerCase().includes(searchTerm) ||
                            user.prenom?.toLowerCase().includes(searchTerm);
                    default:
                        return user.pseudo?.toLowerCase().includes(searchTerm) ||
                            user.email?.toLowerCase().includes(searchTerm) ||
                            user.nom?.toLowerCase().includes(searchTerm) ||
                            user.prenom?.toLowerCase().includes(searchTerm);
                }
            });
        }

        // Filtre par rôle
        if (this.selectedRole) {
            filtered = filtered.filter(user => user.role?.role === this.selectedRole);
        }

        // Tri
        filtered.sort((a, b) => {
            const aValue = this.getFieldValue(a, this.sortField);
            const bValue = this.getFieldValue(b, this.sortField);

            const comparison = aValue.localeCompare(bValue);
            return this.sortDirection === 'asc' ? comparison : -comparison;
        });

        this.filteredUsers = filtered;
    }

    private getFieldValue(user: any, field: string): string {
        switch (field) {
            case 'fullName':
                return `${user.nom || ''} ${user.prenom || ''}`.trim();
            case 'role':
                return user.role?.role || '';
            default:
                return user[field]?.toString() || '';
        }
    }

    onSort(field: string): void {
        if (this.sortField === field) {
            this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
        } else {
            this.sortField = field;
            this.sortDirection = 'asc';
        }
        this.applyFilters();
    }

    onPageChange(page: number): void {
        if (page >= 0 && page < this.totalPages) {
            this.currentPage = page;
            this.loadUsers();
        }
    }

    onPageSizeChange(): void {
        this.currentPage = 0;
        this.loadUsers();
    }

    onSearchTypeChange(): void {
        this.applyFilters();
    }

    onRoleFilterChange(): void {
        this.applyFilters();
    }

    clearFilters(): void {
        this.searchControl.setValue('');
        this.searchType = 'all';
        this.selectedRole = '';
        this.applyFilters();
    }

    // Actions CRUD
    createUser(): void {
        console.log('Navigation vers /users/create');
        this.router.navigate(['/users/create']).then(success => {
            if (success) {
                console.log('Navigation réussie');
            } else {
                console.error('Échec de navigation');
            }
        }).catch(err => {
            console.error('Erreur de navigation:', err);
        });
    }

    editUser(pseudo: string): void {
        this.router.navigate(['/users/edit', pseudo]);
    }

    deleteUser(user: any): void {
        const confirmMessage = `Êtes-vous sûr de vouloir supprimer l'utilisateur "${user.pseudo}" ?`;

        if (confirm(confirmMessage)) {
            this.isLoading = true;

            this.userService.deleteUser(user.pseudo)
                .pipe(takeUntil(this.destroy$))
                .subscribe({
                    next: () => {
                        this.successMessage = `Utilisateur "${user.pseudo}" supprimé avec succès`;
                        this.loadUsers();
                        setTimeout(() => this.successMessage = '', 3000);
                    },
                    error: (error) => {
                        this.errorMessage = error.error?.message || 'Erreur lors de la suppression';
                        this.isLoading = false;
                        console.error('Erreur suppression:', error);
                    }
                });
        }
    }

    viewUserDetails(pseudo: string): void {
        this.router.navigate(['/users/details', pseudo]);
    }

    // Méthodes utilitaires
    getRoleDisplayName(role: string): string {
        const roleMap: { [key: string]: string } = {
            'ROLE_USER': '👤 Utilisateur',
            'ROLE_ADMIN': '⚡ Administrateur'
        };
        return roleMap[role] || role;
    }

    getRoleBadgeClass(role: string): string {
        switch (role) {
            case 'ROLE_ADMIN':
                return 'badge-admin';
            default:
                return 'badge-user';
        }
    }

    getPageNumbers(): number[] {
        const pages = [];
        const startPage = Math.max(0, this.currentPage - 2);
        const endPage = Math.min(this.totalPages - 1, this.currentPage + 2);

        for (let i = startPage; i <= endPage; i++) {
            pages.push(i);
        }

        return pages;
    }

    formatDate(dateString: string): string {
        if (!dateString) return '-';
        try {
            return new Date(dateString).toLocaleDateString('fr-FR');
        } catch {
            return '-';
        }
    }

    trackByPseudo(index: number, user: any): string {
        return user.pseudo || index;
    }
}
