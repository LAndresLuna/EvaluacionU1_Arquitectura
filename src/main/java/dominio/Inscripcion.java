package dominio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Inscripción del alumno. Concentra las reglas del caso de uso: no se repite
 * curso, no se inscribe tras finalizar y la ficha exige al menos un curso.
 *
 * @author andres
 */
public final class Inscripcion {

    private final List<Curso> cursos = new ArrayList<>();
    private boolean finalizada;

    /**
     * @throws ExcepcionInscripcion si ya se finalizó o el curso ya está inscrito
     */
    public void inscribir(Curso curso) {
        if (curso == null) {
            throw new ExcepcionInscripcion("No se puede inscribir un curso vacío");
        }
        if (finalizada) {
            throw new ExcepcionInscripcion("La inscripción ya fue finalizada, "
                    + "no se pueden agregar cursos");
        }
        if (yaEstaInscrito(curso)) {
            throw new ExcepcionInscripcion("El curso " + curso.getCodigo() + " ya está inscrito");
        }
        cursos.add(curso);
    }

    /**
     * Cierra la inscripción y emite la ficha de pago.
     *
     * @throws ExcepcionInscripcion si no hay cursos o ya se finalizó
     */
    public FichaPago finalizar(LocalDate fecha, String folio) {
        if (finalizada) {
            throw new ExcepcionInscripcion("La inscripción ya fue finalizada");
        }
        if (cursos.isEmpty()) {
            throw new ExcepcionInscripcion(
                    "Debe inscribirse al menos un curso para generar la ficha de pago");
        }
        finalizada = true;
        return new FichaPago(folio, fecha, cursos);
    }

    public List<Curso> getCursos() {
        return List.copyOf(cursos);
    }

    public int getCantidadCursos() {
        return cursos.size();
    }

    public double getTotal() {
        return cursos.stream().mapToDouble(Curso::getCosto).sum();
    }

    public boolean isFinalizada() {
        return finalizada;
    }

    public boolean yaEstaInscrito(Curso curso) {
        return curso != null && cursos.stream()
                .anyMatch(inscrito -> inscrito.getCodigo().equalsIgnoreCase(curso.getCodigo()));
    }
}
