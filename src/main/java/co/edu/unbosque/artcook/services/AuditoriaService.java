package co.edu.unbosque.artcook.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.unbosque.artcook.dto.AuditoriaDTO;
import co.edu.unbosque.artcook.entity.Auditoria;
import co.edu.unbosque.artcook.exception.LanzadorExcepciones;
import co.edu.unbosque.artcook.exception.RegistroNoEncontradoException;
import co.edu.unbosque.artcook.repository.AuditoriaRepository;

@Service
public class AuditoriaService implements CRUDOperation<AuditoriaDTO> {

	@Autowired
	private AuditoriaRepository auditoriaRepo;

	@Autowired
	private ModelMapper mapper;

	@Override
	public int create(AuditoriaDTO newData) {
		try {
			LanzadorExcepciones.validarId(newData.getUsuarioId());

			Auditoria auditoria = mapper.map(newData, Auditoria.class);
			auditoria.setFechaAccion(LocalDateTime.now());

			auditoriaRepo.save(auditoria);
			return 0;
		} catch (RegistroNoEncontradoException e) {
			return 1;
		}
	}

	@Override
	public List<AuditoriaDTO> getAll() {
		List<Auditoria> entityList = (List<Auditoria>) auditoriaRepo.findAll();
		List<AuditoriaDTO> dtoList = new ArrayList<>();
		entityList.forEach((entidad) -> {
			AuditoriaDTO dto = mapper.map(entidad, AuditoriaDTO.class);
			dtoList.add(dto);
		});
		return dtoList;
	}

	@Override
	public int deleteById(Long id) {
		return -1;
	}

	@Override
	public int updateById(Long id, AuditoriaDTO newData) {
		return -1;
	}

	@Override
	public long count() {
		return auditoriaRepo.count();
	}

	@Override
	public boolean exist(Long id) {
		return auditoriaRepo.existsById(id);
	}

	public void registrarAccion(Long usuarioId, String accion, String entidad, Long idEntidad, String detalles) {
		Auditoria auditoria = new Auditoria(usuarioId, accion, entidad, idEntidad);
		auditoria.setDetalles(detalles);
		auditoria.setFechaAccion(LocalDateTime.now());
		auditoriaRepo.save(auditoria);
	}

	public List<AuditoriaDTO> obtenerPorUsuario(Long usuarioId) throws RegistroNoEncontradoException {
		try {
			LanzadorExcepciones.validarId(usuarioId);
			List<Auditoria> auditorias = auditoriaRepo.findByUsuarioId(usuarioId);
			List<AuditoriaDTO> dtoList = new ArrayList<>();
			auditorias.forEach((auditoria) -> {
				AuditoriaDTO dto = mapper.map(auditoria, AuditoriaDTO.class);
				dtoList.add(dto);
			});
			return dtoList;
		} catch (RegistroNoEncontradoException e) {
			throw e;
		}
	}

	public List<AuditoriaDTO> obtenerPorAccion(String accion) {
		List<Auditoria> auditorias = auditoriaRepo.findByAccion(accion);
		List<AuditoriaDTO> dtoList = new ArrayList<>();
		auditorias.forEach((auditoria) -> {
			AuditoriaDTO dto = mapper.map(auditoria, AuditoriaDTO.class);
			dtoList.add(dto);
		});
		return dtoList;
	}

	public List<AuditoriaDTO> obtenerPorEntidad(String entidad) {
		List<Auditoria> auditorias = auditoriaRepo.findByEntidad(entidad);
		List<AuditoriaDTO> dtoList = new ArrayList<>();
		auditorias.forEach((auditoria) -> {
			AuditoriaDTO dto = mapper.map(auditoria, AuditoriaDTO.class);
			dtoList.add(dto);
		});
		return dtoList;
	}

	public List<AuditoriaDTO> obtenerPorRango(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
		List<Auditoria> auditorias = auditoriaRepo.findByFechaAccionBetween(fechaInicio, fechaFin);
		List<AuditoriaDTO> dtoList = new ArrayList<>();
		auditorias.forEach((auditoria) -> {
			AuditoriaDTO dto = mapper.map(auditoria, AuditoriaDTO.class);
			dtoList.add(dto);
		});
		return dtoList;
	}
}