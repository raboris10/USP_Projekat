package rs.ac.singidunum.consultme.model;

// Vrsta notifikacije (kolona notifikacija.tip).
// U komentaru je ko je dobija.
public enum TipNotifikacije {
	REZERVACIJA_POTVRDJENA,   // student: uspešno je rezervisao termin
	NOVA_REZERVACIJA,         // profesor: student mu je rezervisao termin
	REZERVACIJA_OTKAZANA,     // profesor: student je otkazao rezervaciju
	TERMIN_IZMENJEN,          // student: profesor je pomerio termin (vreme ili mesto)
	TERMIN_OTKAZAN,           // student: profesor je otkazao termin
	PODSETNIK_24H,            // student i profesor: termin počinje za 24 sata
	PODSETNIK_12H,            // student i profesor: termin počinje za 12 sati
	PODSETNIK_2H              // student i profesor: termin počinje za 2 sata
}
