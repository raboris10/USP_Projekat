package consultme.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "nastavnik")
public class Nastavnik extends Korisnik {

	private String zvanje;

	@ManyToOne(optional = false)
	@JoinColumn(name = "katedra_id")
	private Katedra katedra;

	@JsonIgnore
	@OneToMany(mappedBy = "nastavnik")
	private List<Termin> termini = new ArrayList<>();

	public Nastavnik() {
	}

	public String getZvanje() {
		return zvanje;
	}

	public void setZvanje(String zvanje) {
		this.zvanje = zvanje;
	}

	public Katedra getKatedra() {
		return katedra;
	}

	public void setKatedra(Katedra katedra) {
		this.katedra = katedra;
	}

	public List<Termin> getTermini() {
		return termini;
	}

	public void setTermini(List<Termin> termini) {
		this.termini = termini;
	}

	@Override
	public String getUloga() {
		return "NASTAVNIK";
	}
}
