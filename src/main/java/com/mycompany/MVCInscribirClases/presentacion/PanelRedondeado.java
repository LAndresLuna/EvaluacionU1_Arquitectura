package com.mycompany.MVCInscribirClases.presentacion;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/**
 * Panel con esquinas redondeadas, fondo y borde opcionales.
 *
 * @author andres
 */
public class PanelRedondeado extends JPanel {

    private final int radio;
    private final Color relleno;
    private final Color borde;

    /**
     * @param relleno color de fondo, o {@code null} para no pintar
     * @param borde   color del contorno, o {@code null} para no dibujar
     */
    public PanelRedondeado(int radio, Color relleno, Color borde) {
        this.radio = radio;
        this.relleno = relleno;
        this.borde = borde;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (relleno != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(relleno);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radio, radio);
            g2.dispose();
        }
        if (borde != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(borde);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radio, radio);
            g2.dispose();
        }
        super.paintComponent(g);
    }
}
