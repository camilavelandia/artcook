package co.edu.unbosque.artcook.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.artcook.dto.LoginResponseDTO;
import co.edu.unbosque.artcook.dto.RolUsuarioDTO;
import co.edu.unbosque.artcook.dto.UsuarioDTO;
import co.edu.unbosque.artcook.entity.RolUsuario;
import co.edu.unbosque.artcook.entity.Usuario;
import co.edu.unbosque.artcook.exception.CredencialesInvalidasException;
import co.edu.unbosque.artcook.exception.EmailFormatoException;
import co.edu.unbosque.artcook.exception.EmailNoVerificadoException;
import co.edu.unbosque.artcook.exception.RegistroNoEncontradoException;
import co.edu.unbosque.artcook.exception.UsuarioInactivoException;
import co.edu.unbosque.artcook.repository.UsuarioRepository;
import co.edu.unbosque.artcook.security.JwtUtil;
import co.edu.unbosque.artcook.services.AuditoriaService;
import co.edu.unbosque.artcook.services.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Controlador que gestiona las operaciones relacionadas con los usuarios.
 * Permite registro, login con JWT, verificación de correo,
 * recuperación de contraseña y administración.
 */
@RestController
@RequestMapping("/usuario")
@CrossOrigin(origins = { "http://localhost:4200", "http://localhost:4201", "http://localhost:8081", "*" })
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioSer;

    @Autowired
    private AuditoriaService auditoriaService;

    /**
     * JwtUtil inyectado para generar el token JWT al hacer login exitoso.
     */
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * UsuarioRepository inyectado para cargar el UserDetails al generar el token.
     */
    @Autowired
    private UsuarioRepository usuarioRepo;

    /**
     * Registra un nuevo usuario en el sistema.
     * Envía un correo de verificación al email ingresado.
     *
     * @param nombre     nombre completo del usuario
     * @param email      correo electrónico
     * @param contrasena contraseña
     * @param request    petición HTTP para auditoría
     * @return mensaje de resultado de la operación
     */
    @PostMapping("/registrar")
    public ResponseEntity<String> registrar(@RequestParam String nombre, @RequestParam String email,
            @RequestParam String contrasena, HttpServletRequest request) {

        UsuarioDTO nuevo = new UsuarioDTO(nombre, email, contrasena, RolUsuarioDTO.USER);

        int resultado;
        try {
            resultado = usuarioSer.create(nuevo);
        } catch (Exception e) {
            return new ResponseEntity<>("Error inesperado.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        switch (resultado) {
        case 0:
            auditoriaService.registrarAccion(0L, "REGISTRO", "Usuario", null,
                    "Nuevo usuario registrado: " + email);
            return new ResponseEntity<>("Registro exitoso. Revisa tu correo para verificar tu cuenta.",
                    HttpStatus.CREATED);
        case 1:
            return new ResponseEntity<>("El nombre debe tener minimo 3 caracteres y solo letras.",
                    HttpStatus.BAD_REQUEST);
        case 2:
            return new ResponseEntity<>("El formato del correo no es valido.", HttpStatus.BAD_REQUEST);
        case 3:
            return new ResponseEntity<>("El correo ya esta registrado.", HttpStatus.CONFLICT);
        case 4:
            return new ResponseEntity<>(
                    "La contrasena debe tener minimo 8 caracteres, un numero y un caracter especial.",
                    HttpStatus.BAD_REQUEST);
        case 5:
            return new ResponseEntity<>("Error al enviar el correo de verificacion. Intente de nuevo.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        default:
            return new ResponseEntity<>("Error inesperado.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Verifica el correo electrónico del usuario usando el token enviado por email.
     *
     * @param token   token de verificación
     * @param request petición HTTP para auditoría
     * @return mensaje de resultado de la verificación
     */
    @GetMapping("/verificar")
    public ResponseEntity<String> verificarCorreo(@RequestParam String token,
            HttpServletRequest request) {
        int resultado = usuarioSer.verificarEmail(token);
        switch (resultado) {
        case 0:
            auditoriaService.registrarAccion(0L, "VERIFICAR_EMAIL", "Usuario", null,
                    "Email verificado exitosamente.");
            return new ResponseEntity<>("Correo verificado exitosamente. Ya puedes iniciar sesion.",
                    HttpStatus.ACCEPTED);
        case 1:
            return new ResponseEntity<>("Token de verificacion invalido o ya utilizado.",
                    HttpStatus.BAD_REQUEST);
        default:
            return new ResponseEntity<>("Error inesperado.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Autentica un usuario con email y contraseña.
     * Retorna los datos del usuario MÁS el token JWT para gestionar la sesión en el frontend.
     * El frontend debe guardar este token y enviarlo en cada petición como:
     * Authorization: Bearer {token}
     *
     * @param email      correo electrónico
     * @param contrasena contraseña
     * @param request    petición HTTP para auditoría
     * @return mapa con el token JWT y los datos del usuario (LoginResponseDTO)
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String contrasena,
            HttpServletRequest request) {
        try {
            LoginResponseDTO response = usuarioSer.login(email, contrasena);

            // Carga el UserDetails para generar el token JWT
            Usuario usuario = usuarioRepo.findByEmail(email)
                    .orElseThrow(() -> new CredencialesInvalidasException());

            // Genera el token JWT con el email, rol e ID del usuario
            String token = jwtUtil.generateToken(usuario);

            // Registra el login en auditoría
            auditoriaService.registrarAccion(response.getId(), "LOGIN", "Usuario",
                    response.getId(), "Login exitoso.");

            // Retorna tanto el token como los datos del usuario en un solo objeto
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("token", token);
            resultado.put("tipo", "Bearer");
            resultado.put("id", response.getId());
            resultado.put("nombre", response.getNombre());
            resultado.put("email", response.getEmail());
            resultado.put("rol", response.getRol());
            resultado.put("emailVerificado", response.isEmailVerificado());

            return new ResponseEntity<>(resultado, HttpStatus.OK);

        } catch (EmailFormatoException e) {
            return new ResponseEntity<>("Formato de correo invalido.", HttpStatus.BAD_REQUEST);
        } catch (CredencialesInvalidasException e) {
            return new ResponseEntity<>("Correo o contrasena incorrectos.", HttpStatus.UNAUTHORIZED);
        } catch (EmailNoVerificadoException e) {
            return new ResponseEntity<>("Debe verificar su correo antes de iniciar sesion.",
                    HttpStatus.FORBIDDEN);
        } catch (UsuarioInactivoException e) {
            return new ResponseEntity<>("Su cuenta ha sido desactivada. Contacte al administrador.",
                    HttpStatus.FORBIDDEN);
        }
    }

    /**
     * Obtiene todos los usuarios registrados. Solo para administradores.
     *
     * @return lista de usuarios
     */
    @GetMapping("/mostrartodo")
    public ResponseEntity<List<UsuarioDTO>> obtenerTodo() {
        List<UsuarioDTO> lista = usuarioSer.getAll();
        if (lista.isEmpty()) {
            return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
    }

    /**
     * Obtiene un usuario por su email.
     *
     * @param email correo electrónico del usuario
     * @return datos del usuario
     */
    @GetMapping("/poremail")
    public ResponseEntity<UsuarioDTO> obtenerPorEmail(@RequestParam String email) {
        try {
            UsuarioDTO usuario = usuarioSer.obtenerPorEmail(email);
            return new ResponseEntity<>(usuario, HttpStatus.OK);
        } catch (EmailFormatoException | RegistroNoEncontradoException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Actualiza los datos de un usuario.
     *
     * @param id      identificador del usuario
     * @param nombre  nuevo nombre
     * @param activo  estado activo/inactivo
     * @param request petición HTTP para auditoría
     * @return mensaje de resultado
     */
    @PutMapping("/actualizar")
    public ResponseEntity<String> actualizar(@RequestParam Long id, @RequestParam String nombre,
            @RequestParam boolean activo, HttpServletRequest request) {

        UsuarioDTO dto = new UsuarioDTO();
        dto.setNombre(nombre);
        dto.setActivo(activo);

        int resultado;
        try {
            resultado = usuarioSer.updateByID(id, dto);
        } catch (Exception e) {
            return new ResponseEntity<>("Error inesperado.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        switch (resultado) {
        case 0:
            auditoriaService.registrarAccion(id, "ACTUALIZAR_USUARIO", "Usuario",
                    id, "Usuario ID " + id + " actualizado.");
            return new ResponseEntity<>("Usuario actualizado exitosamente.", HttpStatus.ACCEPTED);
        case 1:
            return new ResponseEntity<>("Usuario no encontrado.", HttpStatus.NOT_FOUND);
        case 2:
            return new ResponseEntity<>("El nombre no es valido.", HttpStatus.BAD_REQUEST);
        default:
            return new ResponseEntity<>("Error inesperado.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Elimina un usuario por su ID. Solo para administradores.
     *
     * @param id      identificador del usuario
     * @param request petición HTTP para auditoría
     * @return mensaje de resultado
     */
    @DeleteMapping("/eliminar")
    public ResponseEntity<String> eliminar(@RequestParam Long id, HttpServletRequest request) {
        int resultado;
        try {
            resultado = usuarioSer.deleteByID(id);
        } catch (Exception e) {
            return new ResponseEntity<>("Error inesperado.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        switch (resultado) {
        case 0:
            auditoriaService.registrarAccion(id, "ELIMINAR_USUARIO", "Usuario",
                    id, "Usuario ID " + id + " eliminado.");
            return new ResponseEntity<>("Usuario eliminado exitosamente.", HttpStatus.ACCEPTED);
        case 1:
            return new ResponseEntity<>("Usuario no encontrado.", HttpStatus.NOT_FOUND);
        default:
            return new ResponseEntity<>("Error inesperado.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Activa o desactiva un usuario. Solo para administradores.
     *
     * @param id      identificador del usuario
     * @param activo  nuevo estado
     * @param request petición HTTP para auditoría
     * @return mensaje de resultado
     */
    @PutMapping("/estado")
    public ResponseEntity<String> cambiarEstado(@RequestParam Long id,
            @RequestParam boolean activo, HttpServletRequest request) {
        try {
            int resultado = activo ? usuarioSer.activarUsuario(id) : usuarioSer.desactivarUsuario(id);
            switch (resultado) {
            case 0:
                String accion = activo ? "ACTIVAR_USUARIO" : "DESACTIVAR_USUARIO";
                auditoriaService.registrarAccion(id, accion, "Usuario",
                        id, "Usuario ID " + id + (activo ? " activado." : " desactivado."));
                return new ResponseEntity<>("Estado del usuario actualizado.", HttpStatus.ACCEPTED);
            case 1:
                return new ResponseEntity<>("Usuario no encontrado.", HttpStatus.NOT_FOUND);
            default:
                return new ResponseEntity<>("Error inesperado.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error inesperado.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Obtiene usuarios filtrados por rol.
     *
     * @param rol rol a filtrar (ADMIN o USER)
     * @return lista de usuarios con ese rol
     */
    @GetMapping("/porrol")
    public ResponseEntity<List<UsuarioDTO>> obtenerPorRol(@RequestParam String rol) {
        try {
            RolUsuario rolEnum = RolUsuario.valueOf(rol.toUpperCase());
            List<UsuarioDTO> lista = usuarioSer.getByRol(rolEnum);
            if (lista.isEmpty()) {
                return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Obtiene el total de usuarios registrados.
     *
     * @return número total de usuarios
     */
    @GetMapping("/contar")
    public ResponseEntity<Long> contar() {
        return new ResponseEntity<>(usuarioSer.count(), HttpStatus.OK);
    }

    /**
     * Verifica si existe un usuario por su ID.
     *
     * @param id identificador del usuario
     * @return true si existe, false en caso contrario
     */
    @GetMapping("/existe")
    public ResponseEntity<Boolean> existe(@RequestParam Long id) {
        return new ResponseEntity<>(usuarioSer.exist(id), HttpStatus.OK);
    }

    /**
     * Solicita la recuperación de contraseña enviando un token al correo del usuario.
     *
     * @param email   correo electrónico del usuario
     * @param request petición HTTP para auditoría
     * @return mensaje de resultado
     */
    @PostMapping("/recuperar")
    public ResponseEntity<String> solicitarRecuperacion(@RequestParam String email,
            HttpServletRequest request) {
        int resultado = usuarioSer.solicitarRecuperacion(email);
        switch (resultado) {
        case 0:
            auditoriaService.registrarAccion(0L, "SOLICITAR_RECUPERACION", "Usuario", null,
                    "Solicitud de recuperacion para: " + email);
            return new ResponseEntity<>(
                    "Se envio un correo con las instrucciones para recuperar tu contrasena.",
                    HttpStatus.OK);
        case 1:
            return new ResponseEntity<>("El correo no esta registrado.", HttpStatus.NOT_FOUND);
        case 2:
            return new ResponseEntity<>("El formato del correo no es valido.", HttpStatus.BAD_REQUEST);
        case 3:
            return new ResponseEntity<>("Error al enviar el correo. Intente de nuevo.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        default:
            return new ResponseEntity<>("Error inesperado.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Cambia la contraseña del usuario usando el token de recuperación.
     *
     * @param token           token de recuperación
     * @param nuevaContrasena nueva contraseña
     * @param request         petición HTTP para auditoría
     * @return mensaje de resultado
     */
    @PostMapping("/cambiarcontrasena")
    public ResponseEntity<String> cambiarContrasena(@RequestParam String token,
            @RequestParam String nuevaContrasena, HttpServletRequest request) {
        int resultado = usuarioSer.cambiarContrasena(token, nuevaContrasena);
        switch (resultado) {
        case 0:
            auditoriaService.registrarAccion(0L, "CAMBIAR_CONTRASENA", "Usuario", null,
                    "Contrasena cambiada exitosamente.");
            return new ResponseEntity<>("Contrasena cambiada exitosamente. Ya puedes iniciar sesion.",
                    HttpStatus.OK);
        case 1:
            return new ResponseEntity<>("El token es invalido o ya fue utilizado.",
                    HttpStatus.BAD_REQUEST);
        case 2:
            return new ResponseEntity<>("El token no es valido.", HttpStatus.BAD_REQUEST);
        case 3:
            return new ResponseEntity<>(
                    "La contrasena debe tener minimo 8 caracteres, un numero y un caracter especial.",
                    HttpStatus.BAD_REQUEST);
        case 4:
            return new ResponseEntity<>("Error inesperado.", HttpStatus.INTERNAL_SERVER_ERROR);
        default:
            return new ResponseEntity<>("Error inesperado.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}