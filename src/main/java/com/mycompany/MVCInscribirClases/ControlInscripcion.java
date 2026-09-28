package com.mycompany.MVCInscribirClases;

import dominio.CatalogoCursos;
import dominio.Curso;
import dominio.ExcepcionInscripcion;
import dominio.FichaPago;
import dominio.Inscripcion;
import dto.CursoDTO;
import dto.FichaPagoDTO;
import dto.ResumenInscripcionDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controlador del patron MVC.
 *
 * <p>Recibe los eventos de la Vista, aplica las reglas del caso de uso sobre las
 * entidades del dominio y publica el resultado en el Modelo con sus setters. El
 * Modelo no decide nada: se limita a notificar el cambio, y la Vista se redibuja
 * sola con lo que encuentra en los getters.</p>
 *
 * @author andres
 */
public class ControlInscripcion {

    private final IModeloInscripcion modelo;
    private final List<Curso> catalogo;
    private final Inscripcion inscripcion;

    /**
     * Crea el controlador con el catálogo de cursos por defecto.
     *
     * @param modelo modelo donde se publica el estado
     */
    public ControlInscripcion(IModeloInscripcion modelo) {
        this(modelo, CatalogoCursos.catalogoPorDefecto());
    }

    /**
     * Crea el controlador con un catalogo de cursos específico.
     *
     * @param modelo   modelo donde se publica el estado
     * @param catalogo cursos disponibles al arrancar
     */
    public ControlInscripcion(IModeloInscripcion modelo, List<Curso> catalogo) {
        this.modelo = modelo;
        this.catalogo = new ArrayList<>(catalogo);
        this.inscripcion = new Inscripcion();
    }

    /**
     * Publica el estado de arranque: el catalogo completo y la inscripción vacía.
     */
    public void iniciar() {
        publicarEstado();
    }

    /**
     * Evento "el alumno seleccionó un curso de la lista de disponibles".
     *
     * @param codigoCurso código del curso seleccionado
     */
    public void inscribir(String codigoCurso) {
        if (codigoCurso == null || codigoCurso.isBlank()) {
            return;
        }
        String codigo = codigoCurso.trim();

        if (inscripcion.isFinalizada()) {
            modelo.setMensajeError("La inscripción ya fue finalizada, no se pueden agregar cursos");
            return;
        }
        Optional<Curso> encontrado = CatalogoCursos.buscarPorCodigo(codigo, catalogo);
        if (encontrado.isEmpty()) {
            modelo.setMensajeError("El curso " + codigo + " no está disponible");
            return;
        }

        Curso curso = encontrado.get();
        try {
            inscripcion.inscribir(curso);
            catalogo.remove(curso);
            modelo.setMensajeError("");
        } catch (ExcepcionInscripcion e) {
            modelo.setMensajeError(e.getMessage());
        }
        publicarEstado();
    }

    /**
     * Evento "el alumno presionó Finalizar inscripcion".
     */
    public void finalizarInscripcion() {
        if (!puedeFinalizar()) {
            return;
        }
        try {
            modelo.setFichaPago(aDto(inscripcion.finalizar()));
            modelo.setMensajeError("");
        } catch (ExcepcionInscripcion e) {
            modelo.setMensajeError(e.getMessage());
        }
        publicarEstado();
    }

    /**
     * @return {@code true} si hay cursos disponibles y la inscripción sigue
     *         abierta
     */
    public boolean puedeInscribir() {
        return !inscripcion.isFinalizada() && !catalogo.isEmpty();
    }

    /**
     * @return {@code true} si hay al menos un curso inscrito y la inscripción
     *         sigue abierta
     */
    public boolean puedeFinalizar() {
        return !inscripcion.isFinalizada() && !inscripcion.getCursos().isEmpty();
    }

    /**
     * Traduce el estado del dominio a DTOs y lo publica en el Modelo. Cada
     * setter notifica a la Vista, que vuelve a leer los getters.
     */
    private void publicarEstado() {
        modelo.setCursosDisponibles(catalogo.stream().map(ControlInscripcion::aDto).toList());
        modelo.setResumenInscripcion(new ResumenInscripcionDTO(
                inscripcion.getCursos().stream().map(ControlInscripcion::aDto).toList(),
                inscripcion.getTotal(),
                inscripcion.isFinalizada()));
    }

    private static CursoDTO aDto(Curso curso) {
        return new CursoDTO(curso.getCodigo(), curso.getNombre(), curso.getCosto());
    }

    private static FichaPagoDTO aDto(FichaPago ficha) {
        return new FichaPagoDTO(
                ficha.getFolio(),
                ficha.getFecha(),
                ficha.getCursos().stream().map(ControlInscripcion::aDto).toList(),
                ficha.getTotal());
    }
}
