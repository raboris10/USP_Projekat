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

// Filter se izvršava za SVAKI zahtev, PRE nego što zahtev stigne do kontrolera.
// Zadatak: ako zahtev ima ispravan JWT token, "prijavi" korisnika za taj jedan zahtev.
//
// Zahtev sa tokenom ima zaglavlje:
//   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9....
public class JwtFilter extends OncePerRequestFilter {

	private final JwtService jwtService;

	public JwtFilter(JwtService jwtService) {
		this.jwtService = jwtService;
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
				// 3) Proveri token i pročitaj email i ulogu iz njega
				Claims sadrzaj = jwtService.procitajToken(token);
				String email = sadrzaj.getSubject();
				String uloga = sadrzaj.get("uloga", String.class);

				// 4) Napravi objekat "prijavljeni korisnik" za Spring Security.
				//    Prefiks ROLE_ je Spring-ova konvencija: hasRole("ADMIN") traži "ROLE_ADMIN".
				UsernamePasswordAuthenticationToken prijava = new UsernamePasswordAuthenticationToken(
						email,                                               // ko je (principal)
						null,                                                // lozinka nam ovde ne treba
						List.of(new SimpleGrantedAuthority("ROLE_" + uloga)) // šta sme
				);

				// 5) Sačuvaj ga - od sada Spring zna ko šalje ovaj zahtev
				SecurityContextHolder.getContext().setAuthentication(prijava);

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
