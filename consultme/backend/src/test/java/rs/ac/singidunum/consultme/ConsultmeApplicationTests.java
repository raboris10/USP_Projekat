package rs.ac.singidunum.consultme;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Ovaj test pokreće celu aplikaciju, pa mu treba baza koja radi.
// Zato se u Dockerfile-u testovi preskaču (-DskipTests).
@SpringBootTest
class ConsultmeApplicationTests {

	@Test
	void contextLoads() {
	}

}
