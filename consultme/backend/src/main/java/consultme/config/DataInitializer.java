package consultme.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import consultme.model.Administrator;
import consultme.model.Katedra;
import consultme.model.Nastavnik;
import consultme.model.Predmet;
import consultme.model.Student;
import consultme.repository.AdministratorRepository;
import consultme.repository.KatedraRepository;
import consultme.repository.KorisnikRepository;
import consultme.repository.NastavnikRepository;
import consultme.repository.PredmetRepository;
import consultme.repository.StudentRepository;

// Pri pokretanju pravi prvog administratora (bez njega niko ne bi mogao da se prijavi,
// jer nema registracije) i opciono demo podatke.
@Component
public class DataInitializer implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

	@Autowired
	private KorisnikRepository korisnikRepository;
	@Autowired
	private AdministratorRepository administratorRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Value("${consultme.admin.email}")
	private String adminEmail;
	@Value("${consultme.admin.lozinka}")
	private String adminLozinka;
	
	
	
	@Override
	public void run(String... args) {
		
		
		if (!korisnikRepository.existsByEmail(adminEmail)) {
			Administrator admin = new Administrator();
			admin.setIme("Admin");
			admin.setPrezime("ConsultMe");
			admin.setEmail(adminEmail);
			admin.setLozinka(passwordEncoder.encode(adminLozinka));
			administratorRepository.save(admin);
			log.info("Kreiran administrator: {}", adminEmail);
		}
		
	}

	
}
