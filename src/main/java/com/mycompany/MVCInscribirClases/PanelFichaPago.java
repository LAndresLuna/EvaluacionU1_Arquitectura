package com.mycompany.MVCInscribirClases;

import dto.CursoDTO;
import dto.FichaPagoDTO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Contenido del dialogo de "Ficha de Pago": la referencia, el folio, el detalle
 * de los cursos y el costo total, con el estilo del storyboard.
 *
 * <p>Solo consume el {@link FichaPagoDTO} que publica el Modelo: no conoce el
 * dominio ni el Controlador.</p>
 *
 * @author andres
 */
public class PanelFichaPago extends JPanel {

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final JPanel lista = new JPanel();
    private final JLabel lblTotal = new JLabel();
    private final JLabel lblCantidad = new JLabel();
    private final JLabel lblFolio = new JLabel();
    private final JLabel lblFecha = new JLabel();

    /**
     * Crea el panel con sus componentes ya armados.
     */
    public PanelFichaPago() {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // El resumen y el total van anclados arriba y abajo; la lista de cursos
        // ocupa el centro. El alto de la lista se recalcula al mostrar() segun
        // cuantos cursos haya, para que el separador no quede pegado al borde.
        setLayout(new BorderLayout(0, 16));
        add(crearResumen(), BorderLayout.NORTH);
        add(lista, BorderLayout.CENTER);
        add(crearTotal(), BorderLayout.SOUTH);
    }

    /**
     * Muestra la ficha recibida.
     *
     * @param ficha datos de la ficha de pago generada
     */
    public void mostrar(FichaPagoDTO ficha) {
        if (ficha == null) {
            return;
        }
        // Sin HUD observable: si no, el Observer puede repintar la ficha mientras
        // el dialogo esta construyendo y duplicar sus filas.
        lista.removeAll();
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        int filas = 0;
        for (CursoDTO curso : ficha.cursos()) {
            lista.add(crearFila(curso));
            if (++filas < ficha.cursos().size()) {
                lista.add(Box.createVerticalStrut(8));
            }
        }
        lista.add(Box.createVerticalGlue());
        // alto exacto de las filas: el glue se lleva el sobrante
        lista.setPreferredSize(new Dimension(10, filas * 70));

        int cantidad = ficha.cantidadCursos();
        lblCantidad.setText(cantidad + (cantidad == 1 ? " curso" : " cursos"));
        lblFolio.setText(ficha.folio() == null ? "" : ficha.folio());
        lblFecha.setText(formatearFecha(ficha.fecha()));
        lblTotal.setText(VistaInscripcion.formatearMonto(ficha.total()));

        lista.revalidate();
        lista.repaint();
    }

    private JPanel crearResumen() {
        JPanel resumen = new JPanel(new BorderLayout());
        resumen.setOpaque(false);
        resumen.add(crearDato("FOLIO", lblFolio), BorderLayout.WEST);
        resumen.add(crearDato("FECHA", lblFecha), BorderLayout.EAST);
        return resumen;
    }

    private JPanel crearDato(String rotulo, JLabel dato) {
        dato.setFont(VistaInscripcion.fuente(13, Font.BOLD));
        dato.setForeground(VistaInscripcion.TEXTO);
        return new DatoConRotulo(rotulo, dato);
    }

    /**
     * Columna «rotulo encima del dato». Se dibuja a mano porque
     * {@code JLabel} no admite un texto con atributos de interletraje, y el
     * diseno usa versalitas espaciadas en los rotulos.
     */
    private static final class DatoConRotulo extends JPanel {

        private final VistaInscripcion.TextoEspaciado rotulo;
        private final JLabel dato;

