package rs.ac.singidunum.consultme.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import rs.ac.singidunum.consultme.model.Korisnik;

// Pravi i proverava JWT tokene.
//
// JWT token ima 3 dela odvojena tačkom:  zaglavlje.SADRŽAJ.potpis
// - sadržaj (claims): email, uloga, kada ističe... (svako može da ga PROČITA)
// - potpis: napravljen tajnim ključem koji zna samo backend.
//   Ako neko promeni sadržaj (npr. STUDENT -> ADMIN), potpis više ne odgovara
//   i backend odbija token. Zato je token bezbedan iako je sadržaj čitljiv.
@Service
public class JwtService {

	// Ključ kojim se tokeni potpisuju (pravi se iz teksta iz .env -> JWT_SECRET)
	private final SecretKey kljuc;

	// Koliko minuta token važi (iz .env -> JWT_TRAJANJE_MINUTA)
	private final long trajanjeMinuta;

	// @Value čita vrednosti iz application.properties (a one iz .env fajla)
	public JwtService(
			@Value("${jwt.tajni-kljuc}") String tajniKljuc,
			@Value("${jwt.trajanje-minuta}") long trajanjeMinuta) {

		// HMAC-SHA ključ iz teksta; tekst mora imati bar 32 znaka (256 bita)
		this.kljuc = Keys.hmacShaKeyFor(tajniKljuc.getBytes(StandardCharsets.UTF_8));
		this.trajanjeMinuta = trajanjeMinuta;
	}

	// Pravi novi token za korisnika koji se upravo uspešno prijavio
	public String napraviToken(Korisnik korisnik) {
		Date sada = new Date();
		Date istice = new Date(sada.getTime() + trajanjeMinuta * 60 * 1000);   // minuti -> milisekunde

		return Jwts.builder()
				.subject(korisnik.getEmail())                  // "ko je" - email korisnika
				.claim("uloga", korisnik.getUloga().name())     // dodatni podatak: uloga
				.issuedAt(sada)                                 // kada je napravljen
				.expiration(istice)                             // kada ističe
				.signWith(kljuc)                                // potpis tajnim ključem
				.compact();                                     // sve spoji u jedan string
	}

	// Proverava potpis i rok važenja i vraća sadržaj tokena.
	// Ako token nije ispravan ili je istekao, baca JwtException.
	public Claims procitajToken(String token) {
		return Jwts.parser()
				.verifyWith(kljuc)           // proveri potpis našim ključem
				.build()
				.parseSignedClaims(token)    // pročitaj token (ovde pukne ako je neispravan)
				.getPayload();               // vrati sadržaj (claims)
	}

}
