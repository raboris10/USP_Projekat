package rs.ac.singidunum.consultme.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Telo zahteva kojim student rezerviše termin (POST /api/rezervacije), npr:
// { "terminId": 1, "tema": "Pitanja o projektnoj dokumentaciji" }
public record RezervacijaRequest(

		@NotNull(message = "Termin je obavezan.")
		Long terminId,

		@NotBlank(message = "Tema konsultacija je obavezna.")
		@Size(max = 500, message = "Tema može imati najviše 500 znakova.")
		String tema

) {
}
