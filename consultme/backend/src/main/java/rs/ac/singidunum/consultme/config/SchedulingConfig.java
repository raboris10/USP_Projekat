package rs.ac.singidunum.consultme.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

// @EnableScheduling -> uključuje @Scheduled metode, tj. poslove koji se sami pokreću
// na svakih X vremena (kod nas: PodsetnikService proverava termine i šalje podsetnike).
@Configuration
@EnableScheduling
public class SchedulingConfig {

}
