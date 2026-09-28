package dominio;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Ficha de pago generada al finalizar la inscripción.
 *
 * <p>Documento inmutable: guarda el folio, la fecha, los cursos inscritos con su
 * costo y el total a pagar.</p>
 *
 * @author andres
 */
public class FichaPago {

    private final String folio;
    private final LocalDate fecha;
    private final List<Curso> cursos;
    private final double total;

    /**
     * Crea la ficha de pago de una inscripción.
     *
     * @param fecha  fecha de emision de la ficha
     * @param cursos cursos inscritos
     */
    public FichaPago(LocalDate fecha, List<Curso> cursos) {
        this.folio = generarFolio(fecha);
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

    /**
     * Genera el folio consecutivo de la ficha, por ejemplo {@code FIC-2026-0001}.
     *
     * @param fecha fecha de emisión
     * @return folio de la ficha
     */
    private static String generarFolio(LocalDate fecha) {
        return String.format("FIC-%d-%04d", fecha.getYear(), SECUENCIA.incrementAndGet());
    }

    /** Secuencia de folios emitidos en la ejecución actual. */
    private static final AtomicInteger SECUENCIA = new AtomicInteger();
}
