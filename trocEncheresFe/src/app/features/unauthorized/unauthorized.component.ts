// src/app/features/unauthorized/unauthorized.component.ts
import { Component } from '@angular/core';

@Component({
  selector: 'app-unauthorized',
  template: `
    <div class="unauthorized-container">
      <h1>🚫 Accès refusé</h1>
      <p>Vous n'avez pas les permissions nécessaires pour accéder à cette page.</p>
      <a routerLink="/dashboard" class="btn btn-primary mt-3">Retour au tableau de bord</a>
    </div>
  `,
  styles: [`
    .unauthorized-container {
      text-align: center;
      margin-top: 80px;
    }
    h1 {
      color: #c82333;
      margin-bottom: 20px;
    }
    p {
      font-size: 18px;
      margin-bottom: 15px;
    }
  `]
})
export class UnauthorizedComponent {}
