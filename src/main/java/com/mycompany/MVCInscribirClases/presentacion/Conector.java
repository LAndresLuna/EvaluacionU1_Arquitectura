package com.mycompany.MVCInscribirClases.presentacion;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;

/**
 * Linea que une dos pasos del indicador.
 *
 * @author andres
 */
public final class Conector extends JComponent {

    public Conector() {
        setPreferredSize(new Dimension(34, 28));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(Paleta.LINEA_PASO);
        g2.setStroke(new BasicStroke(1.4f));
        g2.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
        g2.dispose();
    }
}
