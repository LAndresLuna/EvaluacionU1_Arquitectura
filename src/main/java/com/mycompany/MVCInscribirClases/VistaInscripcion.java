package com.mycompany.MVCInscribirClases;

import dto.CursoDTO;
import dto.FichaPagoDTO;
import dto.ResumenInscripcionDTO;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedString;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Vista del patrón MVC: la ventana de "Inscribirse en Clases".
 *
 * <p>Muestra el catálogo de cursos disponibles con su costo, la lista de cursos
 * inscritos, el monto a pagar y, al finalizar, la ficha de pago en un diálogo
 * modal. Nunca toca objetos del dominio: pide las acciones al
 * {@link ControlInscripcion} y consume únicamente los DTOs que el Modelo le
 * entrega a través de sus getters, cada vez que el Modelo la notifica
 * (patrón Observer).</p>
 *
 * <p>El estilo reproduce el storyboard de Figma: barra superior, bloque de
 * título con indicador de pasos, dos tarjetas con listas propias y barra
 * inferior con el total. Los colores y las medidas viven en las constantes
 * {@code FONDO}, {@code PRIMARIO}, etc., para poder ajustarlas en un lugar.</p>
 *
 * @author andres
 */
public class VistaInscripcion extends JFrame implements IObserverInscripcion {

    /* ------------------------------------------------------------ paleta */
    static final Color FONDO = new Color(0xF3F4FA);
    static final Color SUPERFICIE = new Color(0xFFFFFF);
    static final Color SUPERFICIE_2 = new Color(0xF9FAFD);
    static final Color AZUL_SUAVE = new Color(0xE7EBFA);
    static final Color PRIMARIO = new Color(0x2847A8);
    static final Color PRIMARIO_HOVER = new Color(0x1F3A8F);
    static final Color TEXTO = new Color(0x172033);
    static final Color TEXTO_2 = new Color(0x9CA4B0);
    static final Color TEXTO_3 = new Color(0x7B8799);
    static final Color VERDE = new Color(0x30846C);
    static final Color BORDE = new Color(0xE6EAF2);
    static final Color SCRIM = new Color(0x2A303D);

    private static final int RADIO = 14;

    /* ------------------- datos fijos del storyboard (no vienen del modelo) */
    private static final String UNIVERSIDAD = "Universidad Central";
    private static final String UNIVERSIDAD_SUB = "PORTAL ACADÉMICO";
    private static final String SEMESTRE = "Semestre 2026-2";
    private static final String USUARIO = "Andrés Luna";
    private static final String USUARIO_SUB = "Ingeniería de Software";
    private static final String INICIALES = "AL";
    private static final String PERIODO = "Periodo agosto–diciembre";
    private static final String TITULO = "Selecciona tus cursos";
    private static final String SECCION = "Inscripción académica";
    private static final String PISTA_INSCRIBIR = "Agregar curso seleccionado";
    private static final String PISTA_VACIA =
            "Selecciona un curso disponible y utiliza \"Inscribir\" para agregarlo a tu carga académica.";

    private static final String TARJETA_LISTA = "lista";
    private static final String TARJETA_VACIA = "vacia";

    /* --------------------------------------------------------- componentes */
    private final ControlInscripcion control;

    private final ListaCursos listaDisponibles;
    private final ListaCursos listaInscritos;
    private final CardLayout centroDerecho = new CardLayout();
    private final JPanel panelDerecho = new JPanel(centroDerecho);

    private final JLabel contadorDisponibles = new JLabel();
    private final JLabel contadorInscritos = new JLabel();
    private final JLabel lblParcial = new JLabel();
    private final JLabel lblResumen = new JLabel();
    private final JLabel lblTotal = new JLabel();
    private final JLabel lblEstado = new JLabel();
    private final JButton btnInscribir = new Boton("Inscribir", false);
    private final JButton btnFinalizar = new Boton("Finalizar Inscripción →", true);
    private final PanelFichaPago panelFichaPago = new PanelFichaPago();

    private JDialog ventanaFicha;
    /** Evita que una segunda notificacion vuelva a abrir la ficha ya mostrada. */
    private boolean fichaMostrada;

