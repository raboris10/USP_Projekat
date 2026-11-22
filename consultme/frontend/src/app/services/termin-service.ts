import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PrijavljeniStudent, Termin, TerminFilter, TerminIzmena, TerminZahtev } from '../model/termin';

// Termini konsultacija (/api/termini)
@Injectable({
  providedIn: 'root',
})
export class TerminService {

  private http = inject(HttpClient);
  private url = '/api/termini';

  // GET /api/termini?profesorId=..&predmetId=..&od=..&do=..
  // U adresu idu samo filteri koji su zadati.
  pretrazi(filter: TerminFilter): Observable<Termin[]> {
    let parametri = new HttpParams();
    if (filter.profesorId) parametri = parametri.set('profesorId', filter.profesorId);
    if (filter.predmetId) parametri = parametri.set('predmetId', filter.predmetId);
    if (filter.od) parametri = parametri.set('od', filter.od);
    if (filter.do) parametri = parametri.set('do', filter.do);

    return this.http.get<Termin[]>(this.url, { params: parametri });
  }

  // ----- Profesor -----

  // GET /api/termini/moji
  moji(): Observable<Termin[]> {
    return this.http.get<Termin[]>(`${this.url}/moji`);
  }

  // GET /api/termini/{id}
  findById(id: number): Observable<Termin> {
    return this.http.get<Termin>(`${this.url}/${id}`);
  }

  // GET /api/termini/{id}/prijavljeni
  prijavljeni(id: number): Observable<PrijavljeniStudent[]> {
    return this.http.get<PrijavljeniStudent[]>(`${this.url}/${id}/prijavljeni`);
  }

  // POST /api/termini -> vraća listu (ponavljajući termin pravi više termina)
  create(zahtev: TerminZahtev): Observable<Termin[]> {
    return this.http.post<Termin[]>(this.url, zahtev);
  }

  // PUT /api/termini/{id}
  update(id: number, izmena: TerminIzmena): Observable<Termin> {
    return this.http.put<Termin>(`${this.url}/${id}`, izmena);
  }

  // PATCH /api/termini/{id}/otkazi
  otkazi(id: number): Observable<Termin> {
    return this.http.patch<Termin>(`${this.url}/${id}/otkazi`, {});
  }

}
