import { Component, computed, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../services/auth-service';
import { NotifikacijaService } from '../../services/notifikacija-service';
import { TemaService } from '../../services/tema-service';

interface Link {
  putanja: string;
  naziv: string;
}

// Linkovi u meniju za svaku ulogu
const LINKOVI: Record<string, Link[]> = {
  STUDENT: [
    { putanja: '/student/termini', naziv: 'Termini' },
    { putanja: '/student/rezervacije', naziv: 'Moje rezervacije' },
  ],
  PROFESOR: [
    { putanja: '/profesor/termini', naziv: 'Moji termini' },
    { putanja: '/profesor/termini/novi', naziv: 'Novi termin' },
  ],
  ADMIN: [
    { putanja: '/admin/korisnici', naziv: 'Korisnici' },
    { putanja: '/admin/katedre', naziv: 'Katedre' },
    { putanja: '/admin/predmeti', naziv: 'Predmeti' },
  ],
};

// Koliko često zvonce pita backend za broj nepročitanih (30 sekundi)
const OSVEZAVANJE_MS = 30000;

// Zaglavlje sa menijem. Prikazuje se samo prijavljenim korisnicima (vidi app.html).
@Component({
  selector: 'app-zaglavlje',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './zaglavlje.html',
  styleUrl: './zaglavlje.css',
})
export class Zaglavlje implements OnInit, OnDestroy {

  authService = inject(AuthService);
  notifikacijaService = inject(NotifikacijaService);
  temaService = inject(TemaService);
  private router = inject(Router);

  // Na telefonu: da li je meni ("hamburger") otvoren
  meniOtvoren = signal(false);

  // computed -> automatski se preračuna kad se promeni prijavljeni korisnik
  linkovi = computed(() => LINKOVI[this.authService.uloga() ?? ''] ?? []);

  nazivUloge = computed(() => {
    switch (this.authService.uloga()) {
      case 'STUDENT': return 'Student';
      case 'PROFESOR': return 'Profesor';
      case 'ADMIN': return 'Administrator';
      default: return '';
    }
  });

  private tajmer?: ReturnType<typeof setInterval>;

  // Kad se zaglavlje pojavi: odmah osveži zvonce, pa ponavljaj na 30 sekundi
  ngOnInit(): void {
    this.notifikacijaService.osveziBroj();
    this.tajmer = setInterval(() => this.notifikacijaService.osveziBroj(), OSVEZAVANJE_MS);
  }

  // Kad zaglavlje nestane (odjava): zaustavi ponavljanje
  ngOnDestroy(): void {
    clearInterval(this.tajmer);
  }

  zatvoriMeni(): void {
    this.meniOtvoren.set(false);
  }

  odjaviSe(): void {
    this.zatvoriMeni();
    this.authService.odjavi();
    this.router.navigate(['/']);
  }

}
