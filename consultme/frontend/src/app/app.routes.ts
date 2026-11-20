import { Routes } from '@angular/router';
import { Home } from './components/home/home';
import { UspesnaPrijava } from './components/uspesna-prijava/uspesna-prijava';
import { authGuard } from './guards/auth-guard';

export const routes: Routes = [
  { path: '', component: Home },                                                           // početna sa prijavom
  { path: 'prijava-uspesna', component: UspesnaPrijava, canActivate: [authGuard] },       // samo za prijavljene
  { path: '**', redirectTo: '' }                                                           // nepoznato -> početna
];
