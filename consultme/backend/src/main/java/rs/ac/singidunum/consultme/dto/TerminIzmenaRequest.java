package rs.ac.singidunum.consultme.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Telo zahteva kojim profesor menja (pomera) termin (PUT /api/termini/{id}).
// Šalju se sva polja, i ona koja se ne menjaju.
// Predmet se ne menja - za drugi predmet se pravi novi termin.
public record TerminIzmenaRequest(

		@NotNull(message = "Početak termina je obavezan.")
		@Future(message = "Termin mora biti u budućnosti.")
		LocalDateTime pocetak,

		@NotNull(message = "Trajanje je obavezno.")
		@Min(value = 10, message = "Termin traje najmanje 10 minuta.")
		@Max(value = 240, message = "Termin traje najviše 240 minuta.")
		Integer trajanjeMin,

		@NotBlank(message = "Mesto je obavezno (kabinet ili link za onlajn sastanak).")
		@Size(max = 255, message = "Mesto može imati najviše 255 znakova.")
		String mesto,

		@NotNull(message = "Kapacitet je obavezan.")
		@Min(value = 1, message = "Kapacitet mora biti bar 1.")
		@Max(value = 100, message = "Kapacitet može biti najviše 100.")
		Integer kapacitet

) {
}
