package consultme.model;

import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "predmet")
public class Predmet {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Sifra je obavezna")
	@Column(nullable = false, unique = true)
	private String sifra;

	@NotBlank(message = "Naziv je obavezan")
	@Column(nullable = false)
	private String naziv;

	@ManyToOne(optional = false)
	@JoinColumn(name = "katedra_id")
	private Katedra katedra;

	@JsonIgnore
	@ManyToMany(mappedBy = "predmeti")
	private Set<Termin> termini = new HashSet<>();

	public Predmet() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSifra() {
		return sifra;
	}

	public void setSifra(String sifra) {
		this.sifra = sifra;
	}

	public String getNaziv() {
		return naziv;
	}

	public void setNaziv(String naziv) {
		this.naziv = naziv;
	}

	public Katedra getKatedra() {
		return katedra;
	}

	public void setKatedra(Katedra katedra) {
		this.katedra = katedra;
	}

	public Set<Termin> getTermini() {
		return termini;
	}

	public void setTermini(Set<Termin> termini) {
		this.termini = termini;
	}
}
