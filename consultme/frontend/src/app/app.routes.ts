import { Routes } from '@angular/router';
import { AdminKatedre } from './components/admin/katedre/katedre';
import { AdminKorisnici } from './components/admin/korisnici/korisnici';
import { AdminPredmeti } from './components/admin/predmeti/predmeti';
import { Home } from './components/home/home';
import { Notifikacije } from './components/notifikacije/notifikacije';
import { TerminForma } from './components/profesor/termin-forma/termin-forma';
import { ProfesorTermini } from './components/profesor/termini/termini';
import { StudentRezervacije } from './components/student/rezervacije/rezervacije';
import { StudentTermini } from './components/student/termini/termini';
import { authGuard } from './guards/auth-guard';
import { ulogaGuard } from './guards/uloga-guard';

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
  { path: 'admin/korisnici', component: AdminKorisnici, canActivate: [ulogaGuard('ADMIN')] },
  { path: 'admin/katedre', component: AdminKatedre, canActivate: [ulogaGuard('ADMIN')] },
  { path: 'admin/predmeti', component: AdminPredmeti, canActivate: [ulogaGuard('ADMIN')] },

  { path: '**', redirectTo: '' }                                                    // nepoznato -> početna
];
