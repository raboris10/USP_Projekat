import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

// PRIVREMENA stranica za delove koji se još prave (student, profesor, admin).
// Naslov stiže iz app.routes.ts (data: { naslov: '...' }).
// Briše se kad sve stranice budu gotove.
@Component({
  selector: 'app-u-izradi',
  template: `
    <div class="naslov-strane"><h1>{{ naslov }}</h1></div>
    <p class="prazno">Ova stranica je u izradi.</p>
  `,
})
export class UIzradi {

  naslov = inject(ActivatedRoute).snapshot.data['naslov'] ?? 'Stranica';

}
