package rs.ac.singidunum.consultme.dto;

import rs.ac.singidunum.consultme.model.Katedra;

// Podaci o katedri koje backend vraća frontendu
public record KatedraDto(
		Long id,
		String naziv
) {

	public static KatedraDto od(Katedra katedra) {
		return new KatedraDto(katedra.getId(), katedra.getNaziv());
	}

}
