package co.edu.unbosque.artcook.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.unbosque.artcook.dto.AuditoriaDTO;
import co.edu.unbosque.artcook.entity.Auditoria;
import co.edu.unbosque.artcook.exception.LanzadorExcepciones;
import co.edu.unbosque.artcook.exception.RegistroNoEncontradoException;
import co.edu.unbosque.artcook.repository.AuditoriaRepository;

/**
 * Servicio que gestiona los registros de auditoría del sistema.
 * Solo el administrador puede consultar los registros de auditoría.
 * La auditoría no se puede eliminar ni modificar.
 */
@Service
public class AuditoriaService implements CRUDOperation<AuditoriaDTO> {

    @Autowired
    private AuditoriaRepository auditoriaRepo;

    @Autowired
    private ModelMapper mapper;

    /**
     * Constructor por defecto.
     */
    public AuditoriaService() {
    }

    /**
     * Registra una nueva acción de auditoría en el sistema.
     *
     * @param data datos de la auditoría a registrar
     * @return 0 si se registró correctamente, 1 si ocurrió un error
     */
    @Override
    public int create(AuditoriaDTO data) {
        try {
            LanzadorExcepciones.validarId(data.getUsuarioId());
            Auditoria auditoria = new Auditoria(
                    data.getUsuarioId(),
                    data.getAccion(),
                    data.getEntidad(),
                    data.getIdEntidad()
            );
            auditoria.setDetalles(data.getDetalles());
            auditoria.setFechaAccion(LocalDateTime.now());
            auditoriaRepo.save(auditoria);
            return 0;
        } catch (RegistroNoEncontradoException e) {
            return 1;
        } catch (Exception e) {
            return 1;
        }
    }

    /**
     * Obtiene todos los registros de auditoría ordenados del más reciente al más antiguo.
     *
     * @return lista de DTOs de auditoría
     */
    @Override
    public List<AuditoriaDTO> getAll() {
        List<Auditoria> entityList = (List<Auditoria>) auditoriaRepo.findAll();
        List<AuditoriaDTO> dtoList = new ArrayList<>();
        entityList.forEach(entidad -> dtoList.add(mapper.map(entidad, AuditoriaDTO.class)));
        return dtoList;
    }

    /**
     * La auditoría no se puede eliminar — método no soportado.
     *
     * @param id identificador
     * @return siempre retorna -1 porque la auditoría no se elimina
     */
    @Override
    public int deleteByID(Long id) {
        return -1;
    }

    /**
     * La auditoría no se puede actualizar — método no soportado.
     *
     * @param id   identificador
     * @param data datos
     * @return siempre retorna -1 porque la auditoría no se modifica
     */
    @Override
    public int updateByID(Long id, AuditoriaDTO data) {
        return -1;
    }

    /**
     * Obtiene el total de registros de auditoría.
     *
     * @return número total de registros
     */
    @Override
    public long count() {
        return auditoriaRepo.count();
    }

    /**
     * Verifica si existe un registro de auditoría por su ID.
     *
     * @param id identificador
     * @return true si existe, false en caso contrario
     */
    @Override
    public boolean exist(Long id) {
        return auditoriaRepo.existsById(id);
    }

    /**
     * Registra una acción en la auditoría usando IDs de entidad.
     * Método de conveniencia para llamar desde otros servicios.
     *
     * @param usuarioId ID del usuario que realizó la acción
     * @param accion    acción realizada
     * @param entidad   entidad afectada
     * @param idEntidad ID del registro afectado (puede ser null)
     * @param detalles  información adicional
     */
    public void registrarAccion(Long usuarioId, String accion, String entidad, Long idEntidad, String detalles) {
        Auditoria auditoria = new Auditoria(usuarioId != null ? usuarioId : 0L, accion, entidad, idEntidad);
        auditoria.setDetalles(detalles);
        auditoria.setFechaAccion(LocalDateTime.now());
        auditoriaRepo.save(auditoria);
    }

    /**
     * Obtiene todos los registros de auditoría de un usuario específico.
     *
     * @param usuarioId ID del usuario
     * @return lista de registros del usuario
     * @throws RegistroNoEncontradoException si el ID es inválido
     */
    public List<AuditoriaDTO> obtenerPorUsuario(Long usuarioId) throws RegistroNoEncontradoException {
        LanzadorExcepciones.validarId(usuarioId);
        return auditoriaRepo.findByUsuarioId(usuarioId).stream()
                .map(a -> mapper.map(a, AuditoriaDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los registros de auditoría de una acción específica.
     *
     * @param accion acción a filtrar
     * @return lista de registros con esa acción
     */
    public List<AuditoriaDTO> obtenerPorAccion(String accion) {
        return auditoriaRepo.findByAccion(accion).stream()
                .map(a -> mapper.map(a, AuditoriaDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los registros de auditoría de una entidad específica.
     *
     * @param entidad nombre de la entidad
     * @return lista de registros de esa entidad
     */
    public List<AuditoriaDTO> obtenerPorEntidad(String entidad) {
        return auditoriaRepo.findByEntidad(entidad).stream()
                .map(a -> mapper.map(a, AuditoriaDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene registros de auditoría en un rango de fechas.
     *
     * @param fechaInicio fecha de inicio
     * @param fechaFin    fecha de fin
     * @return lista de registros en ese rango
     */
    public List<AuditoriaDTO> obtenerPorRango(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return auditoriaRepo.findByFechaAccionBetween(fechaInicio, fechaFin).stream()
                .map(a -> mapper.map(a, AuditoriaDTO.class))
                .collect(Collectors.toList());
    }
}