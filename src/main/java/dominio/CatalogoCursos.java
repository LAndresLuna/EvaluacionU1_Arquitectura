package dominio;

import java.util.List;
import java.util.Optional;

/**
 * Catálogo de cursos disponibles para el periodo de inscripción.
 *
 * <p>Simula el repositorio de donde el modelo obtiene los cursos.</p>
 *
 * @author andres
 */
public final class CatalogoCursos {

    private CatalogoCursos() {
    }

    /**
     * Devuelve el catálogo de cursos del periodo.
     *
     * @return lista de cursos disponibles
     */
    public static List<Curso> catalogoPorDefecto() {
        return List.of(
                new Curso("ISW-101", "Fundamentos de Ingeniería de Software", 3200.0),
                new Curso("ISW-204", "Arquitectura de Software", 4500.0),
                new Curso("ISW-305", "Ingeniería de Software II", 4800.0),
                new Curso("DAT-110", "Bases de Datos", 3800.0),
                new Curso("RED-150", "Redes y Telecomunicaciones", 4100.0),
                new Curso("MET-130", "Métodos Estadísticos", 2900.0)
        );
    }

    /**
     * Busca un curso por su código dentro de una lista de cursos.
     *
     * @param codigo  código del curso buscado
     * @param cursos  lista donde se busca el curso
     * @return el curso encontrado o {@link Optional#empty()} si no existe
     */
    public static Optional<Curso> buscarPorCodigo(String codigo, List<Curso> cursos) {
        if (codigo == null || cursos == null) {
            return Optional.empty();
        }
        return cursos.stream()
                .filter(curso -> curso.getCodigo().equalsIgnoreCase(codigo.trim()))
                .findFirst();
    }
}
