package com.mycompany.MVCInscribirClases;

import dto.CursoDTO;
import dto.FichaPagoDTO;
import dto.ResumenInscripcionDTO;
import java.util.List;

/**
 * Contrato del Modelo del patrón MVC
 *
 * <p>La interfaz solo declara getters y setters. Los getters
 * son lo que la Vista lee cuando el Modelo la notifica, y los setters son lo
 * que el Controlador usa para publicar el estado ya calculado. Ninguna regla de
 * negocio vive aqui: toda notificacion llega por el patron Observer, con
 * {@link IObserverInscripcion#update(IModeloInscripcion)}.</p>
 *
 * <p>El estado se expone en forma de DTOs, de modo que la Vista nunca toca
 * objetos del dominio.</p>
 *
 * @author andres
 */
public interface IModeloInscripcion {

    /**
     * Registra un observador (la Vista) y le notifica de inmediato el estado
     * actual, para que la pantalla se muestre con los datos de arranque.
     *
     * @param observador vista que va a actualizarse
     */
    void suscribir(IObserverInscripcion observador);

    /**
     * @return cursos que todavia se pueden inscribir
     */
    List<CursoDTO> getCursosDisponibles();

    /**
     * @return cursos inscritos, monto a pagar y estado de la inscripción
     */
    ResumenInscripcionDTO getResumenInscripcion();

    /**
     * @return ficha de pago generada, o {@code null} si la inscripción no ha
     *         finalizado
     */
    FichaPagoDTO getFichaPago();

    /**
     * @return mensaje de la última operación rechazada, o cadena vacía
     */
    String getMensajeError();

    /**
     * @return {@code true} si la inscripción ya fue finalizada
     */
    boolean isInscripcionFinalizada();

    /**
     * Publica la nueva lista de cursos disponibles.
     *
     * @param cursos cursos que todavía se pueden inscribir
     */
    void setCursosDisponibles(List<CursoDTO> cursos);

    /**
     * Publica el nuevo estado de la inscripción.
     *
     * @param resumen cursos inscritos, monto a pagar y estado de la inscripción
     */
    void setResumenInscripcion(ResumenInscripcionDTO resumen);

    /**
     * Publica la ficha de pago generada al finalizar.
     *
     * @param ficha ficha con el detalle de la inscripción
     */
    void setFichaPago(FichaPagoDTO ficha);

    /**
     * Publica el resultado de la última operación.
     *
     * @param mensaje texto a mostrar, o cadena vacía si no hubo error
     */
    void setMensajeError(String mensaje);
}
