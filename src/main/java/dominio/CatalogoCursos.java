package dominio;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Catálogo de cursos disponibles. Simula el repositorio.
 *
 * @author andres
 */
public final class CatalogoCursos {

    private final List<Curso> cursos = new ArrayList<>();

    public CatalogoCursos(List<Curso> cursos) {
        if (cursos != null) {
            this.cursos.addAll(cursos);
        }
    }

    public static CatalogoCursos porDefecto() {
        return new CatalogoCursos(List.of(
                new Curso("ISW-101", "Fundamentos de Ingeniería de Software", 3200.0),
                new Curso("ISW-204", "Arquitectura de Software", 4500.0),
                new Curso("ISW-305", "Ingeniería de Software II", 4800.0),
                new Curso("DAT-110", "Bases de Datos", 3800.0),
                new Curso("RED-150", "Redes y Telecomunicaciones", 4100.0),
                new Curso("MET-130", "Métodos Estadísticos", 2900.0)
        ));
    }

    public Optional<Curso> buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        String buscado = codigo.trim();
        return cursos.stream()
                .filter(curso -> curso.getCodigo().toUpperCase(Locale.ROOT)
                        .equals(buscado.toUpperCase(Locale.ROOT)))
                .findFirst();
    }

    public void retirar(Curso curso) {
        cursos.remove(curso);
    }

    public List<Curso> getCursos() {
        return List.copyOf(cursos);
    }

    public boolean estaVacio() {
        return cursos.isEmpty();
    }
}