        DatoConRotulo(String texto, JLabel dato) {
            this.rotulo = VistaInscripcion.espaciada(texto, 10,
                    VistaInscripcion.TEXTO_3, 0.09f);
            this.dato = dato;
            setOpaque(false);
            setLayout(new BorderLayout(0, 5));
            // Sin preferredSize fijo: el ancho lo da el texto y el alto lo
            // reparten las dos lineas del BorderLayout. Fijarlo a mano dejaba el
            // dato del folio y el de la fecha con 0 px de alto.
            add(rotulo, BorderLayout.NORTH);
            add(dato, BorderLayout.CENTER);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension r = rotulo.getPreferredSize();
            Dimension d = dato.getPreferredSize();
            return new Dimension(Math.max(r.width, d.width), r.height + d.height + 5);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }
    }

    private JPanel crearFila(CursoDTO curso) {
        VistaInscripcion.PanelRedondeado fila = new VistaInscripcion.PanelRedondeado(10,
                VistaInscripcion.SUPERFICIE_2, VistaInscripcion.BORDE);
        fila.setLayout(new BorderLayout(12, 0));
        fila.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        fila.setAlignmentX(0.5f);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        JLabel nombre = new JLabel(curso.nombre());
        nombre.setFont(VistaInscripcion.fuente(13, Font.BOLD));
        nombre.setForeground(VistaInscripcion.TEXTO);
        JLabel meta = new JLabel(curso.codigo());
        meta.setFont(VistaInscripcion.fuente(11, Font.PLAIN));
        meta.setForeground(VistaInscripcion.TEXTO_3);
        textos.add(nombre);
        textos.add(Box.createVerticalStrut(3));
        textos.add(meta);

        JLabel costo = new JLabel(VistaInscripcion.formatearMonto(curso.costo()),
                SwingConstants.RIGHT);
        costo.setFont(VistaInscripcion.fuente(13, Font.BOLD));
        costo.setForeground(VistaInscripcion.TEXTO);
        costo.setVerticalAlignment(SwingConstants.CENTER);

        fila.add(textos, BorderLayout.CENTER);
        fila.add(costo, BorderLayout.EAST);
        return fila;
    }

    private JPanel crearTotal() {
        lblTotal.setFont(VistaInscripcion.fuente(24, Font.BOLD));
        lblTotal.setVerticalAlignment(SwingConstants.CENTER);
        lblTotal.setForeground(VistaInscripcion.PRIMARIO);
        lblTotal.setHorizontalAlignment(SwingConstants.RIGHT);

        lblCantidad.setFont(VistaInscripcion.fuente(11, Font.PLAIN));
        lblCantidad.setForeground(VistaInscripcion.TEXTO_3);

        JPanel caja = new VistaInscripcion.PanelRedondeado(10, VistaInscripcion.AZUL_SUAVE, null);
        caja.setLayout(new BorderLayout(12, 4));
        caja.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(VistaInscripcion.espaciada("COSTO TOTAL:", 11, VistaInscripcion.PRIMARIO, 0.09f));
        textos.add(Box.createVerticalStrut(5));
        textos.add(lblCantidad);

        caja.add(textos, BorderLayout.WEST);
        caja.add(lblTotal, BorderLayout.EAST);

        // separador -> caja, ambos anclados arriba; el alto preferido es exacto
        // para que el pack() del dialogo no deje hueco bajo la caja.
        JPanel conjunto = new JPanel(new BorderLayout(0, 14));
        conjunto.setOpaque(false);
        conjunto.add(new JPanelSeparador(), BorderLayout.NORTH);
        conjunto.add(caja, BorderLayout.CENTER);
        conjunto.setPreferredSize(new Dimension(10, 1 + 14 + 78));
        return conjunto;
    }

    private static String formatearFecha(LocalDate fecha) {
        return fecha == null ? "" : FECHA.format(fecha);
    }

    /** Linea divisoria horizontal de 1 px. */
    private static final class JPanelSeparador extends JPanel {

        JPanelSeparador() {
            setOpaque(false);
            setPreferredSize(new Dimension(1, 1));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            setAlignmentX(0.5f);
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            g.setColor(VistaInscripcion.BORDE);
            g.fillRect(0, 0, getWidth(), 1);
        }
    }
}
