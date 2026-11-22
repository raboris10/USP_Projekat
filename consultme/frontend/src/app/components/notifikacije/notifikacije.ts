import { DatePipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { Notifikacija } from '../../model/notifikacija';
import { NotifikacijaService } from '../../services/notifikacija-service';
import { PorukaService } from '../../services/poruka-service';

// Stranica sa svim notifikacijama prijavljenog korisnika (sve uloge)
@Component({
  selector: 'app-notifikacije',
  imports: [DatePipe],
  templateUrl: './notifikacije.html',
  styleUrl: './notifikacije.css',
})
export class Notifikacije implements OnInit {

  private notifikacijaService = inject(NotifikacijaService);
  private porukaService = inject(PorukaService);

  notifikacije = signal<Notifikacija[]>([]);
  ucitavanje = signal(true);

  imaNeprocitanih = computed(() => this.notifikacije().some((n) => !n.procitana));

  ngOnInit(): void {
    this.ucitaj();
  }

  ucitaj(): void {
    this.notifikacijaService.moje().subscribe({
      next: (lista) => {
        this.notifikacije.set(lista);
        this.ucitavanje.set(false);
      },
      error: (err) => {
        this.ucitavanje.set(false);
        this.porukaService.greska(err);
      },
    });
  }

  // Klik na nepročitanu notifikaciju -> označi je kao pročitanu
  oznaci(notifikacija: Notifikacija): void {
    if (notifikacija.procitana) {
      return;
    }
    this.notifikacijaService.oznaciKaoProcitanu(notifikacija.id).subscribe({
      // u listi zamenimo samo tu jednu notifikaciju
      next: (izmenjena) => this.notifikacije.update((lista) =>
        lista.map((n) => (n.id === izmenjena.id ? izmenjena : n))),
      error: (err) => this.porukaService.greska(err),
    });
  }

  oznaciSve(): void {
    this.notifikacijaService.oznaciSveKaoProcitane().subscribe({
      next: () => this.notifikacije.update((lista) => lista.map((n) => ({ ...n, procitana: true }))),
      error: (err) => this.porukaService.greska(err),
    });
  }

  // Kratak naziv vrste notifikacije za prikaz
  naslov(tip: string): string {
    switch (tip) {
      case 'REZERVACIJA_POTVRDJENA': return 'Rezervacija potvrđena';
      case 'NOVA_REZERVACIJA': return 'Nova rezervacija';
      case 'REZERVACIJA_OTKAZANA': return 'Rezervacija otkazana';
      case 'TERMIN_IZMENJEN': return 'Termin pomeren';
      case 'TERMIN_OTKAZAN': return 'Termin otkazan';
      case 'PODSETNIK_24H':
      case 'PODSETNIK_12H':
      case 'PODSETNIK_2H': return 'Podsetnik';
      default: return 'Obaveštenje';
    }
  }

}
