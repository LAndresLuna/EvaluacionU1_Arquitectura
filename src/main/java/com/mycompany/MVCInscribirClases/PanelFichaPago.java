package com.mycompany.MVCInscribirClases;

import com.mycompany.MVCInscribirClases.presentacion.Paleta;
import com.mycompany.MVCInscribirClases.presentacion.PanelRedondeado;
import com.mycompany.MVCInscribirClases.presentacion.Separador;
import com.mycompany.MVCInscribirClases.presentacion.TextoEspaciado;
import dto.CursoDTO;
import dto.FichaPagoDTO;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

/**
 * Contenido del diálogo de "Ficha de Pago". Solo consume el DTO que publica el
 * Modelo: no conoce el dominio ni el Controlador.
 *
 * @author andres
 */
public class PanelFichaPago extends JPanel {

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final int ALTO_FILA = 62;
    private static final int SEPARACION = 8;
    private static final int SEPARACION_BLOQUES = 16;
    private static final int ALTO_MAXIMO_LISTA = 300;

    private final JPanel lista = new JPanel();
    private final JScrollPane scrollLista;
    private final JPanel bloqueTotal = new JPanel(new BorderLayout(0, SEPARACION_BLOQUES));
    private final JPanel cajaTotal = new PanelRedondeado(10, Paleta.AZUL_SUAVE, null);
    private final JLabel lblTotal = new JLabel();
    private final JLabel lblCantidad = new JLabel();
    private final JLabel lblFolio = new JLabel();
    private final JLabel lblFecha = new JLabel();

    public PanelFichaPago() {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        lista.setOpaque(false);
        scrollLista = new JScrollPane(lista,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollLista.setBorder(BorderFactory.createEmptyBorder());
        scrollLista.setOpaque(false);
        scrollLista.getViewport().setOpaque(false);

        setLayout(new BorderLayout(0, SEPARACION_BLOQUES));
        add(crearResumen(), BorderLayout.NORTH);
        add(scrollLista, BorderLayout.CENTER);
        add(crearTotal(), BorderLayout.SOUTH);
    }

    public void mostrar(FichaPagoDTO ficha) {
        if (ficha == null) {
            return;
        }
        lista.removeAll();
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        List<CursoDTO> cursos = ficha.cursos();
        for (int i = 0; i < cursos.size(); i++) {
            lista.add(crearFila(cursos.get(i)));
            if (i < cursos.size() - 1) {
                lista.add(Box.createVerticalStrut(SEPARACION));
            }
        }
        lista.add(Box.createVerticalGlue());

        // El alto se deriva del alto real de las filas y se topa, en vez de
        // multiplicar por un número supuesto.
        Dimension filas = lista.getPreferredSize();
        int alto = Math.min(filas.height, ALTO_MAXIMO_LISTA);
        scrollLista.setPreferredSize(new Dimension(10, Math.max(alto, 1)));
        scrollLista.getViewport().setViewPosition(new Point(0, 0));

        int cantidad = ficha.cantidadCursos();
        lblCantidad.setText(cantidad + (cantidad == 1 ? " curso" : " cursos"));
        lblFolio.setText(ficha.folio() == null ? "" : ficha.folio());
        lblFecha.setText(formatearFecha(ficha.fecha()));
        lblTotal.setText(Paleta.formatearMonto(ficha.total()));

        bloqueTotal.setPreferredSize(new Dimension(10, alturaBloqueTotal()));

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
        dato.setFont(Paleta.fuente(13, Font.BOLD));
        dato.setForeground(Paleta.TEXTO);
        return new DatoConRotulo(rotulo, dato);
    }

    /** Rótulo espaciado encima del dato: un JLabel no admite interletraje. */
    private static final class DatoConRotulo extends JPanel {

        private final TextoEspaciado rotulo;
        private final JLabel dato;

        DatoConRotulo(String texto, JLabel dato) {
            this.rotulo = Paleta.espaciada(texto, 10, Paleta.TEXTO_3, 0.09f);
            this.dato = dato;
            setOpaque(false);
            setLayout(new BorderLayout(0, 5));
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
        PanelRedondeado fila = new PanelRedondeado(10, Paleta.SUPERFICIE_2, Paleta.BORDE);
        fila.setLayout(new BorderLayout(12, 0));
        fila.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, ALTO_FILA));
        fila.setAlignmentX(0.5f);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        JLabel nombre = new JLabel(curso.nombre());
        nombre.setFont(Paleta.fuente(13, Font.BOLD));
        nombre.setForeground(Paleta.TEXTO);
        JLabel meta = new JLabel(curso.codigo());
        meta.setFont(Paleta.fuente(11, Font.PLAIN));
        meta.setForeground(Paleta.TEXTO_3);
        textos.add(nombre);
        textos.add(Box.createVerticalStrut(3));
        textos.add(meta);

        JLabel costo = new JLabel(Paleta.formatearMonto(curso.costo()), SwingConstants.RIGHT);
        costo.setFont(Paleta.fuente(13, Font.BOLD));
        costo.setForeground(Paleta.TEXTO);
        costo.setVerticalAlignment(SwingConstants.CENTER);

        fila.add(textos, BorderLayout.CENTER);
        fila.add(costo, BorderLayout.EAST);
        return fila;
    }

    private JPanel crearTotal() {
        lblTotal.setFont(Paleta.fuente(24, Font.BOLD));
        lblTotal.setVerticalAlignment(SwingConstants.CENTER);
        lblTotal.setForeground(Paleta.PRIMARIO);
        lblTotal.setHorizontalAlignment(SwingConstants.RIGHT);

        lblCantidad.setFont(Paleta.fuente(11, Font.PLAIN));
        lblCantidad.setForeground(Paleta.TEXTO_3);

        cajaTotal.setLayout(new BorderLayout(12, 4));
        cajaTotal.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(Paleta.espaciada("COSTO TOTAL:", 11, Paleta.PRIMARIO, 0.09f));
        textos.add(Box.createVerticalStrut(5));
        textos.add(lblCantidad);

        cajaTotal.add(textos, BorderLayout.WEST);
        cajaTotal.add(lblTotal, BorderLayout.EAST);

        bloqueTotal.setOpaque(false);
        bloqueTotal.add(new Separador(), BorderLayout.NORTH);
        bloqueTotal.add(cajaTotal, BorderLayout.CENTER);
        return bloqueTotal;
    }

    private int alturaBloqueTotal() {
        return new Separador().getPreferredSize().height
                + SEPARACION_BLOQUES
                + cajaTotal.getPreferredSize().height;
    }

    private static String formatearFecha(LocalDate fecha) {
        return fecha == null ? "" : FECHA.format(fecha);
    }
}
