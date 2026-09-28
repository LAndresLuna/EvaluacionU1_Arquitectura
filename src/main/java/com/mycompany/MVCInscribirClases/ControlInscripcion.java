package com.mycompany.MVCInscribirClases;

import dominio.CatalogoCursos;
import dominio.Curso;
import dominio.ExcepcionInscripcion;
import dominio.FichaPago;
import dominio.GeneradorFolio;
import dominio.Inscripcion;
import dto.CursoDTO;
import dto.FichaPagoDTO;
import dto.ResumenInscripcionDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Controlador del patrón MVC.
 *
 * <p>Recibe los eventos de la Vista, aplica las reglas del caso de uso sobre las
 * entidades del dominio y publica el resultado en el Modelo. Cada evento publica
 * su estado completo dentro de un {@link IModeloInscripcion#enLote(Runnable)
 * lote}, de modo que la Vista recibe una sola notificación coherente y nunca una
 * a medio camino.</p>
 *
 * <p>No guarda referencia a la Vista, y lo que la Vista necesita para habilitar
 * sus botones no se le pregunta aquí: se deriva del estado publicado.</p>
 *
 * @author andres
 */
public class ControlInscripcion {

    private final IModeloInscripcion modelo;
    private final CatalogoCursos catalogo;
    private final GeneradorFolio folios;
    private final Inscripcion inscripcion;

    public ControlInscripcion(IModeloInscripcion modelo) {
        this(modelo, CatalogoCursos.porDefecto(), new GeneradorFolio());
    }

    public ControlInscripcion(IModeloInscripcion modelo, List<Curso> catalogo) {
        this(modelo, new CatalogoCursos(catalogo), new GeneradorFolio());
    }

    public ControlInscripcion(IModeloInscripcion modelo, CatalogoCursos catalogo,
            GeneradorFolio folios) {
        this.modelo = Objects.requireNonNull(modelo, "modelo");
        this.catalogo = Objects.requireNonNull(catalogo, "catalogo");
        this.folios = Objects.requireNonNull(folios, "folios");
        this.inscripcion = new Inscripcion();
    }

    /** Publica el estado de arranque: el catálogo completo y la inscripción vacía. */
    public void iniciar() {
        publicarEstado();
    }

    /** Evento "el alumno seleccionó un curso de la lista de disponibles". */
    public void inscribir(String codigoCurso) {
        if (codigoCurso == null || codigoCurso.isBlank()) {
            return;
        }
        modelo.enLote(() -> {
            if (inscripcion.isFinalizada()) {
                modelo.setMensajeError(
                        "La inscripción ya fue finalizada, no se pueden agregar cursos");
                return;
            }
            Optional<Curso> encontrado = catalogo.buscarPorCodigo(codigoCurso);
            if (encontrado.isEmpty()) {
                modelo.setMensajeError("El curso " + codigoCurso.trim() + " no está disponible");
                return;
            }
            Curso curso = encontrado.get();
            try {
                inscripcion.inscribir(curso);
                catalogo.retirar(curso);
                modelo.setMensajeError("");
            } catch (ExcepcionInscripcion e) {
                modelo.setMensajeError(e.getMessage());
            }
            publicarEstado();
        });
    }

    /** Evento "el alumno presionó Finalizar inscripción". */
    public void finalizarInscripcion() {
        modelo.enLote(() -> {
            if (!puedeFinalizar()) {
                return;
            }
            try {
                LocalDate hoy = LocalDate.now();
                FichaPago ficha = inscripcion.finalizar(hoy, folios.siguiente(hoy));
                publicarEstado();
                modelo.setFichaPago(aDto(ficha));
                modelo.setMensajeError("");
            } catch (ExcepcionInscripcion e) {
                modelo.setMensajeError(e.getMessage());
                publicarEstado();
            }
        });
    }

    private boolean puedeFinalizar() {
        return !inscripcion.isFinalizada() && !inscripcion.getCursos().isEmpty();
    }

    private void publicarEstado() {
        modelo.setCursosDisponibles(catalogo.getCursos().stream()
                .map(ControlInscripcion::aDto).toList());
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
