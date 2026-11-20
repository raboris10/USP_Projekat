import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

// Glavna komponenta - samo prikazuje stranu koja odgovara trenutnoj ruti
@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {}
