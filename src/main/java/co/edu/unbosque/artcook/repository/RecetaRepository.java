package co.edu.unbosque.artcook.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.artcook.entity.Receta;
import co.edu.unbosque.artcook.entity.TipoReceta;

@Repository
public interface RecetaRepository extends JpaRepository<Receta, Long> {

	List<Receta> findByUsuarioId(long usuarioId);

	List<Receta> findByTipoReceta(TipoReceta tipoReceta);

	List<Receta> findByActiva(boolean activa);

	List<Receta> findByUsuarioIdAndTipoReceta(long usuarioId, TipoReceta tipoReceta);

	List<Receta> findByUsuarioIdAndActiva(long usuarioId, boolean activa);

	long countByUsuarioId(long usuarioId);

	long countByTipoReceta(TipoReceta tipoReceta);

	Optional<Receta> findByTitulo(String titulo);

	boolean existsByIaSeleccionada(String iaSeleccionada);
}