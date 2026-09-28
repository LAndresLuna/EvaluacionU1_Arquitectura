package dominio;

import java.util.Objects;

/**
 * Curso del catálogo. Inmutable, con igualdad por valor.
 *
 * @author andres
 */
public final class Curso {

    private final String codigo;
    private final String nombre;
    private final double costo;

    public Curso(String codigo, String nombre, double costo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código del curso es obligatorio");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del curso es obligatorio");
        }
        if (costo <= 0) {
            throw new IllegalArgumentException("El costo del curso " + codigo
                    + " debe ser mayor que cero");
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
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof Curso curso)) {
            return false;
        }
        return Double.compare(costo, curso.costo) == 0
                && codigo.equalsIgnoreCase(curso.codigo)
                && nombre.equals(curso.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo.toUpperCase(java.util.Locale.ROOT), nombre, costo);
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre;
    }
}
