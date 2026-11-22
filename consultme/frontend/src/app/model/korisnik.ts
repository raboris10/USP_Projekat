// Odgovor backenda na uspešnu prijavu (LoginResponse.java)
export interface LoginOdgovor {
  token: string;   // JWT token - šalje se uz svaki sledeći zahtev
  email: string;
  ime: string;
  prezime: string;
  uloga: string;   // STUDENT, PROFESOR ili ADMIN
}

// Podaci o korisniku (KorisnikDto.java) - nikad ne sadrže lozinku
export interface Korisnik {
  id: number;
  ime: string;
  prezime: string;
  email: string;
  uloga: string;
}

// Ono što pamtimo o prijavljenom korisniku u browseru (bez tokena)
export interface PrijavljeniKorisnik {
  email: string;
  ime: string;
  prezime: string;
  uloga: string;
}

// Korisnik kako ga vidi administrator (KorisnikAdminDto.java)
export interface KorisnikAdmin {
  id: number;
  ime: string;
  prezime: string;
  email: string;
  uloga: string;      // STUDENT, PROFESOR ili ADMIN
  aktivan: boolean;
}

// Telo zahteva za novog korisnika / izmenu (KorisnikRequest / KorisnikIzmenaRequest.java)
// lozinka se šalje samo kod novog korisnika
export interface KorisnikZahtev {
  ime: string;
  prezime: string;
  email: string;
  uloga: string;
  lozinka?: string;
}
