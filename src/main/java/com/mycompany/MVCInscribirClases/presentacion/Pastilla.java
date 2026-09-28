package com.mycompany.MVCInscribirClases.presentacion;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.AbstractBorder;

/**
 * Pastilla redondeada con un contador o un metadato dentro.
 *
 * @author andres
 */
public final class Pastilla extends JPanel {

    private Pastilla(Color relleno, JLabel contenido) {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createCompoundBorder(
                new RellenoRedondeado(10, relleno),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)));
        add(contenido);
    }

    /**
     * @param verde {@code true} para el acento verde, {@code false} para el azul
     */
    public static Pastilla de(String texto, boolean verde) {
        return new Pastilla(relleno(verde),
                Paleta.etiqueta(texto, 11, Font.BOLD,
                        verde ? Paleta.VERDE : Paleta.PRIMARIO));
    }

    /**
     * Envuelve una etiqueta ya construida, para poder reutilizar un {@code JLabel}
     * vivo (los contadores de las tarjetas).
     */
    public static Pastilla de(JLabel contenido, boolean verde) {
        return new Pastilla(relleno(verde), contenido);
    }

    private static Color relleno(boolean verde) {
        return verde ? Paleta.VERDE_SUAVE : Paleta.AZUL_SUAVE;
    }

    /** Borde que solo pinta el relleno redondeado, para las pastillas. */
    private static final class RellenoRedondeado extends AbstractBorder {

        private final int radio;
        private final Color color;

        RellenoRedondeado(int radio, Color color) {
            this.radio = radio;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillRoundRect(x, y, w - 1, h - 1, radio, radio);
            g2.dispose();
        }
    }
}
