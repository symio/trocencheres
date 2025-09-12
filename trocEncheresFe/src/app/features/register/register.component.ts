// src/app/features/register/register.component.ts
import { Component, OnInit, Input, Output, EventEmitter, SimpleChanges, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { catchError, of } from 'rxjs';
import { environment } from '@env/environment';
import { UserService } from '@app/@core/services/user.service';

export interface PrefilledData {
    pseudo?: string;
    nom?: string;
    prenom?: string;
    email?: string;
    telephone?: string;
    adresse?: {
        rue?: string;
        codePostal?: string;
        ville?: string;
    };
}

@Component({
    selector: 'app-register',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterModule],
    templateUrl: './register.component.html',
    styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit, OnChanges {
    @Input() prefilledData: PrefilledData = {};
    @Input() isAdminMode: boolean = false;
    @Input() hidePasswordFields: boolean = false;
    @Output() formReady = new EventEmitter<FormGroup>();
    @Output() registerSubmit = new EventEmitter<any>();

    registerForm!: FormGroup;
    isLoading = false;
    errorMessage = '';
    successMessage = '';

    constructor(
        private fb: FormBuilder,
        private router: Router,
        private userService: UserService
    ) { }


    ngOnChanges(changes: SimpleChanges): void {
        if (changes['prefilledData'] && changes['prefilledData'].currentValue) {
            this.prefillForm();
        }
    }

    ngOnInit(): void {
        this.createForm();
        this.prefillForm();
        // Émettre le formulaire quand il est prêt
        this.formReady.emit(this.registerForm);
    }

    private createForm(): void {
        this.registerForm = this.fb.group({
            pseudo: ['', [Validators.required, Validators.minLength(3)]],
            nom: ['', [Validators.required]],
            prenom: ['', [Validators.required]],
            email: ['', [Validators.required, Validators.email]],
            telephone: [''], // Optionnel
            adresse: this.fb.group({
                rue: ['', [Validators.required]],
                codePostal: ['', [Validators.required, Validators.pattern(/^[0-9]{5}$/)]],
                ville: ['', [Validators.required]]
            })
        });

        // Ajouter les champs de mot de passe seulement si nécessaire
        if (!this.hidePasswordFields) {
            this.registerForm.addControl('password',
                this.fb.control('', [Validators.required, Validators.minLength(8), this.passwordValidator])
            );
            this.registerForm.addControl('confirmPassword',
                this.fb.control('', [Validators.required])
            );
            // Ajouter le validateur de correspondance des mots de passe
            this.registerForm.setValidators(this.passwordMatchValidator);
        }
    }

    private prefillForm(): void {
        if (this.prefilledData && Object.keys(this.prefilledData).length > 0) {
            // Préparer les données pour le patchValue
            const formData: any = {
                pseudo: this.prefilledData.pseudo || '',
                nom: this.prefilledData.nom || '',
                prenom: this.prefilledData.prenom || '',
                email: this.prefilledData.email || '',
                telephone: this.prefilledData.telephone || '',
            };

            // Ajouter l'adresse si elle existe
            if (this.prefilledData.adresse) {
                formData.adresse = {
                    rue: this.prefilledData.adresse.rue || '',
                    codePostal: this.prefilledData.adresse.codePostal || '',
                    ville: this.prefilledData.adresse.ville || ''
                };
            }

            this.registerForm.patchValue(formData);
        }
    }

    // Validateur personnalisé pour le mot de passe
    private passwordValidator(control: AbstractControl): ValidationErrors | null {
        const password = control.value;
        if (!password) return null;

        const hasNumber = /[0-9]/.test(password);
        const hasUpper = /[A-Z]/.test(password);
        const hasLower = /[a-z]/.test(password);
        const hasSpecial = /[!@#$%^&*(),.?":{}|<>]/.test(password);

        const valid = hasNumber && hasUpper && hasLower && hasSpecial;

        if (!valid) {
            return {
                passwordStrength: {
                    hasNumber,
                    hasUpper,
                    hasLower,
                    hasSpecial
                }
            };
        }
        return null;
    }

    // Validateur pour vérifier que les mots de passe correspondent
    private passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
        const password = control.get('password');
        const confirmPassword = control.get('confirmPassword');

        if (!password || !confirmPassword) {
            return null;
        }

        return password.value === confirmPassword.value ? null : { passwordMismatch: true };
    }

    onSubmit(): void {
        if (this.registerForm.invalid) {
            this.markFormGroupTouched();
            return;
        }

        // Si on est en mode admin, émettre les données vers le parent
        if (this.isAdminMode) {
            const formData = { ...this.registerForm.value };
            if (!this.hidePasswordFields) {
                delete formData.confirmPassword;
            }
            // Supprimer le téléphone s'il est vide
            if (!formData.telephone) delete formData.telephone;

            this.registerSubmit.emit(formData);
            return;
        }

        // Mode normal - inscription directe
        this.performRegistration();
    }

    private performRegistration(): void {
        this.isLoading = true;
        this.errorMessage = '';
        this.successMessage = '';

        // Préparer les données pour l'API (sans confirmPassword et téléphone si vide)
        const formData = { ...this.registerForm.value };
        delete formData.confirmPassword;

        // Supprimer le téléphone s'il est vide
        if (!formData.telephone) delete formData.telephone;

        const request$ = this.userService.register(formData);

        // Appel API
        request$.subscribe({
            next: (response) => {
                this.successMessage = 'Inscription réussie ! Redirection vers la page de connexion...';
                setTimeout(() => this.router.navigate(['/auth/login']), 2000);
            },
            error: (error) => {
                console.error('Erreur:', error);
                this.errorMessage = error.error?.message || 'Une erreur est survenue.';
            },
            complete: () => {
                this.isLoading = false;
            }
        });
    }

    private markFormGroupTouched(): void {
        Object.keys(this.registerForm.controls).forEach(key => {
            const control = this.registerForm.get(key);
            control?.markAsTouched();

            if (control instanceof FormGroup) {
                Object.keys(control.controls).forEach(nestedKey => {
                    control.get(nestedKey)?.markAsTouched();
                });
            }
        });
    }

    // Helpers pour le template
    isFieldInvalid(fieldName: string): boolean {
        const field = this.registerForm.get(fieldName);
        return !!(field && field.invalid && field.touched);
    }

    getFieldError(fieldName: string): string {
        const field = this.registerForm.get(fieldName);
        if (!field || !field.errors) return '';

        if (field.errors['required']) return `${fieldName} est requis`;
        if (field.errors['email']) return 'Format email invalide';
        if (field.errors['minlength']) return `Minimum ${field.errors['minlength'].requiredLength} caractères`;
        if (field.errors['pattern']) return 'Format invalide';
        if (field.errors['passwordStrength']) {
            return 'Le mot de passe doit contenir au moins: une majuscule, une minuscule, un chiffre et un caractère spécial';
        }

        return 'Champ invalide';
    }

    get passwordMismatch(): boolean {
        return !!(this.registerForm.errors?.['passwordMismatch'] &&
            this.registerForm.get('confirmPassword')?.touched);
    }

    // Méthode publique pour accéder au FormGroup depuis le parent
    getFormGroup(): FormGroup {
        return this.registerForm;
    }

    // Méthode pour définir l'état de chargement depuis le parent
    setLoadingState(loading: boolean): void {
        this.isLoading = loading;
    }

    // Méthodes pour définir les messages depuis le parent
    setErrorMessage(message: string): void {
        this.errorMessage = message;
    }

    setSuccessMessage(message: string): void {
        this.successMessage = message;
    }
}
