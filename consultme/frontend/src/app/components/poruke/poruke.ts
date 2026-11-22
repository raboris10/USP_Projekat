import { Component, inject } from '@angular/core';
import { PorukaService } from '../../services/poruka-service';

// Prikazuje kratka obaveštenja iz PorukaService u donjem desnom uglu.
// Postavljena je jednom, u app.html, pa radi na svim stranicama.
@Component({
  selector: 'app-poruke',
  templateUrl: './poruke.html',
  styleUrl: './poruke.css',
})
export class Poruke {

  porukaService = inject(PorukaService);

}
