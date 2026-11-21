package consultme.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "student")
public class Student extends Korisnik {

	@NotBlank(message = "Broj indeksa je obavezan")
	@Column(nullable = false, unique = true)
	private String brojIndeksa;

	@JsonIgnore
	@OneToMany(mappedBy = "student")
	private List<Rezervacija> rezervacije = new ArrayList<>();

	public Student() {
	}

	public String getBrojIndeksa() {
		return brojIndeksa;
	}

	public void setBrojIndeksa(String brojIndeksa) {
		this.brojIndeksa = brojIndeksa;
	}

	public List<Rezervacija> getRezervacije() {
		return rezervacije;
	}

	public void setRezervacije(List<Rezervacija> rezervacije) {
		this.rezervacije = rezervacije;
	}

	@Override
	public String getUloga() {
		return "STUDENT";
	}
}
