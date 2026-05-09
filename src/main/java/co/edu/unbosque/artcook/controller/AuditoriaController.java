package co.edu.unbosque.artcook.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.artcook.dto.AuditoriaDTO;
import co.edu.unbosque.artcook.exception.RegistroNoEncontradoException;
import co.edu.unbosque.artcook.services.AuditoriaService;

@RestController
@RequestMapping("/auditoria")
@CrossOrigin(origins = { "http://localhost:8081", "*" })
public class AuditoriaController {

	@Autowired
	private AuditoriaService auditoriaService;

	@GetMapping("/mostrartodo")
	public ResponseEntity<List<AuditoriaDTO>> obtenerTodo() {
		List<AuditoriaDTO> lista = auditoriaService.getAll();
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
	}

	@GetMapping("/porusuario")
	public ResponseEntity<List<AuditoriaDTO>> obtenerPorUsuario(@RequestParam Long usuarioId) {
		try {
			List<AuditoriaDTO> lista = auditoriaService.obtenerPorUsuario(usuarioId);
			if (lista.isEmpty()) {
				return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
		} catch (RegistroNoEncontradoException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@GetMapping("/poraccion")
	public ResponseEntity<List<AuditoriaDTO>> obtenerPorAccion(@RequestParam String accion) {
		List<AuditoriaDTO> lista = auditoriaService.obtenerPorAccion(accion);
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
	}

	@GetMapping("/porentidad")
	public ResponseEntity<List<AuditoriaDTO>> obtenerPorEntidad(@RequestParam String entidad) {
		List<AuditoriaDTO> lista = auditoriaService.obtenerPorEntidad(entidad);
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
	}

	@GetMapping("/porRango")
	public ResponseEntity<?> obtenerPorRango(@RequestParam String fechaInicio,
			@RequestParam String fechaFin) {
		try {
			LocalDateTime inicio = LocalDateTime.parse(fechaInicio);
			LocalDateTime fin = LocalDateTime.parse(fechaFin);
			List<AuditoriaDTO> lista = auditoriaService.obtenerPorRango(inicio, fin);
			if (lista.isEmpty()) {
				return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
		} catch (Exception e) {
			return new ResponseEntity<>(
				"Formato de fecha invalido. Use el formato: yyyy-MM-ddTHH:mm:ss",
				HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/contar")
	public ResponseEntity<Long> contar() {
		return new ResponseEntity<>(auditoriaService.count(), HttpStatus.OK);
	}

	@GetMapping("/existe")
	public ResponseEntity<Boolean> existe(@RequestParam Long id) {
		return new ResponseEntity<>(auditoriaService.exist(id), HttpStatus.OK);
	}
}