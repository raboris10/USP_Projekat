package rs.ac.singidunum.consultme.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import rs.ac.singidunum.consultme.security.JwtFilter;
import rs.ac.singidunum.consultme.security.JwtService;

// Glavna podešavanja bezbednosti: ko sme na koju rutu i kako se proverava prijava.
// @Configuration -> klasa sa podešavanjima; @Bean metode prave objekte koje Spring koristi.
@Configuration
public class SecurityConfig {

	private final JwtService jwtService;

	public SecurityConfig(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			// CSRF zaštita je za aplikacije sa kolačićima (sesijama). Mi koristimo JWT, pa je gasimo.
			.csrf(AbstractHttpConfigurer::disable)

			// Gasimo Spring-ovu podrazumevanu login stranicu i "basic" prijavu - imamo svoj login
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)

			// STATELESS -> server ne pamti prijavljene korisnike (nema sesije).
			// Svaki zahtev sam donosi token koji dokazuje ko je korisnik.
			.sessionManagement(sesija -> sesija.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

			// Pravila pristupa - proveravaju se redom, odozgo nadole
			.authorizeHttpRequests(pravila -> pravila
				// login mora biti dostupan svima (inače niko ne bi mogao da se prijavi)
				.requestMatchers("/api/auth/login").permitAll()
				// Swagger dokumentacija je dostupna svima
				.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
				// Spring-ova stranica za greške
				.requestMatchers("/error").permitAll()

				// PRIMERI za kasnije (pravila po ulogama):
				// .requestMatchers("/api/admin/**").hasRole("ADMIN")
				// .requestMatchers(HttpMethod.POST, "/api/termini/**").hasRole("PROFESOR")

				// sve ostalo traži prijavljenog korisnika (ispravan token)
				.anyRequest().authenticated())

			// Šta se vraća kad nešto nije dozvoljeno (JSON, kao i ostale greške)
			.exceptionHandling(greske -> greske
				// 401 - korisnik nije prijavljen ili mu je token istekao
				.authenticationEntryPoint((request, response, ex) -> {
					response.setStatus(401);
					response.setContentType("application/json;charset=UTF-8");
					response.getWriter().write("{\"poruka\":\"Niste prijavljeni ili je prijava istekla.\"}");
				})
				// 403 - korisnik je prijavljen, ali njegova uloga nema pravo na ovo
				.accessDeniedHandler((request, response, ex) -> {
					response.setStatus(403);
					response.setContentType("application/json;charset=UTF-8");
					response.getWriter().write("{\"poruka\":\"Nemate pravo pristupa.\"}");
				}))

			// Naš JwtFilter se izvršava pre Spring-ovog filtera za prijavu
			.addFilterBefore(new JwtFilter(jwtService), UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	// BCrypt za lozinke: encode() pravi heš, matches() poredi lozinku sa hešom.
	// Ovaj objekat koristi AuthService pri prijavi.
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
