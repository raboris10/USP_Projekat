package rs.ac.singidunum.consultme.dto;

// Odgovor na uspešnu prijavu. Frontend čuva token i šalje ga uz svaki sledeći zahtev.
// Ostala polja služe da frontend odmah zna ko je prijavljen (bez dodatnog zahteva).
public record LoginResponse(
		String token,
		String email,
		String ime,
		String prezime,
		String uloga
) {
}
