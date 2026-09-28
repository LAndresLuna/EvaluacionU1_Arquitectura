package dominio;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Emite folios consecutivos de ficha de pago, por ejemplo {@code FIC-2026-0001}.
 *
 * <p>Cada instancia lleva su propia secuencia: el folio no es estado global y
 * las pruebas pueden usar el suyo para saber exactamente qué sale.</p>
 *
 * @author andres
 */
public final class GeneradorFolio {

    private final AtomicInteger secuencia = new AtomicInteger();

    public String siguiente(LocalDate fecha) {
        int anio = fecha == null ? LocalDate.now().getYear() : fecha.getYear();
        return String.format("FIC-%d-%04d", anio, secuencia.incrementAndGet());
    }

    public int emitidos() {
        return secuencia.get();
    }
}
