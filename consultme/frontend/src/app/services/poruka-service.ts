import { HttpErrorResponse } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';

export interface Poruka {
  id: number;
  tekst: string;
  vrsta: 'uspeh' | 'greska';
}

// Kratka obaveštenja u uglu ekrana ("Termin je rezervisan", "Termin je popunjen").
// Bilo koja komponenta pozove uspeh(...) ili greska(...), a komponenta Poruke ih prikazuje.
@Injectable({
  providedIn: 'root',
})
export class PorukaService {

  poruke = signal<Poruka[]>([]);
  private sledeciId = 1;

  uspeh(tekst: string): void {
    this.dodaj(tekst, 'uspeh');
  }

  // Prima tekst ili grešku sa backenda.
  // Backend greške uvek šalje kao { "poruka": "..." } (GlobalExceptionHandler.java).
  greska(greska: string | HttpErrorResponse): void {
    const tekst = typeof greska === 'string' ? greska : tekstGreske(greska);
    this.dodaj(tekst, 'greska');
  }

  ukloni(id: number): void {
    this.poruke.update((lista) => lista.filter((p) => p.id !== id));
  }

  private dodaj(tekst: string, vrsta: 'uspeh' | 'greska'): void {
    const id = this.sledeciId++;
    this.poruke.update((lista) => [...lista, { id, tekst, vrsta }]);
    // poruka sama nestaje posle 5 sekundi
    setTimeout(() => this.ukloni(id), 5000);
  }

}

// Izvuče čitljiv tekst iz greške backenda (koristi se i u formama)
export function tekstGreske(greska: HttpErrorResponse): string {
  if (greska.status === 0) {
    return 'Server nije dostupan. Proverite da li radi kontejner consultme-backend.';
  }
  return greska.error?.poruka ?? 'Došlo je do greške. Pokušajte ponovo.';
}
