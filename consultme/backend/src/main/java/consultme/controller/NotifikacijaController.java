package consultme.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import consultme.model.Notifikacija;
import consultme.service.NotifikacijaService;

@RestController
@RequestMapping("/api/notifikacije")
public class NotifikacijaController {

	@Autowired
	private NotifikacijaService notifikacijaService;

	// notifikacije prijavljenog studenta
	@GetMapping("/moje")
	public ResponseEntity<List<Notifikacija>> getMoje(Authentication authentication) {
		return ResponseEntity.status(HttpStatus.OK).body(notifikacijaService.getMoje(authentication.getName()));
	}
}
