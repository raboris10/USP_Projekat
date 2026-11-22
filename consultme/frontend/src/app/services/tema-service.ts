import { Injectable, signal } from '@angular/core';

// Ključ pod kojim pamtimo izbor teme u browseru
const KLJUC_TEMA = 'consultme_tema';

export type Tema = 'svetla' | 'tamna';

// Svetla / tamna tema.
// Kako radi: na <html> elementu postavljamo atribut data-tema="tamna" ili "svetla",
// a styles.css za "tamna" daje druge vrednosti istim CSS promenljivama (boje).
@Injectable({
  providedIn: 'root',
})
export class TemaService {

  tema = signal<Tema>(this.pocetnaTema());

  constructor() {
    this.primeni(this.tema());
  }

  // Klik na prekidač u zaglavlju
  promeni(): void {
    const nova: Tema = this.tema() === 'tamna' ? 'svetla' : 'tamna';
    this.tema.set(nova);
    this.primeni(nova);
    localStorage.setItem(KLJUC_TEMA, nova); // zapamti izbor
  }

  // Prvi put (ništa nije zapamćeno) -> pratimo podešavanje sistema (Windows / telefon).
  // matchMedia pita browser: "da li korisnik u sistemu koristi tamni režim?"
  private pocetnaTema(): Tema {
    const sacuvana = localStorage.getItem(KLJUC_TEMA);
    if (sacuvana === 'svetla' || sacuvana === 'tamna') {
      return sacuvana;
    }
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'tamna' : 'svetla';
  }

  private primeni(tema: Tema): void {
    document.documentElement.setAttribute('data-tema', tema);
  }

}
