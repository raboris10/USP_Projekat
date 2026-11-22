package rs.ac.singidunum.consultme.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// Rezervacija = student prijavljen na termin (tabela rezervacija)
@Entity
@Table(name = "rezervacija")
public class Rezervacija {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// Veza N:1 -> student ima više rezervacija
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "student_id")
	private Korisnik student;

	// Veza N:1 -> termin ima više rezervacija (do svog kapaciteta)
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "termin_id")
	private Termin termin;

	@Column(nullable = false, length = 500)
	private String tema;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StatusRezervacije status = StatusRezervacije.AKTIVNA;

	// null dok profesor ne upiše da li je student došao
	@Enumerated(EnumType.STRING)
	@Column(nullable = true)
	private Dolazak dolazak;

	@Column(nullable = false)
	private LocalDateTime kreirana = LocalDateTime.now();

	public Rezervacija() {
		super();
	}

	public Rezervacija(Korisnik student, Termin termin, String tema) {
		super();
		this.student = student;
		this.termin = termin;
		this.tema = tema;
	}

	// ----- getteri i setteri -----

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Korisnik getStudent() {
		return student;
	}

	public void setStudent(Korisnik student) {
		this.student = student;
	}

	public Termin getTermin() {
		return termin;
	}

	public void setTermin(Termin termin) {
		this.termin = termin;
	}

	public String getTema() {
		return tema;
	}

	public void setTema(String tema) {
		this.tema = tema;
	}

	public StatusRezervacije getStatus() {
		return status;
	}

	public void setStatus(StatusRezervacije status) {
		this.status = status;
	}

	public Dolazak getDolazak() {
		return dolazak;
	}

	public void setDolazak(Dolazak dolazak) {
		this.dolazak = dolazak;
	}

	public LocalDateTime getKreirana() {
		return kreirana;
	}

	public void setKreirana(LocalDateTime kreirana) {
		this.kreirana = kreirana;
	}

}
