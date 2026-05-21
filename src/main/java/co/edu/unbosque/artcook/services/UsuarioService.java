package co.edu.unbosque.artcook.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import co.edu.unbosque.artcook.dto.LoginResponseDTO;
import co.edu.unbosque.artcook.dto.RolUsuarioDTO;
import co.edu.unbosque.artcook.dto.UsuarioDTO;
import co.edu.unbosque.artcook.entity.RolUsuario;
import co.edu.unbosque.artcook.entity.Usuario;
import co.edu.unbosque.artcook.exception.ContrasenaException;
import co.edu.unbosque.artcook.exception.CredencialesInvalidasException;
import co.edu.unbosque.artcook.exception.EmailDuplicadoException;
import co.edu.unbosque.artcook.exception.EmailFormatoException;
import co.edu.unbosque.artcook.exception.EmailNoVerificadoException;
import co.edu.unbosque.artcook.exception.LanzadorExcepciones;
import co.edu.unbosque.artcook.exception.NombreException;
import co.edu.unbosque.artcook.exception.RegistroNoEncontradoException;
import co.edu.unbosque.artcook.exception.TokenInvalidoException;
import co.edu.unbosque.artcook.exception.UsuarioInactivoException;
import co.edu.unbosque.artcook.repository.UsuarioRepository;

/**
 * Servicio que gestiona las operaciones CRUD y lógica de negocio de los usuarios.
 * Incluye registro, login, verificación de correo, recuperación de contraseña y administración.
 * Las contraseñas se almacenan cifradas con BCrypt.
 */
@Service
public class UsuarioService implements CRUDOperation<UsuarioDTO> {

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private CorreoService correoService;

    /**
     * PasswordEncoder inyectado para cifrar contraseñas con BCrypt.
     * Se integra con Spring Security.
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Constructor por defecto.
     */
    public UsuarioService() {
    }

    /**
     * Registra un nuevo usuario en el sistema.
     * Valida los datos, cifra la contraseña, genera el token de verificación y envía el correo.
     *
     * @param data datos del usuario a registrar
     * @return código de estado: 0 éxito, otro número indica tipo de error
     */
    @Override
    public int create(UsuarioDTO data) {
        try {
            LanzadorExcepciones.verificarNombre(data.getNombre());
            LanzadorExcepciones.verificarFormatoEmail(data.getEmail());
            LanzadorExcepciones.verificarEmailDuplicado(usuarioRepo.existsByEmail(data.getEmail()));
            LanzadorExcepciones.verificarContrasena(data.getContrasena());

            Usuario nuevo = new Usuario(
                data.getNombre(),
                data.getEmail(),
                // la contraseña se hashea con BCrypt antes de guardar
                passwordEncoder.encode(data.getContrasena()),
                RolUsuario.valueOf(data.getRol() != null ? data.getRol().name() : RolUsuarioDTO.USER.name())
            );

            String token = UUID.randomUUID().toString();
            nuevo.setTokenVerificacion(token);
            usuarioRepo.save(nuevo);
            correoService.enviarCorreoVerificacion(data.getEmail(), data.getNombre(), token);

            return 0;
        } catch (NombreException e) {
            return 1;
        } catch (EmailFormatoException e) {
            return 2;
        } catch (EmailDuplicadoException e) {
            return 3;
        } catch (ContrasenaException e) {
            return 4;
        } catch (Exception e) {
            return 5;
        }
    }

    /**
     * Obtiene todos los usuarios registrados.
     *
     * @return lista de DTOs de usuarios
     */
    @Override
    public List<UsuarioDTO> getAll() {
        List<Usuario> entityList = usuarioRepo.findAll();
        List<UsuarioDTO> dtoList = new ArrayList<>();
        entityList.forEach(entidad -> dtoList.add(mapper.map(entidad, UsuarioDTO.class)));
        return dtoList;
    }

    /**
     * Elimina un usuario por su ID.
     *
     * @param id identificador del usuario
     * @return código de estado: 0 éxito, 1 no encontrado
     */
    @Override
    public int deleteByID(Long id) {
        try {
            LanzadorExcepciones.verificarExistencia(usuarioRepo.existsById(id), "Usuario", id);
            usuarioRepo.deleteById(id);
            return 0;
        } catch (RegistroNoEncontradoException e) {
            return 1;
        }
    }

