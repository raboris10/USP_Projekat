package rs.ac.singidunum.consultme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Početna klasa aplikacije. @SpringBootApplication kaže Spring-u da pretraži
// ovaj paket i sve ispod njega (model, service, controller...) i sam poveže klase.
@SpringBootApplication
public class ConsultmeApplication {

	// main metoda - odavde se pokreće cela aplikacija
	public static void main(String[] args) {
		SpringApplication.run(ConsultmeApplication.class, args);
	}

}
