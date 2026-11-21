package consultme.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import consultme.model.Korisnik;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

	@Value("${consultme.jwt.secret}")
	private String secret;

	@Value("${consultme.jwt.trajanje-minuta}")
	private long trajanjeMinuta;

	private SecretKey kljuc() {
		return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	public String generisiToken(Korisnik korisnik) {
		Date sada = new Date();
		Date istice = new Date(sada.getTime() + trajanjeMinuta * 60 * 1000);
		return Jwts.builder()
				.subject(korisnik.getEmail())
				.claim("id", korisnik.getId())
				.claim("uloga", korisnik.getUloga())
				.issuedAt(sada)
				.expiration(istice)
				.signWith(kljuc())
				.compact();
	}

	public long getTrajanjeSekundi() {
		return trajanjeMinuta * 60;
	}

	public String izvuciEmail(String token) {
		return parsiraj(token).getSubject();
	}

	// potpis ispravan i token nije istekao
	public boolean jeValidan(String token) {
		try {
			parsiraj(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	private Claims parsiraj(String token) {
		return Jwts.parser().verifyWith(kljuc()).build().parseSignedClaims(token).getPayload();
	}
}
