package com.mycompany.MVCInscribirClases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dto.CursoDTO;
import dto.FichaPagoDTO;
import dto.ResumenInscripcionDTO;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ModeloInscripcion: estado observable, lotes y robustez")
class ModeloInscripcionTest {

    private ModeloInscripcion modelo;
    private Contador observador;

    /** Observador de pruebas que solo cuenta las notificaciones recibidas. */
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
        observador = new Contador();
    }

    @Test
    @DisplayName("el estado inicial es vacio, no nulo")
    void estadoInicial() {
        assertEquals(List.of(), modelo.getCursosDisponibles());
        assertEquals(0, modelo.getResumenInscripcion().cursos().size());
        assertEquals("", modelo.getMensajeError());
        assertNull(modelo.getFichaPago());
        assertFalse(modelo.isInscripcionFinalizada());
    }

    @Test
    @DisplayName("suscribir notifica de inmediato el estado actual")
    void suscribirNotificaDeInmediato() {
        modelo.suscribir(observador);

        assertEquals(1, observador.veces());
    }

    @Test
    @DisplayName("suscribir dos veces el mismo observador no duplica notificaciones")
    void noDuplicaObservadores() {
        modelo.suscribir(observador);
        modelo.suscribir(observador);

        assertEquals(1, observador.veces());
    }

    @Test
    @DisplayName("un observador desuscrito ya no recibe notificaciones")
    void desuscribir() {
        modelo.suscribir(observador);
        modelo.desuscribir(observador);
        observador.reiniciar();

        modelo.setMensajeError("algo");

        assertEquals(0, observador.veces());
    }

    @Test
    @DisplayName("desuscribirse desde el propio update no rompe la notificacion")
    void desuscribirDuranteLaNotificacion() {
        IObserverInscripcion harto = new IObserverInscripcion() {
            @Override
            public void update(IModeloInscripcion m) {
                m.desuscribir(this);
            }
        };
        modelo.suscribir(observador);
        modelo.suscribir(harto);
        observador.reiniciar();

        modelo.setMensajeError("primero");
        modelo.setMensajeError("segundo");

        // El segundo update recorre una copia de la lista: desuscribirse a mitad
        // no provoca ConcurrentModificationException.
        assertEquals(2, observador.veces());
    }

    @Test
    @DisplayName("un setter con el mismo valor no notifica")
    void noNotificaSiNoCambia() {
        modelo.setCursosDisponibles(List.of(new CursoDTO("ISW-101", "Fundamentos", 3200.0)));
        modelo.suscribir(observador);
        observador.reiniciar();

        modelo.setCursosDisponibles(List.of(new CursoDTO("ISW-101", "Fundamentos", 3200.0)));

        assertEquals(0, observador.veces());
    }

    @Test
    @DisplayName("enLote agrupa varios cambios en una sola notificacion")
    void enLoteNotificaUnaVez() {
        modelo.suscribir(observador);
        observador.reiniciar();

        modelo.enLote(() -> {
            modelo.setCursosDisponibles(List.of(new CursoDTO("ISW-101", "Fundamentos", 3200.0)));
            modelo.setResumenInscripcion(
                    new ResumenInscripcionDTO(List.of(), 3200.0, true));
            modelo.setFichaPago(new FichaPagoDTO("FIC-2026-0001", null, List.of(), 3200.0));
            modelo.setMensajeError("");
        });

        assertEquals(1, observador.veces());
    }

    @Test
    @DisplayName("enLote entrega un estado ya completo y coherente")
    void enLoteEntregaUnEstadoCompleto() {
        modelo.suscribir(observador);
        observador.reiniciar();

        modelo.enLote(() -> {
            modelo.setResumenInscripcion(
                    new ResumenInscripcionDTO(List.of(), 3200.0, true));
            modelo.setFichaPago(new FichaPagoDTO("FIC-2026-0001", null, List.of(), 3200.0));
        });

        // Lo que recibe el observador ya tiene la inscripcion cerrada y la ficha
        // emitida: nunca una ficha huérfana con el resumen todavía abierto.
        IModeloInscripcion recibido = observador.recibidos.get(0);
        assertTrue(recibido.isInscripcionFinalizada());
        assertNotNull(recibido.getFichaPago());
    }

    @Test
    @DisplayName("los lotes anidados notifican una sola vez al cerrarse el externo")
    void lotesAnidados() {
        modelo.suscribir(observador);
        observador.reiniciar();

        modelo.enLote(() -> {
            modelo.setMensajeError("uno");
            modelo.enLote(() -> modelo.setResumenInscripcion(
                    new ResumenInscripcionDTO(List.of(), 1.0, false)));
            assertEquals(0, observador.veces());
        });

        assertEquals(1, observador.veces());
    }

    @Test
    @DisplayName("un lote que no cambia nada no notifica")
    void loteSinCambiosNoNotifica() {
        modelo.suscribir(observador);
        observador.reiniciar();

        modelo.enLote(() -> modelo.setMensajeError(""));

        assertEquals(0, observador.veces());
    }

    @Test
    @DisplayName("ningun setter acepta null: guarda el equivalente vacio")
    void settersToleranNull() {
        modelo.setCursosDisponibles(null);
        modelo.setResumenInscripcion(null);
        modelo.setMensajeError(null);

        assertEquals(List.of(), modelo.getCursosDisponibles());
        assertEquals(0, modelo.getResumenInscripcion().cursos().size());
        assertEquals("", modelo.getMensajeError());
    }

    @Test
    @DisplayName("getCursosDisponibles no se puede modificar desde fuera")
    void listaExpuestaEsInmutable() {
        modelo.setCursosDisponibles(List.of(new CursoDTO("ISW-101", "Fundamentos", 3200.0)));

        org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class,
                () -> modelo.getCursosDisponibles().clear());
    }

    @Test
    @DisplayName("puedeInscribir y puedeFinalizar se derivan del estado publicado")
    void disponibilidadDerivada() {
        // Antes de que el Controlador publique nada no hay catalogo, asi que no
        // se puede inscribir: la respuesta sale del estado, no de una regla
        // aparte que se pueda desincronizar.
        assertFalse(modelo.puedeInscribir());
        assertFalse(modelo.puedeFinalizar());

        modelo.setCursosDisponibles(List.of(new CursoDTO("ISW-101", "Fundamentos", 3200.0)));
        modelo.setResumenInscripcion(new ResumenInscripcionDTO(
                List.of(new CursoDTO("ISW-101", "Fundamentos", 3200.0)), 3200.0, false));

        assertTrue(modelo.puedeInscribir());
        assertTrue(modelo.puedeFinalizar());

        modelo.setCursosDisponibles(List.of());
        assertFalse(modelo.puedeInscribir());

        modelo.setResumenInscripcion(new ResumenInscripcionDTO(
                List.of(new CursoDTO("ISW-101", "Fundamentos", 3200.0)), 3200.0, true));

        assertFalse(modelo.puedeInscribir());
        assertFalse(modelo.puedeFinalizar());
    }
}
