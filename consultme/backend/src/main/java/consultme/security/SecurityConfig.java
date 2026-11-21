package consultme.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

	@Autowired
	private JwtAuthFilter jwtAuthFilter;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.cors(Customizer.withDefaults())
			.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.exceptionHandling(e -> e.authenticationEntryPoint(
					(req, res, ex) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Niste prijavljeni")))
			.authorizeHttpRequests(auth -> auth
					
				// prijava je jedini javni endpoint (nema registracije)
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.requestMatchers("/api/auth/login").permitAll()
				

				// termini: pregled svih za svakog prijavljenog, a spisak rezervacija i CSV samo za nastavnike
				.requestMatchers(HttpMethod.GET, "/api/termini/moji", "/api/termini/*/rezervacije",
						"/api/termini/*/evidencija/csv").hasRole("NASTAVNIK")
				.requestMatchers(HttpMethod.GET, "/api/termini/**", "/api/nastavnici/**",
						"/api/katedre/**", "/api/predmeti/**").authenticated()

				// administrator: CRUD studenata, nastavnika, katedri i predmeta
				.requestMatchers("/api/studenti/**", "/api/nastavnici/**",
						"/api/katedre/**", "/api/predmeti/**").hasRole("ADMIN")

				// nastavnik: definisanje, pomeranje i otkazivanje termina, evidencija dolazaka
				.requestMatchers("/api/termini/**").hasRole("NASTAVNIK")
				.requestMatchers("/api/rezervacije/*/evidencija").hasRole("NASTAVNIK")

				// student: rezervacije i notifikacije
				.requestMatchers("/api/rezervacije/**", "/api/notifikacije/**").hasRole("STUDENT")
				.requestMatchers(
					        "/swagger-ui/**",
					        "/swagger-ui.html",
					        "/v3/api-docs/**",
					        "/v3/api-docs.yaml"
					    ).permitAll()
				.anyRequest().authenticated())
			.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	// dozvoljava Angular frontendu (localhost:4200) da zove API
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration cfg = new CorsConfiguration();
		cfg.setAllowedOrigins(List.of("http://localhost:4200","http://localhost:8087"));
		cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		cfg.setAllowedHeaders(List.of("*"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", cfg);
		return source;
	}
}
