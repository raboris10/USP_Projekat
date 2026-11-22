import { DatePipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { Rezervacija } from '../../../model/rezervacija';
import { PorukaService } from '../../../services/poruka-service';
import { RezervacijaService } from '../../../services/rezervacija-service';
import { DatumBlok } from '../../datum-blok/datum-blok';

type Tab = 'predstojece' | 'prosle';

// Student: moje rezervacije, podeljene na predstojeće i prošle (po vremenu termina)
@Component({
  selector: 'app-student-rezervacije',
  imports: [DatePipe, DatumBlok],
  templateUrl: './rezervacije.html',
  styleUrl: './rezervacije.css',
})
export class StudentRezervacije implements OnInit {

  private rezervacijaService = inject(RezervacijaService);
  private porukaService = inject(PorukaService);

  rezervacije = signal<Rezervacija[]>([]);
  ucitavanje = signal(true);
  tab = signal<Tab>('predstojece');

  // Predstojeće: najbliži termin prvi. Prošle: najnoviji prvi (tako ih vraća backend).
  // computed -> automatski se preračuna kad se promeni lista rezervacija
  predstojece = computed(() => this.rezervacije()
    .filter((r) => new Date(r.termin.pocetak) > new Date())
    .sort((a, b) => a.termin.pocetak.localeCompare(b.termin.pocetak)));

  prosle = computed(() => this.rezervacije()
    .filter((r) => new Date(r.termin.pocetak) <= new Date()));

  prikazane = computed(() => (this.tab() === 'predstojece' ? this.predstojece() : this.prosle()));

  // Dijalog za potvrdu otkazivanja (null = zatvoren)
  zaOtkazivanje = signal<Rezervacija | null>(null);
  slanje = signal(false);

  ngOnInit(): void {
    this.ucitaj();
  }

  ucitaj(): void {
    this.rezervacijaService.moje().subscribe({
      next: (lista) => {
        this.rezervacije.set(lista);
        this.ucitavanje.set(false);
      },
      error: (err) => {
        this.ucitavanje.set(false);
        this.porukaService.greska(err);
      },
    });
  }

  potvrdiOtkazivanje(): void {
    const rezervacija = this.zaOtkazivanje();
    if (!rezervacija) {
      return;
    }

    this.slanje.set(true);
    this.rezervacijaService.otkazi(rezervacija.id).subscribe({
      next: (izmenjena) => {
        this.slanje.set(false);
        this.zaOtkazivanje.set(null);
        // u listi zamenimo samo tu rezervaciju (sada je OTKAZANA)
        this.rezervacije.update((lista) => lista.map((r) => (r.id === izmenjena.id ? izmenjena : r)));
        this.porukaService.uspeh('Rezervacija je otkazana.');
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
