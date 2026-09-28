package dto;

import java.util.List;

/**
 * Estado de la inscripción que muestra la zona derecha: cursos inscritos,
 * monto a pagar y si ya fue cerrada.
 *
 * @author andres
 */
public record ResumenInscripcionDTO(List<CursoDTO> cursos, double total, boolean finalizada) {

    public ResumenInscripcionDTO {
        cursos = List.copyOf(cursos);
    }

    public int cantidadCursos() {
        return cursos.size();
    }
}
