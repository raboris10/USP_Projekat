import { Termin } from './termin';

// Rezervacija kako je vidi student (RezervacijaDto.java)
export interface Rezervacija {
  id: number;
  tema: string;
  status: string;             // AKTIVNA ili OTKAZANA
  dolazak: string | null;
  kreirana: string;
  mozeDaSeOtkaze: boolean;    // backend već proveri rok od 2 sata
  termin: Termin;
}
