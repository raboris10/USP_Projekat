import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Rezervacija } from '../model/rezervacija';

// Rezervacije studenta (/api/rezervacije)
@Injectable({
  providedIn: 'root',
})
export class RezervacijaService {

  private http = inject(HttpClient);
  private url = '/api/rezervacije';

  // GET /api/rezervacije/moje
  moje(): Observable<Rezervacija[]> {
    return this.http.get<Rezervacija[]>(`${this.url}/moje`);
  }

  // POST /api/rezervacije  { terminId, tema }
  rezervisi(terminId: number, tema: string): Observable<Rezervacija> {
    return this.http.post<Rezervacija>(this.url, { terminId, tema });
  }

  // PATCH /api/rezervacije/{id}/otkazi
  otkazi(id: number): Observable<Rezervacija> {
    return this.http.patch<Rezervacija>(`${this.url}/${id}/otkazi`, {});
  }

}
