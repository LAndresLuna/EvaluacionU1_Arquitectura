package com.mycompany.MVCInscribirClases;

import javax.swing.SwingUtilities;

/**
 * Punto de entrada de la aplicación.
 *
 * <p>Se encarga del ensamblaje (wiring) del patron MVC: crea el Modelo, el
 * Controlador y la Vista, publica el estado de arranque, registra la Vista como
 * observador del Modelo y muestra la ventana. Ninguna de las tres piezas se
 * conoce entre si mas allá de las interfaces públicas.</p>
 *
 * @author andres
 */
public class Principal {

    /**
     * Arranca la aplicación de escritorio.
     *
     * @param args argumentos de línea de comandos (no se usan)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            IModeloInscripcion modelo = new ModeloInscripcion();
            ControlInscripcion control = new ControlInscripcion(modelo);
            VistaInscripcion vista = new VistaInscripcion(control);
            control.iniciar();
            modelo.suscribir(vista);
            vista.iniciar();
        });
    }
}
