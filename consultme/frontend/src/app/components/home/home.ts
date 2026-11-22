import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth-service';
import { tekstGreske } from '../../services/poruka-service';
import { TemaService } from '../../services/tema-service';

@Component({
  selector: 'app-home',
  imports: [FormsModule],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {

  private authService = inject(AuthService);
  private router = inject(Router);
  temaService = inject(TemaService);   // prekidač teme u uglu stranice za prijavu

  // Polja forme (povezana sa input-ima preko [(ngModel)])
  email = '';
  lozinka = '';

  greska = signal<string>('');          // tekst greške ispod forme
  ucitavanje = signal<boolean>(false);  // true dok čekamo odgovor backenda

  // Ako je korisnik već prijavljen (npr. osvežio stranicu), odmah ga prebaci na njegovu glavnu stranicu
  constructor() {
    if (this.authService.jePrijavljen()) {
      this.router.navigate([this.authService.pocetnaStranica()]);
    }
  }

  // Klik na "Prijavi se" (ili Enter u formi)
  prijaviSe(): void {
    // osnovna provera pre slanja; detaljnu validaciju radi backend
    if (this.email.trim() === '' || this.lozinka === '') {
      this.greska.set('Unesite email i lozinku.');
      return;
    }

    this.greska.set('');
    this.ucitavanje.set(true);

    this.authService.prijavi(this.email, this.lozinka).subscribe({
      // uspeh -> token je sačuvan u servisu, idemo na glavnu stranicu za ulogu
      next: () => {
        this.ucitavanje.set(false);
        this.router.navigate([this.authService.pocetnaStranica()]);
      },
      // greška -> backend vraća { "poruka": "..." } (GlobalExceptionHandler.java)
      error: (err: HttpErrorResponse) => {
        this.ucitavanje.set(false);
        this.greska.set(tekstGreske(err));
      },
    });
  }

}
