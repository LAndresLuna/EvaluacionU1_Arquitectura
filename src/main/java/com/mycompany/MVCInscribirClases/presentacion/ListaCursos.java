package com.mycompany.MVCInscribirClases.presentacion;

import dto.CursoDTO;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

/**
 * Lista vertical de cursos, dibujada con componentes propios en lugar de una
 * {@code JTable} para reproducir las filas del storyboard. Puede ser
 * seleccionable o de solo lectura.
 *
 * <p>La selección es estado local de la Vista, no del Modelo, así que la lista
 * avisa de sus cambios con {@link #setListener(Runnable)}.</p>
 *
 * @author andres
 */
public final class ListaCursos extends JPanel {

    private static final int ALTO_FILA = 58;
    private static final int ESPACIO = 10;
    private static final int INDICADOR = 18;

    private final boolean seleccionable;
    private final List<FilaCurso> filas = new ArrayList<>();
    private List<CursoDTO> cursos = List.of();
    private String codigoSeleccionado;
    private Runnable listener = () -> {
    };

    /**
     * Crea la lista.
     *
     * @param seleccionable {@code true} si el usuario puede resaltar filas
     */
    public ListaCursos(boolean seleccionable) {
        this.seleccionable = seleccionable;
        setOpaque(false);
        setLayout(new GridBagLayout());
    }

    public void setListener(Runnable listener) {
        this.listener = listener == null ? () -> {
        } : listener;
    }

    /**
     * Reemplaza los cursos de la lista. Si el contenido no cambió no reconstruye
     * nada, para que la fila resaltada no pierda el resaltado. Si el curso
     * resaltado ya no está, limpia la selección y avisa.
     *
     * @param cursos cursos a mostrar
     */
    public void setCursos(List<CursoDTO> cursos) {
        List<CursoDTO> nuevos = cursos == null ? List.of() : List.copyOf(cursos);
        if (nuevos.equals(this.cursos)) {
            return;
        }
        this.cursos = nuevos;

        filas.clear();
        removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, ESPACIO, 0);
        for (CursoDTO curso : nuevos) {
            FilaCurso fila = new FilaCurso(curso);
            filas.add(fila);
            gbc.gridy = filas.size() - 1;
            add(fila, gbc);
        }
        // Fila elástica: empuja el contenido hacia arriba cuando sobran espacio.
        gbc.gridy = filas.size();
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(new JPanel(), gbc);
        revalidate();
        repaint();

        if (codigoSeleccionado != null && !contiene(codigoSeleccionado)) {
            limpiarSeleccion();
        }
    }

    /** @return código del curso resaltado, o {@code null} si no hay ninguno */
    public String getCodigoSeleccionado() {
        return codigoSeleccionado;
    }

    /**
     * Resalta la fila del curso indicado, como si el usuario le hubiera dado
     * click.
     *
     * @return {@code true} si se resaltar alguna fila
     */
    public boolean seleccionar(String codigo) {
        if (!seleccionable || codigo == null) {
            return false;
        }
        for (FilaCurso fila : filas) {
            if (fila.curso.codigo().equals(codigo)) {
                seleccionar(fila);
                return true;
            }
        }
        return false;
    }

    /** Quita el resaltado y avisa del cambio. */
    public void limpiarSeleccion() {
        boolean habiaSeleccion = codigoSeleccionado != null;
        codigoSeleccionado = null;
        for (FilaCurso fila : filas) {
            fila.setSeleccionada(false);
        }
        if (habiaSeleccion) {
            listener.run();
        }
    }

    private boolean contiene(String codigo) {
        return cursos.stream().anyMatch(curso -> curso.codigo().equals(codigo));
    }

    private void seleccionar(FilaCurso fila) {
        if (!seleccionable) {
            return;
        }
        codigoSeleccionado = fila.curso.codigo();
        for (FilaCurso otra : filas) {
            otra.setSeleccionada(otra == fila);
        }
        listener.run();
    }

    /** Fila de un curso: indicador, nombre, código y costo. */
    private final class FilaCurso extends JPanel {

        private final CursoDTO curso;
        private boolean seleccionada;

        private FilaCurso(CursoDTO curso) {
            this.curso = curso;
            setOpaque(false);
            setLayout(new BorderLayout(12, 0));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, ALTO_FILA));
            setPreferredSize(new Dimension(10, ALTO_FILA));

            add(new Indicador(), BorderLayout.WEST);
            add(crearCentro(), BorderLayout.CENTER);
            add(crearCosto(), BorderLayout.EAST);

            if (seleccionable) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setToolTipText("Seleccionar " + curso.nombre());
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        seleccionar(FilaCurso.this);
                    }
                });
            }
            pintarFondo();
        }

        private JPanel crearCentro() {
            JPanel centro = new JPanel(new BorderLayout());
            centro.setOpaque(false);
            centro.add(Paleta.etiqueta(curso.nombre(), 14, Font.BOLD, Paleta.TEXTO),
                    BorderLayout.NORTH);
            centro.add(Paleta.etiqueta(curso.codigo(), 11, Font.PLAIN, Paleta.TEXTO_3),
                    BorderLayout.SOUTH);
            return centro;
        }

        private JLabel crearCosto() {
            JLabel costo = Paleta.etiqueta(Paleta.formatearMonto(curso.costo()), 14, Font.BOLD,
                    Paleta.TEXTO, SwingConstants.RIGHT);
            costo.setVerticalAlignment(SwingConstants.CENTER);
            return costo;
        }

        private void setSeleccionada(boolean valor) {
            if (this.seleccionada == valor) {
                return;
            }
            this.seleccionada = valor;
            pintarFondo();
            repaint();
        }

        private void pintarFondo() {
            if (seleccionada) {
                setOpaque(true);
                setBackground(Paleta.AZUL_SUAVE);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Paleta.PRIMARIO, 1, true),
                        BorderFactory.createEmptyBorder(1, 13, 1, 13)));
            } else {
                setOpaque(false);
                setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (!seleccionada && seleccionable) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Paleta.SUPERFICIE_2);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Paleta.BORDE);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    /** Círculo de la izquierda, con check verde en la lista de inscritos. */
    private final class Indicador extends JComponent {

        private Indicador() {
            setPreferredSize(new Dimension(INDICADOR, INDICADOR));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int d = INDICADOR;
            int x = (getWidth() - d) / 2;
            int y = (getHeight() - d) / 2;
            g2.setColor(Color.WHITE);
            g2.fillOval(x, y, d, d);
            g2.setColor(seleccionable ? Paleta.PRIMARIO : Paleta.VERDE);
            g2.setStroke(new BasicStroke(1.6f));
            g2.drawOval(x, y, d - 1, d - 1);
            if (!seleccionable) {
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND));
                g2.drawLine(x + 5, y + 9, x + 8, y + 12);
                g2.drawLine(x + 8, y + 12, x + 13, y + 6);
            }
            g2.dispose();
        }
    }

    /**
     * Envuelve un componente en un scroll sin marco y con barra fina.
     *
     * @return scroll panel listo para agregar a una tarjeta
     */
    public static JScrollPane enScrollPane(JComponent contenido) {
        JScrollPane scroll = new JScrollPane(contenido,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 14));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        JScrollBar barra = scroll.getVerticalScrollBar();
        barra.setPreferredSize(new Dimension(8, 8));
        barra.setOpaque(false);
        barra.setUnitIncrement(18);
        return scroll;
    }
}
