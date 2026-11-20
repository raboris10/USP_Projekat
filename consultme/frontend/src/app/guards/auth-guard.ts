import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth-service';

// Guard proverava da li sme da se otvori neka ruta (vidi app.routes.ts).
// VAŽNO: ovo štiti samo PRIKAZ u browseru. Prava zaštita podataka je na backendu
// (SecurityConfig + JwtFilter) - bez ispravnog tokena backend ništa ne vraća.
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.jePrijavljen()) {
    return true; // prijavljen -> pusti
  }

  // nije prijavljen -> preusmeri na početnu stranu sa prijavom
  return router.createUrlTree(['/']);
};
