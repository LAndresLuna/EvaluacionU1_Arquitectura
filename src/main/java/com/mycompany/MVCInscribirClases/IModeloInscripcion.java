package com.mycompany.MVCInscribirClases;

import dto.CursoDTO;
import dto.FichaPagoDTO;
import dto.ResumenInscripcionDTO;
import java.util.List;

/**
 * Contrato del Modelo del patrón MVC: solo getters y setters.
 *
 * <p>Declara <em>qué</em> se puede ver y cómo se escribe el estado, no
 * <em>cómo</em> se avisa. Suscribirse y agrupar publicaciones no son estado:
 * son cableado, y por eso viven en {@link ModeloInscripcion}. Así, una Vista que
 * se programe contra esta interfaz no puede ni cambiar un valor ni suscribirse
 * por su cuenta.</p>
 *
 * <p>El estado se expone en DTOs para que la Vista no toque el dominio, y la
 * disponibilidad ({@link #puedeInscribir()} y {@link #puedeFinalizar()}) se
 * deriva del estado publicado en lugar de preguntarse al Controlador, para que
 * el MVC no quede circular.</p>
 *
 * @author andres
 */
public interface IModeloInscripcion {

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
