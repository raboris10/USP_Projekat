import { Component, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth-service';
import { Korisnik } from '../../model/korisnik';

@Component({
  selector: 'app-uspesna-prijava',
  imports: [],
  templateUrl: './uspesna-prijava.html',
  styleUrl: './uspesna-prijava.css',
})
export class UspesnaPrijava implements OnInit {

  private authService = inject(AuthService);
  private router = inject(Router);

  // Podaci koje je vratio backend sa zaštićene rute /api/korisnici/ja
  korisnik = signal<Korisnik | null>(null);
  greska = signal<string>('');

  // Čim se strana otvori, pitaj backend "ko sam ja?".
  // Interceptor sam dodaje token, pa ovo radi SAMO ako je prijava stvarno uspela.
  ngOnInit(): void {
    this.authService.ja().subscribe({
      next: (podaci) => this.korisnik.set(podaci),
      error: () => this.greska.set('Backend nije potvrdio prijavu.'),
    });
  }

  odjaviSe(): void {
    this.authService.odjavi();
    this.router.navigate(['/']);
  }

}
