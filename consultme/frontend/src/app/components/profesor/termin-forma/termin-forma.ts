import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Predmet } from '../../../model/predmet';
import { AuthService } from '../../../services/auth-service';
import { PorukaService, tekstGreske } from '../../../services/poruka-service';
import { SifarnikService } from '../../../services/sifarnik-service';
import { TerminService } from '../../../services/termin-service';

// Profesor: forma za NOVI termin (/profesor/termini/novi)
// i za IZMENU postojećeg (/profesor/termini/5/izmena) - ista forma, dva režima.
@Component({
  selector: 'app-termin-forma',
  imports: [FormsModule, RouterLink],
  templateUrl: './termin-forma.html',
  styleUrl: './termin-forma.css',
})
export class TerminForma implements OnInit {

  private terminService = inject(TerminService);
  private sifarnikService = inject(SifarnikService);
  private authService = inject(AuthService);
  private porukaService = inject(PorukaService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  // Id termina iz adrese; null -> pravimo novi termin
  terminId: number | null = null;

  // Predmeti koje prijavljeni profesor drži (samo njih nudimo u padajućem meniju)
  mojiPredmeti = signal<Predmet[]>([]);

  // Polja forme
  datum = '';          // "2026-12-01"
  vreme = '';          // "12:00"
  trajanjeMin = 60;
  mesto = '';
  kapacitet = 5;
  predmetId: number | null = null;
  ponavljaj = false;
  ponavljanjeDo = '';

  // Prijavljenih na termin (samo kod izmene) - kapacitet ne sme ispod toga
  brojPrijavljenih = signal(0);

  greska = signal('');
  slanje = signal(false);
  ucitavanje = signal(false);

  jeIzmena(): boolean {
    return this.terminId !== null;
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    this.terminId = id ? Number(id) : null;

    // Predmeti: prijavljeni profesor je u listi profesora predmeta (poredimo po email-u)
    const mojEmail = this.authService.prijavljeniKorisnik()?.email;
    this.sifarnikService.predmeti().subscribe({
      next: (lista) => this.mojiPredmeti.set(
        lista.filter((p) => p.profesori.some((prof) => prof.email === mojEmail))),
    });

    if (this.terminId !== null) {
      this.ucitajTermin(this.terminId);
    }
  }

  // Izmena: popuni formu podacima postojećeg termina
  private ucitajTermin(id: number): void {
    this.ucitavanje.set(true);
    this.terminService.findById(id).subscribe({
      next: (t) => {
        // "2026-12-01T12:00:00" -> datum "2026-12-01" i vreme "12:00"
        this.datum = t.pocetak.substring(0, 10);
        this.vreme = t.pocetak.substring(11, 16);
        this.trajanjeMin = t.trajanjeMin;
        this.mesto = t.mesto;
        this.kapacitet = t.kapacitet;
        this.predmetId = t.predmetId;
        this.brojPrijavljenih.set(t.brojPrijavljenih);
        this.ucitavanje.set(false);
      },
      error: (err) => {
        this.porukaService.greska(err);
        this.router.navigate(['/profesor/termini']);
      },
    });
  }

  sacuvaj(): void {
    const greska = this.proveri();
    if (greska) {
      this.greska.set(greska);
      return;
    }
    this.greska.set('');
    this.slanje.set(true);

    const pocetak = `${this.datum}T${this.vreme}:00`;

    if (this.terminId === null) {
      // ----- novi termin -----
      this.terminService.create({
        pocetak,
        trajanjeMin: this.trajanjeMin,
        mesto: this.mesto.trim(),
        kapacitet: this.kapacitet,
        predmetId: this.predmetId,
        ponavljanjeDo: this.ponavljaj ? this.ponavljanjeDo : null,
      }).subscribe({
        next: (napravljeni) => {
          this.porukaService.uspeh(napravljeni.length > 1
            ? `Napravljeno je ${napravljeni.length} termina.`
            : 'Termin je napravljen.');
          this.router.navigate(['/profesor/termini']);
        },
        error: (err) => this.neuspeh(err),
      });
    } else {
      // ----- izmena -----
      this.terminService.update(this.terminId, {
        pocetak,
        trajanjeMin: this.trajanjeMin,
        mesto: this.mesto.trim(),
        kapacitet: this.kapacitet,
      }).subscribe({
        next: () => {
          this.porukaService.uspeh('Izmene su sačuvane.');
          this.router.navigate(['/profesor/termini']);
        },
        error: (err) => this.neuspeh(err),
      });
    }
  }

  // Osnovne provere pre slanja (sve iste provere radi i backend)
  private proveri(): string {
    if (!this.datum || !this.vreme) {
      return 'Unesite datum i vreme termina.';
    }
    if (new Date(`${this.datum}T${this.vreme}`) <= new Date()) {
      return 'Termin mora biti u budućnosti.';
    }
    if (!this.trajanjeMin || this.trajanjeMin < 10 || this.trajanjeMin > 240) {
      return 'Trajanje mora biti između 10 i 240 minuta.';
    }
    if (this.mesto.trim() === '') {
      return 'Unesite mesto: kabinet ili link za onlajn sastanak.';
    }
    if (!this.kapacitet || this.kapacitet < 1 || this.kapacitet > 100) {
      return 'Kapacitet mora biti između 1 i 100.';
    }
    if (this.kapacitet < this.brojPrijavljenih()) {
      return `Kapacitet ne može biti manji od broja prijavljenih studenata (${this.brojPrijavljenih()}).`;
    }
    if (this.ponavljaj && !this.ponavljanjeDo) {
      return 'Izaberite datum do kog se termin ponavlja.';
    }
    if (this.ponavljaj && this.ponavljanjeDo < this.datum) {
      return 'Datum ponavljanja mora biti posle prvog termina.';
    }
    return '';
  }

  // Greška sa backenda (npr. preklapanje sa drugim terminom) ostaje u formi
  private neuspeh(err: HttpErrorResponse): void {
    this.slanje.set(false);
    this.greska.set(tekstGreske(err));
  }

}