    /**
     * Actualiza los datos de un usuario por su ID.
     *
     * @param id   identificador del usuario
     * @param data nuevos datos del usuario
     * @return código de estado según resultado
     */
    @Override
    public int updateByID(Long id, UsuarioDTO data) {
        try {
            LanzadorExcepciones.verificarExistencia(usuarioRepo.existsById(id), "Usuario", id);
            LanzadorExcepciones.verificarNombre(data.getNombre());

            Usuario temp = usuarioRepo.findById(id).get();
            temp.setNombre(data.getNombre());
            temp.setActivo(data.isActivo());
            if (data.getRol() != null) {
                temp.setRol(RolUsuario.valueOf(data.getRol().name()));
            }
            temp.setFechaActualizacion(LocalDateTime.now());
            usuarioRepo.save(temp);
            return 0;
        } catch (RegistroNoEncontradoException e) {
            return 1;
        } catch (NombreException e) {
            return 2;
        } catch (Exception e) {
            return 3;
        }
    }

    /**
     * Obtiene el total de usuarios registrados.
     *
     * @return número total de usuarios
     */
    @Override
    public long count() {
        return usuarioRepo.count();
    }

    /**
     * Verifica si existe un usuario por su ID.
     *
     * @param id identificador del usuario
     * @return true si existe, false en caso contrario
     */
    @Override
    public boolean exist(Long id) {
        return usuarioRepo.existsById(id);
    }

    /**
     * Autentica un usuario y retorna sus datos para la sesión.
     * Usa BCrypt para comparar la contraseña ingresada con la almacenada.
     *
     * @param email      correo electrónico
     * @param contrasena contraseña en texto plano
     * @return LoginResponseDTO con los datos del usuario autenticado
     * @throws EmailFormatoException          si el formato del email es inválido
     * @throws CredencialesInvalidasException si las credenciales son incorrectas
     * @throws EmailNoVerificadoException     si el email no ha sido verificado
     * @throws UsuarioInactivoException       si el usuario está desactivado
     */
    public LoginResponseDTO login(String email, String contrasena)
            throws EmailFormatoException, CredencialesInvalidasException,
            EmailNoVerificadoException, UsuarioInactivoException {

        LanzadorExcepciones.verificarFormatoEmail(email);

        Optional<Usuario> opt = usuarioRepo.findByEmail(email);
        // CIFRADO: se compara la contraseña en texto plano con el hash BCrypt almacenado
        if (opt.isEmpty() || !passwordEncoder.matches(contrasena, opt.get().getContrasena())) {
            throw new CredencialesInvalidasException();
        }

        Usuario usuario = opt.get();
        LanzadorExcepciones.verificarEmailVerificado(usuario.isEmailVerificado());
        LanzadorExcepciones.verificarUsuarioActivo(usuario.isActivo());

        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepo.save(usuario);

        return new LoginResponseDTO(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getEmail(),
            RolUsuarioDTO.valueOf(usuario.getRol().name()),
            usuario.isEmailVerificado()
        );
    }

    /**
     * Verifica el correo electrónico de un usuario usando el token enviado.
     *
     * @param token token de verificación
     * @return 0 si se verificó correctamente, 1 si el token es inválido
     */
    public int verificarEmail(String token) {
        try {
            Optional<Usuario> opt = usuarioRepo.findByTokenVerificacion(token);
            LanzadorExcepciones.verificarToken(opt.isPresent());
            Usuario usuario = opt.get();
            usuario.setEmailVerificado(true);
            usuario.setTokenVerificacion(null);
            usuarioRepo.save(usuario);
            correoService.enviarCorreoBienvenida(usuario.getEmail(), usuario.getNombre());
            return 0;
        } catch (TokenInvalidoException e) {
            return 1;
        }
    }

