package rs.ac.singidunum.consultme.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Telo zahteva za dodavanje i izmenu katedre (POST i PUT /api/katedre), npr:
// { "naziv": "Katedra za informatiku i računarstvo" }
public record KatedraRequest(

		@NotBlank(message = "Naziv katedre je obavezan.")
		@Size(max = 150, message = "Naziv katedre može imati najviše 150 znakova.")
		String naziv

) {
}
