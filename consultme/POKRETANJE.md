# ConsultMe – pokretanje razvojnog okruženja

Tri kontejnera u jednom Docker Compose projektu (`consultme`):

```
Browser ──> localhost:8087 ──> [frontend] Angular dev server
                                   │  sve što počinje sa /api prosleđuje dalje (proxy.conf.json)
                                   ▼
                               [backend]  Spring Boot + Spring Security + JWT   (localhost:8088)
                                   │
                                   ▼
                               [db]       MySQL 8.4, baza consultme_db          (localhost:8089)
```

Potrebni su samo **Git** i **Docker Desktop**. Java, Node.js, Maven i MySQL ne moraju biti instalirani: sve to postoji unutar kontejnera.

## Prvo pokretanje

1. Pokrenite **Docker Desktop**.
2. Preuzmite projekat i uđite u folder `consultme`:
   ```bash
   git clone https://github.com/raboris10/USP_Projekat.git
   cd USP_Projekat/consultme
   ```
3. Napravite svoj `.env` od šablona (objašnjenje je u sledećem poglavlju):
   ```bash
   cp .env.example .env          # Git Bash
   copy .env.example .env        # Windows CMD
   ```
4. Otvorite `.env` i upišite svoje lozinke baze i JWT ključ. Ključ možete da napravite ovako, pa rezultat nalepite iza `JWT_SECRET=`:
   ```bash
   openssl rand -hex 32
   ```
5. Pokrenite sve tri kontejnera (prvi put traje nekoliko minuta):
   ```bash
   docker compose up --build
   ```
6. Sačekajte da se u logu backenda pojavi `Started ConsultmeApplication` i otvorite http://localhost:8087.

| Šta | Adresa |
|---|---|
| Aplikacija | http://localhost:8087 |
| Swagger (testiranje API-ja) | http://localhost:8088/swagger-ui.html |
| Baza (DBeaver) | `localhost:8089`, baza `consultme_db`, korisnik i lozinka iz **vašeg** `.env` (`DB_USER` / `DB_PASSWORD`) |

## Fajl `.env`

`.env` je tekstualni fajl sa podešavanjima oblika `IME=vrednost`, u folderu `consultme` (pored `docker-compose.yml`). Docker Compose ga sam pročita pri pokretanju i vrednosti prosledi kontejnerima.

**Zašto ga nema na GitHub-u:** u njemu su tajne (lozinke baze i JWT ključ), pa je naveden u `.gitignore`. Na GitHub-u je samo šablon `.env.example`, sa istim imenima i izmišljenim vrednostima. Svako pravi svoj `.env` od šablona.

> **Bez `.env` projekat ne radi.** Docker javlja `The "DB_PASSWORD" variable is not set`, baza ne dobija lozinku, a backend nema JWT ključ.

| Podešavanje | Čemu služi | Šta upisati |
|---|---|---|
| `DB_NAME` | Ime baze | Ostavite `consultme_db` |
| `DB_USER`, `DB_PASSWORD` | Nalog kojim se backend (i DBeaver) povezuje na bazu | Izmislite lozinku, važi samo za vašu lokalnu bazu |
| `DB_ROOT_PASSWORD` | Glavni (root) nalog MySQL-a | Izmislite lozinku |
| `JWT_SECRET` | Tajni ključ kojim backend potpisuje tokene | Nasumičan tekst od bar 32 znaka (`openssl rand -hex 32`) |
| `JWT_TRAJANJE_MINUTA` | Koliko dugo važi prijava | `120` |
| `DB_HOST_PORT`, `BACKEND_HOST_PORT`, `FRONTEND_HOST_PORT` | Portovi na vašem računaru | `8089`, `8088`, `8087`; menjajte samo ako je port zauzet |

### Tri različite "lozinke"

| Šta | Gde se nalazi | Ko je koristi |
|---|---|---|
| Lozinke za prijavu u aplikaciju (`admin123`, `lozinka123`...) | U bazi kao BCrypt heš, upisuje ih `db/init.sql`. **Nemaju veze sa `.env`.** | Korisnik na stranici za prijavu |
| Lozinka baze (`DB_PASSWORD`) | U vašem `.env` | Backend pri povezivanju na MySQL, i vi u DBeaver-u |
| JWT ključ (`JWT_SECRET`) | U vašem `.env` | Samo backend, za potpisivanje i proveru tokena |

Zato svako u timu može da ima drugačiji `.env`, a da se svi prijavljuju istim test nalozima.

**Da li svi moraju imati isti JWT ključ?** Ne. Svako pokreće svoj backend, a on proverava samo tokene koje je sam izdao. Ako promenite `JWT_SECRET`, postojeći tokeni prestaju da važe i treba se ponovo prijaviti.

### Zamke na Windows-u

