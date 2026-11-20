package rs.ac.singidunum.consultme.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// @Entity -> ova klasa predstavlja red u tabeli baze
// @Table  -> ime tabele iz db/init.sql
@Entity
@Table(name = "korisnik")
public class Korisnik {

	// Primarni ključ; IDENTITY znači da broj dodeljuje baza (AUTO_INCREMENT)
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String ime;

	@Column(nullable = false)
	private String prezime;

	// unique = true -> dva korisnika ne mogu imati isti email
	@Column(nullable = false, unique = true)
	private String email;

	// Ovde je BCrypt HEŠ lozinke, nikad prava lozinka.
	// Ova klasa se nikad ne šalje frontendu direktno (koristimo DTO klase), pa heš ne "curi".
	@Column(nullable = false)
	private String lozinka;

	// EnumType.STRING -> u bazu se upisuje tekst "STUDENT", a ne broj 0
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Uloga uloga;

	@Column(nullable = false)
	private Boolean aktivan = true;

	// Prazan konstruktor je obavezan za JPA (Hibernate pravi objekte preko njega)
	public Korisnik() {
		super();
	}

	public Korisnik(String ime, String prezime, String email, String lozinka, Uloga uloga) {
		super();
		this.ime = ime;
		this.prezime = prezime;
		this.email = email;
		this.lozinka = lozinka;
		this.uloga = uloga;
	}

	// ----- getteri i setteri -----

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getIme() {
		return ime;
	}

	public void setIme(String ime) {
		this.ime = ime;
	}

	public String getPrezime() {
		return prezime;
	}

	public void setPrezime(String prezime) {
		this.prezime = prezime;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getLozinka() {
		return lozinka;
	}

	public void setLozinka(String lozinka) {
		this.lozinka = lozinka;
	}

	public Uloga getUloga() {
		return uloga;
	}

	public void setUloga(Uloga uloga) {
		this.uloga = uloga;
	}

	public Boolean getAktivan() {
		return aktivan;
	}

	public void setAktivan(Boolean aktivan) {
		this.aktivan = aktivan;
	}

}
