package co.edu.unbosque.artcook.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import co.edu.unbosque.artcook.entity.Auditoria;

/**
 * Repositorio para gestionar los registros de auditoría del sistema.
 */
@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    /**
     * Obtiene todos los registros de auditoría de un usuario específico por su ID.
     *
     * @param usuarioId ID del usuario
     * @return lista de registros del usuario
     */
    List<Auditoria> findByUsuarioId(long usuarioId);

    /**
     * Obtiene todos los registros de auditoría de una acción específica.
     *
     * @param accion acción a filtrar
     * @return lista de registros con esa acción
     */
    List<Auditoria> findByAccion(String accion);

    /**
     * Obtiene todos los registros de auditoría de una entidad específica.
     *
     * @param entidad nombre de la entidad
     * @return lista de registros de esa entidad
     */
    List<Auditoria> findByEntidad(String entidad);

    /**
     * Obtiene registros de un usuario filtrados por acción.
     *
     * @param usuarioId ID del usuario
     * @param accion    acción a filtrar
     * @return lista de registros del usuario con esa acción
     */
    List<Auditoria> findByUsuarioIdAndAccion(long usuarioId, String accion);

    /**
     * Obtiene registros de auditoría entre dos fechas.
     *
     * @param fechaInicio fecha de inicio
     * @param fechaFin    fecha de fin
     * @return lista de registros en ese rango de fechas
     */
    List<Auditoria> findByFechaAccionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Obtiene registros de un usuario en un rango de fechas.
     *
     * @param usuarioId   ID del usuario
     * @param fechaInicio fecha de inicio
     * @param fechaFin    fecha de fin
     * @return lista de registros filtrados
     */
    List<Auditoria> findByUsuarioIdAndFechaAccionBetween(long usuarioId, LocalDateTime fechaInicio,
            LocalDateTime fechaFin);

    /**
     * Cuenta el total de registros de un usuario.
     *
     * @param usuarioId ID del usuario
     * @return cantidad de registros del usuario
     */
    long countByUsuarioId(long usuarioId);

    /**
     * Cuenta el total de registros de una acción específica.
     *
     * @param accion acción a contar
     * @return cantidad de registros con esa acción
     */
    long countByAccion(String accion);

    /**
     * Obtiene todos los registros ordenados del más reciente al más antiguo.
     *
     * @return lista de registros ordenados por fecha descendente
     */
    @Query("SELECT a FROM Auditoria a ORDER BY a.fechaAccion DESC")
    List<Auditoria> findAllOrderByFechaDesc();
}
