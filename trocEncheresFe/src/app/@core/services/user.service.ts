// src/app/core/services/user.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, forkJoin, of, switchMap } from 'rxjs';
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

    getAllUsers(page: number = 0, size: number = 20): Observable<SpringDataResponse<User>> {
        return this.http.get<SpringDataResponse<User>>(
            `${this.apiUrl}/utilisateurs?page=${page}&size=${size}`
        );
    }

    getUser(pseudo: string): Observable<User> {
        return this.http.get<User>(`${this.apiUrl}/utilisateurs/${pseudo}`);
    }

    /**
     * Récupère les détails complets d'un utilisateur
     * (utilise adresseId et roleId exposés par l'entité Utilisateur)
     */
    getUserWithDetails(pseudo: string): Observable<any> {
        return this.getUser(pseudo).pipe(
            switchMap(user => {
                const roleRequest = user.roleId
                    ? this.http.get(`${this.apiUrl}/roles/${user.roleId}`)
                    : of(null);

                const adresseRequest = user.adresseId
                    ? this.http.get(`${this.apiUrl}/addresses/${user.adresseId}`)
                    : of(null);

                return forkJoin({
                    user: of(user),
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

    getAllRoles(): Observable<Role[]> {
        return this.http.get<SpringDataResponse<Role>>(`${this.apiUrl}/roles`)
            .pipe(map(response => response._embedded?.['roles'] || []));
    }

    getRoleByName(roleName: string): Observable<Role> {
        return this.http.get<Role>(
            `${this.apiUrl}/roles/search/findByRole?role=${roleName}`
        ).pipe(map(response => response || null));
    }

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
        return this.createAdresse(userData.adresse).pipe(
            switchMap(adresse => {
                return this.getRoleByName(userData.role).pipe(
                    switchMap(role => {
                        const userPayload = {
                            pseudo: userData.pseudo,
                            nom: userData.nom,
                            prenom: userData.prenom,
                            email: userData.email,
                            telephone: userData.telephone,
                            credit: userData.credit,
                            password: userData.password,
                            adresse: adresse._links.self.href,
                            role: role._links.self.href
                        };
                        return this.http.post<User>(`${this.apiUrl}/utilisateurs`, userPayload);
                    })
                );
            })
        );
    }

    private createAdresse(adresseData: {
        rue: string;
        codePostal: string;
        ville: string;
    }): Observable<Adresse> {
        const payload = { ...adresseData, adresseEni: false };
        return this.http.post<Adresse>(`${this.apiUrl}/addresses`, payload);
    }

    updateUser(pseudo: string, userData: Partial<User>): Observable<User> {
        const headers = new HttpHeaders({ 'Content-Type': 'application/json' });
        return this.http.patch<User>(`${this.apiUrl}/utilisateurs/${pseudo}`, userData, { headers });
    }

    updateUserRole(pseudo: string, newRoleName: string): Observable<void> {
        return this.getRoleByName(newRoleName).pipe(
            switchMap(role => {
                const headers = new HttpHeaders({ 'Content-Type': 'text/uri-list' });
                return this.http.put<void>(
                    `${this.apiUrl}/utilisateurs/${pseudo}/role`,
                    role._links.self.href,
                    { headers }
                );
            })
        );
    }

    updateUserAdresse(pseudo: string, adresseData: {
        rue: string;
        codePostal: string;
        ville: string;
    }): Observable<void> {
        return this.getUser(pseudo).pipe(
            switchMap(user => {
                if (!user.adresseId) {
                    throw new Error('Utilisateur sans adresse');
                }
                return this.http.get<Adresse>(`${this.apiUrl}/addresses/${user.adresseId}`);
            }),
            switchMap(currentAdresse => {
                const updatedAdresse = { ...currentAdresse, ...adresseData };
                return this.http.put<void>(currentAdresse._links.self.href, updatedAdresse);
            })
        );
    }

    deleteUser(pseudo: string): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/utilisateurs/${pseudo}`);
    }

    searchUsersByEmail(email: string): Observable<User[]> {
        return this.http.get<SpringDataResponse<User>>(
            `${this.apiUrl}/utilisateurs/search/findByEmailContaining?email=${email}`
        ).pipe(map(response => response._embedded?.['utilisateurs'] || []));
    }

    searchUsersByNom(nom: string): Observable<User[]> {
        return this.http.get<SpringDataResponse<User>>(
            `${this.apiUrl}/utilisateurs/search/findByNomContaining?nom=${nom}`
        ).pipe(map(response => response._embedded?.['utilisateurs'] || []));
    }

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
    adresseId?: number;
    roleId?: number;
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
