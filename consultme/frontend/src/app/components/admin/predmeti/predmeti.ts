import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Katedra } from '../../../model/katedra';
import { Korisnik } from '../../../model/korisnik';
import { Predmet } from '../../../model/predmet';
import { KatedraService } from '../../../services/katedra-service';
import { PorukaService, tekstGreske } from '../../../services/poruka-service';
import { PredmetService } from '../../../services/predmet-service';
import { SifarnikService } from '../../../services/sifarnik-service';

// Admin: predmeti (dodavanje, izmena, brisanje) i dodela profesora predmetu
@Component({
  selector: 'app-admin-predmeti',
  imports: [FormsModule],
  templateUrl: './predmeti.html',
  styleUrl: './predmeti.css',
})
export class AdminPredmeti implements OnInit {

  private predmetService = inject(PredmetService);
  private katedraService = inject(KatedraService);
  private sifarnikService = inject(SifarnikService);
  private porukaService = inject(PorukaService);

  predmeti = signal<Predmet[]>([]);
  katedre = signal<Katedra[]>([]);
  profesori = signal<Korisnik[]>([]);
  ucitavanje = signal(true);

  katedraFilter: number | null = null;

  dijalog = signal<'forma' | 'brisanje' | null>(null);
  izabran = signal<Predmet | null>(null);   // null u formi -> novi predmet
  greskaDijaloga = signal('');
  slanje = signal(false);

  // Polja forme
  naziv = '';
  sifra = '';
  katedraId: number | null = null;
  izabraniProfesori = new Set<number>();   // id-jevi označenih profesora

  ngOnInit(): void {
    this.katedraService.findAll().subscribe({ next: (lista) => this.katedre.set(lista) });
    this.sifarnikService.profesori().subscribe({ next: (lista) => this.profesori.set(lista) });
    this.ucitaj();
  }

  ucitaj(): void {
    this.predmetService.findAll(this.katedraFilter).subscribe({
      next: (lista) => {
        this.predmeti.set(lista);
        this.ucitavanje.set(false);
      },
      error: (err) => {
        this.ucitavanje.set(false);
        this.porukaService.greska(err);
      },
    });
  }

  imenaProfesora(predmet: Predmet): string {
    return predmet.profesori.map((p) => `${p.ime} ${p.prezime}`).join(', ');
  }

  otvoriFormu(predmet: Predmet | null): void {
    this.izabran.set(predmet);
    this.naziv = predmet?.naziv ?? '';
    this.sifra = predmet?.sifra ?? '';
    this.katedraId = predmet?.katedraId ?? this.katedre()[0]?.id ?? null;
    this.izabraniProfesori = new Set(predmet?.profesori.map((p) => p.id) ?? []);
    this.otvori('forma');
  }

  otvoriBrisanje(predmet: Predmet): void {
    this.izabran.set(predmet);
    this.otvori('brisanje');
  }

  zatvori(): void {
    this.dijalog.set(null);
  }

  // Kvačica pored profesora u formi
  promeniProfesora(id: number, oznacen: boolean): void {
    if (oznacen) {
      this.izabraniProfesori.add(id);
    } else {
      this.izabraniProfesori.delete(id);
    }
  }

  sacuvaj(): void {
    if (!this.naziv.trim() || !this.sifra.trim() || !this.katedraId) {
      this.greskaDijaloga.set('Popunite naziv, šifru i katedru.');
      return;
    }

    const izmena = this.izabran();
    const zahtev = {
      naziv: this.naziv.trim(),
      sifra: this.sifra.trim(),
      katedraId: this.katedraId,
      profesorIds: [...this.izabraniProfesori],   // Set -> niz
    };

    this.slanje.set(true);
    const zahtevKaBackendu = izmena
      ? this.predmetService.update(izmena.id, zahtev)
      : this.predmetService.create(zahtev);

    zahtevKaBackendu.subscribe({
      next: () => {
        this.zatvori();
        this.porukaService.uspeh(izmena ? 'Izmene su sačuvane.' : 'Predmet je dodat.');
        this.ucitaj();
      },
      error: (err) => this.neuspeh(err),
    });
  }

  potvrdiBrisanje(): void {
    const predmet = this.izabran();
    if (!predmet) return;

    this.slanje.set(true);
    this.predmetService.delete(predmet.id).subscribe({
      next: () => {
        this.zatvori();
        this.porukaService.uspeh('Predmet je obrisan.');
        this.ucitaj();
      },
      // npr. "Predmet ne može da se obriše jer za njega postoje termini."
      error: (err) => this.neuspeh(err),
    });
  }

  private otvori(dijalog: 'forma' | 'brisanje'): void {
    this.greskaDijaloga.set('');
    this.slanje.set(false);
    this.dijalog.set(dijalog);
  }

  private neuspeh(err: HttpErrorResponse): void {
    this.slanje.set(false);
    this.greskaDijaloga.set(tekstGreske(err));
  }

}
