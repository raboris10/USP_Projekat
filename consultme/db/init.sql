-- =====================================================================
-- ConsultMe - početna skripta baze
-- =====================================================================
-- Izvršava se SAMO pri prvom pokretanju baze (kada je volumen prazan).
-- Ako je izmenite, bazu resetujete sa:
--   docker compose down -v
--   docker compose up --build
-- Baza consultme_db je već napravljena preko MYSQL_DATABASE iz .env fajla,
-- pa ovde nema CREATE DATABASE.
-- =====================================================================

-- Da bi slova š, č, ć, ž, đ ispravno ušla u bazu
SET NAMES utf8mb4;


-- =====================================================================
-- TABELE
-- Redosled je bitan: prvo tabele na koje drugi pokazuju (FOREIGN KEY).
-- =====================================================================

-- ---------------------------------------------------------------------
-- KORISNIK - jedna tabela za sve tri uloge (student, profesor, admin)
-- ---------------------------------------------------------------------
CREATE TABLE korisnik (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,  -- jedinstveni broj, baza ga sama dodeljuje
    ime      VARCHAR(50)  NOT NULL,
    prezime  VARCHAR(50)  NOT NULL,
    email    VARCHAR(100) NOT NULL UNIQUE,       -- koristi se za prijavu, ne sme da se ponavlja
    lozinka  VARCHAR(100) NOT NULL,              -- BCrypt HEŠ lozinke (nikad obična lozinka!)
    uloga    VARCHAR(20)  NOT NULL,              -- STUDENT, PROFESOR ili ADMIN
    aktivan  BOOLEAN      NOT NULL DEFAULT TRUE  -- neaktivan nalog ne može da se prijavi
);

-- ---------------------------------------------------------------------
-- KATEDRA
-- ---------------------------------------------------------------------
CREATE TABLE katedra (
    id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    naziv  VARCHAR(150) NOT NULL UNIQUE
);

-- ---------------------------------------------------------------------
-- PREDMET - svaki predmet pripada jednoj katedri (veza N:1)
-- ---------------------------------------------------------------------
CREATE TABLE predmet (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    naziv       VARCHAR(150) NOT NULL,
    sifra       VARCHAR(20)  NOT NULL UNIQUE,    -- kratka oznaka predmeta, npr. USP
    katedra_id  BIGINT       NOT NULL,
    FOREIGN KEY (katedra_id) REFERENCES katedra(id)
);

-- ---------------------------------------------------------------------
-- PROFESOR_PREDMET - pomoćna tabela za vezu više-prema-više (M:N):
-- jedan profesor drži više predmeta, jedan predmet ima više profesora
-- ---------------------------------------------------------------------
CREATE TABLE profesor_predmet (
    profesor_id  BIGINT NOT NULL,
    predmet_id   BIGINT NOT NULL,
    PRIMARY KEY (profesor_id, predmet_id),      -- isti par ne može da se upiše dva puta
    FOREIGN KEY (profesor_id) REFERENCES korisnik(id),
    FOREIGN KEY (predmet_id)  REFERENCES predmet(id)
);

-- ---------------------------------------------------------------------
-- TERMIN - konsultacije jednog profesora
-- Ponavljajući termini se čuvaju kao više pojedinačnih redova.
-- ---------------------------------------------------------------------
CREATE TABLE termin (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    profesor_id   BIGINT       NOT NULL,                  -- čiji je termin
    predmet_id    BIGINT       NULL,                      -- opciono: konsultacije mogu biti i opšte
    pocetak       DATETIME     NOT NULL,                  -- datum i vreme početka
    trajanje_min  INT          NOT NULL,                  -- trajanje u minutima
    mesto         VARCHAR(255) NOT NULL,                  -- kabinet ili link za onlajn sastanak
    kapacitet     INT          NOT NULL,                  -- najveći broj studenata
    status        VARCHAR(20)  NOT NULL DEFAULT 'AKTIVAN',-- AKTIVAN ili OTKAZAN
    FOREIGN KEY (profesor_id) REFERENCES korisnik(id),
    FOREIGN KEY (predmet_id)  REFERENCES predmet(id)
);

-- ---------------------------------------------------------------------
-- REZERVACIJA - veza studenta i termina
-- ---------------------------------------------------------------------
CREATE TABLE rezervacija (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id  BIGINT       NOT NULL,
    termin_id   BIGINT       NOT NULL,
    tema        VARCHAR(500) NOT NULL,                       -- šta student želi da pita
    status      VARCHAR(20)  NOT NULL DEFAULT 'AKTIVNA',     -- AKTIVNA ili OTKAZANA
    dolazak     VARCHAR(20)  NULL,                           -- prazno, DOSAO ili NIJE_DOSAO
    kreirana    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (student_id, termin_id),                          -- student ne može dvaput isti termin
    FOREIGN KEY (student_id) REFERENCES korisnik(id),
    FOREIGN KEY (termin_id)  REFERENCES termin(id)
);


-- =====================================================================
-- POČETNI PODACI
-- =====================================================================

