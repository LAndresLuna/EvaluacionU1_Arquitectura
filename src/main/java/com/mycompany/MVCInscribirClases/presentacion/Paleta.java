package com.mycompany.MVCInscribirClases.presentacion;

import java.awt.Color;
import java.awt.Font;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * Colores, tipografía y formato de moneda. Ajustar el diseño es editar esta
 * clase, no perseguir constantes por media aplicación.
 *
 * @author andres
 */
public final class Paleta {

    /* ------------------------------------------------------------- colores */
    public static final Color FONDO = new Color(0xF3F4FA);
    public static final Color SUPERFICIE = new Color(0xFFFFFF);
    public static final Color SUPERFICIE_2 = new Color(0xF9FAFD);
    public static final Color AZUL_SUAVE = new Color(0xE7EBFA);
    public static final Color PRIMARIO = new Color(0x2847A8);
    public static final Color PRIMARIO_HOVER = new Color(0x1F3A8F);
    public static final Color PRIMARIO_APAGADO = new Color(0x9BA5C9);
    public static final Color TEXTO = new Color(0x172033);
    public static final Color TEXTO_2 = new Color(0x9CA4B0);
    public static final Color TEXTO_3 = new Color(0x7B8799);
    public static final Color VERDE = new Color(0x30846C);
    public static final Color VERDE_SUAVE = new Color(0xE6F4EF);
    public static final Color BORDE = new Color(0xE6EAF2);
    public static final Color SCRIM = new Color(0x2A303D);
    public static final Color ERROR = new Color(0xB3261E);
    public static final Color CONTORNO = new Color(0xB9C0CF);
    public static final Color TENUE = new Color(0x8A93A6);
    public static final Color LINEA_PASO = new Color(0xD3D8E3);

    public static final int RADIO = 14;

    private Paleta() {
    }

    public static Font fuente(int tamano, int estilo) {
        return new Font(Font.SANS_SERIF, estilo, tamano);
    }

    public static String formatearMonto(double monto) {
        NumberFormat formato = NumberFormat.getCurrencyInstance(Locale.US);
        formato.setMinimumFractionDigits(2);
        formato.setMaximumFractionDigits(2);
        return formato.format(monto);
    }

    public static JLabel etiqueta(String texto, int tamano, int estilo, Color color) {
        return etiqueta(texto, tamano, estilo, color, SwingConstants.LEFT);
    }

    public static JLabel etiqueta(String texto, int tamano, int estilo, Color color,
            int alineacion) {
        JLabel etiqueta = new JLabel(texto, alineacion);
        etiqueta.setFont(fuente(tamano, estilo));
        etiqueta.setForeground(color);
        return etiqueta;
    }

    public static TextoEspaciado espaciada(String texto, int tamano, Color color) {
        return espaciada(texto, tamano, color, 0.09f);
    }

    public static TextoEspaciado espaciada(String texto, int tamano, Color color,
            float tracking) {
        return new TextoEspaciado(texto, tamano, Font.BOLD, color, tracking);
    }
}
