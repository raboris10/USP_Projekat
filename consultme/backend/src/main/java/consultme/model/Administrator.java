package consultme.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "administrator")
public class Administrator extends Korisnik {

	public Administrator() {
	}

	@Override
	public String getUloga() {
		return "ADMIN";
	}
}
