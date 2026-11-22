import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Poruke } from './components/poruke/poruke';
import { Zaglavlje } from './components/zaglavlje/zaglavlje';
import { AuthService } from './services/auth-service';
import { TemaService } from './services/tema-service';

// Glavna komponenta: zaglavlje (samo za prijavljene), stranica trenutne rute i poruke
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Zaglavlje, Poruke],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {

  authService = inject(AuthService);

  // TemaService se ovde samo "probudi", da primeni zapamćenu temu čim se aplikacija otvori
  private temaService = inject(TemaService);

}
