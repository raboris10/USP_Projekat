import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth-service';
import { PorukaService } from '../services/poruka-service';

// Interceptor "presreće" SVAKI HTTP zahtev iz aplikacije pre nego što ode na server.
// Tako token dodajemo na jednom mestu, umesto u svakom servisu posebno.
export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const authService = inject(AuthService);
  const porukaService = inject(PorukaService);
  const router = inject(Router);

  const token = authService.getToken();

  // Ako imamo token, napravi KOPIJU zahteva sa zaglavljem Authorization
  // (zahtevi su nepromenljivi, zato clone)
  const zahtev = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  // Pošalji zahtev i prati odgovor
  return next(zahtev).pipe(
    catchError((greska: HttpErrorResponse) => {
      // 401 na bilo kojoj ruti osim login-a znači: token je istekao, neispravan,
      // ili je administrator deaktivirao nalog -> odjavi korisnika i vrati ga na prijavu.
      // Proveravamo jePrijavljen() da poruka ne iskoči više puta kad padne više zahteva odjednom.
      if (greska.status === 401 && !req.url.includes('/api/auth/login') && authService.jePrijavljen()) {
        authService.odjavi();
        porukaService.greska('Prijava je istekla. Prijavite se ponovo.');
        router.navigate(['/']);
      }
      // proslediti grešku dalje, da je komponenta može prikazati
      return throwError(() => greska);
    })
  );
};
