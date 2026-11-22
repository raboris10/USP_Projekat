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

// Termin konsultacija jednog profesora (tabela termin)
@Entity
@Table(name = "termin")
public class Termin {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// Veza N:1 -> profesor ima više termina, termin pripada jednom profesoru
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "profesor_id")
	private Korisnik profesor;

	// Veza N:1, ali OPCIONA (optional = true) -> termin ne mora da bude vezan za predmet
	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "predmet_id")
	private Predmet predmet;

	// LocalDateTime = datum + vreme (kolona DATETIME)
	@Column(nullable = false)
	private LocalDateTime pocetak;

	// Hibernate sam pretvara trajanjeMin -> trajanje_min (camelCase u snake_case)
	@Column(nullable = false)
	private Integer trajanjeMin;

	@Column(nullable = false)
	private String mesto;

	@Column(nullable = false)
	private Integer kapacitet;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StatusTermina status = StatusTermina.AKTIVAN;

	public Termin() {
		super();
	}

	public Termin(Korisnik profesor, Predmet predmet, LocalDateTime pocetak, Integer trajanjeMin, String mesto,
			Integer kapacitet) {
		super();
		this.profesor = profesor;
		this.predmet = predmet;
		this.pocetak = pocetak;
		this.trajanjeMin = trajanjeMin;
		this.mesto = mesto;
		this.kapacitet = kapacitet;
	}

	// ----- getteri i setteri -----

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Korisnik getProfesor() {
		return profesor;
	}

	public void setProfesor(Korisnik profesor) {
		this.profesor = profesor;
	}

	public Predmet getPredmet() {
		return predmet;
	}

	public void setPredmet(Predmet predmet) {
		this.predmet = predmet;
	}

	public LocalDateTime getPocetak() {
		return pocetak;
	}

	public void setPocetak(LocalDateTime pocetak) {
		this.pocetak = pocetak;
	}

	public Integer getTrajanjeMin() {
		return trajanjeMin;
	}

	public void setTrajanjeMin(Integer trajanjeMin) {
		this.trajanjeMin = trajanjeMin;
	}

	public String getMesto() {
		return mesto;
	}

	public void setMesto(String mesto) {
		this.mesto = mesto;
	}

	public Integer getKapacitet() {
		return kapacitet;
	}

	public void setKapacitet(Integer kapacitet) {
		this.kapacitet = kapacitet;
	}

	public StatusTermina getStatus() {
		return status;
	}

	public void setStatus(StatusTermina status) {
		this.status = status;
	}

}
