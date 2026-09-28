package com.mycompany.MVCInscribirClases.presentacion;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Dimension;
import javax.swing.JComponent;

/**
 * Etapa del indicador de pasos ("1 Datos - 2 Cursos - 3 Confirmación").
 *
 * @author andres
 */
public final class Paso extends JComponent {

    private static final int DIAMETRO = 24;
    private static final int SEPARACION = 8;

    private final String numero;
    private final String titulo;
    private final boolean activo;

    public Paso(String numero, String titulo, boolean activo) {
        this.numero = numero;
        this.titulo = titulo;
        this.activo = activo;
        setPreferredSize(new Dimension(activo ? 96 : 82, 28));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        int y = (getHeight() - DIAMETRO) / 2;
        g2.setColor(activo ? Paleta.PRIMARIO : Paleta.SUPERFICIE);
        g2.fillOval(0, y, DIAMETRO, DIAMETRO);
        g2.setColor(activo ? Paleta.PRIMARIO : Paleta.CONTORNO);
        g2.setStroke(new BasicStroke(1.4f));
        g2.drawOval(0, y, DIAMETRO - 1, DIAMETRO - 1);

        g2.setFont(Paleta.fuente(11, java.awt.Font.BOLD));
        g2.setColor(activo ? java.awt.Color.WHITE : Paleta.TENUE);
        int ancho = g2.getFontMetrics().stringWidth(numero);
        g2.drawString(numero, (DIAMETRO - ancho) / 2, y + DIAMETRO / 2 + 4);

        g2.setFont(Paleta.fuente(13, activo ? java.awt.Font.BOLD : java.awt.Font.PLAIN));
        g2.setColor(activo ? Paleta.TEXTO : Paleta.TENUE);
        g2.drawString(titulo, DIAMETRO + SEPARACION, getHeight() / 2 + 5);
        g2.dispose();
    }
}
