package consultme.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "termin")
public class Termin {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull(message = "Pocetak je obavezan")
	@Column(nullable = false)
	private LocalDateTime pocetak;

	@NotNull(message = "Kraj je obavezan")
	@Column(nullable = false)
	private LocalDateTime kraj;

	@Min(value = 1, message = "Kapacitet mora biti najmanje 1")
	@Column(nullable = false)
	private int kapacitet;

	@NotBlank(message = "Mesto je obavezno")
	@Column(nullable = false)
	private String mesto;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StatusTermina status = StatusTermina.OTVOREN;

	@ManyToOne(optional = false)
	@JoinColumn(name = "nastavnik_id")
	private Nastavnik nastavnik;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "termin_predmet", joinColumns = @JoinColumn(name = "termin_id"), inverseJoinColumns = @JoinColumn(name = "predmet_id"))
	private Set<Predmet> predmeti = new HashSet<>();

	@JsonIgnore
	@OneToMany(mappedBy = "termin")
	private List<Rezervacija> rezervacije = new ArrayList<>();

	public Termin() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getPocetak() {
		return pocetak;
	}

	public void setPocetak(LocalDateTime pocetak) {
		this.pocetak = pocetak;
	}

	public LocalDateTime getKraj() {
		return kraj;
	}

	public void setKraj(LocalDateTime kraj) {
		this.kraj = kraj;
	}

	public int getKapacitet() {
		return kapacitet;
	}

	public void setKapacitet(int kapacitet) {
		this.kapacitet = kapacitet;
	}

	public String getMesto() {
		return mesto;
	}

	public void setMesto(String mesto) {
		this.mesto = mesto;
	}

	public StatusTermina getStatus() {
		return status;
	}

	public void setStatus(StatusTermina status) {
		this.status = status;
	}

	public Nastavnik getNastavnik() {
		return nastavnik;
	}

	public void setNastavnik(Nastavnik nastavnik) {
		this.nastavnik = nastavnik;
	}

	public Set<Predmet> getPredmeti() {
		return predmeti;
	}

	public void setPredmeti(Set<Predmet> predmeti) {
		this.predmeti = predmeti;
	}

	public List<Rezervacija> getRezervacije() {
		return rezervacije;
	}

	public void setRezervacije(List<Rezervacija> rezervacije) {
		this.rezervacije = rezervacije;
	}

	public long brojAktivnihRezervacija() {
		long broj = 0;
		for (Rezervacija r : rezervacije) {
			if (r.getStatus() == StatusRezervacije.AKTIVNA) {
				broj++;
			}
		}
		return broj;
	}

	// vraca se u JSON-u kao "zauzeto"
	public long getZauzeto() {
		return brojAktivnihRezervacija();
	}

	public boolean imaSlobodnoMesto() {
		return status != StatusTermina.OTKAZAN && brojAktivnihRezervacija() < kapacitet;
	}

	// postavlja OTVOREN / POPUNJEN prema broju aktivnih rezervacija (otkazan termin ostaje otkazan)
	public void azurirajStatus() {
		if (status == StatusTermina.OTKAZAN) {
			return;
		}
		status = brojAktivnihRezervacija() >= kapacitet ? StatusTermina.POPUNJEN : StatusTermina.OTVOREN;
	}
}
