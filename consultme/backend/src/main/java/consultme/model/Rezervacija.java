package consultme.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "rezervacija")
public class Rezervacija {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String tema;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StatusRezervacije status = StatusRezervacije.AKTIVNA;

	@Column(nullable = false)
	private LocalDateTime kreirana;

	@ManyToOne(optional = false)
	@JoinColumn(name = "student_id")
	private Student student;

	@ManyToOne(optional = false)
	@JoinColumn(name = "termin_id")
	private Termin termin;

	@JsonIgnore
	@OneToMany(mappedBy = "rezervacija", cascade = CascadeType.ALL)
	private List<Notifikacija> notifikacije = new ArrayList<>();

	@OneToOne(mappedBy = "rezervacija", cascade = CascadeType.ALL)
	private Evidencija evidencija;

	public Rezervacija() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public LocalDateTime getKreirana() {
		return kreirana;
	}

	public void setKreirana(LocalDateTime kreirana) {
		this.kreirana = kreirana;
	}

	public Student getStudent() {
		return student;
	}

	public void setStudent(Student student) {
		this.student = student;
	}

	public Termin getTermin() {
		return termin;
	}

	public void setTermin(Termin termin) {
		this.termin = termin;
	}

	public List<Notifikacija> getNotifikacije() {
		return notifikacije;
	}

	public void setNotifikacije(List<Notifikacija> notifikacije) {
		this.notifikacije = notifikacije;
	}

	public Evidencija getEvidencija() {
		return evidencija;
	}

	public void setEvidencija(Evidencija evidencija) {
		this.evidencija = evidencija;
	}

	public void otkazi() {
		this.status = StatusRezervacije.OTKAZANA;
	}
}
