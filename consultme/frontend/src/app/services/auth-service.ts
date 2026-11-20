import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { Korisnik, LoginOdgovor, PrijavljeniKorisnik } from '../model/korisnik';

// Ključevi pod kojima čuvamo podatke u localStorage-u
const KLJUC_TOKEN = 'consultme_token';
const KLJUC_KORISNIK = 'consultme_korisnik';

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  private http = inject(HttpClient);

  // Relativne adrese - proxy.conf.json ih prosleđuje Spring backendu
  private authUrl = '/api/auth';
  private korisniciUrl = '/api/korisnici';

  // Podaci o prijavljenom korisniku; null ako niko nije prijavljen.
  // Signal -> svaka komponenta koja ga čita automatski se osveži kad se promeni.
  prijavljeniKorisnik = signal<PrijavljeniKorisnik | null>(null);

  // Pri pokretanju aplikacije proveri da li je neko već prijavljen
  // (npr. korisnik je osvežio stranicu - podaci su ostali u localStorage-u)
  constructor() {
    const sacuvan = localStorage.getItem(KLJUC_KORISNIK);
    if (sacuvan && this.getToken()) {
      this.prijavljeniKorisnik.set(JSON.parse(sacuvan)); // string -> objekat
    }
  }

  // POST /api/auth/login
  // Šalje email i lozinku. Ako backend vrati token, odmah ga sačuvamo (tap).
  // Lozinku proverava BACKEND, a ne Angular (za razliku od KVA projekta).
  prijavi(email: string, lozinka: string): Observable<LoginOdgovor> {
    return this.http.post<LoginOdgovor>(`${this.authUrl}/login`, { email, lozinka }).pipe(
      // tap -> "uradi nešto sa odgovorom usput", a odgovor ide dalje komponenti
      tap((odgovor) => {
        const korisnik: PrijavljeniKorisnik = {
          email: odgovor.email,
          ime: odgovor.ime,
          prezime: odgovor.prezime,
          uloga: odgovor.uloga,
        };
        localStorage.setItem(KLJUC_TOKEN, odgovor.token);
        localStorage.setItem(KLJUC_KORISNIK, JSON.stringify(korisnik)); // objekat -> string
        this.prijavljeniKorisnik.set(korisnik);
      })
    );
  }

  // GET /api/korisnici/ja
  // Zaštićena ruta - radi samo ako interceptor doda ispravan token.
  // Služi kao dokaz da backend prepoznaje token.
  ja(): Observable<Korisnik> {
    return this.http.get<Korisnik>(`${this.korisniciUrl}/ja`);
  }

  // Odjava - obriši sve iz browsera. Backend nema šta da briše (STATELESS, nema sesije).
  odjavi(): void {
    localStorage.removeItem(KLJUC_TOKEN);
    localStorage.removeItem(KLJUC_KORISNIK);
    this.prijavljeniKorisnik.set(null);
  }

  // Token koji interceptor dodaje uz zahteve
  getToken(): string | null {
    return localStorage.getItem(KLJUC_TOKEN);
  }

  // Da li je neko prijavljen (koristi guard)
  jePrijavljen(): boolean {
    return this.prijavljeniKorisnik() !== null;
  }

}
