import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { KorisnikAdmin } from '../../../model/korisnik';
import { AdminKorisnikService } from '../../../services/admin-korisnik-service';
import { AuthService } from '../../../services/auth-service';
import { PorukaService, tekstGreske } from '../../../services/poruka-service';

// Koji dijalog je otvoren
type Dijalog = 'forma' | 'lozinka' | 'deaktivacija' | null;

// Admin: lista korisnika, dodavanje, izmena, reset lozinke, deaktivacija i aktivacija
@Component({
  selector: 'app-admin-korisnici',
  imports: [FormsModule],
  templateUrl: './korisnici.html',
})
export class AdminKorisnici implements OnInit {

  private adminKorisnikService = inject(AdminKorisnikService);
  private authService = inject(AuthService);
  private porukaService = inject(PorukaService);

  korisnici = signal<KorisnikAdmin[]>([]);
  ucitavanje = signal(true);

  // Filteri
  uloga: string | null = null;
  pretraga = '';
  private tajmerPretrage?: ReturnType<typeof setTimeout>;

  // Dijalozi
  dijalog = signal<Dijalog>(null);
  izabrani = signal<KorisnikAdmin | null>(null);   // null u formi -> novi korisnik
  greskaDijaloga = signal('');
  slanje = signal(false);

  // Polja forme
  ime = '';
  prezime = '';
  email = '';
  ulogaForme = 'STUDENT';
  lozinka = '';

  ngOnInit(): void {
    this.ucitaj();
  }

  ucitaj(): void {
    this.adminKorisnikService.pretrazi(this.uloga, this.pretraga).subscribe({
      next: (lista) => {
        this.korisnici.set(lista);
        this.ucitavanje.set(false);
      },
      error: (err) => {
        this.ucitavanje.set(false);
        this.porukaService.greska(err);
      },
    });
  }

  // Pretraga dok se kuca: čekamo 300 ms posle poslednjeg slova, da ne šaljemo zahtev za svako slovo
  pretragaPromenjena(): void {
    clearTimeout(this.tajmerPretrage);
    this.tajmerPretrage = setTimeout(() => this.ucitaj(), 300);
  }

  // Admin ne može da deaktivira sebe niti sebi promeni ulogu
  jeSam(korisnik: KorisnikAdmin): boolean {
    return korisnik.email === this.authService.prijavljeniKorisnik()?.email;
  }

  nazivUloge(uloga: string): string {
    switch (uloga) {
      case 'STUDENT': return 'Student';
      case 'PROFESOR': return 'Profesor';
      case 'ADMIN': return 'Administrator';
      default: return uloga;
    }
  }

  // ----- Otvaranje dijaloga -----

  otvoriFormu(korisnik: KorisnikAdmin | null): void {
    this.izabrani.set(korisnik);
    this.ime = korisnik?.ime ?? '';
    this.prezime = korisnik?.prezime ?? '';
    this.email = korisnik?.email ?? '';
    this.ulogaForme = korisnik?.uloga ?? 'STUDENT';
    this.lozinka = '';
    this.otvori('forma');
  }

  otvoriLozinku(korisnik: KorisnikAdmin): void {
    this.izabrani.set(korisnik);
    this.lozinka = '';
    this.otvori('lozinka');
  }

  otvoriDeaktivaciju(korisnik: KorisnikAdmin): void {
    this.izabrani.set(korisnik);
    this.otvori('deaktivacija');
  }

  zatvori(): void {
    this.dijalog.set(null);
  }

  private otvori(dijalog: Dijalog): void {
    this.greskaDijaloga.set('');
    this.slanje.set(false);
    this.dijalog.set(dijalog);
  }

  // ----- Akcije -----

  sacuvajKorisnika(): void {
    const izmena = this.izabrani();

    if (!this.ime.trim() || !this.prezime.trim() || !this.email.trim()) {
      this.greskaDijaloga.set('Popunite ime, prezime i email.');
      return;
    }
    if (!izmena && this.lozinka.length < 6) {
      this.greskaDijaloga.set('Početna lozinka mora imati bar 6 znakova.');
      return;
    }

    const zahtev = {
      ime: this.ime.trim(),
      prezime: this.prezime.trim(),
      email: this.email.trim(),
      uloga: this.ulogaForme,
      ...(izmena ? {} : { lozinka: this.lozinka }),   // lozinka samo za novog korisnika
    };

    this.slanje.set(true);
    const zahtevKaBackendu = izmena
      ? this.adminKorisnikService.update(izmena.id, zahtev)
      : this.adminKorisnikService.create(zahtev);

    zahtevKaBackendu.subscribe({
      next: () => {
        this.zatvori();
        this.porukaService.uspeh(izmena ? 'Izmene su sačuvane.' : 'Korisnik je dodat.');
        this.ucitaj();
      },
      error: (err) => this.neuspeh(err),
    });
  }

  sacuvajLozinku(): void {
    const korisnik = this.izabrani();
    if (!korisnik) return;

    if (this.lozinka.length < 6) {
      this.greskaDijaloga.set('Lozinka mora imati bar 6 znakova.');
      return;
    }

    this.slanje.set(true);
    this.adminKorisnikService.resetujLozinku(korisnik.id, this.lozinka).subscribe({
      next: () => {
        this.zatvori();
        this.porukaService.uspeh(`Lozinka za ${korisnik.ime} ${korisnik.prezime} je promenjena.`);
      },
      error: (err) => this.neuspeh(err),
    });
  }

  potvrdiDeaktivaciju(): void {
    const korisnik = this.izabrani();
    if (!korisnik) return;

    this.slanje.set(true);
    this.adminKorisnikService.deaktiviraj(korisnik.id).subscribe({
      next: (izmenjen) => {
        this.zatvori();
        this.zameni(izmenjen);
        this.porukaService.uspeh('Nalog je deaktiviran.');
      },
      // npr. "Profesor ima zakazane buduće termine..." - ostaje u dijalogu
      error: (err) => this.neuspeh(err),
    });
  }

  // Aktivacija ne traži potvrdu (ništa se ne gubi)
  aktiviraj(korisnik: KorisnikAdmin): void {
    this.adminKorisnikService.aktiviraj(korisnik.id).subscribe({
      next: (izmenjen) => {
        this.zameni(izmenjen);
        this.porukaService.uspeh('Nalog je aktiviran.');
      },
      error: (err) => this.porukaService.greska(err),
    });
  }

  private zameni(izmenjen: KorisnikAdmin): void {
    this.korisnici.update((lista) => lista.map((k) => (k.id === izmenjen.id ? izmenjen : k)));
  }

  private neuspeh(err: HttpErrorResponse): void {
    this.slanje.set(false);
    this.greskaDijaloga.set(tekstGreske(err));
  }

}
