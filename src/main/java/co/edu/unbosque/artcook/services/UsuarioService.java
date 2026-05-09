package co.edu.unbosque.artcook.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import co.edu.unbosque.artcook.dto.LoginResponseDTO;
import co.edu.unbosque.artcook.dto.RolUsuarioDTO;
import co.edu.unbosque.artcook.dto.UsuarioDTO;
import co.edu.unbosque.artcook.entity.RolUsuario;
import co.edu.unbosque.artcook.entity.Usuario;
import co.edu.unbosque.artcook.exception.ContrasenaException;
import co.edu.unbosque.artcook.exception.CredencialesInvalidasException;
import co.edu.unbosque.artcook.exception.EmailFormatoException;
import co.edu.unbosque.artcook.exception.EmailNoVerificadoException;
import co.edu.unbosque.artcook.exception.LanzadorExcepciones;
import co.edu.unbosque.artcook.exception.NombreException;
import co.edu.unbosque.artcook.exception.RegistroNoEncontradoException;
import co.edu.unbosque.artcook.exception.TokenInvalidoException;
import co.edu.unbosque.artcook.exception.UsuarioInactivoException;
import co.edu.unbosque.artcook.repository.UsuarioRepository;

@Service
public class UsuarioService implements CRUDOperation<UsuarioDTO> {

	@Autowired
	private UsuarioRepository usuarioRepo;

	@Autowired
	private ModelMapper mapper;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private CorreoService correoService;

	@Override
	public int create(UsuarioDTO newData) {

		try {
			LanzadorExcepciones.validarNombre(newData.getNombre());
		} catch (NombreException e) {
			return 1;
		}

		try {
			LanzadorExcepciones.validarEmail(newData.getEmail());
		} catch (EmailFormatoException e) {
			return 2;
		}

		if (usuarioRepo.existsByEmail(newData.getEmail())) {
			return 3;
		}

		try {
			LanzadorExcepciones.validarContrasena(newData.getContrasena());
		} catch (ContrasenaException e) {
			return 4;
		}

		Usuario usuario = mapper.map(newData, Usuario.class);
		usuario.setContrasena(passwordEncoder.encode(newData.getContrasena()));
		usuario.setRol(RolUsuario.USER);
		usuario.setTokenVerificacion(UUID.randomUUID().toString());
		usuario.setEmailVerificado(false);
		usuario.setActivo(true);
		usuario.setFechaCreacion(LocalDateTime.now());

		usuarioRepo.save(usuario);
		newData.setId(usuario.getId());

		try {
			correoService.enviarTokenVerificacion(usuario.getEmail(), usuario.getTokenVerificacion());
		} catch (Exception e) {
			return 5;
		}

		return 0;
	}

	@Override
	public List<UsuarioDTO> getAll() {
		List<Usuario> entityList = (List<Usuario>) usuarioRepo.findAll();
		List<UsuarioDTO> dtoList = new ArrayList<>();
		entityList.forEach((entidad) -> {
			UsuarioDTO dto = mapper.map(entidad, UsuarioDTO.class);
			dtoList.add(dto);
		});
		return dtoList;
	}

	@Override
	public int deleteById(Long id) {
		try {
			LanzadorExcepciones.validarId(id);
			if (usuarioRepo.existsById(id)) {
				usuarioRepo.deleteById(id);
				return 0;
			}
			throw new RegistroNoEncontradoException("El usuario con id " + id + " no existe");
		} catch (RegistroNoEncontradoException e) {
			return 1;
		}
	}

	@Override
	public int updateById(Long id, UsuarioDTO newData) {
		try {
			LanzadorExcepciones.validarId(id);
		} catch (RegistroNoEncontradoException e) {
			return 1;
		}

		Optional<Usuario> encontrado = usuarioRepo.findById(id);
		if (!encontrado.isPresent()) {
			return 1;
		}

		Usuario temp = encontrado.get();

		if (newData.getNombre() != null) {
			try {
				LanzadorExcepciones.validarNombre(newData.getNombre());
			} catch (NombreException e) {
				return 2;
			}
			temp.setNombre(newData.getNombre());
		}

		if (newData.getEmail() != null) {
			try {
				LanzadorExcepciones.validarEmail(newData.getEmail());
			} catch (EmailFormatoException e) {
				return 3;
			}
			temp.setEmail(newData.getEmail());
		}

		temp.setFechaActualizacion(LocalDateTime.now());
		usuarioRepo.save(temp);
		return 0;
	}

	@Override
	public long count() {
		return usuarioRepo.count();
	}

	@Override
	public boolean exist(Long id) {
		return usuarioRepo.existsById(id);
	}

