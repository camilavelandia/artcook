package co.edu.unbosque.artcook.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import co.edu.unbosque.artcook.entity.Receta;
import co.edu.unbosque.artcook.entity.TipoReceta;

/**
 * Repositorio para gestionar operaciones de la entidad Receta en la base de datos.
 */
@Repository
public interface RecetaRepository extends JpaRepository<Receta, Long> {

    /**
     * Obtiene todas las recetas de un usuario específico por su ID.
     *
     * @param usuarioId ID del usuario
     * @return lista de recetas del usuario
     */
    List<Receta> findByUsuarioId(long usuarioId);

    /**
     * Obtiene todas las recetas de un tipo específico.
     *
     * @param tipoReceta tipo de receta (COCINA o MANUALIDAD)
     * @return lista de recetas del tipo indicado
     */
    List<Receta> findByTipoReceta(TipoReceta tipoReceta);

    /**
     * Obtiene todas las recetas según su estado activo/inactivo.
     *
     * @param activa estado de la receta
     * @return lista de recetas activas o inactivas
     */
    List<Receta> findByActiva(boolean activa);

    /**
     * Obtiene recetas de un usuario filtradas por tipo.
     *
     * @param usuarioId  ID del usuario
     * @param tipoReceta tipo de receta
     * @return lista de recetas filtradas
     */
    List<Receta> findByUsuarioIdAndTipoReceta(long usuarioId, TipoReceta tipoReceta);

    /**
     * Obtiene recetas de un usuario filtradas por estado activo.
     *
     * @param usuarioId ID del usuario
     * @param activa    estado de la receta
     * @return lista de recetas del usuario según estado
     */
    List<Receta> findByUsuarioIdAndActiva(long usuarioId, boolean activa);

    /**
     * Busca recetas cuyo título contenga el texto dado (búsqueda insensible a mayúsculas).
     *
     * @param titulo texto a buscar en el título
     * @return lista de recetas que coinciden
     */
    @Query("SELECT r FROM Receta r WHERE LOWER(r.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))")
    List<Receta> findByTituloContaining(@Param("titulo") String titulo);

    /**
     * Busca una receta por su título exacto.
     *
     * @param titulo título a buscar
     * @return Optional con la receta si se encuentra
     */
    Optional<Receta> findByTitulo(String titulo);

    /**
     * Cuenta el total de recetas de un usuario.
     *
     * @param usuarioId ID del usuario
     * @return cantidad de recetas del usuario
     */
    long countByUsuarioId(long usuarioId);

    /**
     * Cuenta el total de recetas de un tipo específico.
     *
     * @param tipoReceta tipo de receta
     * @return cantidad de recetas del tipo indicado
     */
    long countByTipoReceta(TipoReceta tipoReceta);

    /**
     * Verifica si existe al menos una receta con la IA seleccionada indicada.
     *
     * @param iaSeleccionada nombre de la IA
     * @return true si existe, false en caso contrario
     */
    boolean existsByIaSeleccionada(String iaSeleccionada);
}