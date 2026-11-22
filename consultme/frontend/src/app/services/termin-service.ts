import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Termin, TerminFilter } from '../model/termin';

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

}
