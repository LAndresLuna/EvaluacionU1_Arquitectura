package dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("FichaPago y GeneradorFolio: documento inmutable y folios consecutivos")
class FichaPagoTest {

    private static final LocalDate HOY = LocalDate.of(2026, 8, 3);

    @Test
    @DisplayName("suma el costo de los cursos y los guarda")
    void calculaElTotal() {
        FichaPago ficha = new FichaPago("FIC-2026-0001", HOY, List.of(
                new Curso("ISW-101", "Fundamentos", 3200.0),
                new Curso("ISW-204", "Arquitectura", 4500.0)));

        assertEquals(2, ficha.getCantidadCursos());
        assertEquals(7700.0, ficha.getTotal());
        assertEquals("FIC-2026-0001", ficha.getFolio());
        assertEquals(HOY, ficha.getFecha());
    }

    @Test
    @DisplayName("rechaza una lista de cursos nula")
    void rechazaCursosNulos() {
        assertThrows(IllegalArgumentException.class,
                () -> new FichaPago("FIC-2026-0001", HOY, null));
    }

    @Test
    @DisplayName("copia la lista de cursos: cambiarla despues no altera la ficha")
    void copiaLaListaDeCursos() {
        List<Curso> cursos = new ArrayList<>();
        cursos.add(new Curso("ISW-101", "Fundamentos", 3200.0));
        FichaPago ficha = new FichaPago("FIC-2026-0001", HOY, cursos);

        cursos.add(new Curso("ISW-204", "Arquitectura", 4500.0));

        assertEquals(1, ficha.getCantidadCursos());
        assertEquals(3200.0, ficha.getTotal());
        assertThrows(UnsupportedOperationException.class,
                () -> ficha.getCursos().clear());
    }

    @Test
    @DisplayName("el generador emite folios consecutivos y con el anio de la fecha")
    void generaFoliosConsecutivos() {
        GeneradorFolio folios = new GeneradorFolio();

        assertEquals("FIC-2026-0001", folios.siguiente(HOY));
        assertEquals("FIC-2026-0002", folios.siguiente(HOY));
        assertEquals("FIC-2027-0003", folios.siguiente(LocalDate.of(2027, 1, 5)));
        assertEquals(3, folios.emitidos());
    }

    @Test
    @DisplayName("cada generador lleva su propia secuencia: el folio no es estado global")
    void cadaGeneradorEsIndependiente() {
        GeneradorFolio uno = new GeneradorFolio();
        GeneradorFolio otro = new GeneradorFolio();

        uno.siguiente(HOY);
        uno.siguiente(HOY);

        assertEquals("FIC-2026-0001", otro.siguiente(HOY));
    }

    @Test
    @DisplayName("dos fichas con los mismos datos pero distinto folio no son iguales")
    void distinguePorFolio() {
        Curso curso = new Curso("ISW-101", "Fundamentos", 3200.0);
        FichaPago primera = new FichaPago("FIC-2026-0001", HOY, List.of(curso));
        FichaPago segunda = new FichaPago("FIC-2026-0002", HOY, List.of(curso));

        assertNotEquals(primera, segunda);
        assertTrue(primera.toString().contains("FIC-2026-0001"));
    }
}
