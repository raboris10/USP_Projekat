import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { KorisnikAdmin, KorisnikZahtev } from '../model/korisnik';

// Upravljanje korisnicima - samo administrator (/api/admin/korisnici)
@Injectable({
  providedIn: 'root',
})
export class AdminKorisnikService {

  private http = inject(HttpClient);
  private url = '/api/admin/korisnici';

  // GET /api/admin/korisnici?uloga=..&pretraga=..
  pretrazi(uloga: string | null, pretraga: string): Observable<KorisnikAdmin[]> {
    let parametri = new HttpParams();
    if (uloga) parametri = parametri.set('uloga', uloga);
    if (pretraga.trim()) parametri = parametri.set('pretraga', pretraga.trim());
    return this.http.get<KorisnikAdmin[]>(this.url, { params: parametri });
  }

  // POST /api/admin/korisnici
  create(zahtev: KorisnikZahtev): Observable<KorisnikAdmin> {
    return this.http.post<KorisnikAdmin>(this.url, zahtev);
  }

  // PUT /api/admin/korisnici/{id}
  update(id: number, zahtev: KorisnikZahtev): Observable<KorisnikAdmin> {
    return this.http.put<KorisnikAdmin>(`${this.url}/${id}`, zahtev);
  }

  // PATCH /api/admin/korisnici/{id}/lozinka
  resetujLozinku(id: number, lozinka: string): Observable<void> {
    return this.http.patch<void>(`${this.url}/${id}/lozinka`, { lozinka });
  }

  // PATCH /api/admin/korisnici/{id}/deaktiviraj
  deaktiviraj(id: number): Observable<KorisnikAdmin> {
    return this.http.patch<KorisnikAdmin>(`${this.url}/${id}/deaktiviraj`, {});
  }

  // PATCH /api/admin/korisnici/{id}/aktiviraj
  aktiviraj(id: number): Observable<KorisnikAdmin> {
    return this.http.patch<KorisnikAdmin>(`${this.url}/${id}/aktiviraj`, {});
  }

}
