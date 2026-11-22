package rs.ac.singidunum.consultme.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import rs.ac.singidunum.consultme.model.Uloga;

// Telo zahteva kojim administrator menja korisnika (PUT /api/admin/korisnici/{id}).
// Lozinka se ovde ne menja - za to postoji poseban zahtev (reset lozinke).
public record KorisnikIzmenaRequest(

		@NotBlank(message = "Ime je obavezno.")
		@Size(max = 50, message = "Ime može imati najviše 50 znakova.")
		String ime,

		@NotBlank(message = "Prezime je obavezno.")
		@Size(max = 50, message = "Prezime može imati najviše 50 znakova.")
		String prezime,

		@NotBlank(message = "Email je obavezan.")
		@Email(message = "Email nije u ispravnom formatu.")
		@Size(max = 100, message = "Email može imati najviše 100 znakova.")
		String email,

		@NotNull(message = "Uloga je obavezna (STUDENT, PROFESOR ili ADMIN).")
		Uloga uloga

) {
}
