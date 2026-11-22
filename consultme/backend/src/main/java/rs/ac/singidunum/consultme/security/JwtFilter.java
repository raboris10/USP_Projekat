package rs.ac.singidunum.consultme.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import rs.ac.singidunum.consultme.model.Korisnik;
import rs.ac.singidunum.consultme.repository.KorisnikRepository;

// Filter se izvršava za SVAKI zahtev, PRE nego što zahtev stigne do kontrolera.
// Zadatak: ako zahtev ima ispravan JWT token, "prijavi" korisnika za taj jedan zahtev.
//
// Zahtev sa tokenom ima zaglavlje:
//   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9....
//
// Pored provere tokena, filter proverava i BAZU: da li korisnik i dalje postoji i da li je aktivan.
// Tako deaktivirani korisnik odmah gubi pristup (ne čeka da mu token istekne),
// a promena uloge važi odmah (uloga se čita iz baze, ne iz tokena).
public class JwtFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final KorisnikRepository korisnikRepository;

	public JwtFilter(JwtService jwtService, KorisnikRepository korisnikRepository) {
		this.jwtService = jwtService;
		this.korisnikRepository = korisnikRepository;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		// 1) Pročitaj zaglavlje Authorization
		String zaglavlje = request.getHeader("Authorization");

		// 2) Ako postoji i počinje sa "Bearer ", izvuci sam token (sve posle 7. znaka)
		if (zaglavlje != null && zaglavlje.startsWith("Bearer ")) {
			String token = zaglavlje.substring(7);

			try {
				// 3) Proveri token (potpis i rok) i pročitaj email iz njega
				Claims sadrzaj = jwtService.procitajToken(token);
				String email = sadrzaj.getSubject();

				// 3a) Korisnik mora i dalje da postoji i da bude aktivan.
				//     Ako nije -> ne prijavljujemo ga, pa zaštićene rute vraćaju 401.
				Korisnik korisnik = korisnikRepository.findByEmail(email).orElse(null);

				if (korisnik != null && korisnik.getAktivan()) {
					// 4) Napravi objekat "prijavljeni korisnik" za Spring Security.
					//    Uloga se čita iz BAZE (ne iz tokena), pa promena uloge važi odmah.
					//    Prefiks ROLE_ je Spring-ova konvencija: hasRole("ADMIN") traži "ROLE_ADMIN".
					UsernamePasswordAuthenticationToken prijava = new UsernamePasswordAuthenticationToken(
							email,                                                                  // ko je (principal)
							null,                                                                   // lozinka nam ovde ne treba
							List.of(new SimpleGrantedAuthority("ROLE_" + korisnik.getUloga().name())) // šta sme
					);

					// 5) Sačuvaj ga - od sada Spring zna ko šalje ovaj zahtev
					SecurityContextHolder.getContext().setAuthentication(prijava);
				}

			} catch (JwtException | IllegalArgumentException e) {
				// Token je neispravan ili istekao -> korisnik ostaje neprijavljen.
				// SecurityConfig će onda za zaštićene rute vratiti 401.
				SecurityContextHolder.clearContext();
			}
		}

		// 6) Pusti zahtev dalje (ka sledećem filteru, pa ka kontroleru)
		filterChain.doFilter(request, response);
	}

}
