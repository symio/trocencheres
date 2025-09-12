// src/app/core/services/user.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, forkJoin, switchMap } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '@env/environment';

@Injectable({
    providedIn: 'root'
})
export class UserService {
    private apiUrl = `${environment.apiUrl}`;

    constructor(private http: HttpClient) { }

    register(userData: any): Observable<any> {
        return this.http.post(`${this.apiUrl}/profil/register`, userData);
    }

    /**
     * Récupère tous les utilisateurs via Spring Data REST
     */
    getAllUsers(page: number = 0, size: number = 20): Observable<SpringDataResponse<User>> {
        return this.http.get<SpringDataResponse<User>>(
            `${this.apiUrl}/utilisateurs?page=${page}&size=${size}`
        );
    }

    /**
     * Récupère un utilisateur par son pseudo
     */
    getUser(pseudo: string): Observable<User> {
        return this.http.get<User>(`${this.apiUrl}/utilisateurs/${pseudo}`);
    }

    /**
     * Récupère les détails complets d'un utilisateur (avec rôle et adresse)
     */
    getUserWithDetails(pseudo: string): Observable<any> {
        return this.getUser(pseudo).pipe(
            switchMap(user => {
                const roleRequest = this.http.get(`${this.apiUrl}/utilisateurs/${pseudo}/role`);
                const adresseRequest = this.http.get(`${this.apiUrl}/utilisateurs/${pseudo}/adresse`);

                return forkJoin({
                    user: [user],
                    role: roleRequest,
                    adresse: adresseRequest
                });
            }),
            map(result => ({
                ...result.user,
                role: result.role,
                adresse: result.adresse
            }))
        );
    }

    /**
     * Récupère tous les rôles disponibles
     */
    getAllRoles(): Observable<Role[]> {
        return this.http.get<SpringDataResponse<Role>>(`${this.apiUrl}/roles`)
            .pipe(
                map(response => response._embedded?.['roles'] || [])
            );
    }

    /**
     * Trouve un rôle par son nom
     */
    getRoleByName(roleName: string): Observable<Role> {
        return this.http.get<Role>(
            `${this.apiUrl}/roles/search/findByRole?role=${roleName}`
        ).pipe(
            map(response => response || null)
        );
    }

    /**
     * Crée un utilisateur via Spring Data REST (approche en 3 étapes)
     */
    createUser(userData: {
        pseudo: string;
        nom: string;
        prenom: string;
        email: string;
        telephone?: string;
        credit: number;
        password: string;
        role: string;
        adresse: {
            rue: string;
            codePostal: string;
            ville: string;
        };
    }): Observable<User> {

        // Étape 1 : Créer l'adresse
        return this.createAdresse(userData.adresse).pipe(
            switchMap(adresse => {
                // Étape 2 : Récupérer le rôle
                return this.getRoleByName(userData.role).pipe(
                    switchMap(role => {
                        // Étape 3 : Créer l'utilisateur avec les liens vers adresse et rôle
                        const userPayload = {
                            pseudo: userData.pseudo,
                            nom: userData.nom,
                            prenom: userData.prenom,
                            email: userData.email,
                            telephone: userData.telephone,
                            credit: userData.credit,
                            password: userData.password,
                            // Utiliser les URIs Spring Data REST pour les relations
                            adresse: adresse._links.self.href,
                            role: role._links.self.href
                        };

                        return this.http.post<User>(`${this.apiUrl}/utilisateurs`, userPayload);
                    })
                );
            })
        );
    }

    /**
     * Crée une adresse via Spring Data REST
     */
    private createAdresse(adresseData: {
        rue: string;
        codePostal: string;
        ville: string;
    }): Observable<Adresse> {
        const payload = {
            rue: adresseData.rue,
            codePostal: adresseData.codePostal,
            ville: adresseData.ville,
            adresseEni: false // valeur par défaut
        };

        return this.http.post<Adresse>(`${this.apiUrl}/addresses`, payload);
    }

    /**
     * Met à jour un utilisateur (PATCH pour modification partielle)
     */
    updateUser(pseudo: string, userData: Partial<User>): Observable<User> {
        const headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        return this.http.patch<User>(
            `${this.apiUrl}/utilisateurs/${pseudo}`,
            userData,
            { headers }
        );
    }

    /**
     * Met à jour le rôle d'un utilisateur
     */
    updateUserRole(pseudo: string, newRoleName: string): Observable<void> {
        return this.getRoleByName(newRoleName).pipe(
            switchMap(role => {
                const headers = new HttpHeaders({
                    'Content-Type': 'text/uri-list'
                });

                return this.http.put<void>(
                    `${this.apiUrl}/utilisateurs/${pseudo}/role`,
                    role._links.self.href,
                    { headers }
                );
            })
        );
    }

    /**
     * Met à jour l'adresse d'un utilisateur
     */
    updateUserAdresse(pseudo: string, adresseData: {
        rue: string;
        codePostal: string;
        ville: string;
    }): Observable<void> {
        // Récupérer l'adresse actuelle et la mettre à jour
        return this.http.get<Adresse>(`${this.apiUrl}/utilisateurs/${pseudo}/adresse`).pipe(
            switchMap(currentAdresse => {
                const updatedAdresse = {
                    ...currentAdresse,
                    ...adresseData
                };

                return this.http.put<void>(
                    currentAdresse._links.self.href,
                    updatedAdresse
                );
            })
        );
    }

    /**
     * Supprime un utilisateur
     */
    deleteUser(pseudo: string): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/utilisateurs/${pseudo}`);
    }

    /**
     * Recherche d'utilisateurs (si vous avez des méthodes de recherche custom)
     */
    searchUsersByEmail(email: string): Observable<User[]> {
        return this.http.get<SpringDataResponse<User>>(
            `${this.apiUrl}/utilisateurs/search/findByEmailContaining?email=${email}`
        ).pipe(
            map(response => response._embedded?.['utilisateurs'] || [])
        );
    }

    searchUsersByNom(nom: string): Observable<User[]> {
        return this.http.get<SpringDataResponse<User>>(
            `${this.apiUrl}/utilisateurs/search/findByNomContaining?nom=${nom}`
        ).pipe(
            map(response => response._embedded?.['utilisateurs'] || [])
        );
    }

    /**
     * Méthode helper pour extraire l'ID d'une URI Spring Data REST
     */
    private extractIdFromUri(uri: string): string | null {
        const matches = uri.match(/\/(\d+)$/);
        return matches ? matches[1] : null;
    }

}

interface User {
    pseudo: string;
    nom: string;
    prenom: string;
    email: string;
    telephone?: string;
    credit: number;
    _links?: any;
}

interface Role {
    role: string;
    isAdmin: boolean;
    _links?: any;
}

interface Adresse {
    rue: string;
    codePostal: string;
    ville: string;
    _links?: any;
}

interface SpringDataResponse<T> {
    _embedded: { [key: string]: T[] };
    _links: any;
    page?: {
        size: number;
        totalElements: number;
        totalPages: number;
        number: number;
    };
}
