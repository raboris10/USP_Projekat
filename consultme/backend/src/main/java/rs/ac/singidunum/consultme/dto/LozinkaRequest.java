package rs.ac.singidunum.consultme.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Telo zahteva za reset lozinke (PATCH /api/admin/korisnici/{id}/lozinka), npr:
// { "lozinka": "nova123" }
// Max 72 -> BCrypt gleda samo prva 72 bajta lozinke.
public record LozinkaRequest(

		@NotBlank(message = "Nova lozinka je obavezna.")
		@Size(min = 6, max = 72, message = "Lozinka mora imati između 6 i 72 znaka.")
		String lozinka

) {
}
