// Notifikacija (NotifikacijaDto.java)
export interface Notifikacija {
  id: number;
  tip: string;            // REZERVACIJA_POTVRDJENA, TERMIN_OTKAZAN, PODSETNIK_24H...
  poruka: string;
  terminId: number | null;
  procitana: boolean;
  kreirana: string;       // datum i vreme kao tekst, npr. "2026-11-22T18:30:00"
}