-- ---------------------------------------------------------------------
-- KORISNICI (lozinke važe samo za razvoj!)
--
-- Test nalozi - domen @consultme.rs:
--   admin@consultme.rs     / admin123      ADMIN     (IT tehničar)
--   profesor@consultme.rs  / profesor123   PROFESOR
--   student@consultme.rs   / student123    STUDENT
--
-- "Fakultetski" nalozi - simulirani domen @fakultet.rs, svi sa lozinkom lozinka123:
--   zoran.zoranic@fakultet.rs, maja.majic@fakultet.rs, nikola.nikolic@fakultet.rs   PROFESOR
--   ana.anic@fakultet.rs, bora.boric@fakultet.rs, ceca.cecic@fakultet.rs            STUDENT
--
-- U koloni lozinka su BCrypt heševi tih lozinki (svaki heš je drugačiji,
-- iako je lozinka ista - BCrypt svaki put dodaje nasumičnu "so").
-- ---------------------------------------------------------------------
INSERT INTO korisnik (id, ime, prezime, email, lozinka, uloga) VALUES
-- test nalozi (domen @consultme.rs)
(1, 'Ivan',  'Ilić',    'admin@consultme.rs',        '$2a$10$FcIGQzO6gf9oX8U4HY8dEunSvCyG/E4XMJ0iNJ/Zn6.qWwvFO102i', 'ADMIN'),
(2, 'Petar', 'Petrović', 'profesor@consultme.rs',     '$2a$10$O6kKOAlLnf4B48kEkl2RfueZ6R.Ko.ODal92BMGG1fhACTAIq0WL.', 'PROFESOR'),
(3, 'Sara',  'Sarić',   'student@consultme.rs',      '$2a$10$jDJ14hidXnomM3vvX1U/0uxjwn.fDgnBtdsyBa.17dqAG0Ws3YSFC', 'STUDENT'),
-- profesori i studenti sa "fakultetskim" nalozima (simulirani domen @fakultet.rs)
(4, 'Zoran', 'Zoranić', 'zoran.zoranic@fakultet.rs', '$2a$10$yoVkJR2tMq4xt4GzSjCL/ujCVw7fqcex2j.9.3s7DYtgZCZ5Gmj.C', 'PROFESOR'),
(5, 'Maja',  'Majić',   'maja.majic@fakultet.rs',    '$2a$10$k9haPkesdtBpFu/xaT0KwOKoqbnwZZ3Y5lcItsvZWdoTD6OO7C7WO', 'PROFESOR'),
(6, 'Nikola', 'Nikolić', 'nikola.nikolic@fakultet.rs', '$2a$10$CdPrFG0H9QSfJIOWZEget.HJIAXOld/Bc3bgzri4PngD7dqqzY/p6', 'PROFESOR'),
(7, 'Ana',   'Anić',    'ana.anic@fakultet.rs',      '$2a$10$nm7oevmsVot3C8d27u4gn.Xkg7rgM0RbjkHTcUzbAra.T3HaxAmS.', 'STUDENT'),
(8, 'Bora',  'Borić',   'bora.boric@fakultet.rs',    '$2a$10$5fTWV9da.I0mz6mFR5p6QeNTlfwluy6HCcC402enj0/K2CH5arZla', 'STUDENT'),
(9, 'Ceca',  'Cecić',   'ceca.cecic@fakultet.rs',    '$2a$10$q1xq75paMYafpvggKEvvfebnC5MWdY7hkhCx84Q3nWLbPA/VqjRoK', 'STUDENT');

-- ---------------------------------------------------------------------
-- Jedna katedra i dva predmeta
-- ---------------------------------------------------------------------
INSERT INTO katedra (id, naziv) VALUES
(1, 'Katedra za informatiku i računarstvo');

INSERT INTO predmet (id, naziv, sifra, katedra_id) VALUES
(1, 'Upravljanje softverskim projektom', 'USP', 1),
(2, 'Veb programiranje',                 'VP',  1);

-- Ko drži koji predmet (profesor_id, predmet_id)
INSERT INTO profesor_predmet (profesor_id, predmet_id) VALUES
(2, 1), (2, 2),   -- test profesor: oba predmeta
(4, 1),           -- Zoran Zoranić: USP
(5, 2),           -- Maja Majić: VP
(6, 1), (6, 2);   -- Nikola Nikolić: oba predmeta

-- ---------------------------------------------------------------------
-- Termini, računati od dana kada se baza napravi
-- ---------------------------------------------------------------------
INSERT INTO termin (id, profesor_id, predmet_id, pocetak, trajanje_min, mesto, kapacitet) VALUES
(1, 4, 1,    TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '12:00:00'), 60, 'Kabinet 305',                    5),  -- Zoran, USP
(2, 5, 2,    TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '10:00:00'), 45, 'Kabinet 210',                    3),  -- Maja, VP
(3, 6, NULL, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '16:00:00'), 45, 'https://meet.example.com/nikolic', 10), -- Nikola, opšte, onlajn
(4, 2, 1,    TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '14:00:00'), 30, 'Kabinet 101',                    4);  -- test profesor

-- ---------------------------------------------------------------------
-- Rezervacije (student_id, termin_id, tema)
-- ---------------------------------------------------------------------
INSERT INTO rezervacija (student_id, termin_id, tema) VALUES
(7, 1, 'Pitanja o projektnoj dokumentaciji'),   -- Ana kod Zorana
(8, 1, 'Gantov dijagram i kontrolne tačke'),    -- Bora kod Zorana
(9, 2, 'Povezivanje Angulara sa backendom');    -- Ceca kod Maje
