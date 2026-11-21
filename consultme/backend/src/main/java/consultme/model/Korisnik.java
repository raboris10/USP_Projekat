package consultme.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "korisnik")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Korisnik {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Ime je obavezno")
	@Column(nullable = false)
	private String ime;

	@NotBlank(message = "Prezime je obavezno")
	@Column(nullable = false)
	private String prezime;

	@NotBlank(message = "Email je obavezan")
	@Email(message = "Email nije ispravan")
	@Column(nullable = false, unique = true)
	private String email;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Size(min = 6, message = "Lozinka mora imati najmanje 6 karaktera")
	@Column(nullable = false)
	private String lozinka;

	public Korisnik() {
	}

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

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	// naziv uloge: STUDENT, NASTAVNIK ili ADMIN (vraca se i u JSON-u)
	public abstract String getUloga();
}
