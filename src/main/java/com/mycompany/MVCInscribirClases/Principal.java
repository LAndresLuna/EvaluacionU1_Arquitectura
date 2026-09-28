package com.mycompany.MVCInscribirClases;

import javax.swing.SwingUtilities;

/**
 * Punto de entrada: ensambla el MVC y muestra la ventana.
 *
 * <p>Es el único lugar con los tres tipos a la vista, y por eso necesita la clase
 * concreta {@link ModeloInscripcion}: el cableado del Observer ({@code suscribir})
 * no está en la interfaz. La Vista recibe esa misma instancia ya tipada como
 * interfaz, así que no hereda esos permisos.</p>
 *
 * <p>El orden importa: se suscribe la Vista antes de publicar el estado de
 * arranque, así la pantalla inicial no depende de quién notificó primero.</p>
 *
 * @author andres
 */
public class Principal {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ModeloInscripcion modelo = new ModeloInscripcion();
            ControlInscripcion control = new ControlInscripcion(modelo);
            VistaInscripcion vista = new VistaInscripcion(modelo, control);

            modelo.suscribir(vista);
            control.iniciar();
            vista.iniciar();
        });
    }
}
