// src/app/pages/users/admin-add-user/admin-add-user.component.ts
import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { UserService } from '@app/@core/services/user.service';
import { RegisterComponent, PrefilledData } from '@app/features/register/register.component';
import { forkJoin } from 'rxjs';

@Component({
    selector: 'admin-add-user',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterModule, RegisterComponent],
    templateUrl: './admin-add-user.component.html',
    styleUrls: ['./admin-add-user.component.scss']
})
export class AdminAddComponent implements OnInit {
    @ViewChild(RegisterComponent) registerComponent!: RegisterComponent;

    adminForm!: FormGroup;
    isEditMode = false;
    isLoading = false;
    showPreview = false;
    errorMessage = '';
    successMessage = '';
    availableRoles: any[] = [];
    prefilledData: PrefilledData = {};

    private registerForm: FormGroup | null = null;
    private userPseudo: string | null = null;
    private currentUser: any = null;

    constructor(
        private fb: FormBuilder,
        private router: Router,
        private route: ActivatedRoute,
        private userService: UserService
    ) {
        this.initAdminForm();
    }

    ngOnInit(): void {
        this.userPseudo = this.route.snapshot.paramMap.get('pseudo');
        this.isEditMode = !!this.userPseudo;

        // Charger les rôles disponibles
        this.loadRoles();

        if (this.isEditMode) {
            this.loadUser();
        }
    }

    private initAdminForm(): void {
        this.adminForm = this.fb.group({
            selectedRole: ['ROLE_USER', Validators.required],
            initialCredit: [10, [Validators.min(0), Validators.max(1000)]]
        });
    }

    private loadRoles(): void {
        this.userService.getAllRoles().subscribe({
            next: (roles) => {
                this.availableRoles = roles;
            },
            error: (error) => {
                console.error('Erreur lors du chargement des rôles:', error);
                // Utiliser des rôles par défaut en cas d'erreur
                this.availableRoles = [
                    { role: 'ROLE_USER', isAdmin: false },
                    { role: 'ROLE_ADMIN', isAdmin: true },
                    { role: 'ROLE_MODERATOR', isAdmin: false }
                ];
            }
        });
    }

    private loadUser(): void {
        if (!this.userPseudo) return;

        this.isLoading = true;
        this.userService.getUserWithDetails(this.userPseudo).subscribe({
            next: (user) => {
                this.currentUser = user;
                this.adminForm.patchValue({
                    selectedRole: user.role?.role || 'ROLE_USER',
                    initialCredit: user.credit || 10
                });

                // Préparer les données pré-remplies pour le composant register
                this.prefilledData = {
                    pseudo: user.pseudo,
                    nom: user.nom,
                    prenom: user.prenom,
                    email: user.email,
                    telephone: user.telephone || '',
                    adresse: {
                        rue: user.adresse?.rue || '',
                        codePostal: user.adresse?.codePostal || '',
                        ville: user.adresse?.ville || ''
                    }
                };

                this.isLoading = false;
            },
            error: (error) => {
                this.errorMessage = 'Erreur lors du chargement de l\'utilisateur';
                this.isLoading = false;
                console.error('Erreur:', error);
            }
        });
    }

    onRegisterFormReady(formGroup: FormGroup): void {
        this.registerForm = formGroup;
    }

    onRegisterSubmit(formData: any): void {
        // Cette méthode sera appelée quand le composant register tente de soumettre
        // On intercepte pour ajouter nos données admin
        if (this.isEditMode) {
            this.updateUser(formData);
        } else {
            this.createUser(formData);
        }
    }

    onSubmit(): void {
        if (!this.isFormValid()) {
            this.errorMessage = 'Veuillez corriger les erreurs dans le formulaire';
            return;
        }

        // Déclencher la soumission du composant register
        if (this.registerComponent) {
            this.registerComponent.onSubmit();
        }
    }

    private createUser(registerData: any): void {
        this.isLoading = true;
        this.errorMessage = '';
        this.successMessage = '';

        // Synchroniser l'état de chargement avec le composant register
        this.registerComponent?.setLoadingState(true);

        const userData = {
            pseudo: registerData.pseudo,
            nom: registerData.nom,
            prenom: registerData.prenom,
            email: registerData.email,
            telephone: registerData.telephone || '',
            credit: this.adminForm.get('initialCredit')?.value || 10,
            password: registerData.password,
            role: this.adminForm.get('selectedRole')?.value,
            adresse: registerData.adresse || {
                rue: '',
                codePostal: '',
                ville: ''
            }
        };

        this.userService.createUser(userData).subscribe({
            next: (response) => {
                this.successMessage = 'Utilisateur créé avec succès par l\'administrateur !';
                this.registerComponent?.setSuccessMessage(this.successMessage);
                this.isLoading = false;
                this.registerComponent?.setLoadingState(false);
                setTimeout(() => {
                    this.router.navigate(['/users']);
                }, 2000);
            },
            error: (error) => {
                this.errorMessage = error.error?.message || 'Erreur lors de la création de l\'utilisateur';
                this.registerComponent?.setErrorMessage(this.errorMessage);
                this.isLoading = false;
                this.registerComponent?.setLoadingState(false);
                console.error('Erreur création:', error);
            }
        });
    }

