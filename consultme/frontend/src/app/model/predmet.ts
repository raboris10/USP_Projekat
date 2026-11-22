import { Korisnik } from './korisnik';

// Predmet (PredmetDto.java)
export interface Predmet {
  id: number;
  naziv: string;
  sifra: string;
  katedraId: number;
  katedraNaziv: string;
  profesori: Korisnik[];
}

// Telo zahteva za novi predmet / izmenu (PredmetRequest.java)
export interface PredmetZahtev {
  naziv: string;
  sifra: string;
  katedraId: number;
  profesorIds: number[];
}
