package consultme.security;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import consultme.repository.KorisnikRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Cita "Authorization: Bearer <token>" i postavlja prijavljenog korisnika.
// Uloga se uzima iz baze, pa obrisan korisnik ili promenjena uloga vaze odmah.
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUtil jwtUtil;
	@Autowired
	private KorisnikRepository korisnikRepository;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		String header = request.getHeader("Authorization");

		if (header != null && header.startsWith("Bearer ")) {
			String token = header.substring(7);

			if (jwtUtil.jeValidan(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
				String email = jwtUtil.izvuciEmail(token);

				korisnikRepository.findByEmail(email).ifPresent(korisnik -> {
					UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
							korisnik.getEmail(), null,
							List.of(new SimpleGrantedAuthority("ROLE_" + korisnik.getUloga())));
					auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(auth);
				});
			}
		}

		chain.doFilter(request, response);
	}
}
