// clase modificada camila-juan

package co.edu.unbosque.artcook.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.artcook.entity.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

	Optional<Usuario> findByEmail(String email);

	Optional<Usuario> findByTokenVerificacion(String tokenVerificacion);

	Optional<Usuario> findByTokenRecuperacion(String tokenRecuperacion);

	boolean existsByEmail(String email);

	long countByActivo(boolean activo);
}
