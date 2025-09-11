// core/services/oauth2.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { map, catchError, tap } from 'rxjs/operators';
import { environment } from '@env/environment';

export interface OAuth2TokenResponse {
    access_token: string;
    token_type: string;
    expires_in: number;
    scope: string;
}

export interface OAuth2Credentials {
    clientId: string;
    clientSecret: string;
}

export interface UserInfo {
    clientId: string;
    scope: string;
    authority: string;
    isAdmin: boolean;
}

@Injectable({
    providedIn: 'root'
})
export class OAuth2Service {
    private tokenSubject = new BehaviorSubject<string | null>(this.getStoredToken());
    private userInfoSubject = new BehaviorSubject<UserInfo | null>(this.getStoredUserInfo());
    private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.checkInitialAuth());

    public token$ = this.tokenSubject.asObservable();
    public userInfo$ = this.userInfoSubject.asObservable();
    public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

    constructor(private http: HttpClient) { }

    /**
     * Authentification avec Client Credentials Flow
     */
    login(credentials: OAuth2Credentials): Observable<OAuth2TokenResponse> {
        const headers = new HttpHeaders({
            'Content-Type': 'application/x-www-form-urlencoded'
        });

        const body = new URLSearchParams({
            grant_type: 'client_credentials',
            client_id: credentials.clientId,
            client_secret: credentials.clientSecret,
            scope: 'access'
        });

        return this.http.post<OAuth2TokenResponse>(
            `${environment.apiUrl}/authorize/token`,
            body.toString(),
            { headers }
        ).pipe(
            tap(response => {
                // Stocker le token
                this.storeToken(response.access_token);

                // Extraire et stocker les infos utilisateur
                this.extractAndStoreUserInfo(response.access_token);

                // Mettre à jour les subjects
                this.tokenSubject.next(response.access_token);
                this.isAuthenticatedSubject.next(true);
            }),
            catchError(error => {
                console.error('[OAuth2Service] Erreur d\'authentification:', error);
                this.logout();
                return throwError(() => error);
            })
        );
    }

    /**
     * Déconnexion
     */
    logout(): Observable<void> {
        return new Observable<void>((observer) => {
            try {
                this.removeStoredToken();
                this.removeStoredUserInfo();
                this.tokenSubject.next(null);
                this.userInfoSubject.next(null);
                this.isAuthenticatedSubject.next(false);

                observer.next();
                observer.complete();
            } catch (error) {
                observer.error(error);
            }
        });
    }

    /**
     * Vérifier si l'utilisateur est connecté
     */
    isAuthenticated(): boolean {
        const token = this.getStoredToken();
        if (!token) {
            return false;
        }

        const isValid = !this.isTokenExpired(token);

        // Mettre à jour le subject si nécessaire
        if (this.isAuthenticatedSubject.value !== isValid) {
            this.isAuthenticatedSubject.next(isValid);
        }

        return isValid;
    }

    /**
     * Vérifier si l'utilisateur a un rôle spécifique
     */
    hasRole(role: string): boolean {
        const userInfo = this.userInfoSubject.value;
        if (!userInfo) return false;

        const hasRole = userInfo.authority === `ROLE_${role.toUpperCase()}`;

        return hasRole;
    }

    /**
     * Vérifier si l'utilisateur est admin
     */
    isAdmin(): boolean {
        const userInfo = this.userInfoSubject.value;
        const isAdmin = userInfo?.isAdmin || false;

        return isAdmin;
    }

    /**
     * Obtenir les informations utilisateur
     */
    getUserInfo(): UserInfo | null {
        return this.userInfoSubject.value;
    }

    /**
     * Obtenir le token actuel
     */
    getToken(): string | null {
        return this.tokenSubject.value;
    }

    // Méthodes privées pour la gestion du stockage
    private checkInitialAuth(): boolean {
        const token = this.getStoredToken();
        return token ? !this.isTokenExpired(token) : false;
    }

    private storeToken(token: string): void {
        localStorage.setItem('oauth2_token', token);
    }

    private getStoredToken(): string | null {
        return localStorage.getItem('oauth2_token');
    }

    private removeStoredToken(): void {
        localStorage.removeItem('oauth2_token');
    }

    private storeUserInfo(userInfo: UserInfo): void {
        localStorage.setItem('user_info', JSON.stringify(userInfo));
    }

    private getStoredUserInfo(): UserInfo | null {
        const stored = localStorage.getItem('user_info');
        return stored ? JSON.parse(stored) : null;
    }

    private removeStoredUserInfo(): void {
        localStorage.removeItem('user_info');
    }

    /**
     * Extraire les informations utilisateur du token JWT
     */
    private extractAndStoreUserInfo(token: string): void {
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));

            const userInfo: UserInfo = {
                clientId: payload.client_id || payload.sub,
                scope: payload.scope || '',
                authority: payload.authority || '',
                isAdmin: payload.isAdmin || false
            };

            this.storeUserInfo(userInfo);
            this.userInfoSubject.next(userInfo);
        } catch (error) {
            console.error('[OAuth2Service] Erreur lors de l\'extraction des infos utilisateur:', error);
        }
    }

    /**
     * Vérifier si le token est expiré
     */
    private isTokenExpired(token: string): boolean {
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            const exp = payload.exp;

            if (!exp) {

                return true;
            }

            const now = Math.floor(Date.now() / 1000);
            const isExpired = exp < now;

            return isExpired;
        } catch (error) {
            console.error('[OAuth2Service] Erreur lors de la vérification d\'expiration:', error);
            return true;
        }
    }
}
