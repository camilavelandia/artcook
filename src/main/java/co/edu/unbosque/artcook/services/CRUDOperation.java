package co.edu.unbosque.artcook.services;

import java.util.List;

/**
 * Interfaz genérica que define las operaciones CRUD básicas del sistema.
 *
 * @param <T> tipo de DTO que maneja la operación
 */
public interface CRUDOperation<T> {

    /**
     * Crea un nuevo registro.
     *
     * @param data datos del objeto a crear
     * @return código de estado: 0 éxito, otro número indica el tipo de error
     * @throws Exception si ocurre un error durante la creación
     */
    int create(T data) throws Exception;

    /**
     * Obtiene todos los registros.
     *
     * @return lista de todos los objetos
     */
    List<T> getAll();

    /**
     * Elimina un registro por su ID.
     *
     * @param id identificador del registro a eliminar
     * @return código de estado: 0 éxito, 1 si no se encontró
     * @throws Exception si ocurre un error durante la eliminación
     */
    int deleteByID(Long id) throws Exception;

    /**
     * Actualiza un registro por su ID.
     *
     * @param id   identificador del registro a actualizar
     * @param data nuevos datos del objeto
     * @return código de estado: 0 éxito, otro número indica el tipo de error
     * @throws Exception si ocurre un error durante la actualización
     */
    int updateByID(Long id, T data) throws Exception;

    /**
     * Obtiene el total de registros.
     *
     * @return número total de registros
     */
    long count();

    /**
     * Verifica si existe un registro por su ID.
     *
     * @param id identificador a verificar
     * @return true si existe, false en caso contrario
     */
    boolean exist(Long id);
}
