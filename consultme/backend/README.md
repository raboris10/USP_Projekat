# ConsultMe – backend

Spring Boot 3 (Java 17+), Maven, Spring Data JPA, Spring Security + JWT, MySQL (H2 za brzi test).
Stil koda isti kao u projektu `projekatPrakse` (`@Autowired`, `ResponseEntity.status(...)`, entiteti u kontrolerima).

## Arhitektura

    consultme
    ├── model        entiteti (JPA) i enumi (klasni dijagram)
    ├── repository   JpaRepository interfejsi, rade sa bazom
    ├── service      poslovna logika, zovu repository
    ├── controller   REST endpointi, zovu service
    ├── security     JWT (JwtUtil, JwtAuthFilter) i SecurityConfig
    ├── exception    ConsultMeException + GlobalExceptionHandler
    └── config       DataInitializer (prvi admin i demo podaci)

Tok: `controller -> service -> repository -> baza`.

## Pokretanje

Brzo, bez MySQL-a (H2 u memoriji + demo podaci):

    mvn spring-boot:run -Dspring-boot.run.profiles=h2

Sa MySQL-om: podesi `spring.datasource.*` u `application.properties`, pa `mvn spring-boot:run`.
Pre prave upotrebe promeni `consultme.jwt.secret` (najmanje 32 karaktera).

## Autentifikacija (JWT)

Nema registracije. Admin dodaje studente i nastavnike, a oni se prijavljuju:

    POST /api/auth/login   {"email": "...", "lozinka": "..."}
    -> {"token": "...", "tip": "Bearer", "istice_za_sekundi": 7200, "korisnik": {...}}

Token se salje u svakom zahtevu: `Authorization: Bearer <token>`.
Uloga se cita iz baze pri svakom zahtevu, pa obrisan korisnik ili promenjena uloga vaze odmah.

| Uloga | Email | Lozinka |
|-------|-------|---------|
| ADMIN | admin@consultme.rs | admin123 (pravi se pri pokretanju) |
| NASTAVNIK (demo) | nastavnik@consultme.rs | nastavnik123 |
| STUDENT (demo) | student@consultme.rs | student123 |

## Endpointi

| Metoda | Putanja | Ko | Opis |
|--------|---------|----|------|
| POST | /api/auth/login | javno | prijava, vraca JWT |
| GET | /api/auth/me | prijavljen | podaci o prijavljenom korisniku |
| GET, POST, PUT, DELETE | /api/studenti | ADMIN | CRUD studenata (POST: lozinka obavezna, PUT: opciona) |
| GET | /api/nastavnici, /{id}, /katedra/{id}, /pretraga?ime= | prijavljen | pregled i pretraga nastavnika |
| POST | /api/nastavnici/katedra/{katedraId} | ADMIN | dodavanje nastavnika |
| PUT, DELETE | /api/nastavnici/{id} | ADMIN | izmena i brisanje |
| PATCH | /api/nastavnici/{id}/katedra/{katedraId} | ADMIN | prebacivanje na drugu katedru |
| GET | /api/katedre, /api/predmeti | prijavljen | pregled |
| POST, PUT, DELETE | /api/katedre, /api/predmeti/katedra/{katedraId} | ADMIN | CRUD katedri i predmeta |
| GET | /api/termini?nastavnikId=&predmetId=&od=&do= | prijavljen | pretraga termina |
| GET | /api/termini/{id} | prijavljen | jedan termin |
| GET | /api/termini/moji | NASTAVNIK | moji termini |
| POST | /api/termini | NASTAVNIK | definisi termin |
| PATCH | /api/termini/{id}/pomeri, /otkazi | NASTAVNIK | pomeri / otkazi termin |
| GET | /api/termini/{id}/rezervacije | NASTAVNIK | rezervacije termina |
| GET | /api/termini/{id}/evidencija/csv | NASTAVNIK | izvoz evidencije u CSV |
| POST | /api/rezervacije | STUDENT | rezervisi termin |
| GET | /api/rezervacije/moje | STUDENT | moje rezervacije |
| PATCH | /api/rezervacije/{id}/otkazi | STUDENT | otkazi rezervaciju |
| PATCH | /api/rezervacije/{id}/evidencija | NASTAVNIK | oznaci dolazak |
| GET | /api/notifikacije/moje | STUDENT | moje notifikacije |

## Primeri (JSON)

Admin dodaje nastavnika (`POST /api/nastavnici/katedra/1`):

    {"ime": "Jovan", "prezime": "Jovic", "email": "jovan@fakultet.rs", "lozinka": "tajna123", "zvanje": "Profesor"}

Admin dodaje studenta (`POST /api/studenti`):

    {"ime": "Ana", "prezime": "Anic", "email": "ana@student.rs", "lozinka": "tajna123", "brojIndeksa": "45/2024"}

Nastavnik definise termin (`POST /api/termini`):

    {"pocetak": "2026-12-10T10:00:00", "kraj": "2026-12-10T11:00:00", "kapacitet": 3,
     "mesto": "Kabinet 214", "predmeti": [{"id": 1}]}

Nastavnik pomera termin (`PATCH /api/termini/1/pomeri`):

    {"pocetak": "2026-12-11T12:00:00", "kraj": "2026-12-11T13:00:00", "mesto": "Kabinet 301"}

Student rezervise (`POST /api/rezervacije`):

    {"terminId": 1, "tema": "Pitanje oko projekta"}

Nastavnik belezi dolazak (`PATCH /api/rezervacije/1/evidencija`):

    {"dosao": true}

## Poslovna pravila

- rezervacija samo za buduci termin i dok ima mesta (kapacitet);
- student ne moze dvaput da rezervise isti termin i ima najvise 2 aktivne rezervacije (`RezervacijaService.MAX_AKTIVNIH_REZERVACIJA`);
- rezervacija se otkazuje najkasnije 2 sata pre termina (`MIN_SATI_ZA_OTKAZIVANJE`);
- otkazivanje termina otkazuje i sve njegove rezervacije;
- nastavnik menja samo svoje termine i evidentira samo svoje rezervacije;
- istovremene rezervacije poslednjeg mesta resava pesimisticko zakljucavanje reda termina;
- potvrda, izmena i otkazivanje salju notifikaciju; podsetnik 24h pre termina salje `PodsetnikService` na 15 min;
- admin ne moze obrisati katedru sa nastavnicima/predmetima, predmet sa terminima, nastavnika sa terminima ni studenta sa rezervacijama.

## Napomene

- `Korisnik` ima dodatno polje `lozinka` (BCrypt hash, nikad se ne vraca u JSON-u).
- `spring.jpa.open-in-view=true` je namerno ukljuceno jer se entiteti vracaju direktno iz kontrolera (kao u `projekatPrakse`).
- Slanje mejla je za sada samo ispis u log (`NotifikacijaService`); pravo slanje ide tu.
- CORS je otvoren za `http://localhost:4200` (Angular), izmena u `SecurityConfig`.
