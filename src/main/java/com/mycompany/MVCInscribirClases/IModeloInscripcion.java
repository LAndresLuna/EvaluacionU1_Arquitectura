package com.mycompany.MVCInscribirClases;

import dto.CursoDTO;
import dto.FichaPagoDTO;
import dto.ResumenInscripcionDTO;
import java.util.List;

/**
 * Contrato del Modelo del patrón MVC.
 *
 * <p>Solo declara estado: los getters son lo que la Vista lee al ser notificada
 * y los setters lo que el Controlador usa para publicar. Ninguna regla de negocio
 * vive aquí, y el estado se expone en DTOs para que la Vista no toque el
 * dominio.</p>
 *
 * <p>La disponibilidad ({@link #puedeInscribir()} y {@link #puedeFinalizar()}) se
 * deriva del estado publicado y no se le pregunta al Controlador, para que el MVC
 * no quede circular.</p>
 *
 * @author andres
 */
public interface IModeloInscripcion {

    void suscribir(IObserverInscripcion observador);

    void desuscribir(IObserverInscripcion observador);

    /**
     * Ejecuta varias publicaciones como una sola. Sin el lote, un evento que
     * cambia el resumen, el mensaje y la ficha llega a la Vista en tres
     * notificaciones, alguna de ellas con un estado a medio camino.
     *
     * <p>Los lotes se pueden anidar: solo notifica al cerrar el más externo.</p>
     */
    void enLote(Runnable publicaciones);

    List<CursoDTO> getCursosDisponibles();

    ResumenInscripcionDTO getResumenInscripcion();

    /** @return la ficha generada, o {@code null} si aún no se ha finalizado */
    FichaPagoDTO getFichaPago();

    /** @return el mensaje de la última operación rechazada, o cadena vacía */
    String getMensajeError();

    boolean isInscripcionFinalizada();

    /** @return si sigue abierta la inscripción y queda algún curso disponible */
    default boolean puedeInscribir() {
        return !isInscripcionFinalizada() && !getCursosDisponibles().isEmpty();
    }

    /** @return si sigue abierta la inscripción y hay al menos un curso inscrito */
    default boolean puedeFinalizar() {
        return !isInscripcionFinalizada() && !getResumenInscripcion().cursos().isEmpty();
    }

    /** Un {@code null} se guarda como el valor vacío equivalente. */
    void setCursosDisponibles(List<CursoDTO> cursos);

    void setResumenInscripcion(ResumenInscripcionDTO resumen);

    void setFichaPago(FichaPagoDTO ficha);

    void setMensajeError(String mensaje);
}