    private updateUser(registerData: any): void {
        if (!this.userPseudo) return;

        this.isLoading = true;
        this.errorMessage = '';
        this.successMessage = '';

        // Synchroniser l'état de chargement avec le composant register
        this.registerComponent?.setLoadingState(true);

        const updateUserData = {
            nom: registerData.nom,
            prenom: registerData.prenom,
            email: registerData.email,
            telephone: registerData.telephone || '',
            credit: this.adminForm.get('initialCredit')?.value
        };

        const updateRole$ = this.userService.updateUserRole(
            this.userPseudo,
            this.adminForm.get('selectedRole')?.value
        );

        const updateUser$ = this.userService.updateUser(this.userPseudo, updateUserData);

        let updateAdresse$ = null;
        if (registerData.adresse && (registerData.adresse.rue || registerData.adresse.codePostal || registerData.adresse.ville)) {
            updateAdresse$ = this.userService.updateUserAdresse(this.userPseudo, registerData.adresse);
        }

        // Combiner les mises à jour
        const updates = [updateUser$, updateRole$];
        if (updateAdresse$) {
            updates.push(updateAdresse$);
        }

        forkJoin(updates).subscribe({
            next: () => {
                this.successMessage = 'Utilisateur mis à jour avec succès !';
                this.registerComponent?.setSuccessMessage(this.successMessage);
                this.isLoading = false;
                this.registerComponent?.setLoadingState(false);
                setTimeout(() => {
                    this.router.navigate(['/users']);
                }, 2000);
            },
            error: (error) => {
                this.errorMessage = error.error?.message || 'Erreur lors de la mise à jour de l\'utilisateur';
                this.registerComponent?.setErrorMessage(this.errorMessage);
                this.isLoading = false;
                this.registerComponent?.setLoadingState(false);
                console.error('Erreur mise à jour:', error);
            }
        });
    }

    onCancel(): void {
        this.router.navigate(['/users']);
    }

    onDelete(): void {
        if (!this.userPseudo) return;

        if (confirm('Êtes-vous sûr de vouloir supprimer cet utilisateur ?')) {
            this.isLoading = true;
            this.userService.deleteUser(this.userPseudo).subscribe({
                next: () => {
                    this.successMessage = 'Utilisateur supprimé avec succès !';
                    this.isLoading = false;
                    setTimeout(() => {
                        this.router.navigate(['/users']);
                    }, 1500);
                },
                error: (error) => {
                    this.errorMessage = error.error?.message || 'Erreur lors de la suppression de l\'utilisateur';
                    this.isLoading = false;
                    console.error('Erreur suppression:', error);
                }
            });
        }
    }

    onPreview(): void {
        this.showPreview = !this.showPreview;
    }

    // Méthodes utilitaires
    isFormValid(): boolean {
        return this.adminForm.valid && (this.registerForm?.valid ?? false);
    }

    isFieldInvalid(fieldName: string): boolean {
        const field = this.adminForm.get(fieldName);
        return !!(field?.invalid && field?.touched);
    }

    getPreviewData(): any {
        if (!this.registerForm) return {};

        return {
            pseudo: this.registerForm.get('pseudo')?.value || '',
            nom: this.registerForm.get('nom')?.value || '',
            prenom: this.registerForm.get('prenom')?.value || '',
            email: this.registerForm.get('email')?.value || '',
            role: this.getRoleDisplayName(this.adminForm.get('selectedRole')?.value || ''),
            credit: this.adminForm.get('initialCredit')?.value || 0
        };
    }

    getRoleDisplayName(role: string): string {
        const roleMap: { [key: string]: string } = {
            'ROLE_USER': '👤 Utilisateur Standard',
            'ROLE_ADMIN': '⚡ Administrateur',
        };
        return roleMap[role] || role;
    }

    // Méthode pour obtenir la liste des rôles pour le template
    get roleOptions() {
        return this.availableRoles.map(role => ({
            value: role.role,
            label: this.getRoleDisplayName(role.role),
            isAdmin: role.isAdmin
        }));
    }
}
