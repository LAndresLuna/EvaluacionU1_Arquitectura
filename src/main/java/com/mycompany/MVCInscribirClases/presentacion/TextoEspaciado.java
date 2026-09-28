package com.mycompany.MVCInscribirClases.presentacion;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedString;
import javax.swing.JComponent;

/**
 * Texto dibujado con {@link TextLayout}, que sí respeta el atributo
 * {@code TRACKING}; un {@code JLabel} no admite un texto con atributos.
 *
 * @author andres
 */
public final class TextoEspaciado extends JComponent {

    private final String texto;
    private final Font fuente;
    private final java.awt.Color color;
    private final float tracking;

    public TextoEspaciado(String texto, int tamano, int estilo, java.awt.Color color,
            float tracking) {
        this.texto = texto;
        this.fuente = Paleta.fuente(tamano, estilo);
        this.color = color;
        this.tracking = tracking;
        setOpaque(false);
        setFont(this.fuente);
    }

    private TextLayout construirLayout() {
        try {
            AttributedString cadena = new AttributedString(texto);
            cadena.addAttribute(TextAttribute.FONT, fuente);
            cadena.addAttribute(TextAttribute.FOREGROUND, color);
            if (tracking > 0) {
                cadena.addAttribute(TextAttribute.TRACKING, tracking);
            }
            return new TextLayout(cadena.getIterator(),
                    new FontRenderContext(null, true, true));
        } catch (RuntimeException e) {
            // Si la fuente no admite alguno de los atributos, el texto se dibuja
            // igual, solo que sin separacion entre letras.
            return new TextLayout(texto, fuente, new FontRenderContext(null, true, true));
        }
    }

    @Override
    public String toString() {
        return texto;
    }

    @Override
    public Dimension getPreferredSize() {
        TextLayout layout = construirLayout();
        return new Dimension((int) Math.ceil(layout.getAdvance()) + 2,
                (int) Math.ceil(layout.getAscent() + layout.getDescent()) + 2);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        TextLayout layout = construirLayout();
        layout.draw(g2, 0, layout.getAscent());
        g2.dispose();
    }
}
