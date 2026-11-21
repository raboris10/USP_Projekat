package consultme.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import consultme.model.Notifikacija;
import consultme.model.Rezervacija;
import consultme.model.TipNotifikacije;
import consultme.repository.NotifikacijaRepository;
import jakarta.transaction.Transactional;

@Service
public class NotifikacijaService {

	private static final Logger log = LoggerFactory.getLogger(NotifikacijaService.class);

	@Autowired
	private NotifikacijaRepository notifikacijaRepository;

	// pravi i belezi notifikaciju; slanje mejla je za sada samo ispis u log
	// (ovde kasnije ide JavaMailSender / SendGrid)
	@Transactional
	public Notifikacija posalji(Rezervacija rezervacija, TipNotifikacije tip) {
		Notifikacija n = new Notifikacija();
		n.setTip(tip);
		n.setRezervacija(rezervacija);
		n.posalji();
		notifikacijaRepository.save(n);
		rezervacija.getNotifikacije().add(n);
		log.info("[MEJL] {} -> {} (rezervacija #{})", tip, rezervacija.getStudent().getEmail(), rezervacija.getId());
		return n;
	}

	public List<Notifikacija> getMoje(String emailStudenta) {
		return notifikacijaRepository.findByRezervacijaStudentEmailOrderByVremeSlanjaDesc(emailStudenta);
	}
}
