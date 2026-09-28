package com.mycompany.MVCInscribirClases;

import javax.swing.SwingUtilities;

/**
 * Punto de entrada: ensambla el MVC y muestra la ventana.
 *
 * <p>Se suscribe la Vista antes de publicar el estado de
 * arranque, así la pantalla inicial no depende de quién notificó primero.</p>
 *
 * @author andres
 */
public class Principal {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            IModeloInscripcion modelo = new ModeloInscripcion();
            ControlInscripcion control = new ControlInscripcion(modelo);
            VistaInscripcion vista = new VistaInscripcion(modelo, control);

            modelo.suscribir(vista);
            control.iniciar();
            vista.iniciar();
        });
    }
}
