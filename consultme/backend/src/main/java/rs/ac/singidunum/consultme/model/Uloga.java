package rs.ac.singidunum.consultme.model;

// Moguće uloge korisnika. U bazi se čuvaju kao tekst (kolona korisnik.uloga).
public enum Uloga {
	STUDENT,
	PROFESOR,
	ADMIN       // IT tehničar koji održava sistem (nije profesor)
}
