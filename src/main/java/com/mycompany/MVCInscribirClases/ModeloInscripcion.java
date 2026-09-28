package com.mycompany.MVCInscribirClases;

import dto.CursoDTO;
import dto.FichaPagoDTO;
import dto.ResumenInscripcionDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Modelo del patron MVC: un contenedor de estado observable.
 *
 * <p>Guarda el estado que la Vista necesita mostrar, en forma de DTOs, y avisa a
 * sus observadores ({@link IObserverInscripcion}) cada vez que un setter cambia
 * un valor. No aplica reglas de negocio ni conoce el dominio: es el lugar donde
 * el Controlador deposita el resultado de su trabajo.</p>
 *
 * @author andres
 */
public class ModeloInscripcion implements IModeloInscripcion {

    private final List<IObserverInscripcion> observadores = new ArrayList<>();

    private List<CursoDTO> cursosDisponibles = List.of();
    private ResumenInscripcionDTO resumen = new ResumenInscripcionDTO(List.of(), 0.0, false);
    private FichaPagoDTO fichaPago;
    private String mensajeError = "";

    @Override
    public void suscribir(IObserverInscripcion observador) {
        observadores.add(observador);
        notificarSuscriptores();
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
        List<CursoDTO> nuevos = List.copyOf(cursos);
        if (nuevos.equals(cursosDisponibles)) {
            return;
        }
        cursosDisponibles = nuevos;
        notificarSuscriptores();
    }

    @Override
    public void setResumenInscripcion(ResumenInscripcionDTO resumen) {
        if (resumen.equals(this.resumen)) {
            return;
        }
        this.resumen = resumen;
        notificarSuscriptores();
    }

    @Override
    public void setFichaPago(FichaPagoDTO ficha) {
        if (Objects.equals(ficha, fichaPago)) {
            return;
        }
        fichaPago = ficha;
        notificarSuscriptores();
    }

    @Override
    public void setMensajeError(String mensaje) {
        String nuevo = mensaje == null ? "" : mensaje;
        if (nuevo.equals(mensajeError)) {
            return;
        }
        mensajeError = nuevo;
        notificarSuscriptores();
    }

    /**
     * Avisa a todos los observadores que el estado del modelo cambió.
     */
    public void notificarSuscriptores() {
        for (IObserverInscripcion observador : observadores) {
            observador.update(this);
        }
    }
}
