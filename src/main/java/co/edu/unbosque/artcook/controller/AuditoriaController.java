package co.edu.unbosque.artcook.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.artcook.dto.AuditoriaDTO;
import co.edu.unbosque.artcook.exception.RegistroNoEncontradoException;
import co.edu.unbosque.artcook.services.AuditoriaService;

/**
 * Controlador que gestiona la consulta de registros de auditoría del sistema.
 * Solo accesible por administradores.
 */
@RestController
@RequestMapping("/auditoria")
public class AuditoriaController {

    @Autowired
    private AuditoriaService auditoriaService;

    /**
     * Obtiene todos los registros de auditoría.
     * Retorna siempre HTTP 200 con la lista completa o vacía para evitar
     * que el frontend reciba null al usar HTTP 204 que no envía body.
     *
     * @return lista completa de registros de auditoría
     */
    @GetMapping("/mostrartodo")
    public ResponseEntity<List<AuditoriaDTO>> obtenerTodo() {
        List<AuditoriaDTO> lista = auditoriaService.getAll();
        return new ResponseEntity<>(lista, HttpStatus.OK);
    }

    /**
     * Obtiene los registros de auditoría de un usuario específico.
     * Retorna siempre HTTP 200 con la lista completa o vacía para evitar
     * que el frontend reciba null al usar HTTP 204 que no envía body.
     *
     * @param usuarioId ID del usuario a consultar
     * @return lista de registros del usuario
     */
    @GetMapping("/porusuario")
    public ResponseEntity<List<AuditoriaDTO>> obtenerPorUsuario(@RequestParam Long usuarioId) {
        try {
            List<AuditoriaDTO> lista = auditoriaService.obtenerPorUsuario(usuarioId);
            return new ResponseEntity<>(lista, HttpStatus.OK);
        } catch (RegistroNoEncontradoException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Obtiene los registros de auditoría filtrados por tipo de acción.
     * Retorna siempre HTTP 200 con la lista completa o vacía para evitar
     * que el frontend reciba null al usar HTTP 204 que no envía body.
     *
     * @param accion acción a filtrar (LOGIN, REGISTRO, GENERAR_RECETA, etc.)
     * @return lista de registros con esa acción
     */
    @GetMapping("/poraccion")
    public ResponseEntity<List<AuditoriaDTO>> obtenerPorAccion(@RequestParam String accion) {
        List<AuditoriaDTO> lista = auditoriaService.obtenerPorAccion(accion);
        return new ResponseEntity<>(lista, HttpStatus.OK);
    }

    /**
     * Obtiene los registros de auditoría filtrados por entidad.
     * Retorna siempre HTTP 200 con la lista completa o vacía para evitar
     * que el frontend reciba null al usar HTTP 204 que no envía body.
     *
     * @param entidad nombre de la entidad (Usuario, Receta, IA, etc.)
     * @return lista de registros de esa entidad
     */
    @GetMapping("/porentidad")
    public ResponseEntity<List<AuditoriaDTO>> obtenerPorEntidad(@RequestParam String entidad) {
        List<AuditoriaDTO> lista = auditoriaService.obtenerPorEntidad(entidad);
        return new ResponseEntity<>(lista, HttpStatus.OK);
    }

    /**
     * Obtiene los registros de auditoría en un rango de fechas.
     * Retorna siempre HTTP 200 con la lista completa o vacía para evitar
     * que el frontend reciba null al usar HTTP 204 que no envía body.
     *
     * @param fechaInicio fecha de inicio en formato yyyy-MM-ddTHH:mm:ss
     * @param fechaFin    fecha de fin en formato yyyy-MM-ddTHH:mm:ss
     * @return lista de registros en ese rango
     */
    @GetMapping("/porRango")
    public ResponseEntity<?> obtenerPorRango(@RequestParam String fechaInicio,
            @RequestParam String fechaFin) {
        try {
            LocalDateTime inicio = LocalDateTime.parse(fechaInicio);
            LocalDateTime fin = LocalDateTime.parse(fechaFin);
            List<AuditoriaDTO> lista = auditoriaService.obtenerPorRango(inicio, fin);
            return new ResponseEntity<>(lista, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                "Formato de fecha invalido. Use el formato: yyyy-MM-ddTHH:mm:ss",
                HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Obtiene el total de registros de auditoría.
     *
     * @return número total de registros
     */
    @GetMapping("/contar")
    public ResponseEntity<Long> contar() {
        return new ResponseEntity<>(auditoriaService.count(), HttpStatus.OK);
    }

    /**
     * Verifica si existe un registro de auditoría por su ID.
     *
     * @param id identificador del registro
     * @return true si existe, false en caso contrario
     */
    @GetMapping("/existe")
    public ResponseEntity<Boolean> existe(@RequestParam Long id) {
        return new ResponseEntity<>(auditoriaService.exist(id), HttpStatus.OK);
    }
}