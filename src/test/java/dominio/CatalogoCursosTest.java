package dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CatalogoCursos: busqueda y retiro de cursos disponibles")
class CatalogoCursosTest {

    private CatalogoCursos catalogo;

    @BeforeEach
    void armar() {
        catalogo = CatalogoCursos.porDefecto();
    }

    @Test
    @DisplayName("el catalogo por defecto trae seis cursos")
    void catalogoPorDefecto() {
        assertEquals(6, catalogo.getCursos().size());
        assertFalse(catalogo.estaVacio());
    }

    @Test
    @DisplayName("busca por codigo ignorando mayusculas y espacios")
    void buscaPorCodigo() {
        assertTrue(catalogo.buscarPorCodigo("ISW-101").isPresent());
        assertTrue(catalogo.buscarPorCodigo("  isw-101  ").isPresent());
        assertEquals("Fundamentos de Ingeniería de Software",
                catalogo.buscarPorCodigo("ISW-101").orElseThrow().getNombre());
    }

    @Test
    @DisplayName("no encuentra un codigo inexistente, nulo ni vacio")
    void noEncuentra() {
        assertTrue(catalogo.buscarPorCodigo("XXX-999").isEmpty());
        assertTrue(catalogo.buscarPorCodigo(null).isEmpty());
        assertTrue(catalogo.buscarPorCodigo("   ").isEmpty());
    }

    @Test
    @DisplayName("retirar un curso lo quita de la lista de disponibles")
    void retira() {
        Curso curso = catalogo.buscarPorCodigo("ISW-101").orElseThrow();

        catalogo.retirar(curso);

        assertEquals(5, catalogo.getCursos().size());
        assertTrue(catalogo.buscarPorCodigo("ISW-101").isEmpty());
    }

    @Test
    @DisplayName("retirar funciona con una copia igual por valor, no con la misma instancia")
    void retiraPorValor() {
        catalogo.retirar(new Curso("ISW-101", "Fundamentos de Ingeniería de Software", 3200.0));

        assertEquals(5, catalogo.getCursos().size());
    }

    @Test
    @DisplayName("getCursos devuelve una copia inmutable")
    void getCursosEsInmutable() {
        List<Curso> copia = catalogo.getCursos();

        org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class,
                () -> copia.clear());
    }

    @Test
    @DisplayName("un catalogo vacio lo dice")
    void detectaSiEstaVacio() {
        CatalogoCursos vacio = new CatalogoCursos(List.of());

        assertTrue(vacio.estaVacio());
    }
}
