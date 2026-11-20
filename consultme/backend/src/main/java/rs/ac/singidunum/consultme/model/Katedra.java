package rs.ac.singidunum.consultme.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Katedra kojoj pripadaju predmeti (tabela katedra)
@Entity
@Table(name = "katedra")
public class Katedra {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String naziv;

	public Katedra() {
		super();
	}

	public Katedra(String naziv) {
		super();
		this.naziv = naziv;
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

}
