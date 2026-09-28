package com.mycompany.MVCInscribirClases;

/**
 * Contrato del observador de la Vista (patrón Observer).
 *
 * <p>El Modelo notifica a la Vista que su estado cambio y la Vista vuelve a
 * leer los datos para actualizarse.</p>
 *
 * @author andres
 */
public interface IObserverInscripcion {

    /**
     * Callback que recibe el Modelo ya actualizado.
     *
     * @param modelo modelo que cambio de estado
     */
    void update(IModeloInscripcion modelo);
}