    /**
     * Obtiene un usuario por su email.
     *
     * @param email correo electrónico del usuario
     * @return DTO del usuario
     * @throws EmailFormatoException         si el formato del email es inválido
     * @throws RegistroNoEncontradoException si el usuario no existe
     */
    public UsuarioDTO obtenerPorEmail(String email)
            throws EmailFormatoException, RegistroNoEncontradoException {
        LanzadorExcepciones.verificarFormatoEmail(email);
        return usuarioRepo.findByEmail(email)
                .map(u -> mapper.map(u, UsuarioDTO.class))
                .orElseThrow(() -> new RegistroNoEncontradoException(
                        "Usuario con email " + email + " no encontrado."));
    }

    /**
     * Obtiene todos los usuarios con un rol específico.
     *
     * @param rol rol a filtrar
     * @return lista de DTOs de usuarios con ese rol
     */
    public List<UsuarioDTO> getByRol(RolUsuario rol) {
        return usuarioRepo.findByRol(rol).stream()
                .map(u -> mapper.map(u, UsuarioDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Activa un usuario por su ID.
     *
     * @param id identificador del usuario
     * @return 0 si se activó, 1 si no se encontró
     */
    public int activarUsuario(Long id) {
        try {
            LanzadorExcepciones.verificarExistencia(usuarioRepo.existsById(id), "Usuario", id);
            Usuario u = usuarioRepo.findById(id).get();
            u.setActivo(true);
            u.setFechaActualizacion(LocalDateTime.now());
            usuarioRepo.save(u);
            return 0;
        } catch (RegistroNoEncontradoException e) {
            return 1;
        }
    }

    /**
     * Desactiva un usuario por su ID y le envía notificación por correo.
     *
     * @param id identificador del usuario
     * @return 0 si se desactivó, 1 si no se encontró
     */
    public int desactivarUsuario(Long id) {
        try {
            LanzadorExcepciones.verificarExistencia(usuarioRepo.existsById(id), "Usuario", id);
            Usuario u = usuarioRepo.findById(id).get();
            u.setActivo(false);
            u.setFechaActualizacion(LocalDateTime.now());
            usuarioRepo.save(u);
            correoService.enviarCorreoDesactivacion(u.getEmail(), u.getNombre());
            return 0;
        } catch (RegistroNoEncontradoException e) {
            return 1;
        }
    }

    /**
     * Solicita la recuperación de contraseña enviando un token al correo del usuario.
     *
     * @param email correo electrónico del usuario
     * @return 0 éxito, 1 no encontrado, 2 formato inválido, 3 error al enviar correo
     */
    public int solicitarRecuperacion(String email) {
        try {
            LanzadorExcepciones.verificarFormatoEmail(email);
            Optional<Usuario> opt = usuarioRepo.findByEmail(email);
            if (!opt.isPresent()) return 1;
            Usuario usuario = opt.get();
            String token = UUID.randomUUID().toString();
            usuario.setTokenRecuperacion(token);
            usuarioRepo.save(usuario);
            correoService.enviarCorreoRecuperacion(email, usuario.getNombre(), token);
            return 0;
        } catch (EmailFormatoException e) {
            return 2;
        } catch (Exception e) {
            return 3;
        }
    }

    /**
     * Cambia la contraseña del usuario usando el token de recuperación.
     * La nueva contraseña se cifra con BCrypt antes de guardar.
     *
     * @param token           token de recuperación
     * @param nuevaContrasena nueva contraseña en texto plano
     * @return 0 éxito, 1 token inválido, 2 token vacío, 3 contraseña inválida, 4 error inesperado
     */
    public int cambiarContrasena(String token, String nuevaContrasena) {
        try {
            LanzadorExcepciones.validarToken(token);
            Optional<Usuario> opt = usuarioRepo.findByTokenRecuperacion(token);
            if (!opt.isPresent()) return 1;
            LanzadorExcepciones.verificarContrasena(nuevaContrasena);
            Usuario usuario = opt.get();
            usuario.setContrasena(passwordEncoder.encode(nuevaContrasena));
            usuario.setTokenRecuperacion(null);
            usuario.setFechaActualizacion(LocalDateTime.now());
            usuarioRepo.save(usuario);
            return 0;
        } catch (TokenInvalidoException e) {
            return 2;
        } catch (ContrasenaException e) {
            return 3;
        } catch (Exception e) {
            return 4;
        }
    }
}