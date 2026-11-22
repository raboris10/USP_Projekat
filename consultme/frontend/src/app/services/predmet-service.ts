import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Predmet, PredmetZahtev } from '../model/predmet';

// Predmeti (/api/predmeti): pregled za sve prijavljene, izmene samo administrator
@Injectable({
  providedIn: 'root',
})
export class PredmetService {

  private http = inject(HttpClient);
  private url = '/api/predmeti';

  // GET /api/predmeti ili /api/predmeti?katedraId=1
  findAll(katedraId: number | null = null): Observable<Predmet[]> {
    const parametri = katedraId ? new HttpParams().set('katedraId', katedraId) : new HttpParams();
    return this.http.get<Predmet[]>(this.url, { params: parametri });
  }

  create(zahtev: PredmetZahtev): Observable<Predmet> {
    return this.http.post<Predmet>(this.url, zahtev);
  }

  update(id: number, zahtev: PredmetZahtev): Observable<Predmet> {
    return this.http.put<Predmet>(`${this.url}/${id}`, zahtev);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

}
