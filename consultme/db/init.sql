-- =====================================================================
-- ConsultMe: kreiranje baze, tabela i pocetnih podataka (MySQL 8)
-- DBeaver: otvori SQL editor, nalepi skriptu i pokreni kao skriptu (Alt+X)
-- Tabele odgovaraju entitetima iz Spring Boot projekta (ddl-auto=update ih nece menjati).
-- =====================================================================

CREATE DATABASE IF NOT EXISTS consultme
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE consultme;

-- Brisanje starih tabela (redosled zbog stranih kljuceva). OBRISACE SVE PODATKE!
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS evidencija;
DROP TABLE IF EXISTS notifikacija;
DROP TABLE IF EXISTS rezervacija;
DROP TABLE IF EXISTS termin_predmet;
DROP TABLE IF EXISTS termin;
DROP TABLE IF EXISTS predmet;
DROP TABLE IF EXISTS administrator;
DROP TABLE IF EXISTS student;
DROP TABLE IF EXISTS nastavnik;
DROP TABLE IF EXISTS korisnik;
DROP TABLE IF EXISTS katedra;
SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------
-- TABELE
-- ---------------------------------------------------------------------

CREATE TABLE katedra (
    id    BIGINT       NOT NULL AUTO_INCREMENT,
    naziv VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB;

-- Korisnik je apstraktna klasa (JOINED nasledjivanje): zajednicka polja su ovde,
-- a polja podklasa u tabelama student, nastavnik i administrator.
CREATE TABLE korisnik (
    id      BIGINT       NOT NULL AUTO_INCREMENT,
    ime     VARCHAR(255) NOT NULL,
    prezime VARCHAR(255) NOT NULL,
    email   VARCHAR(255) NOT NULL,
    lozinka VARCHAR(255) NOT NULL,           -- BCrypt hash
    PRIMARY KEY (id),
    UNIQUE KEY uk_korisnik_email (email)
) ENGINE = InnoDB;

CREATE TABLE administrator (
    id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_administrator_korisnik FOREIGN KEY (id) REFERENCES korisnik (id)
) ENGINE = InnoDB;

CREATE TABLE student (
    id           BIGINT       NOT NULL,
    broj_indeksa VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_indeks (broj_indeksa),
    CONSTRAINT fk_student_korisnik FOREIGN KEY (id) REFERENCES korisnik (id)
) ENGINE = InnoDB;

CREATE TABLE nastavnik (
    id         BIGINT       NOT NULL,
    zvanje     VARCHAR(255) NULL,
    katedra_id BIGINT       NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_nastavnik_korisnik FOREIGN KEY (id) REFERENCES korisnik (id),
    CONSTRAINT fk_nastavnik_katedra  FOREIGN KEY (katedra_id) REFERENCES katedra (id)
) ENGINE = InnoDB;

CREATE TABLE predmet (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    sifra      VARCHAR(255) NOT NULL,
    naziv      VARCHAR(255) NOT NULL,
    katedra_id BIGINT       NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_predmet_sifra (sifra),
    CONSTRAINT fk_predmet_katedra FOREIGN KEY (katedra_id) REFERENCES katedra (id)
) ENGINE = InnoDB;

CREATE TABLE termin (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    pocetak     DATETIME(6)  NOT NULL,
    kraj        DATETIME(6)  NOT NULL,
    kapacitet   INT          NOT NULL,
    mesto       VARCHAR(255) NOT NULL,
    status      VARCHAR(255) NOT NULL,       -- OTVOREN, POPUNJEN, OTKAZAN
    nastavnik_id BIGINT      NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_termin_nastavnik FOREIGN KEY (nastavnik_id) REFERENCES nastavnik (id)
) ENGINE = InnoDB;

CREATE TABLE termin_predmet (
    termin_id  BIGINT NOT NULL,
    predmet_id BIGINT NOT NULL,
    PRIMARY KEY (termin_id, predmet_id),
    CONSTRAINT fk_tp_termin  FOREIGN KEY (termin_id)  REFERENCES termin (id),
    CONSTRAINT fk_tp_predmet FOREIGN KEY (predmet_id) REFERENCES predmet (id)
) ENGINE = InnoDB;

CREATE TABLE rezervacija (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    tema       VARCHAR(255) NULL,
    status     VARCHAR(255) NOT NULL,        -- AKTIVNA, OTKAZANA
    kreirana   DATETIME(6)  NOT NULL,
    student_id BIGINT       NOT NULL,
    termin_id  BIGINT       NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_rezervacija_student FOREIGN KEY (student_id) REFERENCES student (id),
    CONSTRAINT fk_rezervacija_termin  FOREIGN KEY (termin_id)  REFERENCES termin (id)
) ENGINE = InnoDB;

CREATE TABLE notifikacija (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    tip           VARCHAR(255) NOT NULL,     -- POTVRDA, PODSETNIK, OTKAZIVANJE, IZMENA
    vreme_slanja  DATETIME(6)  NULL,
    rezervacija_id BIGINT      NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_notifikacija_rezervacija FOREIGN KEY (rezervacija_id) REFERENCES rezervacija (id)
) ENGINE = InnoDB;

CREATE TABLE evidencija (
    id             BIGINT NOT NULL AUTO_INCREMENT,
    dosao          BIT(1) NOT NULL,
    rezervacija_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_evidencija_rezervacija (rezervacija_id),
    CONSTRAINT fk_evidencija_rezervacija FOREIGN KEY (rezervacija_id) REFERENCES rezervacija (id)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- POCETNI PODACI
-- Lozinke su BCrypt hesevi (ne mogu se upisati kao obican tekst):
--   admin@consultme.rs      -> admin123
--   nastavnici (oba)        -> nastavnik123
--   studenti (oba)          -> student123
-- ---------------------------------------------------------------------

INSERT INTO katedra (id, naziv) VALUES
    (1, 'Katedra za racunarstvo');

INSERT INTO predmet (id, sifra, naziv, katedra_id) VALUES
    (1, 'RI101', 'Objektno orijentisano programiranje', 1),
    (2, 'RI102', 'Baze podataka', 1);

-- 1 administrator
INSERT INTO korisnik (id, ime, prezime, email, lozinka) VALUES
    (1, 'Admin', 'ConsultMe', 'admin@consultme.rs',
     '$2a$10$vf/fD7jXluuk5tirBEfnouT/k3LaPQ6YGUe3q2Jr2eg1HV.M58XB2');
INSERT INTO administrator (id) VALUES (1);

-- 2 nastavnika (profesora)
INSERT INTO korisnik (id, ime, prezime, email, lozinka) VALUES
    (2, 'Petar', 'Petrovic', 'petar.petrovic@consultme.rs',
     '$2a$10$jQICDxvAdMDd6gYf9srJxuTVvRJy9u9zURk5gkuX85zH3Cq3aLSdm'),
    (3, 'Jelena', 'Jovanovic', 'jelena.jovanovic@consultme.rs',
     '$2a$10$jQICDxvAdMDd6gYf9srJxuTVvRJy9u9zURk5gkuX85zH3Cq3aLSdm');
INSERT INTO nastavnik (id, zvanje, katedra_id) VALUES
    (2, 'Redovni profesor', 1),
    (3, 'Docent', 1);

-- 2 studenta
INSERT INTO korisnik (id, ime, prezime, email, lozinka) VALUES
    (4, 'Marko', 'Markovic', 'marko.markovic@consultme.rs',
     '$2a$10$6tc5Aq90IYPSsusx/kHmWuD4mIrKni6K27pHOv8LXWvZJuGrQebBi'),
    (5, 'Ana', 'Anic', 'ana.anic@consultme.rs',
     '$2a$10$6tc5Aq90IYPSsusx/kHmWuD4mIrKni6K27pHOv8LXWvZJuGrQebBi');
INSERT INTO student (id, broj_indeksa) VALUES
    (4, '123/2024'),
    (5, '124/2024');

