package dominio;

/**
 * Excepción de negocio de la inscripción.
 *
 * @author andres
 */
public class ExcepcionInscripcion extends RuntimeException {

    public ExcepcionInscripcion(String mensaje) {
        super(mensaje);
    }
}
