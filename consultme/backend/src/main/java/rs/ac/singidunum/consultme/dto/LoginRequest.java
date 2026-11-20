package rs.ac.singidunum.consultme.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// DTO (Data Transfer Object) = klasa samo za prenos podataka između frontenda i backenda.
// "record" je kratak zapis klase: Java sama pravi konstruktor i gettere (email(), lozinka()).
//
// Ovo je telo zahteva POST /api/auth/login, npr:
// { "email": "student@consultme.rs", "lozinka": "student123" }
public record LoginRequest(

		@NotBlank(message = "Email je obavezan.")
		@Email(message = "Email nije u ispravnom formatu.")
		String email,

		@NotBlank(message = "Lozinka je obavezna.")
		String lozinka

) {
}
