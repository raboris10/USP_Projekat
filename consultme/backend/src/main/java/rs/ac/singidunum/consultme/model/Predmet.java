package rs.ac.singidunum.consultme.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// Predmet (tabela predmet)
@Entity
@Table(name = "predmet")
public class Predmet {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String naziv;

	@Column(nullable = false, unique = true)
	private String sifra;

	// Veza N:1 -> više predmeta pripada jednoj katedri.
	// @JoinColumn kaže koja kolona u tabeli predmet čuva id katedre.
	// LAZY -> katedra se učitava iz baze tek kad je zatražimo (getKatedra()).
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "katedra_id")
	private Katedra katedra;

	// Veza M:N -> predmet ima više profesora, profesor drži više predmeta.
	// @JoinTable opisuje pomoćnu tabelu profesor_predmet:
	//   joinColumns        -> kolona koja pokazuje na OVAJ predmet
	//   inverseJoinColumns -> kolona koja pokazuje na profesora (korisnika)
	@ManyToMany
	@JoinTable(
			name = "profesor_predmet",
			joinColumns = @JoinColumn(name = "predmet_id"),
			inverseJoinColumns = @JoinColumn(name = "profesor_id"))
	private Set<Korisnik> profesori = new HashSet<>();

	public Predmet() {
		super();
	}

	public Predmet(String naziv, String sifra, Katedra katedra) {
		super();
		this.naziv = naziv;
		this.sifra = sifra;
		this.katedra = katedra;
	}

	// ----- getteri i setteri -----

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNaziv() {
		return naziv;
	}

	public void setNaziv(String naziv) {
		this.naziv = naziv;
	}

	public String getSifra() {
		return sifra;
	}

	public void setSifra(String sifra) {
		this.sifra = sifra;
	}

	public Katedra getKatedra() {
		return katedra;
	}

	public void setKatedra(Katedra katedra) {
		this.katedra = katedra;
	}

	public Set<Korisnik> getProfesori() {
		return profesori;
	}

	public void setProfesori(Set<Korisnik> profesori) {
		this.profesori = profesori;
	}

}
