package dominio;

/**
 * Curso que el alumno puede inscribir.
 *
 * <p>Entidad del dominio: no depende de la interfáz gráfica (Vista) ni del
 * transporte de datos (DTO). Solo contiene la información y las reglas propias
 * de un curso del catálogo.</p>
 *
 * @author andres
 */
public class Curso {

    private final String codigo;
    private final String nombre;
    private final double costo;

    /**
     * Crea un curso del catálogo.
     *
     * @param codigo código del curso, por ejemplo {@code ISW-101}
     * @param nombre nombre completo del curso
     * @param costo  costo en pesos, debe ser mayor que cero
     * @throws IllegalArgumentException si el código o el nombre están vacíos o
     *                                  si el costo no es positivo
     */
    public Curso(String codigo, String nombre, double costo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código del curso es obligatorio");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del curso es obligatorio");
        }
        if (costo <= 0) {
            throw new IllegalArgumentException("El costo del curso " + codigo + " debe ser mayor que cero");
        }
        this.codigo = codigo.trim();
        this.nombre = nombre.trim();
        this.costo = costo;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public double getCosto() {
        return costo;
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre;
    }
}
