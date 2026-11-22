package rs.ac.singidunum.consultme.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Telo zahteva za dodavanje i izmenu predmeta (POST i PUT /api/predmeti), npr:
// { "naziv": "Veb programiranje", "sifra": "VP", "katedraId": 1, "profesorIds": [2, 5] }
//
// profesorIds nije obavezno - predmet može da se doda i bez profesora, pa da se oni dodele kasnije.
public record PredmetRequest(

		@NotBlank(message = "Naziv predmeta je obavezan.")
		@Size(max = 150, message = "Naziv predmeta može imati najviše 150 znakova.")
		String naziv,

		@NotBlank(message = "Šifra predmeta je obavezna.")
		@Size(max = 20, message = "Šifra predmeta može imati najviše 20 znakova.")
		String sifra,

		@NotNull(message = "Katedra je obavezna.")
		Long katedraId,

		List<Long> profesorIds

) {
}
