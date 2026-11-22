import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Katedra } from '../model/katedra';

// Katedre (/api/katedre): pregled za sve prijavljene, izmene samo administrator
@Injectable({
  providedIn: 'root',
})
export class KatedraService {

  private http = inject(HttpClient);
  private url = '/api/katedre';

  findAll(): Observable<Katedra[]> {
    return this.http.get<Katedra[]>(this.url);
  }

  create(naziv: string): Observable<Katedra> {
    return this.http.post<Katedra>(this.url, { naziv });
  }

  update(id: number, naziv: string): Observable<Katedra> {
    return this.http.put<Katedra>(`${this.url}/${id}`, { naziv });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

}
