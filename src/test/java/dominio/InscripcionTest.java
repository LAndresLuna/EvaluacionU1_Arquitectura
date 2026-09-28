package dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Inscripcion: reglas del caso de uso")
class InscripcionTest {

    private static final LocalDate HOY = LocalDate.of(2026, 8, 3);

    private Inscripcion inscripcion;
    private Curso isw101;
    private Curso isw204;

    @BeforeEach
    void armar() {
        inscripcion = new Inscripcion();
        isw101 = new Curso("ISW-101", "Fundamentos", 3200.0);
        isw204 = new Curso("ISW-204", "Arquitectura", 4500.0);
    }

    @Test
    @DisplayName("una inscripcion nueva esta vacia y abierta")
    void estadoInicial() {
        assertEquals(0, inscripcion.getCantidadCursos());
        assertEquals(0.0, inscripcion.getTotal());
        assertFalse(inscripcion.isFinalizada());
    }

    @Test
    @DisplayName("inscribe un curso y suma su costo al total")
    void inscribirAcumulaElTotal() {
        inscripcion.inscribir(isw101);
        inscripcion.inscribir(isw204);

        assertEquals(2, inscripcion.getCantidadCursos());
        assertEquals(7700.0, inscripcion.getTotal());
        assertEquals(List.of(isw101, isw204), inscripcion.getCursos());
    }

    @Test
    @DisplayName("no inscribe un curso nulo")
    void rechazaCursoNulo() {
        assertThrows(ExcepcionInscripcion.class, () -> inscripcion.inscribir(null));
        assertEquals(0, inscripcion.getCantidadCursos());
    }

    @Test
    @DisplayName("no inscribe dos veces el mismo curso")
    void rechazaCursoRepetido() {
        inscripcion.inscribir(isw101);

        // Otra instancia con el mismo codigo: el alumno no puede llevar dos
        // veces la misma materia aunque sea un objeto distinto.
        ExcepcionInscripcion error = assertThrows(ExcepcionInscripcion.class,
                () -> inscripcion.inscribir(new Curso("ISW-101", "Fundamentos", 3200.0)));

        assertTrue(error.getMessage().contains("ya está inscrito"));
        assertEquals(1, inscripcion.getCantidadCursos());
    }

    @Test
    @DisplayName("no inscribe despues de finalizar")
    void rechazaInscribirTrasFinalizar() {
        inscripcion.inscribir(isw101);
        inscripcion.finalizar(HOY, "FIC-2026-0001");

        assertThrows(ExcepcionInscripcion.class, () -> inscripcion.inscribir(isw204));
        assertEquals(1, inscripcion.getCantidadCursos());
    }

    @Test
    @DisplayName("no genera ficha de pago sin cursos")
    void rechazaFinalizarSinCursos() {
        ExcepcionInscripcion error = assertThrows(ExcepcionInscripcion.class,
                () -> inscripcion.finalizar(HOY, "FIC-2026-0001"));

        assertTrue(error.getMessage().contains("al menos un curso"));
        assertFalse(inscripcion.isFinalizada());
    }

    @Test
    @DisplayName("no genera dos veces la ficha de pago")
    void rechazaFinalizarDosVeces() {
        inscripcion.inscribir(isw101);
        inscripcion.finalizar(HOY, "FIC-2026-0001");

        assertThrows(ExcepcionInscripcion.class,
                () -> inscripcion.finalizar(HOY, "FIC-2026-0002"));
    }

    @Test
    @DisplayName("finalizar cierra la inscripcion y emite la ficha con el folio recibido")
    void finalizarEmiteLaFicha() {
        inscripcion.inscribir(isw101);
        FichaPago ficha = inscripcion.finalizar(HOY, "FIC-2026-0001");

        assertTrue(inscripcion.isFinalizada());
        assertEquals("FIC-2026-0001", ficha.getFolio());
        assertEquals(HOY, ficha.getFecha());
        assertEquals(1, ficha.getCantidadCursos());
        assertEquals(3200.0, ficha.getTotal());
    }

    @Test
    @DisplayName("getCursos devuelve una copia: el exterior no puede alterarla")
    void getCursosEsUnaCopia() {
        inscripcion.inscribir(isw101);

        List<Curso> copia = inscripcion.getCursos();

        assertThrows(UnsupportedOperationException.class,
                () -> copia.add(isw204));
    }

    @Test
    @DisplayName("yaEstaInscrito responde por codigo, no por identidad")
    void buscaPorCodigo() {
        assertFalse(inscripcion.yaEstaInscrito(isw101));
        inscripcion.inscribir(isw101);
        assertTrue(inscripcion.yaEstaInscrito(new Curso("isw-101", "Otro nombre", 1.0)));
        assertFalse(inscripcion.yaEstaInscrito(null));
    }
}
