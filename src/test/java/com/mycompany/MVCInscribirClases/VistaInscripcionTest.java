package com.mycompany.MVCInscribirClases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import dto.CursoDTO;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Regresion de los botones de la ventana.
 *
 * <p>El boton "Inscribir" depende de dos cosas: lo que dice el Modelo y si hay
 * una fila resaltada. La fila resaltada es estado local de la Vista, asi que el
 * boton tiene que recalcularse tambien cuando el usuario selecciona, no solo
 * cuando el Modelo notifica. Estos tests fijan ese comportamiento: antes de
 * arreglarlo, el boton se quedaba deshabilitado para siempre y la aplicacion
 * era inutilizable.</p>
 *
 * <p>Se omiten en modo headless porque necesitan una pantalla real.</p>
 */
@DisplayName("VistaInscripcion: estado de los botones")
class VistaInscripcionTest {

    private static final String[] CURSOS = {
        "ISW-101", "ISW-204", "ISW-305", "DAT-110", "RED-150", "MET-130"};

    private ModeloInscripcion modelo;
    private ControlInscripcion control;
    private VistaInscripcion vista;

    @BeforeEach
    void armar() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "requiere un entorno grafico");
        SwingUtilities.invokeAndWait(() -> {
            modelo = new ModeloInscripcion();
            control = new ControlInscripcion(modelo);
            vista = new VistaInscripcion(modelo, control);
            modelo.suscribir(vista);
            control.iniciar();
        });
    }

    /** Pulsa un boton como lo haria el usuario, en el hilo de Swing. */
    private void pulsar(javax.swing.JButton boton) throws Exception {
        SwingUtilities.invokeAndWait(boton::doClick);
    }

    private void seleccionar(String codigo) throws Exception {
        SwingUtilities.invokeAndWait(() -> vista.listaDisponibles.seleccionar(codigo));
    }

    @Test
    @DisplayName("Inscribir arranca deshabilitado: no hay nada seleccionado")
    void inscribirArrancaDeshabilitado() {
        assertNull(vista.listaDisponibles.getCodigoSeleccionado());
        assertFalse(vista.btnInscribir.isEnabled());
    }

    @Test
    @DisplayName("seleccionar una fila habilita Inscribir")
    void seleccionarHabilitaInscribir() throws Exception {
        seleccionar("ISW-101");

        assertEquals("ISW-101", vista.listaDisponibles.getCodigoSeleccionado());
        assertTrue(vista.btnInscribir.isEnabled(),
                "elegir un curso debe habilitar el boton Inscribir");
    }

    @Test
    @DisplayName("el flujo completo funciona: seleccionar, inscribir, finalizar")
    void flujoCompleto() throws Exception {
        seleccionar("ISW-101");
        assertTrue(vista.btnInscribir.isEnabled());

        pulsar(vista.btnInscribir);
        assertEquals(1, modelo.getResumenInscripcion().cursos().size());
        assertTrue(vista.btnFinalizar.isEnabled());

        pulsar(vista.btnFinalizar);
        assertTrue(modelo.isInscripcionFinalizada());
    }

    @Test
    @DisplayName("tras inscribir, Inscribir vuelve a deshabilitarse: nada queda resaltado")
    void inscribirSeDeshabilitaTrasEnrolar() throws Exception {
        seleccionar("ISW-101");

        pulsar(vista.btnInscribir);

        assertNull(vista.listaDisponibles.getCodigoSeleccionado());
        assertFalse(vista.btnInscribir.isEnabled(),
                "el boton no debe quedar activo sin una fila resaltada");
    }

    @Test
    @DisplayName("pulsar Inscribir sin seleccion no inscribe nada")
    void pulsarSinSeleccionNoHaceNada() throws Exception {
        SwingUtilities.invokeAndWait(() -> vista.btnInscribir.setEnabled(true));

        pulsar(vista.btnInscribir);

        assertEquals(0, modelo.getResumenInscripcion().cursos().size());
    }

    @Test
    @DisplayName("Finalizar se habilita al inscribir y se bloquea al finalizar")
    void finalizarSeHabilitaYSeBloquea() throws Exception {
        assertFalse(vista.btnFinalizar.isEnabled());

        SwingUtilities.invokeAndWait(() -> control.inscribir("ISW-101"));
        assertTrue(vista.btnFinalizar.isEnabled());

        SwingUtilities.invokeAndWait(() -> control.finalizarInscripcion());
        assertFalse(vista.btnFinalizar.isEnabled());
    }

    @Test
    @DisplayName("al agotarse el catalogo, Inscribir se bloquea aunque se pida resaltar")
    void sinCatalogoNoSePuedeInscribir() throws Exception {
        SwingUtilities.invokeAndWait(() -> Arrays.stream(CURSOS)
                .forEach(control::inscribir));
        SwingUtilities.invokeAndWait(() -> vista.listaDisponibles.seleccionar("ISW-101"));

        assertEquals(List.of(), modelo.getCursosDisponibles());
        assertFalse(vista.btnInscribir.isEnabled());
    }

    @Test
    @DisplayName("la Vista no le pregunta al Controlador que puede hacer")
    void laVistaNoLePreguntaAlControlador() {
        // La disponibilidad la deriva el Modelo del estado publicado. Si alguien
        // reintroduce una consulta al Controlador, esta comprobacion lo delata.
        List<String> consultas = Arrays.stream(ControlInscripcion.class
                        .getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .map(Method::getName)
                .filter(n -> n.startsWith("puede"))
                .toList();

        assertEquals(List.of(), consultas,
                "la disponibilidad debe derivarla el Modelo, no el Controlador");
    }

    @Test
    @DisplayName("los contadores de las tarjetas reflejan el estado")
    void contadores() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            control.inscribir("ISW-101");
            control.inscribir("ISW-204");
        });

        assertEquals("4 disponibles", vista.contadorDisponibles.getText());
        assertEquals("2 inscritos", vista.contadorInscritos.getText());
    }

    @Test
    @DisplayName("el DTO de la ficha trae lo que el Modelo publico")
    void fichaPublicada() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            control.inscribir("ISW-101");
            control.finalizarInscripcion();
        });

        assertEquals(1, modelo.getFichaPago().cantidadCursos());
        List<CursoDTO> cursos = modelo.getFichaPago().cursos();
        assertEquals("ISW-101", cursos.get(0).codigo());
        assertEquals(3200.0, cursos.get(0).costo());
    }
}
