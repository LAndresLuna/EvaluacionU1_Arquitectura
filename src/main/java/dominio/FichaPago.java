package dominio;

import java.time.LocalDate;
import java.util.List;

/**
 * Ficha de pago generada al finalizar la inscripción.
 *
 * <p>Recibe el folio ya emitido por {@link GeneradorFolio} en lugar de calcularlo
 * con un contador estático, para no depender del estado global del proceso.</p>
 *
 * @author andres
 */
public final class FichaPago {

    private final String folio;
    private final LocalDate fecha;
    private final List<Curso> cursos;
    private final double total;

    public FichaPago(String folio, LocalDate fecha, List<Curso> cursos) {
        if (cursos == null) {
            throw new IllegalArgumentException("La ficha de pago necesita al menos un curso");
        }
        this.folio = folio;
        this.fecha = fecha;
        this.cursos = List.copyOf(cursos);
        this.total = this.cursos.stream().mapToDouble(Curso::getCosto).sum();
    }

    public String getFolio() {
        return folio;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public List<Curso> getCursos() {
        return cursos;
    }

    public double getTotal() {
        return total;
    }

    public int getCantidadCursos() {
        return cursos.size();
    }

    @Override
    public String toString() {
        return "FichaPago[" + folio + ", " + getCantidadCursos() + " curso(s), " + total + "]";
    }
}
