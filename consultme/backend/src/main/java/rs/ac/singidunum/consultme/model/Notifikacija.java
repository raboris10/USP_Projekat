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

// Obaveštenje za jednog korisnika - studenta ili profesora (tabela notifikacija).
// Za sada se prikazuje samo u aplikaciji; kasnije se isti zapis šalje i mejlom.
@Entity
@Table(name = "notifikacija")
public class Notifikacija {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// Veza N:1 -> korisnik ima više notifikacija
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "primalac_id")
	private Korisnik primalac;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TipNotifikacije tip;

	// Gotov tekst za prikaz, npr. "Ana Anić je rezervisala Vaš termin (USP)."
	@Column(nullable = false, length = 500)
	private String poruka;

	// Veza N:1, OPCIONA -> termin na koji se notifikacija odnosi (ako se odnosi)
	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "termin_id")
	private Termin termin;

	@Column(nullable = false)
	private Boolean procitana = false;

	@Column(nullable = false)
	private LocalDateTime kreirana = LocalDateTime.now();

	public Notifikacija() {
		super();
	}

	public Notifikacija(Korisnik primalac, TipNotifikacije tip, String poruka, Termin termin) {
		super();
		this.primalac = primalac;
		this.tip = tip;
		this.poruka = poruka;
		this.termin = termin;
	}

	// ----- getteri i setteri -----

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Korisnik getPrimalac() {
		return primalac;
	}

	public void setPrimalac(Korisnik primalac) {
		this.primalac = primalac;
	}

	public TipNotifikacije getTip() {
		return tip;
	}

	public void setTip(TipNotifikacije tip) {
		this.tip = tip;
	}

	public String getPoruka() {
		return poruka;
	}

	public void setPoruka(String poruka) {
		this.poruka = poruka;
	}

	public Termin getTermin() {
		return termin;
	}

	public void setTermin(Termin termin) {
		this.termin = termin;
	}

	public Boolean getProcitana() {
		return procitana;
	}

	public void setProcitana(Boolean procitana) {
		this.procitana = procitana;
	}

	public LocalDateTime getKreirana() {
		return kreirana;
	}

	public void setKreirana(LocalDateTime kreirana) {
		this.kreirana = kreirana;
	}

}
