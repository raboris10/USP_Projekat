import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Korisnik } from '../../../model/korisnik';
import { Predmet } from '../../../model/predmet';
import { Termin } from '../../../model/termin';
import { NotifikacijaService } from '../../../services/notifikacija-service';
import { PorukaService } from '../../../services/poruka-service';
import { RezervacijaService } from '../../../services/rezervacija-service';
import { SifarnikService } from '../../../services/sifarnik-service';
import { TerminService } from '../../../services/termin-service';
import { DatumBlok } from '../../datum-blok/datum-blok';

// Student: pretraga slobodnih termina i rezervacija
@Component({
  selector: 'app-student-termini',
  imports: [FormsModule, DatePipe, DatumBlok],
  templateUrl: './termini.html',
  styleUrl: './termini.css',
})
export class StudentTermini implements OnInit {

  private terminService = inject(TerminService);
  private rezervacijaService = inject(RezervacijaService);
  private sifarnikService = inject(SifarnikService);
  private notifikacijaService = inject(NotifikacijaService);
  private porukaService = inject(PorukaService);

  // Liste za padajuće menije
  profesori = signal<Korisnik[]>([]);
  predmeti = signal<Predmet[]>([]);

  // Filteri (povezani sa poljima preko [(ngModel)]); null / '' = nije zadato
  profesorId: number | null = null;
  predmetId: number | null = null;
  odDatuma = '';
  doDatuma = '';

  termini = signal<Termin[]>([]);
  ucitavanje = signal(true);

  // Id-jevi termina na koje je student već prijavljen -> umesto dugmeta "Rezerviši" piše "Rezervisano"
  mojiTerminIds = signal<Set<number>>(new Set());

  // Dijalog za rezervaciju: koji termin je izabran (null = dijalog zatvoren)
  izabraniTermin = signal<Termin | null>(null);
  tema = '';
  greskaDijaloga = signal('');
  slanje = signal(false);

  ngOnInit(): void {
    this.sifarnikService.profesori().subscribe({ next: (lista) => this.profesori.set(lista) });
    this.sifarnikService.predmeti().subscribe({ next: (lista) => this.predmeti.set(lista) });
    this.pretrazi();
    this.ucitajMojeRezervacije();
  }

  // Poziva se pri svakoj promeni filtera
  pretrazi(): void {
    this.ucitavanje.set(true);
    this.terminService.pretrazi({
      profesorId: this.profesorId,
      predmetId: this.predmetId,
      od: this.odDatuma,
      do: this.doDatuma,
    }).subscribe({
      next: (lista) => {
        this.termini.set(lista);
        this.ucitavanje.set(false);
      },
      error: (err) => {
        this.ucitavanje.set(false);
        this.porukaService.greska(err);
      },
    });
  }

  ponistiFiltere(): void {
    this.profesorId = null;
    this.predmetId = null;
    this.odDatuma = '';
    this.doDatuma = '';
    this.pretrazi();
  }

  imaZadatihFiltera(): boolean {
    return !!(this.profesorId || this.predmetId || this.odDatuma || this.doDatuma);
  }

  // ----- Dijalog za rezervaciju -----

  otvoriDijalog(termin: Termin): void {
    this.izabraniTermin.set(termin);
    this.tema = '';
    this.greskaDijaloga.set('');
  }

  zatvoriDijalog(): void {
    this.izabraniTermin.set(null);
  }

  rezervisi(): void {
    const termin = this.izabraniTermin();
    if (!termin) {
      return;
    }
    if (this.tema.trim() === '') {
      this.greskaDijaloga.set('Ukratko napišite temu konsultacija.');
      return;
    }

    this.slanje.set(true);
    this.rezervacijaService.rezervisi(termin.id, this.tema.trim()).subscribe({
      next: () => {
        this.slanje.set(false);
        this.zatvoriDijalog();
        this.porukaService.uspeh('Termin je rezervisan.');
        // osveži listu (broj slobodnih mesta), oznake "Rezervisano" i zvonce (stigla je potvrda)
        this.pretrazi();
        this.ucitajMojeRezervacije();
        this.notifikacijaService.osveziBroj();
      },
      error: (err) => {
        this.slanje.set(false);
        // greška ostaje u dijalogu, npr. "Termin je popunjen."
        this.greskaDijaloga.set(err.error?.poruka ?? 'Rezervacija nije uspela.');
      },
    });
  }

  // ----- Pomoćno za prikaz -----

  jeLink(mesto: string): boolean {
    return mesto.startsWith('http://') || mesto.startsWith('https://');
  }

  private ucitajMojeRezervacije(): void {
    this.rezervacijaService.moje().subscribe({
      next: (lista) => this.mojiTerminIds.set(new Set(
        lista.filter((r) => r.status === 'AKTIVNA').map((r) => r.termin.id))),
    });
  }

}
