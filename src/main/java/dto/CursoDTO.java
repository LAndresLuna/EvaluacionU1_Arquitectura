package dto;

/**
 * Objeto de transferencia de datos de un curso.
 *
 * <p>Viaja del Modelo hacia la Vista para que la interfaz nunca manipule
 * directamente objetos del dominio.</p>
 *
 * @author andres
 */
public record CursoDTO(String codigo, String nombre, double costo) {
}
