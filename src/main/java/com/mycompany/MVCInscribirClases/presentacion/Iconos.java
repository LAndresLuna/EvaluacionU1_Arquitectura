package com.mycompany.MVCInscribirClases.presentacion;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;

/**
 * Iconos decorativos del storyboard, dibujados a mano: se agrupan aquí porque
 * comparten lo mismo, ser piezas pintadas sin recursos externos.
 *
 * @author andres
 */
public final class Iconos {

    private Iconos() {
    }

    /** Base de los iconos: activa el antialiasing una sola vez. */
    private abstract static class Base extends JComponent {

        Base(int lado) {
            setPreferredSize(new Dimension(lado, lado));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            pintar(g2);
            g2.dispose();
        }

        protected abstract void pintar(Graphics2D g2);

        /** Dibuja texto centrado horizontal y verticalmente. */
        void textoCentrado(Graphics2D g2, String texto, int tamano, Color color) {
            g2.setFont(Paleta.fuente(tamano, Font.BOLD));
            g2.setColor(color);
            int ancho = g2.getFontMetrics().stringWidth(texto);
            int alto = g2.getFontMetrics().getAscent();
            g2.drawString(texto, (getWidth() - ancho) / 2, (getHeight() + alto) / 2 - 2);
        }
    }

    /** Monograma azul redondeado de la barra superior. */
    public static final class Monograma extends Base {

        public Monograma() {
            super(38);
        }

        @Override
        protected void pintar(Graphics2D g2) {
            g2.setColor(Paleta.PRIMARIO);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int x = getWidth() / 2;
            int y = getHeight() / 2;
            g2.drawLine(x, y - 3, x, y - 8);
            g2.drawLine(x - 7, y - 1, x - 7, y + 7);
            g2.drawLine(x - 7, y - 1, x + 7, y - 1);
            g2.drawLine(x + 7, y - 1, x + 7, y + 7);
            g2.drawLine(x - 7, y + 7, x + 7, y + 7);
            g2.drawLine(x, y + 7, x, y + 10);
        }
    }

    /** Boton de cierre del dialogo de la ficha. */
    public static final class Cerrar extends Base {

        public Cerrar() {
            super(34);
        }

        @Override
        protected void pintar(Graphics2D g2) {
            g2.setColor(Paleta.SUPERFICIE_2);
            g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            g2.setColor(Paleta.TEXTO_3);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int c = getWidth() / 2;
            int d = 4;
            g2.drawLine(c - d, c - d, c + d, c + d);
            g2.drawLine(c + d, c - d, c - d, c + d);
        }
    }

    /** Circulo con las iniciales del usuario. */
    public static final class Iniciales extends Base {

        private final String iniciales;

        /**
         * @param iniciales texto a mostrar, por ejemplo {@code AL}
         */
        public Iniciales(String iniciales) {
            super(34);
            this.iniciales = iniciales;
        }

        @Override
        protected void pintar(Graphics2D g2) {
            g2.setColor(Paleta.AZUL_SUAVE);
            g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            textoCentrado(g2, iniciales, 12, Paleta.PRIMARIO);
        }
    }

    /** Chevron del desplegable de usuario (decorativo). */
    public static final class Triangulo extends Base {

        public Triangulo() {
            super(12);
        }

        @Override
        protected void pintar(Graphics2D g2) {
            g2.setColor(Paleta.TEXTO_2);
            g2.drawPolygon(new int[]{3, 9, 6}, new int[]{4, 4, 8}, 3);
        }
    }

    /** Circulo con interrogacion del estado vacio. */
    public static final class Ayuda extends Base {

        public Ayuda() {
            super(40);
            setMaximumSize(new Dimension(40, 40));
        }

        @Override
        protected void pintar(Graphics2D g2) {
            int d = Math.min(getWidth(), getHeight());
            g2.setColor(Paleta.SUPERFICIE_2);
            g2.fillOval(0, 0, d - 1, d - 1);
            g2.setColor(Paleta.CONTORNO);
            g2.setStroke(new BasicStroke(1.4f));
            g2.drawOval(0, 0, d - 1, d - 1);
            textoCentrado(g2, "?", 15, Paleta.TENUE);
        }
    }
}
