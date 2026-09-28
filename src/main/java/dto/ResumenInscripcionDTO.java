package dto;

import java.util.List;

/**
 * Estado de la inscripción que muestra la zona derecha de la pantalla:
 * cursos inscritos, monto a pagar y si la inscripcion ya fue cerrada.
 *
 * @author andres
 */
public record ResumenInscripcionDTO(List<CursoDTO> cursos, double total, boolean finalizada) {

    /**
     * Copia defensiva de la lista de cursos para que la Vista no pueda
     * modificar el estado del modelo.
     */
    public ResumenInscripcionDTO {
        cursos = List.copyOf(cursos);
    }

    /**
     * @return cantidad de cursos inscritos
     */
    public int cantidadCursos() {
        return cursos.size();
    }
}
