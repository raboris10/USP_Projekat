import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Katedra } from '../../../model/katedra';
import { KatedraService } from '../../../services/katedra-service';
import { PorukaService, tekstGreske } from '../../../services/poruka-service';

// Admin: katedre (dodavanje, izmena, brisanje)
@Component({
  selector: 'app-admin-katedre',
  imports: [FormsModule],
  templateUrl: './katedre.html',
})
export class AdminKatedre implements OnInit {

  private katedraService = inject(KatedraService);
  private porukaService = inject(PorukaService);

  katedre = signal<Katedra[]>([]);
  ucitavanje = signal(true);

  dijalog = signal<'forma' | 'brisanje' | null>(null);
  izabrana = signal<Katedra | null>(null);   // null u formi -> nova katedra
  naziv = '';
  greskaDijaloga = signal('');
  slanje = signal(false);

  ngOnInit(): void {
    this.ucitaj();
  }

  ucitaj(): void {
    this.katedraService.findAll().subscribe({
      next: (lista) => {
        this.katedre.set(lista);
        this.ucitavanje.set(false);
      },
      error: (err) => {
        this.ucitavanje.set(false);
        this.porukaService.greska(err);
      },
    });
  }

  otvoriFormu(katedra: Katedra | null): void {
    this.izabrana.set(katedra);
    this.naziv = katedra?.naziv ?? '';
    this.otvori('forma');
  }

  otvoriBrisanje(katedra: Katedra): void {
    this.izabrana.set(katedra);
    this.otvori('brisanje');
  }

  zatvori(): void {
    this.dijalog.set(null);
  }

  sacuvaj(): void {
    if (!this.naziv.trim()) {
      this.greskaDijaloga.set('Unesite naziv katedre.');
      return;
    }

    const izmena = this.izabrana();
    this.slanje.set(true);
    const zahtev = izmena
      ? this.katedraService.update(izmena.id, this.naziv.trim())
      : this.katedraService.create(this.naziv.trim());

    zahtev.subscribe({
      next: () => {
        this.zatvori();
        this.porukaService.uspeh(izmena ? 'Izmene su sačuvane.' : 'Katedra je dodata.');
        this.ucitaj();
      },
      error: (err) => this.neuspeh(err),
    });
  }

  potvrdiBrisanje(): void {
    const katedra = this.izabrana();
    if (!katedra) return;

    this.slanje.set(true);
    this.katedraService.delete(katedra.id).subscribe({
      next: () => {
        this.zatvori();
        this.porukaService.uspeh('Katedra je obrisana.');
        this.ucitaj();
      },
      // npr. "Katedra ne može da se obriše jer ima predmete."
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
