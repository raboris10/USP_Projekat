import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { Notifikacija } from '../model/notifikacija';

// Notifikacije prijavljenog korisnika (/api/notifikacije)
@Injectable({
  providedIn: 'root',
})
export class NotifikacijaService {

  private http = inject(HttpClient);
  private url = '/api/notifikacije';

  // Broj na "zvoncu" u zaglavlju. Signal -> zaglavlje se samo osveži kad se promeni.
  brojNeprocitanih = signal<number>(0);

  // GET /api/notifikacije/moje
  moje(): Observable<Notifikacija[]> {
    return this.http.get<Notifikacija[]>(`${this.url}/moje`);
  }

  // GET /api/notifikacije/broj-neprocitanih -> { "broj": 3 }
  osveziBroj(): void {
    this.http.get<{ broj: number }>(`${this.url}/broj-neprocitanih`).subscribe({
      next: (odgovor) => this.brojNeprocitanih.set(odgovor.broj),
      error: () => {}, // ako ne uspe, zvonce samo ostane kakvo je
    });
  }

  // PATCH /api/notifikacije/{id}/procitana
  oznaciKaoProcitanu(id: number): Observable<Notifikacija> {
    return this.http.patch<Notifikacija>(`${this.url}/${id}/procitana`, {}).pipe(
      tap(() => this.brojNeprocitanih.update((broj) => Math.max(0, broj - 1)))
    );
  }

  // PATCH /api/notifikacije/procitane-sve
  oznaciSveKaoProcitane(): Observable<{ oznaceno: number }> {
    return this.http.patch<{ oznaceno: number }>(`${this.url}/procitane-sve`, {}).pipe(
      tap(() => this.brojNeprocitanih.set(0))
    );
  }

}
