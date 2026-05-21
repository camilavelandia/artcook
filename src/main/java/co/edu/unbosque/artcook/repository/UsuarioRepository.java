package co.edu.unbosque.artcook.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import co.edu.unbosque.artcook.entity.RolUsuario;
import co.edu.unbosque.artcook.entity.Usuario;

/**
 * Repositorio para gestionar operaciones de la entidad Usuario en la base de datos.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su correo electrónico.
     *
     * @param email correo electrónico del usuario
     * @return Optional con el usuario si se encuentra
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verifica si existe un usuario con el email dado.
     *
     * @param email correo electrónico a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByEmail(String email);

    /**
     * Busca un usuario por su token de verificación de correo.
     *
     * @param tokenVerificacion token de verificación
     * @return Optional con el usuario si se encuentra
     */
    Optional<Usuario> findByTokenVerificacion(String tokenVerificacion);

    /**
     * Busca un usuario por su token de recuperación de contraseña.
     *
     * @param tokenRecuperacion token de recuperación
     * @return Optional con el usuario si se encuentra
     */
    Optional<Usuario> findByTokenRecuperacion(String tokenRecuperacion);

    /**
     * Busca un usuario por email y contraseña para autenticación.
     *
     * @param email      correo electrónico
     * @param contrasena contraseña
     * @return Optional con el usuario si las credenciales coinciden
     */
    @Query("SELECT u FROM Usuario u WHERE u.email = :email AND u.contrasena = :contrasena")
    Optional<Usuario> findByEmailAndContrasena(@Param("email") String email,
            @Param("contrasena") String contrasena);

    /**
     * Obtiene todos los usuarios con un rol específico.
     *
     * @param rol rol a filtrar
     * @return lista de usuarios con ese rol
     */
    List<Usuario> findByRol(RolUsuario rol);

    /**
     * Obtiene todos los usuarios activos o inactivos.
     *
     * @param activo estado activo
     * @return lista de usuarios según estado
     */
    List<Usuario> findByActivo(boolean activo);

    /**
     * Obtiene todos los usuarios con email verificado o no.
     *
     * @param emailVerificado estado de verificación
     * @return lista de usuarios según estado de verificación
     */
    List<Usuario> findByEmailVerificado(boolean emailVerificado);

    /**
     * Cuenta el total de usuarios activos o inactivos.
     *
     * @param activo estado activo
     * @return cantidad de usuarios con ese estado
     */
    long countByActivo(boolean activo);
}