- Fajl mora da se zove tačno `.env`. Windows ponekad sakrije ekstenziju, pa fajl iz Notepad-a postane `.env.txt`. Najsigurnije je praviti ga komandom iz koraka 3.
- Fajl mora biti u folderu `consultme`, a ne u `USP_Projekat`.
- Ako promenite lozinke baze **posle** prvog pokretanja, baza pamti stare. Pokrenite `docker compose down -v`, pa ponovo `up`.

## Nalozi za prijavu (samo za razvoj)

Na stranici za prijavu, u okviru **Test nalozi**:

| Uloga | Email | Lozinka |
|---|---|---|
| Administrator | `admin@consultme.rs` | `admin123` |
| Profesor | `zoran.zoranic@fakultet.rs` | `lozinka123` |
| Student | `ana.anic@fakultet.rs` | `lozinka123` |

U bazi postoje i ovi nalozi:

| Uloga | Email | Lozinka |
|---|---|---|
| Profesor | `maja.majic@fakultet.rs`, `nikola.nikolic@fakultet.rs` | `lozinka123` |
| Student | `bora.boric@fakultet.rs`, `ceca.cecic@fakultet.rs` | `lozinka123` |
| Profesor | `profesor@consultme.rs` | `profesor123` |
| Student | `student@consultme.rs` | `student123` |

Test podaci (`db/init.sql`): 1 katedra, 2 predmeta, 4 termina raspoređena 2 do 5 dana od dana kada je baza napravljena, 3 rezervacije i 6 notifikacija.

## Kako radi prijava (JWT)

1. Angular šalje `POST /api/auth/login` sa emailom i lozinkom.
2. Backend proverava lozinku (BCrypt) i vraća **JWT token** sa podacima o korisniku.
3. Angular čuva token u `localStorage`, a `auth-interceptor.ts` ga dodaje uz svaki zahtev: `Authorization: Bearer <token>`.
4. Backend (`JwtFilter.java`) za svaki zahtev proverava potpis i rok tokena, pa u bazi da li korisnik postoji i da li je aktivan.
5. Prava po ulogama proverava `@PreAuthorize` iznad metoda kontrolera. Odgovor **401** znači da korisnik nije prijavljen ili je prijava istekla, a **403** da njegova uloga nema pravo na to.

Token važi 120 minuta. Kad istekne (ili administrator deaktivira nalog), frontend sam vraća korisnika na prijavu.

### Testiranje u Swagger-u

1. Otvorite `POST /api/auth/login` → *Try it out* → unesite email i lozinku → *Execute*.
2. Kopirajte `token` iz odgovora.
3. Kliknite **Authorize** (gore desno), nalepite token i potvrdite.
4. Sada rade i zaštićene rute, npr. `GET /api/korisnici/me`. Za drugu ulogu ponovite postupak sa drugim nalogom.

## API

| Metod i ruta | Pristup | Opis |
|---|---|---|
| `POST /api/auth/login` | svi | Prijava, vraća token |
| `GET /api/korisnici/me` | prijavljeni | Podaci o prijavljenom korisniku |
| `GET /api/korisnici/profesori` | prijavljeni | Lista aktivnih profesora |
| `GET /api/katedre`, `/{id}` | prijavljeni | Pregled katedri |
| `POST`, `PUT /{id}`, `DELETE /{id}` na `/api/katedre` | ADMIN | Dodavanje, izmena, brisanje katedre |
| `GET /api/predmeti[?katedraId]`, `/{id}` | prijavljeni | Pregled predmeta sa katedrom i profesorima |
| `POST`, `PUT /{id}`, `DELETE /{id}` na `/api/predmeti` | ADMIN | Dodavanje, izmena, brisanje predmeta i dodela profesora |
| `GET /api/termini?profesorId&predmetId&od&do` | prijavljeni | Pretraga aktivnih budućih termina |
| `GET /api/termini/moji` | PROFESOR | Svi termini prijavljenog profesora |
| `GET /api/termini/{id}` | prijavljeni | Jedan termin |
| `GET /api/termini/{id}/prijavljeni` | PROFESOR | Prijavljeni studenti (samo za svoj termin) |
| `POST /api/termini` | PROFESOR | Novi termin; sa `ponavljanjeDo` pravi termine svake nedelje |
| `PUT /api/termini/{id}` | PROFESOR | Izmena (pomeranje) svog termina |
| `PATCH /api/termini/{id}/otkazi` | PROFESOR | Otkazivanje svog termina |
| `GET /api/rezervacije/moje` | STUDENT | Moje rezervacije |
| `POST /api/rezervacije` | STUDENT | Rezervacija termina sa temom |
| `PATCH /api/rezervacije/{id}/otkazi` | STUDENT | Otkazivanje (najkasnije 2 h pre termina) |
| `GET /api/notifikacije/moje` | prijavljeni | Moje notifikacije |
| `GET /api/notifikacije/broj-neprocitanih` | prijavljeni | Broj za zvonce |
| `PATCH /api/notifikacije/{id}/procitana`, `/procitane-sve` | prijavljeni | Označavanje kao pročitano |
| `GET /api/admin/korisnici?uloga&pretraga`, `/{id}` | ADMIN | Lista i pretraga korisnika |
| `POST`, `PUT /{id}` na `/api/admin/korisnici` | ADMIN | Dodavanje i izmena korisnika |
| `PATCH /api/admin/korisnici/{id}/lozinka` | ADMIN | Reset lozinke |
| `PATCH /api/admin/korisnici/{id}/deaktiviraj`, `/aktiviraj` | ADMIN | Deaktivacija i aktivacija naloga |

