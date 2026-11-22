import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Korisnik } from '../model/korisnik';
import { Predmet } from '../model/predmet';

// "Šifarnici" = liste za padajuće menije (filtere i forme): profesori i predmeti.
// Pregled je dostupan svim prijavljenim korisnicima.
@Injectable({
  providedIn: 'root',
})
export class SifarnikService {

  private http = inject(HttpClient);

  // GET /api/korisnici/profesori
  profesori(): Observable<Korisnik[]> {
    return this.http.get<Korisnik[]>('/api/korisnici/profesori');
  }

  // GET /api/predmeti
  predmeti(): Observable<Predmet[]> {
    return this.http.get<Predmet[]>('/api/predmeti');
  }

}
