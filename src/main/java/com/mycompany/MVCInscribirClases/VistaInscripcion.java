package com.mycompany.MVCInscribirClases;

import com.mycompany.MVCInscribirClases.presentacion.Boton;
import com.mycompany.MVCInscribirClases.presentacion.Conector;
import com.mycompany.MVCInscribirClases.presentacion.Iconos;
import com.mycompany.MVCInscribirClases.presentacion.ListaCursos;
import com.mycompany.MVCInscribirClases.presentacion.Paleta;
import com.mycompany.MVCInscribirClases.presentacion.PanelRedondeado;
import com.mycompany.MVCInscribirClases.presentacion.Paso;
import com.mycompany.MVCInscribirClases.presentacion.Pastilla;
import dto.CursoDTO;
import dto.FichaPagoDTO;
import dto.ResumenInscripcionDTO;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Vista del patrón MVC: la ventana de "Inscribirse en Clases".
 *
 * <p>Es la única pieza que conoce a las otras dos, y siempre en un solo sentido:
 * le pide acciones al Controlador y consume únicamente los DTOs que el Modelo le
 * entrega por sus getters cada vez que la notifica. Nunca le pregunta al
 * Controlador por el estado; lo resuelve con
 * {@link IModeloInscripcion#puedeInscribir()} y
 * {@link IModeloInscripcion#puedeFinalizar()}.</p>
 *
 * <p>El campo {@code modelo} está declarado como {@link IModeloInscripcion}, que
 * solo expone getters y setters. Es deliberado: al no tener las referencias del
 * cableado ({@code suscribir}, {@code enLote}) ni la clase concreta, esta clase
 * no puede escribir el estado aunque alguien lo intente. No calcula totales ni
 * valida reglas: solo pinta lo que le llegó ya resuelto en los DTOs.</p>
 *
 * <p>El estilo y las medidas viven en {@link Paleta} y en el paquete
 * {@code presentacion}, para poder ajustarlos en un solo lugar.</p>
 *
 * @author andres
 */
public class VistaInscripcion extends JFrame implements IObserverInscripcion {

    private static final int ANCHO_FICHA = 600;

    /* Datos fijos del storyboard (no vienen del modelo) */
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

    /* Los widgets que las pruebas observan son visibles a nivel de paquete, en vez
     * de privados: así el test lee el estado real de la pantalla sin Reflection,
     * que es frágil y se rompe en cuanto cambia un nombre. */
    final IModeloInscripcion modelo;
    final ControlInscripcion control;

    final ListaCursos listaDisponibles = new ListaCursos(true);
    final ListaCursos listaInscritos = new ListaCursos(false);

    private final CardLayout centroDerecho = new CardLayout();
    private final JPanel panelDerecho = new JPanel(centroDerecho);

    final JLabel contadorDisponibles = new JLabel();
    final JLabel contadorInscritos = new JLabel();
    private final JLabel lblParcial = new JLabel();
    private final JLabel lblResumen = new JLabel();
    private final JLabel lblTotal = new JLabel();
    private final JLabel lblEstado = new JLabel();
    final JButton btnInscribir = new Boton("Inscribir", false);
    final JButton btnFinalizar = new Boton("Finalizar Inscripción →", true);
    private final PanelFichaPago panelFichaPago = new PanelFichaPago();

    private JDialog ventanaFicha;
    private boolean fichaMostrada;

    public VistaInscripcion(IModeloInscripcion modelo, ControlInscripcion control) {
        this.modelo = Objects.requireNonNull(modelo, "modelo");
        this.control = Objects.requireNonNull(control, "control");
        listaDisponibles.setListener(this::actualizarBotones);

        setTitle("Inscripción en Clases — Portal Académico");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1080, 660));
        setSize(new Dimension(1280, 800));
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        getContentPane().setBackground(Paleta.FONDO);
        add(crearBarraSuperior(), BorderLayout.NORTH);
        add(crearCuerpo(), BorderLayout.CENTER);
        add(crearBarraInferior(), BorderLayout.SOUTH);

        configurarEstilos();
        configurarEventos();
    }

    public void iniciar() {
        setVisible(true);
    }

    /* observer */

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

    private void refrescar(IModeloInscripcion modelo) {
        List<CursoDTO> disponibles = modelo.getCursosDisponibles();
        ResumenInscripcionDTO resumen = modelo.getResumenInscripcion();
        FichaPagoDTO ficha = modelo.getFichaPago();

        listaDisponibles.setCursos(disponibles);
        listaInscritos.setCursos(resumen.cursos());

        int disponiblesCantidad = disponibles.size();
        contadorDisponibles.setText(disponiblesCantidad
                + (disponiblesCantidad == 1 ? " disponible" : " disponibles"));
        contadorInscritos.setText(plural(resumen.cantidadCursos(), "inscrito", "inscritos"));
        lblParcial.setText(Paleta.formatearMonto(resumen.total()));
        lblResumen.setText(plural(resumen.cantidadCursos(), "curso inscrito", "cursos inscritos"));
        lblTotal.setText(Paleta.formatearMonto(resumen.total()));

        centroDerecho.show(panelDerecho,
                resumen.cantidadCursos() == 0 ? TARJETA_VACIA : TARJETA_LISTA);

        actualizarBotones();
        actualizarEstado(resumen, ficha);

        if (ficha != null && !fichaMostrada) {
            fichaMostrada = true;
            panelFichaPago.mostrar(ficha);
            SwingUtilities.invokeLater(this::abrirFicha);
        }
    }

    /**
     * "Inscribir" necesita dos cosas: que el Modelo diga que se puede inscribir y
     * que el usuario tenga una fila resaltada. La selección es estado local de la
     * Vista, así que este método se llama tanto desde {@link #refrescar} como
     * desde el aviso de selección de {@link ListaCursos}: si solo se llamara
     * desde el refresco, el botón nunca se habilitaría, porque el Modelo no
     * notifica cuando cambia la selección.
     */
    private void actualizarBotones() {
        boolean haySeleccion = listaDisponibles.getCodigoSeleccionado() != null;
        btnInscribir.setEnabled(modelo.puedeInscribir() && haySeleccion);
        btnFinalizar.setEnabled(modelo.puedeFinalizar());
    }

    private void actualizarEstado(ResumenInscripcionDTO resumen, FichaPagoDTO ficha) {
        String error = modelo.getMensajeError();
        if (error != null && !error.isBlank()) {
            lblEstado.setText(error);
            lblEstado.setForeground(Paleta.ERROR);
            return;
        }
        if (ficha != null) {
            lblEstado.setText("Inscripción finalizada. Ficha de pago " + ficha.folio());
            lblEstado.setForeground(Paleta.VERDE);
            return;
        }
        lblEstado.setForeground(Paleta.TEXTO_3);
        if (resumen.cantidadCursos() == 0) {
            lblEstado.setText("Seleccione un curso de la lista de disponibles para inscribirlo.");
        } else {
            lblEstado.setText("Puedes modificar tu selección antes de finalizar.");
        }
    }

    private static String plural(int cantidad, String singular, String plural) {
        return cantidad + (cantidad == 1 ? " " + singular : " " + plural);
    }

    /* ==================================================== construccion === */

    private JPanel crearBarraSuperior() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(Paleta.SUPERFICIE);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Paleta.BORDE),
                BorderFactory.createEmptyBorder(14, 24, 14, 24)));

        barra.add(crearMarca(), BorderLayout.WEST);
        barra.add(crearZonaUsuario(), BorderLayout.EAST);
        return barra;
    }

    private JPanel crearMarca() {
        JPanel marca = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        marca.setOpaque(false);
        marca.add(new Iconos.Monograma());

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(Paleta.etiqueta(UNIVERSIDAD, 15, Font.BOLD, Paleta.TEXTO));
        textos.add(Box.createVerticalStrut(2));
        textos.add(Paleta.espaciada(UNIVERSIDAD_SUB, 10, Paleta.TEXTO_2));
        marca.add(textos);
        return marca;
    }

    private JPanel crearZonaUsuario() {
        JPanel zona = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        zona.setOpaque(false);
        zona.add(Pastilla.de(SEMESTRE, false));

        JPanel divisor = new JPanel();
        divisor.setPreferredSize(new Dimension(1, 30));
        divisor.setBackground(Paleta.BORDE);
        zona.add(divisor);

        zona.add(new Iconos.Iniciales(INICIALES));

        JPanel datos = new JPanel();
        datos.setOpaque(false);
        datos.setLayout(new BoxLayout(datos, BoxLayout.Y_AXIS));
        datos.add(Paleta.etiqueta(USUARIO, 13, Font.BOLD, Paleta.TEXTO));
        datos.add(Box.createVerticalStrut(1));
        datos.add(Paleta.etiqueta(USUARIO_SUB, 11, Font.PLAIN, Paleta.TEXTO_3));
        zona.add(datos);
        zona.add(new Iconos.Triangulo());
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
        textos.add(Paleta.espaciada(SECCION.toUpperCase(), 11, Paleta.PRIMARIO, 0.09f));
        textos.add(Box.createVerticalStrut(6));
        textos.add(Paleta.etiqueta(TITULO, 28, Font.BOLD, Paleta.TEXTO));
        textos.add(Box.createVerticalStrut(6));
        textos.add(Paleta.etiqueta(PERIODO, 13, Font.PLAIN, Paleta.TEXTO_2));
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
        JPanel columnas = new JPanel(new java.awt.GridLayout(1, 2, 22, 0));
        columnas.setOpaque(false);
        columnas.add(crearTarjetaDisponibles());
        columnas.add(crearTarjetaInscritos());
        return columnas;
    }

    private JPanel crearTarjetaDisponibles() {
        PanelRedondeado tarjeta = new PanelRedondeado(Paleta.RADIO, Paleta.SUPERFICIE,
                Paleta.BORDE);
        tarjeta.setLayout(new BorderLayout(0, 0));
        tarjeta.add(crearEncabezadoTarjeta("Cursos Disponibles", "Selecciona un curso para inscribir",
                contadorDisponibles, false), BorderLayout.NORTH);
        tarjeta.add(ListaCursos.enScrollPane(listaDisponibles), BorderLayout.CENTER);
        tarjeta.add(crearPieInscribir(), BorderLayout.SOUTH);
        return tarjeta;
    }

    private JPanel crearTarjetaInscritos() {
        PanelRedondeado tarjeta = new PanelRedondeado(Paleta.RADIO, Paleta.SUPERFICIE,
                Paleta.BORDE);
        tarjeta.setLayout(new BorderLayout(0, 0));
        tarjeta.add(crearEncabezadoTarjeta("Cursos Inscritos", "Tu carga académica actual",
                contadorInscritos, true), BorderLayout.NORTH);

        panelDerecho.setOpaque(false);
        panelDerecho.add(ListaCursos.enScrollPane(listaInscritos), TARJETA_LISTA);
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
                BorderFactory.createMatteBorder(0, 0, 1, 0, Paleta.BORDE),
                BorderFactory.createEmptyBorder(18, 20, 14, 20)));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(Paleta.etiqueta(titulo, 17, Font.BOLD, Paleta.TEXTO));
        textos.add(Box.createVerticalStrut(3));
        textos.add(Paleta.etiqueta(subtitulo, 11, Font.PLAIN, Paleta.TEXTO_3));
        cabecera.add(textos, BorderLayout.WEST);
        cabecera.add(Pastilla.de(contador, verde), BorderLayout.EAST);
        return cabecera;
    }

    private JPanel crearPieInscribir() {
        JPanel pie = new JPanel(new BorderLayout(0, 0));
        pie.setOpaque(false);
        pie.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Paleta.BORDE),
                BorderFactory.createEmptyBorder(16, 20, 18, 20)));

        btnInscribir.setFont(Paleta.fuente(13, Font.BOLD));
        btnInscribir.setAlignmentX(0.5f);
        btnInscribir.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnInscribir.setPreferredSize(new Dimension(10, 40));

        JPanel acciones = new JPanel();
        acciones.setOpaque(false);
        acciones.setLayout(new BoxLayout(acciones, BoxLayout.Y_AXIS));
        acciones.add(btnInscribir);
        acciones.add(Box.createVerticalStrut(8));
        JLabel pista = Paleta.etiqueta(PISTA_INSCRIBIR, 11, Font.PLAIN, Paleta.TEXTO_2,
                SwingConstants.CENTER);
        pista.setAlignmentX(0.5f);
        acciones.add(pista);
        pie.add(acciones, BorderLayout.CENTER);
        return pie;
    }

    private JPanel crearPieResumen() {
        JPanel pie = new JPanel(new BorderLayout(0, 0));
        pie.setOpaque(false);
        pie.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Paleta.BORDE),
                BorderFactory.createEmptyBorder(16, 20, 18, 20)));

        lblParcial.setFont(Paleta.fuente(20, Font.BOLD));
        lblParcial.setForeground(Paleta.TEXTO);
        lblResumen.setFont(Paleta.fuente(11, Font.PLAIN));
        lblResumen.setForeground(Paleta.TEXTO_3);

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
        Iconos.Ayuda icono = new Iconos.Ayuda();
        icono.setAlignmentX(0.5f);
        textos.add(icono);
        textos.add(Box.createVerticalStrut(14));
        JLabel mensaje = Paleta.etiqueta("<html><div style='text-align:center;width:260px'>"
                + PISTA_VACIA + "</div></html>", 12, Font.PLAIN, Paleta.TEXTO_2,
                SwingConstants.CENTER);
        mensaje.setAlignmentX(0.5f);
        textos.add(mensaje);

        vacio.add(textos);
        return vacio;
    }

    private JPanel crearBarraInferior() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(Paleta.SUPERFICIE);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Paleta.BORDE),
                BorderFactory.createEmptyBorder(16, 24, 16, 24)));

        lblEstado.setFont(Paleta.fuente(12, Font.PLAIN));

        JPanel total = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        total.setOpaque(false);
        JLabel etiquetaTotal = Paleta.etiqueta("Total a pagar:", 12, Font.PLAIN, Paleta.TEXTO_3);
        etiquetaTotal.setAlignmentY(SwingConstants.CENTER);
        lblTotal.setFont(Paleta.fuente(24, Font.BOLD));
        lblTotal.setForeground(Paleta.TEXTO);
        total.add(etiquetaTotal);
        total.add(lblTotal);
        total.add(Box.createHorizontalStrut(8));
        btnFinalizar.setFont(Paleta.fuente(13, Font.BOLD));
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
        fondo.setBackground(Paleta.SCRIM);
        fondo.setBorder(BorderFactory.createEmptyBorder(48, 48, 48, 48));

        PanelRedondeado tarjeta = new PanelRedondeado(16, Paleta.SUPERFICIE, null);
        tarjeta.setLayout(new BorderLayout(0, 0));
        tarjeta.add(crearEncabezadoFicha(), BorderLayout.NORTH);
        tarjeta.add(crearCuerpoFicha(), BorderLayout.CENTER);
        tarjeta.add(crearPieFicha(), BorderLayout.SOUTH);
        tarjeta.setPreferredSize(new Dimension(ANCHO_FICHA,
                tarjeta.getPreferredSize().height));
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
                BorderFactory.createMatteBorder(0, 0, 1, 0, Paleta.BORDE),
                BorderFactory.createEmptyBorder(20, 24, 18, 24)));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(Paleta.etiqueta("Ficha de Pago", 20, Font.BOLD, Paleta.TEXTO));
        textos.add(Box.createVerticalStrut(3));
        textos.add(Paleta.etiqueta("Resumen de inscripción · " + SEMESTRE, 11, Font.PLAIN,
                Paleta.TEXTO_3));
        cabecera.add(textos, BorderLayout.WEST);

        Iconos.Cerrar cerrar = new Iconos.Cerrar();
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
        cuerpo.setBorder(BorderFactory.createEmptyBorder(20, 24, 0, 24));
        cuerpo.add(panelFichaPago, BorderLayout.CENTER);
        return cuerpo;
    }

    private JPanel crearPieFicha() {
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pie.setOpaque(false);
        pie.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Paleta.BORDE),
                BorderFactory.createEmptyBorder(16, 24, 20, 24)));
        JButton cerrar = new Boton("Cerrar", false);
        cerrar.setFont(Paleta.fuente(13, Font.BOLD));
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
        contadorDisponibles.setForeground(Paleta.PRIMARIO);
        contadorInscritos.setForeground(Paleta.VERDE);
    }
}
