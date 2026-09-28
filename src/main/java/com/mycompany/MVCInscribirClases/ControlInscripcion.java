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
 * <p>Recibe los eventos de la Vista, se los pasa a las entidades del dominio y
 * publica el resultado en el Modelo. La flecha va en un solo sentido: el
 * Controlador llama al dominio, y el dominio nunca le devuelve la llamada.</p>
 *
 * <p>No reimplementa reglas: las pregunta al dominio
 * ({@code inscripcion.inscribir(...)} lanza {@link ExcepcionInscripcion} y aquí
 * solo se traduce el mensaje). Y no guarda referencia a la Vista, ni lo que la
 * Vista necesita para habilitar sus botones: eso se deriva del estado publicado.</p>
 *
 * <p>Se programa contra {@link ModeloInscripcion} y no contra
 * {@link IModeloInscripcion} porque, además de escribir el estado, necesita
 * agruparlo con {@code enLote}. La Vista sí va contra la interfaz: solo lee.</p>
 *
 * @author andres
 */
public class ControlInscripcion {

    private final ModeloInscripcion modelo;
    private final CatalogoCursos catalogo;
    private final GeneradorFolio folios;
    private final Inscripcion inscripcion;

    public ControlInscripcion(ModeloInscripcion modelo) {
        this(modelo, CatalogoCursos.porDefecto(), new GeneradorFolio());
    }

    public ControlInscripcion(ModeloInscripcion modelo, List<Curso> catalogo) {
        this(modelo, new CatalogoCursos(catalogo), new GeneradorFolio());
    }

    public ControlInscripcion(ModeloInscripcion modelo, CatalogoCursos catalogo,
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
                // El dominio dice si se puede; aquí solo se traduce el rechazo.
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

    /**
     * El Controlador decide si el evento "finalizar" tiene sentido; la Vista no lo
     * pregunta, lo deriva del Modelo. Es la única disponibilidad que vive aquí.
     */
    private boolean puedeFinalizar() {
        return !inscripcion.isFinalizada() && !inscripcion.getCursos().isEmpty();
    }

    /** Frontera: el dominio se lee aquí y sale convertido en DTOs. */
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
