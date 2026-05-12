// clase modificada camila-juan

package co.edu.unbosque.artcook.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.artcook.entity.Auditoria;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

	List<Auditoria> findByUsuarioId(long usuarioId);

	List<Auditoria> findByAccion(String accion);

	List<Auditoria> findByEntidad(String entidad);

	List<Auditoria> findByUsuarioIdAndAccion(long usuarioId, String accion);

	List<Auditoria> findByFechaAccionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

	List<Auditoria> findByUsuarioIdAndFechaAccionBetween(long usuarioId, LocalDateTime fechaInicio,
			LocalDateTime fechaFin);

	long countByUsuarioId(long usuarioId);

	long countByAccion(String accion);
}
