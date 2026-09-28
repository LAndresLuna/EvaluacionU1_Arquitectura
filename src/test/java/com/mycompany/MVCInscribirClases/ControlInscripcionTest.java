package com.mycompany.MVCInscribirClases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dominio.CatalogoCursos;
import dominio.Curso;
import dominio.GeneradorFolio;
import dto.CursoDTO;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ControlInscripcion: orquesta el caso de uso completo")
class ControlInscripcionTest {

    private ModeloInscripcion modelo;
    private ControlInscripcion control;
    private Contador observador;

    /** Observador de pruebas que ademas guarda el estado de cada notificacion. */
    private static final class Contador implements IObserverInscripcion {
        private final List<IModeloInscripcion> recibidos = new ArrayList<>();

        @Override
        public void update(IModeloInscripcion modelo) {
            recibidos.add(modelo);
        }

        int veces() {
            return recibidos.size();
        }

        void reiniciar() {
            recibidos.clear();
        }
    }

    @BeforeEach
    void armar() {
        modelo = new ModeloInscripcion();
        control = new ControlInscripcion(modelo);
        observador = new Contador();
    }

    @Test
    @DisplayName("iniciar publica el catalogo completo y la inscripcion vacia")
    void iniciar() {
        control.iniciar();

        assertEquals(6, modelo.getCursosDisponibles().size());
        assertEquals(0, modelo.getResumenInscripcion().cursos().size());
        assertEquals(0.0, modelo.getResumenInscripcion().total());
        assertNull(modelo.getFichaPago());
    }

    @Test
    @DisplayName("inscribir mueve el curso del catalogo a la inscripcion y suma el total")
    void inscribir() {
        control.iniciar();

        control.inscribir("ISW-101");

        assertEquals(5, modelo.getCursosDisponibles().size());
        assertEquals(1, modelo.getResumenInscripcion().cursos().size());
        assertEquals("ISW-101", modelo.getResumenInscripcion().cursos().get(0).codigo());
        assertEquals(3200.0, modelo.getResumenInscripcion().total());
        assertEquals("", modelo.getMensajeError());
    }

    @Test
    @DisplayName("inscribir acepta el codigo con espacios y minusculas")
    void inscribeConCodigoFlojo() {
        control.iniciar();

        control.inscribir("  isw-101  ");

        assertEquals(1, modelo.getResumenInscripcion().cursos().size());
    }

    @Test
    @DisplayName("un curso inexistente publica un mensaje de error y no cambia el estado")
    void rechazaCodigoInexistente() {
        control.iniciar();

        control.inscribir("XXX-999");

        assertTrue(modelo.getMensajeError().contains("no está disponible"));
        assertEquals(6, modelo.getCursosDisponibles().size());
        assertEquals(0, modelo.getResumenInscripcion().cursos().size());
    }

    @Test
    @DisplayName("un codigo nulo o vacio se ignora en silencio")
    void ignoraCodigoVacio() {
        control.iniciar();

        control.inscribir(null);
        control.inscribir("   ");

        assertEquals(6, modelo.getCursosDisponibles().size());
        assertEquals("", modelo.getMensajeError());
    }

    @Test
    @DisplayName("cada evento del usuario produce una sola notificacion")
    void unaNotificacionPorEvento() {
        control.iniciar();
        modelo.suscribir(observador);
        observador.reiniciar();

        control.inscribir("ISW-101");

        assertEquals(1, observador.veces());
    }

    @Test
    @DisplayName("finalizar emite la ficha y cierra la inscripcion")
    void finalizar() {
        control.iniciar();
        control.inscribir("ISW-101");
        control.inscribir("ISW-204");

        control.finalizarInscripcion();

        assertTrue(modelo.isInscripcionFinalizada());
        assertNotNull(modelo.getFichaPago());
        assertEquals(2, modelo.getFichaPago().cantidadCursos());
        assertEquals(7700.0, modelo.getFichaPago().total());
        assertTrue(modelo.getFichaPago().folio().startsWith("FIC-"));
        assertEquals("", modelo.getMensajeError());
    }

    @Test
    @DisplayName("la notificacion de cierre ya trae la inscripcion cerrada y la ficha")
    void elCierreNotificaUnEstadoCompleto() {
        control.iniciar();
        control.inscribir("ISW-101");
        modelo.suscribir(observador);
        observador.reiniciar();

        control.finalizarInscripcion();

        assertEquals(1, observador.veces());
        IModeloInscripcion recibido = observador.recibidos.get(0);
        assertTrue(recibido.isInscripcionFinalizada());
        assertNotNull(recibido.getFichaPago());
    }

    @Test
    @DisplayName("no se puede finalizar sin cursos")
    void rechazaFinalizarSinCursos() {
        control.iniciar();

        control.finalizarInscripcion();

        assertNull(modelo.getFichaPago());
        assertFalse(modelo.isInscripcionFinalizada());
    }

    @Test
    @DisplayName("no se puede inscribir despues de finalizar")
    void rechazaInscribirTrasFinalizar() {
        control.iniciar();
        control.inscribir("ISW-101");
        control.finalizarInscripcion();

        control.inscribir("ISW-204");

        assertEquals(1, modelo.getResumenInscripcion().cursos().size());
        assertTrue(modelo.getMensajeError().contains("ya fue finalizada"));
    }

    @Test
    @DisplayName("finalizar dos veces no regenera la ficha")
    void rechazaFinalizarDosVeces() {
        control.iniciar();
        control.inscribir("ISW-101");
        control.finalizarInscripcion();
        String folio = modelo.getFichaPago().folio();

        control.finalizarInscripcion();

        assertEquals(folio, modelo.getFichaPago().folio());
    }

    @Test
    @DisplayName("al agotar el catalogo ya no se puede inscribir")
    void agotaElCatalogo() {
        control.iniciar();
        CatalogoCursos.porDefecto().getCursos()
                .forEach(curso -> control.inscribir(curso.getCodigo()));

        assertEquals(0, modelo.getCursosDisponibles().size());
        assertEquals(6, modelo.getResumenInscripcion().cursos().size());
        assertFalse(modelo.puedeInscribir());
        assertTrue(modelo.puedeFinalizar());
    }

    @Test
    @DisplayName("el generador de folios inyectado fija el folio de la ficha")
    void usaElGeneradorInyectado() {
        ModeloInscripcion otroModelo = new ModeloInscripcion();
        ControlInscripcion otroControl = new ControlInscripcion(otroModelo,
                CatalogoCursos.porDefecto(), new GeneradorFolio());
        otroControl.iniciar();
        otroControl.inscribir("ISW-101");
        otroControl.inscribir("ISW-204");

        otroControl.finalizarInscripcion();

        // Un generador propio arranca en 1, independiente del estado del resto.
        assertEquals("FIC-" + java.time.LocalDate.now().getYear() + "-0001",
                otroModelo.getFichaPago().folio());
    }

    @Test
    @DisplayName("acepta un catalogo propio")
    void aceptaCatalogoPropio() {
        ModeloInscripcion otroModelo = new ModeloInscripcion();
        ControlInscripcion otroControl = new ControlInscripcion(otroModelo,
                List.of(new Curso("TEST-1", "Curso de prueba", 1000.0)));
        otroControl.iniciar();

        assertEquals(1, otroModelo.getCursosDisponibles().size());
        otroControl.inscribir("TEST-1");

        CursoDTO inscrito = otroModelo.getResumenInscripcion().cursos().get(0);
        assertEquals("TEST-1", inscrito.codigo());
        assertEquals(1000.0, inscrito.costo());
    }
}
