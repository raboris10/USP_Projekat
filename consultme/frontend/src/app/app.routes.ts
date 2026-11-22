import { Routes } from '@angular/router';
import { Home } from './components/home/home';
import { Notifikacije } from './components/notifikacije/notifikacije';
import { TerminForma } from './components/profesor/termin-forma/termin-forma';
import { ProfesorTermini } from './components/profesor/termini/termini';
import { StudentRezervacije } from './components/student/rezervacije/rezervacije';
import { StudentTermini } from './components/student/termini/termini';
import { UIzradi } from './components/u-izradi/u-izradi';
import { authGuard } from './guards/auth-guard';
import { ulogaGuard } from './guards/uloga-guard';

// UIzradi je privremena stranica; zamenjuje se pravim stranicama u sledećim koracima.
export const routes: Routes = [
  { path: '', component: Home },                                                    // prijava

  // Sve uloge
  { path: 'notifikacije', component: Notifikacije, canActivate: [authGuard] },

  // Student
  { path: 'student/termini', component: StudentTermini, canActivate: [ulogaGuard('STUDENT')] },
  { path: 'student/rezervacije', component: StudentRezervacije, canActivate: [ulogaGuard('STUDENT')] },

  // Profesor
  { path: 'profesor/termini', component: ProfesorTermini, canActivate: [ulogaGuard('PROFESOR')] },
  { path: 'profesor/termini/novi', component: TerminForma, canActivate: [ulogaGuard('PROFESOR')] },
  { path: 'profesor/termini/:id/izmena', component: TerminForma, canActivate: [ulogaGuard('PROFESOR')] },

  // Admin
  { path: 'admin/korisnici', component: UIzradi, canActivate: [ulogaGuard('ADMIN')], data: { naslov: 'Korisnici' } },
  { path: 'admin/katedre', component: UIzradi, canActivate: [ulogaGuard('ADMIN')], data: { naslov: 'Katedre' } },
  { path: 'admin/predmeti', component: UIzradi, canActivate: [ulogaGuard('ADMIN')], data: { naslov: 'Predmeti' } },

  { path: '**', redirectTo: '' }                                                    // nepoznato -> početna
];
