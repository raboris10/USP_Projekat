package rs.ac.singidunum.consultme.model;

// Stanje termina konsultacija (kolona termin.status)
public enum StatusTermina {
	AKTIVAN,    // termin važi i može da se rezerviše
	OTKAZAN     // profesor je otkazao termin
}
