package com.mycompany.MVCInscribirClases.presentacion;

import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 * Linea divisoria horizontal de 1 px.
 *
 * @author andres
 */
public final class Separador extends JPanel {

    public Separador() {
        setOpaque(false);
        setPreferredSize(new Dimension(1, 1));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(Paleta.BORDE);
        g.fillRect(0, 0, getWidth(), 1);
    }
}
