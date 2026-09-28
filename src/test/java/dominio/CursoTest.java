package dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Curso: validacion e igualdad por valor")
class CursoTest {

    @Test
    @DisplayName("guarda los datos sin espacios sobrantes")
    void recortaLosDatos() {
        Curso curso = new Curso("  ISW-101  ", "  Fundamentos  ", 3200.0);

        assertEquals("ISW-101", curso.getCodigo());
        assertEquals("Fundamentos", curso.getNombre());
        assertEquals(3200.0, curso.getCosto());
    }

    @ParameterizedTest(name = "rechaza el codigo \"{0}\"")
    @ValueSource(strings = {"", "   "})
    @DisplayName("rechaza un codigo vacio")
    void rechazaCodigoVacio(String codigo) {
        assertThrows(IllegalArgumentException.class, () -> new Curso(codigo, "Curso", 100.0));
    }

    @Test
    @DisplayName("rechaza un codigo nulo")
    void rechazaCodigoNulo() {
        assertThrows(IllegalArgumentException.class, () -> new Curso(null, "Curso", 100.0));
    }

    @Test
    @DisplayName("rechaza un nombre vacio")
    void rechazaNombreVacio() {
        assertThrows(IllegalArgumentException.class, () -> new Curso("ISW-101", "  ", 100.0));
    }

    @ParameterizedTest(name = "rechaza el costo {0}")
    @ValueSource(doubles = {0.0, -1.0, -0.01})
    @DisplayName("rechaza un costo no positivo")
    void rechazaCostoNoPositivo(double costo) {
        assertThrows(IllegalArgumentException.class, () -> new Curso("ISW-101", "Curso", costo));
    }

    @Test
    @DisplayName("dos cursos con los mismos datos son iguales")
    void igualdadPorValor() {
        Curso a = new Curso("ISW-101", "Fundamentos", 3200.0);
        Curso b = new Curso("ISW-101", "Fundamentos", 3200.0);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("el codigo se compara sin distinguir mayusculas")
    void igualdadIgnoraMayusculas() {
        Curso a = new Curso("ISW-101", "Fundamentos", 3200.0);
        Curso b = new Curso("isw-101", "Fundamentos", 3200.0);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("cursos distintos no son iguales")
    void desigualdad() {
        Curso base = new Curso("ISW-101", "Fundamentos", 3200.0);

        assertNotEquals(base, new Curso("ISW-204", "Fundamentos", 3200.0));
        assertNotEquals(base, new Curso("ISW-101", "Arquitectura", 3200.0));
        assertNotEquals(base, new Curso("ISW-101", "Fundamentos", 4500.0));
        assertNotEquals(base, "ISW-101");
    }

    @Test
    @DisplayName("el toString muestra codigo y nombre")
    void representacion() {
        assertEquals("ISW-101 - Fundamentos",
                new Curso("ISW-101", "Fundamentos", 3200.0).toString());
    }

    @Test
    @DisplayName("el curso es inmutable: no tiene setters")
    void inmutable() {
        assertTrue(java.util.Arrays.stream(Curso.class.getMethods())
                .noneMatch(m -> m.getName().startsWith("set")));
    }
}
