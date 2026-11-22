// Termin konsultacija (TerminDto.java)
export interface Termin {
  id: number;
  pocetak: string;            // "2026-12-01T12:00:00"
  kraj: string;               // računa backend: pocetak + trajanje
  trajanjeMin: number;
  mesto: string;              // kabinet ili link za onlajn sastanak
  kapacitet: number;
  brojPrijavljenih: number;   // broj AKTIVNIH rezervacija
  slobodnihMesta: number;
  status: string;             // AKTIVAN ili OTKAZAN
  profesorId: number;
  profesorIme: string;
  profesorPrezime: string;
  predmetId: number | null;   // null -> opšte konsultacije
  predmetNaziv: string | null;
  predmetSifra: string | null;
}

// Filteri za pretragu termina (svi opcioni)
export interface TerminFilter {
  profesorId?: number | null;
  predmetId?: number | null;
  od?: string;                // "2026-12-01"
  do?: string;
}
