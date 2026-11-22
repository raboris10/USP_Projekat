import { Routes } from '@angular/router';
import { Home } from './components/home/home';
import { Notifikacije } from './components/notifikacije/notifikacije';
import { UIzradi } from './components/u-izradi/u-izradi';
import { authGuard } from './guards/auth-guard';
import { ulogaGuard } from './guards/uloga-guard';

// UIzradi je privremena stranica; zamenjuje se pravim stranicama u sledećim koracima.
export const routes: Routes = [
  { path: '', component: Home },                                                    // prijava

  // Sve uloge
  { path: 'notifikacije', component: Notifikacije, canActivate: [authGuard] },

  // Student
  { path: 'student/termini', component: UIzradi, canActivate: [ulogaGuard('STUDENT')], data: { naslov: 'Termini' } },
  { path: 'student/rezervacije', component: UIzradi, canActivate: [ulogaGuard('STUDENT')], data: { naslov: 'Moje rezervacije' } },

  // Profesor
  { path: 'profesor/termini', component: UIzradi, canActivate: [ulogaGuard('PROFESOR')], data: { naslov: 'Moji termini' } },
  { path: 'profesor/termini/novi', component: UIzradi, canActivate: [ulogaGuard('PROFESOR')], data: { naslov: 'Novi termin' } },

  // Admin
  { path: 'admin/korisnici', component: UIzradi, canActivate: [ulogaGuard('ADMIN')], data: { naslov: 'Korisnici' } },
  { path: 'admin/katedre', component: UIzradi, canActivate: [ulogaGuard('ADMIN')], data: { naslov: 'Katedre' } },
  { path: 'admin/predmeti', component: UIzradi, canActivate: [ulogaGuard('ADMIN')], data: { naslov: 'Predmeti' } },

  { path: '**', redirectTo: '' }                                                    // nepoznato -> početna
];
