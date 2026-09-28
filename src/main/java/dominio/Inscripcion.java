package dominio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Inscripción del alumno: la lista de cursos inscritos y el monto a pagar.
 *
 * <p>Entidad del dominio que concentra las reglas del caso de uso: no se puede
 * inscribir dos veces el mismo curso, no se puede inscribir despues de
 * finalizar y no se puede generar la ficha de pago sin cursos.</p>
 *
 * @author andres
 */
public class Inscripcion {

    private final List<Curso> cursos = new ArrayList<>();
    private boolean finalizada;

    /**
     * Inscribe un curso en la inscripción.
     *
     * @param curso curso seleccionado de la lista de disponibles
     * @throws ExcepcionInscripcion si la inscripción ya fué finalizada o si el
     *                              curso ya está inscrito
     */
    public void inscribir(Curso curso) {
        if (curso == null) {
            throw new ExcepcionInscripcion("No se puede inscribir un curso vacío");
        }
        if (finalizada) {
            throw new ExcepcionInscripcion("La inscripcion ya fué finalizada, no se pueden agregar cursos");
        }
        if (yaEstaInscrito(curso)) {
            throw new ExcepcionInscripcion("El curso " + curso.getCodigo() + " ya está inscrito");
        }
        cursos.add(curso);
    }

    /**
     * Cierra la inscripción y genera la ficha de pago.
     *
     * @return ficha de pago con los cursos inscritos y el total
     * @throws ExcepcionInscripcion si no hay cursos inscritos o si la
     *                              inscripción ya fue finalizada
     */
    public FichaPago finalizar() {
        if (finalizada) {
            throw new ExcepcionInscripcion("La inscripción ya fué finalizada");
        }
        if (cursos.isEmpty()) {
            throw new ExcepcionInscripcion("Debe inscribirse al menos un curso para generar la ficha de pago");
        }
        finalizada = true;
        return new FichaPago(LocalDate.now(), cursos);
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

    /**
     * Indica si el curso ya forma parte de la inscripción.
     *
     * @param curso curso a verificar
     * @return {@code true} si ya está inscrito
     */
    public boolean yaEstaInscrito(Curso curso) {
        return cursos.stream()
                .anyMatch(inscrito -> inscrito.getCodigo().equalsIgnoreCase(curso.getCodigo()));
    }
}
