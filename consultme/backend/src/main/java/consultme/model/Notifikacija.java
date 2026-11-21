package consultme.model;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "notifikacija")
public class Notifikacija {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TipNotifikacije tip;

	private LocalDateTime vremeSlanja;

	@ManyToOne(optional = false)
	@JoinColumn(name = "rezervacija_id")
	private Rezervacija rezervacija;

	public Notifikacija() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public TipNotifikacije getTip() {
		return tip;
	}

	public void setTip(TipNotifikacije tip) {
		this.tip = tip;
	}

	public LocalDateTime getVremeSlanja() {
		return vremeSlanja;
	}

	public void setVremeSlanja(LocalDateTime vremeSlanja) {
		this.vremeSlanja = vremeSlanja;
	}

	public Rezervacija getRezervacija() {
		return rezervacija;
	}

	public void setRezervacija(Rezervacija rezervacija) {
		this.rezervacija = rezervacija;
	}

	// belezi trenutak slanja; stvarno slanje (mejl) radi NotifikacijaService
	public void posalji() {
		this.vremeSlanja = LocalDateTime.now();
	}
}
