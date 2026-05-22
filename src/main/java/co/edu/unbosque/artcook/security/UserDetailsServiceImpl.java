package co.edu.unbosque.artcook.security;

import co.edu.unbosque.artcook.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de detalles de usuario para la autenticación de ArtCook.
 * Carga los datos del usuario desde el repositorio durante el proceso de autenticación JWT.
 * Usa el email como identificador único del usuario.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    /** Repositorio de usuarios utilizado para buscar información de usuarios. */
    private final UsuarioRepository usuarioRepository;

    /**
     * Constructor que inicializa el repositorio de usuarios.
     *
     * @param usuarioRepository repositorio de usuarios a utilizar para las consultas
     */
    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Carga los detalles del usuario por su email.
     * El email es el identificador principal usado en ArtCook para la autenticación.
     *
     * @param email email del usuario a buscar
     * @return detalles del usuario encontrado
     * @throws UsernameNotFoundException si no se encuentra el usuario con el email proporcionado
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con email: " + email));
    }
}
