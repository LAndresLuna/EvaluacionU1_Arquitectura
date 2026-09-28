package com.mycompany.MVCInscribirClases;

import dto.CursoDTO;
import dto.FichaPagoDTO;
import dto.ResumenInscripcionDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Modelo del patrón MVC: un contenedor de estado observable.
 *
 * <p>Guarda en forma de DTOs lo que la Vista debe mostrar y avisa a sus
 * observadores cuando un setter cambia un valor. No aplica reglas de negocio ni
 * conoce el dominio: aquí el Controlador deposita el resultado de su trabajo.</p>
 *
 * <p>Un setter que recibe el mismo valor no notifica, y ningún setter acepta
 * {@code null}: se guarda el equivalente vacío, así los getters nunca devuelven
 * {@code null} y la Vista no necesita defenderse.</p>
 *
 * <p>{@link #suscribir}, {@link #desuscribir} y {@link #enLote} son públicos
 * aquí y no están en {@link IModeloInscripcion}, que solo declara getters y
 * setters: el cableado del Observer no es estado, y así nadie que solo tenga la
 * interfaz puede ponerse a publicar.</p>
 *
 * @author andres
 */
public class ModeloInscripcion implements IModeloInscripcion {

    private final List<IObserverInscripcion> observadores = new ArrayList<>();

    private List<CursoDTO> cursosDisponibles = List.of();
    private ResumenInscripcionDTO resumen = new ResumenInscripcionDTO(List.of(), 0.0, false);
    private FichaPagoDTO fichaPago;
    private String mensajeError = "";

    private int profundidadLote;
    private boolean pendiente;

    /** Registra un observador y le notifica el estado actual de inmediato. */
    public void suscribir(IObserverInscripcion observador) {
        if (observador == null || observadores.contains(observador)) {
            return;
        }
        observadores.add(observador);
        notificarSuscriptores();
    }

    public void desuscribir(IObserverInscripcion observador) {
        observadores.remove(observador);
    }

    /**
     * Ejecuta varias publicaciones como si fueran una sola, para que un evento
     * que cambia el resumen, el mensaje y la ficha llegue a la Vista en una
     * única notificación y no con estados intermedios.
     *
     * <p>Los lotes se pueden anidar: solo notifica al cerrar el más externo.</p>
     */
    public void enLote(Runnable publicaciones) {
        if (publicaciones == null) {
            return;
        }
        profundidadLote++;
        try {
            publicaciones.run();
        } finally {
            profundidadLote--;
            if (profundidadLote == 0) {
                cerrarLote();
            }
        }
    }

    @Override
    public List<CursoDTO> getCursosDisponibles() {
        return cursosDisponibles;
    }

    @Override
    public ResumenInscripcionDTO getResumenInscripcion() {
        return resumen;
    }

    @Override
    public FichaPagoDTO getFichaPago() {
        return fichaPago;
    }

    @Override
    public String getMensajeError() {
        return mensajeError;
    }

    @Override
    public boolean isInscripcionFinalizada() {
        return resumen.finalizada();
    }

    @Override
    public void setCursosDisponibles(List<CursoDTO> cursos) {
        List<CursoDTO> nuevos = cursos == null ? List.of() : List.copyOf(cursos);
        if (nuevos.equals(cursosDisponibles)) {
            return;
        }
        cursosDisponibles = nuevos;
        notificarSiProcede();
    }

    @Override
    public void setResumenInscripcion(ResumenInscripcionDTO resumen) {
        ResumenInscripcionDTO nuevo = resumen == null
                ? new ResumenInscripcionDTO(List.of(), 0.0, false)
                : resumen;
        if (nuevo.equals(this.resumen)) {
            return;
        }
        this.resumen = nuevo;
        notificarSiProcede();
    }

    @Override
    public void setFichaPago(FichaPagoDTO ficha) {
        if (Objects.equals(ficha, fichaPago)) {
            return;
        }
        fichaPago = ficha;
        notificarSiProcede();
    }

    @Override
    public void setMensajeError(String mensaje) {
        String nuevo = mensaje == null ? "" : mensaje;
        if (nuevo.equals(mensajeError)) {
            return;
        }
        mensajeError = nuevo;
        notificarSiProcede();
    }

    /**
     * Recorre una copia de la lista: un observador que se desuscriba desde su
     * propio {@code update()} no debe provocar una
     * {@code ConcurrentModificationException}.
     */
    private void notificarSuscriptores() {
        for (IObserverInscripcion observador : List.copyOf(observadores)) {
            observador.update(this);
        }
    }

    private void notificarSiProcede() {
        if (profundidadLote > 0) {
            pendiente = true;
            return;
        }
        notificarSuscriptores();
    }

    private void cerrarLote() {
        if (!pendiente) {
            return;
        }
        pendiente = false;
        notificarSuscriptores();
    }
}
