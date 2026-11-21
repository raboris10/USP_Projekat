package consultme.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import consultme.model.Korisnik;
import consultme.repository.KorisnikRepository;
import consultme.security.JwtUtil;
import jakarta.persistence.EntityNotFoundException;

@Service
public class AuthService {

	@Autowired
	private KorisnikRepository korisnikRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private JwtUtil jwtUtil;

	// prijava: proverava email i lozinku i vraca JWT token
	public Map<String, Object> login(String email, String lozinka) {
		if (email == null || lozinka == null) {
			throw new BadCredentialsException("Email i lozinka su obavezni");
		}
		Korisnik korisnik = korisnikRepository.findByEmail(email)
				.orElseThrow(() -> new BadCredentialsException("Pogresan email ili lozinka"));

		if (!passwordEncoder.matches(lozinka, korisnik.getLozinka())) {
			throw new BadCredentialsException("Pogresan email ili lozinka");
		}

		Map<String, Object> odgovor = new LinkedHashMap<>();
		odgovor.put("token", jwtUtil.generisiToken(korisnik));
		odgovor.put("tip", "Bearer");
		odgovor.put("istice_za_sekundi", jwtUtil.getTrajanjeSekundi());
		odgovor.put("korisnik", korisnik);
		return odgovor;
	}

	public Korisnik getPrijavljeni(String email) {
		return korisnikRepository.findByEmail(email)
				.orElseThrow(() -> new EntityNotFoundException("Korisnik nije pronadjen"));
	}
}