	public LoginResponseDTO login(String email, String contrasena)
			throws EmailFormatoException, CredencialesInvalidasException,
			EmailNoVerificadoException, UsuarioInactivoException {

		LanzadorExcepciones.validarEmail(email);

		Optional<Usuario> usuario = usuarioRepo.findByEmail(email);
		if (!usuario.isPresent()) {
			throw new CredencialesInvalidasException("El email no está registrado");
		}

		Usuario usuarioEncontrado = usuario.get();
		if (!passwordEncoder.matches(contrasena, usuarioEncontrado.getContrasena())) {
			throw new CredencialesInvalidasException("La contraseña es incorrecta");
		}

		LanzadorExcepciones.validarEmailVerificado(usuarioEncontrado.isEmailVerificado());
		LanzadorExcepciones.validarUsuarioActivo(usuarioEncontrado.isActivo());

		LoginResponseDTO response = new LoginResponseDTO();
		response.setId(usuarioEncontrado.getId());
		response.setNombre(usuarioEncontrado.getNombre());
		response.setEmail(usuarioEncontrado.getEmail());
		response.setRol(RolUsuarioDTO.valueOf(usuarioEncontrado.getRol().name()));
		response.setEmailVerificado(usuarioEncontrado.isEmailVerificado());

		return response;
	}

	public int verificarEmail(String token) {
		try {
			LanzadorExcepciones.validarToken(token);
			Optional<Usuario> usuario = usuarioRepo.findByTokenVerificacion(token);
			LanzadorExcepciones.validarRegistroExiste(usuario.isPresent());
			Usuario usuarioEncontrado = usuario.get();
			usuarioEncontrado.setEmailVerificado(true);
			usuarioEncontrado.setTokenVerificacion(null);
			usuarioEncontrado.setFechaActualizacion(LocalDateTime.now());
			usuarioRepo.save(usuarioEncontrado);
			try {
				correoService.enviarCorreoBienvenida(usuarioEncontrado.getEmail());
			} catch (Exception e) {
			}
			return 0;
		} catch (TokenInvalidoException | RegistroNoEncontradoException e) {
			return 1;
		}
	}

	public int desactivarUsuario(Long id) {
		try {
			LanzadorExcepciones.validarId(id);
			Optional<Usuario> usuario = usuarioRepo.findById(id);
			LanzadorExcepciones.validarRegistroExiste(usuario.isPresent());
			Usuario usuarioEncontrado = usuario.get();
			usuarioEncontrado.setActivo(false);
			usuarioEncontrado.setFechaActualizacion(LocalDateTime.now());
			usuarioRepo.save(usuarioEncontrado);
			return 0;
		} catch (RegistroNoEncontradoException e) {
			return 1;
		}
	}

	public int activarUsuario(Long id) {
		try {
			LanzadorExcepciones.validarId(id);
			Optional<Usuario> usuario = usuarioRepo.findById(id);
			LanzadorExcepciones.validarRegistroExiste(usuario.isPresent());
			Usuario usuarioEncontrado = usuario.get();
			usuarioEncontrado.setActivo(true);
			usuarioEncontrado.setFechaActualizacion(LocalDateTime.now());
			usuarioRepo.save(usuarioEncontrado);
			return 0;
		} catch (RegistroNoEncontradoException e) {
			return 1;
		}
	}

	public UsuarioDTO obtenerPorId(Long id) throws RegistroNoEncontradoException {
		try {
			LanzadorExcepciones.validarId(id);
			Optional<Usuario> usuario = usuarioRepo.findById(id);
			LanzadorExcepciones.validarRegistroExiste(usuario.isPresent());
			return mapper.map(usuario.get(), UsuarioDTO.class);
		} catch (RegistroNoEncontradoException e) {
			throw e;
		}
	}

	public UsuarioDTO obtenerPorEmail(String email)
			throws EmailFormatoException, RegistroNoEncontradoException {
		LanzadorExcepciones.validarEmail(email);
		Optional<Usuario> usuario = usuarioRepo.findByEmail(email);
		LanzadorExcepciones.validarRegistroExiste(usuario.isPresent());
		return mapper.map(usuario.get(), UsuarioDTO.class);
	}

	public int solicitarRecuperacion(String email) {
		try {
			LanzadorExcepciones.validarEmail(email);
			Optional<Usuario> usuario = usuarioRepo.findByEmail(email);
			if (!usuario.isPresent()) {
				return 1;
			}
			Usuario usuarioEncontrado = usuario.get();
			usuarioEncontrado.setTokenRecuperacion(UUID.randomUUID().toString());
			usuarioEncontrado.setFechaActualizacion(LocalDateTime.now());
			usuarioRepo.save(usuarioEncontrado);
			correoService.enviarCorreoRecuperacion(usuarioEncontrado.getEmail(), usuarioEncontrado.getTokenRecuperacion());
			return 0;
		} catch (EmailFormatoException e) {
			return 2;
		} catch (Exception e) {
			return 3;
		}
	}

	public int cambiarContrasena(String token, String nuevaContrasena) {
		try {
			LanzadorExcepciones.validarToken(token);
			LanzadorExcepciones.validarContrasena(nuevaContrasena);
			Optional<Usuario> usuario = usuarioRepo.findByTokenRecuperacion(token);
			if (!usuario.isPresent()) {
				return 1;
			}
			Usuario usuarioEncontrado = usuario.get();
			usuarioEncontrado.setContrasena(passwordEncoder.encode(nuevaContrasena));
			usuarioEncontrado.setTokenRecuperacion(null);
			usuarioEncontrado.setFechaActualizacion(LocalDateTime.now());
			usuarioRepo.save(usuarioEncontrado);
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