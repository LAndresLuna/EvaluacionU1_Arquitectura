package com.mycompany.MVCInscribirClases.presentacion;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;
import javax.swing.SwingConstants;

/**
 * Boton con la forma y los colores del storyboard, con estados de hover y
 * deshabilitado dibujados a mano.
 *
 * @author andres
 */
public final class Boton extends JButton {

    private final boolean primario;

    /**
     * @param primario {@code true} para el botón principal (relleno azul),
     *                 {@code false} para el secundario (contorno)
     */
    public Boton(String texto, boolean primario) {
        this.primario = primario;
        setText(texto);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setHorizontalAlignment(SwingConstants.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        boolean activo = isEnabled();
        boolean encima = getModel().isRollover();

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        if (primario) {
            g2.setColor(!activo ? Paleta.PRIMARIO_APAGADO
                    : encima ? Paleta.PRIMARIO_HOVER : Paleta.PRIMARIO);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
        } else {
            g2.setColor(!activo ? Paleta.SUPERFICIE_2
                    : encima ? Paleta.AZUL_SUAVE : Paleta.SUPERFICIE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.setColor(!activo ? Paleta.BORDE : Paleta.PRIMARIO);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
        }
        g2.dispose();

        // El color del texto se fija solo durante el pintado y se restaura
        // despues, para no dejar el boton con el estado de hover pegado.
        Color anterior = getForeground();
        setForeground(primario
                ? (activo ? Color.WHITE : new Color(0xEFF1F7))
                : Paleta.PRIMARIO);
        super.paintComponent(g);
        setForeground(anterior);
    }
}
