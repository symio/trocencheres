// src/app/core/interceptors/auth.interceptor.ts
import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { OAuth2Service } from '../services/oauth2.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(
    private oauth2Service: OAuth2Service,
    private router: Router
  ) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    // Ne pas ajouter le token pour les requêtes d'authentification
    if (request.url.includes('/authorize/token') || request.url.includes('/profil/register')) {
      return next.handle(request);
    }

    // Ajouter le token d'authentification s'il existe
    const token = this.oauth2Service.getToken();
    if (token) {
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        // Gérer les erreurs 401 (non autorisé)
        if (error.status === 401) {
          this.oauth2Service.logout();
          this.router.navigate(['/auth/login']);
        }

        // Gérer les erreurs 403 (accès refusé)
        if (error.status === 403) {
          console.error('Accès refusé - Permissions insuffisantes');
          // Vous pouvez rediriger vers une page d'erreur ou afficher un message
        }

        return throwError(() => error);
      })
    );
  }
}