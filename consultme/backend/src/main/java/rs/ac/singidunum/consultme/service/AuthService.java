package rs.ac.singidunum.consultme.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import rs.ac.singidunum.consultme.dto.LoginRequest;
import rs.ac.singidunum.consultme.dto.LoginResponse;
import rs.ac.singidunum.consultme.exception.NeispravnaPrijavaException;
import rs.ac.singidunum.consultme.model.Korisnik;
import rs.ac.singidunum.consultme.repository.KorisnikRepository;
import rs.ac.singidunum.consultme.security.JwtService;

// Poslovna logika prijave
@Service
public class AuthService {

	// Ista poruka za pogrešan email i pogrešnu lozinku - namerno,
	// da napadač ne može da sazna koji email-ovi postoje u sistemu.
	private static final String PORUKA_GRESKE = "Pogrešan email ili lozinka.";

	private final KorisnikRepository korisnikRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	// Spring sam prosleđuje ove objekte (dependency injection), kao u vašem ISA projektu
	public AuthService(KorisnikRepository korisnikRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.korisnikRepository = korisnikRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	public LoginResponse prijava(LoginRequest zahtev) {

		// 1) Nađi korisnika po email-u; ako ne postoji -> greška
		Korisnik korisnik = korisnikRepository.findByEmail(zahtev.email().trim().toLowerCase())
				.orElseThrow(() -> new NeispravnaPrijavaException(PORUKA_GRESKE));

		// 2) Uporedi unetu lozinku sa hešom iz baze (BCrypt to radi umesto nas)
		if (!passwordEncoder.matches(zahtev.lozinka(), korisnik.getLozinka())) {
			throw new NeispravnaPrijavaException(PORUKA_GRESKE);
		}

		// 3) Neaktivan nalog ne može da se prijavi
		if (!korisnik.getAktivan()) {
			throw new NeispravnaPrijavaException("Nalog je deaktiviran. Obratite se administratoru.");
		}

		// 4) Sve je u redu -> napravi token i vrati ga zajedno sa osnovnim podacima
		String token = jwtService.napraviToken(korisnik);

		return new LoginResponse(
				token,
				korisnik.getEmail(),
				korisnik.getIme(),
				korisnik.getPrezime(),
				korisnik.getUloga().name());
	}

}
