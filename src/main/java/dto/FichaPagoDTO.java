package dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Ficha de pago que se muestra al finalizar la inscripción.
 *
 * @author andres
 */
public record FichaPagoDTO(String folio, LocalDate fecha, List<CursoDTO> cursos, double total) {

    public FichaPagoDTO {
        cursos = List.copyOf(cursos);
    }

    public int cantidadCursos() {
        return cursos.size();
    }
}
