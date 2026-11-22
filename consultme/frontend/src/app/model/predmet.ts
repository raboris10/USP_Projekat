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
