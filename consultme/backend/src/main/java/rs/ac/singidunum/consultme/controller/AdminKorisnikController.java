package rs.ac.singidunum.consultme.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import rs.ac.singidunum.consultme.dto.KorisnikAdminDto;
import rs.ac.singidunum.consultme.dto.KorisnikIzmenaRequest;
import rs.ac.singidunum.consultme.dto.KorisnikRequest;
import rs.ac.singidunum.consultme.dto.LozinkaRequest;
import rs.ac.singidunum.consultme.model.Uloga;
import rs.ac.singidunum.consultme.service.AdminKorisnikService;

// Upravljanje korisnicima - sve rute su samo za administratora.
// Prefiks /api/admin -> odmah se vidi da je ruta administratorska.
@RestController
@RequestMapping("/api/admin/korisnici")
@PreAuthorize("hasRole('ADMIN')")   // važi za SVE metode u ovoj klasi
public class AdminKorisnikController {

	private final AdminKorisnikService adminKorisnikService;

	public AdminKorisnikController(AdminKorisnikService adminKorisnikService) {
		this.adminKorisnikService = adminKorisnikService;
	}

	// GET
	// ruta: /api/admin/korisnici?uloga=STUDENT&pretraga=ana  (oba parametra opciona)
	@GetMapping
	public List<KorisnikAdminDto> pretrazi(
			@RequestParam(required = false) Uloga uloga,
			@RequestParam(required = false) String pretraga) {
		return adminKorisnikService.pretrazi(uloga, pretraga);
	}

	// GET
	// ruta: /api/admin/korisnici/{id}
	@GetMapping("/{id}")
	public KorisnikAdminDto findById(@PathVariable Long id) {
		return adminKorisnikService.findById(id);
	}

	// POST
	// ruta: /api/admin/korisnici -> novi korisnik
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public KorisnikAdminDto create(@Valid @RequestBody KorisnikRequest zahtev) {
		return adminKorisnikService.create(zahtev);
	}

	// PUT
	// ruta: /api/admin/korisnici/{id} -> izmena imena, prezimena, email-a i uloge
	@PutMapping("/{id}")
	public KorisnikAdminDto update(@PathVariable Long id, @Valid @RequestBody KorisnikIzmenaRequest zahtev,
			Authentication authentication) {
		return adminKorisnikService.update(authentication.getName(), id, zahtev);
	}

	// PATCH
	// ruta: /api/admin/korisnici/{id}/lozinka -> reset lozinke
	// 204 No Content -> uspešno, nema šta da se vrati
	@PatchMapping("/{id}/lozinka")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void resetujLozinku(@PathVariable Long id, @Valid @RequestBody LozinkaRequest zahtev) {
		adminKorisnikService.resetujLozinku(id, zahtev.lozinka());
	}

	// PATCH
	// ruta: /api/admin/korisnici/{id}/deaktiviraj
	@PatchMapping("/{id}/deaktiviraj")
	public KorisnikAdminDto deaktiviraj(@PathVariable Long id, Authentication authentication) {
		return adminKorisnikService.deaktiviraj(authentication.getName(), id);
	}

	// PATCH
	// ruta: /api/admin/korisnici/{id}/aktiviraj
	@PatchMapping("/{id}/aktiviraj")
	public KorisnikAdminDto aktiviraj(@PathVariable Long id) {
		return adminKorisnikService.aktiviraj(id);
	}

}
