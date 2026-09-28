package dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Objeto de transferencia de datos de la ficha de pago que se muestra en
 * pantalla al finalizar la inscripción.
 *
 * @author andres
 */
public record FichaPagoDTO(String folio, LocalDate fecha, List<CursoDTO> cursos, double total) {

    /**
     * Copia defensiva de la lista de cursos.
     */
    public FichaPagoDTO {
        cursos = List.copyOf(cursos);
    }

    /**
     * @return cantidad de cursos inscritos que aparecen en la ficha
     */
    public int cantidadCursos() {
        return cursos.size();
    }
}
