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

// Telo zahteva za novi termin (TerminRequest.java)
export interface TerminZahtev {
  pocetak: string;              // "2026-12-01T12:00:00"
  trajanjeMin: number;
  mesto: string;
  kapacitet: number;
  predmetId: number | null;
  ponavljanjeDo: string | null; // "2027-01-26" ili null (bez ponavljanja)
}

// Telo zahteva za izmenu termina (TerminIzmenaRequest.java) - bez predmeta i ponavljanja
export interface TerminIzmena {
  pocetak: string;
  trajanjeMin: number;
  mesto: string;
  kapacitet: number;
}

// Student prijavljen na termin, kako ga vidi profesor (PrijavljeniStudentDto.java)
export interface PrijavljeniStudent {
  rezervacijaId: number;
  studentId: number;
  ime: string;
  prezime: string;
  email: string;
  tema: string;
  status: string;               // AKTIVNA ili OTKAZANA
  dolazak: string | null;
  kreirana: string;
}
