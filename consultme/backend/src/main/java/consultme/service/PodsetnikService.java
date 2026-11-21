package consultme.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import consultme.model.Rezervacija;
import consultme.model.TipNotifikacije;
import consultme.repository.RezervacijaRepository;
import jakarta.transaction.Transactional;

// na svakih 15 minuta salje podsetnik za rezervacije ciji termin pocinje u narednih 24h
@Service
public class PodsetnikService {

	@Autowired
	private RezervacijaRepository rezervacijaRepository;
	@Autowired
	private NotifikacijaService notifikacijaService;

	@Scheduled(fixedRate = 15 * 60 * 1000)
	@Transactional
	public void posaljiPodsetnike() {
		LocalDateTime sada = LocalDateTime.now();
		for (Rezervacija r : rezervacijaRepository.nadjiZaPodsetnik(sada, sada.plusHours(24))) {
			notifikacijaService.posalji(r, TipNotifikacije.PODSETNIK);
		}
	}
}