    /**
     * Crea la ventana y arma sus componentes.
     *
     * @param control controlador que traduce los eventos de la pantalla
     */
    public VistaInscripcion(ControlInscripcion control) {
        this.control = control;
        this.listaDisponibles = new ListaCursos(true);
        this.listaInscritos = new ListaCursos(false);

        setTitle("Inscripción en Clases — Portal Académico");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1080, 660));
        setSize(new Dimension(1280, 800));
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        getContentPane().setBackground(FONDO);
        add(crearBarraSuperior(), BorderLayout.NORTH);
        add(crearCuerpo(), BorderLayout.CENTER);
        add(crearBarraInferior(), BorderLayout.SOUTH);

        configurarEstilos();
        configurarEventos();
    }

    /**
     * Muestra la ventana en pantalla.
     */
    public void iniciar() {
        setVisible(true);
    }

    /* =================================================== observer ======== */

    /**
     * Callback del patrón Observer: el Modelo avisa que su estado cambió y la
     * Vista vuelve a leer los DTOs para actualizarse. Si la notificación llega
     * desde otro hilo, el refresco se encola en el hilo de Swing.
     *
     * @param modelo modelo ya actualizado
     */
    @Override
    public void update(IModeloInscripcion modelo) {
        if (modelo == null) {
            return;
        }
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> update(modelo));
            return;
        }
        refrescar(modelo);
    }

    /**
     * Vuelca en los componentes el estado que publica el Modelo.
     *
     * @param modelo modelo a leer
     */
    private void refrescar(IModeloInscripcion modelo) {
        List<CursoDTO> disponibles = modelo.getCursosDisponibles();
        ResumenInscripcionDTO resumen = modelo.getResumenInscripcion();
        FichaPagoDTO ficha = modelo.getFichaPago();

        listaDisponibles.setCursos(disponibles);
        contadorDisponibles.setText(disponibles.size() + (disponibles.size() == 1
                ? " disponible" : " disponibles"));

        listaInscritos.setCursos(resumen.cursos());
        contadorInscritos.setText(resumen.cantidadCursos() + (resumen.cantidadCursos() == 1
                ? " inscrito" : " inscritos"));

        lblParcial.setText(formatearMonto(resumen.total()));
        lblResumen.setText(resumen.cantidadCursos() + (resumen.cantidadCursos() == 1
                ? " curso inscrito" : " cursos inscritos"));
        lblTotal.setText(formatearMonto(resumen.total()));

        centroDerecho.show(panelDerecho,
                resumen.cantidadCursos() == 0 ? TARJETA_VACIA : TARJETA_LISTA);

        // El boton Inscribir solo se habilita si ademas hay un curso resaltado,
        // como en el storyboard; el Control decide si la inscripcion sigue abierta.
        btnInscribir.setEnabled(control.puedeInscribir()
                && listaDisponibles.getCodigoSeleccionado() != null);
        btnFinalizar.setEnabled(control.puedeFinalizar());

        actualizarEstado(modelo, resumen, ficha);

        if (ficha != null && !fichaMostrada) {
            fichaMostrada = true;
            panelFichaPago.mostrar(ficha);
            // El dialogo es modal y setVisible bloquea: hay que abrirlo despues
            // de que refrescar termine, o el hilo de Swing se queda esperando.
            SwingUtilities.invokeLater(this::abrirFicha);
        }
    }

    /**
     * Elige el texto de la barra de estado según el resultado de la última
     * operación.
     *
     * @param modelo  modelo ya actualizado
     * @param resumen resumen de la inscripción
     * @param ficha   ficha de pago generada o {@code null}
     */
    private void actualizarEstado(IModeloInscripcion modelo, ResumenInscripcionDTO resumen,
            FichaPagoDTO ficha) {
        String error = modelo.getMensajeError();
        if (error != null && !error.isBlank()) {
            lblEstado.setText(error);
            lblEstado.setForeground(new Color(0xB3261E));
            return;
        }
        if (ficha != null) {
            lblEstado.setText("Inscripción finalizada. Ficha de pago " + ficha.folio());
            lblEstado.setForeground(VERDE);
            return;
        }
        lblEstado.setForeground(TEXTO_3);
        if (resumen.cantidadCursos() == 0) {
            lblEstado.setText("Seleccione un curso de la lista de disponibles para inscribirlo.");
        } else {
            lblEstado.setText("Puedes modificar tu selección antes de finalizar.");
        }
    }

    /**
     * Formatea un monto para mostrarlo en la interfaz.
     *
     * @param monto cantidad a formatear
     * @return texto con el símbolo de moneda y separador de miles
     */
    static String formatearMonto(double monto) {
        NumberFormat formato = NumberFormat.getCurrencyInstance(Locale.US);
        formato.setMinimumFractionDigits(2);
        formato.setMaximumFractionDigits(2);
        return formato.format(monto);
    }

    /* ==================================================== construccion === */

    private JPanel crearBarraSuperior() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(SUPERFICIE);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE),
                BorderFactory.createEmptyBorder(14, 24, 14, 24)));

        barra.add(crearMarca(), BorderLayout.WEST);
        barra.add(crearZonaUsuario(), BorderLayout.EAST);
        return barra;
    }

    private JPanel crearMarca() {
        JPanel marca = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        marca.setOpaque(false);
        marca.add(new IconoMonograma());

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(etiqueta(UNIVERSIDAD, 15, Font.BOLD, TEXTO));
        textos.add(Box.createVerticalStrut(2));
        textos.add(espaciada(UNIVERSIDAD_SUB, 10, TEXTO_2, 0.10f));
        marca.add(textos);
        return marca;
    }

    private JPanel crearZonaUsuario() {
        JPanel zona = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        zona.setOpaque(false);
        zona.add(chip(SEMESTRE, false));

        JPanel divisor = new JPanel();
        divisor.setPreferredSize(new Dimension(1, 30));
        divisor.setBackground(BORDE);
        zona.add(divisor);

        zona.add(new IconoIniciales(INICIALES));

        JPanel datos = new JPanel();
        datos.setOpaque(false);
        datos.setLayout(new BoxLayout(datos, BoxLayout.Y_AXIS));
        datos.add(etiqueta(USUARIO, 13, Font.BOLD, TEXTO));
        datos.add(Box.createVerticalStrut(1));
        datos.add(etiqueta(USUARIO_SUB, 11, Font.PLAIN, TEXTO_3));
        zona.add(datos);
        zona.add(new Triangulo());
        return zona;
    }

    private JPanel crearCuerpo() {
        JPanel cuerpo = new JPanel(new BorderLayout(0, 18));
        cuerpo.setOpaque(false);
        cuerpo.setBorder(BorderFactory.createEmptyBorder(22, 24, 18, 24));
        cuerpo.add(crearEncabezado(), BorderLayout.NORTH);
        cuerpo.add(crearColumnas(), BorderLayout.CENTER);
        return cuerpo;
    }

    private JPanel crearEncabezado() {
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(espaciada(SECCION.toUpperCase(), 11, PRIMARIO, 0.09f));
        textos.add(Box.createVerticalStrut(6));
        textos.add(etiqueta(TITULO, 28, Font.BOLD, TEXTO));
        textos.add(Box.createVerticalStrut(6));
        textos.add(etiqueta(PERIODO, 13, Font.PLAIN, TEXTO_2));
        cabecera.add(textos, BorderLayout.WEST);

        JPanel pasos = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        pasos.setOpaque(false);
        pasos.add(new Paso("1", "Datos", false));
        pasos.add(new Conector());
        pasos.add(new Paso("2", "Cursos", true));
        pasos.add(new Conector());
        pasos.add(new Paso("3", "Confirmación", false));
        cabecera.add(pasos, BorderLayout.EAST);
        return cabecera;
    }

    private JPanel crearColumnas() {
        JPanel columnas = new JPanel(new GridLayout(1, 2, 22, 0));
        columnas.setOpaque(false);
        columnas.add(crearTarjetaDisponibles());
        columnas.add(crearTarjetaInscritos());
        return columnas;
    }

    private JPanel crearTarjetaDisponibles() {
        PanelRedondeado tarjeta = new PanelRedondeado(RADIO, SUPERFICIE, BORDE);
        tarjeta.setLayout(new BorderLayout(0, 0));
        tarjeta.add(crearEncabezadoTarjeta("Cursos Disponibles", "Selecciona un curso para inscribir",
                contadorDisponibles, false), BorderLayout.NORTH);
        tarjeta.add(ListaCourses.envolver(listaDisponibles), BorderLayout.CENTER);
        tarjeta.add(crearPieInscribir(), BorderLayout.SOUTH);
        return tarjeta;
    }

    private JPanel crearTarjetaInscritos() {
        PanelRedondeado tarjeta = new PanelRedondeado(RADIO, SUPERFICIE, BORDE);
        tarjeta.setLayout(new BorderLayout(0, 0));
        tarjeta.add(crearEncabezadoTarjeta("Cursos Inscritos", "Tu carga académica actual",
                contadorInscritos, true), BorderLayout.NORTH);

        panelDerecho.setOpaque(false);
        panelDerecho.add(ListaCourses.envolver(listaInscritos), TARJETA_LISTA);
        panelDerecho.add(crearEstadoVacio(), TARJETA_VACIA);
        centroDerecho.show(panelDerecho, TARJETA_VACIA);
        tarjeta.add(panelDerecho, BorderLayout.CENTER);
        tarjeta.add(crearPieResumen(), BorderLayout.SOUTH);
        return tarjeta;
    }

    private JPanel crearEncabezadoTarjeta(String titulo, String subtitulo, JLabel contador,
            boolean verde) {
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);
        cabecera.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE),
                BorderFactory.createEmptyBorder(18, 20, 14, 20)));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(etiqueta(titulo, 17, Font.BOLD, TEXTO));
        textos.add(Box.createVerticalStrut(3));
        textos.add(etiqueta(subtitulo, 11, Font.PLAIN, TEXTO_3));
        cabecera.add(textos, BorderLayout.WEST);
        cabecera.add(chip(contador, verde), BorderLayout.EAST);
        return cabecera;
    }

    private JPanel crearPieInscribir() {
        JPanel pie = new JPanel(new BorderLayout(0, 0));
        pie.setOpaque(false);
        pie.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDE),
                BorderFactory.createEmptyBorder(16, 20, 18, 20)));

        btnInscribir.setFont(fuente(13, Font.BOLD));
        btnInscribir.setAlignmentX(0.5f);
        btnInscribir.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnInscribir.setPreferredSize(new Dimension(10, 40));

        JPanel acciones = new JPanel();
        acciones.setOpaque(false);
        acciones.setLayout(new BoxLayout(acciones, BoxLayout.Y_AXIS));
        acciones.add(btnInscribir);
        acciones.add(Box.createVerticalStrut(8));
        JLabel pista = etiqueta(PISTA_INSCRIBIR, 11, Font.PLAIN, TEXTO_2, SwingConstants.CENTER);
        pista.setAlignmentX(0.5f);
        acciones.add(pista);
        pie.add(acciones, BorderLayout.CENTER);
        return pie;
    }

    private JPanel crearPieResumen() {
        JPanel pie = new JPanel(new BorderLayout(0, 0));
        pie.setOpaque(false);
        pie.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDE),
                BorderFactory.createEmptyBorder(16, 20, 18, 20)));

        lblParcial.setFont(fuente(20, Font.BOLD));
        lblParcial.setForeground(TEXTO);
        lblResumen.setFont(fuente(11, Font.PLAIN));
        lblResumen.setForeground(TEXTO_3);

        JPanel montos = new JPanel();
        montos.setOpaque(false);
        montos.setLayout(new BoxLayout(montos, BoxLayout.Y_AXIS));
        montos.add(lblParcial);
        montos.add(Box.createVerticalStrut(2));
        montos.add(lblResumen);
        pie.add(montos, BorderLayout.WEST);
        return pie;
    }

    private JPanel crearEstadoVacio() {
        JPanel vacio = new JPanel(new GridBagLayout());
        vacio.setOpaque(false);
        vacio.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        IconoAyuda icono = new IconoAyuda();
        icono.setAlignmentX(0.5f);
        textos.add(icono);
        textos.add(Box.createVerticalStrut(14));
        JLabel mensaje = etiqueta("<html><div style='text-align:center;width:260px'>"
                + PISTA_VACIA + "</div></html>", 12, Font.PLAIN, TEXTO_2, SwingConstants.CENTER);
        mensaje.setAlignmentX(0.5f);
        textos.add(mensaje);

        vacio.add(textos);
        return vacio;
    }

    private JPanel crearBarraInferior() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(SUPERFICIE);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDE),
                BorderFactory.createEmptyBorder(16, 24, 16, 24)));

        lblEstado.setFont(fuente(12, Font.PLAIN));

        JPanel total = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        total.setOpaque(false);
        JLabel etiquetaTotal = etiqueta("Total a pagar:", 12, Font.PLAIN, TEXTO_3);
        etiquetaTotal.setAlignmentY(SwingConstants.CENTER);
        lblTotal.setFont(fuente(24, Font.BOLD));
        lblTotal.setForeground(TEXTO);
        total.add(etiquetaTotal);
        total.add(lblTotal);
        total.add(Box.createHorizontalStrut(8));
        btnFinalizar.setFont(fuente(13, Font.BOLD));
        btnFinalizar.setPreferredSize(new Dimension(210, 44));
        total.add(btnFinalizar);

        barra.add(lblEstado, BorderLayout.CENTER);
        barra.add(total, BorderLayout.EAST);
        return barra;
    }

    /* ==================================================== ficha (modal) == */

    private void abrirFicha() {
        cerrarFicha();
        JDialog ventana = new JDialog(this, true);
        ventana.setUndecorated(true);

        JPanel fondo = new JPanel(new GridBagLayout());
        fondo.setBackground(SCRIM);
        fondo.setBorder(BorderFactory.createEmptyBorder(48, 48, 48, 48));

        PanelRedondeado tarjeta = new PanelRedondeado(16, SUPERFICIE, null);
        tarjeta.setLayout(new BorderLayout(0, 0));
        tarjeta.add(crearEncabezadoFicha(), BorderLayout.NORTH);
        tarjeta.add(crearCuerpoFicha(), BorderLayout.CENTER);
        tarjeta.add(crearPieFicha(), BorderLayout.SOUTH);

        // El alto sale del contenido: con un setSize fijo las filas quedaban
        // cortadas cuando eran pocas.
        // El ancho lo fija el diseno (600 px de contenido). El alto lo deja el
        // pack(): depende de cuantosursos haya en la ficha.
        // El ancho (600) viene del diseno; el alto lo deja el pack() segun
        // cuantos cursos tenga la ficha.
        tarjeta.setPreferredSize(new Dimension(600, tarjeta.getPreferredSize().height));
        fondo.add(tarjeta);

        ventana.setContentPane(fondo);
        ventana.pack();
        ventana.setLocationRelativeTo(this);
        ventana.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ventanaFicha = null;
            }
        });
        ventanaFicha = ventana;
        ventana.setVisible(true);
    }

    private JPanel crearEncabezadoFicha() {
        JPanel cabecera = new JPanel(new BorderLayout(16, 0));
        cabecera.setOpaque(false);
        cabecera.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE),
                BorderFactory.createEmptyBorder(20, 24, 18, 24)));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(etiqueta("Ficha de Pago", 20, Font.BOLD, TEXTO));
        textos.add(Box.createVerticalStrut(3));
        textos.add(etiqueta("Resumen de inscripción · " + SEMESTRE, 11, Font.PLAIN, TEXTO_3));
        cabecera.add(textos, BorderLayout.WEST);

        // La "X" se dibuja a mano: los simbolos Unicode como ✕ no siempre estan
        // en la fuente del sistema y salian como un cuadro vacio.
        IconoCerrar cerrar = new IconoCerrar();
        cerrar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cerrar.setToolTipText("Cerrar la ficha de pago");
        cerrar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cerrarFicha();
            }
        });
        cabecera.add(cerrar, BorderLayout.EAST);
        return cabecera;
    }

    private JPanel crearCuerpoFicha() {
        JPanel cuerpo = new JPanel(new BorderLayout());
        cuerpo.setOpaque(false);
        // Sin padding inferior: el borde de la tarjeta lo aporta el pie, y un
        // margen extra dejaba un escalon de fondo entre la ficha y el boton.
        cuerpo.setBorder(BorderFactory.createEmptyBorder(20, 24, 0, 24));
        cuerpo.add(panelFichaPago, BorderLayout.CENTER);
        return cuerpo;
    }

    private JPanel crearPieFicha() {
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pie.setOpaque(false);
        pie.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDE),
                BorderFactory.createEmptyBorder(16, 24, 20, 24)));
        JButton cerrar = new Boton("Cerrar", false);
        cerrar.setFont(fuente(13, Font.BOLD));
        cerrar.setPreferredSize(new Dimension(120, 40));
        cerrar.addActionListener(e -> cerrarFicha());
        pie.add(cerrar);
        return pie;
    }

    private void cerrarFicha() {
        if (ventanaFicha != null) {
            ventanaFicha.dispose();
            ventanaFicha = null;
        }
    }

    /* ========================================================= eventos === */

    private void configurarEventos() {
        btnInscribir.addActionListener(e -> {
            control.inscribir(listaDisponibles.getCodigoSeleccionado());
            listaDisponibles.limpiarSeleccion();
        });
        btnFinalizar.addActionListener(e -> control.finalizarInscripcion());
    }

    private void configurarEstilos() {
        btnInscribir.setEnabled(true);
        contadorDisponibles.setForeground(PRIMARIO);
        contadorInscritos.setForeground(VERDE);
    }

    /* ================================================ piezas reutilizables */

    /** Etiqueta de texto con familia, tamaño y color del storyboard. */
    static JLabel etiqueta(String texto, int tamano, int estilo, Color color) {
        return etiqueta(texto, tamano, estilo, color, SwingConstants.LEFT);
    }

    private static JLabel etiqueta(String texto, int tamano, int estilo, Color color,
            int alineacion) {
        JLabel etiqueta = new JLabel(texto, alineacion);
        etiqueta.setFont(fuente(tamano, estilo));
        etiqueta.setForeground(color);
        return etiqueta;
    }

    /** Etiqueta con interletraje, para los rotulos en versalitas del diseno. */
    static TextoEspaciado espaciada(String texto, int tamano, Color color) {
        return espaciada(texto, tamano, color, 0.09f);
    }

    /** Etiqueta con interletraje ajustable. */
    static TextoEspaciado espaciada(String texto, int tamano, Color color, float tracking) {
        return new TextoEspaciado(texto, tamano, Font.BOLD, color, tracking);
    }

    /**
     * Texto dibujado con {@code TextLayout}, que si respeta el atributo
     * {@code TRACKING}; {@code JLabel} no admite un texto con atributos.
     */
    static final class TextoEspaciado extends JComponent {

        private final String texto;
        private final Font fuente;
        private final Color color;
        private final float tracking;

        TextoEspaciado(String texto, int tamano, int estilo, Color color, float tracking) {
            this.texto = texto;
            this.fuente = fuente(tamano, estilo);
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
            } catch (Exception e) {
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

    /**
     * Crea una fuente de la tipografia del storyboard.
     *
     * @param tamano tamaño en puntos
     * @param estilo {@link Font#PLAIN} o {@link Font#BOLD}
     * @return fuente lista para usar
     */
    static Font fuente(int tamano, int estilo) {
        return new Font(Font.SANS_SERIF, estilo, tamano);
    }

    /** Panel con esquinas redondeadas, fondo y borde opcionales. */
    static final class PanelRedondeado extends JPanel {

        private final int radio;
        private final Color relleno;
        private final Color borde;

        PanelRedondeado(int radio, Color relleno, Color borde) {
            this.radio = radio;
            this.relleno = relleno;
            this.borde = borde;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(relleno);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radio, radio);
            if (borde != null) {
                g2.setColor(borde);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radio, radio);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Boton con la forma y los colores del storyboard. */
    static final class Boton extends JButton {

        private final boolean primario;

        Boton(String texto, boolean primario) {
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
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            boolean activo = isEnabled();
            if (primario) {
                boolean encima = getModel().isRollover();
                g2.setColor(!activo ? new Color(0x9BA5C9) : encima ? PRIMARIO_HOVER : PRIMARIO);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            } else {
                boolean encima = getModel().isRollover();
                g2.setColor(!activo ? SUPERFICIE_2 : encima ? AZUL_SUAVE : SUPERFICIE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(!activo ? BORDE : PRIMARIO);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            }
            g2.dispose();

            Color anterior = getForeground();
            setForeground(primario ? (activo ? Color.WHITE : new Color(0xEFF1F7)) : PRIMARIO);
            super.paintComponent(g);
            setForeground(anterior);
        }
    }

    /** Pastilla con contador o metadato. */
    private static JPanel chip(String texto, boolean verde) {
        return chip(etiqueta(texto, 11, Font.BOLD, verde ? VERDE : PRIMARIO), verde);
    }

    private static JPanel chip(JLabel etiqueta, boolean verde) {
        Color relleno = verde ? new Color(0xE6F4EF) : AZUL_SUAVE;
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                new RedondeadoPlano(10, relleno),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)));
        panel.add(etiqueta);
        return panel;
    }

    /** Borde que solo pinta el relleno redondeado, para las pastillas. */
    private static final class RedondeadoPlano extends javax.swing.border.AbstractBorder {

        private final int radio;
        private final Color color;

        RedondeadoPlano(int radio, Color color) {
            this.radio = radio;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillRoundRect(x, y, w - 1, h - 1, radio, radio);
            g2.dispose();
        }
    }

    /** Estepa del indicador de pasos. */
    private static final class Paso extends JComponent {

        private final String numero;
        private final String titulo;
        private final boolean activo;

        Paso(String numero, String titulo, boolean activo) {
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
            int d = 24;
            int y = (getHeight() - d) / 2;
            g2.setColor(activo ? PRIMARIO : SUPERFICIE);
            g2.fillOval(0, y, d, d);
            g2.setColor(activo ? PRIMARIO : new Color(0xB9C0CF));
            g2.setStroke(new BasicStroke(1.4f));
            g2.drawOval(0, y, d - 1, d - 1);
            g2.setFont(fuente(11, Font.BOLD));
            g2.setColor(activo ? Color.WHITE : new Color(0x8A93A6));
            int ancho = g2.getFontMetrics().stringWidth(numero);
            g2.drawString(numero, (d - ancho) / 2, y + d / 2 + 4);
            g2.setFont(fuente(13, activo ? Font.BOLD : Font.PLAIN));
            g2.setColor(activo ? TEXTO : new Color(0x8A93A6));
            g2.drawString(titulo, d + 8, getHeight() / 2 + 5);
            g2.dispose();
        }
    }

    /** Linea que une dos pasos. */
    private static final class Conector extends JComponent {

        Conector() {
            setPreferredSize(new Dimension(34, 28));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(0xD3D8E3));
            g2.setStroke(new BasicStroke(1.4f));
            g2.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
            g2.dispose();
        }
    }

    /** Monograma azul redondeado de la barra superior. */
    private static final class IconoMonograma extends JComponent {

        IconoMonograma() {
            setPreferredSize(new Dimension(38, 38));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(PRIMARIO);
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
            g2.dispose();
        }
    }

    /** Boton de cierre del dialogo de la ficha, dibujado sin depender de fuentes. */
    private static final class IconoCerrar extends JComponent {

        IconoCerrar() {
            setPreferredSize(new Dimension(34, 34));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(SUPERFICIE_2);
            g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            g2.setColor(TEXTO_3);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int c = getWidth() / 2;
            int d = 4;
            g2.drawLine(c - d, c - d, c + d, c + d);
            g2.drawLine(c + d, c - d, c - d, c + d);
            g2.dispose();
        }
    }

    /** Circulo con iniciales del usuario. */
    private static final class IconoIniciales extends JComponent {

        private final String iniciales;

        IconoIniciales(String iniciales) {
            this.iniciales = iniciales;
            setPreferredSize(new Dimension(34, 34));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(AZUL_SUAVE);
            g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            g2.setFont(fuente(12, Font.BOLD));
            g2.setColor(PRIMARIO);
            int ancho = g2.getFontMetrics().stringWidth(iniciales);
            int alto = g2.getFontMetrics().getAscent();
            g2.drawString(iniciales, (getWidth() - ancho) / 2,
                    (getHeight() + alto) / 2 - 2);
            g2.dispose();
        }
    }

    /** Chevron del desplegable de usuario (decorativo). */
    private static final class Triangulo extends JComponent {

        Triangulo() {
            setPreferredSize(new Dimension(12, 12));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(TEXTO_2);
            g2.drawPolygon(new int[]{3, 9, 6}, new int[]{4, 4, 8}, 3);
            g2.dispose();
        }
    }

    /** Circulo con interrogacion del estado vacio. */
    private static final class IconoAyuda extends JComponent {

        IconoAyuda() {
            setPreferredSize(new Dimension(40, 40));
            setMaximumSize(new Dimension(40, 40));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int d = Math.min(getWidth(), getHeight());
            g2.setColor(SUPERFICIE_2);
            g2.fillOval(0, 0, d - 1, d - 1);
            g2.setColor(new Color(0xB9C0CF));
            g2.setStroke(new BasicStroke(1.4f));
            g2.drawOval(0, 0, d - 1, d - 1);
            g2.setFont(fuente(15, Font.BOLD));
            g2.setColor(new Color(0x8A93A6));
            String interrogante = "?";
            int ancho = g2.getFontMetrics().stringWidth(interrogante);
            int alto = g2.getFontMetrics().getAscent();
            g2.drawString(interrogante, (d - ancho) / 2, (d + alto) / 2 - 2);
            g2.dispose();
        }
    }

    /**
     * Lista vertical de cursos, dibujada con componentes propios en lugar de
     * una {@code JTable} para reproducir las filas del storyboard.
     */
    static final class ListaCursos extends JPanel {

        private static final int ALTO_FILA = 58;
        private static final int ESPACIO = 10;

        private final boolean seleccionable;
        private final List<FilaCurso> filas = new ArrayList<>();
        private String codigoSeleccionado;

        ListaCursos(boolean seleccionable) {
            this.seleccionable = seleccionable;
            setOpaque(false);
            setLayout(new GridBagLayout());
        }

        void setCursos(List<CursoDTO> cursos) {
            filas.clear();
            removeAll();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(0, 0, ESPACIO, 0);
            for (CursoDTO curso : cursos) {
                FilaCurso fila = new FilaCurso(curso, seleccionable);
                filas.add(fila);
                gbc.gridy = filas.size() - 1;
                add(fila, gbc);
            }
            gbc.gridy = filas.size();
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            add(new JPanel(), gbc);
            revalidate();
            repaint();
        }

        String getCodigoSeleccionado() {
            return codigoSeleccionado;
        }

        void limpiarSeleccion() {
            codigoSeleccionado = null;
            for (FilaCurso fila : filas) {
                fila.setSeleccionada(false);
            }
        }

        private void seleccionar(FilaCurso fila) {
            codigoSeleccionado = fila.curso.codigo();
            for (FilaCurso otra : filas) {
                otra.setSeleccionada(otra == fila);
            }
        }

        /** Fila de un curso: indicador, nombre, codigo y costo. */
        private final class FilaCurso extends JPanel {

            private final CursoDTO curso;
            private boolean seleccionada;

            private FilaCurso(CursoDTO curso, boolean seleccionable) {
                this.curso = curso;
                setOpaque(false);
                setLayout(new BorderLayout(12, 0));
                setMaximumSize(new Dimension(Integer.MAX_VALUE, ALTO_FILA));
                setPreferredSize(new Dimension(10, ALTO_FILA));

                add(new Indicador(seleccionable), BorderLayout.WEST);
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
                JLabel nombre = etiqueta(curso.nombre(), 14, Font.BOLD, TEXTO);
                JLabel meta = etiqueta(curso.codigo(), 11, Font.PLAIN, TEXTO_3);
                centro.add(nombre, BorderLayout.NORTH);
                centro.add(meta, BorderLayout.SOUTH);
                return centro;
            }

            private JLabel crearCosto() {
                JLabel costo = new JLabel(formatearMonto(curso.costo()), SwingConstants.RIGHT);
                costo.setFont(fuente(14, Font.BOLD));
                costo.setForeground(TEXTO);
                costo.setVerticalAlignment(SwingConstants.CENTER);
                return costo;
            }

            private void setSeleccionada(boolean valor) {
                this.seleccionada = valor;
                pintarFondo();
                repaint();
            }

            private void pintarFondo() {
                if (seleccionada) {
                    setOpaque(true);
                    setBackground(AZUL_SUAVE);
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(PRIMARIO, 1, true),
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
                    g2.setColor(SUPERFICIE_2);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(BORDE);
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        }

        /** Circulito de la izquierda, con check verde en la lista de inscritos. */
        private static final class Indicador extends JComponent {

            private final boolean seleccionable;

            Indicador(boolean seleccionable) {
                this.seleccionable = seleccionable;
                setPreferredSize(new Dimension(18, 18));
                setOpaque(false);
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int d = 18;
                int x = (getWidth() - d) / 2;
                int y = (getHeight() - d) / 2;
                g2.setColor(Color.WHITE);
                g2.fillOval(x, y, d, d);
                g2.setColor(seleccionable ? PRIMARIO : VERDE);
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
    }

    /** Ayuda de scroll sin marco, con barra fina, para las listas del diseno. */
    static final class ListaCourses {

        private ListaCourses() {
        }

        static JScrollPane envolver(JComponent contenido) {
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
}
