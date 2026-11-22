import { DatePipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PrijavljeniStudent, Termin } from '../../../model/termin';
import { PorukaService } from '../../../services/poruka-service';
import { TerminService } from '../../../services/termin-service';
import { DatumBlok } from '../../datum-blok/datum-blok';

type Tab = 'predstojeci' | 'prosli';

// Profesor: moji termini (predstojeći i prošli), lista prijavljenih, izmena i otkazivanje
@Component({
  selector: 'app-profesor-termini',
  imports: [DatePipe, RouterLink, DatumBlok],
  templateUrl: './termini.html',
  styleUrl: './termini.css',
})
export class ProfesorTermini implements OnInit {

  private terminService = inject(TerminService);
  private porukaService = inject(PorukaService);

  termini = signal<Termin[]>([]);
  ucitavanje = signal(true);
  tab = signal<Tab>('predstojeci');

  // Predstojeći: najbliži prvi. Prošli: najnoviji prvi.
  predstojeci = computed(() => this.termini()
    .filter((t) => new Date(t.pocetak) > new Date()));

  prosli = computed(() => this.termini()
    .filter((t) => new Date(t.pocetak) <= new Date())
    .reverse());

  prikazani = computed(() => (this.tab() === 'predstojeci' ? this.predstojeci() : this.prosli()));

  // Lista prijavljenih: koji termin je "otvoren" i učitane liste po terminu
  otvorenTerminId = signal<number | null>(null);
  prijavljeni = signal<Record<number, PrijavljeniStudent[]>>({});

  // Dijalog za otkazivanje termina
  zaOtkazivanje = signal<Termin | null>(null);
  slanje = signal(false);

  ngOnInit(): void {
    this.ucitaj();
  }

  ucitaj(): void {
    // backend vraća termine hronološki (najstariji prvi)
    this.terminService.moji().subscribe({
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

  // Klik na "Prijavljeni": otvori/zatvori listu; prvi put je učitaj sa backenda
  prikaziPrijavljene(termin: Termin): void {
    if (this.otvorenTerminId() === termin.id) {
      this.otvorenTerminId.set(null);
      return;
    }
    this.otvorenTerminId.set(termin.id);

    this.terminService.prijavljeni(termin.id).subscribe({
      next: (lista) => this.prijavljeni.update((sve) => ({ ...sve, [termin.id]: lista })),
      error: (err) => this.porukaService.greska(err),
    });
  }

  // Samo aktivne prijave (otkazane se ne prikazuju u listi)
  aktivniPrijavljeni(terminId: number): PrijavljeniStudent[] | undefined {
    return this.prijavljeni()[terminId]?.filter((p) => p.status === 'AKTIVNA');
  }

  // Termin može da se menja/otkaže samo ako je aktivan i još nije počeo
  mozeDaSeMenja(termin: Termin): boolean {
    return termin.status === 'AKTIVAN' && new Date(termin.pocetak) > new Date();
  }

  potvrdiOtkazivanje(): void {
    const termin = this.zaOtkazivanje();
    if (!termin) {
      return;
    }

    this.slanje.set(true);
    this.terminService.otkazi(termin.id).subscribe({
      next: (izmenjen) => {
        this.slanje.set(false);
        this.zaOtkazivanje.set(null);
        this.termini.update((lista) => lista.map((t) => (t.id === izmenjen.id ? izmenjen : t)));
        this.porukaService.uspeh(termin.brojPrijavljenih > 0
          ? 'Termin je otkazan. Prijavljeni studenti su obavešteni.'
          : 'Termin je otkazan.');
      },
      error: (err) => {
        this.slanje.set(false);
        this.zaOtkazivanje.set(null);
        this.porukaService.greska(err);
      },
    });
  }

  jeLink(mesto: string): boolean {
    return mesto.startsWith('http://') || mesto.startsWith('https://');
  }

}
