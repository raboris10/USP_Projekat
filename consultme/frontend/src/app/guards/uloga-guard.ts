import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth-service';

// Guard za stranice jedne uloge, npr. ulogaGuard('STUDENT') u app.routes.ts.
// - nije prijavljen          -> na prijavu
// - prijavljen, druga uloga  -> na svoju glavnu stranicu
// VAŽNO: ovo štiti samo PRIKAZ u browseru. Prava zaštita je na backendu (@PreAuthorize).
export function ulogaGuard(uloga: string): CanActivateFn {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.jePrijavljen()) {
      return router.createUrlTree(['/']);
    }
    if (authService.uloga() !== uloga) {
      return router.createUrlTree([authService.pocetnaStranica()]);
    }
    return true;
  };
}
