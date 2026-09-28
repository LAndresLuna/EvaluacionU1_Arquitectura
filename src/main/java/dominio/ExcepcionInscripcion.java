package dominio;

/**
 * Excepción de negocio de la inscripción.
 *
 * <p>Se lanza cuando el alumno intenta una operación que las reglas del caso de
 * uso no permiten, por ejemplo finalizar la inscripción sin ningun curso.</p>
 *
 * @author andres
 */
public class ExcepcionInscripcion extends RuntimeException {

    /**
     * Crea la excepción con el mensaje de negocio.
     *
     * @param mensaje descripción de la regla violada
     */
    public ExcepcionInscripcion(String mensaje) {
        super(mensaje);
    }
}
