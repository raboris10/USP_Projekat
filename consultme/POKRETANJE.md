# ConsultMe – pokretanje razvojnog okruženja

Tri kontejnera u jednom Docker Compose projektu (`consultme`):

```
Browser ──> localhost:8087 ──> [frontend] Angular dev server
                                   │  /api/... prosleđuje (proxy.conf.json)
                                   ▼
                               [backend]  Spring Boot + Spring Security + JWT
                                   │
                                   ▼
                               [db]       MySQL 8.4 (consultme_db)
```

## Pokretanje

1. Pokrenite **Docker Desktop**.
2. Samo prvi put, u korenu projekta: `copy .env.example .env`
3. `docker compose up --build -d`
4. Proverite da sve radi: `docker compose ps` (baza treba da bude `healthy`)

| Šta | Adresa |
|---|---|
| Aplikacija | http://localhost:8087 |
| Swagger | http://localhost:8088/swagger-ui.html |
| Baza (DBeaver) | `localhost:8089`, baza `consultme_db`, korisnik `consultme` / `consultme123` |

## Podrazumevani nalozi (samo za razvoj)

Test nalozi (domen `@consultme.rs`):

| Uloga | Email | Lozinka |
|---|---|---|
| Administrator (IT tehničar) | admin@consultme.rs | admin123 |
| Profesor | profesor@consultme.rs | profesor123 |
| Student | student@consultme.rs | student123 |

"Fakultetski" nalozi (simulirani domen `@fakultet.rs`), svi sa lozinkom `lozinka123`:

| Uloga | Email |
|---|---|
| Profesor | zoran.zoranic@fakultet.rs |
| Profesor | maja.majic@fakultet.rs |
| Profesor | nikola.nikolic@fakultet.rs |
| Student | ana.anic@fakultet.rs |
| Student | bora.boric@fakultet.rs |
| Student | ceca.cecic@fakultet.rs |

U bazi su lozinke sačuvane kao BCrypt heševi.

## Kako radi prijava (JWT)

1. Angular šalje `POST /api/auth/login` sa emailom i lozinkom.
2. Backend proverava lozinku (BCrypt) i vraća **JWT token**.
3. Angular čuva token u `localStorage`, a `auth-interceptor.ts` ga dodaje uz svaki zahtev:
   `Authorization: Bearer <token>`.
4. Backend (`JwtFilter.java`) proverava token za svaki zahtev. Bez ispravnog tokena zaštićene rute vraćaju 401.

### Testiranje u Swagger-u

1. Otvorite `POST /api/auth/login` → *Try it out* → unesite email i lozinku → *Execute*.
2. Kopirajte `token` iz odgovora.
3. Kliknite **Authorize** (gore desno), nalepite token, potvrdite.
4. Sada radi i zaštićena ruta `GET /api/korisnici/ja`.

## API

| Metod | Ruta | Pristup | Opis |
|---|---|---|---|
| POST | `/api/auth/login` | svi | prijava, vraća token |
| GET | `/api/korisnici/ja` | prijavljeni | podaci o prijavljenom korisniku |

## Model podataka

| Tabela | Veze |
|---|---|
| `korisnik` | uloga: STUDENT, PROFESOR ili ADMIN |
| `katedra` | ima više predmeta |
| `predmet` | pripada jednoj katedri; M:N sa profesorima preko `profesor_predmet` |
| `termin` | pripada jednom profesoru; opciono vezan za predmet |
| `rezervacija` | povezuje studenta i termin; status i evidencija dolaska |

## Svakodnevne komande

| Situacija | Komanda |
|---|---|
| Počinjem rad | `docker compose up --build -d` |
| Da li sve radi? | `docker compose ps` |
| Log backenda | `docker compose logs -f backend` |
| Promenio sam backend kod | `docker compose up --build -d backend` |
| Promenio sam frontend kod | ništa, osveži se sam |
| Promenio sam `db/init.sql` | `docker compose down -v` pa `docker compose up --build -d` |
| Završio sam za danas | `docker compose down` |

Za VS Code (da editor prepoznaje Angular kod) jednom pokrenite `npm ci` u folderu `frontend`.