Greške backend uvek vraća u istom obliku: `{ "poruka": "..." }`.

## Model podataka

| Tabela | Veze i pravila |
|---|---|
| `korisnik` | Uloga: STUDENT, PROFESOR ili ADMIN. Email je jedinstven. Korisnici se ne brišu, nego deaktiviraju |
| `katedra` | Ima više predmeta |
| `predmet` | Pripada jednoj katedri; više-prema-više sa profesorima preko `profesor_predmet` |
| `termin` | Pripada jednom profesoru; opciono vezan za predmet; status AKTIVAN ili OTKAZAN |
| `rezervacija` | Povezuje studenta i termin; status AKTIVNA ili OTKAZANA; kolona `dolazak` za kasniju evidenciju |
| `notifikacija` | Obaveštenje za jednog korisnika; opciono vezano za termin; pročitana ili ne |

Tabele pravi isključivo `db/init.sql` (Hibernate ih ne menja, `ddl-auto=none`). Vremenska zona baze i backenda je `Europe/Belgrade`.

## Pravila koja se mogu podesiti

U `backend/src/main/resources/application.properties`:

| Podešavanje | Podrazumevano | Značenje |
|---|---|---|
| `rezervacije.najvise-aktivnih` | `3` | Najviše aktivnih rezervacija po studentu |
| `rezervacije.otkazivanje-sati-pre` | `2` | Najkasnije koliko sati pre termina student sme da otkaže |
| `podsetnici.interval-ms` | `300000` | Na koliko milisekundi se proverava slanje podsetnika (5 minuta) |

Obaveštenja se za sada upisuju u bazu, prikazuju u aplikaciji i ispisuju u log backenda kao `[NOTIFIKACIJA]` i `[PODSETNICI]`.

## Svakodnevne komande

| Situacija | Komanda |
|---|---|
| Počinjem rad | `docker compose up --build` (ili sa `-d`, da terminal ostane slobodan) |
| Da li sve radi? | `docker compose ps` (baza treba da bude `healthy`) |
| Log backenda | `docker compose logs -f backend` |
| Povukao sam izmene (`git pull`) | `docker compose up --build` |
| Promenio sam backend kod | `docker compose up --build -d backend` |
| Promenio sam frontend kod | ništa, osveži se sam |
| Promenio sam `db/init.sql` ili lozinke baze u `.env` | `docker compose down -v` pa `docker compose up --build` |
| Završio sam za danas | `docker compose down` |

`docker compose down -v` **briše bazu**: pri sledećem pokretanju `init.sql` je pravi ispočetka sa test podacima.

Za VS Code (da editor prepoznaje Angular kod) jednom pokrenite `npm ci` u folderu `frontend`.

## Česti problemi

| Problem | Uzrok i rešenje |
|---|---|
| `variable is not set`, baza se ne pokreće | Nema `.env` (ili se zove `.env.txt`, ili je u pogrešnom folderu). Napravite ga po koraku 3 |
| Baza je `unhealthy` ili backend javlja *Access denied* | Lozinke u `.env` su promenjene posle prvog pokretanja: `docker compose down -v`, pa `up --build` |
| Backend javlja da tabela ne postoji | Stara baza iz ranije verzije projekta: `docker compose down -v`, pa `up --build` |
| Prijava: "Pogrešan email ili lozinka" | Koristite naloge iz tabele iznad. Lozinke za prijavu su u `init.sql`, ne u `.env` |
| Prijava: "Server nije dostupan" | Backend se još pokreće. Sačekajte `Started ConsultmeApplication` u logu |
| *Port is already allocated* | Port je zauzet. Promenite ga u `.env` (npr. `FRONTEND_HOST_PORT=8090`) |
| Posle `git pull` nema novih izmena | Pokrenite sa `docker compose up --build` |
| Iznenada vraća na prijavu: "Prijava je istekla" | Token je istekao (120 min) ili je nalog deaktiviran. Prijavite se ponovo |

## Pokretanje backenda iz Eclipse-a (opciono)

Baza i dalje radi u Dockeru (`docker compose up -d db`), a backend se pokreće iz Eclipse-a. Podrazumevane vrednosti u `application.properties` gađaju bazu na `localhost:8089`, sa korisnikom `consultme` i lozinkom `consultme123`. Ako je u vašem `.env` drugačiji `DB_PASSWORD`, u Eclipse-u (*Run Configurations → Environment*) dodajte promenljivu `DB_PASSWORD` sa vašom lozinkom.
